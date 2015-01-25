/*
 * Copyright 2012 J. Patrick Meyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.polycor;

import com.itemanalysis.psychometrics.analysis.AbstractMultivariateFunction;
import com.itemanalysis.psychometrics.distribution.BivariateNormalDistributionImpl;
import com.itemanalysis.psychometrics.uncmin.Uncmin_methods;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;

import java.util.Arrays;
import java.util.Formatter;

/**
 * Loglikelihood and other methods for computing the maximum likelihood
 * estimate of the polychoric correlation.
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class PolychoricLogLikelihoodML extends AbstractMultivariateFunction implements Uncmin_methods {

    private double[][] data = null; //a k x k matrix of frequencies
    private int nrow = 0;//number of rows
    private int ncol = 0;//number of columns
    private int validNrow = 0; //nrow-1 the highest category is not estimable and is not part of optimization parameters
    private int validNcol = 0;//ncol-1  the highest category is not estimable and is not part of optimization parameters
    private double[] alpha = null;//
    private double[] beta = null;
    private double rho = 0.0;
    private double[][] variance = null;
    private double chiSquare = 0.0;
    private double df = 0.0;
    private double probChiSquare = 0.0;
    private double maxcor = 0.9999;
    BivariateNormalDistributionImpl bvnorm = null;

    public void hessian(double[] x, double[][] y){

    }

    public void gradient(double[] x, double[] y){

    }

    public double f_to_minimize(double[] x){
        double[] shortX = new double[x.length-1];
        for(int i=0;i<shortX.length;i++){
            shortX[i] = x[i+1];
        }
        return value(x);
    }

    /**
     *
     * @param data two way array of frequency counts
     */
    public PolychoricLogLikelihoodML(double[][] data){
        this.data = data;
        this.nrow = data.length;
        this.ncol = data[0].length;
        this.validNrow = this.nrow - 1;
        this.validNcol = this.ncol - 1;
        this.alpha = new double[nrow];
        this.beta = new double[ncol];
        bvnorm = new BivariateNormalDistributionImpl();
    }

    /**
     * number of parameters
     * 
     * @return
     */
    public int domainDimension(){
        return nrow+ncol-1;
    }

    /**
     * Loglikelihood function to be minimized.
     *
     * Parameters are stored such that x[0] is the correlation
     * x[1:validNrow] are the row thresholds
     * x[validNrow:(1+validNrow+validNcol] are the column thresholds
     *
     * @param x
     * @return
     */
    public double value(double[] x){
        double logLike = 0.0;
        if(x[0]>1.0){
            rho = maxcor;
        }else if(x[0]<-1.0){
            rho = -maxcor;
        }else{
            rho = x[0];
        }

        for(int i=0;i<validNrow;i++){
            alpha[i]=x[i+1];
        }
        alpha[validNrow]=10;
        for(int i=0;i<validNcol;i++){
            beta[i]=x[i+1+validNrow];
        }
        beta[validNcol]=10;

        double[][] Pd = new double[nrow][ncol];
        double[][] Pc = new double[nrow][ncol];
        
        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                Pd[i][j] = bvnorm.cumulativeProbability(alpha[i], beta[j], rho);
                Pc[i][j] = Pd[i][j];
                if(i>0){
                    Pd[i][j] -= Pc[i-1][j];
                }
                if(j>0){
                    Pd[i][j] -= Pc[i][j-1];
                }
                if(i > 0 && j > 0){
                    Pd[i][j] += Pc[i-1][j-1];
                }
                logLike += data[i][j]*Math.log(Pd[i][j]);
            }
        }
        return -logLike;
    }

    public void chiSquare(double fmin){
        df = getDf();
        if(df<=0.0){
            probChiSquare = Double.NaN;
        }else{
            ChiSquaredDistribution cs = new ChiSquaredDistribution(df);
            double n = 0.0;
            double sum = 0.0;
            for(int i=0;i<nrow;i++){
                for(int j=0;j<ncol;j++){
                    n += data[i][j];
                }
            }

            for(int i=0;i<nrow;i++){
                for(int j=0;j<ncol;j++){
                    sum += Math.log((data[i][j]+1e-6)/n)*data[i][j];
                }
            }
            chiSquare = 2.0*(fmin+sum);

            probChiSquare = 1.0-cs.cumulativeProbability(chiSquare);
        }
    }

    public ObjectiveFunction getObjectiveFunction(){
        ObjectiveFunction function = new ObjectiveFunction(this);
        return function;
    }

    public ObjectiveFunctionGradient getObjectiveFunctionGradient(){
        ObjectiveFunctionGradient functionGradient = new ObjectiveFunctionGradient(this.gradient());
        return functionGradient;
    }


    public double getDf(){
        return nrow*ncol-nrow-ncol;
    }

    public double getChiSquare(){
        return chiSquare;
    }

    public double getProbChiSquare(){
        return probChiSquare;
    }

    /**
     * Compute covariance matrix by numerical approximation of the hessian.
     *
     * @param x
     */
    public void computeVariance(double[] x) {
        RealMatrix m = this.hessianAt(x);
        LUDecomposition SLUD = new LUDecomposition(m);
        RealMatrix inv = SLUD.getSolver().getInverse();
        variance = inv.getData();
    }

    /**
     *
     * @return variance of optimized parameters
     */
    public double[][] getVariance(){
        return variance;
    }

    /**
     * Extracts the optimized rho of the polychoric correlation
     *
     * @param x array of optimized parameters
     * @return
     */
    public double rho(double[] x){
        return x[0];
    }

    /**
     * Extracts the array of optimized row thresholds
     *
     * @param x array of optimized parameters
     * @return
     */
    public double[] getRowThresholds(double[] x){
        for(int i=0;i<nrow;i++){
            alpha[i]=x[i+1];
        }
        return alpha;
    }

    public double[] getValidRowThresholds(double[] x){
        double[] a = new double[validNrow];
        for(int i=0;i<validNrow;i++){
            alpha[i]=x[i+1];
            a[i]=x[i+1];
        }
        return a;
    }

    /**
     * Extracts the array of optimized column thresholds
     *
     * @param x array of optimized parameters
     * @return
     */
    public double[] getColumnThresholds(double[] x){
        for(int i=0;i<ncol;i++){
            beta[i]=x[i+1+validNrow];
        }
        return beta;
    }

    public double[] getValidColumnThresholds(double[] x){
        double[] b = new double[validNcol];
        for(int i=0;i<validNcol;i++){
            beta[i]=x[i+1+validNrow];
            b[i]=x[i+1+validNrow];
        }
        return b;
    }

    public int getNumberOfValidRowThresholds(){
        return validNrow;
    }

    public int getNumberOfValidColumnThresholds(){
        return validNcol;
    }

    public String print(double[] x){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        int am1 = alpha.length-1;
        int bm1 = beta.length-1;

        f.format("%34s", "Polychoric correlation, ML est. = ");
        f.format("%6.4f", rho); f.format(" (%6.4f)", Math.sqrt(variance[0][0])); f.format("%n");
        f.format("%41s", "Test of bivariate normality: Chisquare = "); f.format("%-8.4f", chiSquare);
        f.format("%6s", " df = ");  f.format("%-6.0f", df); f.format("%5s", " p = ");   f.format("%-6.4f", probChiSquare);
        f.format("%n");
         f.format("%n");
        f.format("%18s", "Row Thresholds"); f.format("%n");
        f.format("%-15s", "Threshold"); f.format("%-10s", "Std.Err."); f.format("%n");

        for(int i=0;i<am1;i++){
            f.format("%6.4f", x[i+1]); f.format("%9s", ""); f.format("%6.4f", Math.sqrt(variance[i+1][i+1])); f.format("%n");
        }

        f.format("%n");
        f.format("%n");
        f.format("%19s", "Column Thresholds"); f.format("%n");
        f.format("%-15s", "Threshold"); f.format("%-10s", "Std.Err."); f.format("%n");

        for(int i=0;i<bm1;i++){
            f.format("% 6.4f", x[i+1+am1]); f.format("%9s", ""); f.format("% 6.4f", Math.sqrt(variance[i+1+am1][i+1+am1])); f.format("%n");
        }

        f.format("%n");
        return f.toString();

    }



}

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

import com.itemanalysis.psychometrics.distribution.BivariateNormalDistributionImpl;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Formatter;

/**
 * LogLikelihood and other methods needed for computing the two-step
 * approximation of the polychoric correlation.
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class PolychoricLogLikelihoodTwoStep implements UnivariateFunction {

    private double[][] data = null; //a k x k matrix of frequencies
    private int nrow = 0;
    private int ncol = 0;
    private double[] alpha = null;//
    private double[] beta = null;
    private double rho = 0.0;
    private double[][] Pd = null;
    private double[][] Pc = null;
    private NormalDistribution normal = null;
    private double maxcor = 0.9999;
    private BivariateNormalDistributionImpl bvnorm = null;

    public PolychoricLogLikelihoodTwoStep(double[][] data){
        this.data = data;
        nrow = data.length;
        ncol = data[0].length;
        alpha = new double[nrow];
        beta = new double[ncol];
        Pd = new double[nrow][ncol];
        Pc = new double[nrow][ncol];
        normal = new NormalDistribution();
        bvnorm = new BivariateNormalDistributionImpl();
        computeThresholds();
    }

    public double[][] getData(){
        return data;
    }

    public double[] getRowThresholds(){
        return alpha;
    }

    public double[] getValidRowThresholds(){
        int validLength = alpha.length-1;
        double[] v = new double[validLength];
        for(int i=0;i<validLength;i++){
            v[i] = alpha[i];
        }
        return v;
    }

    public double[] getColumnThresholds(){
        return beta;
    }

    public double[] getValidColumnThresholds(){
        int validLength = beta.length-1;
        double[] v = new double[validLength];
        for(int i=0;i<validLength;i++){
            v[i] = beta[i];
        }
        return v;
    }

    public int getNumberOfValidRowThresholds(){
        return alpha.length-1;
    }

    public int getNumberOfValidColumnThresholds(){
        return beta.length-1;
    }

    private void computeThresholds(){
        double[] rmarg = new double[nrow];
        double[] cmarg = new double[ncol];
        double ntotal = 0;

        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                rmarg[i] += data[i][j];
                cmarg[j] += data[i][j];
                ntotal += data[i][j];
            }
        }

        for(int i=1;i<nrow;i++){
            rmarg[i] += rmarg[i-1];
            alpha[i-1] = normal.inverseCumulativeProbability(rmarg[i-1]/ntotal);
        }
        alpha[nrow-1]=10;//set last threshold to a large number less than infinity

        for(int j=1;j<ncol;j++){
            cmarg[j] += cmarg[j-1];
            beta[j-1] = normal.inverseCumulativeProbability(cmarg[j-1]/ntotal);
        }
        beta[ncol-1]=10;//set last threshold to a large number less than infinity
    }

    public double value(double x){
        double logLike = 0.0;
        if(x>1.0){
            rho = maxcor;
        }else if(x<-1.0){
            rho = -maxcor;
        }else{
            rho = x;
        }

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

    public double[] getParameterArray(double optimRho){
        double[] a = this.getRowThresholds();
        double[] b = this.getColumnThresholds();

        int am1 = a.length-1;
        int bm1 = b.length-1;

        double[] p = new double[am1+bm1+1];
        p[0] = optimRho;
        
        for(int i=0;i<am1;i++){
            p[i+1] = a[i];
        }
        for(int i=0;i<bm1;i++){
            p[i+1+am1] = b[i];
        }
        return p;
    }

    public double getStandardError(double[] x){
        PolychoricTwoStepVariance se = new PolychoricTwoStepVariance(this);
        return Math.sqrt(se.variance(x)[0][0]);
    }

    public String print(double[] x){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double se = this.getStandardError(x);

        f.format("%34s", "Polychoric correlation, Two-step est. = ");
        f.format("%6.4f", rho); f.format(" (%6.4f)", se); f.format("%n");
        f.format("%n");
        f.format("%-20s", "Row Thresholds"); f.format("%n");

        for(int i=0;i<(alpha.length-1);i++){
            f.format("%6.4f", alpha[i]); f.format("%n");
        }

        f.format("%n");
        f.format("%n");
        f.format("%-20s", "Column Thresholds"); f.format("%n");

        for(int i=0;i<(beta.length-1);i++){
            f.format("% 6.4f", beta[i]); f.format("%n");
        }

        f.format("%n");
        return f.toString();

    }

}

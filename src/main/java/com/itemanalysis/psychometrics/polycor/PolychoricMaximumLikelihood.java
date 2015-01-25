/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.polycor;

import com.itemanalysis.psychometrics.statistics.TwoWayTable;
import com.itemanalysis.psychometrics.uncmin.DefaultUncminOptimizer;
import com.itemanalysis.psychometrics.uncmin.UncminException;
import com.itemanalysis.psychometrics.uncmin.Uncmin_methods;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Formatter;

public class PolychoricMaximumLikelihood extends PolychoricTwoStep{

    protected double probChiSquare = 0.0;
    protected double df = 0;
    protected double chiSquare = 0;
    protected double[][] variance = null;

    public PolychoricMaximumLikelihood(double[][] data){
        super(data);
    }

    public PolychoricMaximumLikelihood(TwoWayTable table){
        super(table.getTable());
    }

    /**
     * Constructor for incrementally updating data
     */
    public PolychoricMaximumLikelihood(){
        super();
    }

    /**
     * Maximum likelihood estimate of the polychoric correlation.
     *
     * @return a polychoric correlation.
     */
    public double value(){
        try{
            rho = super.value();
            MaximumLikelihoodFunction mvLikelihood = new MaximumLikelihoodFunction();
            double[] initial = getParameterArray(rho, rowThresholds, columnThresholds);

            DefaultUncminOptimizer optimizer = new DefaultUncminOptimizer();
            optimizer.minimize(mvLikelihood, initial, false, false, 250, .01);
            fmin = optimizer.getFunctionValue();

            double[] par = optimizer.getParameters();
            rho = extractRho(par, false);
            rho = Math.max(-1, Math.min(rho, 1));//ensure that correlation is between -1 and 1.
            rowThresholds = extractRowThresholds(par, false);
            columnThresholds = extractColumnThresholds(par, false);

            //Compute standard error
            computeStandardError(optimizer.getHessian());

            //Compute chi-square and p-value
            computeChiSquare();

        }catch(UncminException ex){
            ex.printStackTrace();
        }
        return rho;
    }

    private void computeStandardError(double[][] hessian){
        RealMatrix m = new Array2DRowRealMatrix(hessian);
        LUDecomposition SLUD = new LUDecomposition(m);
        RealMatrix inv = SLUD.getSolver().getInverse();
        variance = inv.getData();
    }

    public double[] getRowThresholdStandardErrors(){
        double[] se = new double[nrow-1];
        int offset = 1;
        for(int i=0;i<se.length;i++){
            se[i] = variance[i+offset][i+offset];
        }
        return se;
    }

    public double[] getColumnThresholdStandardErrors(){
        double[] se = new double[ncol-1];
        int offset = nrow;
        for(int i=0;i<se.length;i++){
            se[i] = variance[i+offset][i+offset];
        }
        return se;
    }

    public double getChiSquare(){
        return chiSquare;
    }

    public double getChiSquarePvalue(){
        return probChiSquare;
    }

    private void computeChiSquare(){
        df = nrow*ncol-nrow-ncol;

        if(df<=0.0){
            probChiSquare = 0.0;
        }else{
            ChiSquaredDistribution cs = new ChiSquaredDistribution(df);
            double sum = 0.0;
            for(int i=0;i<nrow;i++){
                for(int j=0;j<ncol;j++){
                    sum += Math.log((data[i][j]+1e-6)/N)*data[i][j];
                }
            }
            chiSquare = 2.0*(fmin+sum);
            probChiSquare = 1.0-cs.cumulativeProbability(chiSquare);
        }
    }

    public String print(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double[] rowSE = getRowThresholdStandardErrors();
        double[] colSE = getColumnThresholdStandardErrors();

        f.format("%34s", "Polychoric correlation, ML est. = ");
        f.format("%6.4f", rho); f.format(" (%6.4f)",variance[0][0]); f.format("%n");
        f.format("%41s", "Test of bivariate normality: Chisquare = "); f.format("%-8.4f", chiSquare);
        f.format("%6s", " df = ");  f.format("%-6.0f", df); f.format("%5s", " p = ");   f.format("%-6.4f", probChiSquare);
        f.format("%n");
         f.format("%n");
        f.format("%18s", "Row Thresholds"); f.format("%n");
        f.format("%-15s", "Threshold"); f.format("%-10s", "Std.Err."); f.format("%n");

        for(int i=0;i<rowThresholds.length;i++){
            f.format("%6.4f", rowThresholds[i]); f.format("%9s", ""); f.format("%6.4f", rowSE[i]); f.format("%n");
        }

        f.format("%n");
        f.format("%n");
        f.format("%19s", "Column Thresholds"); f.format("%n");
        f.format("%-15s", "Threshold"); f.format("%-10s", "Std.Err."); f.format("%n");

        for(int i=0;i<columnThresholds.length;i++){
            f.format("% 6.4f", columnThresholds[i]); f.format("%9s", ""); f.format("% 6.4f", colSE[i]); f.format("%n");
        }

        f.format("%n");
        return f.toString();

    }

    public class MaximumLikelihoodFunction implements Uncmin_methods{
        private double maxCorrelation = 0.9999;

        public double f_to_minimize(double[] x){
            double rho = x[1];
            if(Math.abs(rho) > maxCorrelation) rho = Math.signum(rho)*maxCorrelation;
            double[] rc = extractRowThresholds(x, true);
            double[] cc = extractColumnThresholds(x, true);

            return logLikelihood(rc, cc, rho);
        }

        public void gradient(double[] x, double[] y){

        }

        public void hessian(double[] x, double[][] y){

        }

    }

}

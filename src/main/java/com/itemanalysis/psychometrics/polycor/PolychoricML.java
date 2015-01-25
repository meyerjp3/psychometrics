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

import com.itemanalysis.psychometrics.uncmin.DefaultUncminOptimizer;
import com.itemanalysis.psychometrics.uncmin.UncminException;

import java.util.Formatter;

/**
 * this class implements the maximum likelihood approach to computing
 * the polychoric correlation. the gradientAt and hessian are computed
 * numerically.
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class PolychoricML extends AbstractPolychoricCorrelationOLD {

    PolychoricLogLikelihoodML mloglik = null;

    /**
     * Array of optimized parameters. First rho is the correlation,
     * second set of values is the row thresholds, and the third set
     * is the column thresholds.
     */
    private double[] result = null;

    public double fmin = 0.0;

    private String optimizationSummary = "";

    public PolychoricML(){
        super();
    }

    /**
     *
     * @return polychoric correlation
     */
    public double value(){
        return rho;
    }

    /**
     *
     * @return array of row thresholds
     */
    public double[] getRowThresholds(){
        return mloglik.getRowThresholds(result);
    }

    public double[] getValidRowThresholds(){
        return mloglik.getValidRowThresholds(result);
    }

    /**
     *
     * @return array of column thresholds
     */
    public double[] getColumnThresholds(){
        return mloglik.getColumnThresholds(result);
    }

    public double[] getValidColumnThresholds(){
        return mloglik.getValidColumnThresholds(result);
    }

    public int getNumberOfValidRowThresholds(){
        return mloglik.getNumberOfValidRowThresholds();
    }

    public int getNumberOfValidColumnThresholds(){
        return mloglik.getNumberOfValidColumnThresholds();
    }

    /**
     *
     * @return parameter covariance matrix
     */
    public double[][] getVariance(){
        return mloglik.getVariance();
    }

    public double getCorrelationStandardError(){
        double[][] se = mloglik.getVariance();
        return Math.sqrt(se[0][0]);
    }

    public double chiSquare(){
        return mloglik.getChiSquare();
    }

    public double df(){
        return mloglik.getDf();
    }

    public double chiSquareSig(){
        return mloglik.getProbChiSquare();
    }

    /**
     *
     * @return summary of optimization
     */
    public String optimizationSummary(){
        return optimizationSummary;
    }

    /**
     * Compute the maximum likelihood rho of the polychoric correlation.
     * Parameters from the two-step procedure are used as starting values.
     * Minimization is done using a conjugate gradientAt method.
     *
     * @param data two way array of frequency counts
     */
    public void compute(double[][] data){
        PolychoricTwoStepOLD twoStep = new PolychoricTwoStepOLD();
        mloglik = new PolychoricLogLikelihoodML(data);

        twoStep.compute(data);
        double[] initial = twoStep.getParameterArray();

//        NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(
//                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
//                new SimpleValueChecker(1e-10, 1e-10));
//
//        PointValuePair optimum = optimizer.optimize(
//                new MaxEval(100),
//                mloglik.getObjectiveFunction(),
//                mloglik.getObjectiveFunctionGradient(),
//                GoalType.MINIMIZE,
//                new InitialGuess(initial));
//
//        result = optimum.getPoint();
//        rho = result[0];
//        mloglik.computeVariance(result);
//        fmin = optimum.getValue();
//        mloglik.chiSquare(fmin);
//        rhoComputed = true;

        DefaultUncminOptimizer optimizer = null;
        try{
            optimizer = new DefaultUncminOptimizer();
            optimizer.minimize(mloglik, initial, true, false, 500, 1);
        }catch(UncminException ex){
            ex.printStackTrace();
        }

        result = optimizer.getParameters();
        rho = result[0];
        mloglik.computeVariance(result);
        fmin = optimizer.getFunctionValue();
        mloglik.chiSquare(fmin);
        rhoComputed = true;
    }

    public String printVerbose(){
        return mloglik.print(result);
    }

    public double[] getValidRowThresholdStandardErrors(){
        double r = this.value();
        double[][] v = this.getVariance();
        int x = mloglik.getNumberOfValidRowThresholds();
        double[] se = new double[x];

        for(int i=0;i<x;i++){
            se[i] = Math.sqrt(v[i+1][i+1]);
        }
        return se;
    }

    public double[] getValidColumnThresholdStandardErrors(){
        double r = this.value();
        double[][] v = this.getVariance();
        int x = mloglik.getNumberOfValidColumnThresholds();
        int validRowCount = this.getNumberOfValidRowThresholds();
        double[] se = new double[x];

        for(int i=0;i<x;i++){
            se[i] = Math.sqrt(v[i+1+validRowCount][i+1+validRowCount]);
        }
        return se;
    }

    public String printRowThresholds(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double r = this.value();
        double[][] v = this.getVariance();
        double[] t = this.getValidRowThresholds();

        for(int i=0;i<t.length;i++){
            f.format("% 6.4f", t[i]); f.format("%2s", ""); f.format("(%6.4f)", Math.sqrt(v[i+1][i+1])); f.format("%2s", "");
        }
        return f.toString();
    }

    public String printColumnThresholds(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double r = this.value();
        double[][] v = this.getVariance();
        double[] t = this.getValidColumnThresholds();
        int validRowCount = this.getNumberOfValidRowThresholds();

        for(int i=0;i<t.length;i++){
            f.format("% 6.4f", t[i]); f.format("%2s", ""); f.format("(%6.4f)", Math.sqrt(v[i+1+validRowCount][i+1+validRowCount])); f.format("%2s", "");
        }
        return f.toString();
    }

    public String print(){
        return "";
    }



}

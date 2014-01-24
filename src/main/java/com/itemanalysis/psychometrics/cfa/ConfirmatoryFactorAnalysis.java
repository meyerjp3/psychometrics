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
package com.itemanalysis.psychometrics.cfa;

import org.apache.commons.math3.linear.RealMatrix;

public class ConfirmatoryFactorAnalysis {

    private ConfirmatoryFactorAnalysisModel model = null;

    private ConfirmatoryFactorAnalysisEstimator estimator = null;

    private String optimizationSummary = "";

    public ConfirmatoryFactorAnalysis(RealMatrix varcov, double[] inits, double numberOfExaminees, int modelType, int estimationMethod){
        int nItems = varcov.getColumnDimension();
        setModel(nItems, modelType, inits);
        setEstimator(model, varcov, numberOfExaminees, estimationMethod);
    }

    public ConfirmatoryFactorAnalysis(RealMatrix varcov, double numberOfExaminees, int modelType, int estimationMethod){
        int nItems = varcov.getColumnDimension();
        setModel(nItems, modelType, computeInitialFactorLoadings(varcov));
        setEstimator(model, varcov, numberOfExaminees, estimationMethod);
    }

    public void setModel(int nItems, int modelType, double[] inits){
         switch(modelType){
            case ConfirmatoryFactorAnalysisModel.CONGENERIC:
                model = new CongenericModel(nItems, inits);
                break;
            case ConfirmatoryFactorAnalysisModel.TAU_EQUIVALENT:
                model = new TauEquivalentModel(nItems, inits);
                break;
            case ConfirmatoryFactorAnalysisModel.PARALLEL:
                model = new ParallelModel(nItems, inits);
                break;
        }
    }

    public void setEstimator(ConfirmatoryFactorAnalysisModel model, RealMatrix varcov, double numberOfExaminees, int estimationMethod){
        switch(estimationMethod){
            case ConfirmatoryFactorAnalysisEstimator.UNWEIGHTED_LEAST_SQUARES:
                estimator = new UnweightedLeastSquares(model, varcov, numberOfExaminees);
                break;
            case ConfirmatoryFactorAnalysisEstimator.GENERALIZED_LEAST_SQUARES:
                estimator = new GeneralizedLeastSquares(model, varcov, numberOfExaminees);
                break;
            case ConfirmatoryFactorAnalysisEstimator.MAXIMUM_LIKELIHOOD:
                estimator = new MaximumLikelihoodEstimation(model, varcov, numberOfExaminees);
                break;
        }
    }

    /**
     * Computes the first centroid factor loadings to be used
     * as initial values for the confirmatory factor anlaysis.
     *
     * This only works for a one-factor model. Need to
     * generalize to multiple factors.
     * 
     * @param varcov observed covariance matrix
     * @return
     */
    public double[] computeInitialFactorLoadings(RealMatrix varcov){
        int r = varcov.getRowDimension();
        int c = varcov.getColumnDimension();
        double totalVariance = 0.0;
        double[] rowSum = new double[c];
        double[] inits = new double[c];

        for(int i =0;i<r;i++){
            for(int j=0;j<c;j++){
                rowSum[i] += varcov.getEntry(i, j);
                totalVariance += varcov.getEntry(i, j);
            }
        }

        double denom = Math.sqrt(totalVariance);

        for(int i =0;i<r;i++){
            inits[i] = rowSum[i]/denom;
        }

        return inits;
    }

    public void optimize(int method){
//        if(method==OptimizationMethod.CONJUGATE_GRADIENT){
//            NonLinearConjugateGradientOptimizer cg = new
//                    NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE, new SimpleValueChecker(1e-15, 1e-15));
//            PointValuePair optimum = cg.optimize(500, estimator, GoalType.MINIMIZE, model.getInitialValuesVector());
//        }else{
//            //Levenberg-Marquardt optimizer
//        }
    }

    public String printOptimizationSummary(){
        return optimizationSummary;
    }

    public ConfirmatoryFactorAnalysisModel getModel(){
        return model;
    }

    public ConfirmatoryFactorAnalysisEstimator getEstimator(){
        return estimator;
    }



}

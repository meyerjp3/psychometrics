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
package com.itemanalysis.psychometrics.factoranalysis;

import org.apache.commons.math3.linear.RealMatrix;

import java.util.Formatter;

public class ExploratoryFactorAnalysis {

    private RealMatrix correlationMatrix = null;
    private int nVariables = 0;
    private int nFactors = 0;
    private int nParameters = 0;
    private FactorMethod factorMethod = null;
    private double fmin = 0.0;
    private String title = "";

    public ExploratoryFactorAnalysis(RealMatrix correlationMatrix, int nFactors){
        this.correlationMatrix = correlationMatrix;
        this.nFactors = Math.max(nFactors, 1);
        this.nVariables = correlationMatrix.getColumnDimension();
        this.nParameters = nVariables;
    }

    private double[] getInitialValues(){
        double[] init = new double[nParameters];
        for(int i=0;i<nParameters;i++){
            init[i] = 0.5;
        }
        return init;
    }

    private double[] getLowerBounds(){
        double[] lb = new double[nParameters];
        for(int i=0;i<nParameters;i++){
            lb[i] = 0.005;
        }
        return lb;
    }

    private double[] getUpperBounds(){
        double[] ub = new double[nParameters];
        for(int i=0;i<nParameters;i++){
            ub[i] = 1.0;
        }
        return ub;
    }

    public void estimateParameters(EstimationMethod fm){

        if(fm==EstimationMethod.PRINCOMP){
            factorMethod = new PrincipalComponentsMethod(correlationMatrix, nFactors);
            title = "Principal Components Analysis (no rotation)";
        }else if(fm==EstimationMethod.ML){
            factorMethod = new MaximumLikelihoodMethod(correlationMatrix, nFactors);
            title = "Maximum Likelihood Factor Analysis (no rotation)";
        }else{
            factorMethod = new MINRESmethod(correlationMatrix, nFactors);
            title = "MINRES Factor Analysis (no rotation)";
        }

        fmin = factorMethod.estimateParameters();

//        minres = new MINRESmethod(correlationMatrix, nFactors);
//        NonLinearConjugateGradientOptimizer optimizer
//                = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
//                new SimpleValueChecker(1e-8, 1e-8));
//        uniqueness = optimizer.optimize(new MaxEval(1000),
//                minres.getObjectiveFunction(),
//                minres.getObjectiveFunctionGradient(),
//                GoalType.MINIMIZE,
//                new InitialGuess(getInitialValues()));
//        minres.computeFactorLoadings(uniqueness.getPoint());


//FOR R way
//        int numIterpolationPoints = nParameters + 2;
//        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
//        RandomGenerator g = new JDKRandomGenerator();
//        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(nParameters, new GaussianRandomGenerator(g));
//        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 100, generator);
//        uniqueness = optimizer.optimize(new MaxEval(5000),
//                new ObjectiveFunction(minres),
//                GoalType.MINIMIZE,
//                new SimpleBounds(getLowerBounds(), getUpperBounds()),
////                SimpleBounds.unbounded(nParameters),
//                new InitialGuess(minres.getStartValues()));
//        System.out.println(minres.printStartValues());
//        System.out.println("Fmin: " + uniqueness.getValue());
//        minres.computeFactorLoadings(uniqueness.getPoint());


//        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
//        uniqueness = optimizer.optimize(new MaxEval(10000),
//                new ObjectiveFunction(minres),
//                GoalType.MINIMIZE,
//                new InitialGuess(getInitialValues()),
//                new NelderMeadSimplex(minres.getStartValues()),
//                SimpleBounds.unbounded(nParameters));
//        System.out.println("Fmin: " + uniqueness.getValue() + "  Iter: " + optimizer.getEvaluations());
//        minres.computeFactorLoadings(uniqueness.getPoint());


    }

//    public String printFactorLoadings(){
//        StringBuilder sb = new StringBuilder();
//        Formatter f = new Formatter(sb);
//
//        int offset = 0;
//        int offset2 = nVariables*nFactors;
//        for(int i=0;i<nVariables;i++){
//            f.format("%10s", "v"+(i+1));
//            for(int j=0;j<nFactors;j++){
//                f.format("%10.4f", minres.getFactorLoadingAt(i,j)); f.format("%5s", "");
//            }
//            f.format("%10.4f", uniqueness.getPoint()[i]);
//            f.format("%n");
//        }
//
//        return f.toString();
//    }

    public FactorMethod getFactorMethod(){
        return factorMethod;
    }

    public String printOutput(){
        return printOutput(2);
    }

    public String printOutput(int precision){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        int size = 24;
        String line1 = "=========================";
        String line2 = "-------------------------";
        for(int j=0;j<nFactors;j++){
            size = size + 10;
            line1 += "==========";
            line2 += "----------";
        }
        size = size + 10;
        line1 += "==========";
        line2 += "----------";
        size = size + 6;
        line1 += "======";
        line2 += "------";


        f.format("%36s", title); f.format("%n");
        f.format("%"+size+"s", line1);f.format("%n");
        f.format("%20s", "Name"); f.format("%4s", "");
        for(int j=0;j<nFactors;j++){
            f.format("%6s", "F"+(j+1)); f.format("%4s", "");
        }
        f.format("%6s", "H2");f.format("%4s", "");f.format("%6s", "U2");f.format("%n");
        f.format("%"+size+"s", line2);f.format("%n");

        for(int i=0;i<nVariables;i++){
            f.format("%20s", "V"+ (i+1));f.format("%5s", "");
            for(int j=0;j<nFactors;j++){
                f.format("%6."+precision+"f",  factorMethod.getFactorLoadingAt(i,j));f.format("%4s", "");
            }
            f.format("%6."+precision+"f", factorMethod.getCommunalityAt(i));f.format("%4s", "");
            f.format("%6."+precision+"f", factorMethod.getUniquenessAt(i));f.format("%n");
        }

        f.format("%"+size+"s", line1);f.format("%n");
        f.format("%30s", "Value of the objective function = "); f.format("%-8.4f", fmin);
        f.format("%n");
        f.format("%n");

        f.format("%20s", "");
        for(int j=0;j<nFactors;j++){
            f.format("%6s", "F" + (j+1)); f.format("%2s", "");
        }
        f.format("%n");

        f.format("%20s", "SS loadings");
        for(int j=0;j<nFactors;j++){
            f.format("%6."+precision+"f", factorMethod.getSumsOfSquaresAt(j)); f.format("%2s", "");
        }
        f.format("%n");

        f.format("%20s", "Proportion Var");
        for(int j=0;j<nFactors;j++){
            f.format("%6."+precision+"f", factorMethod.getProportionOfVarianceAt(j)); f.format("%2s", "");
        }
        f.format("%n");

        f.format("%20s", "Proportion Explained");
        for(int j=0;j<nFactors;j++){
            f.format("%6."+precision+"f", factorMethod.getProportionOfExplainedVarianceAt(j)); f.format("%2s", "");
        }
        f.format("%n");



        return f.toString();
    }

}

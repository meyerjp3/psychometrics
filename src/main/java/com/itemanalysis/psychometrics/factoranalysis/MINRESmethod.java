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

import com.itemanalysis.psychometrics.analysis.AbstractDiffFunction;
import com.itemanalysis.psychometrics.analysis.AbstractMultivariateFunction;
import com.itemanalysis.psychometrics.measurement.DiagonalMatrix;
import com.itemanalysis.psychometrics.optimization.DiffFunction;
import com.itemanalysis.psychometrics.statistics.IdentityMatrix;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import java.util.Arrays;
import java.util.Formatter;

public class MINRESmethod implements FactorModel{

    private RealMatrix R = null;
    private RealMatrix R2 = null;
    private int nVariables = 0;
    private int nFactors = 1;
    private int nParam = 0;
    private double[][] factorLoading = null;
    private double[] uniqueness = null;
    private double[] communality = null;
    private double[] sumsOfSquares = null;
    private double[] proportionOfVariance = null;
    private double[] proportionOfExplainedVariance = null;
    private NonLinearConjugateGradientOptimizer optimizer = null;
    private PointValuePair solution = null;

    public MINRESmethod(RealMatrix R, int nFactors){
        this.nVariables = R.getColumnDimension();
        this.nParam = nVariables;
        this.nFactors = nFactors;
        this.R = R;
        this.R2 = R.copy();
    }

    private double[] getInitialValues(){
        double[] init = new double[nParam];
        for(int i=0;i<nParam;i++){
            init[i] = 0.5;
        }
        return init;
    }

    public double estimateParameters(){

        MINRESObjectiveFunction objectiveFunction = new MINRESObjectiveFunction();

        optimizer = new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-8, 1e-8));

        solution = optimizer.optimize(new MaxEval(1000),
                objectiveFunction.getObjectiveFunction(),
                objectiveFunction.getObjectiveFunctionGradient(),
                GoalType.MINIMIZE,
                new InitialGuess(getInitialValues()));

        computeFactorLoadings(solution.getPoint());
        return solution.getValue();
    }

    private void computeFactorLoadings(double[] x){
        uniqueness = x;
        communality = new double[nVariables];

        double[] sqrtPsi = new double[nVariables];
        double[] invSqrtPsi = new double[nVariables];
        for(int i=0;i<nVariables;i++){
            sqrtPsi[i] = Math.sqrt(x[i]);
            invSqrtPsi[i] = 1.0/Math.sqrt(x[i]);
        }
        DiagonalMatrix diagPsi = new DiagonalMatrix(x);
        DiagonalMatrix diagSqtPsi = new DiagonalMatrix(sqrtPsi);
        DiagonalMatrix diagInvSqrtPsi = new DiagonalMatrix(invSqrtPsi);

        RealMatrix Sstar = diagInvSqrtPsi.multiply(R2).multiply(diagInvSqrtPsi);
        EigenDecomposition E = new EigenDecomposition(Sstar);
        RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
        double[] ev = new double[nFactors];
        for(int i=0;i<nFactors;i++){
            ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
        }
        DiagonalMatrix M = new DiagonalMatrix(ev);
        RealMatrix LOAD2 = L.multiply(M);
        RealMatrix LOAD = diagSqtPsi.multiply(LOAD2);

        Sum[] colSums = new Sum[nFactors];
        Sum[] colSumsSquares = new Sum[nFactors];

        for(int j=0;j<nFactors;j++){
            colSums[j] = new Sum();
            colSumsSquares[j] = new Sum();
        }

        factorLoading = new double[nVariables][nFactors];

        for(int i=0;i<nVariables;i++){
            for(int j=0;j<nFactors;j++){
                factorLoading[i][j] = LOAD.getEntry(i,j);
                colSums[j].increment(factorLoading[i][j]);
                colSumsSquares[j].increment(Math.pow(factorLoading[i][j],2));
                communality[i] += Math.pow(factorLoading[i][j], 2);
            }
        }

        //check sign of factor
        double sign = 1.0;
        for(int i=0;i<nVariables;i++){
            for(int j=0;j<nFactors;j++){
                if(colSums[j].getResult()<0){
                    sign = -1.0;
                }else{
                    sign = 1.0;
                }
                factorLoading[i][j] = factorLoading[i][j]*sign;
            }
        }

        double totSumOfSquares = 0.0;
        sumsOfSquares = new double[nFactors];
        proportionOfExplainedVariance = new double[nFactors];
        proportionOfVariance = new double[nFactors];
        for(int j=0;j<nFactors;j++){
            sumsOfSquares[j] = colSumsSquares[j].getResult();
            totSumOfSquares += sumsOfSquares[j];
        }
        for(int j=0;j<nFactors;j++){
            proportionOfExplainedVariance[j] = sumsOfSquares[j]/totSumOfSquares;
            proportionOfVariance[j] = sumsOfSquares[j]/nVariables;
        }

    }

    public double[] getStartValues(){
        double[] start = new double[nVariables];

        if(nFactors==1){
            for(int i=0;i<nVariables;i++){
                start[i] = 0.5;
            }
        }else{
            RealMatrix rInverse = new LUDecomposition(R2).getSolver().getInverse();
            for(int i=0;i<nVariables;i++){
                start[i] = Math.min(1.0/rInverse.getEntry(i,i), 1.0);
            }
        }

        return start;
    }

    public double[][] getFactorLoading(){
        return factorLoading;
    }

    public double getFactorLoadingAt(int i, int j){
        return factorLoading[i][j];
    }

    public double getUniquenessAt(int i){
        return uniqueness[i];
    }

    public double getCommunalityAt(int i){
        return communality[i];
    }

    public double getSumsOfSquaresAt(int j){
        return sumsOfSquares[j];
    }

    public double getProportionOfExplainedVarianceAt(int j){
        return proportionOfExplainedVariance[j];
    }

    public double getProportionOfVarianceAt(int j){
        return proportionOfVariance[j];
    }

    public String printStartValues(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double[] start = getStartValues();
        for(int i=0;i<start.length;i++){
            f.format("%8.4f", start[i]);f.format("%n");
        }
        return f.toString();
    }

    private class MINRESObjectiveFunction extends AbstractMultivariateFunction{

        public double value(double[] param){
            return valueAt(param);
        }

        public double valueAt(double[] param){
            for(int i=0;i<nVariables;i++){
                R.setEntry(i,i,1.0-param[i]);
            }

            EigenDecomposition eigen = new EigenDecomposition(R);
            RealMatrix eigenVectors = eigen.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);

            double[] ev = new double[nFactors];
            for(int i=0;i<nFactors;i++){
                ev[i] = Math.sqrt(eigen.getRealEigenvalue(i));
            }
            DiagonalMatrix evMatrix = new DiagonalMatrix(ev);//USE Apache version of Diagonal matrix when upgrade to version 3.2
            RealMatrix LAMBDA = eigenVectors.multiply(evMatrix);
            RealMatrix SIGMA = (LAMBDA.multiply(LAMBDA.transpose()));
            RealMatrix RESID = R.subtract(SIGMA);

            double sum = 0.0;
            for(int i=0;i<RESID.getRowDimension();i++){
                for(int j=0;j<RESID.getColumnDimension();j++){
                    if(i!=j){
                        sum += Math.pow(RESID.getEntry(i,j),2);
                    }
                }
            }
            return sum;

        }

        //something's not right with this gradient is crashes in teh Eigenvalue decomposition.
        //gradient computed numerically instead;
//    /**
//     * Gradient
//     *
//     * @param x a <code>double[]</code> input vector
//     * @return
//     */
//    public double[] derivativeAt(double[] x){
//        double[] sqrtPsi = new double[nVariables];
//        double[] invSqrtPsi = new double[nVariables];
//        for(int i=0;i<nVariables;i++){
//            sqrtPsi[i] = Math.sqrt(x[i]);
//            invSqrtPsi[i] = 1.0/Math.sqrt(x[i]);
//        }
//        DiagonalMatrix diagPsi = new DiagonalMatrix(x);
//        DiagonalMatrix diagSqtPsi = new DiagonalMatrix(sqrtPsi);
//        DiagonalMatrix SC = new DiagonalMatrix(invSqrtPsi);
//
//        RealMatrix Sstar = SC.multiply(R2).multiply(SC);
//
//        EigenDecomposition E = new EigenDecomposition(Sstar);
//        RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
//        double[] ev = new double[nFactors];
//        for(int i=0;i<nFactors;i++){
//            ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
//        }
//        DiagonalMatrix M = new DiagonalMatrix(ev);
//        RealMatrix LOAD = L.multiply(M);
//
//        RealMatrix LL = diagSqtPsi.multiply(LOAD);
//        RealMatrix G = LL.multiply(LL.transpose()).add(diagPsi).subtract(R2);
//
//        double[] gradient = new double[nVariables];
//        for(int i=0;i<nVariables;i++){
//            gradient[i] = G.getEntry(i,i)/(x[i]*x[i]);
//        }
//        return gradient;
//
//    }

        //here for ConjugateGradientMethod
        public ObjectiveFunction getObjectiveFunction() {
            return new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return valueAt(point);
                }
            });
        }

        //here for ConjugateGradientMethod
        public ObjectiveFunctionGradient getObjectiveFunctionGradient() {
            return new ObjectiveFunctionGradient(new MultivariateVectorFunction() {
                public double[] value(double[] point) {
                    return gradient(point);
                }
            });
        }

    }



}




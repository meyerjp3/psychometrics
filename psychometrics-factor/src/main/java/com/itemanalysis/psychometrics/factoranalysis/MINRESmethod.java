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

import com.itemanalysis.psychometrics.analysis.AbstractMultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

/**
 * Harman's minimum residual (MINRES) method of factor analysis.
 */
public class MINRESmethod extends AbstractFactorMethod {

    private RealMatrix R2 = null;
    private NonLinearConjugateGradientOptimizer optimizer = null;
    private PointValuePair solution = null;

    public MINRESmethod(RealMatrix R, int nFactors, RotationMethod rotationMethod){
        this.nVariables = R.getColumnDimension();
        this.nParam = nVariables;
        this.nFactors = nFactors;
        this.rotationMethod = rotationMethod;
        this.R = R;
        this.R2 = R.copy();
    }

    public double estimateParameters(){
        Sinv = new LUDecomposition(R).getSolver().getInverse();

        MINRESObjectiveFunction objectiveFunction = new MINRESObjectiveFunction();

        optimizer = new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-10, 1e-10),
                1e-4, 1e-4, 1);

        solution = optimizer.optimize(new MaxEval(1000),
                objectiveFunction.getObjectiveFunction(),
                objectiveFunction.getObjectiveFunctionGradient(),
                GoalType.MINIMIZE,
                new InitialGuess(getStartValues()));

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
        com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix diagPsi = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(x);
        com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix diagSqtPsi = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(sqrtPsi);
        com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix diagInvSqrtPsi = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(invSqrtPsi);

        RealMatrix Sstar = diagInvSqrtPsi.multiply(R2).multiply(diagInvSqrtPsi);
        EigenDecomposition E = new EigenDecomposition(Sstar);
        RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
        double[] ev = new double[nFactors];
        for(int i=0;i<nFactors;i++){
            ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
        }
        com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix M = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(ev);
        RealMatrix LOAD2 = L.multiply(M);
        RealMatrix LOAD = diagSqtPsi.multiply(LOAD2);

        //rotate factor loadings
        if(rotationMethod!=RotationMethod.NONE){
            GPArotation gpa = new GPArotation();
            RotationResults results = gpa.rotate(LOAD, rotationMethod);
            LOAD = results.getFactorLoadings();
        }

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

    /**
     * This class is used for the numeric optimization routine.
     */
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
            com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix evMatrix = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(ev);//USE Apache version of Diagonal matrix when upgrade to version 3.2
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

        /**
         * Gradient
         *
         * @param x a <code>double[]</code> input vector
         * @return
         */
        @Override
        public double[] gradientAt(double[] x){
            double[] sqrtPsi = new double[nVariables];
            double[] invSqrtPsi = new double[nVariables];
            for(int i=0;i<nVariables;i++){
                x[i] = Math.max(0.005, x[i]);//ensure that no parameters are negative
                sqrtPsi[i] = Math.sqrt(x[i]);
                invSqrtPsi[i] = 1.0/Math.sqrt(x[i]);
            }
            com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix diagPsi = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(x);
            com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix diagSqtPsi = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(sqrtPsi);
            com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix SC = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(invSqrtPsi);

            RealMatrix Sstar = SC.multiply(R2).multiply(SC);

            EigenDecomposition E = new EigenDecomposition(Sstar);
            RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
            double[] ev = new double[nFactors];
            for(int i=0;i<nFactors;i++){
                ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
            }
            com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix M = new DiagonalMatrix(ev);
            RealMatrix LOAD = L.multiply(M);

            RealMatrix LL = diagSqtPsi.multiply(LOAD);
            RealMatrix G = LL.multiply(LL.transpose()).add(diagPsi).subtract(R2);

            double[] gradient = new double[nVariables];
            for(int i=0;i<nVariables;i++){
                gradient[i] = G.getEntry(i,i)/(x[i]*x[i]);
            }
            return gradient;

        }

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
                    return gradientAt(point);
                }
            });
        }

    }



}




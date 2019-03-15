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
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

public class WeightedLeastSquaresMethod extends AbstractFactorMethod {

    private NonLinearConjugateGradientOptimizer optimizer = null;
    private PointValuePair solution = null;
    private RealMatrix R2 = null;
    
    public WeightedLeastSquaresMethod(RealMatrix R, int nFactors, RotationMethod rotationMethod){
        this.nVariables = R.getColumnDimension();
        this.nParam = nVariables;
        this.nFactors = nFactors;
        this.rotationMethod = rotationMethod;
        this.R = R;
        this.R2 = R.copy();
    }
    
    public double estimateParameters(){

        Sinv = new LUDecomposition(R).getSolver().getInverse();

        WLSObjectiveFunction objectiveFunction = new WLSObjectiveFunction();

        optimizer = new NonLinearConjugateGradientOptimizer(
                NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                new SimpleValueChecker(1e-8, 1e-8));

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

        for(int i=0;i<nVariables;i++){
            R.setEntry(i,i,1.0-x[i]);
        }

        EigenDecomposition E = new EigenDecomposition(R);
        RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
        double[] ev = new double[nFactors];
        for(int i=0;i<nFactors;i++){
            ev[i] = Math.sqrt(E.getRealEigenvalue(i));
        }
        DiagonalMatrix M = new DiagonalMatrix(ev);
        RealMatrix LOAD = L.multiply(M);

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
    private class WLSObjectiveFunction extends AbstractMultivariateFunction {

        public double value(double[] param){
            return valueAt(param);
        }

        public double valueAt(double[] param){
            double[] sdInv = new double[nVariables];

            for(int i=0;i<nVariables;i++){
                R.setEntry(i,i,1.0-param[i]);
                sdInv[i] = 1.0/Sinv.getEntry(i,i);
            }

            DiagonalMatrix diagSdInv = new DiagonalMatrix(sdInv);

            EigenDecomposition eigen = new EigenDecomposition(R);
            RealMatrix eigenVectors = eigen.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);

            double[] ev = new double[nFactors];
            for(int i=0;i<nFactors;i++){
                ev[i] = Math.sqrt(eigen.getRealEigenvalue(i));
            }
            DiagonalMatrix evMatrix = new DiagonalMatrix(ev);//USE Apache version of Diagonal matrix when upgrade to version 3.2
            RealMatrix LAMBDA = eigenVectors.multiply(evMatrix);
            RealMatrix SIGMA = (LAMBDA.multiply(LAMBDA.transpose()));

            double value = 0.0;
            RealMatrix DIF = R.subtract(SIGMA);
            for(int i=0;i<DIF.getRowDimension();i++){
                for(int j=0;j<DIF.getColumnDimension();j++){
                    value = DIF.getEntry(i,j);
                    DIF.setEntry(i,j, Math.pow(value, 2));
                }
            }

            RealMatrix RESID = diagSdInv.multiply(DIF).multiply(diagSdInv);

            double sum = 0.0;
            for(int i=0;i<RESID.getRowDimension();i++){
                for(int j=0;j<RESID.getColumnDimension();j++){
                    sum += RESID.getEntry(i,j);
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
            DiagonalMatrix diagPsi = new DiagonalMatrix(x);
            DiagonalMatrix diagSqtPsi = new DiagonalMatrix(sqrtPsi);
            DiagonalMatrix SC = new DiagonalMatrix(invSqrtPsi);

            RealMatrix Sstar = SC.multiply(R2).multiply(SC);

            EigenDecomposition E = new EigenDecomposition(Sstar);
            RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
            double[] ev = new double[nFactors];
            for(int i=0;i<nFactors;i++){
                ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
            }
            DiagonalMatrix M = new DiagonalMatrix(ev);
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

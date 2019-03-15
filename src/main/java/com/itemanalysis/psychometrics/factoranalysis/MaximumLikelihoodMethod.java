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

/**
 * Exploratory factor analysis by maximum likelihood estimation.
 */
public class MaximumLikelihoodMethod extends AbstractFactorMethod {

    /**
     * Optimizer for obtaining the optimal uniquenesses.
     */
    private NonLinearConjugateGradientOptimizer optimizer = null;

    /**
     * The result of the optimization
     */
    private PointValuePair solution = null;

    public MaximumLikelihoodMethod(RealMatrix correlationMatrix, int nFactors, RotationMethod rotationMethod){
        this.nVariables = correlationMatrix.getColumnDimension();
        this.nFactors = nFactors;
        this.rotationMethod = rotationMethod;
        this.nParam = nVariables;
        this.R = correlationMatrix;
    }

    public double estimateParameters(){

        Sinv = new LUDecomposition(R).getSolver().getInverse();

        MLObjectiveFunction objectiveFunction = new MLObjectiveFunction();

//        System.out.println("START VALUES: " + Arrays.toString(getStartValues()));

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

        double[] sqrtPsi = new double[nVariables];
        double[] invSqrtPsi = new double[nVariables];
        for(int i=0;i<nVariables;i++){
            sqrtPsi[i] = Math.sqrt(x[i]);
            invSqrtPsi[i] = 1.0/Math.sqrt(x[i]);
        }
        DiagonalMatrix diagPsi = new DiagonalMatrix(x);
        DiagonalMatrix diagSqtPsi = new DiagonalMatrix(sqrtPsi);
        DiagonalMatrix diagInvSqrtPsi = new DiagonalMatrix(invSqrtPsi);

        RealMatrix Sstar = diagInvSqrtPsi.multiply(R).multiply(diagInvSqrtPsi);
        EigenDecomposition E = new EigenDecomposition(Sstar);
        RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
        double[] ev = new double[nFactors];
        for(int i=0;i<nFactors;i++){
            ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
        }
        DiagonalMatrix M = new DiagonalMatrix(ev);
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
     * This class is used by the optimization routine.
     */
    private class MLObjectiveFunction extends AbstractMultivariateFunction {

        public double value(double[] param){
            return valueAt(param);
        }

        public double valueAt(double[] param){

//            System.out.println(Arrays.toString(param));

            double[] invSqrtPsi = new double[nVariables];
            for(int i=0;i<nVariables;i++){
                param[i] = Math.max(0.005, param[i]);//ensure that no parameters are negative
                invSqrtPsi[i] = 1.0/Math.sqrt(param[i]);
            }

            RealMatrix SC = new DiagonalMatrix(invSqrtPsi);
            RealMatrix Sstar = SC.multiply(R).multiply(SC);
            EigenDecomposition eigen = new EigenDecomposition(Sstar);

            int size = nVariables-nFactors;
            double[] ev = new double[size];

//            System.out.println("EIGEN VALUES: " + Arrays.toString(eigen.getRealEigenvalues()));

            int offset = 0;
            for(int i=nFactors;i<nVariables;i++){
                ev[offset++] = eigen.getRealEigenvalue(i);
            }

//            System.out.println("EV-short: " + Arrays.toString(ev));

            double sum = 0.0;
            for(int i=0;i<ev.length;i++){
                sum += Math.log(ev[i]) - ev[i];
            }
            double result = sum - nFactors + nVariables;

//            System.out.println("RESULT: " + -result);

            return -result;
        }

        @Override
        public double[] gradientAt(double[] param){
            double[] sqrtPsi = new double[nVariables];
            double[] invSqrtPsi = new double[nVariables];
            for(int i=0;i<nVariables;i++){
                param[i] = Math.max(0.005, param[i]);//ensure that no parameters are negative
                sqrtPsi[i] = Math.sqrt(param[i]);
                invSqrtPsi[i] = 1.0/Math.sqrt(param[i]);
            }
            DiagonalMatrix diagPsi = new DiagonalMatrix(param);
            DiagonalMatrix diagSqtPsi = new DiagonalMatrix(sqrtPsi);

            RealMatrix SC = new DiagonalMatrix(invSqrtPsi);
            RealMatrix Sstar = SC.multiply(R).multiply(SC);
            EigenDecomposition E = new EigenDecomposition(Sstar);
            RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
            double[] ev = new double[nFactors];
            for(int i=0;i<nFactors;i++){
                ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
            }
            DiagonalMatrix M = new DiagonalMatrix(ev);
            RealMatrix LOAD = L.multiply(M);

            RealMatrix LL = diagSqtPsi.multiply(LOAD);
            RealMatrix G = LL.multiply(LL.transpose()).add(diagPsi).subtract(R);

            double[] gradient = new double[nVariables];
            for(int i=0;i<nVariables;i++){
                gradient[i] = G.getEntry(i,i)/(param[i]*param[i]);
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

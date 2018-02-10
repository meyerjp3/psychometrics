/*
 * Copyright 2018 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.reliability;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;


/**
 * Guttman's lambda 6.
 * This calculation uses the squared multiple correlations. If the determinant
 * of teh covariance matrix is near zero, the calculation may be inaccurate because
 * of problems inverting the matrix. A warning is provide in such cases.
 *
 */
public class GuttmanLambda6 extends AbstractScoreReliability {

    public GuttmanLambda6(double[][] matrix){
        this.matrix = matrix;

        nItems = matrix.length;
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.GUTTMAN_LAMBDA6;
    }

    public double value(){
        double[] smc = squaredMultipleCorrelation(this.matrix);

        double sum = 0;
        for(int i=0;i<nItems;i++){
            sum += 1.0-smc[i];
        }

        double observedScoreVariance = this.totalVariance();
        double lambda6 = 1.0 - sum/observedScoreVariance;

        return lambda6;
    }

    /**
     * Computes the squared multiple correlation (smc)
     *
     * @return smc
     *
     */
    private double[] squaredMultipleCorrelation(double[][] corMatrix){
        RealMatrix S = new Array2DRowRealMatrix(corMatrix);
        LUDecomposition lu = new LUDecomposition(S);
        RealMatrix X = lu.getSolver().getInverse();

        double[] smc = new double[nItems];
        for(int i=0;i<nItems;i++){
            smc[i] = 1.0-(1.0/X.getEntry(i,i));
        }

        return smc;
    }

    /**
     * Converts the covariance matrix to a correlaiton matrix.
     *
     * @param cov covariance matrix
     * @return correlation matrix
     */
    private double[][] covToCor(double[][] cov){
        double[][] cor = new double[cov.length][cov.length];

        for(int i=0;i<cov.length;i++){
            for(int j=0;j<cov.length;j++){
                cor[i][j] = cov[i][j]/(Math.sqrt(cov[i][i])*Math.sqrt(cov[j][j]));
            }
        }

        return cor;
    }

    /**
     * Computes reliability with each item omitted in turn. The first element in the array is the
     * reliability estimate without the first item. The second item in the array is the reliability
     * estimate without the second item and so on.
     *
     * @return array of item deleted estimates.
     */
    public double[] itemDeletedReliability(){
        double[] rel = new double[nItems];

        GuttmanLambda6 l6 = null;
        for(int i=0;i<nItems;i++){
            l6 = new GuttmanLambda6(matrixWithoutItemAt(i));
            rel[i] = l6.value();
        }

        return rel;
    }

}

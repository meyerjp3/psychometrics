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

import java.util.Formatter;

public class GuttmanLambda3 extends AbstractScoreReliability {

    public GuttmanLambda3(double[][] matrix){
        this.matrix = matrix;
        nItems = matrix.length;
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.GUTTMAN_LAMBDA3;
    }

    public double value(){
        double k = (double)nItems;
        double observedScoreVariance = this.totalVariance();
        double lambda1 = 1.0-this.diagonalSum()/observedScoreVariance;
        double lambda3 = lambda1*(k/(k-1.0));
        return lambda3;
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
        double totalVariance = this.totalVariance();
        double diagonalSum = this.diagonalSum();
        double totalVarianceAdjusted = 0;
        double diagonalSumAdjusted = 0;
        double lambda1Adjusted = 0;
        double lambda3Adjusted = 0;
        double k = (double)nItems-1.0;

        for(int i=0;i<nItems;i++){
            //Compute item variance
            double itemVariance = matrix[i][i];

            //Compute sum of covariance between this item and all others
            double itemCovariance = 0;
            for(int j=0;j<nItems;j++){
                if(i!=j) itemCovariance += matrix[i][j];
            }
            itemCovariance *= 2;

            totalVarianceAdjusted = totalVariance - itemCovariance - itemVariance;
            diagonalSumAdjusted = diagonalSum - itemVariance;
            lambda1Adjusted = 1-diagonalSumAdjusted/totalVarianceAdjusted;
            lambda3Adjusted = lambda1Adjusted*(k/(k-1.0));
            rel[i] = lambda3Adjusted;

        }

        return rel;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        Formatter f = new Formatter(builder);
        String f2="%.2f";
        f.format("%21s", "Guttman's Lambda-3 = "); f.format(f2,this.value());
        return f.toString();
    }

}

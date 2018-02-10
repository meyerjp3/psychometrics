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

public class GuttmanLambda5 extends AbstractScoreReliability {

    public GuttmanLambda5(double[][] matrix){
        this.matrix = matrix;
        nItems = matrix.length;
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.GUTTMAN_LAMBDA5;
    }

    public double value(){
        double largestSum = sumOfSquareCovariances(0);
        for(int i=1;i<nItems;i++){
            largestSum = Math.max(largestSum, sumOfSquareCovariances(i));
        }

        double observedScoreVariance = this.totalVariance();
        double lambda1 = 1-this.diagonalSum()/observedScoreVariance;
        double lambda5 = lambda1 + 2.0*(Math.sqrt(largestSum)/observedScoreVariance);
        return lambda5;

    }

    /**
     * Sum the squared covariance between teh item at itemIndex and the remaining items.
     *
     * @param itemIndex item index
     * @return sum of the squared covariance
     */
    private double sumOfSquareCovariances(int itemIndex){
        double sum = 0;
        for(int i=0;i<nItems;i++){
            if(i!=itemIndex) sum += matrix[itemIndex][i]*matrix[itemIndex][i];
        }

        return sum;
    }

    private double sumOfSquareCovariancesWithoutItemAt(int itemIndex, int omittedItemIndex){
        double sum = 0;

        for(int i=0;i<nItems;i++){
            if(i!=itemIndex  && i!=omittedItemIndex) sum += matrix[itemIndex][i]*matrix[itemIndex][i];
        }

        return sum;
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
        double lambda5Adjusted = 0;

        for(int i=0;i<nItems;i++){
            //Compute item variance
            double itemVariance = matrix[i][i];

            //Compute sum of covariance between this item and all others
            double itemCovariance = 0;
            double largestSum = -1;
            for(int j=0;j<nItems;j++){
                if(i!=j){
                    largestSum = Math.max(largestSum, sumOfSquareCovariancesWithoutItemAt(j, i));
                    itemCovariance += matrix[i][j];
                }
            }
            itemCovariance *= 2;
            totalVarianceAdjusted = totalVariance - itemCovariance - itemVariance;
            diagonalSumAdjusted = diagonalSum - itemVariance;
            lambda1Adjusted = 1-diagonalSumAdjusted/totalVarianceAdjusted;
            lambda5Adjusted = lambda1Adjusted + 2.0*(Math.sqrt(largestSum)/totalVarianceAdjusted);
            rel[i] = lambda5Adjusted;

        }

        return rel;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        Formatter f = new Formatter(builder);
        String f2="%.2f";
        f.format("%21s", "Guttman's Lambda-5 = "); f.format(f2,this.value());
        return f.toString();
    }

}

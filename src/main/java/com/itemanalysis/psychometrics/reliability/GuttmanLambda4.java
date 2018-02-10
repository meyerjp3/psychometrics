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

public class GuttmanLambda4 extends AbstractScoreReliability {

    public GuttmanLambda4(double[][] matrix){
        this.matrix = matrix;
        this.nItems = matrix.length;
    }

    public ScoreReliabilityType getType() {
        return ScoreReliabilityType.GUTTMAN_LAMBDA4;
    }

    public double value(){
        int[][] index = splitTest();
        double totalVariance = this.totalVariance();
        double part1TotalVariance = partTotalVariance(index[0]);
        double part2TotalVariance = partTotalVariance(index[1]);
        double lambda4 = 2.0*(1.0-(part1TotalVariance+part2TotalVariance)/totalVariance);
        return lambda4;
    }

    /**
     * Splits test into first half and second half. If the test length is odd, then the
     * last item in the first half is also the first item in teh second half.
     * @return item indices for two parts.
     */
    private int[][] splitTest(){
        int halfTestLength = (int)Math.ceil((double)nItems/2.0);
        int[][] splitIndex = new int[2][];

        if((matrix.length % 2) == 0){
            //even number of items
            splitIndex = new int[2][halfTestLength];

            for(int i=0;i<halfTestLength;i++){
                splitIndex[0][i] = i;
                splitIndex[1][i] = halfTestLength + i;
            }

        }else{
            //odd number of items
            splitIndex[0] = new int[halfTestLength];
            splitIndex[1] = new int[halfTestLength-1];

            for(int i=0;i<halfTestLength;i++){
                splitIndex[0][i] = i;
                if(i<halfTestLength-1){
                    splitIndex[1][i] = halfTestLength + i;
                }
            }

        }

        return splitIndex;
    }

    private double partTotalVariance(int[] itemIndex){
        double totalVariance = 0.0;

        for(int i=0;i<itemIndex.length;i++){
            for(int j=0;j<itemIndex.length;j++){
                totalVariance += this.matrix[itemIndex[i]][itemIndex[j]];
            }
        }

        return totalVariance;
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

        GuttmanLambda4 l6 = null;
        for(int i=0;i<nItems;i++){
            l6 = new GuttmanLambda4(matrixWithoutItemAt(i));
            rel[i] = l6.value();
        }

        return rel;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        Formatter f = new Formatter(builder);
        String f2="%.2f";
        f.format("%21s", "Guttman's Lambda-4 = "); f.format(f2,this.value());
        return f.toString();
    }

}

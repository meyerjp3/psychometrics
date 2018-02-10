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

import java.util.Arrays;
import java.util.Formatter;

public class GuttmanLambda4 extends AbstractScoreReliability {

    public GuttmanLambda4(double[][] matrix){
        if((matrix.length % 2) == 0){
            //even number of items
            this.matrix = matrix;
            this.nItems = matrix.length;
        }else{
            //odd number of items
            this.matrix = expandMatrix(matrix);
            this.nItems = this.matrix.length;

        }
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
     * Adds a column to the matrix by copying the first column. Done to create
     * a matrix with an even number of columns.
     *
     * @param m original matrix
     * @return new matrix with additional column
     */
    private double[][] expandMatrix(double[][] m){
        int n = m.length;
        double[][] m2 = new double[n+1][n+1];

        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                m2[i][j] = m[i][j];
                System.out.print(m2[i][j] + " ");
            }
            m2[i][n] = m[i][0];
            m2[n][i] = m[0][i];
            System.out.print(m2[i][n] + " ");
            System.out.println();
        }



        return m2;
    }

    /**
     * Splits test into first half and second half. If the test length is odd, then the
     * last item in the first half is also the first item in teh second half.
     * @return item indices for two parts.
     */
    private int[][] splitTest(){
        int halfTestLength = (int)Math.ceil((double)nItems/2.0);
        int splitIndex[][] = new int[2][halfTestLength];

        for(int i=0;i<halfTestLength;i++){
            splitIndex[0][i] = i;
            splitIndex[1][i] = halfTestLength + i;
        }

        return splitIndex;
    }

//    private int[][] splitTestWithoutItemsAt(int omittedItem){
//        int halfTestLength = (int)Math.ceil(((double)nItems-1.0)/2.0);
//        int splitIndex[][] = new int[2][halfTestLength];
//
////        System.out.println("i= " + nItems + " half: " + halfTestLength );
//
//        //create array of item indices without the omitted item index
//        int index = 0;
//        int[] validIndex = new int[nItems-1];
//        for(int i=0;i<nItems;i++){
//            if(i!=omittedItem){
//                validIndex[index] = i;
//                index++;
//            }
//        }
//
//        for(int i=0;i<halfTestLength;i++){
//            splitIndex[0][i] = validIndex[i];
//            splitIndex[1][i] = validIndex[halfTestLength-1 + i];
//        }
//
////        System.out.println(Arrays.toString(validIndex));
////        System.out.println(Arrays.toString(splitIndex[0]));
////        System.out.println(Arrays.toString(splitIndex[1]));
//
//        if(((nItems-1) % 2) == 0){
//            //even number of items
//
//        }else{
//            //odd number of items
//            //set first item index in part 2 to be the same as last item index in part 1.
//            splitIndex[1][0] = splitIndex[0][halfTestLength-1];
//
//        }
//        return splitIndex;
//    }

    private double partTotalVariance(int[] itemIndex){
        double totalVariance = 0.0;

        for(int i=0;i<itemIndex.length;i++){
            for(int j=0;j<itemIndex.length;j++){
                totalVariance += this.matrix[itemIndex[i]][itemIndex[j]];
            }
        }

        return totalVariance;
    }

//    /**
//     * Creates a submatrix that is the covariance matrix without row itemIndex
//     * and column itemIndex;
//     *
//     * @param index revised row and column indices
//     * @return revised matrix
//     */
//    protected double[][] revisedMatrix(int[][] index){
//        double[][] m = new double[index.length][index.length];
//
//        for(int i=0;i<index.length;i++){
//            for(int j=0;j<index.length;j++){
//                m[i][j] = matrix[index[0][i]][index[1][j]];
//            }
//        }
//        return m;
//    }

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


//        double[] rel = new double[nItems];
//        double totalVarianceAdjusted = 0;
//        double part1TotalVariance = 0;
//        double part2TotalVariance = 0;
//        double lambda4Adjusted = 0;
//
//        for(int i=0;i<nItems;i++){
//            int[][] index = splitTestWithoutItemsAt(i);
//
//            for(int j=0;j<index.length;j++){
//                for(int k=0;k<index[0].length;k++){
//                    totalVarianceAdjusted += matrix[index[0][j]][index[1][k]];
//                }
//            }
//
//            part1TotalVariance = partTotalVariance(index[0]);
//            part2TotalVariance = partTotalVariance(index[1]);
//            lambda4Adjusted = 2.0*(1.0-(part1TotalVariance+part2TotalVariance)/totalVarianceAdjusted);
//
//            System.out.println(Arrays.toString(index[0]));
//            System.out.println(Arrays.toString(index[1]));
//            System.out.println(totalVarianceAdjusted + " " + part1TotalVariance + "  " + part2TotalVariance);
//
//            rel[i] = lambda4Adjusted;
//        }
//
//        return rel;
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

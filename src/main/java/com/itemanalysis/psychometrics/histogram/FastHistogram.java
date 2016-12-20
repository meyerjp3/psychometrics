/**
 * Copyright 2016 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.histogram;

import java.util.Arrays;

public class FastHistogram {

    private int numberOfBins = 10;

    private double bandwidth = 1;

    private double sampleSize = 0;

    private HistogramType type = HistogramType.FREQUENCY;

    /**
     * A two-way array with the number of rows equal to the number of bins.
     * The first column is the lower bound of a bin.
     * The second column is the upper bound of the bin.
     * The third column is the bin midpoint.
     * The fourth column is the frequency of observations in the bin or
     * a relative frequency or normalized frequency.
     */
    private double[][] histogram = null;

    public FastHistogram(HistogramType type){
        this.type = type;
    }

    private void computeBounds(){

    }

    public double[][] histogram(double[] x, double[] breaks){
        sampleSize = (double)x.length;
        numberOfBins = breaks.length-1;
        if(numberOfBins <=1) throw new IllegalArgumentException("Invalid number of bins: " + numberOfBins);
        bandwidth = (breaks[1]-breaks[0]);//assumes a constant bandwidth

        histogram = new double[breaks.length-1][4];
        for(int i=0;i<numberOfBins;i++){
            histogram[i][0] = breaks[i]; //lower bound
            histogram[i][1] = breaks[i+1]; //upper bound
            histogram[i][2] = (breaks[i]+breaks[i+1])/2.0;//midpoint
            histogram[i][3] = 0.0;//frequency
        }

        for(double d : x){
            int index = Arrays.binarySearch(breaks, d);

            if(index==-1){
                index = 0;
            }

            if(index<0 && index!=-1){
                index = ~index-1;
            }

            if(index>=0){
                histogram[index][3]++;
            }

        }

        if(type==HistogramType.RELATIVE_FREQUENCY){
            for(int i=0;i<numberOfBins;i++){
                histogram[i][3] = histogram[i][3]/sampleSize;
            }
        }

        if(type==HistogramType.DENSITY){
            for(int i=0;i<numberOfBins;i++){
                histogram[i][3] = histogram[i][3]/(sampleSize*bandwidth);
            }
        }

        return histogram;

    }

}

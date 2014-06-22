/*
 * Copyright 2012 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.histogram;

/**
 * An interface for class that compute the number of bins in a histogram. The interface is design for a storeless
 * computation of the number of bins. Implementations of this interface do not need to store the entire array of
 * data points. The bins will be computed by incrementally updating them.
 * 
 * @author J. Patrick Meyer
 */
public interface BinCalculation {

//    public void evaluate(double[] x);
//
//    /**
//     * Update the summary statistic with a new data point.
//     *
//     * @param x data point to be added to the summary statistics.
//     */
//    public void increment(double x);

    /**
     * Get the number of histogram bins.
     * 
     * @return number of bins.
     */
    public int numberOfBins();

    /**
     * Gets the width of the bin according to the calculated number of bins.
     * 
     * @return bin width.
     */
    public double binWidth();

    public BinCalculationType getType();

//    /**
//     * Gets the smallest data point.
//     *
//     * @return smallest value.
//     */
//    public double min();
//
//    /**
//     * Gets the largest data point.
//     *
//     * @return largest value.
//     */
//    public double max();
//
//    /**
//     * Get the sample size.
//     *
//     * @return saple size.
//     */
//    public double sampleSize();

}

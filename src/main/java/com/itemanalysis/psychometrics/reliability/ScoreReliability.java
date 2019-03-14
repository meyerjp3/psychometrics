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
package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.data.VariableAttributes;

import java.util.ArrayList;

/**
 * 
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public interface ScoreReliability {

    /**
     * An array of reliability estimates without the item indexed by the position in the array.
     * For example, rel[0] = is the reliability estimate without the item at position 0.
     * Similarly, rel[1] = is the reliability estimate without the item at position 1.
     *
     * @return
     */
    public double[] itemDeletedReliability();

    /**
     * Estimates reliability.
     *
     * @return estimate of reliability
     */
    public double value();

    /**
     * A String representation of all item deleted reliability estimates.
     *
     * @param var DefaultVariableAttributes that provide the variable names.
     * @return
     */
    public String printItemDeletedSummary(ArrayList<VariableAttributes> var);

    /**
     * Type of reliability estimate
     *
     * @return type of reliability estimate.
     */
    public ScoreReliabilityType getType();

    /**
     * Confidence interval for the reliability estimate computed using the F-quadrature.
     * This computation is only correct for Coefficient Alpha because the sampling
     * quadrature for other reliability estimates is unknown. As such, this method
     * returns an approximation, at best, of the reliability estimate for all reliability
     * estimates other than coefficient alpha. Note that the confidence interval is computed
     * using the largest sample size in the covariance matrix. This value would be the sample
     * size for pairwise deletion.
     *
     * @return a confidence interval for the reliability estimate.
     */
    public double[] confidenceInterval(double numberOfExaminees);

    /**
     * Creates a String representation of the confidence interval. It is used for 
     * displaying results.
     * 
     * @param confidenceInterval an array with the lower [0] and upper [1] bounds of the confidence interval 
     * @return a String representation of the confidence interval.
     */
    public String confidenceIntervalToString(double[] confidenceInterval);

//    /**
//     * Set the unbiased flag.
//     *
//     * @param unbiased true if variance calculations should use N-1 in the denominator, and false otherwise.
//     */
//    public void isUnbiased(boolean unbiased);

    /**
     * Total observed score variance. It is the sum of all values in the covariance matrix.
     * @return
     */
    public double totalVariance();

}

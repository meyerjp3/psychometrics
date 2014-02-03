/*
 * Copyright 2013 J. Patrick Meyer
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

package com.itemanalysis.psychometrics.irt.estimation;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

/**
 * Storeless computation of Rasch scale quality statistics such as reliability an separation. Values are computed
 * by incrementally updating each statistic with a new observation. This class is used for both item side quality
 * and person side quality.
 */
public class RaschScaleQualityStatistics {

    private Variance var = null;

    private Mean mean = null;

    public RaschScaleQualityStatistics(){
        var = new Variance(false);
        mean = new Mean();
    }

    /**
     * An incremental update to the scale quality statistics.
     *
     * @param estimate a person ability or item difficulty estimate.
     * @param stdError
     */
    public void increment(double estimate, double stdError){
        var.increment(estimate);
        mean.increment(Math.pow(stdError, 2));
    }

    /**
     * Observed variance of the estimate.
     *
     * @return observed variance.
     */
    public double observedVariance(){
        return var.getResult();
    }

    /**
     * Observed standard deviation of the estimate.
     *
     * @return stanmdard deviation.
     */
    public double observedStandardDeviation(){
        return Math.sqrt(var.getResult());
    }

    /**
     * Mean square error of the estimate.
     *
     * @return mean square error.
     */
    public double meanSquareError(){
        return mean.getResult();
    }

    /**
     * The square root of mean square error.
     *
     * @return standard error.
     */
    public double rootMeanSquareError(){
        return Math.sqrt(meanSquareError());
    }

    /**
     * An adjusted variance for computing scale quality statistics.
     *
     * @return adjusted variance.
     */
    public double adjustedVariance(){
        return Math.max(0,var.getResult() - meanSquareError());//can be negative. constrain to be nonnegative
    }

    /**
     * An adjusted standard deviation for computing scale quality statistics.
     *
     * @return standrd deviation.
     */
    public double adjustedStandardDeviation(){
        return Math.sqrt(adjustedVariance());
    }

    /**
     * The separation index of scale quality.
     *
     * @return the separation index.
     */
    public double separationIndex(){
        return adjustedStandardDeviation()/rootMeanSquareError();
    }

    /**
     * Number of distinct strata that are possible with the given estimates.
     *
     * @return number of strata.
     */
    public double numberOfStrata(){
        return (4.0*separationIndex()+1)/3.0;
    }

    /**
     * Reliability is the reproduceability of the item or person estimates.
     *
     * @return reliability of the estimates.
     */
    public double reliability(){
        return adjustedVariance()/var.getResult();
    }

}

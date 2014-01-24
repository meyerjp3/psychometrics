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
package com.itemanalysis.psychometrics.rasch;


import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

/**
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class ScaleQualityStatistics {

    private Variance var = null;

    private Mean mean = null;

    public ScaleQualityStatistics(){
        var = new Variance(false);
        mean = new Mean();
    }

    public void increment(double estimate, double stdError){
        var.increment(estimate);
        mean.increment(Math.pow(stdError, 2));
    }

    public double observedVariance(){
        return var.getResult();
    }

    public double observedStandardDeviation(){
        return Math.sqrt(var.getResult());
    }

    public double meanSquareError(){
        return mean.getResult();
    }

    public double rootMeanSquareError(){
        return Math.sqrt(meanSquareError());
    }

    public double adjustedVariance(){
        return Math.max(0,var.getResult() - meanSquareError());//can be negative. constrain to be nonnegative
    }

    public double adjustedStandardDeviation(){
        return Math.sqrt(adjustedVariance());
    }

    public double separationIndex(){
        return adjustedStandardDeviation()/rootMeanSquareError();
    }

    public double numberOfStrata(){
        return (4.0*separationIndex()+1)/3.0;
    }

    public double reliability(){
        return adjustedVariance()/var.getResult();
    }

}

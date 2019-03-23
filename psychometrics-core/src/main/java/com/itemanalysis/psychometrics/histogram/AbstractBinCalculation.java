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


import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

/**
 * An abstract implementation of {@link BinCalculation}. It holds the instance variables and method used by each 
 * specific implementation. It allows for a storeless computation the number of bins by incrementally updating 
 * summary statistics.
 * 
 * @author J. Patrick Meyer
 */
public abstract class AbstractBinCalculation implements BinCalculation{

    /**
     * Smallest observed value.
     */
    protected Min min = null;

    /**
     * Largest observe value.
     */
    protected Max max = null;

    /**
     * Standard deviation of values.
     */
    protected StandardDeviation sd = new StandardDeviation();

    /**
     * Sample size.
     */
    protected double n = 0.0;

    /**
     * Creates the object and instantiates the min and max objects.
     */
    public AbstractBinCalculation(){
        min = new Min();
        max = new Max();
    }

    public void evaluate(double[] x){
        for(int i=0;i<x.length;i++){
            increment(x[i]);
        }
    }

    /**
     * Update the summary statistics with a new value.
     * 
     * @param x a data value.
     */
    public void increment(double x){
        min.increment(x);
        max.increment(x);
        sd.increment(x);
        n++;
    }

    /**
     * Gets the sample size.
     * 
     * @return sample size.
     */
    public double sampleSize(){
        return n;
    }

    /**
     * Gets the smallest observed data points.
     * 
     * @return smallest value.
     */
    public double min(){
        return min.getResult();
    }

    /**
     * Gets the largest observed data point.
     * 
     * @return largest value.
     */
    public double max(){
        return max.getResult();
    }


}

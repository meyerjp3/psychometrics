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

import java.io.Serializable;

/**
 * A class representing a single histogram bin. A bin is characterized by a lower bound, upper bound, frequency
 * count, and bin width. This class form a basic element in constructing a histogram. The bin can be lower inclusive,
 * upper inclusive, or both. The constructors require the user to specify whether the bin is lower or upper inclusive.
 * Bin frequencies are counted incrementally. Thus, a bin is storeless in that is does not store each individual
 * value in the bin. It only records summary statistics.
 *
 * @author J. Patrick Meyer
 * 
 */
public class Bin  implements Cloneable, Serializable{

    private double lowerBound=0.0;

	private double upperBound=0.0;

	private boolean lowerInclusive=true;

	private double count = 0.0;

	private double sum = 0.0;

    private double binWidth = 1.0;

    /**
     * Creates a histogram bin with the most amount of flexibility in the arguments.
     *
     * @param lowerBound lower bound of the bin.
     * @param upperBound upper bound of the bin.
     * @param lowerInclusive a value equal to the lower bound is counted in this bin if true. Otherwise, it is not
     *                       counted in this bin.
     */
	public Bin(double lowerBound, double upperBound, boolean lowerInclusive){
        this.lowerBound=lowerBound;
		this.upperBound=upperBound;
		this.lowerInclusive=lowerInclusive;
        binWidth = upperBound-lowerBound;
	}

    /**
     * This constructor is mainly used when the bin is used for something other than a histogram.
     * See {@link com.itemanalysis.psychometrics.statistics.Deciles}.
     *
     * @param lowerBound lower bound of the bin.
     * @param upperBound upper bound of the bin.
     * @param lowerInclusive a value equal to the lower bound is counted in this bin if true. Otherwise, it is not
     *                       counted in this bin.
     * @param upperInclusive a value equal to the upper bound is counted in this bin if true. Otherwise, it is not
     *                       counted in this bin.
     */
    public Bin(double lowerBound, double upperBound, boolean lowerInclusive, boolean upperInclusive){
        this.lowerBound=lowerBound;
		this.upperBound=upperBound;
		this.lowerInclusive=lowerInclusive;
    }

    /**
     * Incrementally count a value as belonging to this bin if it fits within the bounds.
     *
     * @param value a value to be counted.
     */
	public void increment(double value){
		if(lowerTest(value) && upperTest(value)){
			sum+=value;
			count++;
		}
	}

    /**
     * Incrementally count a value as belonging to this bin if it fits within the bounds. The observation is
     * weighted by the frequency argument.
     *
     * @param value a value to be counted.
     * @param frequency a frequency weight for the observation.
     */
	public void increment(double value, double frequency){
		if(lowerTest(value) && upperTest(value)){
			sum+=value;
			count+=frequency;
		}
	}

    /**
     * Tests whether a value belongs to this bin.
     *
     * @param value a value to be tested.
     * @return true if the value belongs to the bin. False otherwise.
     */
    public boolean inBin(double value){
        if(lowerTest(value) && upperTest(value)){
            return true;
        }
        return false;
    }

    /**
     * An internal test of whether the value is within the lower bound. The test depends on whether the bin is
     * lower inclusive or not.
     *
     * @param value a value to be tested.
     * @return true if the value passes the lower bound test and false otherwise.
     */
	private boolean lowerTest(double value){
		if(lowerInclusive){
			if(value >= lowerBound) return true;
		}else{
			if(value > lowerBound) return true;
		}
		return false;
	}

    /**
     * An internal test of whther the value is within the upper bound. The test depends on whether the bin is
     * upper inclusive or not.
     *
     * @param value a value to be tested.
     * @return true if the value passes the upper bound test and false otherwise.
     */
	private boolean upperTest(double value){
        if(lowerInclusive){
            if(value < upperBound) return true;
        }else{
            if(value <= upperBound) return true;
        }
		return false;
	}

    /**
     * Get the bin midpiont.
     *
     * @return bin midpoint.
     */
	public double getMidPoint(){
		if(lowerBound==upperBound) return upperBound;
		return lowerBound+(upperBound-lowerBound)/2;
	}

    /**
     * Gets the mean of the bin.
     *
     * @return bin mean.
     */
	public double getBinMean(){
        if(count==0.0) return Double.NaN;
		return sum/count;
	}

    /**
     * Gets the frequency of observations in the bin.
     *
     * @return frequency of observations in the bin.
     */
	public double getFrequency(){
		return count;
	}

    /**
     * Gets the lower bound of the bin.
     *
     * @return the lower bound.
     */
	public double getLowerBound(){
		return lowerBound;
	}

    /**
     * Gets the upper bound of the bin.
     *
     * @return the upper bound.
     */
	public double getUpperBound(){
		return upperBound;
	}

    /**
     * Gets a boolean indicating whether the bin is lower inclusive or not.
     *
     * @return true if lower inclusive, false otherwise.
     */
	public boolean lowerInclusive(){
		return lowerInclusive;
	}

    /**
     * Gets the bin width.
     *
     * @return bin width.
     */
	public double getBinWidth(){
		return binWidth;
	}

    /**
     * Evaluates the equality of of two bins.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Bin) {
            Bin bin = (Bin) obj;
            boolean b0 = bin.lowerBound == this.lowerBound;
            boolean b1 = bin.upperBound == this.upperBound;
            boolean b2 = bin.count == this.count;
            return b0 && b1 && b2;
        }
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

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

import com.itemanalysis.psychometrics.histogram.Histogram.HistogramType;

import java.io.Serializable;

/**
 *
 * @author J. Patrick Meyer
 * 
 */
public class Bin  implements Cloneable, Serializable{

	private static final long serialVersionUID = 7614685080015589931L;

    private double lowerBound=0.0;

	private double upperBound=0.0;

	private boolean lowerInclusive=true;

	private boolean upperInclusive=true;

	private double count = 0.0;

	private double sum = 0.0;

    private double binWidth = 1.0;

    private double sampleSize = 0.0;

    private HistogramType type = HistogramType.FREQUENCY;

	public Bin(double sampleSize, double lowerBound, double upperBound, boolean lowerInclusive, boolean upperInclusive, HistogramType type){
		this.sampleSize = sampleSize;
        this.lowerBound=lowerBound;
		this.upperBound=upperBound;
		this.lowerInclusive=lowerInclusive;
		this.upperInclusive=upperInclusive;
        this.type = type;
        binWidth = upperBound-lowerBound;
	}

    /**
     * This constructor is mainly used when the bin is used for something other than a histogram.
     * See Decile.java.
     *
     * @param lowerBound
     * @param upperBound
     * @param lowerInclusive
     * @param upperInclusive
     */
    public Bin(double lowerBound, double upperBound, boolean lowerInclusive, boolean upperInclusive){
        this.lowerBound=lowerBound;
		this.upperBound=upperBound;
		this.lowerInclusive=lowerInclusive;
		this.upperInclusive=upperInclusive;
        type = HistogramType.FREQUENCY;
    }

	public void increment(double value){
		if(lowerTest(value) && upperTest(value)){
			sum+=value;
			count++;
		}
	}

	public void increment(double value, double frequency){
		if(lowerTest(value) && upperTest(value)){
			sum+=value;
			count+=frequency;
		}
	}

    public boolean inBin(double value){
        if(lowerTest(value) && upperTest(value)){
            return true;
        }
        return false;
    }

	private boolean lowerTest(double value){
		if(lowerInclusive){
			if(value >= lowerBound) return true;
		}else{
			if(value > lowerBound) return true;
		}
		return false;
	}

	private boolean upperTest(double value){
		if(upperInclusive){
			if(value <= upperBound) return true;
		}else{
			if(value < upperBound) return true;
		}
		return false;
	}

	public double getMidPoint(){
		if(lowerBound==upperBound) return upperBound;
		return lowerBound+(upperBound-lowerBound)/2;
	}

    public double getValue(){
        if(type==HistogramType.RELATIVE_FREQUENCY){
            return getFrequency();
        }else if(type==HistogramType.DENSITY){
            return getDensity();
        }else{
            return getFrequency();
        }
    }

	public double getBinMean(){
        if(count==0.0) return Double.NaN;
		return sum/count;
	}

	private double getFrequency(){
		return count;
	}

    private double getRelativeFrequency(){
        if(sampleSize==0.0) return Double.NaN;
        return count/sampleSize;
    }

    private double getDensity(){
        if(sampleSize==0.0 || binWidth==0.0) return Double.NaN;
        return count/(sampleSize*binWidth);
    }

	public double getLowerBound(){
		return lowerBound;
	}

	public double getUpperBound(){
		return upperBound;
	}

	public boolean lowerInclusive(){
		return lowerInclusive;
	}

	public boolean upperInclusive(){
		return upperInclusive;
	}

	public double getBinWidth(){
		return binWidth;
	}

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

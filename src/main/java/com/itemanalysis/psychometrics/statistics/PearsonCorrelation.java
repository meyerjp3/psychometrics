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
package com.itemanalysis.psychometrics.statistics;


/**
 * Computes a Pearson correlation. Individual values are not stored in memory.
 * Can either supply the x and y arrays, or incrementally update the
 * calculation by calling increment(), or both.
 *
 *
 * @author J. Patrick Meyer
 */
public class PearsonCorrelation implements Comparable<PearsonCorrelation>{

    private StreamingCovariance covariance = null;

	public PearsonCorrelation(boolean unbiased){
        covariance = new StreamingCovariance(unbiased);
	}

	public PearsonCorrelation(StreamingCovariance covariance){
	    this.covariance = covariance;
    }

    public PearsonCorrelation(double[] x, double[] y){
	    for(int i=0;i<x.length;i++){
	        this.increment(x[i], y[i]);
        }
    }

	public PearsonCorrelation(){
		this(true);
	}

	public void increment(Double X, Double Y){
        if(X!=null && Y!=null){
            covariance.increment(X, Y);
        }
	}

	/**
	 * Correct correlation for spuriousness. This method assumes that
	 * the test item is Y and the test score is X. Used for the
     * point-biserial and biserial correlation in an item analysis.
	 *
	 * @return correlation corrected for spuriousness
	 */
	public Double correctedValue(){
		double testSd = covariance.sdX();
		double itemSd = covariance.sdY();
		double rOld = this.value();
        double denom = Math.sqrt(itemSd*itemSd+testSd*testSd-2*rOld*itemSd*testSd);
        if(denom==0.0) return Double.NaN;
		return (rOld*testSd-itemSd)/denom;
	}

	public double value(){
         return covariance.correlation();
    }

    public double standardError(){
        return covariance.correlationStandardError();
    }

    public double pValue(){
        return covariance.correlationPvalue();
    }

    public double sampleSize(){
        return covariance.sampleSize();
    }

	public int compareTo(PearsonCorrelation that){
        double thisV = this.value();
        double thatV = that.value();
		if(thisV>thatV) return 1;
		if(thisV<thatV) return -1;
		return 0;
	}

    @Override
	public boolean equals(Object obj){
		if(this==obj)return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		Double v = new Double(this.value());
		return ((Double)obj)==v;

	}

    @Override
	public int hashCode(){
		Double v = new Double(this.value());
		return v.hashCode();
	}

}

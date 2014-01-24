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
package com.itemanalysis.psychometrics.polycor;


import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * This class is mainly a short-cut for the Pearson correlation.
 * The same computations are provided in Covariance.java with the exception
 * of the correctedValue() method. This method is only found here and it
 * is used primarily for correcting a correlation for spuriousness
 * for an item analysis.
 *
 *
 * @author J. Patrick Meyer
 */
public class PearsonCorrelation implements Comparable<PearsonCorrelation>{

    private Covariance covariance = null;

    private StandardDeviation sdX = null;

    private StandardDeviation sdY = null;

	public PearsonCorrelation(){
        covariance = new Covariance();
        sdX = new StandardDeviation();
        sdY = new StandardDeviation();
	}

	public void increment(Double X, Double Y){
        if(X!=null || Y!=null){
            covariance.increment(X, Y);
            sdX.increment(X);
            sdY.increment(Y);
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
		double testSd = sdX.getResult();
		double itemSd = sdY.getResult();
		double rOld = this.value();
        double denom = Math.sqrt(itemSd*itemSd+testSd*testSd-2*rOld*itemSd*testSd);
        if(denom==0.0) return Double.NaN;
		return (rOld*testSd-itemSd)/denom;
	}

	public double value(){
        return covariance.correlation().doubleValue();
	}

    public double value(boolean unbiased){
        return covariance.correlation(unbiased).doubleValue();
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

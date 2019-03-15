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


import org.apache.commons.math3.distribution.TDistribution;

/**
 * 
 * @author J. Patrick Meyer
 */
public class StreamingCovariance implements Comparable<StreamingCovariance>{

	private double deltaX = 0.0;
    private double deltaY = 0.0;
    private double meanX = 0.0;
    private double meanY = 0.0;
	private double N=0;
	private double covarianceNumerator=0.0;
    private double varNumeratorX = 0.0;
    private double varNumeratorY = 0.0;
    private double covariance = 0;
    boolean unbiased = true;

    public StreamingCovariance(boolean unbiased){
        this.unbiased = unbiased;
    }

	public StreamingCovariance(){
        this(true);
	}

	public StreamingCovariance(double[] x, double[] y, boolean unbiased){
	    this.unbiased = unbiased;
	    for(int i=0;i<x.length;i++){
	        this.increment(x[i], y[i]);
        }
    }

    public StreamingCovariance(double[] x, double[] y){
	    this(x, y, true);
    }

    /**
     * Update formula for recursive on-line (i.e. one pass) method of computing the covariance.This method
     * is given by XXXX. It is an extension of the on-line mean and variance algorithms by Knuth (1998) and
     * Welford (1962). It is more numerically accurate than the computational formula (i.e. naive) for
     * the covariance. The standard deviation algorithms are also more numerically accurate than the
     * computational formula (i.e. naive).
     *
     * Donald E. Knuth (1998). The Art of Computer Programming, volume 2: Seminumerical Algorithms, 3rd edn., p. 232. Boston: Addison-Wesley.
     * B. P. Welford (1962)."Note on a method for calculating corrected sums of squares and products". Technometrics 4(3):419–420.
     *
     * @param x
     * @param y
     */
    public void increment(double x, double y){
        N++;
        deltaX = x - meanX;
        deltaY = y - meanY;
        meanX += deltaX/N;
        meanY += deltaY/N;
        covarianceNumerator += ((N-1.0)/N)*deltaX*deltaY;
        varNumeratorX += deltaX*(x-meanX);
        varNumeratorY += deltaY*(y-meanY);
    }

    public double value(){
        if(unbiased){
            if(N<2) return Double.NaN;
            return covarianceNumerator/(N-1.0);
        }else{
            if(N<1) return Double.NaN;
            return covarianceNumerator/N;
        }
    }
    
    public double varX(){
        if(unbiased){
            if(N<2) return Double.NaN;
            return varNumeratorX/(N-1.0);
        }else{
            if(N<1) return Double.NaN;
            return varNumeratorX/N;
        }
    }

    public double sdX(){
        return Math.sqrt(varX());
    }
    
    public double varY(){
        if(unbiased){
            if(N<2) return Double.NaN;
            return varNumeratorY/(N-1.0);
        }else{
            if(N<1) return Double.NaN;
            return varNumeratorY/N;
        }
    }

    public double sdY(){
        return Math.sqrt(varY());
    }

    public double correlation(){
        double cv = value();
        double r = cv/(Math.sqrt(varX())*Math.sqrt(varY()));
        return r;
    }

    public double correlationStandardError(){
        if(N<3) return Double.NaN;
        double r = correlation();
        double r2 = Math.pow(r,2);
        double se = Math.sqrt((1-r2)/(N-2.0));
        return se;
    }

    public double correlationPvalue(){
        double se = correlationStandardError();
        if(se<=0.0) return Double.NaN;
        double r = correlation();
        double tval = r/se;
        double df = N-2.0;
        TDistribution t = new TDistribution(df);
        double pvalue = 1-t.cumulativeProbability(tval);
        double twoSidedPvalue = 2.0*Math.min(pvalue, 1-pvalue);//from R function cor.test()
        return twoSidedPvalue;
    }

	public double sampleSize(){
		return N;
	}

	public int compareTo(StreamingCovariance that){
		if(this.value()>that.value()) return 1;
		if(this.value()<that.value()) return -1;
		return 0;
	}

	public boolean equals(Object obj){
		if(this==obj)return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		return ((Double)obj)==this.value();

	}

	public int hashCode(){
		return Double.valueOf(value()).hashCode();
	}

}

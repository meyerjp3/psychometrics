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

import com.itemanalysis.psychometrics.statistics.PearsonCorrelation;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Iterator;

/**
 * This class computes the polyserial correlation between a continuous X variable and
 * an ordered categorical Y variable. It is consistent with the polyserial function
 * from the polycor package in R. It uses teh plugin method of calculation.
 *
 *
 * @author J. Patrick Meyer
 */
public class PolyserialCorrelationPlugin {

    private PearsonCorrelation r = null;

    private Frequency freqY = null;

    private StandardDeviation sdX = null;

    private StandardDeviation sdY = null;

    private NormalDistribution norm = null;
    
    public PolyserialCorrelationPlugin(){
        r = new PearsonCorrelation();
        sdX = new StandardDeviation();
        sdY = new StandardDeviation();
        freqY = new Frequency();
        norm = new NormalDistribution();
    }

    public void increment(double x, int y){
        r.increment(x, (double)y);
        sdX.increment(x);
        sdY.increment(y);
        freqY.addValue(y);
    }
    
    public double[] getThresholds(){
        double[] alpha = new double[freqY.getUniqueCount()-1];
        Iterator<Comparable<?>> iter = freqY.valuesIterator();
        Comparable<?> v = null;
        int index = 0;
        while(iter.hasNext()){
            v = iter.next();
            if(iter.hasNext()){
                alpha[index] = norm.inverseCumulativeProbability(freqY.getCumPct(v));
                index++;
            }
        }
        return alpha;
        
    }

    public double value(){
        double[] thresholds = null;
        thresholds = getThresholds();
        double thresholdProbSum = 0.0;
        for(int i=0;i<thresholds.length;i++){
            thresholdProbSum+=norm.density(thresholds[i]);
        }
        if(thresholdProbSum==0.0) return Double.NaN;
        double n = (double)freqY.getSumFreq();
        double psr = Math.sqrt((n-1.0)/n)*sdY.getResult()*r.value()/thresholdProbSum;
        return psr;
    }

    /**
	 * Correct pearson correlation for spuriousness due to including the studied
     * item score Y in the computation of X values. This method is used for the
     * polyserial correlation in an item analysis.
	 *
	 * @return correlation corrected for spuriousness
	 */
	public double spuriousCorrectedPearsonCorrelation(){
		double testSd = sdX.getResult();
		double itemSd = sdY.getResult();
		double rOld = r.value();
        double denom = Math.sqrt(itemSd*itemSd+testSd*testSd-2*rOld*itemSd*testSd);
        if(denom==0.0) return Double.NaN;
        return (rOld*testSd-itemSd)/denom;
	}

    /**
     * Correct polyserial correlation for spuriousness due to including the studied
     * item score Y in the computation of X values. This method is used for the
     * polyserial correlation in an item analysis.
	 *
	 * @return correlation corrected for spuriousness
     */
    public double spuriousCorrectedValue(){
        double[] thresholds = null;
        double correctedR = spuriousCorrectedPearsonCorrelation();
        thresholds = getThresholds();
        double thresholdProbSum = 0.0;
        for(int i=0;i<thresholds.length;i++){
            thresholdProbSum+=norm.density(thresholds[i]);
        }
        if(thresholdProbSum==0.0) return Double.NaN;
        double n = (double)freqY.getSumFreq();
        double psr = Math.sqrt((n-1.0)/n)*sdY.getResult()*correctedR/thresholdProbSum;
        return psr;
    }


}

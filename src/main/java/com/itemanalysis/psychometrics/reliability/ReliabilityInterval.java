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

import org.apache.commons.math3.distribution.FDistribution;

import java.util.Formatter;

/**
 *
 * Confidence interval is based on the sampling quadrature derived for
 * Cronbach's alpha. It may or may not be appropriate for other methods
 * of estimating reliability.
 *
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class ReliabilityInterval {
	
	private double sampleSize=0;
	private int precision=4;
	private double numberOfVariables=0;
    private ScoreReliability reliability = null;
    private boolean unbiased = false;
	
	public ReliabilityInterval(ScoreReliability reliability, double sampleSize, double numberOfVariables, boolean unbiased){
		this.reliability=reliability;
		this.sampleSize=Double.valueOf(sampleSize).intValue();
		this.numberOfVariables=numberOfVariables;
        this.unbiased = unbiased;
	}
	
	public double[] confidenceInterval(){
		double N=sampleSize;
		double nI = numberOfVariables;
		double df1=N-1.0;
		double df2=(N-1.0)*(nI-1.0);
		double[] ci=new double[2];

        try{
			FDistribution fDist = new FDistribution(df1, df2);
            ci[0] = 1.0-((1.0-reliability.value())*fDist.inverseCumulativeProbability(0.975));
            ci[1] = 1.0-((1.0-reliability.value())*fDist.inverseCumulativeProbability(0.025));
        }catch(Exception ex){
            ci[0] = Double.NaN;
            ci[1] = Double.NaN;
        }

		
		return ci;
	}

	public String print(){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		String f2="";
		if(precision==2){
			f2="%.2f";
		}else if(precision==4){
			f2="%.4f";
		}

        f.format("%18s", "95% Confidence Interval: (");
        f.format(f2,this.confidenceInterval()[0]);
        f.format("%2s", ", ");
        f.format(f2,this.confidenceInterval()[1]);
        f.format("%1s", ")");
		
		return f.toString();
	}

}

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

import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class KR21 extends AbstractScoreReliability{
    
    private double mean=0.0;

    private double sd=0.0;
	
	public KR21(int numberOfItems, double testMean, double testStdDev, boolean unbiased){
		nItems=numberOfItems;
		mean=testMean;
		sd=testStdDev;
	}

    public KR21(int numberOfItems, double testMean, double testStdDev){
        this(numberOfItems, testMean, testStdDev, false);
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.KR21;
    }
	
	public double value(){
        double nI = (double)nItems;
		double c=nItems/(nItems-1.0);
		double kr21=c*(1.0-(mean*(nI-mean))/(nI*sd*sd));
		return kr21;
	}

    @Override
    public double[] itemDeletedReliability(){
        double[] delRel = new double[nItems];
        for(int i=0;i<nItems;i++){
            delRel[i] = value();
        }
        return delRel;
    }

    @Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		String f2="%.4f";
		f.format("%7s", "KR-21: "); f.format(f2,this.value());
		return f.toString();
	}

//    public String printItemDeletedSummary(ArrayList<DefaultVariableAttributes> var){
//        return "";
//    }


}

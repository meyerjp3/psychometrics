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

import com.itemanalysis.psychometrics.data.VariableInfo;
import org.apache.commons.math3.distribution.FDistribution;

import java.util.ArrayList;
import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class KR21 implements  ScoreReliability, Comparable<KR21>{

	private int precision=4;

    private double kr21=0.0;
    
    private double nI=0.0;
    
    private double mean=0.0;

    private double sd=0.0;

    private double[] confidenceInterval = {0.0,0.0};

    private double[] cdel = null;

    private static final String name = "KR21";
	
	public KR21(double numberOfItems, double testMean, double testStdDev){
		nI=numberOfItems;
		mean=testMean;
		sd=testStdDev;
        cdel = new double[1];
	}
	
	public double value(boolean unbiased){
		double c=nI/(nI-1.0);
		kr21=c*(1.0-(mean*(nI-mean))/(nI*sd*sd));
		return kr21;
	}

    public double[] valueIfItemDeleted(){
		return cdel;
	}

    public void incrementValueIfItemDeleted(int index, double value){
        cdel[0]=this.value(false);
    }

    public double sem(boolean unbiased){
		return sd*Math.sqrt((1-this.value(unbiased)));
	}

    public String name(){
        return name;
    }

    public double[] confidenceInterval(double numberOfExaminees, boolean unbiased){
        double numberOfItems = nI;
		double df1=numberOfExaminees-1.0;
		double df2=(numberOfExaminees-1.0)*(numberOfItems-1.0);
        FDistribution fDist = new FDistribution(df1, df2);
        try{
            confidenceInterval[0] = 1.0-((1.0-this.value(unbiased))*fDist.inverseCumulativeProbability(0.975));
            confidenceInterval[1] = 1.0-((1.0-this.value(unbiased))*fDist.inverseCumulativeProbability(0.025));
        }catch(Exception ex){
            confidenceInterval[0] = Double.NaN;
            confidenceInterval[1] = Double.NaN;
        }


		return confidenceInterval;
	}

    public String confidenceIntervalToString(){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		f.format("(%6.4f, ",confidenceInterval[0]);
        f.format("%6.4f)",confidenceInterval[1]);
		return f.toString();
	}

	public String print(boolean unbiased){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		String f2="%.4f";
		f.format("%7s", "KR-21: "); f.format(f2,this.value(unbiased));
		return f.toString();
	}

    public String ifDeletedToString(ArrayList<VariableInfo> var){
        return "";
    }

    @Override
	public boolean equals(Object obj){
		if(this==obj)return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		Double v = new Double(kr21);
		return ((Double)obj)==v;
	}

    @Override
	public int hashCode(){
		Double v = new Double(kr21);
		return v.hashCode();
	}

    public int compareTo(KR21 that){
		if(this.value(true)>that.value(true)) return 1;
		if(this.value(true)<that.value(true)) return -1;
		return 0;
	}


}

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
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;
import org.apache.commons.math3.distribution.FDistribution;

import java.util.ArrayList;
import java.util.Formatter;



public class FeldtBrennan implements ScoreReliability, Comparable<FeldtBrennan>{
	
	private CovarianceMatrix matrix = null;
	private int precision=4, n=0;
	private double fcc = 0.0;
    private double[] confidenceInterval = {0.0,0.0};
    private double[] cdel = null;
	private static final String name = "FB";
	
	public FeldtBrennan(CovarianceMatrix matrix){
		this.matrix=matrix;
        n=matrix.getNumberOfVariables();
        cdel = new double[n];
	}
	
	private double squaredRowSums(){
		double value=0.0;
		for(int i=0;i<n;i++){
			value+=Math.pow(matrix.rowSum(i, true), 2);
		}
		return value;
	}
	
	public double[] valueIfItemDeleted(){
		return cdel;
	}

    public void incrementValueIfItemDeleted(int index, double value){
        cdel[index]=value;
    }
	
	public double sem(boolean unbiased){
		return Math.sqrt(matrix.totalVariance(true)*(1-this.value(unbiased)));
	}

    public String name(){
        return name;
    }

    public double[] confidenceInterval(double numberOfExaminees, boolean unbiased){
        double numberOfItems = (double)matrix.getNumberOfVariables();
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
		String f2="";
		if(precision==2){
			f2="%.2f";
		}else if(precision==4){
			f2="%.4f";
		}
		
		f.format("%16s", "Feldt-Brennan = "); f.format(f2,this.value(unbiased));
		return f.toString();
	}
    
    public String ifDeletedToString(ArrayList<VariableInfo> var){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = valueIfItemDeleted();
        f.format("%-56s", " Feldt-Brennan if Item Deleted"); f.format("%n");
		f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", var.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }
	
	public double value(boolean unbiased){
		double observedScoreVariance = matrix.totalVariance(unbiased);
		double componentVariance = matrix.diagonalSum(unbiased);
		fcc=(observedScoreVariance*(observedScoreVariance-componentVariance))/(Math.pow(observedScoreVariance, 2)-this.squaredRowSums());
		return fcc;
	}
	
	public int compareTo(FeldtBrennan that){
		if(this.fcc>that.fcc) return 1;
		if(this.fcc<that.fcc) return -1;
		return 0;
	}
	
	public boolean equals(Object obj){
		if(this==obj)return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		Double v = new Double(fcc);
		return ((Double)obj)==v;
		
	}
	
	public int hashCode(){
		Double v = new Double(fcc);
		return v.hashCode();
	}

}

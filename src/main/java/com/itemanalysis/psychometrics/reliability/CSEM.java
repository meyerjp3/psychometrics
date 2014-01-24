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
public class CSEM implements Comparable<CSEM>{
	
	private double CSEM=0.0;
    
    private double trueScore=0.0;

    private double maximumPossibleScore=0.0;
    
    private double reliability=0.0;

    private double kr21=0.0;
	
	public CSEM(double cutScore, double maximumPossibleScore, double reliability, double kr21){
		trueScore=cutScore;
		this.maximumPossibleScore=maximumPossibleScore;
		this.reliability=reliability;
		this.kr21=kr21;
	}
	
	public double value(){	
		double first=(1.0-reliability)/(1.0-kr21);
		double second=(trueScore*(maximumPossibleScore-trueScore))/(maximumPossibleScore-1.0);
		CSEM=Math.sqrt(first*second);
		return CSEM;
	}

    public double trueScore(){
        return trueScore;
    }
	
	public int compareTo(CSEM that){
		if(this.CSEM>that.CSEM) return 1;
		if(this.CSEM<that.CSEM) return -1;
		return 0;
	}

    @Override
	public boolean equals(Object obj){
		if(this==obj)return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		Double v = new Double(CSEM);
		return ((Double)obj)==v;
		
	}

    @Override
	public int hashCode(){
		Double v = new Double(CSEM);
		return v.hashCode();
	}

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String f2="%.4f";
        f.format("%13s", "True Score = "); f.format(f2, this.trueScore());  f.format("%n");
        f.format("%7s", "CSEM = "); f.format(f2, this.value()); f.format("%n");
        f.format("%n");
        return f.toString();
    }

}

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
public class ConditionalSEM implements Comparable<ConditionalSEM>{

    private double[] CSEM = null;

    private Integer[] cut = null;

    private double MPS = 0.0;

    private ScoreReliability reliability = null;

    private KR21 kr = null;

    private boolean unbiased = false;

	public ConditionalSEM(Integer[] cutScore, double maximumPossibleScore, ScoreReliability reliability, KR21 kr21, boolean unbiased){
		cut=cutScore;
		MPS=maximumPossibleScore;
		this.reliability=reliability;
		kr=kr21;
        this.unbiased = unbiased;
        CSEM = new double[cut.length];
	}

	public double[] value(){
        double[] first = new double[cut.length];
        double[] second = new double[cut.length];
        double tempCut = 0.0;
        for(int i=0;i<first.length;i++){
            tempCut = (double)cut[i];
            first[i]=(1.0-reliability.value())/(1.0-kr.value());
            second[i]=(tempCut*(MPS-tempCut))/(MPS-1.0);
            CSEM[i]=Math.sqrt(first[i]*second[i]);
        }
		return CSEM;
	}

	public int compareTo(ConditionalSEM that){
		if(this.CSEM[0]>that.CSEM[0]) return 1;
		if(this.CSEM[0]<that.CSEM[0]) return -1;
		return 0;
	}

	public boolean equals(Object obj){
		if(this==obj)return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		Double v = new Double(CSEM[0]);
		return ((Double)obj)==v;

	}

	public int hashCode(){
		Double v = new Double(CSEM[0]);
		return v.hashCode();
	}

    public String print(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        this.value();

        f.format("%n");
        f.format("%45s", "CONDITIONAL STANDARD ERROR OF MEASUREMENT    "); f.format("%n");
        f.format("%45s", "============================================="); f.format("%n");
        f.format("%10s", "True Score"); f.format("%5s", ""); f.format("%10s", "CSEM"); f.format("%n");
        f.format("%45s", "---------------------------------------------"); f.format("%n");
        for(int i=0;i<cut.length;i++){
            f.format("%10.4s", cut[i]); f.format("%5s", ""); f.format("%10.4s", CSEM[i]); f.format("%n");
        }
        f.format("%45s", "---------------------------------------------"); f.format("%n");
        return f.toString();
    }

}

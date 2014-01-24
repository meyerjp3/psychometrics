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
package com.itemanalysis.psychometrics.scaling;


import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.Frequency;

import java.util.Formatter;
import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class PercentileRank {

    private Frequency freqTable = null;

    private Integer min = Integer.MIN_VALUE;
    
    private Integer max = Integer.MAX_VALUE;

    private int precision = 4;

    private TreeMap<Integer, Double> prankTable = null;

    public PercentileRank(Integer min, Integer max){
        freqTable = new Frequency();
        this.min = min;
        this.max = max;
	}

    public PercentileRank(){
		this(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public int getSize(){
		return freqTable.getUniqueCount();
	}

    public void addValue(Integer score){
        if(score>=min && score<=max){
            freqTable.addValue(score.intValue());
        }
    }

    /**
     * All doubles are converted to integers see page 44, Kolen and Brennan(2004).
     *
     * @param score a test score.
     */
    public void addValue(Double score){
        int iScore = Double.valueOf(Math.floor(score+0.5)).intValue();
        if(iScore>=min && iScore<=max){
            freqTable.addValue(iScore);
        }
    }

    public void addAllValues(int[] x){
        for(int i=0;i<x.length;i++){
            this.addValue(x[i]);
        }
    }

    public void addAllValues(double[] x){
        for(int i=0;i<x.length;i++){
            this.addValue(x[i]);
        }
    }

	public void clear(){
		freqTable.clear();
	}

	public long getCount(Integer score){
        return freqTable.getCount(score.intValue());
	}

    /**
     * Creates a TreeMap<Integer, Double> lookup table of percentile ranks.
     * The key is a score level and the rho is a percentile rank. This
     * method is useful when finding percentile ranks that correspond to an examinee's
     * raw score. After calling this method, individual elements in the
     * TreeMap can be accessed with getPercentileRankAt(int rho) or
     * valueIterator().
     * 
     */
    public void createLookupTable(){
        prankTable = new TreeMap<Integer, Double>();
        Iterator<Comparable<?>> iter = freqTable.valuesIterator();

        int index = 0;
        int length = freqTable.getUniqueCount();
        double[] xval = new double[length+2];
        double[] yval = new double[length+2];

        int x = 0;
        int xstar = 0;

        //create boundaries below the minimum possible test score
        //and above teh maximum possible test score.
        //This change allows for the interpolation of percentile
        //ranks at the min and max possible test score.
        xval[0] = min-.5;
        yval[0] = 0;
        xval[length+1] = (double)max+0.5;
        yval[length+1] = 100;

        index=1;

        //compute percentile ranks fro observed scores
        while(iter.hasNext()){
            x = ((Long)iter.next()).intValue();
            xstar = Double.valueOf(Math.floor(x+0.5)).intValue();

            xval[index] = Double.valueOf(xstar);
            yval[index] = Double.valueOf(percentileRank(x, xstar));
            index++;
        }

        //interpolate values
        LinearInterpolator interpolator = new LinearInterpolator();
        PolynomialSplineFunction splineFunction = interpolator.interpolate(xval, yval);

        //create lookup table with interpolated values
        x = min;
        double y = 0.0;
        while(x<=max){
            y = splineFunction.value(x);
            prankTable.put(x, y);
            x+=1;
        }

    }

    /**
     * For r number of score levels between min and max, inclusive, this method
     * returns a r x 2 array with integer based scores in first column
     * and percentile ranks in the second column. This method is useful when
     * only the raw scores and corresponding percentile ranks are needed.
     *
     * @return two-way array of raw scores scores and percentile ranks.
     */
    public double[][] evaluate(){
        double[][] prank = new double[freqTable.getUniqueCount()][2];
        int xstar;
        int index = 0;

        Iterator<Comparable<?>> iter = freqTable.valuesIterator();
        int x = 0;
        while(iter.hasNext()){
           x = ((Long)iter.next()).intValue();
           xstar = Double.valueOf(Math.floor(x+0.5)).intValue();
           prank[index][0] = xstar;
           prank[index][1] = percentileRank(x, xstar);
           index++;
//           System.out.println("x: " + x + " xstar: " + xstar + " Fexstar: " + Fxstar + " Fxstarm1: " + Fxstarm1 + " px: " + px + " cp: " + getCummulativeProportion(xstar));
       }
        return prank;
    }

    private double percentileRank(int x, int xstar){
        double Fxstar = Math.max(0.0, Math.min(freqTable.getCumPct(xstar), 1.0));
        double Fxstarm1 = Math.max(0.0, Math.min(freqTable.getCumPct(xstar-1), 1.0));
        double px = Math.max(0.0, Math.min(100, 100*(Fxstarm1+(x-(xstar-0.5))*(Fxstar-Fxstarm1))));
        return px;
    }

    public double[][] evaluate(int[] score){
        this.addAllValues(score);
        return this.evaluate();
    }

    public double[][] evaluate(double[] score){
        this.addAllValues(score);
        return this.evaluate();
    }

    public double getPercentileRankAt(int value){
        Integer i = Integer.valueOf(value);
        if(prankTable.containsKey(i)){
            return prankTable.get(i);
        }else{
            return 0.0;
        }
    }

    public Iterator<Integer> valueIterator(){
        return prankTable.keySet().iterator();
    }

    @Override
    public String toString(){
        if(freqTable.getUniqueCount()==0 || freqTable.getSumFreq()==0){
            return "Percentile ranks not computed.";
        }
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String f1="%10.4f";
		String f2="%10s";

//        double[][] prank = this.evaluate();
        this.createLookupTable();

        //add header
        f.format("%28s","SCORE TABLE");
        f.format("%n");
        f.format("%45s","=============================================");
        f.format("%n");
        f.format(f2,"Original");
        f.format("%5s", "");
        f.format(f2,"Percentile");
        f.format("%n");

        f.format(f2,"Value");
        f.format("%5s", "");
        f.format(f2,"Rank");
        f.format("%n");
        f.format("%45s","---------------------------------------------");
        f.format("%n");


        Iterator<Integer> iter = prankTable.keySet().iterator();
        Integer key = 1;
        while(iter.hasNext()){
            key = iter.next();
			f1="%10." + precision + "f";

            f.format(f2,key);
            f.format("%5s", "");
            f.format(f1,prankTable.get(key));
            f.format("%5s", "");
            f.format("%n");
        }
        f.format("%45s","=============================================");
        f.format("%n");
        return f.toString();
    }

}

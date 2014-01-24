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
package com.itemanalysis.psychometrics.histogram;


import com.itemanalysis.psychometrics.distribution.DistributionApproximation;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;

/**
 * Computes histogram using two passes over the data. The first pass is needed for
 * BinCalculation.java which is needed in the constructor for Histogram.java.
 * Provides midpoints and frequencies, relative frequencies, or evaluate at midpoints.
 *
 * @author J. Patrick Meyer
 *
 */
public class Histogram implements DistributionApproximation {

    private double numberOfBins = 1;

	private double binWidth = 1.0;

	private ArrayList<Bin> bins = new ArrayList<Bin>();

    private BinCalculation binCalc = null;

    private double[] points = null;

    private double[] density = null;
    
    private HistogramType type = HistogramType.FREQUENCY;

    public enum HistogramType{
        FREQUENCY, RELATIVE_FREQUENCY, DENSITY;
    }

	public Histogram(BinCalculation binCalc, HistogramType type){
        this.binCalc = binCalc;
        this.type = type;
        createBins();
	}

    private void createBins(){
		Bin bin=null;
		double start=binCalc.min();
        double max = binCalc.max();
        numberOfBins = binCalc.numberOfBins();
        binWidth = binCalc.binWidth();
        double n = binCalc.sampleSize();
        for(int i=0;i<numberOfBins;i++){
            if(i<numberOfBins-1){
                bin = new Bin(n, start, start+binWidth, true, false, type);
            }else{
                bin = new Bin(n, start, Math.min(max, start+binWidth), true, true, type);
            }
            start+=binWidth;
            bins.add(bin);
        }
	}

	public void increment(double value){
		for(Bin b : bins){
            b.increment(value);
        }
	}

    public int getNumberOfBins(){
        return bins.size();
    }

    public Bin getBinAt(int index){
        return bins.get(index);
    }

    /**
     *
     * @return midpoints of bins
     */
    public double[] getPoints(){
        if(points!=null) return points;
        points = new double[bins.size()];
		int index=0;
		for(Bin b : bins){
			points[index]=b.getMidPoint();
			index++;
		}
		return points;
    }

    public double[] evaluate(){
        if(points==null) getPoints();
        if(density!=null) return density;
        density = new double[bins.size()];
        int index=0;
        for(Bin b : bins){
            density[index]=b.getValue();
            index++;
        }
        return density;
    }

    public double getPointAt(int index){
        if(points==null) getPoints();
        return points[index];
    }

    public double getDensityAt(int index){
        if(density==null) evaluate();
        return density[index];
    }

    public int getNumberOfPoints(){
        return bins.size();
    }

    public Iterator<Bin> iterator(){
        return bins.iterator();
    }

    @Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);

		String li = "", ui="";

		f.format("%n");
		f.format("%64s", "                        HISTOGRAM VALUES                        ");f.format("%n");
		f.format("%64s", "================================================================");f.format("%n");
		f.format("%16s","Lower Bound");f.format("%18s","Upper Bound"); f.format("%15s","MidPoint"); f.format("%15s","Density");f.format("%n");
		f.format("%64s", "----------------------------------------------------------------"); f.format("%n");

		for(Bin b : bins){
			if(b.lowerInclusive()) li = "[";
			else li = "(";

			if(b.upperInclusive()) ui = "]";
			else ui = ")";

			f.format("%1s", li);f.format("% 15.5f, ",b.getLowerBound());f.format("% 15.5f",b.getUpperBound()); f.format("%1s", ui); f.format("% 15.5f",b.getMidPoint()); f.format("% 15.5f",b.getValue());
			f.format("%n");
		}
		f.format("%64s", "================================================================");f.format("%n");f.format("%n");
		return f.toString();
	}

}

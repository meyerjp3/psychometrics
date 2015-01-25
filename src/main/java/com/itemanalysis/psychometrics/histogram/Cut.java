/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.histogram;

import java.util.ArrayList;

/**
 * Cuts a continuous variable into numberOfBins intervals that range from
 * min to max. It then incrementally counts the observations in each interval.
 *
 */
public class Cut {

    private double min = 0;
    private double max = 0;
    private int numberOfBins = 0;
    private boolean lowerInclusive = true;
    SimpleBinCalculation binCalculation = null;
    private ArrayList<Bin> bins = new ArrayList<Bin>();

    public Cut(double min, double max, int numberOfBins, boolean lowerInclusive){
        this.numberOfBins = numberOfBins;
        this.lowerInclusive = lowerInclusive;
        binCalculation = new SimpleBinCalculation(numberOfBins, min, max);
        this.min =  min-1/1000;
        this.max = max+1/1000;
        createBins();
    }

    public Cut(double min, double max, int numberOfBins){
        this(min, max, numberOfBins, true);
    }

    private void createBins(){
        if(bins!=null) bins.clear();
		Bin bin=null;

        double binWidth = binCalculation.binWidth();

        for(int i=1;i<numberOfBins;i++){
            bin = new Bin(min+(i-1)*binWidth, min+i*binWidth, lowerInclusive);
            bins.add(bin);
        }
        bin = new Bin(min+(numberOfBins-1)*binWidth, max, lowerInclusive);
        bins.add(bin);
	}

    public void increment(double value){
        for(Bin b : bins){
            b.increment(value);
        }
    }

    public int getNumberOfBins(){
        return numberOfBins;
    }

    public Bin getBinAt(int index){
        return bins.get(index);
    }

    @Override
    public String toString(){
        String s = "";
        for(Bin b : bins){
            s+= "("+ b.getLowerBound() + ", " + b.getUpperBound() + ") " + b.getFrequency() + "\n";
        }
        return s;
    }

}

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

/**
 * Sturges' method for computing the number of bins.
 *
 * @author J. Patrick Meyer
 */
public class SturgesBinCalculation implements BinCalculation{

    private double n = 0;
    private double min = 0;
    private double max = 0;

    public SturgesBinCalculation(double n, double min, double max){
        this.n = n;
        this.min = min;
        this.max = max;
    }

    /**
     * Gets the number of bins as computed by Sturges' method.
     * 
     * @return number of bins.
     */
    public int numberOfBins(){
        if(n==0.0) return 1;
		double logBase2Ofn = Math.log10(n)/Math.log10(2);
        int numberOfBins = (int)Math.ceil(logBase2Ofn + 1.0);
        return numberOfBins;
    }

    /**
     * Gets the bin width according to the number of bins calculated by Sturges' method.
     * 
     * @return bin width.
     */
    public double binWidth(){
        if(n==0.0) return 1.0;
        int numberOfBins = numberOfBins();
        double binWidth = (max-min)/numberOfBins;
        return binWidth;
    }

    public BinCalculationType getType(){
        return BinCalculationType.STURGES;
    }

}

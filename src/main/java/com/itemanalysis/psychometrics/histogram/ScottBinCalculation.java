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
 * Scott's method form computing the number of bins in a histogram.
 *
 * @author J. Patrick Meyer
 */
public class ScottBinCalculation implements BinCalculation{

    private double n = 0;
    private double min = 0;
    private double max = 0;
    private double sd = 0;


    public ScottBinCalculation(double n, double min, double max, double sd){
        this.n = n;
        this.min = min;
        this.max = max;
        this.sd = sd;

    }

    public int numberOfBins(){
        if(n==0.0) return 1;
        double binWidth=3.5*sd/Math.pow(n,1.0/3.0);
        int numberOfBins=(int)Math.ceil((max-min)/binWidth);
        return numberOfBins;
    }

    public double binWidth(){
        if(n==0.0) return 1.0;
        double binWidth=3.5*sd/Math.pow(n,1.0/3.0);
        return binWidth;
    }

    public BinCalculationType getType(){
        return BinCalculationType.SCOTT;
    }

}

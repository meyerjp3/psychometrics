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
 * An implementation of {@link AbstractBinCalculation} such that the user can provide the number of bins.
 *
 * @author J. Patrick Meyer
 */
public class SimpleBinCalculation implements BinCalculation{

    private double numberOfBins = 1;
    private double min = 0;
    private double max = 0;


    public SimpleBinCalculation(int numberOfBins, double min, double max){
        this.numberOfBins = (double)numberOfBins;
        this.min = min;
        this.max = max;
    }

    public int numberOfBins(){
        return (int)numberOfBins;
    }

    public double binWidth(){
        return (max-min)/numberOfBins;
    }

    public BinCalculationType getType(){
        return BinCalculationType.SIMPLE;
    }

}

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
 *
 * @author J. Patrick Meyer
 */
public class SturgesBinCalculation extends AbstractBinCalculation{

    public SturgesBinCalculation(){

    }

    public int numberOfBins(){
        if(n==0.0) return 1;
		double logBase2Ofn = Math.log(n)/Math.log(2);
        int numberOfBins = (int)Math.ceil(logBase2Ofn + 1.0);
        return numberOfBins;
    }

    public double binWidth(){
        double n = sampleSize();
        if(n==0.0) return 1.0;
		double logBase2Ofn = Math.log(n)/Math.log(2);
        int numberOfBins = (int)Math.ceil(logBase2Ofn + 1.0);
        double binWidth = (max.getResult()-min.getResult())/numberOfBins;
        return binWidth;
    }

}

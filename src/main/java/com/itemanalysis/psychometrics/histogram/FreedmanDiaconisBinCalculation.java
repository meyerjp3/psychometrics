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

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.util.ResizableDoubleArray;

/**
 *
 * @author J. Patrick Meyer
 */
public class FreedmanDiaconisBinCalculation extends AbstractBinCalculation{

    private ResizableDoubleArray x = null;

    public FreedmanDiaconisBinCalculation(){
        x = new ResizableDoubleArray();
    }

    @Override
    public void increment(double value){
        x.addElement(value);
        min.increment(value);
        max.increment(value);
        sd.increment(value);
        n++;
    }

    public int numberOfBins(){
        if(n==0.0) return 1;
        Percentile p = new Percentile();
        double[] y = x.getElements();
        p.evaluate(y);
        double q3 = p.evaluate(y, 75);
        double q1 = p.evaluate(y, 25);
		double iqr=q3-q1;
        double binWidth=2.0*(iqr/Math.pow(n,1.0/3.0));
        int numberOfBins=(int)Math.ceil((max.getResult()-min.getResult())/binWidth);
        return numberOfBins;
    }

    public double binWidth(){
        if(n==0.0) return 1;
        Percentile p = new Percentile();
        double[] y = x.getElements();
        p.evaluate(y);
        double q3 = p.evaluate(y, 75);
        double q1 = p.evaluate(y, 25);
		double iqr=q3-q1;
        double binWidth=2.0*(iqr/Math.pow(n,1.0/3.0));
        return binWidth;
    }


}

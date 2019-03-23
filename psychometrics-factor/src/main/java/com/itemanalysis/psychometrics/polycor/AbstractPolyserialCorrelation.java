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
package com.itemanalysis.psychometrics.polycor;

import com.itemanalysis.psychometrics.statistics.PearsonCorrelation;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.ResizableDoubleArray;

public abstract class AbstractPolyserialCorrelation implements PolyserialCorrelation{

    protected ResizableDoubleArray x = null;

    protected ResizableDoubleArray y = null;

    protected NormalDistribution norm = new NormalDistribution();

    protected double[] thresholds = null;

    protected double N = 0;

    protected boolean incremental = false;

    public void summarize(double[] x, int[] y){
        if(x.length!=y.length) throw new IllegalArgumentException("X and Y are of different lengths.");
        N = (double) x.length;
        Mean meanX = new Mean();
        StandardDeviation sdX = new StandardDeviation();
        PearsonCorrelation rxy = new PearsonCorrelation();
        Frequency table = new Frequency();

        for(int i=0;i<N;i++){
            meanX.increment(x[i]);
            sdX.increment(x[i]);
            rxy.increment(x[i], (double)y[i]);
            table.addValue(y[i]);
        }

        //compute thresholds
        int nrow = table.getUniqueCount();
        double[] freqDataY = new double[nrow];
        double ntotal = table.getSumFreq();
        for(int i=0;i<(nrow-1);i++){
            freqDataY[i] = table.getCumFreq(i+1);
            thresholds[i] = norm.inverseCumulativeProbability(freqDataY[i]/ntotal);
        }
        thresholds[nrow-1] = 10;//set last threshold to a large number less than infinity

    }

    public void summarize(){
        int[] yy = new int[y.getNumElements()];
        for(int i=0;i<y.getNumElements();i++){
            yy[i] = (int)y.getElement(i);
        }
        summarize(x.getElements(), yy);
    }

    public void increment(double x, int y){
        this.x.addElement(x);
        this.y.addElement((double)y);
    }

}

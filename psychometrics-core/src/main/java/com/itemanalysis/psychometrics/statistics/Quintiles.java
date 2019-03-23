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
package com.itemanalysis.psychometrics.statistics;

import com.itemanalysis.psychometrics.histogram.Bin;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 *
 * @author J. Patrick Meyer
 */
public class Quintiles {

    private Percentile[] q = null;

    private double[] prob = {20, 40, 60, 80, 100};

    private int size = prob.length;

    public Quintiles(){
        q = new Percentile[size];
        this.intialize();
    }

    private void intialize(){
        for(int i=0;i<size;i++){
            q[i] = new Percentile();
        }
    }

    public double[] value(double[] x){
        double[] value = new double[size];
        for(int i=0;i<size;i++){
            value[i] = q[i].evaluate(x, prob[i]);
        }
        return value;
    }

    public Bin[] getBins(double[] x){
        Bin[] bins = new Bin[size];
        double[] v = value(x);
        double lower = 0.0;
        Bin b = null;
        for(int i=0;i<size;i++){
            b = new Bin(lower, v[i], i==0, true);
            lower = v[i];
            bins[i] = b;
        }
        return bins;
    }

}

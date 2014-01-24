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


import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

/**
 *
 * @author J. Patrick Meyer
 */
public abstract class AbstractBinCalculation implements BinCalculation{

    protected Min min = null;

    protected Max max = null;

    protected StandardDeviation sd = new StandardDeviation();

    protected double n = 0.0;

    public AbstractBinCalculation(){
        min = new Min();
        max = new Max();
    }

    public void increment(double x){
        min.increment(x);
        max.increment(x);
        sd.increment(x);
        n++;
    }

    public double sampleSize(){
        return n;
    }

    public double min(){
        return min.getResult();
    }

    public double max(){
        return max.getResult();
    }


}

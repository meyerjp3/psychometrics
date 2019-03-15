/*
 * Copyright 2013 J. Patrick Meyer
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

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import java.util.Formatter;

public class StreamingDescriptiveStatistics {

    private Min min = new Min();
    private Max max = new Max();
    private Mean m = new Mean();
    private StandardDeviation sd = new StandardDeviation();
    private Skewness skew = new Skewness();
    private Kurtosis kurt = new Kurtosis();

    public StreamingDescriptiveStatistics(){

    }

    public void increment(double x){
        min.increment(x);
        max.increment(x);
        m.increment(x);
        sd.increment(x);
        skew.increment(x);
        kurt.increment(x);
    }

    public double getMin(){
        return min.getResult();
    }

    public double getMax(){
        return max.getResult();
    }

    public double getMean(){
        return m.getResult();
    }

    public double getStandardDeviation(){
        return sd.getResult();
    }

    public double getSkewness(){
        return skew.getResult();
    }

    public double getKurtosis(){
        return kurt.getResult();
    }

    public long getSampleSize(){
        return m.getN();
    }

    public void clear(){
        min.clear();
        max.clear();
        m.clear();
        sd.clear();
        skew.clear();
        kurt.clear();
    }

    @Override
    public String toString(){
        return toString("DESCRIPTIVE STATISTICS");
    }

    public String toString(String title){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-50s", title);f.format("%n");
        f.format("%30s", "==============================");f.format("%n");
        f.format("%-10s", "Statistic");f.format("%5s", "");
        f.format("%10s", "Value");f.format("%5s", "");f.format("%n");
        f.format("%30s", "------------------------------");f.format("%n");
        f.format("%-10s", "N");f.format("%5s", "");
        f.format("%10.4f", (double)m.getN());f.format("%5s", "");f.format("%n");
        f.format("%-10s", "Min");f.format("%5s", "");
        f.format("%10.4f", min.getResult());f.format("%5s", "");f.format("%n");
        f.format("%-10s", "Max");f.format("%5s", "");
        f.format("%10.4f", max.getResult());f.format("%5s", "");f.format("%n");
        f.format("%-10s", "Mean");f.format("%5s", "");
        f.format("%10.4f", m.getResult());f.format("%5s", "");f.format("%n");
        f.format("%-10s", "St. Dev.");f.format("%5s", "");
        f.format("%10.4f", sd.getResult());f.format("%5s", "");f.format("%n");
        f.format("%-10s", "Skewness");f.format("%5s", "");
        f.format("%10.4f", skew.getResult());f.format("%5s", "");f.format("%n");
        f.format("%-10s", "Kurtosis");f.format("%5s", "");
        f.format("%10.4f", kurt.getResult());f.format("%5s", "");f.format("%n");
        f.format("%30s", "==============================");f.format("%n");
        return f.toString();
    }



}

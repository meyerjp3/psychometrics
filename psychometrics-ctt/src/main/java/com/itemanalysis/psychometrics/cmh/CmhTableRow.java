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
package com.itemanalysis.psychometrics.cmh;


import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.util.Formatter;
import java.util.Iterator;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class CmhTableRow {

    private Object rowValue = null;

    private Frequency columns = null;

    private double rowTotal = 0.0;

    private Mean mean = null;

    public CmhTableRow(Object rowValue){
        this.rowValue = rowValue;
        columns = new Frequency();
        mean = new Mean();
    }

    public void count(Object rowValue, Double itemScore){
        if(rowValue!=null && rowValue.equals(this.rowValue)){
            columns.addValue(itemScore);
            mean.increment(itemScore);
            rowTotal++;
        }
    }

    public boolean validCode(Object rowValue){
        return rowValue.equals(this.rowValue);
    }

    public double rowTotal(){
        return rowTotal;
    }

    public double getMean(){
        return mean.getResult();
    }
    
    /**
     * Computes the sum of scores for those values in columns.
     *
     * @return sum of scores
     */
    public double sumOfScores(){
        double sum = 0.0;
        Iterator<Comparable<?>> iter = columns.valuesIterator();
        Double score = null;
        while(iter.hasNext()){
            score = (Double)iter.next();
            sum += score*columns.getCount(score);
        }
        return sum;
    }

    public double freqAt(Double score){
        return columns.getCount(score);
    }

    public int size(){
        return columns.getUniqueCount();
    }

    /**
     * For computing common odds ratio
     *
     * @return
     */
    public double rightFrequecy(){
        Double right = 1.0;
        return columns.getCount(right);
    }

    /**
     * For computing common odds ratio
     *
     * @return
     */
    public double wrongFrequency(){
        Double wrong = 0.0;
        return columns.getCount(wrong);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-10s", rowValue); f.format("%5s", "");

        Iterator<Comparable<?>> iter =columns.valuesIterator();
        double freq = 0;

        while(iter.hasNext()){
            freq = columns.getCount(iter.next());
            f.format("%10.0f", freq);
            f.format("%5s", "");
        }
        return f.toString();
    }

}

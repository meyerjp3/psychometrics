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
package com.itemanalysis.psychometrics.scaling;

import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.statistics.LinearTransformation;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Formatter;
import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class NormalizedScore {

    private TreeMap<Integer, Double> normScoreTable = null;

    private PercentileRank prank = null;
    
    public NormalizedScore(){
        
    }

    /**
     * Creates a TreeMap<Integer, Double> lookup table of normalized scores.
     * The key is a raw score level and the rho is a normalized score. This
     * method is useful when finding normalized scores that correspond to an examinee's
     * raw score. After calling this method, individual elements in the
     * TreeMap can be accessed with getNormalizedScoreAt(int rho) or
     * valueIterator().
     *
     */
    public void createLookupTable(PercentileRank prank, LinearTransformation linear){
        this.prank = prank;
        NormalDistribution normal = new NormalDistribution();
        normScoreTable = new TreeMap<Integer, Double>();
        prank.createLookupTable();
        Iterator<Integer> iter = prank.valueIterator();
        double p = 0.0;
        double q = 0.0;
        Integer i = null;
        while(iter.hasNext()){
            i = iter.next();
            p = prank.getPercentileRankAt(i)/100.0;
            q = normal.inverseCumulativeProbability(p);
            normScoreTable.put(i, linear.transform(q));
        }
    }

    /**
     * For r number of score levels between min and max (as defined in PercentileRank),
     * inclusive, this method returns a r x 2 array with integer based scores in first
     * column and normalized scores in the second column. This method is useful when
     * only the raw scores and corresponding normalized scores are needed.
     *
     * @return two-way array of raw scores and normalized scores.
     */
    public double[][] evaluate(PercentileRank prank, DefaultLinearTransformation linear){
        this.prank = prank;
        NormalDistribution normal = new NormalDistribution();
        double[][] pr = prank.evaluate();
        double p = 0.0;
        double q = 0.0;
        for(int i=0;i<pr.length;i++){
            p = pr[i][1]/100;
            q = normal.inverseCumulativeProbability(p);
            pr[i][1] = linear.transform(q);
        }
        return pr;
    }

     public double getNormalizedScoreAt(int value){
        Integer i = Integer.valueOf(value);
        if(normScoreTable.containsKey(i)){
            return normScoreTable.get(i);
        }else{
            return 0.0;
        }
    }

     public double getNormalizedScoreAt(double value){
         int xstar = Double.valueOf(Math.floor(value+0.5)).intValue();
         return getNormalizedScoreAt(xstar);
     }

    public Iterator<Integer> valueIterator(){
        return normScoreTable.keySet().iterator();
    }

    public String printTable(int precision){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        String f1="%10." + precision + "f";
		String f2="%10s";
//		String f3="%10.4f";

        //table header
        f.format("%28s","SCORE TABLE");
        f.format("%n");
        f.format("%45s","=============================================");
        f.format("%n");
        f.format(f2,"Original");
        f.format("%5s", "");
        f.format(f2,"Percentile");
        f.format("%5s", "");
        f.format(f2,"Normalized");
        f.format("%n");

        f.format(f2,"Value");
        f.format("%5s", "");
        f.format(f2,"Rank");
        f.format("%5s", "");
        f.format(f2,"Score");
        f.format("%n");
        f.format("%45s","---------------------------------------------");
        f.format("%n");

        int rs = 0;
        double value = 0.0;
        double pr = 0.0;

        Iterator<Integer> iter = normScoreTable.keySet().iterator();
        while(iter.hasNext()){
            rs = iter.next();
            pr = prank.getPercentileRankAt(rs);
            value = normScoreTable.get(rs);
            f.format(f1,(double)rs);
            f.format("%5s", "");
            f.format(f1,pr);
            f.format("%5s", "");
            f.format(f1,value);
            f.format("%5s", "");
            f.format("%n");
        }

        f.format("%45s","=============================================");
        f.format("%n");
        return f.toString();
    }

    @Override
    public String toString(){
        return printTable(2);
    }
    
}

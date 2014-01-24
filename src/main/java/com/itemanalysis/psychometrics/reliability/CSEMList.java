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
package com.itemanalysis.psychometrics.reliability;

import java.util.Formatter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class CSEMList {

    private TreeMap<Double, CSEM> csem = null;

    private double maximumPossibleScore = 0.0;

    private double reliability = 0.0;

    private double kr21 = 0.0;

    public CSEMList(double maximumPossibleScore, double reliability, double kr21){
        csem = new TreeMap<Double, CSEM>();
        this.maximumPossibleScore = maximumPossibleScore;
        this.reliability = reliability;
        this.kr21 = kr21;
    }

    /**
     * Increment for each true score provided.
     * @param trueScore
     */
    public void increment(Double trueScore){
        CSEM temp = csem.get(trueScore);
        if(temp==null){
            temp = new CSEM(trueScore, maximumPossibleScore, reliability, kr21);
            csem.put(trueScore, temp);
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String f2="%.4f";
        Set<Double> keys = csem.keySet();
        Iterator<Double> iter = keys.iterator();
        f.format("%10s", "True Score"); f.format("%5s", " "); f.format("%10s", "CSEM");
        while(iter.hasNext()){
            Double k = iter.next();
            f.format(f2, k); f.format("%5s", " "); f.format(f2, csem.get(k)); f.format("%n");
        }
        f.format("%n");
        return f.toString();
    }

}

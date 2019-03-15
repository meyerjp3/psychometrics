/**
 * Copyright 2016 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.data.VariableName;

import java.util.HashMap;
import java.util.TreeMap;

@Deprecated
public abstract class AbstractItemResponseSummary {

    //Item name
    protected VariableName variableName = null;

    //Total number of responses to the item
    protected double totalFrequency = 0;

    //A map to store each response and the count of each response
    protected TreeMap<String, Double> stringResponseMap = null;

    //A map to store each response and the count of each response
    protected TreeMap<Double, Double> doubleResponseMap = null;

    //A map to store each response and its corresponding score value
    protected HashMap<String, Double> stringScoreMap = null;

    //A map to store each response and its correspoding score value
    protected HashMap<Double, Double> doubleScoreMap = null;

    public AbstractItemResponseSummary(VariableName variableName){
        this.variableName = variableName;
        stringResponseMap = new TreeMap<String, Double>();
        doubleResponseMap = new TreeMap<Double, Double>();

        stringScoreMap = new HashMap<String, Double>();
        doubleScoreMap = new HashMap<Double, Double>();
    }

    public VariableName getName(){
        return variableName;
    }

    public double getTotalFrequency(){
        return totalFrequency;
    }

}

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
package com.itemanalysis.psychometrics.rasch;

import com.itemanalysis.psychometrics.measurement.DefaultItemScoring;

import java.util.Formatter;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * This object is a frequency table that stores counts of scored responses to an item.
 * Frequency counts for each category are stored in an ItemCategoryFrequency object.
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class ItemFrequencyRow {

    String groupId = "";

    private TreeMap<Byte, ItemFrequencyCell> tableRow = null;

    /**
     * Largest possible item response rho
     */
    private byte iMaxPS = Byte.MIN_VALUE;

    /**
     * Smallest possible item response rho
     */
    private byte iMinPS = Byte.MAX_VALUE;

    /**
     * The number of steps is the number of response categories minus 1.
     */
    private int numberOfSteps = 1;

    public ItemFrequencyRow(String groupId, DefaultItemScoring scoring){
        this.groupId = groupId;
        tableRow = new TreeMap<Byte, ItemFrequencyCell>();
        addAllCategories(scoring);
    }

    private void addAllCategories(DefaultItemScoring scoring){
        Iterator<Object> iter = scoring.categoryIterator();
        while(iter.hasNext()){
            byte scoreValue = (byte)scoring.computeItemScore(iter.next());
            ItemFrequencyCell icf = new ItemFrequencyCell(scoreValue, groupId);
            tableRow.put(scoreValue, icf);
            iMaxPS = (byte)Math.max(iMaxPS, scoreValue);
            iMinPS = (byte)Math.min(iMinPS, scoreValue);
        }
        numberOfSteps = tableRow.size()-1;
    }

    public boolean groupMember(String groupId){
        return this.groupId.equals(groupId);
    }

    /*
     * Loop over categories and increment frequency count. This method is called when
     * summarizing a data file.
     */
    public void increment(String groupId, byte score){
        for(Byte i : tableRow.keySet()){
            tableRow.get(i).increment(score, groupId);
        }
    }

    /**
     * Resets all frequency counts to zero
     */
    public void clearAllFrequencies(){
        for(Byte i : tableRow.keySet()){
            tableRow.get(i).resetFrequencies();
        }
    }

    /*
     * This method returns the number of examinees with a response score of rho or higher.
     */
    public double Sij(byte value){
        return tableRow.get(value).Sij();
    }

    /**
     * This method returns the number of examinees with a response score of rho.
     *
     * @param value
     * @return
     */
    public double Tij(byte value){
        return tableRow.get(value).Tij();
    }

    /**
     * Sip is the item score. It assumes that the lowest possible item rho is zero.
     * The largest possible rho is mN, where m is the number of steps and
     * N is the sample size for the item.
     *
     * @return item score
     */
    public double Sip(){
        double sum = 0.0;
        for(Byte i : tableRow.keySet()){
            if(i>0){
                sum+=tableRow.get(i).Sij();
            }
        }
        return sum;
    }

    /**
     * Adjusts the item score for perfect values.
     *
     * @param adjust
     * @return
     */
    public double adjustedSip(double adjust){
        double m = this.maxItemScore();
        double itemScore = Sip();

        if(itemScore==m){
            return itemScore-adjust;
        }else if(itemScore==0){
            return adjust;
        }else{
            return itemScore;
        }
    }

    /*
     * Maximum possible item score. It is mN, where m = the number of steps
     * and N is the sample size for this item.
     */
    public double maxItemScore(){
        return Tip()*(double)numberOfSteps;
    }

    /**
     * The sum of examinees response to item i. This is the total number of examinees
     * responding to item i. It is used for obtaining the sample size for each item.
     *
     * @return number of examinees responding to the item.
     */
    public double Tip(){
        double sum = 0.0;
        for(Byte i : tableRow.keySet()){
            sum+=tableRow.get(i).Tij();
        }
        return sum;
    }

    public Iterator<Byte> iterator(){
        return tableRow.keySet().iterator();
    }

    public String printFrequencyTable(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%10s", "Category: ");
        for(Byte i : tableRow.keySet()){
            f.format("%8s", i); f.format("%5s", "");
        }

        f.format("%8s", "Valid Sum");
        f.format("%n");

        f.format("%10s", "----------");
        for(Byte i : tableRow.keySet()){
            f.format("%8s", "--------"); f.format("%5s", "-----");
        }
        f.format("%8s", "--------");
        f.format("%n");

        f.format("%10s", "Freq Tij: ");
        for(Byte i : tableRow.keySet()){
            f.format("%8.2f", tableRow.get(i).Tij()); f.format("%5s", "");
        }
        f.format("%8.2f", Tip());
        f.format("%n");

        f.format("%10s", "Freq Sij: ");
        for(Byte i : tableRow.keySet()){
            f.format("%8.2f", tableRow.get(i).Sij()); f.format("%5s", "");
        }
        f.format("%8.2f", Sip());

        return f.toString();

    }

}

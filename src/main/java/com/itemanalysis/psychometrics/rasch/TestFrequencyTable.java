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

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.measurement.DefaultItemScoring;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * This class represents a frequency table with each row corresponding to
 * a test item. The rows are ragged and contain one column for each category
 * and one column for a group identifier. The group identifiers are needed
 * for the rating scale model. Rasch items and partial credit items belong
 * to their own group.
 *
 * All items for a test are stored in this table.
 * 
 * Method provide access to the table cell frequencies Tij and Sij as well as the
 * row marginal frequencies Tip and Sip (note the index p represents summation over
 * the index it replaces) and the conditional column marginal frequencies Tpj and Spj.
 * The column margins are conditional on the group Identifier.
 * 
 * After the class has been instantiated, the addItem() method should be called
 * for each item before any other method. The addItem method creates the table rows.
 *
 * This class is the top level class. It is based on ItemFrequencyRow and ItemFrequencyCell
 * and it provides wrapper methods for those classes.
 *
 *
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class TestFrequencyTable {
    
    private LinkedHashMap<VariableName, ItemFrequencyRow> table = null;

    private HashMap<VariableName, Integer> nameToPosition = null;

    private HashMap<Integer, VariableName> positionToName = null;

    private HashMap<VariableName, Boolean> extremeItem = null;

    private HashMap<String, Byte> groupNumberOfCategories = null;

    private HashMap<VariableName, String> groupIds = null;

    //need to store number of categories for each group

    public TestFrequencyTable(){
        table = new LinkedHashMap<VariableName, ItemFrequencyRow>();
        groupNumberOfCategories = new HashMap<String, Byte>();
        groupIds = new HashMap<VariableName, String>();
        extremeItem = new HashMap<VariableName, Boolean>();
        nameToPosition = new HashMap<VariableName, Integer>();
        positionToName = new HashMap<Integer, VariableName>();
    }

    /**
     * Adds an item to the table as a new row and creates all categories.
     *
     * @param name
     * @param groupId
     * @param scoring
     */
    public void addItem(VariableName name, String groupId, Integer position, DefaultItemScoring scoring){
        ItemFrequencyRow row = new ItemFrequencyRow(groupId, scoring);
        groupNumberOfCategories.put(groupId, Double.valueOf(scoring.maximumPossibleScore()).byteValue());
        table.put(name, row);
        groupIds.put(name, groupId);
        nameToPosition.put(name, position);
        positionToName.put(position, name);
    }

    /**
     * Count an item score
     *
     * @param name name of item
     * @param groupId group identifier
     * @param score item score
     */
    public void increment(VariableName name, String groupId, byte score){
        table.get(name).increment(groupId, score);
    }

    public Iterator<VariableName> iterator(){
        return table.keySet().iterator();
    }

    public ItemFrequencyRow getRow(VariableName name){
        return table.get(name);
    }

    /**
     * This method returns the number of examinees with a response score of rho
     * to an item in groupId.
     *
     * @param value
     * @return
     */
    public double Tij(VariableName name, byte value, String groupId){
        ItemFrequencyRow row = table.get(name);
        if(row.groupMember(groupId)){
            return row.Tij(value);
        }else{
            return 0.0;
        }
    }

    /*
     * This method returns the number of examinees with a response score of rho or higher
     * to an item in groupId.
     */
    public double Sij(VariableName name, byte value, String groupId){
        ItemFrequencyRow row = table.get(name);
        if(row.groupMember(groupId)){
            return row.Sij(value);
        }else{
            return 0.0;
        }
    }

    /**
     * The sum of examinees response to item i. This is the total number of examinees
     * responding to item i. It is used for obtaining the sample size for each item.
     *
     * @return number of examinees responding to the item.
     */
    public double Tip(VariableName name){
        ItemFrequencyRow row = table.get(name);
        return row.Tip();
    }

    /**
     * Sip is the item score. It assumes that the lowest possible item rho is zero.
     * The largest possible rho is mN, where m is the number of steps and
     * N is the sample size for the item.
     *
     * @return item score
     */
    public double Sip(VariableName name){
        ItemFrequencyRow row = table.get(name);
        return row.Sip();
    }

    /**
     * Sums the number of examinees responding in category j over all items in this group.
     *
     * @param value
     * @return
     */
    public double Tpj(byte value, String groupId){
        double sum = 0.0;
        ItemFrequencyRow row = null;
        for(VariableName v : table.keySet()){
            row = table.get(v);
            if(row.groupMember(groupId)){
                sum += row.Tij(value);
            }
        }
        return sum;
    }

    /**
     * Sums the number of examinees responding in category j or higher over all items in this group.
     * This is the observed category score.
     *
     * @param value
     * @return
     */
    public double Spj(byte value, String groupId){
        double sum = 0.0;
        ItemFrequencyRow row = null;
        for(VariableName v : table.keySet()){
            row = table.get(v);
            if(row.groupMember(groupId)){
                sum += row.Sij(value);
            }
        }
        return sum;
    }

    /**
     * Computes the total sample size (i.e. total number of examinees).
     * Sum of Tij over all i and j
     *
     * @return
     */
    public double grandTotal(){
        double n = 0.0;
        for(VariableName v : table.keySet()){
            n += table.get(v).Tip();
        }
        return n;
    }

    /**
     * Returns the total number of observations for group identified by groupId.
     *
     * @param groupId
     * @return
     */
    public double groupTotal(String groupId){
        double sum = 0.0;
        ItemFrequencyRow row = null;
        for(VariableName v : table.keySet()){
            row = table.get(v);
            if(row.groupMember(groupId)){
                sum += row.Tip();
            }
        }
        return sum;
    }

    /**
     * This method checks to see if all observations are in a single category.
     * If the number of observations in group identified by groupId are all in
     * one category, return true. Otherwise return false. It checks to see if
     * the marginal sum, Ti+, is equal to any of the category sums, T+j.
     *
     * Thresholds will not be estimated if this method is true.
     *
     * @param groupId
     * @return
     */
    public boolean flagAllInOneCateogry(String groupId){
        double tpj = 0.0;
        double tipSum = groupTotal(groupId);
        Iterator<Byte> iter = null;
        for(VariableName v : table.keySet()){
            if(groupIds.get(v).equals(groupId)){
                iter = table.get(v).iterator();
                while(iter.hasNext()){
                    tpj = Tpj(iter.next(), groupId);
                    if(tpj==tipSum) return true;
                }
            }
        }
        return false;
    }

    /**
     * Sum of Tpj
     *
     * @param groupId
     * @return
     */
//    public double groupCategoryTotal(String groupId){
//        byte k = groupNumberOfCategories.get(groupId).byteValue();
//        double sum = 0.0;
//        for(byte i=0;i<k;i++){
//            sum += Tpj(i, groupId);
//        }
//        return sum;
//    }

    /**
     * Resets all frequencies to zero.
     */
    public void clearAllFrequencies(){
        for(VariableName v : table.keySet()){
            table.get(v).clearAllFrequencies();
        }
    }

    public String printTable(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        for(VariableName v : table.keySet()){
            sb.append("Item: "); sb.append(v.toString()); sb.append("\n");
            Iterator<Byte> iter = table.get(v).iterator();
            iter = table.get(v).iterator();
            f.format("%10s", "==========");
            while(iter.hasNext()){
                f.format("%13s", "=============");
                iter.next();
            }
            f.format("%8s", "========");
            f.format("%n");


            sb.append(table.get(v).printFrequencyTable());
            sb.append("\n");

            iter = table.get(v).iterator();
            iter = table.get(v).iterator();
            f.format("%10s", "==========");
            while(iter.hasNext()){
                f.format("%13s", "=============");
                iter.next();
            }
            f.format("%8s", "========");
            f.format("%n");
            f.format("%n");f.format("%n");


        }
        return sb.toString();
    }

}

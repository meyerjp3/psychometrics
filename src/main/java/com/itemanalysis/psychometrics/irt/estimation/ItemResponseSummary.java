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
package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;

import java.util.Arrays;
import java.util.Formatter;

public class ItemResponseSummary {

    private byte[] scoreCategories = null;
    private double[] Tij = null;//cell frequencies
//    private double[] Tpj = null;
    private double[] Sij = null;//number in score category or higher category
//    private double[] Spj = null;
    private double Sip = 0.0;//raw item score
    private double Tip = 0.0;
    public double adjustment = 0.3;
    private int nCat = 2;
    private int itemsInGroup = 1;
    private int positionInArray = 0;
    private VariableName variableName = null;
    private String groupId = "";

    public ItemResponseSummary(VariableName variableName, String groupId, double adjustment, byte[] scoreCategories){
        this.variableName = variableName;
        this.groupId = groupId;
        this.adjustment = Math.min(Math.max(0, adjustment), .5);//adjustment must be between 0 and 0.5
        this.scoreCategories = scoreCategories;
        Arrays.sort(this.scoreCategories);
        nCat = scoreCategories.length;
        Tij = new double[nCat];
//        Tpj = new double[nCat];
        Sij = new double[nCat];
//        Spj = new double[nCat];
    }

    public ItemResponseSummary(VariableName variableName, double adjustment, byte[] scoreCategories){
        this(variableName, variableName.toString(), adjustment, scoreCategories);
    }

    public ItemResponseSummary(VariableName variableName, byte[] scoreCategories){
        this(variableName, variableName.toString(), 0.3, scoreCategories);
    }

    /**
     * Count frequency of responses in each score category.
     *
     * @param itemResponse
     */
    public void increment(byte itemResponse){
        for(int i=0;i<nCat;i++){
            if(itemResponse==scoreCategories[i]){
                Tij[i]++;
                Tip++;
            }
            if(itemResponse>=scoreCategories[i]){
                Sij[i]++;
//                if(i>0) Sip++;
            }
        }
        Sip += Byte.valueOf(itemResponse).doubleValue();
    }

    /**
     * After frequencies have been counted for all items. Use this method to combine
     * frequencies for items in the same group.
     *
     * @param itemResponseSummary
     */
//    public void incrementGroupFrequencies(ItemResponseSummary itemResponseSummary){
//        if(sameItemGroup(itemResponseSummary)){
//            itemsInGroup++;
//            for(int i=0;i<nCat;i++){
//                Tpj[i] += itemResponseSummary.TijAt(i);
//                Spj[i] += itemResponseSummary.SijAt(i);
//            }
//        }
//    }

    /**
     * Returns frequency of the score category at this index.
     *
     * @param index
     * @return
     */
    public double TijAt(int index){
        return Tij[index];
    }

    public double Tip(){
        return Tip;
    }

//    /**
//     * This count is part of the threshold update in the partial credit
//     * and rating scale model.
//     *
//     * @param index
//     * @return
//     */
//    public double TpjAt(int index){
//        //TODO check that these extreme score adjustments are ok
//
//        if(Tpj[index]==itemsInGroup*Tip){
//            return Tpj[index]-adjustment;
//        }else if(Tpj[index]==0){
//            return Tpj[index]+adjustment;
//        }
//        return Tpj[index];
//    }

    /**
     * Returns the frequency of the score category at this index of higher.
     *
     * @param index
     * @return
     */
    public double SijAt(int index){
        return Sij[index];
    }

    /**
     * Returns the raw item score. This number is part of hte item difficulty update
     * in the Rasch, partial credit, and rating scale model. The adjustment is an
     * extreme score adjustment that prevents infinite estimates.
     *
     * @return
     */
    public double Sip(){
        if(Sip==maxSip()) return Sip-adjustment;
        if(Sip==minSip()) return Sip+adjustment;
        return Sip;
    }

    /**
     * Minimum possible raw item score
     *
     * @return
     */
    public double minSip(){
        return Tip*scoreCategories[0];
    }


    /**
     * Maximum possible raw item score
     *
     * @return
     */
    public double maxSip(){
        return Tip*scoreCategories[nCat-1];
    }

//    /**
//     * This number is part of the PROX calculation for obtaining start values.
//     *
//     * @param index
//     * @return
//     */
//    public double SpjAt(int index){
//        return Spj[index];
//    }

    public int getNumberOfCategories(){
        return nCat;
    }

    public String getGroupId(){
        return groupId;
    }

    public int getScoreCategoryAt(int index){
        return scoreCategories[index];
    }

    public int getPositionInArray(){
        return positionInArray;
    }

    public void setPositionInArray(int positionInArray){
        this.positionInArray = positionInArray;
    }

    public double getAdjustment(){
        return adjustment;
    }

    /**
     * Compares this item summary to another one to determine of they
     * belong to the same item group. This is needed to determine if
     * items belong to the same rating scale.
     *
     * @param itemResponseSummary
     * @return
     */
    public boolean sameItemGroup(ItemResponseSummary itemResponseSummary){
        if(!this.groupId.equals(itemResponseSummary.getGroupId())) return false;
        if(this.nCat!=itemResponseSummary.getNumberOfCategories()) return false;
        for(int i=0;i<nCat;i++){
            if(scoreCategories[i]!=itemResponseSummary.getScoreCategoryAt(i)) return false;
        }
        return true;
    }

    public boolean isExtremeMaximum(){
        if(Sip==minSip()) return true;
        return false;
    }

    public boolean isExtremeMinimum(){
        if(Sip==maxSip()) return true;
        return false;
    }

    public boolean isExtreme(){
        if(isExtremeMaximum() || isExtremeMinimum()) return true;
        return false;
    }

    public void clearCounts(){
        Sip = 0;
        Tip = 0;
        for(int i=0;i<nCat;i++){
            Tij[i] = 0;
            Sij[i] = 0;
//            Tpj[i] = 0;
//            Spj[i] = 0;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        int lineLength = nCat*10;
        String line = "";
        for(int i=0;i<lineLength;i++){
            line+="-";
        }

        f.format("%5s", "");
        f.format("%12s", variableName.toString() + "(" + groupId + ")");f.format("%n");

        f.format("%5s", "");
        for(int i=0;i<nCat;i++){
            f.format("%5d", (int)scoreCategories[i]);  f.format("%5s", "");
        }
        f.format("%n");

        f.format("%5s", ""); f.format("%-"+lineLength+"s", line);f.format("%n");


        f.format("%5s|", "Tij ");
        for(int i=0;i<nCat;i++){
            f.format("%5.2f", Tij[i]);  f.format("%5s", "");
        }
        if(isExtreme()) f.format("%7s", "EXTREME");
        f.format("%n");


        f.format("%5s|", "Sij ");
        for(int i=0;i<nCat;i++){
            f.format("%5.2f", Sij[i]);  f.format("%5s", "");
        }
        f.format("%5s", "Tip = "); f.format("%5.2f", Tip());
        f.format("%8s", "   Sip = "); f.format("%5.2f", Sip());
        f.format("%n");f.format("%n");

        return f.toString();

    }


}

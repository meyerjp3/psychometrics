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

/**
 * This class is used to count and store item response frequencies for {@link JointMaximumLikelihoodEstimation}.
 * It is not called directly. Rather, it is called via {@link JointMaximumLikelihoodEstimation#summarizeData(double)}.
 * 
 */
public class ItemResponseSummary {

    private byte[] scoreCategories = null;
    private double[] Tij = null;//cell frequencies
    private double[] Sij = null;//number in score category or higher category
    private double Sip = 0.0;//raw item score
    private double Tip = 0.0;
    public double adjustment = 0.3;
    private int nCat = 2;
    private int positionInArray = 0;
    private VariableName variableName = null;
    private String groupId = "";

    /**
     * Create an item response summary object with information about the variable. The groupId is used to determine
     * whether an item is combined with other items in the group. The group Id may be unique to the item or
     * shared for a set of items.
     *
     * @param variableName name of item.
     * @param groupId a groupId for the item.
     * @param adjustment extreme score adjustment.
     * @param scoreCategories category scoring.
     */
    public ItemResponseSummary(VariableName variableName, String groupId, double adjustment, byte[] scoreCategories){
        this.variableName = variableName;
        this.groupId = groupId;
        this.adjustment = Math.min(Math.max(0, adjustment), .5);//adjustment must be between 0 and 0.5
        this.scoreCategories = scoreCategories;
        Arrays.sort(this.scoreCategories);
        nCat = scoreCategories.length;
        Tij = new double[nCat];
        Sij = new double[nCat];
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
            }
        }
        Sip += Byte.valueOf(itemResponse).doubleValue();
    }

    /**
     * Frequencies for each category are stored in an array. This method gets the frequency of the score
     * category at this index.
     *
     * @param index position of score category.
     * @return frequency of examinees in a score category.
     */
    public double TijAt(int index){
        return Tij[index];
    }

    /**
     * Total number of exmainees responding to this item.
     **/
    public double Tip(){
        return Tip;
    }

    /**
     * Sij is the frequency of responses in this score category or a higher category.
     * This method returns the frequency of the score category at this index of higher.
     *
     * @param index position of response frequency.
     * @return number of examinees responding in this category or a higher category.
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

    /**
     * Gets the number of score categories for this item.
     *
     * @return number of score categories.
     */
    public int getNumberOfCategories(){
        return nCat;
    }

    /**
     * Gets the groupId for this item.
     *
     * @return groupID.
     */
    public String getGroupId(){
        return groupId;
    }

    /**
     * Gets the score point value for a particular category.
     *
     * @param index array position of the category score.
     * @return score value for the category.
     */
    public int getScoreCategoryAt(int index){
        return scoreCategories[index];
    }

    /**
     * Stores the items position in the array of item response models. See
     * {@link JointMaximumLikelihoodEstimation#updateDifficulty}
     *
     * @return the item summary's position in the array if items.
     */
    public int getPositionInArray(){
        return positionInArray;
    }

    /**
     * Stores the item's position in the array of item response models. It is set during the data summary steps. See
     * {@link JointMaximumLikelihoodEstimation#initializeCounts()}
     *
     * @param positionInArray
     */
    public void setPositionInArray(int positionInArray){
        this.positionInArray = positionInArray;
    }

    /**
     * Gets the extreme score adjustment factor.
     *
     * @return extreme score adjustment.
     */
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

    /**
     * An extreme maximum item score is one in which the raw item score equals the minimum possible raw item score.
     *
     * @return true if the item is an extreme maximum. Returns false otherwise.
     */
    public boolean isExtremeMaximum(){
        if(Sip==minSip()) return true;
        return false;
    }

    /**
     * An extreme minimum item score is one in which the raw item score equals the maximum possible raw item score.
     *
     * @return true if an extreme minimum. Return false otherwise.
     */
    public boolean isExtremeMinimum(){
        if(Sip==maxSip()) return true;
        return false;
    }

    /**
     * A boolean method that returns true if the item is an extreme minimum or and extreme maximum.
     *
     * @return true if an extreme item. REturn false otherwise.
     */
    public boolean isExtreme(){
        if(isExtremeMaximum() || isExtremeMinimum()) return true;
        return false;
    }

    /**
     * Resets the item counts to zero. This is needed during the recursive data summary step in
     * {@link JointMaximumLikelihoodEstimation#summarizeData(double)}.
     *
     */
    public void clearCounts(){
        Sip = 0;
        Tip = 0;
        for(int i=0;i<nCat;i++){
            Tij[i] = 0;
            Sij[i] = 0;
        }
    }

    /**
     * A string representation of the frequency counts. Used for displaying output.
     *
     * @return a string of frequency counts.
     */
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

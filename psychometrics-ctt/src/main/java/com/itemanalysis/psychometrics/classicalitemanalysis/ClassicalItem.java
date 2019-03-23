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
package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.data.ItemScoring;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.exceptions.ItemScoringException;
import com.itemanalysis.psychometrics.scaling.RawScore;

import java.util.Formatter;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * This is the main class for conducting a classical item analysis. It scores the item
 * and incrementally updates the item statistics. Output includes item difficulty
 * and discrimination.
 *
 * It computes statistic for the entire item ("overall") and optionally for each
 * possible response option.
 *
 * @author J. Patrick Meyer 
 */
@Deprecated
public class ClassicalItem {

    private ItemStats itemStats = null;

    private TreeMap<Object, ItemStats> categoryStats = null;

    /**
     * compute statistic for all response options.
     */
    boolean allCategories = true;

    /**
     * Information about teh variable
     */
    private VariableAttributes variableAttributes = null;

    /**
     * A continuous item (not binary and not polytomous)
     */
    private boolean continuousItem = false;

    /**
     * Item scoring tells the class how to score each possible response option.
     */
    private ItemScoring itemScoring = null;

    public ClassicalItem(VariableAttributes variableAttributes, boolean biasCorrection, boolean allCategories, boolean pearson, boolean computeDIndex){
        this.variableAttributes = variableAttributes;
        this.itemScoring = variableAttributes.getItemScoring();
        this.allCategories = allCategories;
        initializeCategories(biasCorrection, pearson, computeDIndex);
    }

    private void initializeCategories(boolean biasCorrection, boolean pearson, boolean computeDIndex){
        continuousItem = variableAttributes.getType().getItemType()== ItemType.CONTINUOUS_ITEM;

        itemStats = new ItemStats(variableAttributes.getName().toString(), biasCorrection, pearson, continuousItem, computeDIndex);
        categoryStats = new TreeMap<Object, ItemStats>();

        if(!continuousItem){
            Iterator<Object> iter = itemScoring.categoryIterator();
            ItemStats iStats = null;
            Object obj = null;
            while(iter.hasNext()){
                obj = iter.next();
                iStats = new ItemStats(obj, biasCorrection, pearson, continuousItem, computeDIndex);
                categoryStats.put(obj, iStats);
            }
        }
    }

    public boolean isContinuous(){
        return variableAttributes.getType().getItemType()==ItemType.CONTINUOUS_ITEM;
    }

    /**
     * Incrementally update item statistics
     *
     * @param rawScore sum score for the entire test
     * @param response observed item response (not the score value)
     */
    public void increment(RawScore rawScore, Object response)throws ItemScoringException{
        increment(rawScore.value(), response);
//        double testScore = rawScore.value();
//        double itemScore = itemScoring.computeItemScore(response);
//        double catScore = 0.0;
//
//        //increment overall item statistics
//        itemStats.increment(testScore, itemScore);
//
//        //increment category statistics for binary and polytomous items
//        if(allCategories && !continuousItem){
//            ItemStats catStats = null;
//            for(Object o : categoryStats.keySet()){
//                catStats = categoryStats.get(o);
//                catScore = itemScoring.computeCategoryScore(o, response);
//                catStats.increment(testScore, catScore);
//            }
//        }
        
    }

    public void increment(double sumScore, Object response)throws ItemScoringException{
        double itemScore = itemScoring.computeItemScore(response);
        double catScore = 0.0;

        //increment overall item statistics
        itemStats.increment(sumScore, itemScore);

        //increment category statistics for binary and polytomous items
        if(allCategories && !continuousItem){
            ItemStats catStats = null;
            for(Object o : categoryStats.keySet()){
                catStats = categoryStats.get(o);
                catScore = itemScoring.computeCategoryScore(o, response);
                catStats.increment(sumScore, catScore);
            }
        }
    }

    public void incrementDindex(Object response, double testScore, double lowerCut, double upperCut)throws ItemScoringException {
        double itemScore = itemScoring.computeItemScore(response);
        itemStats.incrementDindex(itemScore, testScore, lowerCut, upperCut);
        double catScore = 0.0;

        //increment category statistics for binary and polytomous items
        if(allCategories && !continuousItem){
            ItemStats catStats = null;
            for(Object o : categoryStats.keySet()){
                catStats = categoryStats.get(o);
                catScore = itemScoring.computeCategoryScore(o, response);
                catStats.incrementDindex(catScore, testScore, lowerCut, upperCut);
            }
        }
    }

    /**
     * Principal Factor Analysis Centroid method approximaiton.
     * This method provides starting values for confirmatory factor
     * analysis factor loadings.
     *
     * @return an approximation to teh factor loading
     */
    @Deprecated
    public double factorLoadingApproximation(){
        return Math.pow(itemStats.getStdDev(),2)*itemStats.getDiscrimination();
    }

    /**
     * Number of response categories
     * @return the number of response categories
     */
    public int numberOfCategories(){
        if(allCategories) return itemScoring.numberOfCategories();
        return 1;
    }

    /**
     * Name of item
     *
     * @return name of the item
     */
    public VariableName getName(){
        return variableAttributes.getName();
    }

    /**
     * Classical item difficulty estimate
     *
     * @return item difficulty
     */
    public double getDifficulty(){
        return itemStats.getDifficulty();
    }

    /**
     * Item standard deviation
     *
     * @return item standard deviation
     */
    public double getStdDev(){
        return itemStats.getStdDev();
    }

    /**
     * Classical item discrimination estimate (i.e. item-total correlation)
     *
     * @return item discrimination
     */
    public double getDiscrimination(){
        return itemStats.getDiscrimination();
    }

    public double getDindexLower(){
        return itemStats.getDindexLower();
    }

    public double getDindexUpper(){
        return itemStats.getDindexUpper();
    }

    public double getDindex(){
        return itemStats.getDIndex();
    }

    /**
     * Return the proportion selecting a particular response option.
     *
     * @param index value of teh response option
     * @return proportion endorsing the resposne option
     */
    public double getDifficultyAt(Object index){
        return categoryStats.get(index).getDifficulty();
    }

    /**
     * Standard deviation for the proportion of examinees endorsing teh response option.
     *
     * @param index value of teh response option
     * @return standard deviation for the proportion of examinees endorsing teh response option
     */
    public double getStdDevAt(Object index){
        return categoryStats.get(index).getStdDev();
    }

    /**
     * Option-total score correlation. For teh correct answer this value is the item discrimination.
     * For teh distractors it is the distractor total correlation.
     *
     * @param index value of teh response option
     * @return option-total correlation
     */
    public double getDiscriminationAt(Object index){
        return categoryStats.get(index).getDiscrimination();
    }

    /**
     * Iterator for the catgory objects
     * @return iterator for the categories
     */
    public Iterator<Object> categoryIterator(){
        return categoryStats.keySet().iterator();
    }

    /**
     * Header for the item analysis output. Usually only called for the first item.
     *
     * @return output header
     */
    public String printHeader(){
		StringBuilder buffer = new StringBuilder();
		Formatter f = new Formatter(buffer);
        f.format("%-10s", " Item");f.format("%2s", " ");
		f.format("%15s", "Option (Score)");f.format("%2s", " ");
		f.format("%10s", "Difficulty");f.format("%2s", " ");
        f.format("%10s", "Std. Dev.");f.format("%2s", " ");
		f.format("%10s", "Discrimin.");f.format("%2s", " ");
		f.format("%n");
        f.format("%-10s"," ---------");f.format("%2s", " ");
		f.format("%15s", "---------------");f.format("%2s", " ");
		f.format("%10s", "----------");f.format("%2s", " ");
		f.format("%10s", "----------");f.format("%2s", " ");
		f.format("%10s", "----------");f.format("%2s", " ");
		f.format("%n");
		return f.toString();
	}

    /**
     * Provide all requested statistics for the item. This output aligns with the printHeader() method.
     * @return string output
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-11s", " " + variableAttributes.getName());f.format("%1s", " ");

        //list overall item statistics
        f.format("%15s", "Overall"); f.format("%2s", " "); //name for overall statistics
        f.format("% 10.4f", itemStats.getDifficulty()); f.format("%2s", " "); //item difficulty
        f.format("% 10.4f", itemStats.getStdDev());  f.format("%2s", " "); //item standard deviation
        f.format("% 10.4f", itemStats.getDiscrimination());  f.format("%2s", " "); //item correlation
        f.format("%n");

        //list category statistics if requested
        if(allCategories && !continuousItem){
            Iterator<Object> iter = itemScoring.categoryIterator();
            Object obj = null;
            ItemStats catStats = null;
            String scoreString = "";
            while(iter.hasNext()){
                obj = iter.next();
                scoreString = itemScoring.getCategoryScoreString(obj);
                catStats = categoryStats.get(obj);
                f.format("%27s", scoreString);f.format("%2s", " ");  //list original values
                f.format("%36s", catStats.toString()); f.format("%n");
            }
        }
        return f.toString();
    }

}

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
package com.itemanalysis.psychometrics.measurement;

import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.scaling.RawScore;

import java.util.Formatter;
import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 * @author J. Patrick Meyer 
 */
public class ClassicalItem {

    private ItemStats itemStats = null;

    private TreeMap<Object, ItemStats> categoryStats = null;

    private boolean biasCorrection = true;

    boolean allCategories = true;

    private VariableInfo varInfo = null;

    private boolean continuousItem = false;

    private boolean pearson = true;

    private DefaultItemScoring itemScoring = null;

    public ClassicalItem(VariableInfo varInfo){
        this(varInfo, true, true, true);
    }

    public ClassicalItem(VariableInfo varInfo, boolean biasCorrection, boolean allCategories, boolean pearson){
        this.varInfo = varInfo;
        this.itemScoring = varInfo.getItemScoring();
        this.biasCorrection = biasCorrection;
        this.allCategories = allCategories;
        this.pearson = pearson;
        initializeCategories();
    }

    private void initializeCategories(){
        continuousItem = varInfo.getType().getItemType()==VariableType.CONTINUOUS_ITEM;

        itemStats = new ItemStats(varInfo.getName().toString(), biasCorrection, pearson, continuousItem);
        categoryStats = new TreeMap<Object, ItemStats>();

        if(!continuousItem){
            Iterator<Object> iter = itemScoring.categoryIterator();
            ItemStats iStats = null;
            Object obj = null;
            while(iter.hasNext()){
                obj = iter.next();
                iStats = new ItemStats(obj, biasCorrection, pearson, continuousItem);
                categoryStats.put(obj, iStats);
            }
        }
    }

    public boolean isContinuous(){
        return varInfo.getType().getItemType()==VariableType.CONTINUOUS_ITEM;
    }

    public void increment(RawScore rawScore, Object response){
        double testScore = rawScore.value();
        double itemScore = itemScoring.computeItemScore(response, varInfo.getType());
        double catScore = 0.0;

        //increment overall item statistics
        itemStats.increment(testScore, itemScore);

        //increment category statistics for binary and polytomous items
        if(allCategories && !continuousItem){
            ItemStats catStats = null;
            for(Object o : categoryStats.keySet()){
                catStats = categoryStats.get(o);
                catScore = itemScoring.computeCategoryScore(o, response);
                catStats.increment(testScore, catScore);
            }
        }
        
    }

    /**
     * Principal Factor Analysis Centroid method approximaiton.
     * This method provides starting values for confirmatory factor
     * analysis factor loadings.
     *
     * @return
     */
    public double factorLoadingApproximation(){
        return Math.pow(itemStats.getStdDev(),2)*itemStats.getDiscrimination();
    }

    public int numberOfCategories(){
        if(allCategories) return itemScoring.numberOfCategories();
        return 1;
    }

    public VariableName getName(){
        return varInfo.getName();
    }

    public double getDifficulty(){
        return itemStats.getDifficulty();
    }

    public double getStdDev(){
        return itemStats.getStdDev();
    }

    public double getDiscrimination(){
        return itemStats.getDiscrimination();
    }

    public double getDifficultyAt(Object index){
        return categoryStats.get(index).getDifficulty();
    }

    public double getStdDevAt(Object index){
        return categoryStats.get(index).getStdDev();
    }

    public double getDiscriminationAt(Object index){
        return categoryStats.get(index).getDiscrimination();
    }

    public Iterator<Object> categoryIterator(){
        return categoryStats.keySet().iterator();
    }

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

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        
//        ClassicalCategory cat = null;
        f.format("%-11s", " " + varInfo.getName());f.format("%1s", " ");

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

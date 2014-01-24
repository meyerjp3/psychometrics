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
import com.itemanalysis.psychometrics.scaling.DefaultLinearTransformation;
import org.apache.commons.math3.util.Precision;

import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class RatingScaleItem {

    private VariableName name = null;

    private String groupId = "";

    private int column = 0;

    private double difficulty = 0.0;

    private double difficultyStdError = 0.0;

    private double[] thresholds = null;

    private double[] thresholdsStdError = null;

    private double proposalDifficulty = 0.0;

    private FitStatistics fit = null;

    /**
     * Object for computing probabilities in the RatingScaleModel
     */
    private RatingScaleModel rsm = null;

    private boolean fixedParameter  = false;

    private boolean extremeItem = false;

    /**
     * Flag to indicate a dropped item. No estimation is done.
     * The main reason for a dropped item is that the item has
     * a problem with the category counts such as a
     * category with zero count or all responses in one category.
     */
    private boolean droppedItem = false;

    private int numberOfCategories = 2;

    private int maximumPossibleScore = 2;

    public RatingScaleItem(VariableName name, String groupId, int maximumPossibleScore, int column){
        this.name = name;
        this.groupId = groupId;
        this.numberOfCategories = maximumPossibleScore+1;
        this.maximumPossibleScore = maximumPossibleScore;
        this.column = column;
        rsm = new RatingScaleModel();
        fit = new FitStatistics();
        thresholds = new double[numberOfCategories];
        thresholdsStdError = new double[numberOfCategories];
        for(int i=0;i<numberOfCategories;i++){
            thresholds[i] = 0.0;
            thresholdsStdError[i] = 0.0;
        }
    }

    public void setFixedItemDifficulty(double difficulty){
        this.difficulty = difficulty;
        this.proposalDifficulty = difficulty;
        fixedParameter = true;
    }

    public void setProposalDifficulty(double proposalDifficulty){
        this.proposalDifficulty = proposalDifficulty;
    }

    public void setFixedThresholdAt(int index, double threshold){
        this.thresholds[index] = threshold;
        fixedParameter = true;
    }

    public VariableName getName(){
        return name;
    }

    public String getGroupId(){
        return groupId;
    }

    public int getNumberOfCategories(){
        return numberOfCategories;
    }

    public int getMaximumPossibleScore(){
        return maximumPossibleScore;
    }

    /**
     * Return column position in data set
     *
     * @return
     */
    public int getColumn(){
        return column;
    }

    public void setExtremeItem(boolean extremeItem){
        this.extremeItem = extremeItem;
    }

    public boolean extremeItem(){
        return extremeItem;
    }

    public void setDroppedItem(boolean droppedItem){
        this.droppedItem = droppedItem;
    }

    public boolean droppedItem(){
        return droppedItem;
    }

    /**
     * accept new difficulty rho and return change in rho
     *
     * @return
     */
    public double acceptProposalDifficulty(){
        if(fixedParameter) return 0.0;
        double delta = Math.abs(difficulty - proposalDifficulty);
        difficulty = proposalDifficulty;
        return delta;
    }

    /**
     * PROX estimate of item difficulty. This is called to produce a starting rho for the
     * JMLE routine.
     *
     * @param maxItemScore
     * @param adjustedItemScore
     */
    public void prox(double maxItemScore, double adjustedItemScore){
        if(!fixedParameter){
            double p = adjustedItemScore/maxItemScore;
            double q = 1.0-p;
            difficulty = Math.log(q/p);
            proposalDifficulty = difficulty;
        }
        
    }

    /**
     * Adjusts the item difficulty estimate for UCON bias. This method should be called only once. It is optional.
     *
     * @param numberOfItems
     */
    public void biasAdjustment(int numberOfItems){
        if(!fixedParameter){
            double v = (((double)numberOfItems-1.0)/(double)numberOfItems);
            difficulty*=v;
        }
    }

    /**
     * Adjusts item difficulty according to the overall item mean. This method is needed for model identification.
     *
     * @param mean
     */
    public void recenter(double mean){
        if(!fixedParameter){
            difficulty-=mean;
        }
    }

    /**
     * Adjusts item difficulty according to the overall item mean. This method is needed for model identification.
     *
     * @param mean
     */
    public void recenterProposalDifficulty(double mean){
        if(!fixedParameter){
            proposalDifficulty-=mean;
        }
    }

    public boolean fixedParameter(){
        return fixedParameter;
    }

    public double getDifficulty(){
        return difficulty;
    }

    public double[] getThresholds(){
        return thresholds;
    }

    public void setThresholds(double[] thresholds){
        this.thresholds = thresholds;
    }

    /**
     * Linear transformation of difficulty. this method is used for scaling and equating
     *
     */
    public void linearTransformation(DefaultLinearTransformation lt, int precision){
        difficulty = lt.transform(difficulty);
        difficultyStdError *= lt.getScale();
        difficulty = Precision.round(difficulty, precision);
    }

    public void linearTransformation(DefaultLinearTransformation lt, double scale){
        linearTransformation(lt, 4);
    }

    /**
     * Computes the standard error of the difficulty estimate. This method should be called once the
     * final difficulty estimate has been obtained. It must be called before a call to getStandardError().
     *
     * @param theta
     */
    public void computeStandardError(double[] theta, boolean[] extremeTheta, byte[][] data){
        double sum = 0.0;

        for(int i=0;i<theta.length;i++){
            if(data[i][column]>-1 && !extremeTheta[i]){
                sum+=rsm.denomInf(theta[i], difficulty, thresholds);
            }
        }
        if(sum==0.0) difficultyStdError = Double.NaN;
        difficultyStdError = 1.0/Math.sqrt(sum);
    }

    public double getDifficultyStandardError(){
        return difficultyStdError;
    }

    public FitStatistics getFitStatistics(){
        return fit;
    }

    public void incrementFitStatistics(double theta, byte Xni){
        if(Xni>-1){
            fit.increment(
                    Xni,
                    rsm.expectedValue(theta, difficulty, thresholds),
                    rsm.varianceOfResponse(theta, difficulty, thresholds),
                    rsm.kurtosisOfResponse(theta, difficulty, thresholds)
                    );
        }
        
    }

    public String printItem(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%10s", this.getName().toString()); f.format("%5s", "");
        f.format("%8.2f", getDifficulty()); f.format("%5s", "");


        for(int i=0;i<thresholds.length;i++){
            f.format("%8.2f", thresholds[i]); f.format("%5s", "");
        }

        return f.toString();

    }
    

}

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

package com.itemanalysis.psychometrics.irt.model;

import com.itemanalysis.psychometrics.irt.estimation.ItemResponseSummary;
import com.itemanalysis.psychometrics.irt.estimation.RaschCategoryFitStatistic;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;

/**
 * A rating scale group is a set of test items that share the same rating scale structure. Category
 * frequencies for items in a rating scale group are combined for estimation of threshold parameters.
 * This class is used for combining the frequencies and holding estimates of the thresholds and
 * also their fit statistics.
 *
 * @author J. Patrick Meyer
 *
 */
public class RaschRatingScaleGroup {

    private String groupId = "";
    private int nCat = 2;
    private double totalCount = 0;
    private ArrayList<Integer> columnPosition = null;
    private ArrayList<ItemResponseModel> irm = null;
    private double[] Tpj = null;
    private double[] Spj = null;
    private byte[] scoreWeight;
    private double[] thresholds = null;
    private double[] proposalThresholds = null;
    private double[] thresholdStandardError = null;
    private boolean isFixed = false;
    private RaschCategoryFitStatistic[] fitStatistics = null;
    private int extremeCategory = 0;
    private boolean poissonCountsModel = false;

    /**
     * Creates a rating scael group using an ID and set number of categories. Any item with the same group ID will
     * be placed into this group.
     *
     * @param groupId A unique identifier for the group.
     * @param nCat number of score categories for this group.
     */
    public RaschRatingScaleGroup(String groupId, int nCat){
        this.groupId = groupId;
        this.nCat = nCat;
        Tpj = new double[nCat];
        Spj = new double[nCat];
        columnPosition = new ArrayList<Integer>();
        irm = new ArrayList<ItemResponseModel>();
        thresholdStandardError = new double[nCat-1];
    }

    private void initializezFit(){
        fitStatistics = new RaschCategoryFitStatistic[nCat-1];
        for(int m=0;m<nCat-1;m++){
            fitStatistics[m] = new RaschCategoryFitStatistic((int)scoreWeight[m+1], scoreWeight);
        }
    }

    /**
     * Called for every item on the test, this method will add any item that has the same groupId and number
     * of score categories.
     *
     * @param irm an item response model.
     * @param isum an item summary object.
     * @param position column position of the item in the data array.
     */
    public void addItem(ItemResponseModel irm, ItemResponseSummary isum, int position){
        if(this.groupId.equals(irm.getGroupId()) && this.nCat==irm.getNcat()){
            if(irm instanceof IrmPoissonCounts) poissonCountsModel = true;
            this.irm.add(irm);
            this.columnPosition.add(position);
            if(thresholds==null){
                this.thresholds = irm.getThresholdParameters();
                this.proposalThresholds = irm.getThresholdParameters();
                this.scoreWeight = irm.getScoreWeights();
            }
            for(int i=0;i<nCat;i++){
                Tpj[i] += isum.TijAt(i);
                Spj[i] += isum.SijAt(i);
            }
            totalCount += isum.Tip();
            if(irm.isFixed()) isFixed = true;//if any item in the group is fixed, then all of them are
        }
    }

    /**
     * The columns of data for each item in the group is recorded and stored in an array. Gets the array
     * of column positions.
     *
     * @return column positions for items in this group.
     */
    public int[] getPositions(){
        int n = columnPosition.size();
        int[] pos = new int[n];
        int index = 0;
        for(Integer i : this.columnPosition){
            pos[index] = i;
        }
        return pos;
    }

    /**
     * An iterator for item response models for this group.
     *
     * @return an iterator.
     */
    public Iterator<ItemResponseModel> getIterator(){
        return irm.iterator();
    }

    /**
     * A groupId uniquely identifies each item in the rating scale group. Gets the groupId value.
     *
     * @return groupId
     */
    public String getGroupId(){
        return groupId;
    }

    /**
     * The number of score categories for this group is fixed. Gets the number of score categories.
     *
     * @return number of score categories.
     */
    public int getNumberOfCategories(){
        return nCat;
    }

    /**
     * This count is part of the threshold update in the partial credit
     * and rating scale model.
     *
     * @param index
     * @return
     */
    public double TpjAt(int index){
        return Tpj[index];
    }

    /**
     * Spj is the sum of Sij over all categories.
     *
     * @param index an index of the category for which the statistic is sought.
     * @return Spj for a category.
     */
    public double SpjAt(int index){
        return Spj[index];
    }

    /**
     * Threshold parameters cannot be estimated if one or more categories do not have any observations.
     * This method checks for empty categories and sets the flag for the item. A code of 0 indicates
     * that all categories have at least one observation, a code of -1 indicates a category with no
     * observations, and a code of +1 indicates a category that contains all of the responses.
     *
     * Do not drop an item if it has fixed parameters.
     */
    public void checkForDroppping(){
        if(isFixed) return;

        if(poissonCountsModel) return;//Poisson counts is allowed to have zero counts in categories.

        for(int m=0;m<nCat;m++){
            if(Tpj[m]==0){
                extremeCategory = -1;
            }else if(Tpj[m]==totalCount){
                extremeCategory = 1;
            }
        }
    }

    /**
     * Status of the item with response to dropping. See {@link #checkForDroppping()}.
     *
     * @return
     */
    public int dropStatus(){
        return extremeCategory;
    }

    /**
     * Resets the category counts. This method is used during the data summary in
     * {@link com.itemanalysis.psychometrics.irt.estimation.JointMaximumLikelihoodEstimation#summarizeData(double)}.
     */
    public void clearCounts(){
        for(int i=0;i<nCat;i++){
            Tpj[i] = 0;
            Spj[i] = 0;
        }
    }

    /**
     * Threshold parameters represent the point where the probability of scoring in two adjacent categories
     * is equal. They are estimated in {@link com.itemanalysis.psychometrics.irt.estimation.JointMaximumLikelihoodEstimation}.
     * Threshold estimates are stored in an array. This method gets the stored estimates.
     *
     * @return threshold estimates.
     */
    public double[] getThresholds(){
        return thresholds;
    }

    /**
     * Gets threshold estimates for a particular category.
     *
     * @param index position of category in threshold array.
     * @return estimate for the threshold at the position given by index.
     */
    public double getThresholdAt(int index){
        return thresholds[index];
    }

    /**
     * Sets the array of threshold estimates. This should only be used for setting initial values.
     *
     * @param thresholds values of thresholds.
     */
    public void setThresholds(double[] thresholds){
        this.thresholds = thresholds;
    }

    /**
     * During each iteration of the joint maximum likelihood routine, preliminary threshold estimates are obtained.
     * These proposal values are stored in a separate array. After one complete iteration is complete, the threshold
     * values are set to the proposal values. See
     * {@link com.itemanalysis.psychometrics.irt.estimation.JointMaximumLikelihoodEstimation#updateThresholds(RaschRatingScaleGroup)}.
     *
     * @return
     */
    public double[] getProposalThresholds(){
        return proposalThresholds;
    }

    /**
     * Sets the array of proposal values.
     *
     * @param proposalThresholds proposed threshold values.
     */
    public void setProposalThresholds(double[] proposalThresholds){
        this.proposalThresholds = proposalThresholds;
        for(ItemResponseModel i : irm){
            i.setProposalThresholds(proposalThresholds);
        }
    }

    /**
     * After a complete iteration, the threshold values are replaced by the prposal values.
     */
    public void acceptAllProposalValues(){
        if(!isFixed){
            this.thresholds = this.proposalThresholds;
        }

    }

    public boolean isFixed(){
        return isFixed;
    }

    public boolean isPoissoncounts(){
        return poissonCountsModel;
    }

    /**
     * Each category has an associated score weight. They are stored in an array. Gets the score weights.
     *
     * @return score weights.
     */
    public byte[] getScoreWeights(){
        return scoreWeight;
    }

    /**
     * Sums the probability of responding in category at position k over all items in this rating scale group.
     *
     * @param theta person ability estimate.
     * @param k score category for which the probabilities are summed.
     * @return
     */
    public double probabilitySumAt(double theta, int k){
        double sum = 0;
         for(ItemResponseModel i : irm){
             sum += i.probability(theta, scoreWeight[k]);
         }
        return sum;
    }

    /**
     * Sums the probability of responding in category "category" or higher for all items in this group.
     *
     * @param m an item response model.
     * @param theta person ability estimate.
     * @param category category for which the sum is sought.
     * @return
     */
    private double probabilityOfCategoryOrHigher(ItemResponseModel m, double theta, int category){
        double sum = 0;
        for(int k=category;k<nCat;k++){
            sum += m.probability(theta, scoreWeight[k]);
        }
        return sum;
    }

    /**
     * Computes the standard error of the threshold parameters.
     *
     * @param theta array of person ability values
     * @param extremePersons array of extreme person flags
     */
    public void computeCategoryStandardError(double[] theta, int[] extremePersons){
        double[] difSum = new double[nCat-1];
        double p = 0;
        double q = 0;

        for(int i=0;i<theta.length;i++){
            if(extremePersons[i]==0){
                for(ItemResponseModel m : irm){
                    for(int k=1;k<nCat;k++){
                        p = probabilityOfCategoryOrHigher(m, theta[i], k);
                        q = 1-p;
                        difSum[k-1] += p*q;
                    }
                }
            }
        }

        for(int k=0;k<nCat-1;k++){
            thresholdStandardError[k] = 1.0/Math.sqrt(difSum[k]);
        }

        for(ItemResponseModel m : irm){
            m.setThresholdStdError(thresholdStandardError);
        }
    }

    /**
     * Each threshold parameter has an associated standard error. The standard errors are not computed during
     * estimation. You must call {@link #computeCategoryStandardError(double[], int[])} before calling this
     * method. Otherwise, the returned standard errors will be zero (and will be incorrect).
     *
     * @return array of threshold standard errors.
     */
    public double[] getThresholdStandardError(){
        return thresholdStandardError;
    }

    /**
     * Gets a specific threshold standard error.
     *
     * @param index array position of the particular threshold standard error.
     * @return a threshold standard error.
     */
    public double getThresholdStdErrorAt(int index){
        return thresholdStandardError[index];
    }

    /**
     * Category fit statistics are incrementally updated with repeated calls to this method.
     *
     * @param model and item response model.
     * @param theta person ability estimate.
     * @param Xni an observed item response.
     */
    public void incrementFitStatistics(ItemResponseModel model, double theta, byte Xni){
        if(fitStatistics==null) initializezFit();

        for(int m=0;m<nCat-1;m++){
            fitStatistics[m].increment(Xni, theta, model);
        }

    }

    /**
     * Gets category fit statistics for a particular category.
     *
     * @param index array index of category fit statistics.
     * @return category fit statistics.
     */
    public RaschCategoryFitStatistic getCategoryFitAt(int index){
        if(null!=fitStatistics[index]) return fitStatistics[index];
        return null;
    }

    /**
     * Variance of response Xni. This method is needed for computation of fit statistics.
     * @param theta examinee ability
     * @return
     */
    private double varianceOfResponse(ItemResponseModel irm, double theta){
        double Wni = 0.0;
        double Eni = irm.expectedValue(theta);
        for(int m=0;m<irm.getNcat();m++){
            Wni += Math.pow(m-Eni, 2)*irm.probability(theta, m);
        }
        return Wni;
    }

    /**
     * Category frequency summaries are stored in this object. This method provides a listing of the frequencies.
     *
     * @return output of response frequencies.
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        int lineLength = 8;
        String line = "-----------------------";
        for(int i=0;i<nCat;i++){
            line+="------------";
            lineLength+=12;
        }

        f.format("%-15s", "Group: " + groupId);
        f.format("%8s", "Type");
        for(int i=0;i<nCat;i++){
            f.format("%12s", "Cat"+(i+1));
        }
        f.format("%n");
        f.format("%"+lineLength+"s", line);
        f.format("%n");

        f.format("%15s", "");
        f.format("%8s", "Tpj:");
        for(int i=0;i<nCat;i++){
            f.format("%12.0f", TpjAt(i));
        }
        f.format("%n");
        f.format("%15s", "");
        f.format("%8s", "Spj:");
        for(int i=0;i<nCat;i++){
            f.format("%12.0f", SpjAt(i));
        }
        f.format("%n");f.format("%n");
        return f.toString();
    }

}

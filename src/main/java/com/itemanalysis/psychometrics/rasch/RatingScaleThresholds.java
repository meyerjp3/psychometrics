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
import com.itemanalysis.psychometrics.scaling.DefaultLinearTransformation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class RatingScaleThresholds {

    private String groupId = "";

    private DefaultItemScoring scoring = null;

    private double[] thresholds = null;

    private double[] proposalThresholds = null;

    private double[] standardErrors = null;

    private LinkedHashMap<Integer, CategoryFitStatistics> fit = null;

    private ArrayList<RatingScaleItem> items = null;

    /**
     * Object for computing probabilities in the RatingScaleModel
     */
    private RatingScaleModel rsm = null;

    private boolean fixedParameter  = false;

    private int numberOfCategories = 2;

    /**
     * Currently, all categories are flagged for an extreme category.
     * Will need to change this to allow certain cases like estimation
     * of categories with zero responses.
     *
     * Items contributing to this thresholds are dropped and no item
     * or threshold estimation is conducted.
     *
     * Currently, and item is flagged as extreme if a category has zero count
     * or if all responses are in one category. Also, if all items belonging to
     * this RatingScaleThreshold are extreme, it will be flagged as extreme.
     *
     */
    private boolean extremeThreshold = false;

    /**
     * Largest change in the amount of change in item difficulty from previous iteration.
     *
     */
//    private double DELTA = 1;

    private Mean mean = null;

    private StandardDeviation sd = null;

    /**
     * The number of categories is maxCategory+1
     *
     * @param groupId identifier for this rating scale category group
     * @param maxCategory largest category rho
     */
    public RatingScaleThresholds(String groupId, int maxCategory){
        this.groupId = groupId;
        numberOfCategories = maxCategory;
        thresholds = new double[numberOfCategories];
        proposalThresholds = new double[numberOfCategories];
        standardErrors = new double[numberOfCategories];
        mean = new Mean();
        sd = new StandardDeviation();
        items = new ArrayList<RatingScaleItem>();
        rsm = new RatingScaleModel();
        fit = new LinkedHashMap<Integer, CategoryFitStatistics>();
        initializeAllFit();
    }

    private void initializeAllFit(){
        for(int i=0;i<numberOfCategories;i++){
            CategoryFitStatistics cFit = new CategoryFitStatistics(i);
            fit.put(i, cFit);
        }
    }

    public void addItemToGroup(RatingScaleItem item){
        items.add(item);
    }

    public ArrayList<RatingScaleItem> getItemsInGroup(){
        return items;
    }

    public Iterator<RatingScaleItem> getItemIterator(){
        return items.iterator();
    }

    public int getNumberOfItemsInGroup(){
        return items.size();
    }

    /**
     * Sets this RatingScaleThresholds to extreme. All items
     * pertaining to this RatingScaleThresholds are dropped.
     * No estimation is done if extremeThreshold==true.
     *
     * @param extremeThreshold
     */
    public void setExtreme(boolean extremeThreshold){
        this.extremeThreshold = extremeThreshold;
        for(RatingScaleItem rsi : items){
            rsi.setDroppedItem(true);
        }
    }

    public boolean extremeThreshold(){
        return extremeThreshold;
    }

    /**
     * Inestimable thresholds are those in which the number of extreme items in the group
     * equals the number of items in the group. No estimates can be produced in this case,
     * because all responses are in a single category.
     *
     * @return
     */
    public boolean checkInestimableThresholds(){
        int n = 0;
        for(RatingScaleItem rsi : items){
            if(rsi.extremeItem()) n++;
        }
        return n==items.size();
    }

    public int getNumberOfNonextremeItems(){
        int n = 0;
        for(RatingScaleItem rsi : items){
            if(!rsi.extremeItem()) n++;
        }
        return n;
    }

    public int[] getItemColumns(){
        int n = items.size();
        int[] c = new int[n];
        for(int i=0;i<n;i++){
            c[i] = items.get(i).getColumn();
        }
        return c;
    }

    public int[] getValidItemColumns(){
        int ni = 0;
        for(RatingScaleItem i : items){
            if(!i.extremeItem()) ni++;
        }
        int[] c = new int[ni];
        int j = 0;
        for(RatingScaleItem i : items){
            if(!i.extremeItem()) c[j] = i.getColumn();
            j++;
        }
        return c;
    }

    /**
     * returns number of non extreme items in group.
     *
     * @return
     */
    public double[] getGroupItemDifficulties(){
        double[] difficulties = new double[getNumberOfNonextremeItems()];
        int i=0;
        for(RatingScaleItem rsi : items){
            if(!rsi.extremeItem()) difficulties[i] = rsi.getDifficulty();
            i++;
        }
        return difficulties;
    }

    public void setProposalThresholds(double[] proposalThresholds){
        this.proposalThresholds = proposalThresholds;
    }

    public void setProposalThresholdAt(int index, double threshold){
        proposalThresholds[index] = threshold;
    }

    public double getThresholdAt(int index){
        return thresholds[index];
    }

    public double getProposalThresholdAt(int index){
        return proposalThresholds[index];
    }

    public double[] getThresholds(){
        return thresholds;
    }

    public boolean fixedParameter(){
        return fixedParameter;
    }

    public void setFixedThresholdAt(int index, double threshold){
        thresholds[index] = threshold;
        fixedParameter = true;
    }

    /**
     * Computes step difficulty estimates via PROX for all steps in this group.
     * These computations are the start values.
     *
     */
    public void categoryProx(double[] Spj){
        if(fixedParameter) return;
        double previous=0.0;
        double current=0.0;
        for(int i=0;i<numberOfCategories;i++){
            current = Spj[i];
            if(i>0){
                thresholds[i] = Math.log(previous/current);
                proposalThresholds[i] = thresholds[i];
            }else{
                thresholds[i] = 0.0;
                proposalThresholds[i] = 0.0;
            }
            previous=current;
        }
    }

    public void recenterProposalThresholds(){
        double m = mean.evaluate(proposalThresholds, 1, numberOfCategories-1);
        for(int i=1;i<numberOfCategories;i++){
            proposalThresholds[i] = proposalThresholds[i] - m;
        }
    }

    public void recenterThresholds(){
        double m = mean.evaluate(thresholds, 1, numberOfCategories-1);
        for(int i=1;i<numberOfCategories;i++){
            thresholds[i] = thresholds[i] - m;
        }
    }

    public double acceptProposalThresholds(){
        if(fixedParameter) return 0.0;
        double delta = 0.0;
        for(int i=0;i<numberOfCategories;i++){
            delta = Math.max(Math.abs(thresholds[i]-proposalThresholds[i]), delta);
            thresholds[i] = proposalThresholds[i];
        }
        return delta;
    }

    public int getNumberOfCategories(){
        return numberOfCategories;
    }

    public String getGroupId(){
        return groupId;
    }

    public void setProposalThreshold(double[] proposalThresholds){
        this.proposalThresholds = proposalThresholds;
    }

    /**
     * Linear transformation of difficulty. this method is used for scaling and equating
     *
     */
    public void linearTransformation(DefaultLinearTransformation lt, int precision){
        for(int i=0;i<numberOfCategories;i++){
            thresholds[i] = lt.transform(thresholds[i]);
            standardErrors[i] *= lt.getScale();
            thresholds[i] = Precision.round(thresholds[i], precision);
        }

    }

    public void linearTransformation(DefaultLinearTransformation lt){
         linearTransformation(lt, 4);
    }

    /**
     * Adjusts the step difficulty estimate for UCON bias. This method should be called only once. It is optional.
     *
     * @param numberOfItems
     */
    public void biasAdjustment(int numberOfItems){
        if(!fixedParameter){
            for(int i=0;i<numberOfCategories;i++){
                double v = (((double)numberOfItems-1.0)/numberOfItems);
                thresholds[i]*=v;
            }
        }
    }

    /**
     * Computes the standard error of the step difficulty estimate. This method should be called once the
     * final difficulty estimate has been obtained. It must be called before a call to getStandardError().
     *
     * @param theta
     */
    public void computeStandardErrors(double[] theta, boolean[] extremePersons, byte[][] data){
        double[] sum = new double[numberOfCategories];
        for(int i=0;i<numberOfCategories;i++) sum[i] = 0.0;
        double[] itemDifficulties = getGroupItemDifficulties();
        standardErrors[0] = 0.0;
        for(int i=0;i<theta.length;i++){
            if(hasGroupResponse(i, data) && !extremePersons[i]){
                for(int j=1;j<numberOfCategories;j++){
                    sum[j]+=rsm.totalDenomDifferenceForCategoryJmle(theta[i], itemDifficulties, thresholds, j);
                }
            }
        }
        
        for(int j=1;j<numberOfCategories;j++){
            standardErrors[j] = 1.0/Math.sqrt(sum[j]);
        }

    }

    public double getStandardErrorAt(int index){
        return standardErrors[index];
    }

    public double[] getStandardErrors(){
        return standardErrors;
    }

    public CategoryFitStatistics getCategoryFitAt(int index){
        return fit.get(index);
    }

    public void incrementFit(double Xni, double theta, double difficulty){
        double Pnik = 0.0;
        double Eni = rsm.expectedValue(theta, difficulty, thresholds);
        double Wni = rsm.varianceOfResponse(theta, difficulty, thresholds);
        for(int j=0;j<numberOfCategories;j++){
            Pnik = rsm.value(theta, difficulty, thresholds, j);
            fit.get(j).increment(Xni, Eni, Wni, Pnik);
        }
    }

    /**
     * Checks to see if a person responded to any item in the group.
     * Returns true if at least one item in the group has a response from this person.
     * Returns false otherwise. The person is indicated by their row in the data array.
     *
     * @return
     */
    public boolean hasGroupResponse(int row, byte[][] data){
        int col = 0;
        int count = 0;
        for(RatingScaleItem i : items){
            col = i.getColumn();
            if(data[row][col]>-1) count++;
        }
        return count>0;
    }

    public double getThresholdMean(){
        double m = mean.evaluate(thresholds, 1, numberOfCategories-1);
        return m;
    }

    public double getThresholdStandardDeviation(){
        double s = sd.evaluate(thresholds, 1, numberOfCategories-1);
        return s;
    }

}

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

    public void addItem(ItemResponseModel irm, ItemResponseSummary isum, int position){
        if(this.groupId.equals(irm.getGroupId()) && this.nCat==irm.getNcat()){
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

    public int[] getPositions(){
        int n = columnPosition.size();
        int[] pos = new int[n];
        int index = 0;
        for(Integer i : this.columnPosition){
            pos[index] = i;
        }
        return pos;
    }

    public Iterator<ItemResponseModel> getIterator(){
        return irm.iterator();
    }

    public String getGroupId(){
        return groupId;
    }

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

    public double SpjAt(int index){
        return Spj[index];
    }

    public void checkForDroppping(){
        for(int m=0;m<nCat;m++){
            if(Tpj[m]==0){
                extremeCategory = -1;
            }else if(Tpj[m]==totalCount){
                extremeCategory = 1;
            }
        }
    }

    public int dropStatus(){
        return extremeCategory;
    }

    public void clearCounts(){
        for(int i=0;i<nCat;i++){
            Tpj[i] = 0;
            Spj[i] = 0;
        }
    }

    public double[] getThresholds(){
        return thresholds;
    }

    public double getThresholdAt(int index){
        return thresholds[index];
    }

    public void setThresholds(double[] thresholds){
        this.thresholds = thresholds;
    }

    public double[] getProposalThresholds(){
        return proposalThresholds;
    }

    public void setProposalThresholds(double[] proposalThresholds){
        this.proposalThresholds = proposalThresholds;
        for(ItemResponseModel i : irm){
            i.setProposalThresholds(proposalThresholds);
        }
    }

    public void acceptAllProposalValues(){
        if(!isFixed){
            this.thresholds = this.proposalThresholds;
        }

    }

    public byte[] getScoreWeights(){
        return scoreWeight;
    }

    public double probabilitySumAt(double theta, int k){
        double sum = 0;
         for(ItemResponseModel i : irm){
             sum += i.probability(theta, scoreWeight[k]);
         }
        return sum;
    }

    private double probabilityOfCategoryOrHigher(ItemResponseModel m, double theta, int category){
        double sum = 0;
        for(int k=category;k<nCat;k++){
            sum += m.probability(theta, scoreWeight[k]);
        }
        return sum;
    }

    /**
     * Computes teh standard error of the threshold parameters.
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

    public double[] getThresholdStandardError(){
        return thresholdStandardError;
    }

    public double getThresholdStdErrorAt(int index){
        return thresholdStandardError[index];
    }

    public void incrementFitStatistics(ItemResponseModel model, double theta, byte Xni){
        if(fitStatistics==null) initializezFit();

        for(int m=0;m<nCat-1;m++){
            fitStatistics[m].increment(Xni, theta, model);
        }

    }

    public RaschCategoryFitStatistic getCategoryFitAt(int index){
        return fitStatistics[index];
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
//        f.format("%n");
//        f.format("%"+lineLength+"s", line);
        f.format("%n");f.format("%n");
        return f.toString();
    }

}

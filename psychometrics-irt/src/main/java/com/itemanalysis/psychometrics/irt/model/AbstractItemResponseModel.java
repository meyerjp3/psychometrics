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

import com.itemanalysis.psychometrics.data.ItemScoring;
import com.itemanalysis.psychometrics.data.VariableLabel;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.estimation.ItemFitStatistic;
import org.apache.commons.math3.exception.DimensionMismatchException;

public abstract class AbstractItemResponseModel implements ItemResponseModel{

    protected String groupId = "";
    protected VariableName name = new VariableName("");
    protected VariableLabel label = new VariableLabel("");
    protected double[] scoreWeight;
    protected int ncat = 0;
    protected int ncatM1 = 0;
    protected int minCategory = 0;
    protected int maxCategory = 1;
    protected boolean isFixed= false;//parameters are fixed or can be updated
    protected double minWeight = Double.MAX_VALUE;//not sure why need these. They were in IrmPCM. Will minCategory work?
    protected double maxWeight = Double.MIN_VALUE;//not sure why need these. They were in IrmPCM. Will maxCategory work?
    private ItemScoring itemScoring = null;
    private ItemFitStatistic itemFitStatistic = null;

    public void setName(VariableName name){
        this.name = name;
        if("".equals(groupId)) groupId = name.toString();
    }

    public VariableName getName(){
        return name;
    }

    public void setLabel(VariableLabel label){
        this.label = label;
    }

    public VariableLabel getLabel() {
        return label;
    }

    public void setGroupId(String groupId){
        this.groupId = groupId;
    }

    public String getGroupId(){
        if("".equals(groupId)) return name.toString();
        return groupId;
    }

    public int getNcat(){
        return ncat;
    }

    public void setItemFitStatistic(ItemFitStatistic itemFitStatistic){
        this.itemFitStatistic = itemFitStatistic;
    }

    public ItemFitStatistic getItemFitStatistic(){
        return itemFitStatistic;
    }

    public boolean isFixed(){
        return isFixed;
    }

    /**
     * If parameters can be estimated, the isFixed should be false. Setting
     * isFixed to true prevents the item paramters from being estimated.
     * @param isFixed
     */
    public void setFixed(boolean isFixed){
        this.isFixed = isFixed;
    }

    protected void defaultScoreWeights(){
        scoreWeight = new double[ncat];
        for(int i=0;i<ncat;i++){
            scoreWeight[i] = Integer.valueOf(i).doubleValue();
            minWeight = Math.min(minWeight, scoreWeight[i]);
            maxWeight = Math.max(maxWeight, scoreWeight[i]);
        }
    }

    public void setScoreWeights(double[] scoreWeight)throws DimensionMismatchException {
        if(scoreWeight.length!=ncat) throw new DimensionMismatchException(scoreWeight.length, ncat);
        this.scoreWeight = scoreWeight;
        for(int i=0;i<scoreWeight.length;i++){
            minWeight = Math.min(minWeight, scoreWeight[i]);
            maxWeight = Math.max(maxWeight, scoreWeight[i]);
        }
    }

    public double getMinScoreWeight(){
        return scoreWeight[0];
    }

    public double getMaxScoreWeight(){
        return scoreWeight[ncat-1];
    }

    public byte[] getScoreWeights(){
        byte[] sw = new byte[ncat];
        for(int i=0;i<ncat;i++){
            sw[i] = Double.valueOf(scoreWeight[i]).byteValue();
        }
        return sw;
    }

    public void setItemScoring(ItemScoring itemScoring){
         this.itemScoring = itemScoring;
    }

    public ItemScoring getItemScoring(){
         return itemScoring;
    }

}

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
package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.statistics.LinearTransformation;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.util.Precision;

import java.util.LinkedHashMap;
import java.util.Set;

public class MeanMeanMethod implements LinearTransformation{

    private LinkedHashMap<VariableName, ItemResponseModel> itemFormX = null;
    private LinkedHashMap<VariableName, ItemResponseModel> itemFormY = null;
    private double intercept = 0.0;
    private double slope = 1.0;
    private int precision = 2;
    Set<VariableName> sY = null;

    public MeanMeanMethod(LinkedHashMap<VariableName, ItemResponseModel> itemFormX, LinkedHashMap<VariableName, ItemResponseModel> itemFormY)throws DimensionMismatchException{
        this.itemFormX = itemFormX;
        this.itemFormY = itemFormY;
        checkDimensions();
        evaluate();
    }

    /**
     * For a common item linking design, both test form must have a set of items that are the same.
     * The parameter estimates will differ, but the common item must be paired. This method checks
     * that the common items are found in both item sets (Form X and Form Y). If not an exception occurs.
     *
     * This method checks that HashMaps have the same number of elements and that the keys in each map
     * are the same. The KeySet from itemFormY (sY) will be used for the keys hereafter.
     *
     *
     * @throws DimensionMismatchException
     */
    private void checkDimensions()throws DimensionMismatchException{
        Set<VariableName> sX = itemFormX.keySet();
        sY = itemFormY.keySet();
        if(sX.size()!=sY.size()) throw new DimensionMismatchException(itemFormX.size(), itemFormY.size());
        int mismatch = 0;
        for(VariableName v : sX){
            if(!sY.contains(v)) mismatch++;
        }
        for(VariableName v : sY){
            if(!sX.contains(v)) mismatch++;
        }
        if(mismatch>0) throw new DimensionMismatchException(mismatch, 0);
    }

    private void evaluate(){
        Mean meanDiscriminationX = new Mean();
        Mean meanDifficultyX = new Mean();
        Mean meanDiscriminationY = new Mean();
        Mean meanDifficultyY = new Mean();
        ItemResponseModel irmX;
        ItemResponseModel irmY;

        for(VariableName v : sY){
            irmX = itemFormX.get(v);
            irmY = itemFormY.get(v);

            irmX.incrementMeanMean(meanDiscriminationX, meanDifficultyX);
            irmY.incrementMeanMean(meanDiscriminationY, meanDifficultyY);

        }

        slope = meanDiscriminationX.getResult() / meanDiscriminationY.getResult();
        intercept = meanDifficultyY.getResult()-slope*meanDifficultyX.getResult();

    }

    public double getIntercept(){
        return Precision.round(intercept, precision);
    }

    public double getScale(){
        return Precision.round(slope, precision);
    }

    public void setPrecision(int precision){
        this.precision = precision;
    }

    public double transform(double x){
        return slope*x+intercept;
    }

}

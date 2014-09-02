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

import com.itemanalysis.psychometrics.irt.model.IrmType;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.scaling.LinearTransformation;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.Precision;

import java.util.LinkedHashMap;
import java.util.Set;

public class MeanSigmaMethod implements LinearTransformation{

    private LinkedHashMap<String, ItemResponseModel> itemFormX = null;
    private LinkedHashMap<String, ItemResponseModel> itemFormY = null;
    private double intercept = 0.0;
    private double slope = 1.0;
    private int precision = 2;
    Set<String> sY = null;
    private boolean populationStdDev = true;

    public MeanSigmaMethod(LinkedHashMap<String, ItemResponseModel> itemFormX, LinkedHashMap<String, ItemResponseModel> itemFormY,
                           boolean populationStdDev)throws DimensionMismatchException {
        this.itemFormX = itemFormX;
        this.itemFormY = itemFormY;
        this.populationStdDev = populationStdDev;
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
        Set<String> sX = itemFormX.keySet();
        sY = itemFormY.keySet();
        if(sX.size()!=sY.size()) throw new DimensionMismatchException(itemFormX.size(), itemFormY.size());
        int mismatch = 0;
        for(String s : sX){
            if(!sY.contains(s)) mismatch++;
        }
        for(String s : sY){
            if(!sX.contains(s)) mismatch++;
        }
        if(mismatch>0) throw new DimensionMismatchException(mismatch, 0);
    }

    private void evaluate(){
        Mean mX = new Mean();
        StandardDeviation sdX = new StandardDeviation(populationStdDev);
        Mean mY = new Mean();
        StandardDeviation sdY = new StandardDeviation(populationStdDev);
        ItemResponseModel irmX;
        ItemResponseModel irmY;

        for(String s : sY){
            irmX = itemFormX.get(s);
            irmY = itemFormY.get(s);

            irmX.incrementMeanSigma(mX, sdX);
            irmY.incrementMeanSigma(mY, sdY);

        }

        if(checkRaschModel()){
            slope = 1.0;
        }else{
            slope = sdY.getResult() / sdX.getResult();
        }
        intercept = mY.getResult()-slope*mX.getResult();


    }

    /**
     * Check to see if all item response models are in teh Rasch family of models.
     *
     * @return true if all item response models are in the Rasch family. Otherwise, return false.
     */
    private boolean checkRaschModel(){
        ItemResponseModel irm = null;
        for(String s : itemFormY.keySet()){
            irm = itemFormY.get(s);

            if(irm.getType()== IrmType.L3){
                if(irm.getNumberOfParameters()>1) return false;
            }else if(irm.getType()!=IrmType.PCM){
                return false;
            }
        }
        return true;
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

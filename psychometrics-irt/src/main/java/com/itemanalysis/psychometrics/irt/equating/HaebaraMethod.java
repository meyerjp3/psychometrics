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

import com.itemanalysis.psychometrics.analysis.AbstractMultivariateFunction;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.statistics.LinearTransformation;
import com.itemanalysis.psychometrics.uncmin.Uncmin_methods;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.Precision;

import java.util.HashMap;
import java.util.Set;

public class HaebaraMethod extends AbstractMultivariateFunction implements LinearTransformation, Uncmin_methods, UnivariateFunction {

    private HashMap<VariableName, ItemResponseModel> itemFormX = null;
    private HashMap<VariableName, ItemResponseModel> itemFormY = null;
    private QuadratureRule xDistribution = null;
    private QuadratureRule yDistribution = null;
    private int xDistributionSize = 0;
    private int yDistributionSize = 0;
    private EquatingCriterionType criterion = null;
    private double intercept = 0.0;
    private double slope = 1.0;
    private int precision = 2;
    private Set<VariableName> sY = null;
    private boolean standardized = true;

    public HaebaraMethod(HashMap<VariableName, ItemResponseModel> itemFormX, HashMap<VariableName, ItemResponseModel> itemFormY,
                         QuadratureRule xDistribution, QuadratureRule yDistribution,
                         EquatingCriterionType criterion)throws DimensionMismatchException{
        this.itemFormX = itemFormX;
        this.itemFormY = itemFormY;
        this.xDistribution = xDistribution;
        this.yDistribution = yDistribution;
        this.criterion = criterion;
        xDistributionSize = xDistribution.getNumberOfPoints();
        yDistributionSize = yDistribution.getNumberOfPoints();
        checkDimensions();
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
     * @throws org.apache.commons.math3.exception.DimensionMismatchException
     */
    private void checkDimensions()throws DimensionMismatchException {
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

    /**
     * Flag to standardize criterion function. This will affect teh value of the crierion function but no the
     * slope and intercept values.
     *
     * @param standardized if true the criterion function is standardized. If not, it is not standardized.
     */
    public void setStandardized(boolean standardized){
        this.standardized = standardized;
    }

    /**
     * Function to be minimized by ConjugateGradientSearch
     *
     * Uncmin index starts at 1
     * argument[1]=B (intercept) equating constant
     * argument[2]=A (slope) equating constant
     */
    public double value(double[] coefficient){
        double F = 0.0;
        switch(criterion){
            case Q1: F = getQ1(coefficient); break;
            case Q2: F = getQ2(coefficient); break;
            case Q1Q2: F = getQ1(coefficient) + getQ2(coefficient); break;
        }
        return F;
    }

    /**
     * Method needed for Brent optimizer when minimizing teh objective function for the Rasch family of models.
     * This method is required by the UnivariateFunction interface.
     *
     * @param x intercept
     * @return value of teh criterion function.
     */
    public double value(double x){
        double[] p = {x};
        return value(p);
    }

    public double getQ1(double[] coefficient){
        double dif = 0.0;
        double dif2 = 0.0;
        double sum = 0.0;
        int ncat = 2;
        ItemResponseModel irmY = null;
        ItemResponseModel irmX = null;
        double theta = 0.0, weight = 1.0;
        double LW = 0;
        double LP = 0;

        double slope = 1.0;
        double intercept = coefficient[0];
        if(coefficient.length>1){
            slope = coefficient[1];//Non-Rasch family of models
        }

        for(int i=0;i<yDistributionSize;i++){
            theta = yDistribution.getPointAt(i);
            weight = yDistribution.getDensityAt(i);
            LW += weight;
            for(VariableName v : sY){
                irmY = itemFormY.get(v);
                irmX = itemFormX.get(v);
                ncat = irmY.getNcat();
                for(int k=0;k<ncat;k++){
                    dif = irmY.probability(theta, k) - irmX.tStarProbability(theta, k, intercept, slope);
                    dif2 = dif*dif;
                    sum += dif2*weight;
                }
                if(i==0) LP += ncat;
            }
        }

        double L1 = LP*LW;
        if(standardized){
            return sum/L1;
        }else{
            return sum;
        }

    }

    public double getQ2(double[] coefficient){
        double dif = 0.0;
        double dif2 = 0.0;
        double sum = 0.0;
        int ncat = 2;
        ItemResponseModel irmY = null;
        ItemResponseModel irmX = null;
        double theta = 0.0, weight = 1.0;
        double LP = 0;
        double LW = 0;

        double slope = 1.0;
        double intercept = coefficient[0];
        if(coefficient.length>1){
            slope = coefficient[1];//Non-Rasch family
        }

        for(int i=0;i<xDistributionSize;i++){
            theta = xDistribution.getPointAt(i);
            weight = xDistribution.getDensityAt(i);
            LW += weight;
            for(VariableName v : sY){
                irmY = itemFormY.get(v);
                irmX = itemFormX.get(v);
                ncat = irmY.getNcat();
                for(int k=0;k<ncat;k++){
                    dif = irmX.probability(theta, k) - irmY.tSharpProbability(theta, k, intercept, slope);
                    dif2 = dif*dif;
                    sum += dif2*weight;
                }
                if(i==0) LP += ncat;
            }
        }

        double L2 = LP*LW;
        if(standardized){
            return sum/L2;
        }else{
            return sum;
        }

    }

    public void setIntercept(double intercept){
        this.intercept=intercept;
    }

    public void setScale(double scale){
        this.slope=scale;
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

    public double f_to_minimize(double[] x){
        double[] y = new double[x.length-1];
        if(x.length==2){
            y[0] = x[1];//Rasch family of models
        }else{
            y[0] = x[1];
            y[1] = x[2];
        }
        return value(y);
    }

    public void hessian(double[] x, double[][] h){

    }

    public void gradient(double[] x, double[] g){

    }

}

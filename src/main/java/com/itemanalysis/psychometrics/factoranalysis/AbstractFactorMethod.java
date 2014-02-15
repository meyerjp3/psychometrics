/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.factoranalysis;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * An abstract implementation of FactorMethod. It includes methods that are common
 * to all specific instances of FactorMethod.
 *
 * See the FactorMethod interface for details on each of the methods below.
 */
public abstract class AbstractFactorMethod implements FactorMethod{

    protected RealMatrix R = null;//the correlation matrix
    protected int nVariables = 0;
    protected int nFactors = 1;
    protected int nParam = 0;
    protected RotationMethod rotationMethod = RotationMethod.NONE;
    protected double[][] factorLoading = null;
    protected double[] uniqueness = null;
    protected double[] communality = null;
    protected double[] sumsOfSquares = null;
    protected double[] proportionOfVariance = null;
    protected double[] proportionOfExplainedVariance = null;

    /**
     * Lower bounds of the parameters
     * @return
     */
    public double[] getLowerBounds(){
        double[] lower = new double[nParam];
        for(int i=0;i<nParam;i++){
            lower[i] = 0.005;
        }
        return lower;
    }

    /**
     * Upper bound of the parameters
     * @return
     */
    public double[] getUpperBounds(){
        double[] upper = new double[nParam];
        for(int i=0;i<nParam;i++){
            upper[i] = 1.0;
        }
        return upper;
    }


    public double[][] getFactorLoading(){
        return factorLoading;
    }

    public double getFactorLoadingAt(int i, int j){
        return factorLoading[i][j];
    }

    public double getUniquenessAt(int i){
        return uniqueness[i];
    }

    public double getCommunalityAt(int i){
        return communality[i];
    }

    public double getSumsOfSquaresAt(int j){
        return sumsOfSquares[j];
    }

    public double getProportionOfExplainedVarianceAt(int j){
        return proportionOfExplainedVariance[j];
    }

    public double getProportionOfVarianceAt(int j){
        return proportionOfVariance[j];
    }

}

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

import com.itemanalysis.psychometrics.data.VariableName;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Formatter;

/**
 * An abstract implementation of FactorMethod. It includes methods that are common
 * to all specific instances of FactorMethod.
 *
 * See the FactorMethod interface for details on each of the methods below.
 */
public abstract class AbstractFactorMethod implements FactorMethod{

    protected RealMatrix R = null;//the correlation matrix
    protected RealMatrix Sinv = null;
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
    protected VariableName[] variableNames = null;

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

    /**
     * If the number of factors is greater than 1, compute the squared multiple correlation (SMC)
     * to use as start values for the communality.
     *
     * @return start values
     */
    public double[] getStartValues(){
        double[] start = new double[nVariables];
        double smc = 0.0;

        if(null==Sinv){
            for(int i=0;i<nVariables;i++){
                start[i] = 1.0;
            }
            return start;
        }

        if(nFactors==1){
            for(int i=0;i<nVariables;i++){
                start[i] = 0.5;
            }
        }else{
            for(int i=0;i<nVariables;i++){
                smc = 1.0-(1.0/Sinv.getEntry(i,i));
                start[i] = Math.min(R.getEntry(i,i)-smc, 1.0); //from psych package
//                start[i] = Math.min((1.0/Sinv.getEntry(i,i)), 1.0);
            }
        }

        return start;
    }

    public double[] getSquaredMultipleCorrelation(){
        double[] smc = new double[nVariables];
        for(int i=0;i<nVariables;i++){
            smc[i] =1.0-(1.0/Sinv.getEntry(i,i)); //from psych package
        }
        return smc;
    }

    public String printStartValues(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double[] start = getStartValues();
        for(int i=0;i<start.length;i++){
            f.format("%8.4f", start[i]);f.format("%n");
        }
        return f.toString();
    }

    public double[][] getFactorLoading(){
        return factorLoading;
    }

    /**
     *
     * @param i variable index of the factor loading
     * @param j factor index of the loading
     * @return
     */
    public double getFactorLoadingAt(int i, int j){
        return factorLoading[i][j];
    }

    /**
     *
     * @param factorID position number of factor of interest.
     * @return array of factor loadings
     */
    public double[] getLoadingsForFactorAt(int factorID){
        double[] l = new double[nVariables];
        for(int i=0;i<nVariables;i++){
            l[i] = factorLoading[i][factorID];
        }
        return l;
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

    public VariableName getVariableNameAt(int index){
        if(variableNames==null){
            VariableName variableName = new VariableName("V" + (index+1));
            return variableName;
        }
        return variableNames[index];
    }

    public void setVariableNameAt(int index, VariableName name){
        if(variableNames==null) variableNames = new VariableName[nVariables];
        variableNames[index] = name;
    }

    public void setVariableNameAt(int index, String name){
        VariableName variableName = new VariableName(name);
        setVariableNameAt(index, variableName);
    }

}

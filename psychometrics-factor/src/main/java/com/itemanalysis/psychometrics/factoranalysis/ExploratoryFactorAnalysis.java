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
 * This class is the main entry point for conducting exploratory factor analysis. Users will focus on this class. 
 * All other classes in this package provide support for this class. All factor analysis methods require a 
 * correlation matrix and number indicating the number of factors.
 */
public class ExploratoryFactorAnalysis {

    private RealMatrix correlationMatrix = null;
    private int nVariables = 0;
    private int nFactors = 0;
    private int nParameters = 0;
    private FactorMethod factorMethod = null;
    private RotationMethod rotationMethod = RotationMethod.NONE;
    private double fmin = 0.0;
    private String title = "";
    private VariableName[] variableNames = null;
    private EstimationMethod estimationMethod = EstimationMethod.MINRES;

    /**
     * The constructor requires a correlation matrix and the number of factors.
     *
     * @param correlationMatrix correlation matrix
     * @param nFactors number of factor to use
     */
    public ExploratoryFactorAnalysis(RealMatrix correlationMatrix, int nFactors){
        this.correlationMatrix = correlationMatrix;
        this.nFactors = Math.max(nFactors, 1);
        this.nVariables = correlationMatrix.getColumnDimension();
        this.nParameters = nVariables;

        //set default names
        variableNames = new VariableName[nVariables];
        for(int j=0;j<nVariables;j++){
            variableNames[j] = new VariableName("V" + (j+1));
        }
    }

    /**
     * The main method for estimating parameters. It will use the estimation method provided in the argument.
     *
     * @param fm estimation method for computing factor loadings
     * @param rotationMethod method of rotating factor loadings
     */
    public void estimateParameters(EstimationMethod fm, RotationMethod rotationMethod){
        this.rotationMethod = rotationMethod;

        if(fm==EstimationMethod.PRINCOMP){
            factorMethod = new PrincipalComponentsMethod(correlationMatrix, nFactors, rotationMethod);
            title = "Principal Components Analysis (" + rotationMethod.toString() + ")";
            estimationMethod = EstimationMethod.PRINCOMP;
        }else if(fm==EstimationMethod.ML){
            factorMethod = new MaximumLikelihoodMethod(correlationMatrix, nFactors, rotationMethod);
            title = "Maximum Likelihood Factor Analysis (" + rotationMethod.toString() + ")";
            estimationMethod = EstimationMethod.ML;
        }else if(fm==EstimationMethod.WLS){
            factorMethod = new WeightedLeastSquaresMethod(correlationMatrix, nFactors, rotationMethod);
            title = "Weighted Least Squares Factor Analysis (" + rotationMethod.toString() + ")";
            estimationMethod = EstimationMethod.WLS;
        }else if(fm==EstimationMethod.GLS){
            factorMethod = new GeneralizedLeastSquaresMethod(correlationMatrix, nFactors, rotationMethod);
            title = "Generalized Least Squares Factor Analysis (" + rotationMethod.toString() + ")";
            estimationMethod = EstimationMethod.GLS;
        }else{
            factorMethod = new MINRESmethod(correlationMatrix, nFactors, rotationMethod);
            title = "MINRES Factor Analysis (" + rotationMethod.toString() + ")";
            estimationMethod = EstimationMethod.MINRES;
        }

        fmin = factorMethod.estimateParameters();

    }

    public void setVariableNameAt(int index, VariableName variableName){
        variableNames[index] = variableName;
    }

    public void setVariableNameAt(int index, String name){
        VariableName variableName = new VariableName(name);
        setVariableNameAt(index, variableName);
    }

    public void estimateParameters(EstimationMethod fm){
        estimateParameters(fm, RotationMethod.NONE);
    }

    /**
     * Return s the factor method for obtaining values of the estimates and other information as indicated in the
     * FactorMethod interface.
     *
     * @return a FactorMethod
     */
    public FactorMethod getFactorMethod(){
        return factorMethod;
    }

    public String printOutput(){
        return printOutput(2);
    }

    public String printOutput(String title){
        this.title = title;
        return printOutput();
    }

    public String printOutput(String title, int precision){
        this.title = title;
        return printOutput(precision);
    }

    /**
     * Formatted output of the results. It includes the factor loadings, communalities, and unqiuenesses.
     *
     * @param precision number of decimal places to report
     * @return string of result
     */
    public String printOutput(int precision){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        int size = 24;
        String line1 = "=========================";
        String line2 = "-------------------------";
        for(int j=0;j<nFactors;j++){
            size = size + 10;
            line1 += "==========";
            line2 += "----------";
        }
        size = size + 10;
        line1 += "==========";
        line2 += "----------";
        size = size + 6;
        line1 += "======";
        line2 += "------";


        f.format("%36s", title); f.format("%n");
        f.format("%"+size+"s", line1);f.format("%n");
        f.format("%20s", "Name"); f.format("%4s", "");
        for(int j=0;j<nFactors;j++){
            f.format("%6s", "F"+(j+1)); f.format("%4s", "");
        }
        f.format("%6s", "H2");f.format("%4s", "");f.format("%6s", "U2");f.format("%n");
        f.format("%"+size+"s", line2);f.format("%n");

        for(int i=0;i<nVariables;i++){
//            f.format("%20s", "V"+ (i+1));f.format("%5s", "");
            f.format("%20s", variableNames[i].toString());f.format("%5s", "");
            for(int j=0;j<nFactors;j++){
                f.format("%6."+precision+"f",  factorMethod.getFactorLoadingAt(i,j));f.format("%4s", "");
            }
            f.format("%6."+precision+"f", factorMethod.getCommunalityAt(i));f.format("%4s", "");
            f.format("%6."+precision+"f", factorMethod.getUniquenessAt(i));f.format("%n");
        }

        f.format("%"+size+"s", line1);f.format("%n");
        f.format("%30s", "Value of the objective function = "); f.format("%-8.4f", fmin);
        f.format("%n");
        f.format("%n");

        f.format("%20s", "");
        for(int j=0;j<nFactors;j++){
            f.format("%6s", "F" + (j+1)); f.format("%2s", "");
        }
        f.format("%n");

        if(estimationMethod==EstimationMethod.PRINCOMP){
            f.format("%20s", "Eigen value");
        }else{
            f.format("%20s", "SS loadings");
        }
        for(int j=0;j<nFactors;j++){
            f.format("%6."+precision+"f", factorMethod.getSumsOfSquaresAt(j)); f.format("%2s", "");
        }
        f.format("%n");

        f.format("%20s", "Proportion Var");
        for(int j=0;j<nFactors;j++){
            f.format("%6."+precision+"f", factorMethod.getProportionOfVarianceAt(j)); f.format("%2s", "");
        }
        f.format("%n");

        f.format("%20s", "Proportion Explained");
        for(int j=0;j<nFactors;j++){
            f.format("%6."+precision+"f", factorMethod.getProportionOfExplainedVarianceAt(j)); f.format("%2s", "");
        }
        f.format("%n");



        return f.toString();
    }

}

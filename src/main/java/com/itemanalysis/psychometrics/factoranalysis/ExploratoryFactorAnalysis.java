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
    private double fmin = 0.0;
    private String title = "";

    /**
     * The constructor requires a corelation matrix and the number of factors.
     *
     * @param correlationMatrix correlation matrix
     * @param nFactors number of factor to use
     */
    public ExploratoryFactorAnalysis(RealMatrix correlationMatrix, int nFactors){
        this.correlationMatrix = correlationMatrix;
        this.nFactors = Math.max(nFactors, 1);
        this.nVariables = correlationMatrix.getColumnDimension();
        this.nParameters = nVariables;
    }

    /**
     * The main method for estimating parameters. It will use the estimation method provided in the argument.
     *
     * @param fm estimation method for computing factor loadings
     */
    public void estimateParameters(EstimationMethod fm){

        if(fm==EstimationMethod.PRINCOMP){
            factorMethod = new PrincipalComponentsMethod(correlationMatrix, nFactors);
            title = "Principal Components Analysis (no rotation)";
        }else if(fm==EstimationMethod.ML){
            factorMethod = new MaximumLikelihoodMethod(correlationMatrix, nFactors);
            title = "Maximum Likelihood Factor Analysis (no rotation)";
        }else if(fm==EstimationMethod.WLS){
            factorMethod = new WeightedLeastSquaresMethod(correlationMatrix, nFactors);
            title = "Weighted Least Squares Factor Analysis (no rotation)";
        }else if(fm==EstimationMethod.GLS){
//            factorMethod = new GeneralizedLeastSquaresMethod(correlationMatrix, nFactors);
//            title = "Generalized Least Squares Factor Analysis (no rotation)";
        }else{
            factorMethod = new MINRESmethod(correlationMatrix, nFactors);
            title = "MINRES Factor Analysis (no rotation)";
        }

        fmin = factorMethod.estimateParameters();

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
            f.format("%20s", "V"+ (i+1));f.format("%5s", "");
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

        f.format("%20s", "SS loadings");
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

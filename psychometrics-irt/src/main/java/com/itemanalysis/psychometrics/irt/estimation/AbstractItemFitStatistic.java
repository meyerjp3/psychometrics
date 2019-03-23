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
package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;

public abstract class AbstractItemFitStatistic implements ItemFitStatistic{

    protected int numberOfBins = 20;

    protected int numberOfCategories = 2;

    protected int minExpectedCellCount = 1;

    protected int totalCount = 0;

    protected double[][] table = null;

    protected double[] rowMargin = null;

    protected double[] rowThetaSum = null;

    protected boolean[] validRow = null;

    protected double[][] expectedValues = null;

    protected ItemResponseModel irm = null;

    protected void initialize(){
        table = new double[numberOfBins][numberOfCategories];
        rowMargin = new double[numberOfBins];
        rowThetaSum = new double[numberOfBins];
        validRow = new boolean[numberOfBins];
    }

    protected void condenseTable(){
        int midpoint = (int)(numberOfBins/2);
        double theta = 0;
        double prob = 0;

        //Collapse lower rows toward middle
        for(int i=0;i<midpoint;i++){
            validRow[i] = true;

            INNER:
            for(int j=0;j<numberOfCategories;j++){
                if(expectedValues[i][j]<minExpectedCellCount){
                    validRow[i] = false;
                    break INNER;
                }
            }

            if(!validRow[i]){
                for(int j=0;j<numberOfCategories;j++){
                    expectedValues[i+1][j] = expectedValues[i+1][j] + expectedValues[i][j];
                    table[i+1][j] = table[i+1][j] + table[i][j];
                }
                rowMargin[i+1] = rowMargin[i+1] + rowMargin[i];
                rowThetaSum[i+1] = rowThetaSum[i+1] + rowThetaSum[i];
            }else{
                //Recompute expected values
                theta = rowThetaSum[i]/rowMargin[i];
                for(int j=0;j<numberOfCategories;j++){
                    //NOTE: PARSCALE seems to round theta to two decimal places before computing expected value
                    prob = irm.probability(theta, j);
                    if(Double.isNaN(prob)) prob = 0;
                    expectedValues[i][j] = rowMargin[i]*prob;
                }
            }
        }//end lower condense


        //Collapse upper rows toward middle
        for(int i = numberOfBins-1;i>=midpoint;i--){
            validRow[i] = true;

            INNER:
            for(int j=0;j<numberOfCategories;j++){
                if(expectedValues[i][j]<minExpectedCellCount){
                    validRow[i] = false;
                    break INNER;
                }
            }


            if(!validRow[i]){
                for(int j=0;j<numberOfCategories;j++){
                    expectedValues[i-1][j] = expectedValues[i-1][j] + expectedValues[i][j];
                    table[i-1][j] = table[i-1][j] + table[i][j];
                }
                rowMargin[i-1] = rowMargin[i-1] + rowMargin[i];
                rowThetaSum[i-1] = rowThetaSum[i-1] + rowThetaSum[i];
            }else{
                //Recompute expected values
                theta = rowThetaSum[i]/rowMargin[i];
                for(int j=0;j<numberOfCategories;j++){
                    //NOTE: PARSCALE seems to round theta to two decimal places before computing expected value
                    prob = irm.probability(theta, j);
                    if(Double.isNaN(prob)) prob = 0;
                    expectedValues[i][j] = rowMargin[i]*prob;
                }
            }
        }//end upper condense

    }

    public int getTotalCount(){
        return totalCount;
    }

    public double getFrequencyAt(int i, int k){
        return table[i][k];
    }

    public double getProportionAt(int i, int k){
        return table[i][k]/rowMargin[i];
    }

    public String printContingencyTable(String title, boolean observedFrequencies){
        String s = title + "\n";
        double value = 0;
        for(int i=0;i<numberOfBins;i++){
            s+= "   SCORE " + i + ": ";
            if(validRow[i]){
                for(int j=0;j<numberOfCategories;j++){
                    if(observedFrequencies){
                        value = table[i][j];
                    }else{
                        value = table[i][j]*totalCount;
                    }
                    s += value +"  (" + expectedValues[i][j] + ")  ";
                }
                s += "  ROW N: " + rowMargin[i];
                s+= "   THETA: " + (rowThetaSum[i]/rowMargin[i]) + "\n";
            }
        }
        s += "\n";
        return s;
    }

    /**
     * Prints output in similar way to mirt package in R. This method is mainly
     * for viewing result during debugging. It shows the observed and expected frequencies.
     *
     * @return
     */
    public String printMirtFormatTable(boolean validRowsOnly){
        String s = "MIRT FORMATTED SUMMARY TABLE\n";
        double value = 0;
        for(int i=0;i<numberOfBins;i++){
            if(validRowsOnly){
                if(validRow[i]){
                    s+= "   SCORE " + i + ": ";
                    for(int j=0;j<numberOfCategories;j++){
                        value = table[i][j];
                        s += value +"  (" + expectedValues[i][j] + ")  ";
                    }
                    s += "  ROW N: " + rowMargin[i];
                    s+= "   THETA: " + (rowThetaSum[i]) + "\n";
                }
            }else{
                s+= "   SCORE " + i + ": ";
                for(int j=0;j<numberOfCategories;j++){
                    value = table[i][j];
                    s += value +"  (" + expectedValues[i][j] + ")  ";
                }
                s += "  ROW N: " + rowMargin[i];
                s+= "   THETA: " + (rowThetaSum[i]) + "\n";
            }

        }
        s += "\n";
        return s;
    }

}

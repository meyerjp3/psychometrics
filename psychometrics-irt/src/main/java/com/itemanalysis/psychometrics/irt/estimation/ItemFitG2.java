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
import com.itemanalysis.psychometrics.histogram.Cut;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import java.util.Formatter;

public class ItemFitG2 extends AbstractItemFitStatistic{

    private Cut thetaCut = null;

    private double G2 = 0;

    private double dfG2 = 1;

    private double pvalueG2 = 1;

    /**
     * This constructor is designed for the case where the number of table rows and the boundaries
     * are specified in thetaCut. Values will be assigned to each bin using the increment(theta, itemResponse)
     * method.
     *
     * @param irm an item response model object.
     * @param thetaCut an object that divides the range of these into bins.
     * @param minExpectedCellCount minimum expected value allowed in a table row.
     */
    public ItemFitG2(ItemResponseModel irm, Cut thetaCut, int minExpectedCellCount){
        this.irm = irm;
        this.numberOfCategories = irm.getNcat();
        this.thetaCut = thetaCut;
        this.minExpectedCellCount = minExpectedCellCount;
        this.numberOfBins = thetaCut.getNumberOfBins();
        initialize();
    }

    /**
     * Increment fit statistic. The item response is zero based. It should be
     * an ordered integer: 0, 1, 2, 3, ...
     *
     * @param theta EAP estimate of examinee ability
     * @param itemResponse an item response with values starting at 0. The value of teh response is also the
     *                     array index for the fit statistic.
     */
    public void increment(double theta, int itemResponse){
        for(int i=0;i<numberOfBins;i++){
            if(itemResponse!=-1 && thetaCut.getBinAt(i).inBin(theta)){
                table[i][itemResponse]++;
                rowMargin[i]++;
                rowThetaSum[i]+=theta;
                totalCount++;
                break;
            }
        }
    }

    public void increment(double theta, int itemResponse, double frequency){
        for(int i=0;i<numberOfBins;i++){
            if(itemResponse!=-1 && thetaCut.getBinAt(i).inBin(theta)){
                table[i][itemResponse]+=frequency;
                rowMargin[i]+=frequency;
                rowThetaSum[i]+=(theta*(double)frequency);
                totalCount+=frequency;
                break;
            }
        }
    }

    protected void initializeExpectedFrequencies(){
        expectedValues = new double[numberOfBins][numberOfCategories];
        double theta = 0;
        double prob = 0;
        for(int i=0;i<numberOfBins;i++){
            theta = rowThetaSum[i]/rowMargin[i];
            validRow[i] = true;//initialize to true
            for(int j=0;j<numberOfCategories;j++){
                //NOTE: PARSCALE seems to round theta to two decimal places before computing expected value
                prob = irm.probability(theta, j);
                if(Double.isNaN(prob)) prob = 0;
                expectedValues[i][j] = rowMargin[i]*prob;
            }
        }
    }

    public void compute(){
        int validRowCount = 0;
        initializeExpectedFrequencies();

        //For debugging
//        System.out.println(printContingencyTable("ORIGINAL TABLE"));

        condenseTable();

        //For debugging
//        System.out.println(printContingencyTable("CONDENSED TABLE"));

        double r = 0;
        G2 = 0;
        double expectedFrequency = 0;
        for(int i=0;i<numberOfBins;i++){
            if(validRow[i]){
                for(int j=0;j<numberOfCategories;j++){
                    r = table[i][j]+.001;//Add small amount to avoid taking log of zero
                    expectedFrequency = expectedValues[i][j]+.001;//Add small amount to avoid taking log of zero
                    G2 += r*Math.log(r / expectedFrequency);

                    //For debugging
//                    System.out.println("i: " + i + "  j: " + j + "  R: " + r + " EF: " + expectedFrequency );
                }
                validRowCount++;
            }
        }

        G2 = G2*2.0;
        dfG2 = ((double)validRowCount)*((double)numberOfCategories-1.0);

        try{
            ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(dfG2);
            pvalueG2 = 1.0-chiSquaredDistribution.cumulativeProbability(G2);
        }catch(Exception ex){
            pvalueG2 = Double.NaN;
        }

    }

    public double getThetaAt(int i){
        return rowThetaSum[i]/rowMargin[i];
    }

    public double getValue(){
        return G2;
    }

    public double getDegreesOfFreedom(){
        return dfG2;
    }

    public double getPValue(){
        return pvalueG2;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%5s", "G2  ");
        f.format("%8.4d", getValue()); f.format("%2s", "");
        f.format("%4d", (int)getDegreesOfFreedom()); f.format("%2s", "");
        f.format("%1.4f", getPValue());
        return f.toString();
    }

}

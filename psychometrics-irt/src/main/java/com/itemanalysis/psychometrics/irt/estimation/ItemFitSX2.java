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

import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import java.util.Formatter;

/**
 * Computes the generalized S-X2 statistic developed by:
 *
 * Kang, T., and Chen, T., T. (2007). An investigation of the performance of the generalized
 * S-X2 item-fit index for polytomous IRT models. ACT Research Report Series 2007-1. ACT: Iowa City, Iowa.
 *
 * For a test of all binary items, this statistic is identical to Orlando and Thissen's S-X2 statistic
 * that is descrined in:
 *
 * Orlando, M., and Thissen, D. (2000). Likelihood based item-fit indices for dichotomous item
 * response theory models. Applied Psychological Measurement, 24(1), 50-64.
 *
 * This implementation differs from that described in Kang and Chen (2007) in one major way.
 * The table is collapsed across rows only. No collapsing is done over response categories.
 * Any row with an average expected value less than 1 will be collapsed (see Orlando and Thissen, 2000).
 *
 *
 */
public class ItemFitSX2 extends AbstractItemFitStatistic {

    private double SX2 = 0;

    private double dfSX2 = 0;

    private double pvalueSX2 = 0;

    private int nPoints = 0;

    private int maxItemScore = 0;

    private int maxTestScore = 0;

    private int[] validColumnIndex = null;

    int end = 0;

    private QuadratureRule latentDistribution = null;

    private IrtObservedScoreDistribution irtObservedScoreDistribution = null;

    private IrtObservedScoreDistribution irtObservedScoreDistributionWithoutItem = null;

    public ItemFitSX2(IrtObservedScoreDistribution irtObservedScoreDistribution,
                      IrtObservedScoreDistribution irtObservedScoreDistributionWithoutItem,
                      ItemResponseModel irm, int maxTestScorePlusOne, int minExpectedCellCount){
        this.irtObservedScoreDistribution = irtObservedScoreDistribution;
        this.irtObservedScoreDistributionWithoutItem = irtObservedScoreDistributionWithoutItem;
        this.irm = irm;
        this.numberOfBins = maxTestScorePlusOne;
        this.numberOfCategories = irm.getNcat();
        this.minExpectedCellCount = minExpectedCellCount;
        this.latentDistribution = irtObservedScoreDistribution.getLatentDistribution();
        this.nPoints = latentDistribution.getNumberOfPoints();
        this.maxItemScore = (int)irm.getMaxScoreWeight();
        this.maxTestScore = numberOfBins-1;
        end = maxTestScore-maxItemScore;

        validColumnIndex = new int[numberOfBins];

        initialize();
    }

    /**
     * Increment fit statistic assuming there is a table row for each possible summed score.
     *
     * ItemFitChiSquare(ItemResponseModel irm, int numberOfBins, int minExpectedCellCount)
     *
     * @param summedScore summed score value.
     * @param itemResponse an item response.
     */
    public void increment(int summedScore, int itemResponse){
        if(itemResponse!=-1){
            table[summedScore][itemResponse]++;
            rowMargin[summedScore]++;
            totalCount++;
        }
    }

    public void increment(int summedScore, int itemResponse, double frequency){
        if(itemResponse!=-1){
            table[summedScore][itemResponse]+=frequency;
            rowMargin[summedScore]+=frequency;
            totalCount+=frequency;
        }
    }

    private double expectedValue(int summedScore, int itemScore){
        double theta = 0;
        double prob = 0;
        double probScore = 0;
        double probScoreWithout = 0;
        double density = 0;
        double numerator = 0;
        double denominator = 0;
        int withoutScore = summedScore-itemScore;

        for(int k=0;k<nPoints;k++){
            theta = latentDistribution.getPointAt(k);
            density = latentDistribution.getDensityAt(k);

            prob = irm.probability(theta, itemScore);
            probScore = irtObservedScoreDistribution.getProbabilityAt(k, summedScore);
            probScoreWithout = irtObservedScoreDistributionWithoutItem.getProbabilityAt(k, withoutScore);

            numerator += prob*probScoreWithout*density;
            denominator += probScore*density;
        }

        if(denominator<=0) return 0;
        return numerator/denominator;
    }

    private void initializeExpectedFrequencies() {
        expectedValues = new double[numberOfBins][numberOfCategories];

        for (int i = 1; i < table.length-1; i++) {
            validRow[i] = true;//initialize to true

            if(i<maxItemScore){
                for(int j = 0; j <= i; j++){
                    //expectedValue*rowMargin because using expected frequency, not expected proportion
                    expectedValues[i][j] = expectedValue(i, j)*rowMargin[i];
                }
            }else if(i>end){
                for(int j = i-end; j<=maxItemScore; j++){
                    //expectedValue*rowMargin because using expected frequency, not expected proportion
                    expectedValues[i][j] = expectedValue(i, j)*rowMargin[i];
                }
            }else{
                for (int j = 0; j < numberOfCategories; j++) {
                    //expectedValue*rowMargin because using expected frequency, not expected proportion
                    expectedValues[i][j] = expectedValue(i, j)*rowMargin[i];
                }
            }


        }
    }

//    private void collapseColumns(int i){
//        if(numberOfCategories==2) return;
//        boolean collapsed = false;
//
//        for(int j=0;j<(numberOfCategories-2);j++){
//            if(expectedValues[i][j]<minExpectedCellCount){
//                //bump up
//                expectedValues[i][j+1] += expectedValues[i][j];
//                expectedValues[i][j] = 0;
//
//                table[i][j+1] += table[i][j];
//                table[i][j] = 0;
//
//                collapsed = true;
//            }
//
//        }
//        if(expectedValues[i][numberOfCategories-1]<minExpectedCellCount){
//            expectedValues[i][numberOfCategories-2] += expectedValues[i][numberOfCategories-1];
//            expectedValues[i][numberOfCategories-1] = 0;
//
//            table[i][numberOfCategories-2] += table[i][numberOfCategories-1];
//            table[i][numberOfCategories-1] = 0;
//
//            collapsed = true;
//        }
//
//
//        if(collapsed){
//            //shift everything left
//            int index=0;
//            for(int j=0;j<numberOfCategories;j++){
//                index=j;
//                while(expectedValues[i][j]==0 && index<numberOfCategories){
//                    expectedValues[i][j] = expectedValues[i][index];
//                    expectedValues[i][index] = 0;
//
//                    table[i][j] += table[i][index];
//                    table[i][index] = 0;
//
//                    index++;
//                }
//                if(expectedValues[i][j]>0){
//                    validColumnIndex[i]++;
//                }
//            }
//        }else{
//            validColumnIndex[i]=numberOfCategories;
//        }
//
//
//    }

    @Override
    protected void condenseTable(){
        validRow[0] = false;
        validRow[numberOfBins-1] = false;
        int midpoint = (int)(numberOfBins/2);

//        for(int i=1;i<maxTestScore;i++){
//            if(i<maxItemScore || i>end){
//                validRow[i] = false;
//            }else{
//                validRow[i] = true;
//            }
//            //Collapse columns
//            collapseColumns(i);
//        }

        double sum = 0;

        //Collapse lower rows toward middle
        for(int i=1;i<midpoint;i++){//skip first row
            validRow[i] = true;
//            if(validColumnIndex[i]==0) validRow[i] = false;

            INNER:
//            for(int j=0;j<validColumnIndex[i];j++){
            for(int j=0;j<numberOfCategories;j++){
               sum += expectedValues[i][j];
//                if(expectedValues[i][j]<minExpectedCellCount){
//                    validRow[i] = false;
//                    break INNER;
//                }
            }
//            System.out.println(meanEv.getResult());
            if(sum/numberOfCategories < minExpectedCellCount || i<maxItemScore) validRow[i] = false;
            sum=0;

            if(!validRow[i]){
                for(int j=0;j<numberOfCategories;j++){
                    expectedValues[i+1][j] = expectedValues[i+1][j] + expectedValues[i][j];
                    table[i+1][j] = table[i+1][j] + table[i][j];
                }
                rowMargin[i+1] = rowMargin[i+1] + rowMargin[i];
                rowThetaSum[i+1] = rowThetaSum[i+1] + rowThetaSum[i];
            }
        }//end lower condense


        //Collapse upper rows toward middle
        for(int i = numberOfBins-2;i>=midpoint;i--){//skip last row
//            if(validColumnIndex[i]==0) validRow[i] = false;

            INNER:
//            for(int j=0;j<validColumnIndex[i];j++){
            for(int j=0;j<numberOfCategories;j++){
                sum += expectedValues[i][j];
//                if(expectedValues[i][j]<minExpectedCellCount){
//                    validRow[i] = false;
//                    break INNER;
//                }
            }
            if(sum/numberOfCategories < minExpectedCellCount || i>end) validRow[i] = false;
            sum=0;


            if(!validRow[i]){
                for(int j=0;j<numberOfCategories;j++){
                    expectedValues[i-1][j] = expectedValues[i-1][j] + expectedValues[i][j];
                    table[i-1][j] = table[i-1][j] + table[i][j];
                }
                rowMargin[i-1] = rowMargin[i-1] + rowMargin[i];
                rowThetaSum[i-1] = rowThetaSum[i-1] + rowThetaSum[i];
            }
        }//end upper condense

    }

    public double getThetaAt(int i){
        return irtObservedScoreDistribution.getEAP(i);
    }

    public void compute(){
        int validRowCount = 0;
        initializeExpectedFrequencies();

        //For debugging
//        System.out.println(this.printMirtFormatTable(false));

        condenseTable();

        //For debugging
//        System.out.println(this.printMirtFormatTable(true));

        //Compute statistic.
        double d = 0;
        for(int x=1;x<numberOfBins-1;x++){
            if(validRow[x]){
                validRowCount++;
                for(int h=0;h<numberOfCategories;h++){
                    if(expectedValues[x][h]>0) {
                        d = table[x][h]-expectedValues[x][h];
                        SX2 += (d*d)/expectedValues[x][h];
                    }
//                    else{
//                        numberOfCombinedCategories++;
//                    }
                }
            }
        }

//        System.out.println("  VALROW: " + validRowCount + "  NPAR: " + irm.getNumberOfEstimatedParameters() + "  COMBINED: " + numberOfCombinedCategories);

        //Compute degrees of freedom
//        double K = maxTestScore-2.0*maxItemScore+1;//K and validRow count are the same
        dfSX2 = maxItemScore*validRowCount-irm.getNumberOfEstimatedParameters();
        try{
            ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(dfSX2);
            pvalueSX2 = 1.0-chiSquaredDistribution.cumulativeProbability(SX2);
        }catch(Exception ex){
            pvalueSX2 = Double.NaN;
        }
    }

    public double getValue(){
        return SX2;
    }

    public double getDegreesOfFreedom(){
        return dfSX2;
    }

    public double getPValue(){
        return pvalueSX2;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%5s", "SX2  ");
        f.format("%8.4f", getValue()); f.format("%2s", "");
        f.format("%4d", (int)getDegreesOfFreedom()); f.format("%2s", "");
        f.format("%1.4f", getPValue());
        return f.toString();
    }

}

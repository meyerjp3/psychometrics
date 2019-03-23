/*
 * Copyright 2014 J. Patrick Meyer
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

package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import org.apache.commons.math3.util.Precision;

/**
 * A class for creating a raw to ablity score transformation table. It has its own estimation method.
 * However, the estimation method is identical to
 * {@link JointMaximumLikelihoodEstimation#updatePerson(int, int, double, double)}.
 *
 */
public class RaschScoreTable {

    private ItemResponseModel[] irm = null;
    private int[] extremeItem = null;
    private int[] droppedStatus = null;
    private double[] theta = null;
    private double[] thetaStdError = null;
    private double[] sumScore = null;
    private double MinPS = 0.0;
    private double MaxPS = 0.0;
    private int nItems = 0;
    private int precision = 2;

    /**
     * The constructor should be called after joint maximum likelhood estimation is complete.
     *
     * @param irm an array of item resposne model objects.
     * @param extremeItem an array of extreme item codes.
     * @param droppedStatus an array of dropped item status codes. This argument is mainly needed for polytomous items.
     */
    public RaschScoreTable(ItemResponseModel[] irm, int[] extremeItem, int[] droppedStatus, int precision){
        this.irm = irm;
        this.extremeItem = extremeItem;
        this.droppedStatus = droppedStatus;
        nItems = irm.length;
        this.precision = precision;
        computeMPS();
    }

    /**
     * Computes the maximum possible test score. This value is needed for identifying extreme examinees.
     */
    private void computeMPS(){

        //An item response model with fixed difficulty cannot be an extreme item
        for(int i=0;i<nItems;i++){
            if(irm[i].isFixed()){
                extremeItem[i]=0;
            }
        }

        for(int i=0;i<nItems;i++){
            if(droppedStatus[i]==0 && extremeItem[i]==0){
                MinPS += irm[i].getMinScoreWeight();
                MaxPS += irm[i].getMaxScoreWeight();
            }
        }

        int size = (int)(MaxPS-MinPS+1.0);
        theta = new double[size];
        thetaStdError = new double [size];
        sumScore = new double[size];

        for(int i=0;i<size;i++){
            sumScore[i] = i;
        }

    }

    /**
     * This method is the main entry point into the class. It should be called soon after instantiation.
     *
     * @param globalMaxIter maximum number of iterations.
     * @param globalConvergence convergence criterion.
     * @param adjustment extreme score adjustment factor.
     */
    public void updateScoreTable(int globalMaxIter, double globalConvergence, double adjustment){
        for(int i=0;i<theta.length;i++){
            updatePerson(i, globalMaxIter, globalConvergence, adjustment);
        }
    }

    private double updatePerson(int index, int maxIter, double converge, double adjustment){
        int iter = 0;
        double delta = 1.0+converge;
        double previousTheta = 0.0;
        double TCC1 = 0.0; //this is the TCC at current theta rho
        double TCC2 = 0.0; //this is the TCC at current theta rho + d
        double shift = 0.0;

        //adjust extreme person scores
        double adjustedSumScore = sumScore[index];
        if(sumScore[index]==MinPS){
            adjustedSumScore += adjustment;
        }else if(sumScore[index]==MaxPS){
            adjustedSumScore -= adjustment;
        }

        while(delta > converge && iter < maxIter){
            previousTheta = theta[index];
            TCC1 = 0.0;
            TCC2 = 0.0;

            for(int j=0;j<nItems;j++){
                if(droppedStatus[j]==0 && extremeItem[j]==0){
                    TCC1 += irm[j].expectedValue(theta[index]);
                    shift = theta[index]+delta;
                    TCC2 += irm[j].expectedValue(shift);
                }
            }
            theta[index] = curveFit(theta[index], adjustedSumScore, delta, TCC1, TCC2, MinPS, MaxPS);
            delta = Math.abs(previousTheta-theta[index]);
            iter++;
        }
        return theta[index];
    }

    /**
     * This implementation of JMLE involve proportional curve fitting. The curve fit is done in this method.
     * This method is called for updating item difficulty and person ability (i.e. theta).
     *
     * @param estimate current estimate
     * @param score person or item score.
     * @param delta Current change in logits. It gets smaller with each global iteration.
     * @param expected expected score based on the current parameter estimates
     * @param expectedPlus expected score  based on the current parameter estimates plus delta
     * @param minObserved minimum possible item or person score.
     * @param maxObserved maximum possible item or person score.
     * @return updated parameter estimate.
     */
    private double curveFit(double estimate, double score, double delta, double expected, double expectedPlus, double minObserved, double maxObserved){
        double slope = delta/(logisticOgive(expectedPlus, minObserved, maxObserved) -
                logisticOgive(expected, minObserved, maxObserved));
        double intercept = estimate - slope*logisticOgive(expected, minObserved, maxObserved);
        double tempEstimate = slope*logisticOgive(score, minObserved, maxObserved)+intercept;
        //do not change theta by more than one logit per iteration - from WINSTEPS documents
        double newValue = Math.max(Math.min(estimate+1, tempEstimate), estimate-1);
        return newValue;
    }

    /**
     * Local logistic ogive from WINSTEPS documentation.
     *
     * @param x observed score
     * @param xMin minimum possible score
     * @param xMax maximum possible score
     * @return
     */
    private double logisticOgive(double x, double xMin, double xMax){
        return Math.log((x-xMin)/(xMax-x));
    }

    /**
     * The score table is formatted for output here.
     *
     * @return score table.
     */
    public String printScoreTable(){
        TextTableColumnFormat[] cformats = new TextTableColumnFormat[3];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setDoubleFormat(8, precision, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[2] = new TextTableColumnFormat();
        cformats[2].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);

        int n = theta.length;

        TextTable table = new TextTable();
        table.addAllColumnFormats(cformats, n+5);
        table.getRowAt(0).addHeader(0, 3, "SCORE TABLE", TextTablePosition.CENTER);
        table.getRowAt(1).addHorizontalRule(0, 3, "=");
        table.getRowAt(2).addHeader(0, 1, "Score", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(1, 1, "Theta", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(2, 1, "Std. Error", TextTablePosition.CENTER);
        table.getRowAt(3).addHorizontalRule(0, 6, "-");
        for(int i=0;i<n;i++){
            table.getRowAt(i+4).addDoubleAt(0, sumScore[i]);
            table.getRowAt(i+4).addDoubleAt(1, theta[i]);
            table.getRowAt(i+4).addDoubleAt(2, thetaStdError[i]);
        }
        table.getRowAt(n+4).addHorizontalRule(0, 3, "=");

        return table.toString();
    }

    /**
     * A linear transformation can be applied to teh score table. This method should be called after
     * {@link #computePersonStandardErrors()}.
     *
     * @param lt linear transformation to be applied.
     * @param precision number of decimal places to retain after the linear transformation.
     */
    public void linearTransformation(DefaultLinearTransformation lt, int precision){
        for(int i=0;i<theta.length;i++){
            theta[i] = Precision.round(lt.transform(theta[i]), precision);
            thetaStdError[i] *= lt.getScale();
        }
    }

    /**
     * Person ability parameter standard error calculation.
     */
    public void computePersonStandardErrors(){
        thetaStdError = new double[theta.length];
        double info = 0.0;
        for(int i=0;i<theta.length;i++){
            info = 0.0;
            for(int j=0;j<nItems;j++){
                if(droppedStatus[j]==0 && extremeItem[j]==0){
                    info += irm[j].itemInformationAt(theta[i]);
                }
            }
            thetaStdError[i] = 1.0/Math.sqrt(info);
        }
    }

    public Object[][] getOutputArray(){

        Object[][] out = new Object[theta.length][3];

        for(int i=0;i<theta.length;i++){
            out[i][0] = sumScore[i];
            out[i][1] = theta[i];
            out[i][2] = thetaStdError[i];
        }
        return out;
    }

}

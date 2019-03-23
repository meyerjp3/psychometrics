/*
 * Copyright 2013 J. Patrick Meyer
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

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.factoranalysis.EstimationMethod;
import com.itemanalysis.psychometrics.factoranalysis.ExploratoryFactorAnalysis;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.irt.model.RaschRatingScaleGroup;
import com.itemanalysis.psychometrics.statistics.StreamingCovarianceMatrix;
import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedHashMap;

/**
 * Joint maximum likelihood estimation (JMLE) of the Rasch, partial credit, and rating scale models. Methods conduct item
 * and person parameter estimation, computation of standard errors, and linear transformations of parameters. Fixed
 * common item calibration is also possible. JMLE is an iterative procedure. At each iteration, item and person
 * parameters estimates are updated using a proportional curve fitting algorithm instead of the Newton-Raphson method.
 * For details on the estimation algorithm see:
 * <p>
 * Meyer, J. P., & Hailey, E. (2012). A study of Rasch partial credit, and rating scale model<br />
 *  &nbsp;&nbsp;&nbsp;&nbsp;parameter recovery in WINSTEPS and jMetrik. <em>Journal of Applied Measurement</em>,<br />
 *  &nbsp;&nbsp;&nbsp;&nbsp;<em>13</em>(3), 248-258.
 * </p>
 * In additional to parameter estimates, this class also provides a way to obtain item and person fit statistics and
 * scale quality statistics.
 *
 * @Author J. Patrick Meyer
 */
public class JointMaximumLikelihoodEstimation{

    private byte data[][] = null;
    private double[] theta = null;
    private double[] thetaStdError = null;
    private double[] sumScore = null;
    private ArrayList<IterationRecord> iterationHistory = null;
    private int[] extremePerson = null;
    private int[] extremeItem = null;
    private int[] droppedStatus = null;
    private ItemResponseModel[] irm = null;
    private RaschFitStatistics[] itemFit = null;
    private ItemResponseSummary[] itemSummary = null;
    private LinkedHashMap<String, RaschRatingScaleGroup> rsg = null;
    private int nPeople = 0;
    private int nItems = 0;
    private int maxCategory = 2;
    private double[] minPossibleTestScore = null;//sum of min score points for nonextreme items completed by the examinee
    private double[] maxPossibleTestScore = null;//sum of max score points for nonextreme items completed by the examinee
    private int[] numberOfCompletedItems = null;
    private double adjustment = 0.3;
    private int extremeCount = 0;
    private int droppedCount = 0;
    private Object[][] scoreConversionTable = null;

    /**
     * The object is created with a set of data and array of item response models (See {@link ItemResponseModel}.
     * The data must be arranged such that each row respresents an examinee and each column represents an item.
     * All item responses must be zero or larger (i.e. nonnegative). Use an item response value of -1 to indicate
     * a missing item response. The length of the item response model array must equal the number of columns in
     * the data array.
     *
     * @param data a two-way array of item responses.
     * @param irm an array of item response model.
     * @throws DimensionMismatchException
     */
    public JointMaximumLikelihoodEstimation(byte[][] data, ItemResponseModel[] irm)throws DimensionMismatchException{
        if(data[0].length!=irm.length) throw new DimensionMismatchException(data[0].length, irm.length);
        this.irm = irm;
        this.nPeople = data.length;
        this.data = data;
        iterationHistory = new ArrayList<IterationRecord>();
        initializeCounts();
    }

    private void initializeCounts(){
        this.nItems = irm.length;
        this.extremeItem = new int[nItems];
        this.droppedStatus = new int[nItems];
        this.itemSummary = new ItemResponseSummary[nItems];
        this.extremePerson = new int[nPeople];
        this.sumScore = new double[nPeople];
        this.minPossibleTestScore = new double[nPeople];
        this.maxPossibleTestScore = new double[nPeople];
        this.numberOfCompletedItems = new int[nPeople];
        rsg = new LinkedHashMap<String, RaschRatingScaleGroup>();

        //initialize item summaries
        ItemResponseSummary temp = null;
        VariableName vName = null;
        String groupId = "";
        for(int j=0;j<nItems;j++){
            vName = irm[j].getName();
            groupId = irm[j].getGroupId();
            temp = new ItemResponseSummary(vName, groupId, adjustment, irm[j].getScoreWeights());
            temp.setPositionInArray(j);
            maxCategory = Math.max(maxCategory, irm[j].getNcat());
            itemSummary[j] = temp;
            extremeItem[j] = 0;
        }

        //?? do I need this??
        for(int i=0;i<nPeople;i++){
            extremePerson[i]=0;
        }
    }

    /**
     * summarizeData() is recursive. This helper method clears the scores before each iteration of summarizeData().
     */
    private void clearScores(){
        for(int i=0;i<nPeople;i++){
            sumScore[i]=0;
            minPossibleTestScore[i] = 0;
            maxPossibleTestScore[i] = 0;
        }
        for(int j=0;j<nItems;j++){
            itemSummary[j].clearCounts();
        }
        for(String s : rsg.keySet()){
            rsg.get(s).clearCounts();
        }

        rsg.clear();
    }

    /**
     * Summarizes data into frequency counts. It keeps track of extreme persons (i.e. examinees that
     * earn the minimum or maximum possible test score) and extreme items (i.e. items answered correctly by
     * everyone or no one). Extreme items and persons are flagged. Frequency counts represent the valid
     * counts (i.e. frequencies for non-extreme persons and items). This method is recursive. It continues
     * as long as new extreme items or persons are detected. Otherwise, it stops.
     *
     * @param adjustment and adjustmnet factor applied to the ItemResponseSummary. This adjustment is needed
     *                   for estimation of extreme persons and items.
     */
    public void summarizeData(double adjustment)throws NoDataException{
        int extremePersonCount = 0;
        int extremeItemCount = 0;
        int droppedItemCount = 0;
        clearScores();

        //summarize each item individually
        for(int i=0;i<nPeople;i++){
            for(int j=0;j<nItems;j++){
                if(data[i][j]!=-1){
                    if(droppedStatus[j]==0 && extremeItem[j]==0){
                        //person scores should use only the nonextreme items
                        sumScore[i] += data[i][j];
                        minPossibleTestScore[i] += irm[j].getMinScoreWeight();
                        maxPossibleTestScore[i] += irm[j].getMaxScoreWeight();
                    }
                    if(extremePerson[i]==0){
                        //item scores should use only the nonextreme examinees
                        itemSummary[j].increment(data[i][j]);
                    }

                    numberOfCompletedItems[i]++;//count number of nonmissing item responses
                }

            }

            //check for extreme person
            if(sumScore[i]==minPossibleTestScore[i]){
                extremePerson[i] = -1;
                extremePersonCount++;
            }else if(sumScore[i]==maxPossibleTestScore[i]){
                extremePerson[i] = 1;
                extremePersonCount++;
            }else{
                extremePerson[i] += 0;
            }
        }

        //Combine items in the same rating scale group group.
        //Must be done after frequencies are tabulated for each item.
        String groupId = "";
        RaschRatingScaleGroup raschRatingScaleGroup = null;
        for(int j=0;j<nItems;j++){
            if(irm[j].getNcat()>2){
                groupId = irm[j].getGroupId();
                raschRatingScaleGroup = rsg.get(groupId);
                if(raschRatingScaleGroup==null){
                    raschRatingScaleGroup = new RaschRatingScaleGroup(groupId, irm[j].getNcat());
                    rsg.put(groupId, raschRatingScaleGroup);
                }
                raschRatingScaleGroup.addItem(irm[j], itemSummary[j], j);
            }
        }

        //check for extreme items
        for(int j=0;j<nItems;j++){
            if(itemSummary[j].isExtremeMaximum()){
                extremeItem[j] = 1;
                extremeItemCount++;
            }else if(itemSummary[j].isExtremeMinimum()){
                extremeItem[j] = -1;
                extremeItemCount++;
            }else{
                extremeItem[j] += 0;
            }

            //check for extreme category
            if(irm[j].getNcat()>2){
                RaschRatingScaleGroup tempGroup = rsg.get(irm[j].getGroupId());
                tempGroup.checkForDroppping();
                droppedStatus[j] = tempGroup.dropStatus();
                if(droppedStatus[j]!=0) droppedItemCount++;
            }


        }//end loop over items

        double priorCount = extremeCount;
        extremeCount = extremeItemCount + extremePersonCount;

        int priorDropCount = droppedCount;
        droppedCount = droppedItemCount;

        if(droppedItemCount!=priorDropCount ||
                (extremeCount != priorCount &&
                extremeItemCount <= nItems &&
                extremePersonCount <= nPeople)){

            summarizeData(adjustment);

        }

        if(extremeItemCount==nItems || extremePersonCount==nPeople){
            //no data remaining, throw an exception
            throw new NoDataException();
        }

    }

    /**
     * A shortcut method that only requires input for the maximum number of global iterations and the global
     * convergence criterion.
     *
     * @param globalMaxIter maximum number of iteration in JMLE
     * @param globalConv maximum change in logits convergen criterion
     * @return
     */
    public boolean estimateParameters(int globalMaxIter, double globalConv){
        return estimateParameters(globalMaxIter, globalConv, 1, .01, true);
    }

    /**
     * A shortcut method that only requires input for the maximum number of global iterations and the global
     * convergence criterion.
     *
     * @param globalMaxIter maximum number of iteration in JMLE
     * @param globalConv maximum change in logits convergen criterion
     * @param centerItems establish identification by centering item about the item difficulty mean (the approach
     *                    typically used in Rasch measurement). If false establish identification by centering
     *                    persons around the mean ability. Centering only done for nonextreme persons and items.
     * @return
     */
    public boolean estimateParameters(int globalMaxIter, double globalConv, boolean centerItems){
        return estimateParameters(globalMaxIter, globalConv, 1, .01, centerItems);
    }

    /**
     * This method is where the estimation is managed. It corresponds to the global iterations in the Joint
     * Maximum Likelihood Estimation paradigm. First items and thresholds are updated, then persons
     * are updated. These cycles continue until the number of iterations reaches the global maximum
     * or the convergence criterion is reached.
     *
     * @param globalMaxIter maximum number of iteration in JMLE
     * @param globalConv maximum change in logits convergen criterion
     * @param personMaxIter maximum number of iterations during the person update. Usually a maximum of one person
     *                      update is sufficient.
     * @param personConv convergence criterion during the person update only.
     * @param centerItems establish identification by centering item about the item difficulty mean (the approach
     *                    typically used in Rasch measurement). If false establish identification by centering
     *                    persons around the mean ability. Centering only done for nonextreme persons and items.
     * @return true if estimation is possible. False if data are not suited for estimation.
     */
    public boolean estimateParameters(int globalMaxIter, double globalConv, int personMaxIter, double personConv, boolean centerItems){
        double delta = 1.0+globalConv;
        double itemDelta = 0.0;
        double personDelta = 0.0;
        int iter = 0;

        //PROX starting values
        theta = new double[nPeople];
        thetaProx();

        while(delta > globalConv && iter < globalMaxIter){

            //update items and thresholds
            itemDelta = updateAllItems(delta, centerItems);

            //accept proposal values
            for(int j=0;j<nItems;j++){
                irm[j].acceptAllProposalValues();
            }
            if(maxCategory>2){
                for(String s:rsg.keySet()){
                    rsg.get(s).acceptAllProposalValues();
                }
            }

            //update persons
            personDelta = updateAllPersons(personMaxIter, personConv, adjustment, centerItems);

            //get max change in logits
            delta = Math.max(itemDelta, personDelta);
            iterationHistory.add(new IterationRecord(delta, getLogLikelihood()));
            iter++;
        }

        return true;

    }

    /**
     * This method manages the item updates during the JMLE iterations.
     *
     * @param delta current value of the maximum observed change in logits
     * @param centerItems establish identification by centering item about the item difficulty mean (the approach
     *                    typically used in Rasch measurement). If false establish identification by centering
     *                    persons around the mean ability. Centering only done for nonextreme persons and items.
     * @return maximum change in logits observed during the item updates
     */
    private double updateAllItems(double delta, boolean centerItems){
        double maxDelta = 0.0;
        double difficulty = 0.0;
        double tempDifficulty = 0.0;
        Mean mean = new Mean();

        boolean hasFixed = false;

        //update each non extreme item
        for(int j=0;j<nItems;j++){
            if(irm[j].isFixed()){
                hasFixed=true;
            }else{
                if(droppedStatus[j]==0){
                    tempDifficulty = updateDifficulty(irm[j], itemSummary[j], delta);
                    if(extremeItem[j]==0) mean.increment(tempDifficulty);
                }
            }

        }

        //Center non extreme items about the mean item difficulty.
        //Accept all proposal values.
        for(int j=0;j<nItems;j++){
            if(hasFixed){
                //with fixed values, there is no need to constrain item difficulty to be zero.
                if(!irm[j].isFixed()){
                    maxDelta = Math.max(maxDelta, Math.abs(irm[j].getProposalDifficulty()-irm[j].getDifficulty()));
                }
            }else{
                //without fixed item parameter, constrain item difficulty to be zero.
                if(droppedStatus[j]==0 && extremeItem[j]==0){
                    difficulty = irm[j].getDifficulty();
                    tempDifficulty = irm[j].getProposalDifficulty();
                    if(centerItems) tempDifficulty -= mean.getResult();//center
                    irm[j].setProposalDifficulty(tempDifficulty);
                    maxDelta = Math.max(maxDelta, Math.abs(tempDifficulty-difficulty));
                }
            }
        }

        //update thresholds
        if(maxCategory>2){
            //update thresholds
            for(String s:rsg.keySet()){
                if(!rsg.get(s).isFixed() && !rsg.get(s).isPoissoncounts()){
                    maxDelta = Math.max(maxDelta, updateThresholds(rsg.get(s)));
                }
            }
        }

        return maxDelta;
    }

    /**
     * Update of an individual item. The update only involves nonextreme examinees that responded to the item.
     * Only call this method for items with free parameters to be estimated.
     *
     * @param irm an item response model with free parameters to be estimated.
     * @param isum an item response summary object with item frequencies.
     * @param delta current value of the observed maximum change in logits.
     * @return
     */
    private double updateDifficulty(ItemResponseModel irm, ItemResponseSummary isum, double delta){
        double iTCC1 = 0.0;
        double iTCC2 = 0.0;
        double proposalDifficulty = 0.0;
        double difficulty = irm.getDifficulty();
        int pos = isum.getPositionInArray();

        for(int i=0;i<nPeople;i++){
            //update items using nonextreme people who completed hte item
            if(extremePerson[i]==0 && data[i][pos]!=-1){
                iTCC1+=irm.expectedValue(theta[i]);
                irm.setDifficulty(difficulty+delta);
                iTCC2+=irm.expectedValue(theta[i]);
                irm.setDifficulty(difficulty);
            }
        }
        proposalDifficulty = curveFit(difficulty, isum.Sip(), delta, iTCC1, iTCC2, isum.minSip(), isum.maxSip());
        irm.setProposalDifficulty(proposalDifficulty);
        return proposalDifficulty;
    }

//    /**
//     * All thresholds updated here.
//     *
//     * @return largest change in threshold estimate.
//     */
//    private double updateAllThresholds(){
//        double tDelta = 0.0;
//        double maxDelta = 0.0;
//        RaschRatingScaleGroup raschRatingScaleGroup = null;
//        for(String s:rsg.keySet()){
//            raschRatingScaleGroup = rsg.get(s);
//            if(raschRatingScaleGroup.dropStatus()==0){
//                tDelta = updateThresholds(raschRatingScaleGroup);
//                maxDelta = Math.max(maxDelta, tDelta);
//                raschRatingScaleGroup.acceptAllProposalValues();
//            }
//        }
//        return maxDelta;
//    }

    /**
     * Thresholds for a single rating scale group are updated in this method. Updates only involve nonextreme
     * examinees that respond to the item.
     *
     * @param raschRatingScaleGroup group for which thresholds are updated.
     * @return maximum change in logits for this update.
     */
    private double updateThresholds(RaschRatingScaleGroup raschRatingScaleGroup){
        double thresh = 0.0;
        int nCat = raschRatingScaleGroup.getNumberOfCategories();
        double[] catKSum = new double[nCat];
        double[] thresholds = null;
        double[] proposalThresholds = new double[nCat-1];
        Mean tMean = new Mean();
        double maxDelta = 0.0;
        thresholds = raschRatingScaleGroup.getThresholds();

        for(int i=0;i<nPeople;i++){
            if(extremePerson[i]==0){
                thresholds = raschRatingScaleGroup.getThresholds();
                for(int k=0;k<nCat;k++){
                    catKSum[k] += raschRatingScaleGroup.probabilitySumAt(theta[i], k);
                }
            }
        }

        int prevCat = 0;
        int nextCat = 0;
        for(int k=0;k<nCat-1;k++){
            nextCat++;
            thresh = thresholds[k];
            proposalThresholds[k] = thresh - Math.log(raschRatingScaleGroup.TpjAt(nextCat)/ raschRatingScaleGroup.TpjAt(prevCat)) +
                    Math.log(catKSum[nextCat]/catKSum[prevCat]);
            //do not change threshold by more than one logit - from WINSTEPS documentation
            proposalThresholds[k] = Math.max(Math.min(thresh+1.0,proposalThresholds[k]),thresh-1.0);
            tMean.increment(proposalThresholds[k]);
            prevCat = nextCat;
        }

        //recenter thresholds around the mean threshold
        double m = tMean.getResult();
        for(int k=0;k<nCat-1;k++){
            proposalThresholds[k] = proposalThresholds[k] - m;
            maxDelta = Math.max(Math.abs(proposalThresholds[k]-thresholds[k]), maxDelta);
        }
        raschRatingScaleGroup.setProposalThresholds(proposalThresholds);

        return maxDelta;
    }

    /**
     * Update of all persons is handled here.
     *
     * @param maxIter maximum number of iteration in the person update.
     * @param converge convergence criterion for the person update. The criterion is the maximum change in logits.
     * @param adjustment extreme score adjustment.
     * @param centerItems establish identification by centering item about the item difficulty mean (the approach
     *                    typically used in Rasch measurement). If false establish identification by centering
     *                    persons around the mean ability. Centering only done for nonextreme persons and items.
     * @return maximum observed value change in logits from updating all examinees.
     */
    private double updateAllPersons(int maxIter, double converge, double adjustment, boolean centerItems){
        double maxDelta = 0.0;
        double tempTheta = 0.0;
        Mean personMean = new Mean();
        for(int i=0;i<nPeople;i++){
            tempTheta = theta[i];
            theta[i] = updatePerson(i, maxIter, converge, adjustment);
            if(extremePerson[i]==0){
                personMean.increment(theta[i]);
                maxDelta = Math.max(Math.abs(theta[i]-tempTheta), maxDelta);
            }
        }

        if(!centerItems){
            double m = personMean.getResult();
            for(int i=0;i<nPeople;i++){
                if(extremePerson[i]==0) theta[i] -= m;
            }
        }

        return maxDelta;
    }

    /**
     * An individual proficiency estimate is updated here.
     *
     * @param index array index of the person for which the update is computed.
     * @param maxIter maximum number of iteration in the person update.
     * @param converge convergence criterion for the person update. The criterion is the maximum change in logits.
     * @param adjustment extreme score adjustment.
     * @return newly updated theta value for computation of the maximum change in logits in updateAllPersons().
     */
    private double updatePerson(int index, int maxIter, double converge, double adjustment){
        int iter = 0;
        double delta = 1.0+converge;
        double previousTheta = 0.0;
        double TCC1 = 0.0; //this is the TCC at current theta rho
        double TCC2 = 0.0; //this is the TCC at current theta rho + d
        double shift = 0.0;

        //adjust extreme person scores
        double adjustedSumScore = sumScore[index];
        if(extremePerson[index]==-1){
            adjustedSumScore += adjustment;
        }else if(extremePerson[index]==1){
            adjustedSumScore -= adjustment;
        }

        while(delta > converge && iter < maxIter){
            previousTheta = theta[index];
            TCC1 = 0.0;
            TCC2 = 0.0;

            for(int j=0;j<nItems;j++){
                if(droppedStatus[j]==0){
                    if(extremeItem[j]==0 && data[index][j]!=-1){
                        TCC1 += irm[j].expectedValue(theta[index]);
                        shift = theta[index]+delta;
                        TCC2 += irm[j].expectedValue(shift);
                    }
                }

            }

            theta[index] = curveFit(theta[index], adjustedSumScore, delta, TCC1, TCC2, minPossibleTestScore[index], maxPossibleTestScore[index]);
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
     * PROX start values for examinees.
     */
    private void thetaProx(){
        double aRS = 0.0;
        double thetaProx = 0.0;
        for(int i=0;i<nPeople;i++){
            aRS = sumScore[i];
            if(extremePerson[i]==-1){
                aRS += adjustment;
            }else if(extremePerson[i]==1){
                aRS -=adjustment;
            }
            thetaProx = Math.log(aRS/(maxPossibleTestScore[i]-aRS));
            theta[i] = thetaProx;
        }

    }

    /**
     * Computes PROX difficulty estimates for item difficulty. These are used as starting values in JMLE.
     */
    public void itemProx(){
        for(int j=0;j<nItems;j++){
            if(droppedStatus[j]==0 && !irm[j].isFixed()){
                double maxItemScore = itemSummary[j].maxSip();
                double adjustedScore = itemSummary[j].Sip();
                double p = adjustedScore/maxItemScore;
                double q = 1.0-p;
                double prox = Math.log(q/p);
                irm[j].setDifficulty(prox);
                irm[j].setProposalDifficulty(prox);

                int ncat = irm[j].getNcat();

                //threshold prox values
                if(ncat>2){
                    double previous = 0.0;
                    double current = 0.0;
                    double[] threshold = new double[ncat-1];
                    RaschRatingScaleGroup group = rsg.get(irm[j].getGroupId());

                    Mean tMean = new Mean();
                    for(int k=0;k<ncat;k++){
                        current = group.SpjAt(k);
                        if(k>0){
                            threshold[k-1] = Math.log(previous/current);
                            tMean.increment(threshold[k-1]);
                        }
                        previous=current;
                    }

                    for(int k=0;k<ncat-1;k++){
                        threshold[k] -= tMean.getResult();
                    }

                    irm[j].setThresholdParameters(threshold);
                    irm[j].setProposalThresholds(threshold);
                }

            }

        }

    }

    /**
     * Compute log-likelihood given the current values of item and person parameters.
     *
     * @return log-likelihood
     */
    public double getLogLikelihood(){
        double sum = 0.0;
        for(int i=0;i<nPeople;i++){
                for(int j=0;j<nItems;j++){
                    if(droppedStatus[j]==0){
                        if(data[i][j]>-1){
                            sum += Math.log(irm[j].probability(theta[i], data[i][j]));
                        }
                    }

                }
        }
        return sum;
    }

    /**
     * Item response model objects are stored in an array. Gets the array of item response models.
     *
     * @return an array of item response models.
     */
    public ItemResponseModel[] getItems(){
        return irm;
    }

    /**
     * Person ability estimates are stored as an array of doubles. Get the array of person estimates.
     *
     * @return an array of person estimates.
     */
    public double[] getPersons(){
        return theta;
    }

    /**
     * Standard errors of person ability estimates are stored as an array of doubles. Gets the array of standard errors.
     * Will return null if {@link #computePersonStandardErrors} has not been called.
     *
     * @return
     */
    public double[] getPersonStdError(){
        return thetaStdError;
    }

    /**
     * Item difficulty standard error calculation. Standard errors for threshold parameters are also computed
     * for polytomous items.
     */
    public void computeItemStandardErrors(){
        double tempStdError = 0.0;
        for(int i=0;i<nPeople;i++){
            if(extremePerson[i]==0){
                for(int j=0;j<nItems;j++){
                    if(droppedStatus[j]==0){
                        if(data[i][j]!=-1){
                            tempStdError = irm[j].getDifficultyStdError();
                            tempStdError+=irm[j].itemInformationAt(theta[i]);
                            irm[j].setDifficultyStdError(tempStdError);
                        }
                    }
                }
            }
        }
        for(int j=0;j<nItems;j++){
            if(droppedStatus[j]==0){
                tempStdError = irm[j].getDifficultyStdError();
                irm[j].setDifficultyStdError(1.0 / Math.sqrt(tempStdError));
            }
        }
        if(maxCategory>2) computeAllThresholdStandardErrors();
    }

    /**
     * Item threshold standard error calculation.
     */
    private void computeAllThresholdStandardErrors(){
        RaschRatingScaleGroup gr = null;
        for(String s:rsg.keySet()){
            gr = rsg.get(s);
            if(gr.dropStatus()==0) gr.computeCategoryStandardError(theta, extremePerson);
        }
    }

    /**
     * Person ability parameter standard error calculation. Must be called before {@link #getPersonStdError}.
     */
    public void computePersonStandardErrors(){
        thetaStdError = new double[nPeople];
        double info = 0.0;
        for(int i=0;i<nPeople;i++){
            info = 0.0;
            for(int j=0;j<nItems;j++){
                if(data[i][j]!=-1 && extremeItem[j]==0 && droppedStatus[j]==0){
                    info += irm[j].itemInformationAt(theta[i]);
                }
            }
            thetaStdError[i] = 1.0/Math.sqrt(info);
        }
    }

//    public double[] getPersonStandardErrors(){
//        if(thetaStdError==null) computePersonStandardErrors();
//        return thetaStdError;
//    }

    /**
     * An extreme iem is one in which all examinees with obtain the lowest possible item score or one in which
     * all examinee obtain the highest possible item score. Gets the count of extreme items.
     *
     * @return count of extreme items.
     */
    public int numberOfNonexremeItems(){
        int L = 0;
        for(int j=0;j<extremeItem.length;j++){
            if(extremeItem[j]==0 && droppedStatus[j]==0) L++;
        }
        return L;
    }

    /**
     * An extreme person is one that obtains the minimum possible test score or the maximum possible test score.
     * Gets the count of extreme persons.
     *
     * @return count of extreme persons.
     */
    public int numberOfNonextremePeople(){
        int N = 0;
        for(int i=0;i<extremePerson.length;i++){
            if(extremePerson[i]==0) N++;
        }
        return N;
    }

    /**
     * Correct item difficulty and person ability estimates for bias. This is like using
     * the STBIAS=Y option in WINSTEPS.
     */
    public void biasCorrection(){
        double L = (double)numberOfNonexremeItems();
        double N = (double)numberOfNonextremePeople();
        double M = Math.min(2,maxCategory);
        double temp = 0.0;
        double itemAdjustment = ((M-1)*(L-1))/((M-1)*(L-1)+1.0);
        double personAdjustment = ((M-1)*(N-1))/((M-1)*(N-1)+1.0);

        //apply to items
        for(ItemResponseModel m:irm){
            temp = m.getDifficulty();
            temp *= itemAdjustment;
            m.setDifficulty(temp);
        }

        for(int i=0;i<nPeople;i++){
            theta[i] = theta[i]*personAdjustment;
        }
    }

    /**
     * This method is for displaying frequency tables for each item. It is mainly used for
     * binary items and the frequencies involved in calculation of item difficulty.
     *
     * @return
     */
    public String printFrequencyTables(){
        String s = "";
        for(int j=0;j<nItems;j++){
            s += itemSummary[j].toString();
        }
        return s;
    }

    /**
     * This method is for displaying frequency tables for polytomous items. It will show combined
     * frequencies for items in the same rating scale group. These frequencies are mainly used
     * for estimation of threshold parameters.
     *
     * @return
     */
    public String printRatingScaleTables(){
        String out = "";
        for(String s:rsg.keySet()){
            out += rsg.get(s).toString();
        }
        return out;
    }

    /**
     * Person ability estimates are stored in an array of doubles. Gets the entire arrya of person ability
     * estimates.
     *
     * @return person ability estimates.
     */
    public double[] getPersonEstimates(){
        return theta;
    }

    /**
     * Apply linear transformation to person and item parameter estimates. The transformation is
     * Xnew = X(scale) + intercept.
     *
     * @param intercept intercept transformation coefficient.
     * @param scale scale transformation coefficient.
     */
    public void linearTransformation(double intercept, double scale){

        for(ItemResponseModel m:irm){
            m.scale(intercept, scale);
        }

        for(int i=0;i<nPeople;i++){
            theta[i] = scale*theta[i]+intercept;
            thetaStdError[i] = thetaStdError[i]*scale;
        }
    }

    /**
     * After parameters have been estimated, fit statistics can be computed. Fit statistics include
     * INFIT and OUTFIT mean squared error statistic and their standardized versions. See
     * {@link RaschFitStatistics}. This method computes fit statistics for all items.
     */
    public void computeItemFitStatistics(){
        itemFit = new RaschFitStatistics[nItems];

        for(int j=0;j<nItems;j++){
            itemFit[j] = new RaschFitStatistics();
        }

        for(int i=0;i<nPeople;i++) {
            if (extremePerson[i] == 0) {
                for (int j = 0; j < nItems; j++) {
                    if (data[i][j] > -1 && droppedStatus[j] == 0) {
                        itemFit[j].increment(irm[j], theta[i], data[i][j]);
                    }
                }
            }
        }

    }

    /**
     * Conducts a principal components analysis of standardized residuals. Extreme items and dropped items are
     * excluded from the correlaiton matrix. Extreme persons are not included in the computation of the correlation.
     * Missing data are scored as zero points in computation of the correlation. These conventions are consistent
     * with WINSTEPS.
     *
     * @param nFactors number of factors retained in the principal components analysis.
     * @return an ExploratoryFactorAnalysis object.
     */
    public ExploratoryFactorAnalysis getPrincipalComponentsForStandardizedResiduals(int nFactors){
        double residual1 = 0;
        double residual2 = 0;
        int validIndex1 = 0;
        int validIndex2 = 0;

        int validItemCount = 0;
        for(int j=0;j<nItems;j++){
            if(extremeItem[j]==0 && droppedStatus[j]==0) validItemCount++;
        }

        StreamingCovarianceMatrix covMatrix = new StreamingCovarianceMatrix(validItemCount, true);

        for(int i=0;i<nPeople;i++) {
            if(extremePerson[i]==0){
                validIndex1=0;
                for (int j = 0; j < nItems; j++) {


                    if(extremeItem[j]==0 && droppedStatus[j]==0){

                        if (data[i][j] > -1){
                            residual1 = data[i][j] - irm[j].expectedValue(theta[i]);
                            residual1 /= Math.sqrt(irm[j].itemInformationAt(theta[i]));
                        }else{
                            //Missing data scored as zero points. Same thing is done in WINSTEPS.
                            residual1 = 0;
                        }

                        validIndex2 = validIndex1;
                        for(int k=j;k<nItems;k++){
                            if(extremeItem[k]==0 && droppedStatus[k] == 0){
                                if (data[i][k] > -1){
                                    residual2 = data[i][k] - irm[k].expectedValue(theta[i]);
                                    residual2 /= Math.sqrt(irm[k].itemInformationAt(theta[i]));
                                }else{
                                    //Missing data scored as zero points. Same thing is done in WINSTEPS.
                                    residual2 = 0;
                                }

                                covMatrix.increment(validIndex1, validIndex2, residual1, residual2);

                                validIndex2++;
                            }

                        }//end inner loop over items

                        validIndex1++;

                    }



                }//end outer loop over items
            }

        }

        Array2DRowRealMatrix R = new Array2DRowRealMatrix(covMatrix.correlation());
        ExploratoryFactorAnalysis efa = new ExploratoryFactorAnalysis(R, nFactors);

        //set variable names
        validIndex1 = 0;
        for(int j=0;j<nItems;j++){
            if(extremeItem[j]==0 && droppedStatus[j] == 0){
                efa.setVariableNameAt(validIndex1, irm[j].getName());
                validIndex1++;
            }
        }

        efa.estimateParameters(EstimationMethod.PRINCOMP);






        return efa;

    }

    /**
     * Computes person fit statistics for the examinee at the specified index. This method computes person
     * fit statistic for a single examinee.
     *
     * @param index array position of examinee for which the fit statistic is computed.
     * @return
     */
    public RaschFitStatistics getPersonFitStatisticsAt(int index){
        RaschFitStatistics fit = new RaschFitStatistics();
        for(int j=0;j<nItems;j++){
            if(extremeItem[j]==0){
                if(data[index][j]>-1 && droppedStatus[j]==0){
                    fit.increment(irm[j], theta[index], data[index][j]);
                }
            }
        }
        return fit;
    }

    /**
     * After estimation, INFIT and OUTFIT mean square fit statistics for category thresholds can be computed.
     * This method computes category fit statistics for all items and categories.
     */
    public void computeItemCategoryFitStatistics(){
        if(maxCategory > 2){
            RaschRatingScaleGroup g = null;
            for(int i=0;i<nPeople;i++){
                if(extremePerson[i]==0){
                    for(int j=0;j<nItems;j++){
                        if(data[i][j]>-1 && droppedStatus[j]==0){
                            g = rsg.get(irm[j].getGroupId());
                            if(g!=null) g.incrementFitStatistics(irm[j], theta[i], data[i][j]);
                        }
                    }
                }
            }
        }

    }


    /**
     * After estimation, overall indices of scale quality can be computed such as reliability and separation.
     * This method returns the scale quality statistics for the entire set of items.
     *
     * @return scale quality statistics for items.
     */
    public RaschScaleQualityStatistics getItemSideScaleQuality(){
        RaschScaleQualityStatistics iStats = new RaschScaleQualityStatistics();
        for(int j=0;j<nItems;j++){
            if(extremeItem[j]==0 && droppedStatus[j]==0){
                iStats.increment(irm[j].getDifficulty(), irm[j].getDifficultyStdError());
            }
        }
        return iStats;
    }

    /**
     * After estimation, overall indices of scale quality can be computed such as reliability and separation.
     * This method returns the scale quality statistics for the entire group of examinees.
     *
     * @return
     */
    public RaschScaleQualityStatistics getPersonSideScaleQuality(){
        RaschScaleQualityStatistics pStats = new RaschScaleQualityStatistics();
        for(int i=0;i<nPeople;i++){
            if(extremePerson[i]==0) pStats.increment(theta[i], thetaStdError[i]);
        }
        return pStats;
    }

    /**
     * Each item can have a different number of score categories. Gets the maximum score category.
     *
     * @return maximum score category among all items.
     */
    public int getMaxCategory(){
        return maxCategory;
    }

    /**
     * The total number of items on the test is stored. It includes all good items and all extreme items.
     *
     * @return total number of items.
     */
    public int getNumberOfItems(){
        return nItems;
    }

    /**
     * The total number of examinees is stored. It includes all extreme and nonextreme examinees.
     *
     * @return total number of examinees.
     */
    public int getNumberOfPeople(){
        return nPeople;
    }

    /**
     * The number of items (nonextreme and extreme) completed by each examinee
     *
     * @return an array of item counts.
     */
    public int getNumberOfCompletedItemsAt(int index){
        return numberOfCompletedItems[index];
    }

    /**
     * An {@link ItemResponseModel} object is stored in an array for each item. Gets the item response model
     * at the given index.
     *
     * @param index array position of the item response model.
     * @return item response model at the given index.
     */
    public ItemResponseModel getItemResponseModelAt(int index){
        return irm[index];
    }

    /**
     * Fit statistics for each item are stored in an array. Gets the fit statistics for the item in the array
     * at position given by the index.
     *
     * @param index array position of the item fit statistics.
     * @return fit statistics for an item.
     */
    public RaschFitStatistics getItemFitStatisticsAt(int index){
        return itemFit[index];
    }

    /**
     * Polytomous response models be long to a single {@link RaschRatingScaleGroup}. Gets the rating scale
     * group for item at the indexed position in the array.
     *
     * @param index array position of the item.
     * @return rating scale group for the item.
     */
    public RaschRatingScaleGroup getRatingScaleGroupAt(int index){
        String groupId = irm[index].getGroupId();
        RaschRatingScaleGroup group = rsg.get(groupId);
        return group;
    }

    /**
     * Extreme items are items in whih all examinees obtain the maximum possitble item score or the minimum possible
     * item score. An array stores flag as to whether items are extreme. Gets the indicator of the item's extreme
     * status.
     *
     * @param index position of item in the array.
     * @return extreme status for the item.
     */
    public int getExtremeItemAt(int index){
        return extremeItem[index];
    }

    /**
     * Person ability estimates are stored in an array. Gets the estimate of ability for the person at the position
     * given by the index.
     *
     * @param index array position of the examinee.
     * @return current ability estimate for an examinee.
     */
    public double getPersonEstimateAt(int index){
        return theta[index];
    }

    /**
     * Person estimate standard errors are stored in an array of doubles. Gets the standard error at the given position
     * of the array. The method {@link #computePersonStandardErrors} must be called before this method. Otherwise, it
     * will result in a null pointer exception.
     *
     * @param index position in array.
     * @return person estimate standard error.
     */
    public double getPersonEstimateStdErrorAt(int index){
        return thetaStdError[index];
    }

    /**
     * The valid sum score is the sum score based on the nonextreme items. Valid sum scores are stored as an array
     * of doubles. Gets the valid sum score at the given position in the array.
     *
     * @param index position of valid sum score in the array.
     * @return valid sum score.
     */
    public double getValidSumScoreAt(int index){
        return sumScore[index];
    }

    public double getSumScoreAt(int index){
        double sum = 0.0;
        for(int j=0;j<nItems;j++){
            if(data[index][j]>-1) sum += data[index][j];
        }
        return sum;
    }

    /**
     * An extreme person is an examinee that obtains the minimum or maximum possible test score. Ability estimates
     * are not possible with extreme examinees without an adjustment. Extreme persons are flagged in a integer array
     * such that a code of 0 is a nonextreme examinee, -1 is an extreme minimum, and +1 is an extreme maximum.
     *
     * @param index position in array.
     * @return extreme person code.
     */
    public int getExtremePersonAt(int index){
        return extremePerson[index];
    }

    /**
     * A residual is the difference between the observed and expected item response. Computes and returns the
     * residual for person i to item j.
     *
     * @param i index of examinee in data array.
     * @param j index of item in array of item response models.
     * @return residual for person i to item j.
     */
    public double getResidualAt(int i, int j){
        if(data[i][j]==-1) return Double.NaN;
        double p = irm[j].expectedValue(theta[i]);
        double x = data[i][j];
        return x-p;
    }

    public byte getResponseAt(int i, int j){
        return data[i][j];
    }

    /**
     * A polytomous item with at least one category with 0 observations is dropped from the analysis. The dropped
     * status of each item is stored in an integer array. The dropped status is coded such that a value of 0 indicates
     * the item is not dropped, a value of -1 indicates a category with no observations, and a +1 indicates a category
     * with all of the observations (hence one or more other categories have 0 observations).
     *
     * @param index array index of item.
     * @return dropped status.
     */
    public int getDroppedStatusAt(int index){
        return droppedStatus[index];
    }

    /**
     * This method is for displaying person parameter estimates, standard errors, and other information.
     * It will display statistic for every examinee.
     * @return
     */
    public String printPersonStats(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%12s", "Sum Score"); f.format("%12s", "Theta"); f.format("%12s", "Std. Error"); f.format("%12s", "Extreme"); f.format("%n");
        f.format("%50s", "--------------------------------------------------");f.format("%n");
        for(int i=0;i<nPeople;i++){
            f.format("%12.2f", sumScore[i]);
            f.format("%12.2f", theta[i]);
            f.format("%12.2f", thetaStdError[i]);
            if(extremePerson[i]==-1){
                f.format("%12s", "MINIMUM");
            }else if(extremePerson[i]==1){
                f.format("%12s", "MAXIMUM");
            }else{
                f.format("%12s", "");
            }
            f.format("%n");
        }
        f.format("%50s", "--------------------------------------------------");f.format("%n");
        return f.toString();
    }


    public String printBasicItemStats(){
        return printItemStats("");
    }

    /**
     * This method is for displaying item parameter estimates, standard errors, and other information.
     *
     * @return
     */
    public String printBasicItemStats(String title){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        int maxCat = 0;
        for(int j=0;j<nItems;j++){
            maxCat = Math.max(irm[j].getNcat(), maxCat);
        }

        f.format("%40s", title); f.format("%n");

        String line1 = "========================================================";
        int lineLength = 44;
        if(maxCat>2){
            for(int k=0;k<maxCat-1;k++){
                line1+="============";
                lineLength+=12;
            }
        }
        f.format("%"+lineLength+"s", line1);f.format("%n");

        f.format("%20s", "Name"); f.format("%12s", "Difficulty"); f.format("%12s", "Std. Error");
        if(maxCat>2){
            for(int k=0;k<maxCat-1;k++){
                f.format("%12s", "step"+(k+1));
            }
        }

        f.format("%12s", "Extreme"); f.format("%n");

        String line = "--------------------------------------------------------";
        lineLength = 44;
        if(maxCat>2){
            for(int k=0;k<maxCat-1;k++){
                line+="------------";
                lineLength+=12;
            }
        }

        f.format("%"+lineLength+"s", line);f.format("%n");
        for(int j=0;j<nItems;j++){
            f.format("%20s", irm[j].getName());

            if(droppedStatus[j]==0){
                f.format("%12.2f", irm[j].getDifficulty());
                f.format("%12.2f", irm[j].getDifficultyStdError());
                if(irm[j].getNcat()>2){
                    double[] t = irm[j].getThresholdParameters();
                    for(int k=0;k<t.length;k++){
                        f.format("%12.2f", t[k]);
                    }
                }

                if(itemSummary[j].isExtremeMaximum()){
                    f.format("%12s", "MAXIMUM");
                }else if(itemSummary[j].isExtremeMinimum()){
                    f.format("%12s", "MINIMUM");
                }else{
                    f.format("%12s", "");
                }
                f.format("%n");

                if(irm[j].getNcat()>2){
                    String id = irm[j].getGroupId();
                    double[] tse = rsg.get(id).getThresholdStandardError();
                    f.format("%44s", "");
                    for(int k=0;k<tse.length;k++){
                        f.format("%12.2f", tse[k]);
                    }
                    f.format("%n");
                }
            }else{
                f.format("%12s", "DROPPED");f.format("%n");
            }



        }
        f.format("%"+lineLength+"s", line1);f.format("%n");
        return f.toString();
    }

    /**
     * Joint maximum likelihood estimation is iterative. An iteration history is saved. The history includes
     * the largest change in logits and the log-likelihood at each iteration. This method provides a string
     * representation of the iteration history.
     *
     * @return a summary of the iteration history.
     */
    public String printIterationHistory(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        int index = 0;

        f.format("%n");
        f.format("%-20s", "ITERATION HISTORY");f.format("%n");
        f.format("%62s", "=============================================================="); f.format("%n");
        f.format("%10s", "Iteration");  f.format("%5s","");  f.format("%17s", "Delta");  f.format("%5s",""); f.format("%20s", "Log-likelihood"); f.format("%n");
        f.format("%62s", "--------------------------------------------------------------"); f.format("%n");
        for(IterationRecord ir:iterationHistory){
            f.format("%10s", ++index);
            f.format("%5s","");
            f.format("%42s", ir.toString());
            f.format("%n");
        }
        f.format("%62s", "=============================================================="); f.format("%n");

        return f.toString();
    }

    /**
     * The sum score is a sufficient statistic for the latent trait in the Rasch family of models. As such,
     * it is easy to create a table that shows the 1:1 correspondence between the sum score and person ability.
     * A score table is created showing this correspondence. It requires its own set of iterations, which is why
     * some parameters concern the iteration.
     *
     * @param maxIter maximum number of iterations to use when estimating ability.
     * @param converge convergence criterion when estimating ability.
     * @param adjustment extreme score adjustment that is applied to extreme persons.
     * @param transformation linear transformation to be applied to person ability estimates.
     * @param precision number of decimal places to use when reporting estimates int eh score table.
     * @return a score table that ranges from the minimum possible score to the maximum possible score.
     */
    public String printScoreTable(int maxIter, double converge, double adjustment,
                                  DefaultLinearTransformation transformation, int precision){
        RaschScoreTable table = new RaschScoreTable(irm, extremeItem, droppedStatus, precision);
        table.updateScoreTable(maxIter, converge, adjustment);
        table.computePersonStandardErrors();
        table.linearTransformation(transformation, precision);//must come after computation of standard errors
        scoreConversionTable = table.getOutputArray();
        return table.printScoreTable();
    }

    public Object[][] getScoreconversionTableForOutputter(){
        return scoreConversionTable;
    }

    /**
     * Creates a string of item statistics. It includes (a) item name, (b) item difficulty, (c) difficulty
     * standard error, (d) four item fit statistics, and (e) a extreme item flag. This method for displaying
     * statistics is the one used in jMetrik for displaying results.
     *
     * @param title a title for the output table.
     * @return a table of item statistics.
     */
    public String printItemStats(String title){
        TextTableColumnFormat[] cformats = new TextTableColumnFormat[8];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setStringFormat(10, TextTableColumnFormat.OutputAlignment.LEFT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setDoubleFormat(10, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[2] = new TextTableColumnFormat();
        cformats[2].setDoubleFormat(10, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[3] = new TextTableColumnFormat();
        cformats[3].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[4] = new TextTableColumnFormat();
        cformats[4].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[5] = new TextTableColumnFormat();
        cformats[5].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[6] = new TextTableColumnFormat();
        cformats[6].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[7] = new TextTableColumnFormat();
        cformats[7].setStringFormat(8, TextTableColumnFormat.OutputAlignment.LEFT);

        TextTable textTable = new TextTable();
        textTable.addAllColumnFormats(cformats, nItems+5);
        textTable.getRowAt(0).addHeader(0, 7, title, TextTablePosition.CENTER);
        textTable.getRowAt(1).addHorizontalRule(0, 7, "=");
        textTable.getRowAt(2).addHeader(0, 1, "Item", TextTablePosition.LEFT);
        textTable.getRowAt(2).addHeader(1, 1, "Difficulty", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(2, 1, "Std. Error", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(3, 1, "INFIT", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(4, 1, "Z-INFIT", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(5, 1, "OUTFIT", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(6, 1, "Z-OUTFIT", TextTablePosition.RIGHT);
        textTable.getRowAt(3).addHorizontalRule(0, 7, "-");

        int index = 4;
        RaschRatingScaleGroup group = null;
        for(int j=0;j<nItems;j++){
            group = rsg.get(irm[j].getGroupId());
            textTable.getRowAt(index).addStringAt(0, irm[j].getName().toString());
            if(group!=null && group.dropStatus()!=0){
                textTable.getRowAt(index).addHeader(1, 6, "DROPPED", TextTablePosition.CENTER);
            }else{
                textTable.getRowAt(index).addDoubleAt(1, irm[j].getDifficulty());
                textTable.getRowAt(index).addDoubleAt(2, irm[j].getDifficultyStdError());
                if(itemFit!=null){
                    textTable.getRowAt(index).addDoubleAt(3, itemFit[j].getWeightedMeanSquare());
                    textTable.getRowAt(index).addDoubleAt(4, itemFit[j].getStandardizedWeightedMeanSquare());
                    textTable.getRowAt(index).addDoubleAt(5, itemFit[j].getUnweightedMeanSquare());
                    textTable.getRowAt(index).addDoubleAt(6, itemFit[j].getStandardizedUnweightedMeanSquare());
                }else{
                    textTable.getRowAt(index).addDoubleAt(3, Double.NaN);
                    textTable.getRowAt(index).addDoubleAt(4, Double.NaN);
                    textTable.getRowAt(index).addDoubleAt(5, Double.NaN);
                    textTable.getRowAt(index).addDoubleAt(6, Double.NaN);
                }

                if(extremeItem[j]==-1){
                    textTable.getRowAt(index).addStringAt(7, "Minimum");
                }else if(extremeItem[j]==1){
                    textTable.getRowAt(index).addStringAt(7, "Maximum");
                }

            }
            index++;
        }
        textTable.getRowAt(nItems+4).addHorizontalRule(0, 7, "=");

        String output = textTable.toString();
        if(droppedCount==1){
            output += "Item dropped due to unobserved categories. Collapse categories to retain item.";
        }else if(droppedCount>1){
            output += "Multiple items dropped due to unobserved categories. Collapse categories to retain items.";
        }

        return output;
    }

    /**
     * Polytomous items have a set of threshold statistics that must be reported. They are listed in a separate
     * output table as defined by this method.
     *
     * @return a table of category statistics.
     */
    public String printCategoryStats(){
        if(rsg.isEmpty()) return "";
        TextTableColumnFormat[] cformats = new TextTableColumnFormat[6];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setStringFormat(10, TextTableColumnFormat.OutputAlignment.LEFT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setIntFormat(10, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[2] = new TextTableColumnFormat();
        cformats[2].setDoubleFormat(10, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[3] = new TextTableColumnFormat();
        cformats[3].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[4] = new TextTableColumnFormat();
        cformats[4].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[5] = new TextTableColumnFormat();
        cformats[5].setDoubleFormat(8, 2, TextTableColumnFormat.OutputAlignment.RIGHT);

        int numRows = 0;
        for(String s : rsg.keySet()){
            numRows += rsg.get(s).getNumberOfCategories() + 1;
        }

        TextTable textTable = new TextTable();
        textTable.addAllColumnFormats(cformats, numRows + 5);
        textTable.getRowAt(0).addHeader(0, 8, "FINAL JMLE CATEGORY STATISTICS", TextTablePosition.CENTER);
        textTable.getRowAt(1).addHorizontalRule(0, 8, "=");
        textTable.getRowAt(2).addHeader(0, 1, "Group", TextTablePosition.LEFT);
        textTable.getRowAt(2).addHeader(1, 1, "Category", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(2, 1, "Threshold", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(3, 1, "Std. Error", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(4, 1, "WMS", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(5, 1, "UMS", TextTablePosition.RIGHT);
        textTable.getRowAt(3).addHorizontalRule(0, 6, "-");

        int index = 4;
        RaschRatingScaleGroup group = null;
        RaschCategoryFitStatistic catFit = null;
        double[] thresholds = null;
        double[] thresholdsStdError = null;
        for(String s : rsg.keySet()){
            group = rsg.get(s);
            thresholds = group.getThresholds();
            thresholdsStdError = group.getThresholdStandardError();
            for(int i=0;i<group.getNumberOfCategories();i++){
                if(i==0){
                    textTable.getRowAt(index).addStringAt(0, s);
                    textTable.getRowAt(index).addIntAt(1, i);
                    if(group.dropStatus()!=0){
                        textTable.getRowAt(index).addHeader(2, 4, "DROPPED", TextTablePosition.LEFT);
                    }else{
                        textTable.getRowAt(index).addHeader(2, 4, " ", TextTablePosition.LEFT);
                    }
                }else{
                    textTable.getRowAt(index).addStringAt(0, " ");
                    textTable.getRowAt(index).addIntAt(1, i);
                    if(group.dropStatus()!=0){
                        textTable.getRowAt(index).addHeader(2, 1, " ", TextTablePosition.LEFT);
                        textTable.getRowAt(index).addHeader(3, 1, " ", TextTablePosition.LEFT);
                        textTable.getRowAt(index).addHeader(4, 1, " ", TextTablePosition.LEFT);
                        textTable.getRowAt(index).addHeader(5, 1, " ", TextTablePosition.LEFT);
                    }else{
                        textTable.getRowAt(index).addDoubleAt(2, thresholds[i-1]);
                        textTable.getRowAt(index).addDoubleAt(3, thresholdsStdError[i-1]);

                        catFit = group.getCategoryFitAt(i-1);
                        if(null==catFit){
                            textTable.getRowAt(index).addStringAt(4, " ");
                            textTable.getRowAt(index).addStringAt(5, " ");
                        }else{
                            textTable.getRowAt(index).addDoubleAt(4, catFit.getWeightedMeanSquare());
                            textTable.getRowAt(index).addDoubleAt(5, catFit.getUnweightedMeanSquare());
                        }


                    }
                }
                index++;
            }

            textTable.getRowAt(index).addHeader(0, 6, " ", TextTablePosition.LEFT);//add empty row
            index++;
        }
        textTable.getRowAt(numRows+3).addHorizontalRule(0, 6, "=");
        return textTable.toString();
    }

//    public RaschDimensionalityEvaluation getRaschDimensionalityEvaluation(){
//        return dimensionalityEvaluation;
//    }

//    public String printDimensionalityResults(){
//        StringBuilder sb = new StringBuilder();
//
//        ExploratoryFactorAnalysis efa = dimensionalityEvaluation.principalComponentsAnalysis();
//
//        sb.append(dimensionalityEvaluation.printEigenValues());
//        sb.append("\n\n");
//        sb.append(efa.printOutput());
//        return sb.toString();
//
//
////        Formatter f = new Formatter(sb);
////
////        double totalVariance = 0;
////        double[] eigenValues = dimensionalityEvaluation.eigenValues();
////        for(int j=0;j<eigenValues.length;j++){
////            totalVariance += eigenValues[j];
////        }
////
////        f.format("%-50s", "STANDARDIZED RESIDUAL EIGEN VALUES"); f.format("%n");
////        f.format("%45s", "=================================================="); f.format("%n");
////        f.format("%10s", "Component"); f.format("%5s", "");
////        f.format("%11s", "Eigen Value"); f.format("%5s", "");
////        f.format("%18s", "Explained Variance");  f.format("%n");
////        f.format("%50s", "--------------------------------------------------"); f.format("%n");
////
////        for(int j=0;j<eigenValues.length;j++){
////            f.format("%10d", (j+1)); f.format("%5s", "");
////            f.format("%10.4f", eigenValues[j]);f.format("%5s", "");
////            f.format("%16.4f%%", eigenValues[j]/totalVariance*100);f.format("%n");
////        }
////        f.format("%50s", "=================================================="); f.format("%n");
////
////        return f.toString();
//    }

}

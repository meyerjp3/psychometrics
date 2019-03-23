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
import com.itemanalysis.psychometrics.histogram.*;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.utils.StopWatch;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.concurrent.ForkJoinPool;

/**
 * Marginal maximum likelihood estimation (MMLE) for item parameters in Item Response Theory (IRT).
 * Estimation is accomplished using teh EM algorithm. Computation is done in parallel to substantially
 * reduce the amount of time needed for the EM algorithm.
 *
 * Bayes modal estimates of item parameters are computed when a prior quadrature has been included
 * in an item response model. The priors maye be specified for any or all item parameters. Any item
 * can use priors. As such, you can have some items with one or more priors and other items without any priors.
 *
 */
public class MarginalMaximumLikelihoodEstimation {

    private ItemResponseModel[] irm = null;
    private ItemResponseVector[] responseVector = null;
    private ItemFitStatistic[] itemFit = null;
    private boolean g2ItemFit = true;
    private int nItems = 0;
    private int nResponseVectors = 0;
    private int nPoints = 0;
    private EstepEstimates estepEstimates = null;
    private QuadratureRule latentDistribution = null;
    private static int PROCESSORS =  Runtime.getRuntime().availableProcessors();
    private ArrayList<EMStatusListener> emStatusListeners = new ArrayList<EMStatusListener>();
    private ForkJoinPool pool = null;
    private boolean verbose = false;
    private DensityEstimationType densityEstimationType = DensityEstimationType.FIXED_NO_ESTIMATION;
    private IrtObservedScoreDistribution mainIrtObservedScoreDistribution = null;

    /**
     * Stores counts of the number of error codes encountered during he Mstep.
     * codeCount[0] = number of bad termination codes from UNCMIN
     * codeCount[1] = number of times a negative discrimination parameter occurred
     * codeCount[2] = number of times a negative guessing parameter occured
     * codeCount[3] = number of times a slipping parameter was greater than 1.
     *
     * The values should be 0 <= codeCount[i] <= nItems. Any nonzero value should be a concern.
     *
     *
     */
    private int[] codeCount = null;

    public MarginalMaximumLikelihoodEstimation(ItemResponseVector[] responseVector, ItemResponseModel[] irm, QuadratureRule latentDistribution){
        this.responseVector = responseVector;
        this.irm = irm;
        this.latentDistribution = latentDistribution;
        nPoints = latentDistribution.getNumberOfPoints();
        nItems = irm.length;
        nResponseVectors = responseVector.length;
        pool = new ForkJoinPool(PROCESSORS);
    }

    /**
     * Estep computation is done with parallel processing to speed the computation time. The threshold for
     * the parallel computation is set in {@link EstepParallel}.
     * At the completion of this method, the expected count (nk) and expected number of correct answers (rjk)
     * has been computed and they are contained in {@link #estepEstimates}. These values are used in teh Mstep
     * to update the item parameter estimates.
     *
     */
    private void doEStep(){
        EstepParallel estepParallel = new EstepParallel(responseVector, irm, latentDistribution, 0, responseVector.length);
        estepEstimates = pool.invoke(estepParallel);
    }

    /**
     * Mstep computation is done in parallel. However, doing this step in paralle does not seem to make much
     * of a difference. The greatest increase in performance is the parallel computation of the Estep. At the
     * end of this method item parameter estimates for all items are updated to their latest value.
     * @return
     */
    private double doMStep(){

        //Get latent mean and SD if estimating these values. Needed for computing max change (see below).
        double oldMean = 0;
        double oldSD = 1;
        if(DensityEstimationType.FIXED_NO_ESTIMATION!=densityEstimationType){
            oldMean = latentDistribution.getMean();
            oldSD = latentDistribution.getStandardDeviation();
        }


        MstepParallel mstepParallel = new MstepParallel(irm, latentDistribution, estepEstimates, densityEstimationType, 0, irm.length);

        //start parallel processing
        pool.invoke(mstepParallel);

        //Count error codes
        codeCount = new int[4];
        int[] tc = mstepParallel.getCodeCount();
        for(int i=0;i<4;i++){
            codeCount[i]+=tc[i];
        }

        //pass optimizer status to a log or something
//        fireEMStatusEvent(uncminStatusListener.toString());

        //update the item parameter estimates
        double maxChange = 0.0;
        for(int j=0;j<nItems;j++){
            maxChange = Math.max(maxChange, irm[j].acceptAllProposalValues());
        }

        return maxChange;

    }

    /**
     * Estimate parameters using the specified convergence criterion and maximum number of iterations. If using this call
     * the latent quadrature is not estimated.
     *
     * @param converge convergence criterion should be a positivie number close to zero such as 1e-3. If it is a negative number algorithm will run to maximum number of iterations.
     * @param maxIter maximum number of iterations. Algorithm will stop if the maximum is reached evne if convergence criterion is not satistfied.
     */
    public void estimateParameters(double converge, int maxIter){
        estimateParameters(converge, maxIter, densityEstimationType);
    }

    /**
     * The EM algorithm for estimating item parameters is conducted with this method. You can display
     * the iteration number (iter), current maximum change in item parameter estimates (delta), and
     * the value of the loglikelihood from each cycle by using
     * {@link DefaultEMStatusListener}. You can also write
     * a custom DefaultEMStatusListener to write this information to a log file.
     *
     * @param converge maximum change in parameter estimate convergence criterion.
     * @param maxIter maximum number of EM cycles.
     * @param densityEstimationType method to estimate (or not estimate) latent quadrature
     */
    public void estimateParameters(double converge, int maxIter, DensityEstimationType densityEstimationType){
        this.densityEstimationType = densityEstimationType;

        fireEMStatusEvent("STARTING EM CYCLES...");
        fireEMStatusEvent("Number of available processors = " + PROCESSORS);


        StopWatch stopWatch = new StopWatch();
        double delta = 1.0+converge;
        int iter = 0;

        //For debugging
//        if(verbose){
//            doEStep();
//            fireEMStatusEvent(iter, delta, completeDataLogLikelihood());
//        }

        while(delta > converge && iter < maxIter){
            doEStep();
            delta = doMStep();
            iter++;

            //Format and send EM cycle summary to EMStatusListeners
           if(verbose)  fireEMStatusEvent(iter, delta, completeDataLogLikelihood(), codeToString());
        }

        if(!verbose) fireEMStatusEvent(iter, delta, completeDataLogLikelihood(), codeToString());
        fireEMStatusEvent("Elapsed time: " + stopWatch.getElapsedTime());
        if(delta>converge) fireEMStatusEvent("WARNING: convergence criterion not met. Increase the maximum number of iterations.");

    }

    public int getNumberOfItems(){
        return nItems;
    }

    public int getNumberOfPeople(){
        int N = 0;
        for(int i=0;i<responseVector.length;i++){
            N+= responseVector[i].getFrequency();
        }
        return N;
    }

    public ItemResponseModel getItemResponseModelAt(int j){
        return irm[j];
    }

    public QuadratureRule getLatentDistribution(){
        return latentDistribution;
    }

//    /**
//     * Computes the residual (observed item response minus expected item response).
//     *
//     * @param i person index
//     * @param j item index
//     * @return residual value
//     */
//    public double getResidualAt(int i, int j){
//        if(theta==null) estimatePersonAbility(PersonScoringType.EAP, -6.0, 6.0, 0.0, 1.0, 60);
//        double r = responseVector[i].getResponseAt(j) - irm[j].expectedValue(theta[i]);
//        return r;
//    }
//
//    /**
//     * Estimates person ability and the corresponding standard error of the estimate.
//     *
//     * @param scoreType type of person score
//     * @param min minimum possible score value
//     * @param max maximum possible score value
//     * @param mean mean of normal quadrature (for MAP and EAP)
//     * @param sd standard deviation of normal quadrature (for MAP and EAP)
//     * @param nPoints number of quadrature points (for EAP estimation)
//     */
//    public void estimatePersonAbility(PersonScoringType scoreType, double min, double max, double mean, double sd, int nPoints){
//        IrtExaminee irtExaminee = new IrtExaminee("", irm);
//        theta = new double[responseVector.length];
//        thetaStdError = new double[responseVector.length];
//
//        if(PersonScoringType.MLE==scoreType){
//            for(int i=0;i<responseVector.length;i++){
//                irtExaminee.setResponseVector(responseVector[i]);
//                theta[i] = irtExaminee.maximumLikelihoodEstimate(min, max);
//                thetaStdError[i] = irtExaminee.mleStandardErrorAt(theta[i]);
//            }
//        }else if(PersonScoringType.MAP==scoreType){
//            for(int i=0;i<responseVector.length;i++){
//                irtExaminee.setResponseVector(responseVector[i]);
//                theta[i] = irtExaminee.mapEstimate(mean, sd, min, max);
//                thetaStdError[i] = irtExaminee.mapStandardErrorAt(theta[i]);
//            }
//        }else{
//            for(int i=0;i<responseVector.length;i++){
//                irtExaminee.setResponseVector(responseVector[i]);
//                theta[i] = irtExaminee.eapEstimate(mean, sd, min, max, nPoints);
//                thetaStdError[i] = irtExaminee.eapStandardErrorAt(theta[i]);
//            }
//        }
//    }
//
//    /**
//     * Uses default values of number of points, mean, and sd
//     *
//     * @param scoreType type of person score
//     * @param min minimum possible score value
//     * @param max maximum possible score value
//     */
//    public void estimatePersonAbility(PersonScoringType scoreType, double min, double max){
//        estimatePersonAbility(scoreType, min, max, 0.0, 1.0, 60);
//    }
//
//    /**
//     * Uses default values of number of points, mean, and sd
//     *
//     * @param scoreType type of person score
//     * @param min minimum possible score value
//     * @param max maximum possible score value
//     * @param mean mean of normal quadrature
//     * @param sd standard deviation of normal quadrature
//     */
//    public void estimatePersonAbility(PersonScoringType scoreType, double min, double max, double mean, double sd){
//        estimatePersonAbility(scoreType, min, max, mean, sd, 60);
//    }
//
//    public double getPersonScoreAt(int index){
//        return theta[index];
//    }
//
//    public double getPersonScoreStandardErrorAt(int index){
//        return thetaStdError[index];
//    }

    private String codeToString(){
        String s = "[";
        for(int i=0;i<4;i++){
            s+= codeCount[i];
            if(i<3) s += " ";
        }
        s += "]";
        return s;
    }

    /**
     * Computes the complete data log-likelihood. The kernel of the log-likelihood is computed incrementally
     * in the Estep. This method incorporates the prior probabilities of item parameters into the log-
     * likelihood function.
     *
     * @return log-likelihood value
     */
    public double completeDataLogLikelihood(){
        double logLike = estepEstimates.getLoglikelihood();
        for(int j=0;j<nItems;j++){
            irm[j].addPriorsToLogLikelihood(logLike, irm[j].getItemParameterArray());
        }
        return logLike;
    }

    public void computeItemStandardErrors(){
        ItemLogLikelihood itemLogLikelihood = new ItemLogLikelihood();
        for(int j=0;j<irm.length;j++){
            itemLogLikelihood.setModel(irm[j], latentDistribution, estepEstimates.getRjkAt(j), estepEstimates.getNt());
            itemLogLikelihood.stdError(irm[j]);
        }
    }

    public IrtObservedScoreDistribution getIrtObservedScoreDistribution(){
        return mainIrtObservedScoreDistribution;
    }

    public void computeSX2ItemFit(int minExpectedCount){
        g2ItemFit = false;
        itemFit = new ItemFitSX2[nItems];

        IrtObservedScoreCollection irtObservedScoreCollection = new IrtObservedScoreCollection(irm, latentDistribution);
        mainIrtObservedScoreDistribution = irtObservedScoreCollection.getIrtObservedScoreDistribution();
        int maxTestScore = irtObservedScoreCollection.getMaxPossibleTestScore();

        int summedScore = 0;
        double theta = 0;
        for(int i=0;i<responseVector.length;i++){
            summedScore = (int)responseVector[i].getSumScore();

            for(int j=0;j<nItems;j++){
                if(i==0) itemFit[j] = new ItemFitSX2(
                        mainIrtObservedScoreDistribution,
                        irtObservedScoreCollection.getObservedScoreDistributionAt(j),
                        irm[j], maxTestScore+1, minExpectedCount);
                ((ItemFitSX2)itemFit[j]).increment(summedScore, responseVector[i].getResponseAt(j), responseVector[i].getFrequency());
            }
        }

        //Compute Item Fit
        for(int j=0;j<nItems;j++){
            itemFit[j].compute();
            irm[j].setItemFitStatistic(itemFit[j]);
        }

    }

    public ItemFitStatistic[] getFitStatistics(){
        return itemFit;
    }

    /**
     *
     */
    public void computeG2ItemFit(int nbins, int minExpectedCount){
        IrtExaminee irtExaminee = new IrtExaminee("", irm);
        double[] eapEstimate = new double[responseVector.length];
        SummaryStatistics stats = new SummaryStatistics();

        //First loop over response vectors
        for(int i=0;i<responseVector.length;i++){
            //EAP estimate of ability
            irtExaminee.setResponseVector(responseVector[i]);
            eapEstimate[i] = irtExaminee.eapEstimate(latentDistribution);

            for(int N=0;N<responseVector[i].getFrequency();N++){//Expand response vectors
                stats.addValue(eapEstimate[i]);
            }
        }

        DefaultLinearTransformation linearTransformation = new DefaultLinearTransformation(
                stats.getMean(),
                latentDistribution.getMean(),
                stats.getStandardDeviation(),
                latentDistribution.getStandardDeviation());

        //Create fit statistic objects
        itemFit = new ItemFitG2[nItems];

        double lower = linearTransformation.transform(stats.getMin())-.01;//Subtract a small number to ensure lowest theta is counted
        double upper = linearTransformation.transform(stats.getMax())+.01;//Add a small number to ensure largest theta is counted
        Cut thetaCut = new Cut(lower, upper, nbins);

        for(int j=0;j<nItems;j++){
            itemFit[j] = new ItemFitG2(irm[j], thetaCut, minExpectedCount);
            irm[j].setItemFitStatistic(itemFit[j]);
        }

        //Second loop over response vectors
        //Increment fit statistics
        double A = linearTransformation.getScale();
        for(int i=0;i<responseVector.length;i++){
            //Estimate EAP standard deviation should be the same as the standard deviation
            //of the latent quadrature used to estimate the item parameters.
            eapEstimate[i] = eapEstimate[i]*A;

            for(int N=0;N<responseVector[i].getFrequency();N++) {//Expand table
                for(int j=0;j<nItems;j++){
                    ((ItemFitG2)itemFit[j]).increment(eapEstimate[i], responseVector[i].getResponseAt(j));
                }
            }
        }

//        System.out.println(thetaCut.toString());

        //Compute Item Fit
        for(int j=0;j<nItems;j++){
            itemFit[j].compute();
        }

    }

    public void computeRaschItemFit(){
        RaschFitStatistics[] raschFit = new RaschFitStatistics[nItems];
        IrtExaminee irtExaminee = new IrtExaminee(irm);
        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();

        double theta = 0;
        for(int i=0;i<responseVector.length;i++){
            for(int j=0;j<nItems;j++){
                if(i==0) raschFit[j] = new RaschFitStatistics();
                theta = irtObservedScoreDistribution.getEAP((int)responseVector[i].getSumScore());
                raschFit[j].increment(irm[j], theta, responseVector[i].getResponseAt(j));
            }
        }

        for(int j=0;j<nItems;j++){
            System.out.println(raschFit[j].getWeightedMeanSquare() + "  " + raschFit[j].getUnweightedMeanSquare());
        }

    }

    public String printItemParameters(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%58s", "MMLE ITEM PARAMETER ESTIMATES");f.format("%n");
        f.format("%87s", "======================================================================================="); f.format("%n");
        f.format("%-18s", "Item");
        f.format("%5s", "Code");
        f.format("%9s", "Apar");
        f.format("%7s", "(SE)");
        f.format("%9s", "Bpar"); f.format("%1s", "");
        f.format("%6s", "(SE)");
        f.format("%9s", "Cpar"); f.format("%1s", "");
        f.format("%6s", "(SE)");
        f.format("%9s", "Upar"); f.format("%1s", "");
        f.format("%6s", "(SE)"); f.format("%n");
        f.format("%87s", "---------------------------------------------------------------------------------------"); f.format("%n");

        for(int j=0;j<nItems;j++){
           sb.append(irm[j].toString() + "\n");
        }

        f.format("%87s", "======================================================================================="); f.format("%n");

        return f.toString();
    }

    public String printLatentDistribution(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%30s", "     LATENT DISTRIBUTION      ");f.format("%n");
        f.format("%30s", "==============================");
        f.format("%n");
        f.format("%10s", "Point");f.format("%4s", "");f.format("%16s", "Density");f.format("%n");
        f.format("%30s", "------------------------------");f.format("%n");
        for(int k=0;k<latentDistribution.getNumberOfPoints();k++){
            f.format("% 10.8f", latentDistribution.getPointAt(k));
            f.format("%4s", "");
            f.format("% 10.8e", latentDistribution.getDensityAt(k));//15 wide
            f.format("%n");
        }
        f.format("%30s", "==============================");f.format("%n");
        f.format("%12s", "Mean = "); f.format("%8.4f", latentDistribution.getMean()); f.format("%n");
        f.format("%12s", "Std. Dev. = "); f.format("%8.4f", latentDistribution.getStandardDeviation()); f.format("%n");
        return f.toString();
    }

    public String printItemFitStatistics(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%34s", "ITEM FIT STATISTICS"); f.format("%n");
        f.format("%50s", "=================================================="); f.format("%n");
        f.format("%-18s", "Item"); f.format("%2s", "");
        if(g2ItemFit){
            f.format("%8s", "G2");
        }else{
            f.format("%8s", "S-X2");
        }
        f.format("%2s", "");
        f.format("%8s", "df");f.format("%2s", "");
        f.format("%8s", "p-value"); f.format("%n");
        f.format("%50s", "--------------------------------------------------"); f.format("%n");

        for(int j=0;j<nItems;j++){
            f.format("%-18s", irm[j].getName());f.format("%2s", "");
            f.format("%8.4f", itemFit[j].getValue());f.format("%2s", "");
            f.format("%8.4f", itemFit[j].getDegreesOfFreedom());f.format("%2s", "");
            f.format("%8.4f", itemFit[j].getPValue());f.format("%n");
        }

        f.format("%50s", "=================================================="); f.format("%n");

        return f.toString();
    }

    public void setVerbose(boolean verbose){
        this.verbose = verbose;
    }

//=====================================================================================================================
// METHOD FOR HANDLING THE EMSTATUS LISTENER
// These methods have nothnig to do with estimation. They only pertain to the publication of intermediate results.
//=====================================================================================================================

    public void addEMStatusListener(EMStatusListener listener){
        emStatusListeners.add(listener);
    }

    public void removeEMStatusListener(EMStatusListener listener){
        emStatusListeners.remove(listener);
    }

    public void fireEMStatusEvent(String message){
        for(EMStatusListener l : emStatusListeners){
            l.handleEMStatusEvent(new EMStatusEventObject(this, message));
        }
    }

    public void fireEMStatusEvent(int iteration, double delta, double loglikelihood, String termCode){
        for(EMStatusListener l : emStatusListeners){
            l.handleEMStatusEvent(new EMStatusEventObject(this, iteration, delta, loglikelihood, termCode));
        }
    }



}

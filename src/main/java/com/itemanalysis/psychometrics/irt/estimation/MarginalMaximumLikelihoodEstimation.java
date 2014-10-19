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

import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.psychometrics.uncmin.DefaultUncminStatusListener;
import org.apache.commons.math3.stat.Frequency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;

/**
 * Marginal maximum likelihood estimation (MMLE) for item parameters in Item Response Theory (IRT).
 * Estimation is accomplished using teh EM algorithm. Computation is done in parallel to substantially
 * reduce the amount of time needed for the EM algorithm.
 *
 * Bayes modal estimates of item parameters are computed when a prior distribution has been included
 * in an item response model. The priors maye be specified for any or all item parameters. Any item
 * can use priors. As such, you can have some items with one or more priors and other items without any priors.
 *
 */
public class MarginalMaximumLikelihoodEstimation {

    private ItemResponseModel[] irm = null;
    private ItemResponseVector[] responseVector = null;
    private int nItems = 0;
    private int nResponseVectors = 0;
    private int nPoints = 0;
    private EstepEstimates estepEstimates = null;
    private DistributionApproximation latentDistribution = null;
    private static int PROCESSORS =  Runtime.getRuntime().availableProcessors();
    private ArrayList<EMStatusListener> emStatusListeners = new ArrayList<EMStatusListener>();
    private ForkJoinPool pool = null;
    private boolean verbose = false;
    private boolean estimateLatentDistribution = false;

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

    public MarginalMaximumLikelihoodEstimation(ItemResponseVector[] responseVector, ItemResponseModel[] irm, DistributionApproximation latentDistribution){
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
     * the parallel computation is set in {@link com.itemanalysis.psychometrics.irt.estimation.EstepParallel}.
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

        MstepParallel mstepParallel = new MstepParallel(irm, latentDistribution, estepEstimates, 0, irm.length);

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

        //TODO activate this option when fully tested
        //estimate latent distribution here
//        if(estimateLatentDistribution){
//            latentDistribution = mstepParallel.updateLatentDistribution();
//        }

        return maxChange;

    }

    public void estimateParameters(double converge, int maxIter){
        estimateParameters(converge, maxIter, false);
    }

    /**
     * The EM algorithm for estimating item parameters is conducted with this method. You can display
     * the iteration number (iter), current maximum change in item parameter estimates (delta), and
     * the value of the loglikelihood from each cycle by using
     * {@link com.itemanalysis.psychometrics.irt.estimation.DefaultEMStatusListener}. You can also write
     * a custom DefaultEMStatusListener to write this information to a log file.
     *
     * @param converge maximum change in parameter estimate convergence criterion.
     * @param maxIter maximum number of EM cycles.
     */
    public void estimateParameters(double converge, int maxIter, boolean estimateLatentDistribution){
        this.estimateLatentDistribution = estimateLatentDistribution;

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

        f.format("%30s", "     Latent Distribution      ");f.format("%n");
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

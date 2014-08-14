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
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.concurrent.RecursiveTask;

/**
 * Estep of the EM algorithm for estimating item parameters in MMLE. The computation is done in parallel if the
 * number of response vectors exceeds {@link #PARALLEL_THRESHOLD}.
 */
public class EstepParallel extends RecursiveTask<EstepEstimates> {

    private int start = 0;
    private int length = 0;
    private int nItems = 0;
    private int nPoints = 0;
    private ItemResponseVector[] responseVector = null;
    private DistributionApproximation latentDistribution = null;
    private ItemResponseModel[] irm = null;
    private static int PARALLEL_THRESHOLD = 250;

    /**
     * Default constructoir may be called recursively for parallel compuations.
     *
     * @param responseVector response vectors for a given set of data
     * @param irm an array of item response models whose parameters are being estimated
     * @param latentDistribution latent distribution quadrature used for computing the marginal likelihood
     * @param start beginning index for the response vector. Manual calls should always be 0, recursive calls are done automatically.
     * @param length length of the response vector segment. This length is manually set to the length of teh response vector.
     *               Recursive calls use the value specified by {@link #PARALLEL_THRESHOLD}.
     */
    public EstepParallel(ItemResponseVector[] responseVector, ItemResponseModel[] irm, DistributionApproximation latentDistribution, int start, int length){
        this.responseVector = responseVector;
        this.irm = irm;
        this.latentDistribution = latentDistribution;
        this.nPoints = latentDistribution.getNumberOfPoints();
        this.nItems = irm.length;
        this.start = start;
        this.length = length;
    }

    /**
     * Estep computation when the number of response vectors is less than {@link #PARALLEL_THRESHOLD} or when
     * the recursive algorithm has reached its stopping condition. In parallel processing, this method represents
     * each chunk that is processed in parallel.
     *
     * @return
     */
    protected EstepEstimates computeDirectly(){
        double response = 0;
        double point = 0.0;
        double density = 0.0;
        double mlike = 0.0;
        double value = 0.0;
        double clike = 0.0;
        double freq = 0.0;
        EstepEstimates estepEstimates = new EstepEstimates(nItems, nPoints);

        for(int l=start;l<(start+length);l++){
            mlike = computeMarginalLikelihood(responseVector[l]);
            freq = responseVector[l].getFrequency();
            for(int k=0;k<nPoints;k++){
                point = latentDistribution.getPointAt(k);
                density = latentDistribution.getDensityAt(k);
                clike = conditionalLikelihood(point, responseVector[l]);

                //nk
                value = freq*clike*density/mlike;
                estepEstimates.incrementNk(k, value);

                //rjk
                for(int j=0;j<nItems;j++){
                    response = Byte.valueOf(responseVector[l].getResponseAt(j)).doubleValue();
                    value = response*freq*clike*density/mlike;
                    estepEstimates.incrementRjk(j, k, value);
                }
            }
            estepEstimates.incrementLoglikelihood(freq*Math.log(mlike));
        }
        return estepEstimates;
    }

    /**
     * Recusive computation of the Estep for parallel processing. It will aggregate all chunks that are processed
     * in parallel.
     *
     * @return values of nk, rjk, and the loglikelihood (without priors).
     */
    @Override
    protected EstepEstimates compute(){
        if(length<PARALLEL_THRESHOLD){
            return computeDirectly();
        }else{
            int split = length/2;
            EstepParallel estep1 = new EstepParallel(responseVector, irm, latentDistribution, start, split);
            EstepParallel estep2 = new EstepParallel(responseVector, irm, latentDistribution, start+split, length-split);
            estep1.fork();
            EstepEstimates sum2 = estep2.compute();
            EstepEstimates sum1 = estep1.join();
            return sum2.add(sum1);
        }
    }

    /**
     * Computes the conditional likelihood of a response vector at the point given by quadPoint.
     *
     * @param quadPoint a quadrature points (i.e. theta value)
     * @param responseVector a response vector
     */
    private double conditionalLikelihood(double quadPoint, ItemResponseVector responseVector){
        double value = 1.0;
        for(int j=0;j<nItems;j++){
            value *= irm[j].probability(quadPoint, responseVector.getResponseAt(j));
        }
        return value;
    }

    /**
     * Computes the marginal likelihood of a response vector.
     *
     */
    private double computeMarginalLikelihood(ItemResponseVector itemResponseVector){
        double mlike = 0.0;
        for(int k=0;k<nPoints;k++){
            mlike += conditionalLikelihood(latentDistribution.getPointAt(k), itemResponseVector)*latentDistribution.getDensityAt(k);
        }
        return mlike;
    }

}

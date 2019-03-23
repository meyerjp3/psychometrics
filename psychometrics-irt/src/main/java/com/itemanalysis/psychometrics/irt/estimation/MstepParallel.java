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
import com.itemanalysis.psychometrics.irt.model.IrmType;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.uncmin.DefaultUncminOptimizer;
import com.itemanalysis.psychometrics.uncmin.UncminException;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

/**
 * Mstep of the EM algorithm for estimating item parameters in Item Response Theory.
 * Computations can be done in parallel but performance gains appear to be minimal.
 */
public class MstepParallel extends RecursiveAction {

    private ItemResponseModel[] irm = null;
    private QuadratureRule latentDistribution = null;
    private EstepEstimates estepEstimates = null;
    private int start = 0;
    private int length = 0;
    private static int PARALLEL_THRESHOLD = 100;
    private DefaultUncminOptimizer optimizer = null;
    private int[] codeCount = new int[4];
    private DensityEstimationType densityEstimationType = DensityEstimationType.FIXED_NO_ESTIMATION;

//    private QNMinimizer qn = null;

    public MstepParallel(ItemResponseModel[] irm, QuadratureRule latentDistribution, EstepEstimates estepEstimates, DensityEstimationType densityEstimationType, int start, int length){
        this.irm = irm;
        this.latentDistribution = latentDistribution;
        this.estepEstimates = estepEstimates;
        this.densityEstimationType = densityEstimationType;
        this.start = start;
        this.length = length;
        optimizer = new DefaultUncminOptimizer(10);
//        qn = new QNMinimizer(15, true);
//        qn.setRobustOptions();
//        qn.shutUp();
    }

    /**
     * Mstep computation when the number of items is less than the threshold or when the
     * stopping condition has been reached. For each item, it uses the optimizer to obtain
     * the estimates that maximize the marginal likelihood.
     */
    protected void computeDirectly(){
        ItemLogLikelihood itemLogLikelihood = new ItemLogLikelihood();
        double[] initialValue = null;
        int nPar = 1;
        double[] param = null;

        for(int j=start;j<start+length;j++){
            nPar = irm[j].getNumberOfParameters();

            itemLogLikelihood.setModel(irm[j], latentDistribution, estepEstimates.getRjkAt(j), estepEstimates.getNt());
            initialValue = irm[j].getItemParameterArray();

            try{
                optimizer.minimize(itemLogLikelihood, initialValue, true, false, 500, 1);//Last paramter, 1, could also be 2
                param = optimizer.getParameters();

                if(optimizer.getTerminationCode()>3) codeCount[0]++;

            }catch(UncminException ex){
                codeCount[0]++;
                ex.printStackTrace();
            }

//            param = qn.minimize(itemLogLikelihood,1e-8,initialValue,500);

            if(irm[j].getType()==IrmType.L3 || irm[j].getType()==IrmType.L4){
                if(nPar==4){
                    if(param[0]<0) codeCount[1]++;
                    if(param[2]<0) codeCount[2]++;
                    if(param[3]>1) codeCount[3]++;
                    irm[j].setProposalDiscrimination(param[0]);
                    irm[j].setProposalDifficulty(param[1]);
                    irm[j].setProposalGuessing(Math.min(1.000, Math.max(param[2], 0.001)));//set negative estimates to just above zero
                    irm[j].setProposalSlipping(Math.max(0.60, Math.min(param[3], 0.999)));//set estimates above one to just below 1.
                }else if(nPar==3){
                    if(param[0]<0) codeCount[1]++;
                    if(param[2]<0) codeCount[2]++;
                    irm[j].setProposalDiscrimination(param[0]);
                    irm[j].setProposalDifficulty(param[1]);
                    irm[j].setProposalGuessing(Math.min(1.000, Math.max(param[2], 0.001)));//set negative estimates to just above zero
                }else if(nPar==2){
                    if(param[0]<0) codeCount[1]++;
                    irm[j].setProposalDiscrimination(param[0]);
                    irm[j].setProposalDifficulty(param[1]);
                }else{
                    irm[j].setProposalDifficulty(param[0]);
                }

            }else if(irm[j].getType()==IrmType.GPCM){
                irm[j].setProposalDiscrimination(param[0]);
                irm[j].setProposalStepParameters(Arrays.copyOfRange(param, 1, param.length));

            }else if(irm[j].getType()==IrmType.PCM2){
                irm[j].setProposalStepParameters(param);
            }

        }//end item loop

    }

    /**
     * Parallel processing handled here.
     */
    @Override
    protected void compute(){
        if(length<=PARALLEL_THRESHOLD){
            computeDirectly();
        }else{
            int split = length/2;
            MstepParallel mstep1 = new MstepParallel(irm, latentDistribution, estepEstimates, densityEstimationType, start, split);
            MstepParallel mstep2 = new MstepParallel(irm, latentDistribution, estepEstimates, densityEstimationType, start+split, length-split);
            invokeAll(mstep1, mstep2);
        }


        //Estimate latent density - It is done here to prevent problems with parallel processing. Also,
        //parallel processing would not accelerate this part because only iterates over quadrature points.
        if(DensityEstimationType.EMPIRICAL_HISTOGRAM_FREE==densityEstimationType ||
                DensityEstimationType.EMPIRICAL_HISTOGRAM_STANDARDIZED==densityEstimationType ||
                DensityEstimationType.EMPIRICAL_HISTOGRAM_STANDARDIZED_KEEP_POINTS==densityEstimationType){

            this.empiricalHistogramLatentDensityEstimation();
        }


    }

    /**
     * Nonparametric estimation (empirical histogram method) of the latent density.
     *
     * @return
     */
    public QuadratureRule empiricalHistogramLatentDensityEstimation(){
        double sumNk = estepEstimates.getSumNt();
        double[] nk = estepEstimates.getNt();
        double[] linTrans = null;

        //Update posterior probabilities
        //With no changes this results in DensityEstimationType.EMPIRICAL_HISTOGRAM_FREE
        for(int k=0;k<nk.length;k++){
            latentDistribution.setDensityAt(k, nk[k]/sumNk);
        }

        if(DensityEstimationType.EMPIRICAL_HISTOGRAM_STANDARDIZED_KEEP_POINTS==densityEstimationType){
            linTrans = latentDistribution.standardize(true);
        }else if(DensityEstimationType.EMPIRICAL_HISTOGRAM_STANDARDIZED==densityEstimationType){
            linTrans = latentDistribution.standardize(false);
        }

        return latentDistribution;
    }

    public int[] getCodeCount(){
        return codeCount;
    }


}

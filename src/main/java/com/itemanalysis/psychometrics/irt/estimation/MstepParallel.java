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
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmType;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.uncmin.DefaultUncminOptimizer;
import com.itemanalysis.psychometrics.uncmin.UncminException;
import com.itemanalysis.psychometrics.uncmin.UncminStatusListener;

import java.util.concurrent.RecursiveAction;

/**
 * Mstep of the EM algorithm for estimating item parameters in Item Response Theory.
 * Computations can be done in parallel but performance gains appear to be minimal.
 */
public class MstepParallel extends RecursiveAction {

    private ItemResponseModel[] irm = null;
    private DistributionApproximation latentDistribution = null;
    private EstepEstimates estepEstimates = null;
    private int start = 0;
    private int length = 0;
    private static int PARALLEL_THRESHOLD = 100;
    private DefaultUncminOptimizer optimizer = null;
    private UncminStatusListener uncminStatusListener = null;

//    private QNMinimizer qn;

    public MstepParallel(ItemResponseModel[] irm, DistributionApproximation latentDistribution, EstepEstimates estepEstimates, int start, int length){
        this.irm = irm;
        this.latentDistribution = latentDistribution;
        this.estepEstimates = estepEstimates;
        this.start = start;
        this.length = length;
        optimizer = new DefaultUncminOptimizer(10);
//        qn = new QNMinimizer(15, true);
//        qn.setRobustOptions();
//        qn.shutUp();
    }

    public void addUncminStatusListener(UncminStatusListener listener){
        this.uncminStatusListener = listener;
        optimizer.addUncminStatusListener(this.uncminStatusListener);
    }

    /**
     * Mstep computation when the number of items is less than the threshold or when the
     * stopping condition has been reached. For each item, it uses the optimizer to obtain
     * the estimates that maximize the marginal likelihood.
     */
    protected void computeDirectly()throws IllegalArgumentException{
        ItemDichotomous itemDichotomous = new ItemDichotomous();
        double[] initialValue = null;
        int nPar = 1;

        for(int j=start;j<start+length;j++){
            nPar = irm[j].getNumberOfParameters();

            if(irm[j].getType()==IrmType.L3){
                itemDichotomous.setModel((Irm3PL)irm[j], latentDistribution, estepEstimates.getRjkAt(j), estepEstimates.getNk());
                initialValue = new double[nPar];
                if(nPar==3){
                    initialValue[0] = irm[j].getDiscrimination();
                    initialValue[1] = irm[j].getDifficulty();
                    initialValue[2] = irm[j].getGuessing();
                }else if(nPar==2){
                    initialValue[0] = irm[j].getDiscrimination();
                    initialValue[1] = irm[j].getDifficulty();
                }else{
                    initialValue[0] = irm[j].getDifficulty();
                }

            }else{
                //add functionality for polytomous models
            }


            double[] param = null;

            try{

//            param = qn.minimize(itemDichotomous,1e-8,initialValue,500);
                optimizer.minimize(itemDichotomous, initialValue, true, false, 150);
                param = optimizer.getParameters();
//                double f = optimizer.getFunctionValue();
//                double fTemp = 0.0;
//
            }catch(UncminException ex){
                //TODO the exception will be lost in a non-console program because not stack trace will be shown.
                ex.printStackTrace();
            }

            if(irm[j].getType()==IrmType.L3){
                if(nPar==3){
                    irm[j].setProposalDiscrimination(param[0]);
                    irm[j].setProposalDifficulty(param[1]);
                    irm[j].setProposalGuessing(Math.min(1.000, Math.max(param[2], 0.05)));//set negative estimates to just above zero
                }else if(nPar==2){
                    irm[j].setProposalDiscrimination(param[0]);
                    irm[j].setProposalDifficulty(param[1]);
                }else{
                    irm[j].setProposalDifficulty(param[0]);
                }
            }

        }
    }

    /**
     * Parallel processing handled here.
     */
    @Override
    protected void compute(){
        if(length<=PARALLEL_THRESHOLD){
            computeDirectly();
            return;
        }else{
            int split = length/2;

            MstepParallel mstep1 = new MstepParallel(irm, latentDistribution, estepEstimates, start, split);
            mstep1.addUncminStatusListener(uncminStatusListener);
            MstepParallel mstep2 = new MstepParallel(irm, latentDistribution, estepEstimates, start+split, length-split);
            mstep2.addUncminStatusListener(uncminStatusListener);
//
            invokeAll(mstep1, mstep2);
        }
    }

    public DistributionApproximation getUpdatedLatentDistribution(){
        double sumNk = estepEstimates.getSumNk();
        double[] nk = estepEstimates.getNk();
        for(int k=0;k<nk.length;k++){
            latentDistribution.setDensityAt(k, nk[k]/sumNk);
        }
        return latentDistribution;
    }

}

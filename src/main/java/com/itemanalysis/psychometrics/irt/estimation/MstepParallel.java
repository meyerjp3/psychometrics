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
import com.itemanalysis.psychometrics.uncmin.Uncmin;
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
    private Uncmin optimizer = null;
    private UncminStatusListener uncminStatusListener = null;

    public MstepParallel(ItemResponseModel[] irm, DistributionApproximation latentDistribution, EstepEstimates estepEstimates, int start, int length){
        this.irm = irm;
        this.latentDistribution = latentDistribution;
        this.estepEstimates = estepEstimates;
        this.start = start;
        this.length = length;
        optimizer = new Uncmin(10);
//        QNMinimizer qn = new QNMinimizer(15, true);
//        qn.setRobustOptions();
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
    protected void computeDirectly(){
        ItemDichotomous itemDichotomous = new ItemDichotomous();
        double[] initialValue = null;
        int nPar = 1;

        for(int j=start;j<start+length;j++){
            nPar = irm[j].getNumberOfParameters();

            if(irm[j].getType()== IrmType.L3){
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

            double[] iv = new double[nPar+1];
            for(int i=0;i<nPar;i++){
                iv[i+1] = initialValue[i];
            }

            try{
                optimizer.minimize(itemDichotomous, initialValue, true, false, 150);
            }catch(UncminException ex){
                //TODO the exception will be lost in a non-console program because not stack trace will be shown.
                ex.printStackTrace();
            }

            double[] param = optimizer.getParameters();
//            double[] param = qn.minimize(itemDichotomous,1e-8,initialValue,500);

            if(irm[j].getType()==IrmType.L3){
                if(nPar==3){
                    irm[j].setProposalDiscrimination(param[0]);
                    irm[j].setProposalDifficulty(param[1]);

                    //only accept guessing parameter estimates between 0 and 1 inclusive.
                    irm[j].setProposalGuessing(Math.min(1.000, Math.max(param[2], 0.0)));
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
        if(length<= PARALLEL_THRESHOLD){
            computeDirectly();
            return;
        }else{
            int split = length/2;

            MstepParallel mstep1 = new MstepParallel(irm, latentDistribution, estepEstimates, start, split);
            mstep1.addUncminStatusListener(uncminStatusListener);
            MstepParallel mstep2 = new MstepParallel(irm, latentDistribution, estepEstimates, start+split, length-split);
            mstep2.addUncminStatusListener(uncminStatusListener);

            invokeAll(mstep1, mstep2);
        }
    }

}

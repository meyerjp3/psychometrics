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

/**
 * This class computes the IRT observed score quadrature for an entire test. It also creates
 * an IRT observed score quadrature for the entire test without a particular item. This class is
 * mainly used to create observed score distributions that are needed for IRT ItemFitStatistics.
 */
public class IrtObservedScoreCollection {

    private int nItems = 0;

    private int maxTestScore = 0;

    private ItemResponseModel[] irm = null;

    private QuadratureRule latentDistribution = null;

    private IrtObservedScoreDistribution irtObservedScoreDistribution = null;

    private IrtObservedScoreDistribution[] irtObservedScoreWithout = null;

    public IrtObservedScoreCollection(ItemResponseModel[] irm, QuadratureRule latentDistribution){
        this.irm = irm;
        this.latentDistribution = latentDistribution;
        this.nItems = irm.length;
        initialize();
    }

    private ItemResponseModel[] removeItemAt(int indexToRemove){
        ItemResponseModel[] irmSubset = new ItemResponseModel[nItems-1];
        int index = 0;

        for(int j=0;j<nItems;j++){
            if(indexToRemove!=j){
                irmSubset[index] = irm[j];
                index++;
            }
        }
        return irmSubset;
    }

    private void initialize(){
        //IRT observed score quadrature
        irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();

        //Observed score distributions without studied item (item at given index)
        irtObservedScoreWithout = new IrtObservedScoreDistribution[nItems];

        //Compute IRT observed score quadrature for each item by excluding studied item. (Could be done in parallel)
        for(int j=0;j<nItems;j++){
            irtObservedScoreWithout[j] = new IrtObservedScoreDistribution(removeItemAt(j), latentDistribution);
            irtObservedScoreWithout[j].compute();
            maxTestScore += irm[j].getMaxScoreWeight();
        }
    }

    public IrtObservedScoreDistribution getIrtObservedScoreDistribution(){
        return irtObservedScoreDistribution;
    }

    public IrtObservedScoreDistribution getObservedScoreDistributionAt(int index){
        return irtObservedScoreWithout[index];
    }

    public int getMaxPossibleTestScore(){
        return maxTestScore;
    }

}

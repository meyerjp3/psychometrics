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

/**
 * Storeless computation of Rasch category INFIT and OUTFIT statistics. Statistics are
 * incrementally updated with each observation and then computed before the result is returned.
 */
public class RaschCategoryFitStatistic {

    double k = 0.0;
    double sumKmEni = 0.0;
    double sumKmEniWni = 0.0;
    double sumPnikKmEni = 0.0;
    double sumPnikKmEniWni = 0.0;
    byte[] scoreWeight = null;
    int nCat = 2;

    public RaschCategoryFitStatistic(Integer k, byte[] scoreWeight){
        this.k = Integer.valueOf(k).doubleValue();
        this.scoreWeight = scoreWeight;
        this.nCat = scoreWeight.length;
    }

    public void increment(double Xni, double theta, ItemResponseModel model){
        double Eni = model.expectedValue(theta);
        double Wni = varianceOfResponse(model, theta);
        double Pnik = 0;

        double kMe2 = (k-Eni)*(k-Eni);
        if(Xni==k){
            sumKmEni += kMe2;//Ox
            sumKmEniWni += kMe2/Wni;
        }

        Pnik = model.probability(theta, (int)k);
        sumPnikKmEni += Pnik*kMe2;//Mx
        sumPnikKmEniWni += Pnik*kMe2/Wni;

    }

    /**
     * Computes and the returns the OUTFIT mean square fit statistic.
     *
     * @return OUTFIT mean squares fit statistic.
     */
    public double getUnweightedMeanSquare(){
        return sumKmEniWni/sumPnikKmEniWni;
    }

    /**
     * Computes and then returns the INFIT mean square fit statistic.
     *
     * @return INFIT mean square fit statistic.
     */
    public double getWeightedMeanSquare(){
        return sumKmEni/sumPnikKmEni;//Ox/Mx
    }

    /**
     * Variance of response Xni. This method is needed for computation of fit statistics.
     * @param theta examinee ability
     * @return
     */
    public double varianceOfResponse(ItemResponseModel irm, double theta){
        double Wni = 0.0;
        double Eni = irm.expectedValue(theta);
        for(int m=0;m<irm.getNcat();m++){
            Wni += Math.pow(m-Eni, 2)*irm.probability(theta, m);
        }
        return Wni;
    }

    /**
     * Kurtosis of response Xni.  This method is needed for computation of fit statistics.
     * @param theta
     * @return
     */
    public double kurtosisOfResponse(ItemResponseModel irm, double theta){
        double Wni = 0.0;
        double Eni = irm.expectedValue(theta);
        for(int m=0;m<irm.getNcat();m++){
            Wni += Math.pow(m-Eni, 4)*irm.probability(theta, m);
        }
        return Wni;
    }


}

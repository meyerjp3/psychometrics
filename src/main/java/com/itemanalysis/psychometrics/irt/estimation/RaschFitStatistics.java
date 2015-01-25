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
 * Storeless computation of Rasch item or person INFIT and OUTFIT statistics. Statistics are
 * incrementally updated with each observation and then computed before the result is returned.
 */
public class RaschFitStatistics {

    /**
     * Value in equation that accumulates by calling increment
     */
    public double Z2niSum = 0.0;

    /**
     * Value in equation that accumulates by calling increment
     */
    public double WniZ2niSum = 0.0;

    /**
     * Value in equation that accumulates by calling increment
     */
    public double WniSum = 0.0;

    /**
     * Value in equation that accumulates by calling increment
     */
    public double umsNumeratorSum = 0.0;

    /**
     * Value in equation that accumulates by calling increment
     */
    public double wmsNumeratorSum = 0.0;

    /**
     * Value in equation that accumulates by calling increment.
     * This rho is the same size or number of items.
     */
    double N = 0.0;

    public RaschFitStatistics(){

    }

    public void increment(ItemResponseModel irm, double theta, byte Xni){
        increment(Xni, irm.expectedValue(theta), varianceOfResponse(irm, theta), kurtosisOfResponse(irm, theta));
    }

    /**
     * Incrementally update item fit statistics.
     *
     * @param Xni score rho for person n to item i
     * @param Eni expected score for person n to item i. It is computed in RatingScaleModel.java
     * @param Wni Variance of Xni. It is computed in RatingScaleModel.java.
     * @param Cni Kurtosis of Xni. It is computed in RatingScaleModel.java.
     */
    private void increment(byte Xni, double Eni, double Wni, double Cni){
        double Yni = Xni-Eni;
        double Zni = Yni/Math.sqrt(Wni);
        double Z2ni = Zni*Zni;
        double W2ni = (Wni*Wni);
        Z2niSum += Z2ni;
        WniZ2niSum += Wni*Z2ni;
        WniSum += Wni;
        umsNumeratorSum += Cni/W2ni;
        wmsNumeratorSum +=Cni-W2ni;
        N++;
    }

    /**
     * Computes and return the OUTFIT mean square fit statistic.
     *
     * @return OUTFIT mean square fit statistic.
     */
    public double getUnweightedMeanSquare(){
        return Z2niSum/N;
    }

    /**
     * Computes and returns the INFIT mean square fit statistic.
     *
     * @return INFIT mean square fit statistic.
     */
    public double getWeightedMeanSquare(){
        return WniZ2niSum/WniSum;
    }

    /**
     * Standardized INFIT
     *
     * @return
     */
    public double getStandardizedWeightedMeanSquare(){
        double variance = wmsNumeratorSum/(WniSum*WniSum);
        double q = Math.sqrt(variance);
        //limiting case recommended by Mike Linacre via Phil Chalmers mirt R package
        q = Math.min(q, 1.4142);
        double vi = WniZ2niSum/WniSum;
        double t = (Math.pow(vi, 1.0/3.0)-1.0)*(3.0/q)+(q/3.0);
        return t;
    }

    /**
     * Standardized OUTFIT
     *
     * @return
     */
    public double getStandardizedUnweightedMeanSquare(){
        double variance = umsNumeratorSum/(N*N)-1.0/N;
        double q = Math.sqrt(variance);
        double vi = Z2niSum/N;

        //limiting case recommended by Mike Linacre via Phil Chalmers mirt R package
        q = Math.min(q, 1.4142);

        double t = (Math.pow(vi, 1.0/3.0)-1.0)*(3.0/q)+(q/3.0);
        return t;

    }

    /**
     * Variance of response Xni. This method is needed for computation of fit statistics.
     *
     * NOTE: This value is the same as item information. Perhaps only for Rasch model??
     *
     * @param theta examinee ability
     * @return
     */
    public double varianceOfResponse(ItemResponseModel irm, double theta){
        double Wni = 0.0;
        double Eni = irm.expectedValue(theta);
        for(int m=0;m<irm.getNcat();m++){
            Wni += Math.pow(m-Eni, 2)*irm.probability(theta, m);
        }

//        System.out.println("VAR: " + Wni + "  INFO: " + irm.itemInformationAt(theta));

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

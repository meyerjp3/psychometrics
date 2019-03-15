/*
 * Copyright 2012 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.polycor;


import com.itemanalysis.psychometrics.statistics.PearsonCorrelation;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 *
 * @author J. Patrick Meyer
 */
public class PolyserialLogLikelihoodTwoStep {

    private Mean meanX = null;

    private StandardDeviation sdX = null;

    /**
     * X is the continuous variable
     */
    private double[] dataX = null;

    /**
     * Y is the categorical variable where values are consecutive integers starting at 1
     */
    private int[] dataY = null;

    private double[] freqDataY = null;

    private PearsonCorrelation rxy = null;

    private NormalDistribution normal = null;

    private double[] alpha = null;

    private int nrow = 0;

    public PolyserialLogLikelihoodTwoStep(double[] dataX, int[] dataY){
        this.dataX = dataX;
        this.dataY = dataY;
        this.normal = new NormalDistribution();
        alpha = new double[nrow];
    }

    public void summarize()throws  DimensionMismatchException{
        if(dataX.length!=dataY.length) throw new DimensionMismatchException(dataX.length, dataY.length);
        Frequency table = new Frequency();
        meanX = new Mean();
        sdX = new StandardDeviation();
        rxy = new PearsonCorrelation();
        for(int i=0;i<nrow;i++){
            meanX.increment(dataX[i]);
            sdX.increment(dataX[i]);
            rxy.increment(dataX[i], (double)dataY[i]);
            table.addValue(dataY[i]);
        }

        //compute thresholds
        nrow = table.getUniqueCount();
        freqDataY = new double[nrow];
        double ntotal = table.getSumFreq();
        for(int i=0;i<(nrow-1);i++){
            freqDataY[i] = table.getCumFreq(i+1);
            alpha[i] = normal.inverseCumulativeProbability(freqDataY[i]/ntotal);
        }
        alpha[nrow-1] = 10;//set last threshold to a large number less than infinity
    }

    public double value(double x){
        double z = 0.0;
        double prbZ = 0.0;
        double loglik = 0.0;
        double tauStar = 0.0;
        double tauStarM1 = 0.0;
        double dif = 0.0;

        for(int i=0;i<dataX.length;i++){
            z = (dataX[i]-meanX.getResult())-sdX.getResult();
            prbZ = normal.density(z);
            tauStar = (alpha[dataY[i]-1] - x*z)/Math.sqrt(1 - x*x);
            if(dataY[i]>1){
                tauStarM1 = (alpha[dataY[i]-1] - x*z)/Math.sqrt(1 - x*x);
            }else{
                tauStarM1 = -10;//some large number greater than negative infinity
            }
            dif = normal.cumulativeProbability(tauStar) - normal.cumulativeProbability(tauStarM1);
            loglik += Math.log(prbZ*dif);
        }
        return -loglik;
    }

    public double[] getThresholds(){
        return alpha;
    }


}

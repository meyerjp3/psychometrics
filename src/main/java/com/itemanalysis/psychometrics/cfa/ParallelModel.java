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
package com.itemanalysis.psychometrics.cfa;

import com.itemanalysis.psychometrics.statistics.IdentityMatrix;
import com.itemanalysis.psychometrics.statistics.IdentityVector;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class ParallelModel extends AbstractConfirmatoryFactorAnalysisModel{

    RealMatrix SIGMA = null;

    RealMatrix B = null;

    RealMatrix Ivec = null;

    public ParallelModel(int nItems, double[] inits){
        super(nItems);
        this.setInitialFactorLoading(inits);
        Ivec = new IdentityVector(nItems);
    }

    public ParallelModel(int nItems){
        super(nItems);
        Ivec = new IdentityVector(nItems);
    }

        public String getName(){
        return "                  PARALLEL MODEL";
    }

    public int getNumberOfParameters(){
        return 2;
    }

    public void setInitialFactorLoading(double[] inits){
        double sum = 0.0;
        for(int i=0;i<inits.length;i++){
            sum+=inits[i];
        }
        for(int i=0;i<factorLoading.length;i++){
            factorLoading[i]=sum/inits.length;
        }
    }

    public double[] getInitialValuesVector(){
        double[] inits = new double[this.getNumberOfParameters()];
        inits[0] = factorLoading[0];
        inits[1] = errorVariance[0];
        return inits;
    }

    public void setParameters(double[] argument){
        for(int i=0;i<nItems;i++){
            factorLoading[i]=argument[0];
            errorVariance[i]=argument[1];
        }
    }

    public RealMatrix getImpliedCovariance(double[] argument){
        setParameters(argument);
        RealMatrix I = new IdentityMatrix(nItems);
        B = Ivec.scalarMultiply(argument[0]*argument[0]);
        RealMatrix THETA = I.scalarMultiply(argument[1]);
        SIGMA = B.multiply(Ivec.transpose()).add(THETA);
        return SIGMA;
    }

    public RealMatrix getBeta(double[] argument){
        setParameters(argument);
        B = Ivec.scalarMultiply(argument[0]);
        return B;
    }

    public void setGradient(RealMatrix factorLoadingFirstDerivative, RealMatrix errorVarianceFirstDerivative, double[] gradient){
        double sumL = 0.0, sumE = 0.0;

        for(int i=0;i<factorLoadingFirstDerivative.getRowDimension();i++){
            sumL += factorLoadingFirstDerivative.getEntry(i, 0);
        }
        gradient[0]=sumL;

        for(int i=0;i<errorVariance.length;i++){
            sumE += errorVarianceFirstDerivative.getEntry(i, i);
        }
        gradient[1] = sumE;
    }

    public double[] getGradient(RealMatrix factorLoadingFirstDerivative, RealMatrix errorVarianceFirstDerivative){
        double[] gradient = new double[getNumberOfParameters()];
        double sumL = 0.0, sumE = 0.0;

        for(int i=0;i<factorLoadingFirstDerivative.getRowDimension();i++){
            sumL += factorLoadingFirstDerivative.getEntry(i, 0);
        }
        gradient[0]=sumL;

        for(int i=0;i<errorVariance.length;i++){
            sumE += errorVarianceFirstDerivative.getEntry(i, i);
        }
        gradient[1] = sumE;
        return gradient;
    }

}

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

import com.itemanalysis.psychometrics.measurement.DiagonalMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class CongenericModel extends AbstractConfirmatoryFactorAnalysisModel{

    RealMatrix SIGMA = null;

    RealMatrix B = null;

    public CongenericModel(int nItems, double[] inits){
        super(nItems);
        this.setInitialFactorLoading(inits);
    }

    public CongenericModel(int nItems){
        super(nItems);
    }

    public String getName(){
        return "                 CONGENERIC MODEL";
    }

    public int getNumberOfParameters(){
        return nItems*2;
    }

    public void setInitialFactorLoading(double[] inits){
        for(int i=0;i<factorLoading.length;i++){
            factorLoading[i]=inits[i];
        }
    }

    public double[] getInitialValuesVector(){
        double[] inits = new double[this.getNumberOfParameters()];
        int n = factorLoading.length;
        for(int i=0;i<n;i++){
            inits[i]=factorLoading[i];
            inits[n+i]=errorVariance[i];
        }
        return inits;
    }

    public void setParameters(double[] argument){
        int index=0;
        for(int i=0;i<nItems;i++){
            index=i+nItems;
            factorLoading[i]=argument[i];
            errorVariance[i]=argument[index];
        }
    }

    public RealMatrix getImpliedCovariance(double[] argument){
        setParameters(argument);
        B = new Array2DRowRealMatrix(getFactorLoading()); //param
        RealMatrix BTran = B.transpose();
        RealMatrix THETA = new DiagonalMatrix(getErrorVariance());//param
        SIGMA = B.multiply(BTran).add(THETA);
        return SIGMA;
    }

    public RealMatrix getBeta(double[] argument){
        setParameters(argument);
        B = new Array2DRowRealMatrix(getFactorLoading()); //param
        return B;
    }
    
    public void setGradient(RealMatrix factorLoadingFirstDerivative, RealMatrix errorVarianceFirstDerivative, double[] gradient){
        int step = factorLoading.length;
        for(int i=0;i<step;i++){
            gradient[i]=factorLoadingFirstDerivative.getEntry(i, 0);
        }
        for(int i=0;i<errorVariance.length;i++){
            gradient[step+i]=errorVarianceFirstDerivative.getEntry(i, i);
        }
    }

    public double[] getGradient(RealMatrix factorLoadingFirstDerivative, RealMatrix errorVarianceFirstDerivative){
        double[] gradient = new double[getNumberOfParameters()];
        int step = factorLoading.length;
        for(int i=0;i<step;i++){
            gradient[i]=factorLoadingFirstDerivative.getEntry(i, 0);
        }
        for(int i=0;i<errorVariance.length;i++){
            gradient[step+i]=errorVarianceFirstDerivative.getEntry(i, i);
        }
        return gradient;
    }

}

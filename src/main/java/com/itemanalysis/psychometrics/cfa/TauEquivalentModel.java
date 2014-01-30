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
import com.itemanalysis.psychometrics.statistics.IdentityVector;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class TauEquivalentModel extends AbstractConfirmatoryFactorAnalysisModel{

    RealMatrix SIGMA = null;

    RealMatrix B = null;

    RealMatrix Ivec = null;

    public TauEquivalentModel(int nItems, double[] inits){
        super(nItems);
        this.setInitialFactorLoading(inits);
        Ivec = new IdentityVector(nItems);
    }

    public TauEquivalentModel(int nItems){
        super(nItems);
        Ivec = new IdentityVector(nItems);
    }

    public String getName(){
        return "               TAU-EQUIVALENT MODEL";
    }

    public int getNumberOfParameters(){
        return nItems+1;
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
        for(int i=0;i<errorVariance.length;i++){
            inits[i+1]=errorVariance[i];
        }
        return inits;
    }

    public void setParameters(double[] argument){
        for(int i=0;i<nItems;i++){
            factorLoading[i]=argument[0];//equality constraints
            errorVariance[i]=argument[i+1];
        }
    }

    public RealMatrix getImpliedCovariance(double[] argument){
        setParameters(argument);
        RealMatrix B2 = Ivec.scalarMultiply(Math.pow(argument[0], 2));
        RealMatrix THETA = new DiagonalMatrix(errorVariance);//param
        SIGMA = B2.multiply(Ivec.transpose()).add(THETA);


//        System.out.println("TAU: " );
//        for(int i=0;i<SIGMA.getRowDimension();i++){
//            for(int j=0;j<SIGMA.getColumnDimension();j++){
//                System.out.print(" " + SIGMA.getEntry(i, j));
//            }
//            System.out.println();
//        }
//        System.out.println();


        return SIGMA;
    }

    public RealMatrix getBeta(double[] argument){
        setParameters(argument);
        B = Ivec.scalarMultiply(argument[0]);
        return B;
    }

    public void setGradient(RealMatrix factorLoadingFirstDerivative, RealMatrix errorVarianceFirstDerivative, double[] gradient){
        double sum=0.0;
        for(int i=0;i<factorLoadingFirstDerivative.getRowDimension();i++){
            sum+=factorLoadingFirstDerivative.getEntry(i, 0);
        }
        gradient[0]=sum;

        for(int i=0;i<errorVariance.length;i++){
            gradient[i+1]=errorVarianceFirstDerivative.getEntry(i, i);
        }

    }

    public double[] getGradient(RealMatrix factorLoadingFirstDerivative, RealMatrix errorVarianceFirstDerivative){
        double[] gradient = new double[getNumberOfParameters()];
        double sum=0.0;
        for(int i=0;i<factorLoadingFirstDerivative.getRowDimension();i++){
            sum+=factorLoadingFirstDerivative.getEntry(i, 0);
        }
        gradient[0]=sum;

        for(int i=0;i<errorVariance.length;i++){
            gradient[i+1]=errorVarianceFirstDerivative.getEntry(i, i);
        }
        return gradient;
    }

}

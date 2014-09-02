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
package com.itemanalysis.psychometrics.factoranalysis;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class VarimaxCriteria implements RotationCriteria {

    private double functionValue = 0;
    private RealMatrix gradient = null;

    public VarimaxCriteria(){

    }

    public double getValue(){
        return functionValue;
    }

    public RealMatrix getGradient(){
        return gradient;
    }

    /**
     * Computes the function value for varimax rotation.
     *
     * @param L matrix of factor loadings.
     */
    public void computeValues(RealMatrix L){
        //initialize dimensions and column mean array
        int nrow = L.getRowDimension();
        int ncol = L.getColumnDimension();
        Mean[] colMean = new Mean[ncol];
        for(int i=0;i<ncol;i++){
            colMean[i] = new Mean();
        }

        //square each element in matrix
        RealMatrix L2 = L.copy();
        double value = 0.0;
        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                value = L.getEntry(i,j);
                value *= value;
                L2.setEntry(i,j, value);
                colMean[j].increment(value);
            }
        }

        double dif = 0.0;
        RealMatrix QL = new Array2DRowRealMatrix(nrow, ncol);
        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                dif = L2.getEntry(i,j)-colMean[j].getResult();
                QL.setEntry(i,j, dif);
            }
        }

        //compute gradientAt
        gradient = new Array2DRowRealMatrix(nrow, ncol);
        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                value = -L.getEntry(i,j)* QL.getEntry(i,j);
                gradient.setEntry(i,j, value);
            }
        }

        //compute function value
        RealMatrix B = QL.transpose().multiply(QL);
        double sum = B.getTrace();
        functionValue = -sum/4.0;

    }


}

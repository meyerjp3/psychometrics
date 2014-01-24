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

import com.itemanalysis.psychometrics.measurement.DiagonalMatrix;
import com.itemanalysis.psychometrics.optimization.DiffFunction;
import com.itemanalysis.psychometrics.statistics.IdentityMatrix;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.linear.*;

import java.util.Formatter;

public class MINRESmethod implements DiffFunction, MultivariateFunction{

    private RealMatrix R = null;
    private int nVariables = 0;
    private int nFactors = 1;
    private int nParam = 0;
    private double[][] factorLoading = null;


    public MINRESmethod(RealMatrix R, int nFactors){
        this.nVariables = R.getColumnDimension();
        this.nParam = nVariables;
        this.nFactors = nFactors;
        this.R = R;

    }

    public int domainDimension(){
        return nParam;
    }

    public double value(double[] param){
        return valueAt(param);
    }

    public double valueAt(double[] param){
        for(int i=0;i<nVariables;i++){
            R.setEntry(i,i,1.0-param[i]);
        }

        EigenDecomposition eigen = new EigenDecomposition(R);
        RealMatrix eigenVectors = eigen.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);

        double[] ev = new double[nFactors];
        for(int i=0;i<nFactors;i++){
            ev[i] = Math.sqrt(eigen.getRealEigenvalue(i));
        }
        DiagonalMatrix evMatrix = new DiagonalMatrix(ev);//USE Apache version of Diagonal matrix when upgrade to version 3.2
        RealMatrix LAMBDA = eigenVectors.multiply(evMatrix);
        RealMatrix SIGMA = (LAMBDA.multiply(LAMBDA.transpose()));
        RealMatrix RESID = R.subtract(SIGMA);

        double sum = 0.0;
        for(int i=0;i<RESID.getRowDimension();i++){
            for(int j=0;j<RESID.getColumnDimension();j++){
                if(i!=j){
                    sum += Math.pow(RESID.getEntry(i,j),2);
                }
            }
        }
        return sum;

    }

//    public double valueAt(double[] param){
//        RealMatrix LAMBDA = new Array2DRowRealMatrix(nVariables, nFactors);
//        IdentityMatrix U = new IdentityMatrix(nVariables);
//        int offset = 0;
//        for(int i=0;i<nVariables;i++){
//            for(int j=0;j<nFactors;j++){
//                LAMBDA.setEntry(i,j,param[offset++]);
//            }
//        }
//        for(int i=0;i<nVariables;i++){
//            U.setEntry(i,i,Math.pow(param[offset++],2));
//        }
//
//        RealMatrix SIGMA = (LAMBDA.multiply(LAMBDA.transpose())).add(U);
//        RealMatrix D = R.subtract(SIGMA);
//        for(int i=0;i<D.getRowDimension();i++){
//            for(int j=0;j<D.getColumnDimension();j++){
//                D.setEntry(i,j, Math.pow(D.getEntry(i, j), 2));
//            }
//        }
//
//        double f = 0.5*D.getTrace();
//        return f;
//
//    }

    /**
     * Gradient
     *
     * @param x a <code>double[]</code> input vector
     * @return
     */
    public double[] derivativeAt(double[] x){
        double[] sqrtPsi = new double[nVariables];
        double[] invSqrtPsi = new double[nVariables];
        for(int i=0;i<nVariables;i++){
            sqrtPsi[i] = Math.sqrt(x[i]);
            invSqrtPsi[i] = 1.0/Math.sqrt(x[i]);
        }
        DiagonalMatrix diagPsi = new DiagonalMatrix(x);
        DiagonalMatrix diagSqtPsi = new DiagonalMatrix(sqrtPsi);
        DiagonalMatrix diagInvSqrtPsi = new DiagonalMatrix(invSqrtPsi);

        RealMatrix Sstar = diagInvSqrtPsi.multiply(R).multiply(diagInvSqrtPsi);

        EigenDecomposition E = new EigenDecomposition(Sstar);
        RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
        double[] ev = new double[nFactors];
        for(int i=0;i<nFactors;i++){
            ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
        }
        DiagonalMatrix M = new DiagonalMatrix(ev);
        RealMatrix LOAD = L.multiply(M);

        RealMatrix LL = diagSqtPsi.multiply(LOAD);
        RealMatrix G = LL.multiply(LL.transpose()).add(diagPsi).subtract(R);

        double[] gradient = new double[nVariables];
        for(int i=0;i<nVariables;i++){
            gradient[i] = G.getEntry(i,i)/(x[i]*x[i]);
        }
        return gradient;
    }

    //NOT WORKING
    //TODO check these computations
    public void computeFactorLoadings(double[] x, RealMatrix cor){
        double[] sqrtPsi = new double[nVariables];
        double[] invSqrtPsi = new double[nVariables];
        for(int i=0;i<nVariables;i++){
            sqrtPsi[i] = Math.sqrt(x[i]);
            invSqrtPsi[i] = 1.0/Math.sqrt(x[i]);
        }
//        DiagonalMatrix diagPsi = new DiagonalMatrix(x);
        DiagonalMatrix diagSqtPsi = new DiagonalMatrix(sqrtPsi);
        DiagonalMatrix diagInvSqrtPsi = new DiagonalMatrix(invSqrtPsi);

        RealMatrix Sstar = diagInvSqrtPsi.multiply(cor).multiply(diagInvSqrtPsi);
        EigenDecomposition E = new EigenDecomposition(Sstar);
        RealMatrix L = E.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);
        double[] ev = new double[nFactors];
        for(int i=0;i<nFactors;i++){
            ev[i] = Math.sqrt(Math.max(E.getRealEigenvalue(i) - 1, 0));
        }
        DiagonalMatrix M = new DiagonalMatrix(ev);
        RealMatrix LOAD2 = L.multiply(M);
        RealMatrix LOAD = diagSqtPsi.multiply(LOAD2);

        factorLoading = new double[nVariables][nFactors];
        for(int i=0;i<nVariables;i++){
            for(int j=0;j<nFactors;j++){
                factorLoading[i][j] = LOAD.getEntry(i,j);
            }
        }

    }

    public double[][] getFactorLoading(){
        return factorLoading;
    }

    public double getFactorLoadingAt(int i, int j){
        return factorLoading[i][j];
    }


}




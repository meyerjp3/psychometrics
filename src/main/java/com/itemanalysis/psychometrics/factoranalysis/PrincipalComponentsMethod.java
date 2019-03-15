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

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

/**
 * Principal components analysis.
 */
public class PrincipalComponentsMethod extends AbstractFactorMethod {

    public PrincipalComponentsMethod(RealMatrix R, int nFactors, RotationMethod rotationMethod){
        this.R = R;
        this.nVariables = R.getColumnDimension();
        this.nFactors = nFactors;
        this.rotationMethod = rotationMethod;
    }

    public double estimateParameters(){

        EigenDecomposition eigen = new EigenDecomposition(R);
        RealMatrix eigenVectors = eigen.getV().getSubMatrix(0,nVariables-1, 0, nFactors-1);

        double[] ev = new double[nFactors];
        for(int i=0;i<nFactors;i++){
            ev[i] = Math.sqrt(eigen.getRealEigenvalue(i));
        }
        DiagonalMatrix evMatrix = new DiagonalMatrix(ev);//USE Apache version of Diagonal matrix when upgrade to version 3.2
        RealMatrix LOAD = eigenVectors.multiply(evMatrix);

        //rotate factor loadings
        if(rotationMethod!=RotationMethod.NONE){
            GPArotation gpa = new GPArotation();
            RotationResults results = gpa.rotate(LOAD, rotationMethod);
            LOAD = results.getFactorLoadings();
        }

        Sum[] colSums = new Sum[nFactors];
        Sum[] colSumsSquares = new Sum[nFactors];

        for(int j=0;j<nFactors;j++){
            colSums[j] = new Sum();
            colSumsSquares[j] = new Sum();
        }

        factorLoading = new double[nVariables][nFactors];
        communality = new double[nVariables];
        uniqueness = new double[nVariables];

        for(int i=0;i<nVariables;i++){
            for(int j=0;j<nFactors;j++){
                factorLoading[i][j] = LOAD.getEntry(i,j);
                colSums[j].increment(factorLoading[i][j]);
                colSumsSquares[j].increment(Math.pow(factorLoading[i][j],2));
                communality[i] += Math.pow(factorLoading[i][j], 2);
            }
        }

        //check sign of factor
        double sign = 1.0;
        for(int i=0;i<nVariables;i++){
            for(int j=0;j<nFactors;j++){
                if(colSums[j].getResult()<0){
                    sign = -1.0;
                }else{
                    sign = 1.0;
                }
                factorLoading[i][j] = factorLoading[i][j]*sign;
            }
            uniqueness[i] = 1.0-communality[i];
        }

        double totSumOfSquares = 0.0;
        sumsOfSquares = new double[nFactors];
        proportionOfExplainedVariance = new double[nFactors];
        proportionOfVariance = new double[nFactors];
        for(int j=0;j<nFactors;j++){
            sumsOfSquares[j] = colSumsSquares[j].getResult();
            totSumOfSquares += sumsOfSquares[j];
        }
        for(int j=0;j<nFactors;j++){
            proportionOfExplainedVariance[j] = sumsOfSquares[j]/totSumOfSquares;
            proportionOfVariance[j] = sumsOfSquares[j]/nVariables;
        }

        return 0.0;

    }


}

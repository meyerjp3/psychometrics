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
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FactorAnalysisTest {

    /**
     * Correlation matrix from Harman74.cor$cov in R psych package.
     * @return
     */
    private double[][] readHarman74Data(){
        double[][] harman74 = new double[24][24];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/harman74.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    harman74[row][j] = Double.parseDouble(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return harman74;
    }


    @Test
    public void eigenValues(){
        RealMatrix R = new Array2DRowRealMatrix(readHarman74Data());

        int nFactors = 2;
        int nVariables=24;
        double[] x = new double[nVariables];
        for(int i=0;i<nVariables;i++){
            x[i]=.5;
        }

        for(int i=0;i<nVariables;i++){
            R.setEntry(i,i,1.0-x[i]);
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
        double squared = 0.0;
        for(int i=0;i<RESID.getRowDimension();i++){
            for(int j=0;j<RESID.getColumnDimension();j++){
                if(i!=j){
                    sum += Math.pow(RESID.getEntry(i,j),2);
                }
            }
        }


        System.out.println(sum);





//        RealMatrix SIGMA = (LAMBDA.multiply(LAMBDA.transpose()));
//
//        System.out.println("SIGMA");
//        for(int i=0;i<SIGMA.getRowDimension();i++){
//            for(int j=0;j<SIGMA.getColumnDimension();j++){
//                System.out.print(SIGMA.getEntry(i, j) + " ");
//            }
//            System.out.println();
//        }
    }

    //@Ignore
    @Test
    public void harmanTest(){
        RealMatrix correlationMatrix = new Array2DRowRealMatrix(readHarman74Data());
        FactorAnalysis fa = new FactorAnalysis(correlationMatrix, 2);
        fa.estimateParameters();
        System.out.println(fa.printFactorLoadings());



    }

}

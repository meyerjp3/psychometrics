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

    private double[][] readM255(){
        double[][] harman74 = new double[12][12];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/m255-cor.txt"));
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

    //@Ignore
    @Test
    public void harmanTest(){
        RealMatrix correlationMatrix = new Array2DRowRealMatrix(readHarman74Data());
        FactorAnalysis fa = new FactorAnalysis(correlationMatrix, 4);
        fa.estimateParameters();
        System.out.println(fa.printOutput());



    }

    @Test
    public void m255Test(){
        RealMatrix R = new Array2DRowRealMatrix(readM255());
        FactorAnalysis fa = new FactorAnalysis(R, 3);
        fa.estimateParameters();
        System.out.println(fa.printOutput());


    }

}

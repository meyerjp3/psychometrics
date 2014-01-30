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

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class ExploratoryFactorAnalysisTest {

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

    @Test
    public void harmanTestMINRES(){
        System.out.println("MINRES Factor Analysis Test: Harman data");
        RealMatrix correlationMatrix = new Array2DRowRealMatrix(readHarman74Data());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(correlationMatrix, 4);
        fa.estimateParameters(EstimationMethod.MINRES);
        System.out.println();
        System.out.println(fa.printOutput());
    }

    @Test
    public void m255TestMINRES(){
        System.out.println("MINRES Factor Analysis Test: m255 data");
        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.MINRES);
        System.out.println();
        System.out.println(fa.printOutput());
    }

    @Test
    public void harmanTestPrincipalComponents(){
        System.out.println("Principal Components Analysis Test: Harman data");
        RealMatrix R = new Array2DRowRealMatrix(readHarman74Data());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 4);
        fa.estimateParameters(EstimationMethod.PRINCOMP);
        System.out.println();
        System.out.println(fa.printOutput());
    }

    @Test
    public void m255TestPrincipalComponents(){
        System.out.println("Principal Components Analysis Test: m255 data");
        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.PRINCOMP);
        System.out.println();
        System.out.println(fa.printOutput());

        double[][] true_loadings = {
                {0.7020, -0.465531,  0.06718},
                {0.7146, -0.437463,  0.11367},
                {0.7455, -0.299328,  0.12827},
                {0.6779, -0.293400,  0.19308},
                {0.7957, -0.073120,  0.05381},
                {0.7476,  0.354893,  0.13867},
                {0.6342,  0.493008,  0.25213},
                {0.5783,  0.298165,  0.36896},
                {0.7453,  0.159165, -0.14060},
                {0.6373,  0.387494, -0.19624},
                {0.8163, -0.009932, -0.34579},
                {0.7055,  0.016659, -0.51390}
        };

        double[] true_uniqueness = {0.2859, 0.2851, 0.3382, 0.4171, 0.3586, 0.2959,
                0.2912, 0.4405, 0.3994, 0.4052, 0.2140, 0.2379};

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<3;j++){
                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-4);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-4);
        }

    }

    @Test
    public void harmanTestMaximumLikelihood(){
        System.out.println("Maximum likelihood Factor Analysis Test: Harman data");
        RealMatrix R = new Array2DRowRealMatrix(readHarman74Data());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 4);
        fa.estimateParameters(EstimationMethod.ML);
        System.out.println();
        System.out.println(fa.printOutput());

        //true values from psych package in R.
        //fa(Harman74.cor$cov,4, fm="ml", rotate="none").
        double[][] true_loadings = {
                {0.5534,0.0437,0.4538,-0.2179},
                {0.3439,-0.0104,0.289,-0.1343},
                {0.3769,-0.1113,0.4209,-0.1578},
                {0.4648,-0.0710,0.2976,-0.1978},
                {0.7408,-0.2251,-0.217,-0.0376},
                {0.7370,-0.3473,-0.1452,0.0601},
                {0.7379,-0.3245,-0.2416,-0.0959},
                {0.6961,-0.1206,-0.0335,-0.1201},
                {0.7492,-0.3910,-0.1599,0.0605},
                {0.4861,0.617,-0.3782,-0.0136},
                {0.5401,0.3700,-0.0393,0.1375},
                {0.4474,0.5716,-0.0401,-0.191},
                {0.5785,0.3068,0.1174,-0.2583},
                {0.4039,0.0447,0.0823,0.4268},
                {0.3648,0.0708,0.1617,0.3738},
                {0.4523,0.0724,0.4193,0.2555},
                {0.4384,0.1901,0.0806,0.4086},
                {0.4644,0.3145,0.2446,0.1813},
                {0.4155,0.0930,0.1743,0.1639},
                {0.6019,-0.0907,0.1909,0.0370},
                {0.5608,0.2705,0.1461,-0.0898},
                {0.5949,-0.0806,0.1926,0.0379},
                {0.6695,-6e-04,0.2155,-0.0902},
                {0.6544,0.2373,-0.1125,0.0555}
        };

        double[] true_uniqueness = {
                0.4385, 0.7801, 0.6435, 0.6512, 0.3520, 0.3115, 0.2826, 0.4854, 0.2566, 0.2397,
                0.5510, 0.4351, 0.4907, 0.6460, 0.6960, 0.5491, 0.5982, 0.5926, 0.7615, 0.5916,
                0.5829, 0.6010, 0.4973, 0.4998};

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<3;j++){
                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-3);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-3);
        }

    }

    @Test
    public void m255TestMaximumLikelihood(){
        System.out.println("Maximum likelihood Factor Analysis Test: m255 data");

        //true values from psych package in R.
        //fa(r,3, fm="ml", rotate="none")
        double[][] true_loadings={
                {0.6819,-0.3916,0.1228},
                {0.6981,-0.3708,0.1608},
                {0.7186,-0.2215,0.1283},
                {0.6259,-0.1853,0.1691},
                {0.7605,-0.0195,0.1163},
                {0.7194,0.3442,0.1819},
                {0.5942,0.431,0.2393},
                {0.5199,0.1779,0.1124},
                {0.7099,0.1372,-0.003},
                {0.5903,0.2653,0.0051},
                {0.8466,0.0131,-0.3268},
                {0.6966,0.0282,-0.2788}
        };

        //true values from psych package in R.
        double[] true_uniqueness = {0.3666, 0.3493, 0.4181, 0.5454, 0.4077, 0.3309, 0.4038, 0.6855, 0.4772, 0.5811,
                0.1763, 0.4362};

        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.ML);
        System.out.println();
        System.out.println(fa.printOutput());

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<3;j++){
                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-3);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-3);
        }


    }



}

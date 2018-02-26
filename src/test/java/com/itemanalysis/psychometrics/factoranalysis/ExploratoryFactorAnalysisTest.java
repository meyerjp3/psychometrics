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

    /**
     * Test start values for the communality.
     * The true vaues are the squared multiple correlation (SMC) computed in the psych package in R.
     *
     * library(psych)
     * options(digits=15)
     * R<-matrix(c(
     * 1,0.6602,0.5649,0.5324,0.5203,0.3789,0.2628,0.3069,0.4448,0.3101,0.531,0.4303,
     * 0.6602,1,0.6265,0.4898,0.5344,0.4143,0.3082,0.3089,0.4194,0.3109,0.5374,0.431,
     * 0.5649,0.6265,1,0.5135,0.5708,0.4618,0.3659,0.3491,0.4782,0.3528,0.5738,0.4344,
     * 0.5324,0.4898,0.5135,1,0.5736,0.3951,0.318,0.3284,0.4206,0.3305,0.454,0.4174,
     * 0.5203,0.5344,0.5708,0.5736,1,0.5663,0.4524,0.3863,0.5677,0.4309,0.6025,0.4988,
     * 0.3789,0.4143,0.4618,0.3951,0.5663,1,0.6306,0.457,0.5393,0.5091,0.559,0.4551,
     * 0.2628,0.3082,0.3659,0.318,0.4524,0.6306,1,0.4096,0.4777,0.4629,0.4331,0.3561,
     * 0.3069,0.3089,0.3491,0.3284,0.3863,0.457,0.4096,1,0.3895,0.3754,0.3984,0.356,
     * 0.4448,0.4194,0.4782,0.4206,0.5677,0.5393,0.4777,0.3895,1,0.4878,0.605,0.4814,
     * 0.3101,0.3109,0.3528,0.3305,0.4309,0.5091,0.4629,0.3754,0.4878,1,0.4922,0.4348,
     * 0.531,0.5374,0.5738,0.454,0.6025,0.559,0.4331,0.3984,0.605,0.4922,1,0.6827,
     * 0.4303,0.431,0.4344,0.4174,0.4988,0.4551,0.3561,0.356,0.4814,0.4348,0.6827,1), nrow=12, byrow=TRUE)
     *
     * smc(R)
     */
    @Test
    public void smcTest(){
        System.out.println("SMC test: m255 data");
        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 2);
        fa.estimateParameters(EstimationMethod.MINRES);

        //System.out.println(fa.printOutput(3));

        double[] trueStartValues = {0.531585981070840, 0.551247464024654, 0.527292132264549,
                0.432978821972321, 0.566918817558485, 0.559797287040695, 0.453127105279017,
                0.284772895724122, 0.493299407818581, 0.377457545259643, 0.645814037220527,
                0.497023187312404};

        double[] startValues = fa.getFactorMethod().getSquaredMultipleCorrelation();
        //System.out.println(fa.getFactorMethod().printStartValues());

        for(int i=0;i<trueStartValues.length;i++){
            assertEquals("  start value at " + (i+1), trueStartValues[i], startValues[i], 1e-15);
        }
    }

    @Test
    public void harmanTestMINRES(){
        System.out.println("MINRES Factor Analysis Test: Harman data");
        RealMatrix R = new Array2DRowRealMatrix(readHarman74Data());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 4);
        fa.estimateParameters(EstimationMethod.MINRES);
//        System.out.println();
        System.out.println(fa.printOutput(3));

        //true values obtained from psych package in R
        //fa(r=Harman74.cor$cov, nf=4, fm="minres", rotate="none")
//        double[][] true_loadings = {
//                {0.5534,0.0436,0.4538,-0.2179},
//                {0.3439,-0.0104,0.2890,-0.1344},
//                {0.3769,-0.1114,0.4209,-0.1578},
//                {0.4648,-0.0710,0.2976,-0.1977},
//                {0.7408,-0.2251,-0.2171,-0.0376},
//                {0.7369,-0.3473,-0.1452,0.0601},
//                {0.7379,-0.3245,-0.2416,-0.0958},
//                {0.6961,-0.1207,-0.0335,-0.1201},
//                {0.7492,-0.3911,-0.1599,0.0605},
//                {0.4861,0.6171,-0.3782,-0.0137},
//                {0.5401,0.3700,-0.0393,0.1374},
//                {0.4474,0.5715,-0.0401,-0.1909},
//                {0.5785,0.3067,0.1174,-0.2584},
//                {0.4039,0.0447,0.0823,0.4266},
//                {0.3648,0.0708,0.1617,0.3738},
//                {0.4523,0.0724,0.4192,0.2554},
//                {0.4385,0.1901,0.0806,0.4089},
//                {0.4645,0.3145,0.2447,0.1814},
//                {0.4155,0.0930,0.1743,0.1640},
//                {0.6019,-0.0907,0.1909,0.037},
//                {0.5608,0.2705,0.1461,-0.0900},
//                {0.5949,-0.0807,0.1926,0.0380},
//                {0.6695,-6e-04,0.2154,-0.0902},
//                {0.6544,0.2373,-0.1125,0.0555}
//        };

        //true values obtained from psych package in R
        //fa(r=Harman74.cor$cov, nf=4, fm="minres", rotate="none") but changed to
        //use a conjugate gradientAt optimization method.
        double[][] true_loadings = {
                {0.5540,  0.0540,  0.4504, -0.2163},
                {0.3446, -0.0043,  0.2900, -0.1354},
                {0.3783, -0.1019,  0.4232, -0.1582},
                {0.4655, -0.0650,  0.2993, -0.1975},
                {0.7410, -0.2273, -0.2142, -0.0379},
                {0.7376, -0.3484, -0.1400,  0.0602},
                {0.7383, -0.3272, -0.2369, -0.0961},
                {0.6964, -0.1196, -0.0321, -0.1206},
                {0.7499, -0.3919, -0.1541,  0.0608},
                {0.4831,  0.6076, -0.3881, -0.0136},
                {0.5394,  0.3721, -0.0495,  0.1383},
                {0.4461,  0.5737, -0.0534, -0.1925},
                {0.5782,  0.3118,  0.1091, -0.2584},
                {0.4039,  0.0477,  0.0797,  0.4245},
                {0.3648,  0.0749,  0.1586,  0.3727},
                {0.4527,  0.0808,  0.4170,  0.2553},
                {0.4383,  0.1933,  0.0754,  0.4097},
                {0.4642,  0.3209,  0.2374,  0.1827},
                {0.4158,  0.0975,  0.1723,  0.1666},
                {0.6025, -0.0859,  0.1921,  0.0361},
                {0.5604,  0.2743,  0.1403, -0.0895},
                {0.5956, -0.0754,  0.1931,  0.0389},
                {0.6698,  0.0049,  0.2149, -0.0905},
                {0.6535,  0.2365, -0.1179,  0.0559}
        };

        double[] true_uniqueness = {
                0.4385, 0.7802, 0.6436, 0.6514, 0.3520, 0.3117, 0.2827, 0.4854, 0.2566, 0.2397,
                0.5511, 0.4354, 0.4908, 0.6463, 0.6961, 0.5494, 0.5978, 0.5925, 0.7616, 0.5916,
                0.5828, 0.6009, 0.4973, 0.4998};

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<4;j++){
                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-2);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-2);
        }

    }

    /**
     * This test shows the least amount of agreement with R. Only one decimal place of precision.
     * I am not sure why because the MINRES method worked fine for the other test.
     */
    @Test
    public void m255TestMINRES(){
        System.out.println("MINRES Factor Analysis Test (no rotation): m255 data");
        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.MINRES);
//        System.out.println();
        System.out.println(fa.printOutput(3));

        double[][] true_loadings = {
                {0.6836,-0.3908,0.1130},
                {0.7002,-0.3712,0.1542},
                {0.7200,-0.2217,0.1227},
                {0.6285,-0.1843,0.1545},
                {0.7622,-0.0188,0.1045},
                {0.7216,0.3458,0.1759},
                {0.5962,0.4292,0.2326},
                {0.5214,0.1782,0.1026},
                {0.7102,0.1378,-0.0139},
                {0.5910,0.2662,-0.0076},
                {0.8389,0.0129,-0.3197},
                {0.6957,0.0292,-0.3029}
        };

        double[] true_uniqueness = {0.3675, 0.3482, 0.4172, 0.5472, 0.4075, 0.3284, 0.4082, 0.6869, 0.4762, 0.5798,
                0.1942, 0.4219};

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<3;j++){
                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-2);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-2);
        }

    }

    @Test
    public void m255TestMINRESVarimax(){
        System.out.println("MINRES Factor Analysis Test (Varimax rotation): m255 data");
        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.MINRES, RotationMethod.VARIMAX);
        System.out.println();
        System.out.println(fa.printOutput());

        double[][] true_loadings = {
                {0.6836,-0.3908,0.1130},
                {0.7002,-0.3712,0.1542},
                {0.7200,-0.2217,0.1227},
                {0.6285,-0.1843,0.1545},
                {0.7622,-0.0188,0.1045},
                {0.7216,0.3458,0.1759},
                {0.5962,0.4292,0.2326},
                {0.5214,0.1782,0.1026},
                {0.7102,0.1378,-0.0139},
                {0.5910,0.2662,-0.0076},
                {0.8389,0.0129,-0.3197},
                {0.6957,0.0292,-0.3029}
        };

        double[] true_uniqueness = {0.3675, 0.3482, 0.4172, 0.5472, 0.4075, 0.3284, 0.4082, 0.6869, 0.4762, 0.5798,
                0.1942, 0.4219};

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<3;j++){
//                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-1);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
//            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-1);
        }

    }

    @Test
    public void m255TestMINRESOblimin(){
        System.out.println("MINRES Factor Analysis Test (Oblimin rotation): m255 data");
        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.MINRES, RotationMethod.OBLIMIN);
        System.out.println();
        System.out.println(fa.printOutput());

//        double[][] true_loadings = {
//                {0.6836,-0.3908,0.1130},
//                {0.7002,-0.3712,0.1542},
//                {0.7200,-0.2217,0.1227},
//                {0.6285,-0.1843,0.1545},
//                {0.7622,-0.0188,0.1045},
//                {0.7216,0.3458,0.1759},
//                {0.5962,0.4292,0.2326},
//                {0.5214,0.1782,0.1026},
//                {0.7102,0.1378,-0.0139},
//                {0.5910,0.2662,-0.0076},
//                {0.8389,0.0129,-0.3197},
//                {0.6957,0.0292,-0.3029}
//        };
//
//        double[] true_uniqueness = {0.3675, 0.3482, 0.4172, 0.5472, 0.4075, 0.3284, 0.4082, 0.6869, 0.4762, 0.5798,
//                0.1942, 0.4219};

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<3;j++){
//                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-1);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
//            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-1);
        }

    }

    @Test
    public void harmanTestPrincipalComponents(){
        System.out.println("Principal Components Analysis Test: Harman data");
        RealMatrix R = new Array2DRowRealMatrix(readHarman74Data());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 4);
        fa.estimateParameters(EstimationMethod.PRINCOMP);
//        System.out.println();
//        System.out.println(fa.printOutput());

        //True values obtained from R but changed the optimization routine from L-BFGS-B
        //to CG. This change was needed to obtain the same small value of the objective function obtained
        //by the tested class. The L-BFGS-B method stopped at a larger value of the objective function.
        double[][] true_loadings = {
                {0.6157,-0.0054,0.4277,-0.2045},
                {0.3996,-0.0794,0.4001,-0.2015},
                {0.4446,-0.1908,0.4764,-0.1058},
                {0.5105,-0.178,0.3347,-0.2159},
                {0.6949,-0.3213,-0.3353,-0.0528},
                {0.6904,-0.4176,-0.2651,0.0806},
                {0.6768,-0.4249,-0.3554,-0.0725},
                {0.6941,-0.2428,-0.1436,-0.1163},
                {0.6944,-0.4506,-0.2908,0.0796},
                {0.4741,0.5418,-0.4463,-0.2016},
                {0.5763,0.4338,-0.2101,0.0338},
                {0.4823,0.5489,-0.1267,-0.3404},
                {0.618,0.2790,0.0354,-0.3659},
                {0.4479,0.0926,-0.0551,0.5554},
                {0.4156,0.1423,0.0784,0.5259},
                {0.5337,0.0909,0.3916,0.3272},
                {0.4876,0.2755,-0.0524,0.4691},
                {0.5437,0.3863,0.1983,0.1519},
                {0.4755,0.1379,0.1219,0.1933},
                {0.643,-0.1864,0.1321,0.0701},
                {0.6215,0.2323,0.0998,-0.2017},
                {0.6395,-0.1458,0.1103,0.0556},
                {0.7116,-0.1049,0.1495,-0.103},
                {0.6726,0.1958,-0.2333,-0.0618}
        };

        double[] true_uniqueness = {
                0.3961, 0.6333, 0.5278, 0.5490, 0.2986, 0.2721, 0.2298, 0.4251, 0.2239, 0.2419,
                0.4344, 0.3342, 0.4050, 0.4793, 0.5243, 0.4465, 0.4635, 0.4928, 0.7027, 0.5294,
                0.5091, 0.5545, 0.4496, 0.4510};

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<4;j++){
                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-4);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-4);
        }

    }

    @Test
    public void m255TestPrincipalComponents(){
        System.out.println("Principal Components Analysis Test: m255 data");
        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.PRINCOMP);
//        System.out.println();
//        System.out.println(fa.printOutput());

        //True values obtained from R using code extracted from the psych package in R.
        //Needed to extract code from principal() function to get four decimal places.
        //principal(r=r, nfactors=3, rotate="none")
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
//        System.out.println();
//        System.out.println(fa.printOutput());

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
            for(int j=0;j<4;j++){
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
//        System.out.println();
//        System.out.println(fa.printOutput());

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
         public void harmanWeightedLeastSquaresTest(){
        System.out.println("Weighted LeastSquares Factor Analysis Test: Harman data");
        RealMatrix R = new Array2DRowRealMatrix(readHarman74Data());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 4);
        fa.estimateParameters(EstimationMethod.WLS);
        System.out.println();
        System.out.println(fa.printOutput());

        //true values from psych package in R.
        //fa(Harman74.cor$cov,4, fm="wls", rotate="none").
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

//        FactorMethod factorMethod = fa.getFactorMethod();
//        for(int i=0;i<R.getColumnDimension();i++){
//            for(int j=0;j<4;j++){
//                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-3);
//            }
//        }
//
//        for(int i=0;i<R.getColumnDimension();i++){
//            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-3);
//        }

    }

    @Test
    public void m255TestWeightedLeastSquares(){
        System.out.println("Weighted Least Squares Factor Analysis Test: m255 data");

        //true values from psych package in R.
        //fa(r,3, fm="wls", rotate="none")
        double[][] true_loadings={
                {0.6837, -0.3971, 0.0730},
                {0.6986, -0.3801, 0.1199},
                {0.7196, -0.2332, 0.0998},
                {0.6389, -0.1924, 0.1008},
                {0.7688, -0.0401, 0.0651},
                {0.7320,  0.3360, 0.1460},
                {0.6117,  0.4198, 0.2035},
                {0.5308,  0.1715, 0.0660},
                {0.7114,  0.1308, -0.0457},
                {0.5966,  0.2614, -0.0466},
                {0.8194,  0.0014, -0.3626},
                {0.6807,  0.0215, -0.3363}
        };

        //true values from psych package in R.
        double[] true_uniqueness = {0.3677, 0.3483, 0.4171,0.5472, 0.4074, 0.3282, 0.4093, 0.6871, 0.4761, 0.5798, 0.1951, 0.4212};

        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.WLS);
        System.out.println();
        System.out.println(fa.printOutput());

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<3;j++){
//                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-2);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
//            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-2);
        }


    }


    @Test
    public void harmanGeneralizedLeastSquaresTest(){
        System.out.println("Generalized LeastSquares Factor Analysis Test: Harman data");
        RealMatrix R = new Array2DRowRealMatrix(readHarman74Data());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 4);
        fa.estimateParameters(EstimationMethod.GLS);
        System.out.println();
        System.out.println(fa.printOutput());

        //true values from psych package in R.
        //fa(Harman74.cor$cov,4, fm="gls", rotate="none").
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

//        FactorMethod factorMethod = fa.getFactorMethod();
//        for(int i=0;i<R.getColumnDimension();i++){
//            for(int j=0;j<4;j++){
//                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-3);
//            }
//        }
//
//        for(int i=0;i<R.getColumnDimension();i++){
//            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-3);
//        }

    }

    @Test
    public void m255TestGeneralizedLeastSquares(){
        System.out.println("Generalized Least Squares Factor Analysis Test: m255 data");

        //true values from psych package in R.
        //fa(r,3, fm="gls", rotate="none")
        double[][] true_loadings={
                {0.6837, -0.3971, 0.0730},
                {0.6986, -0.3801, 0.1199},
                {0.7196, -0.2332, 0.0998},
                {0.6389, -0.1924, 0.1008},
                {0.7688, -0.0401, 0.0651},
                {0.7320,  0.3360, 0.1460},
                {0.6117,  0.4198, 0.2035},
                {0.5308,  0.1715, 0.0660},
                {0.7114,  0.1308, -0.0457},
                {0.5966,  0.2614, -0.0466},
                {0.8194,  0.0014, -0.3626},
                {0.6807,  0.0215, -0.3363}
        };

        //true values from psych package in R.
        double[] true_uniqueness = {0.3677, 0.3483, 0.4171,0.5472, 0.4074, 0.3282, 0.4093, 0.6871, 0.4761, 0.5798, 0.1951, 0.4212};

        RealMatrix R = new Array2DRowRealMatrix(readM255());
        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, 3);
        fa.estimateParameters(EstimationMethod.GLS);
        System.out.println();
        System.out.println(fa.printOutput());

        FactorMethod factorMethod = fa.getFactorMethod();
        for(int i=0;i<R.getColumnDimension();i++){
            for(int j=0;j<3;j++){
                assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i,j), 4), 1e-2);
            }
        }

        for(int i=0;i<R.getColumnDimension();i++){
            assertEquals("  Uniqueness: ", true_uniqueness[i], Precision.round(factorMethod.getUniquenessAt(i), 4), 1e-2);
        }


    }


}

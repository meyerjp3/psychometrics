/*
 * Copyright 2013 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.*;
import org.junit.Test;


import java.util.LinkedHashMap;

import static junit.framework.Assert.assertEquals;

public class IrtTrueScoreEquatingTest {

    LinkedHashMap<VariableName, ItemResponseModel> itemFormX = null;
    LinkedHashMap<VariableName, ItemResponseModel> itemFormY = null;

    public IrtTrueScoreEquatingTest(){

    }

    @Test
    public  void trueScoreEquatingTest1(){
        System.out.println("True score equating test with 3PL");
        init3PLM();

        //True value obtained from Kolen's polyequate program
        double[] yequivTrue = {0.000000,0.888027,1.776054,2.66408,3.552107,4.440134,5.328161,6.134033,7.185802,8.39496,
                9.621725,10.82559,12.000215,13.149525,14.280262,15.399495,16.513518,17.627108,18.742944,
                19.861174,20.979305,22.09259,23.194994,24.280626,25.345249,26.387408,27.408805,28.413824,
                29.408303,30.397704,31.384411,32.363711,33.317925,34.209551,34.979905,35.575623,36.000000};

        double[] xthetaTrue = {-99.000000,-99.000000,-99.000000,-99.000000,-99.000000,-99.000000,-99.000000,
                -4.336128,-2.770096,-2.063286,-1.607182,-1.268170,-0.995059,-0.763274,-0.559258,-0.374740,
                -0.204264,-0.044016,0.108779,0.256195,0.399813,0.540889,0.680503,0.819717,0.959750,
                1.102159,1.249018,1.403104,1.568130,1.749146,1.953320,2.191603,2.482434,2.860365,
                3.399176,4.321431,99.000000};

        IrtTrueScoreEquating irtEq = new IrtTrueScoreEquating(itemFormX, itemFormY);
        irtEq.equateScores();

//        System.out.println(irtEq.printResults());

        double[] yequivEst = irtEq.getYEquivalentScores();
        double[] yequivTheta = irtEq.getFormXThetaValues();

        for(int i=0;i<yequivTheta.length;i++){
            assertEquals("True score equating THETA test 1: 3PLM", xthetaTrue[i], yequivTheta[i], 1e-5);
        }

        for(int i=0;i<yequivTrue.length;i++){
            assertEquals("True score equating test 1: 3PLM", yequivTrue[i], yequivEst[i], 1e-5);
        }

    }

    @Test
    public  void trueScoreEquatingTest2(){
        System.out.println("True score equating test with 3PL and GPCM");
        initMixedFormat();

        //True value obtained from polyequate program by Kolen et al.
        double[] yequivTrue = {0.000000,0.800918,1.601837,2.505823,3.769920,
            4.940334,6.044153,7.104130,8.135673,9.150035,10.154059,11.151285,
            12.143439,13.131138,14.114030,15.090912,16.060224,17.020592,
            17.971175,18.912790,19.851019,20.789404,21.701693,22.647306,24.000000};
        double[] xthetaTrue = {-99.000000,-99.000000,-99.000000,-4.351326,-2.173359,
            -1.493876,-1.055053,-0.714006,-0.431074,-0.188090,0.027123,
            0.223379,0.406995,0.582850,0.755538,0.930351,1.114001,
            1.315253,1.545813,1.822200,2.169455,2.621238,3.206924,
            4.117257,99.000000};

        IrtTrueScoreEquating irtEq = new IrtTrueScoreEquating(itemFormX, itemFormY);
        irtEq.equateScores();

//        System.out.println(irtEq.printResults());

        double[] yequivEst = irtEq.getYEquivalentScores();
        double[] yequivTheta = irtEq.getFormXThetaValues();

        for(int i=0;i<yequivTheta.length;i++){
            assertEquals("True score equating THETA test 2: Mixed-format", xthetaTrue[i], yequivTheta[i], 1e-5);
        }

        for(int i=0;i<yequivTrue.length;i++){
            assertEquals("True score equating test 2: Mixed-format", yequivTrue[i], yequivEst[i], 1e-5);
        }

    }

    @Test
    public  void trueScoreEquatingTest3(){
        System.out.println("True score equating test with 3PL and GRM");
        initMixedFormat2();

        //True value obtained from polyequate program by Kolen et al.
        double[] yequivTrue = {0.000000,0.800918,1.601837,2.481116,3.384396,4.208612,
                5.034510,5.892746,6.793426,7.733135,8.700971,9.686628,
                10.686402,11.703859,12.746813,13.823575,14.939268,
                16.092578,17.277277,18.492807,19.740730,20.918832,22.000000};
        double[] xthetaTrue = {-99.000000,-99.000000,-99.000000,-4.365042,-3.034702,-2.550979,
                -2.217423,-1.940678,-1.690257,-1.453299,-1.223588,-0.997198,
                -0.770611,-0.540377,-0.303367,-0.056779,0.202585,0.480023,
                0.786995,1.153228,1.670564,2.694141,99.000000};

        IrtTrueScoreEquating irtEq = new IrtTrueScoreEquating(itemFormX, itemFormY);
        irtEq.equateScores();

//        System.out.println(irtEq.printResults());

        double[] yequivEst = irtEq.getYEquivalentScores();
        double[] yequivTheta = irtEq.getFormXThetaValues();

        for(int i=0;i<yequivTheta.length;i++){
            assertEquals("True score equating THETA test 3: Mixed-format", xthetaTrue[i], yequivTheta[i], 1e-5);
        }

        for(int i=0;i<yequivTrue.length;i++){
            assertEquals("True score equating test 3: Mixed-format", yequivTrue[i], yequivEst[i], 1e-5);
        }

    }

    private void initMixedFormat(){
        itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("item1"), new Irm3PL(0.920353277679441,-1.181970629805,0.244001,1.7));
        itemFormX.put(new VariableName("item2"), new Irm3PL(1.170994236576,-1.111834306335,0.242883,1.7));
        itemFormX.put(new VariableName("item3"), new Irm3PL(0.609056109168193,-1.150369527755,0.260893,1.7));
        itemFormX.put(new VariableName("item4"), new Irm3PL(0.886869070441168,-0.550536364405,0.243497,1.7));
        itemFormX.put(new VariableName("item5"), new Irm3PL(1.05983303832279,-0.281302485405,0.319135,1.7));
        itemFormX.put(new VariableName("item6"), new Irm3PL(0.806179909475657,0.00388215394000008,0.277826,1.7));
        itemFormX.put(new VariableName("item7"), new Irm3PL(1.32554832150229,0.326604428895,0.157979,1.7));
        itemFormX.put(new VariableName("item8"), new Irm3PL(1.2106179296997,0.675148828855,0.084828,1.7));
        itemFormX.put(new VariableName("item9"), new Irm3PL(1.52987732052845,0.868505776025,0.181874,1.7));
        itemFormX.put(new VariableName("item10"), new Irm3PL(1.3678877449149,1.47226315786,0.246856,1.7));
        itemFormX.put(new VariableName("item11"), new Irm3PL(0.536740756166129,2.177620817575,0.309243,1.7));
        itemFormX.put(new VariableName("item12"), new Irm3PL(1.32565611774289,3.17675688572,0.192339,1.7));
        double[] step1 = {0.89576021814,-0.89576021814};
        itemFormX.put(new VariableName("item13"), new IrmGPCM2(0.330731115752338,-0.44611721271,step1,1.7));
        double[] step2 = {0.0869532364700001,-0.0869532364700001};
        itemFormX.put(new VariableName("item14"), new IrmGPCM2(1.19127830416914,0.88345078701,step2, 1.7));
        double[] step3 = {1.716223932855,-1.716223932855};
        itemFormX.put(new VariableName("item15"), new IrmGPCM2(0.464028517005469,2.379009065235,step3, 1.7));
        double[] step4 = {-0.389003497996667,0.882710239228333,-0.493706741231667};
        itemFormX.put(new VariableName("item16"), new IrmGPCM2(0.658666878992595,0.375180544696667,step4, 1.7));
        double[] step5 = {0.822498071375,-0.161448079285,-0.66104999209};
        itemFormX.put(new VariableName("item17"), new IrmGPCM2(0.679246161290125,1.53676010099,step5, 1.7));

        itemFormY.put(new VariableName("item1"), new Irm3PL(0.887276,-1.334798,0.134406,1.7));
        itemFormY.put(new VariableName("item2"), new Irm3PL(1.184412,-1.129004,0.237765,1.7));
        itemFormY.put(new VariableName("item3"), new Irm3PL(0.609412,-1.464546,0.151393,1.7));
        itemFormY.put(new VariableName("item4"), new Irm3PL(0.923812,-0.576435,0.240097,1.7));
        itemFormY.put(new VariableName("item5"), new Irm3PL(0.822776,-0.476357,0.192369,1.7));
        itemFormY.put(new VariableName("item6"), new Irm3PL(0.707818,-0.235189,0.189557,1.7));
        itemFormY.put(new VariableName("item7"), new Irm3PL(1.306976,0.242986,0.165553,1.7));
        itemFormY.put(new VariableName("item8"), new Irm3PL(1.295471,0.598029,0.090557,1.7));
        itemFormY.put(new VariableName("item9"), new Irm3PL(1.366841,0.923206,0.172993,1.7));
        itemFormY.put(new VariableName("item10"), new Irm3PL(1.389624,1.380666,0.238008,1.7));
        itemFormY.put(new VariableName("item11"), new Irm3PL(0.293806,2.02807,0.203448,1.7));
        itemFormY.put(new VariableName("item12"), new Irm3PL(0.885347,3.152928,0.195473,1.7));
        double[] step1y = {0.893232,-0.893232};
        itemFormY.put(new VariableName("item13"), new IrmGPCM2(0.346324,-0.494115,step1y,1.7));
        double[] step2y = {0.09975,-0.09975};
        itemFormY.put(new VariableName("item14"), new IrmGPCM2(1.252012,0.856264,step2y, 1.7));
        double[] step3y = {1.850498,-1.850498};
        itemFormY.put(new VariableName("item15"), new IrmGPCM2(0.392282,2.825801,step3y, 1.7));
        double[] step4y = {-0.300428333333333,0.761845666666667,-0.461417333333333};
        itemFormY.put(new VariableName("item16"), new IrmGPCM2(0.660841,0.342977333333333,step4y, 1.7));
        double[] step5y = {1.001974,-0.107221,-0.894753};
        itemFormY.put(new VariableName("item17"), new IrmGPCM2(0.669612,1.643267,step5y, 1.7));

    }

    @Test
    public  void trueScoreEquatingTest4(){
        System.out.println("True score equating test with 3PL and PCM");
        initMixedFormat3();

        //True value obtained from polyequate program by Kolen et al.
        double[] yequivTrue = {0.000000,0.800918,1.601837,2.604812,3.857490,4.989778,6.066360,7.108517,8.129894,
        9.137815,10.135757,11.126110,12.110829,13.091186,14.067759,15.040825,16.010594,16.976605,17.936671,
        18.887563,19.831627,20.786060,21.734646,22.588589,24.000000};
        double[] xthetaTrue = {-99.000000,-99.000000,-99.000000,-3.063275,-1.789319,-1.255132,-0.866778,-0.558202,
        -0.310743,-0.106866,0.070264,0.233216,0.390340,0.546898,0.706279,0.871319,1.045568,1.234259,1.445181,
        1.690393,1.991401,2.393716,2.976234,3.813960,99.000000};

        IrtTrueScoreEquating irtEq = new IrtTrueScoreEquating(itemFormX, itemFormY);
        irtEq.equateScores();

//        System.out.println(irtEq.printResults());

        double[] yequivEst = irtEq.getYEquivalentScores();
        double[] yequivTheta = irtEq.getFormXThetaValues();

        for(int i=0;i<yequivTheta.length;i++){
            assertEquals("True score equating 3PL PCM theta", xthetaTrue[i], yequivTheta[i], 1e-5);
        }

        for(int i=0;i<yequivTrue.length;i++){
            assertEquals("True score equating 3PL PCM true score", yequivTrue[i], yequivEst[i], 1e-5);
        }

    }

    private void initMixedFormat2(){
        itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("item1"), new Irm3PL(0.920353277679441,-1.181970629805,0.244001,1.7));
        itemFormX.put(new VariableName("item2"), new Irm3PL(1.170994236576,-1.111834306335,0.242883,1.7));
        itemFormX.put(new VariableName("item3"), new Irm3PL(0.609056109168193,-1.150369527755,0.260893,1.7));
        itemFormX.put(new VariableName("item4"), new Irm3PL(0.886869070441168,-0.550536364405,0.243497,1.7));
        itemFormX.put(new VariableName("item5"), new Irm3PL(1.05983303832279,-0.281302485405,0.319135,1.7));
        itemFormX.put(new VariableName("item6"), new Irm3PL(0.806179909475657,0.00388215394000008,0.277826,1.7));
        itemFormX.put(new VariableName("item7"), new Irm3PL(1.32554832150229,0.326604428895,0.157979,1.7));
        itemFormX.put(new VariableName("item8"), new Irm3PL(1.2106179296997,0.675148828855,0.084828,1.7));
        itemFormX.put(new VariableName("item9"), new Irm3PL(1.52987732052845,0.868505776025,0.181874,1.7));
        itemFormX.put(new VariableName("item10"), new Irm3PL(1.3678877449149,1.47226315786,0.246856,1.7));
        itemFormX.put(new VariableName("item11"), new Irm3PL(0.536740756166129,2.177620817575,0.309243,1.7));
        itemFormX.put(new VariableName("item12"), new Irm3PL(1.32565611774289,3.17675688572,0.192339,1.7));
        double[] step1 = {-2.141500, 0.038200};
        itemFormX.put(new VariableName("item13"), new IrmGRM(1.119600 ,step1, 1.7));
        double[] step2 = {-1.752300, -1.066000};
        itemFormX.put(new VariableName("item14"), new IrmGRM(1.229000,step2, 1.7));
        double[] step3 = {-2.312600, -1.881600};
        itemFormX.put(new VariableName("item15"), new IrmGRM(0.640500,step3, 1.7));
        double[] step4 = {-1.972800, -0.281000};
        itemFormX.put(new VariableName("item16"), new IrmGRM(1.162200,step4, 1.7));
        double[] step5 = {-2.220700, -0.825200};
        itemFormX.put(new VariableName("item17"), new IrmGRM(1.224900,step5, 1.7));

        itemFormY.put(new VariableName("item1"), new Irm3PL(0.887276,-1.334798,0.134406,1.7));
        itemFormY.put(new VariableName("item2"), new Irm3PL(1.184412,-1.129004,0.237765,1.7));
        itemFormY.put(new VariableName("item3"), new Irm3PL(0.609412,-1.464546,0.151393,1.7));
        itemFormY.put(new VariableName("item4"), new Irm3PL(0.923812,-0.576435,0.240097,1.7));
        itemFormY.put(new VariableName("item5"), new Irm3PL(0.822776,-0.476357,0.192369,1.7));
        itemFormY.put(new VariableName("item6"), new Irm3PL(0.707818,-0.235189,0.189557,1.7));
        itemFormY.put(new VariableName("item7"), new Irm3PL(1.306976,0.242986,0.165553,1.7));
        itemFormY.put(new VariableName("item8"), new Irm3PL(1.295471,0.598029,0.090557,1.7));
        itemFormY.put(new VariableName("item9"), new Irm3PL(1.366841,0.923206,0.172993,1.7));
        itemFormY.put(new VariableName("item10"), new Irm3PL(1.389624,1.380666,0.238008,1.7));
        itemFormY.put(new VariableName("item11"), new Irm3PL(0.293806,2.02807,0.203448,1.7));
        itemFormY.put(new VariableName("item12"), new Irm3PL(0.885347,3.152928,0.195473,1.7));
        double[] step1y = { -1.778600, 0.717700};
        itemFormY.put(new VariableName("item13"), new IrmGRM(0.917100,step1y,1.7));
        double[] step2y = {-1.411500, -0.494600};
        itemFormY.put(new VariableName("item14"), new IrmGRM(0.975100,step2y, 1.7));
        double[] step3y = { -1.847800, -1.407800};
        itemFormY.put(new VariableName("item15"), new IrmGRM(0.589000,step3y, 1.7));
        double[] step4y = { -1.615100, 0.300200};
        itemFormY.put(new VariableName("item16"), new IrmGRM(0.980400,step4y, 1.7));
        double[] step5y = {-1.935500, -0.226700};
        itemFormY.put(new VariableName("item17"), new IrmGRM(1.011700,step5y, 1.7));

    }


    /**
     * Item parameters from Kolen and Brennan
     */
    private void init3PLM(){
        itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();
        itemFormX.put(new VariableName("item1"), new Irm3PL(0.467344,-2.616539,0.175056,1.7));
        itemFormX.put(new VariableName("item2"), new Irm3PL(0.670924,-1.068342,0.116490,1.7));
        itemFormX.put(new VariableName("item3"), new Irm3PL(0.386976,-1.339358,0.208748,1.7));
        itemFormX.put(new VariableName("item4"), new Irm3PL(1.228024,0.064115,0.282599,1.7));
        itemFormX.put(new VariableName("item5"), new Irm3PL(0.828161,-0.701780,0.262510,1.7));
        itemFormX.put(new VariableName("item6"), new Irm3PL(0.496452,-1.511753,0.203834,1.7));
        itemFormX.put(new VariableName("item7"), new Irm3PL(0.731571,0.030429,0.322391,1.7));
        itemFormX.put(new VariableName("item8"), new Irm3PL(0.973113,-0.657195,0.220907,1.7));
        itemFormX.put(new VariableName("item9"), new Irm3PL(0.641447,-0.479277,0.159961,1.7));
        itemFormX.put(new VariableName("item10"), new Irm3PL(0.779740,0.688159,0.364807,1.7));
        itemFormX.put(new VariableName("item11"), new Irm3PL(0.815589,0.344648,0.239862,1.7));
        itemFormX.put(new VariableName("item12"), new Irm3PL(0.563967,-0.444704,0.123961,1.7));
        itemFormX.put(new VariableName("item13"), new Irm3PL(1.047915,-0.014127,0.253470,1.7));
        itemFormX.put(new VariableName("item14"), new Irm3PL(0.892128,0.422782,0.156932,1.7));
        itemFormX.put(new VariableName("item15"), new Irm3PL(0.908926,0.626040,0.298628,1.7));
        itemFormX.put(new VariableName("item16"), new Irm3PL(0.781655,0.213016,0.252099,1.7));
        itemFormX.put(new VariableName("item17"), new Irm3PL(0.759750,0.098919,0.227257,1.7));
        itemFormX.put(new VariableName("item18"), new Irm3PL(0.822383,-0.274926,0.053538,1.7));
        itemFormX.put(new VariableName("item19"), new Irm3PL(0.557961,-0.051057,0.120098,1.7));
        itemFormX.put(new VariableName("item20"), new Irm3PL(0.897580,0.610857,0.203621,1.7));
        itemFormX.put(new VariableName("item21"), new Irm3PL(0.295784,2.173474,0.148927,1.7));
        itemFormX.put(new VariableName("item22"), new Irm3PL(0.716916,0.742542,0.233231,1.7));
        itemFormX.put(new VariableName("item23"), new Irm3PL(0.947343,0.180908,0.064409,1.7));
        itemFormX.put(new VariableName("item24"), new Irm3PL(1.239634,0.700229,0.245270,1.7));
        itemFormX.put(new VariableName("item25"), new Irm3PL(0.436801,1.117559,0.142681,1.7));
        itemFormX.put(new VariableName("item26"), new Irm3PL(0.781728,0.763879,0.087920,1.7));
        itemFormX.put(new VariableName("item27"), new Irm3PL(1.599493,1.149537,0.199245,1.7));
        itemFormX.put(new VariableName("item28"), new Irm3PL(1.279241,1.270811,0.164220,1.7));
        itemFormX.put(new VariableName("item29"), new Irm3PL(0.821706,1.311960,0.143102,1.7));
        itemFormX.put(new VariableName("item30"), new Irm3PL(0.596854,2.130355,0.085290,1.7));
        itemFormX.put(new VariableName("item31"), new Irm3PL(1.075705,1.701967,0.244298,1.7));
        itemFormX.put(new VariableName("item32"), new Irm3PL(0.728471,1.511532,0.086474,1.7));
        itemFormX.put(new VariableName("item33"), new Irm3PL(1.197162,1.325326,0.078897,1.7));
        itemFormX.put(new VariableName("item34"), new Irm3PL(0.493857,3.580113,0.139884,1.7));
        itemFormX.put(new VariableName("item35"), new Irm3PL(0.787070,3.165387,0.108961,1.7));
        itemFormX.put(new VariableName("item36"), new Irm3PL(1.104752,2.034859,0.107530,1.7));

        itemFormY.put(new VariableName("item1"), new Irm3PL(0.870350,-1.450715,0.157647,1.7));
        itemFormY.put(new VariableName("item2"), new Irm3PL(0.462772,-0.406996,0.109378,1.7));
        itemFormY.put(new VariableName("item3"), new Irm3PL(0.441595,-1.334933,0.155883,1.7));
        itemFormY.put(new VariableName("item4"), new Irm3PL(0.544796,-0.901734,0.138071,1.7));
        itemFormY.put(new VariableName("item5"), new Irm3PL(0.619973,-1.486483,0.211368,1.7));
        itemFormY.put(new VariableName("item6"), new Irm3PL(0.572995,-1.321004,0.191298,1.7));
        itemFormY.put(new VariableName("item7"), new Irm3PL(1.175228,0.069050,0.294746,1.7));
        itemFormY.put(new VariableName("item8"), new Irm3PL(0.445023,0.232402,0.272323,1.7));
        itemFormY.put(new VariableName("item9"), new Irm3PL(0.598719,-0.709831,0.117663,1.7));
        itemFormY.put(new VariableName("item10"), new Irm3PL(0.847924,-0.425342,0.144462,1.7));
        itemFormY.put(new VariableName("item11"), new Irm3PL(1.031996,-0.818383,0.093584,1.7));
        itemFormY.put(new VariableName("item12"), new Irm3PL(0.604125,-0.353942,0.081759,1.7));
        itemFormY.put(new VariableName("item13"), new Irm3PL(0.829722,-0.019137,0.128310,1.7));
        itemFormY.put(new VariableName("item14"), new Irm3PL(0.725171,-0.315511,0.085425,1.7));
        itemFormY.put(new VariableName("item15"), new Irm3PL(0.990164,0.531956,0.302443,1.7));
        itemFormY.put(new VariableName("item16"), new Irm3PL(0.774935,0.539442,0.217930,1.7));
        itemFormY.put(new VariableName("item17"), new Irm3PL(0.594230,0.898656,0.229885,1.7));
        itemFormY.put(new VariableName("item18"), new Irm3PL(0.808079,-0.115649,0.064791,1.7));
        itemFormY.put(new VariableName("item19"), new Irm3PL(0.964044,-0.194763,0.163258,1.7));
        itemFormY.put(new VariableName("item20"), new Irm3PL(0.783557,0.350592,0.129939,1.7));
        itemFormY.put(new VariableName("item21"), new Irm3PL(0.413973,2.553812,0.240967,1.7));
        itemFormY.put(new VariableName("item22"), new Irm3PL(0.761758,-0.158110,0.113708,1.7));
        itemFormY.put(new VariableName("item23"), new Irm3PL(1.195895,0.505649,0.239728,1.7));
        itemFormY.put(new VariableName("item24"), new Irm3PL(1.355437,0.581109,0.224322,1.7));
        itemFormY.put(new VariableName("item25"), new Irm3PL(1.186899,0.622889,0.257697,1.7));
        itemFormY.put(new VariableName("item26"), new Irm3PL(1.029556,0.389830,0.185611,1.7));
        itemFormY.put(new VariableName("item27"), new Irm3PL(1.041731,0.939158,0.165121,1.7));
        itemFormY.put(new VariableName("item28"), new Irm3PL(1.205473,1.135046,0.232287,1.7));
        itemFormY.put(new VariableName("item29"), new Irm3PL(0.969740,0.697642,0.107035,1.7));
        itemFormY.put(new VariableName("item30"), new Irm3PL(0.633562,1.896027,0.079396,1.7));
        itemFormY.put(new VariableName("item31"), new Irm3PL(1.082216,1.386423,0.185511,1.7));
        itemFormY.put(new VariableName("item32"), new Irm3PL(1.019458,0.919670,0.102719,1.7));
        itemFormY.put(new VariableName("item33"), new Irm3PL(1.134661,1.079013,0.063009,1.7));
        itemFormY.put(new VariableName("item34"), new Irm3PL(1.194845,1.841148,0.099913,1.7));
        itemFormY.put(new VariableName("item35"), new Irm3PL(1.196146,2.029683,0.083187,1.7));
        itemFormY.put(new VariableName("item36"), new Irm3PL(0.925521,2.133706,0.125873,1.7));

    }

    private void initMixedFormat3(){
        itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("item1"), new Irm3PL(0.920353277679441,-1.181970629805,0.244001,1.7));
        itemFormX.put(new VariableName("item2"), new Irm3PL(1.170994236576,-1.111834306335,0.242883,1.7));
        itemFormX.put(new VariableName("item3"), new Irm3PL(0.609056109168193,-1.150369527755,0.260893,1.7));
        itemFormX.put(new VariableName("item4"), new Irm3PL(0.886869070441168,-0.550536364405,0.243497,1.7));
        itemFormX.put(new VariableName("item5"), new Irm3PL(1.05983303832279,-0.281302485405,0.319135,1.7));
        itemFormX.put(new VariableName("item6"), new Irm3PL(0.806179909475657,0.00388215394000008,0.277826,1.7));
        itemFormX.put(new VariableName("item7"), new Irm3PL(1.32554832150229,0.326604428895,0.157979,1.7));
        itemFormX.put(new VariableName("item8"), new Irm3PL(1.2106179296997,0.675148828855,0.084828,1.7));
        itemFormX.put(new VariableName("item9"), new Irm3PL(1.52987732052845,0.868505776025,0.181874,1.7));
        itemFormX.put(new VariableName("item10"), new Irm3PL(1.3678877449149,1.47226315786,0.246856,1.7));
        itemFormX.put(new VariableName("item11"), new Irm3PL(0.536740756166129,2.177620817575,0.309243,1.7));
        itemFormX.put(new VariableName("item12"), new Irm3PL(1.32565611774289,3.17675688572,0.192339,1.7));
        double[] step1 = {-0.89576021814,0.89576021814};
        itemFormX.put(new VariableName("item13"), new IrmPCM(-0.44611721271,step1,1.7));
        double[] step2 = {-0.0869532364700001,0.0869532364700001};
        itemFormX.put(new VariableName("item14"), new IrmPCM(0.88345078701,step2, 1.7));
        double[] step3 = {-1.716223932855,1.716223932855};
        itemFormX.put(new VariableName("item15"), new IrmPCM(2.379009065235,step3, 1.7));
        double[] step4 = {0.389003497996667,-0.882710239228333,0.493706741231667};
        itemFormX.put(new VariableName("item16"), new IrmPCM(0.375180544696667,step4, 1.7));
        double[] step5 = {-0.822498071375,0.161448079285,0.66104999209};
        itemFormX.put(new VariableName("item17"), new IrmPCM(1.53676010099,step5, 1.7));

        itemFormY.put(new VariableName("item1"), new Irm3PL(0.887276,-1.334798,0.134406,1.7));
        itemFormY.put(new VariableName("item2"), new Irm3PL(1.184412,-1.129004,0.237765,1.7));
        itemFormY.put(new VariableName("item3"), new Irm3PL(0.609412,-1.464546,0.151393,1.7));
        itemFormY.put(new VariableName("item4"), new Irm3PL(0.923812,-0.576435,0.240097,1.7));
        itemFormY.put(new VariableName("item5"), new Irm3PL(0.822776,-0.476357,0.192369,1.7));
        itemFormY.put(new VariableName("item6"), new Irm3PL(0.707818,-0.235189,0.189557,1.7));
        itemFormY.put(new VariableName("item7"), new Irm3PL(1.306976,0.242986,0.165553,1.7));
        itemFormY.put(new VariableName("item8"), new Irm3PL(1.295471,0.598029,0.090557,1.7));
        itemFormY.put(new VariableName("item9"), new Irm3PL(1.366841,0.923206,0.172993,1.7));
        itemFormY.put(new VariableName("item10"), new Irm3PL(1.389624,1.380666,0.238008,1.7));
        itemFormY.put(new VariableName("item11"), new Irm3PL(0.293806,2.02807,0.203448,1.7));
        itemFormY.put(new VariableName("item12"), new Irm3PL(0.885347,3.152928,0.195473,1.7));
        double[] step1y = {-0.893232,0.893232};
        itemFormY.put(new VariableName("item13"), new IrmPCM(-0.494115,step1y,1.7));
        double[] step2y = {-0.09975,0.09975};
        itemFormY.put(new VariableName("item14"), new IrmPCM(0.856264,step2y, 1.7));
        double[] step3y = {-1.850498,1.850498};
        itemFormY.put(new VariableName("item15"), new IrmPCM(2.825801,step3y, 1.7));
        double[] step4y = {0.300428333333333,-0.761845666666667,0.461417333333333};
        itemFormY.put(new VariableName("item16"), new IrmPCM(0.342977333333333,step4y, 1.7));
        double[] step5y = {-1.001974,0.107221,0.894753};
        itemFormY.put(new VariableName("item17"), new IrmPCM(1.643267,step5y, 1.7));

    }



}

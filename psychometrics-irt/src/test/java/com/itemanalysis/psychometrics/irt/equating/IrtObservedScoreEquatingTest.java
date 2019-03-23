package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmGRM;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.quadrature.UserSuppliedQuadratureRule;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class IrtObservedScoreEquatingTest {


    /**
     * Kolen and Brennan Chapter 6 Example
     *
     */
    @Test
    public void binaryItemsTest(){
        System.out.println("IRT Observed Score Equating Test: Binary Items");
        double[][] iParamX = {
                {0.46732,-2.61642,0.1751},
                {0.67096,-1.06823,0.1165},
                {0.38697,-1.33932,0.2087},
                {1.22807,0.06421,0.2826},
                {0.82818,-0.70177,0.2625},
                {0.49648,-1.51173,0.2038},
                {0.73159,0.03046,0.3224},
                {0.97315,-0.6572,0.2209},
                {0.64146,-0.47926,0.1600},
                {0.77971,0.68823,0.3648},
                {0.81559,0.3447,0.2399},
                {0.56399,-0.44468,0.1240},
                {1.04789,-0.01412,0.2535},
                {0.89212,0.42279,0.1569},
                {0.90896,0.62602,0.2986},
                {0.78167,0.2131,0.2521},
                {0.75973,0.0989,0.2273},
                {0.8224,-0.27485,0.0535},
                {0.55796,-0.03929,0.1201},
                {0.89756,0.61085,0.2036},
                {0.29581,2.1735,0.1489},
                {0.71696,0.74257,0.2332},
                {0.94739,0.18099,0.0644},
                {1.23963,0.70023,0.2453},
                {0.43679,1.11762,0.1427},
                {0.78175,0.76386,0.0879},
                {1.59947,1.14961,0.1992},
                {1.27926,1.27086,0.1642},
                {0.82172,1.31202,0.1431},
                {0.5969,2.13034,0.0853},
                {1.0757,1.70201,0.2443},
                {0.72844,1.5116,0.0865},
                {1.1972,1.32531,0.0789},
                {0.49385,3.58008,0.1399},
                {0.78711,3.1654,0.1090},
                {1.10478,2.03484,0.1075}
        };

        double[][] iParamY = {
                {0.8704,-1.4507,0.1576},
                {0.4628,-0.4070,0.1094},
                {0.4416,-1.3349,0.1559},
                {0.5448,-0.9017,0.1381},
                {0.6200,-1.4865,0.2114},
                {0.5730,-1.3210,0.1913},
                {1.1752,0.0691,0.2947},
                {0.4450,0.2324,0.2723},
                {0.5987,-0.7098,0.1177},
                {0.8479,-0.4253,0.1445},
                {1.0320,-0.8184,0.0936},
                {0.6041,-0.3539,0.0818},
                {0.8297,-0.0191,0.1283},
                {0.7252,-0.3155,0.0854},
                {0.9902,0.5320,0.3024},
                {0.7749,0.5394,0.2179},
                {0.5942,0.8987,0.2299},
                {0.8081,-0.1156,0.0648},
                {0.9640,-0.1948,0.1633},
                {0.7836,0.3506,0.1299},
                {0.4140,2.5538,0.2410},
                {0.7618,-0.1581,0.1137},
                {1.1959,0.5056,0.2397},
                {1.3554,0.5811,0.2243},
                {1.1869,0.6229,0.2577},
                {1.0296,0.3898,0.1856},
                {1.0417,0.9392,0.1651},
                {1.2055,1.1350,0.2323},
                {0.9697,0.6976,0.1070},
                {0.6336,1.8960,0.0794},
                {1.0822,1.3864,0.1855},
                {1.0195,0.9197,0.1027},
                {1.1347,1.0790,0.0630},
                {1.1948,1.8411,0.0999},
                {1.1961,2.0297,0.0832},
                {0.9255,2.1337,0.1259}
        };

        int nItems = 36;
        ItemResponseModel[] irmX = new ItemResponseModel[nItems];
        ItemResponseModel[] irmY = new ItemResponseModel[nItems];

        for(int i=0;i<nItems;i++){
            irmX[i] = new Irm3PL(iParamX[i][0], iParamX[i][1], iParamX[i][2], 1.7);
            irmY[i] = new Irm3PL(iParamY[i][0], iParamY[i][1], iParamY[i][2], 1.7);
        }

        double[][] distX = {
                {-5.208486E+000, 1.010000E-004},
                {-4.162956E+000, 2.760000E-003},
                {-3.117426E+000, 3.021000E-002},
                {-2.071895E+000, 1.420000E-001},
                {-1.026365E+000, 3.149000E-001},
                {1.798905E-002, 3.158000E-001},
                {1.063519E+000, 1.542000E-001},
                {2.109050E+000, 3.596000E-002},
                {3.154580E+000, 3.925000E-003},
                {4.200110E+000, 1.860000E-004}
        };

        double[][] distY = {
                {-4.0000, 0.000117},
                {-3.1110, 0.003242},
                {-2.2220, 0.034490},
                {-1.3330, 0.147100},
                {-0.4444, 0.314800},
                {0.4444, 0.311000},
                {1.3330, 0.152600},
                {2.2220, 0.034060},
                {3.1110, 0.002510},
                {4.0000, 0.000112},
        };

        //True results obtained from EquitingRecipes example
        double[] trueEquatedValues = {
                -0.34282,0.61797,1.58023,2.54607,3.51863,4.50272,
                5.50491,6.53186,7.58611,8.66206,9.74833,10.83677,
                11.93076,13.04597,14.19741,15.37004,16.51341,17.59764,
                18.64402,19.67901,20.73909,21.8786,23.10523,24.29262,
                25.36488,26.36725,27.34587,28.32431,29.32187,30.35358,
                31.38002,32.34828,33.28258,34.20073,35.07631,35.85308,36.39047
        };

        QuadratureRule quadX = new UserSuppliedQuadratureRule(distX);
        QuadratureRule quadY = new UserSuppliedQuadratureRule(distY);

        IrtObservedScoreEquating irtObservedScoreEquating = new IrtObservedScoreEquating(irmX, quadX, irmY, quadY, 1.0);
        irtObservedScoreEquating.equateScores();

//        System.out.println(irtObservedScoreEquating.toString());
//        System.out.println();
//        System.out.println();
//        System.out.println(irtObservedScoreEquating.printDistributionSummary());
//        System.out.println();
//        System.out.println();
//        System.out.println(irtObservedScoreEquating.printFormXDistribution());
//        System.out.println();
//        System.out.println();
//        System.out.println(irtObservedScoreEquating.printFormYDistribution());


        for(int i=0;i<trueEquatedValues.length;i++){
            assertEquals("  Score " + i, trueEquatedValues[i], irtObservedScoreEquating.getYEquivalentObservedScoreAt(i), 1e-5);
        }


    }

    /**
     * from Equating Recipes example MainCh16Mf
     *
     */
    @Test
    public void mixedItemsTest(){
        System.out.println("IRT Observed Score Equating Test: Mixed Format Items");

        double[][] iParamX = {
                {0.88597,  -1.54199,   0.12400,},
                {0.52952,  -0.38306,   0.13610,},
                {0.51058,  -0.88696,   0.12760,},
                {0.56305,  -1.56736,   0.16190,},
                {0.81488,  -0.04510,   0.20500,},
                {0.47647,  -0.25802,   0.11680,},
                {0.80911,  -0.46633,   0.10530,},
                {1.32018,  -0.76799,   0.12020,},
                {0.74041,   0.10628,   0.13200,},
                {1.03178,  -0.13457,   0.14930,},
                {0.92230,  -1.86453,   0.78146,  1.53033},
                {1.01242,  -1.39207,  -0.55895,  1.16397},
                {0.52763,  -2.07223,  -1.54903,  1.67673},
                {0.95739,  -1.65974,   0.39398,  2.11739},
                {1.00904,  -1.96067,  -0.26664,  1.91284}
        };

        double[][] iParamY = {
                {0.7444, -1.5617,  0.1609,},
                {0.5562, -0.1031,  0.1753,},
                {0.5262, -1.0676,  0.1602,},
                {0.6388, -1.3880,  0.1676,},
                {0.8793, -0.2051,  0.1422,},
                {0.4105,  0.0555,  0.2120,},
                {0.7686, -0.3800,  0.2090,},
                {1.0539, -0.7570,  0.1270,},
                {0.7400,  0.0667,  0.1543,},
                {0.7479,  0.0281,  0.1489,},
                {0.9171, -1.7786,  0.7177,  1.45011},
                {0.9751, -1.4115, -0.4946,  1.15969},
                {0.5890, -1.8478, -1.4078,  1.51339},
                {0.9804, -1.6151,  0.3002,  2.04728},
                {1.0117, -1.9355, -0.2267,  1.88991}
        };

        double[][] distX = {
                {-4.084193E+000,   2.627000E-004},
                {-3.022009E+000,   4.983000E-003},
                {-1.959824E+000,   3.490000E-002},
                {-8.988534E-001,   1.430000E-001},
                {1.635739E-001,   2.939000E-001},
                {1.225516E+000,   3.215000E-001},
                {2.287700E+000,   1.630000E-001},
                {3.349885E+000,   3.470000E-002},
                {4.410855E+000,   3.656000E-003},
                {5.473040E+000,   1.511000E-004}
        };

        double[][] distY = {
                {-0.4093E+01,  0.1323E-03},
                {-0.3185E+01,  0.3191E-02},
                {-0.2277E+01,  0.3153E-01},
                {-0.1370E+01,  0.1463E+00},
                {-0.4619E+00,  0.3138E+00},
                {0.4459E+00,  0.3182E+00},
                {0.1354E+01,  0.1534E+00},
                {0.2261E+01,  0.3096E-01},
                {0.3169E+01,  0.2480E-02},
                {0.4077E+01,  0.9835E-04}
        };

        //From Equating Recipes
        double[] trueEquatedValues = {0.11673,1.11392,2.10105,3.08675,4.07555,
                5.06551,6.05239,7.03633,8.02307,9.01550,
                10.0100,10.99953,11.97886,12.95479,13.94250,
                14.94580,15.95938,16.97872,18.00032,19.02026,
                20.03690,21.05122,22.06333,23.06807,24.06021,
                25.03341};

        int nItems = 15;
        ItemResponseModel[] irmX = new ItemResponseModel[nItems];
        ItemResponseModel[] irmY = new ItemResponseModel[nItems];

        for(int i=0;i<10;i++){
            irmX[i] = new Irm3PL(iParamX[i][0], iParamX[i][1], iParamX[i][2], 1.7);
            irmY[i] = new Irm3PL(iParamY[i][0], iParamY[i][1], iParamY[i][2], 1.7);
        }

        int index = 10;
        for(int i=0;i<5;i++){
            index = 10+i;
            irmX[index] = new IrmGRM(iParamX[index][0], Arrays.copyOfRange(iParamX[index], 1, iParamX[index].length), 1.7);
            irmY[index] = new IrmGRM(iParamY[index][0], Arrays.copyOfRange(iParamY[index], 1, iParamY[index].length), 1.7);
        }

        QuadratureRule quadX = new UserSuppliedQuadratureRule(distX);
        QuadratureRule quadY = new UserSuppliedQuadratureRule(distY);

        IrtObservedScoreEquating irtObservedScoreEquating = new IrtObservedScoreEquating(irmX, quadX, irmY, quadY, 1.0);
        irtObservedScoreEquating.equateScores();

//        System.out.println(irtObservedScoreEquating.toString());
//        System.out.println();
//        System.out.println();
//        System.out.println(irtObservedScoreEquating.printDistributionSummary());
//        System.out.println();
//        System.out.println();
//        System.out.println(irtObservedScoreEquating.printFormXDistribution());
//        System.out.println();
//        System.out.println();
//        System.out.println(irtObservedScoreEquating.printFormYDistribution());


        for(int i=0;i<trueEquatedValues.length;i++){
            assertEquals("  Score " + i, trueEquatedValues[i], irtObservedScoreEquating.getYEquivalentObservedScoreAt(i), 1e-5);
        }

    }


}
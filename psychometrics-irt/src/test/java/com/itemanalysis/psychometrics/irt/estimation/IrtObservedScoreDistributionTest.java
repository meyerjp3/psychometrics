package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.*;
import com.itemanalysis.psychometrics.quadrature.*;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.quadrature.UniformQuadratureRule;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class IrtObservedScoreDistributionTest {


    @Test
    public void binaryItemTest1(){

        ItemResponseModel[] irm = new ItemResponseModel[3];
        irm[0] = new Irm3PL(0.5, -1.0, 1.0);
        irm[1] = new Irm3PL(1.0,  0.0, 1.0);
        irm[2] = new Irm3PL(1.5,  1.0, 1.0);

        double[] points = {-3, -2, -1, 0, 1, 2, 3};
        double[] density = {1, 1, 1, 1, 1, 1, 1};
        QuadratureRule latentDistribution = new UserSuppliedQuadratureRule(points, density);

        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.computeAllBinaryItems();
        System.out.println(irtObservedScoreDistribution.toString());


    }

    @Test
    public void polytomousItemTest1(){

        ItemResponseModel[] irm = new ItemResponseModel[3];
        irm[0] = new Irm3PL(0.5, -1.0, 1.0);
        irm[1] = new Irm3PL(1.0,  0.0, 1.0);
        irm[2] = new Irm3PL(1.5,  1.0, 1.0);

        double[] points = {-3, -2, -1, 0, 1, 2, 3};
        double[] density = {1, 1, 1, 1, 1, 1, 1};
        QuadratureRule latentDistribution = new UserSuppliedQuadratureRule(points, density);

        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();
        System.out.println(irtObservedScoreDistribution.toString());


        for(int i=0;i<4;i++){
            System.out.println("Score: " + i + "  Theta: " + irtObservedScoreDistribution.getEAP(i));
        }
    }

    @Test
    public void binaryItemsTest(){
        int nItems = 50;

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, nItems, true);

        double[][] parscale_param = {
                {0.77134,-1.20157,0.21826},
                {0.92653,1.06962,0.13582},
                {0.7394,0.66486,0.26312},
                {1.27052,-0.68461,0.14705},
                {1.15402,-0.06561,0.27267},
                {0.85473,-1.11052,0.00000},
                {0.87896,-0.84055,0.13343},
                {0.9351,1.09924,0.28928},
                {0.80445,-1.29691,0.14567},
                {1.70099,-1.82461,0.23838},
                {0.84604,-1.8712,0.20175},
                {1.3569,1.77424,0.11301},
                {0.73857,0.17206,0.14577},
                {0.90448,-0.04446,0.16733},
                {1.06378,0.66933,0.20617},
                {1.17214,-1.69306,0.18765},
                {1.19297,-0.836,0.33433},
                {0.96405,-0.02601,0.25501},
                {1.12433,-0.1781,0.23934},
                {1.04734,0.07057,0.22328},
                {1.06249,-2.96365,0.20138},
                {0.87757,-1.38206,0.33482},
                {1.06221,-0.75069,0.18338},
                {0.77941,0.15689,0.27037},
                {0.97511,1.17067,0.18709},
                {0.8666,-1.02596,0.16801},
                {0.6142,2.87065,0.24542},
                {0.92965,-0.20217,0.11476},
                {0.90248,-0.4771,0.09884},
                {1.07959,0.19929,0.15952},
                {0.82947,-0.22282,0.15908},
                {1.00463,-0.4113,0.25502},
                {1.03297,-0.9803,0.26221},
                {0.84426,1.41334,0.15932},
                {0.95875,-1.22485,0.17769},
                {0.70148,-0.14216,0.3191},
                {1.15302,0.00717,0.1719},
                {0.89399,-0.06892,0.23587},
                {1.14904,0.92012,0.12101},
                {1.14389,2.10656,0.24905},
                {0.92095,-0.44256,0.28191},
                {0.73603,-1.99767,0.20534},
                {0.96288,0.54437,0.16837},
                {1.69054,-0.53875,0.20511},
                {0.86499,0.7092,0.13796},
                {0.66786,-0.16466,0.24747},
                {1.1017,-0.65105,0.23805},
                {1.13996,1.50206,0.11545},
                {0.90298,0.26238,0.11898},
                {0.86469,0.13259,0.10187}
        };

        //Create array of item response models using estimate parameters with PARSCALE
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        Irm3PL pl3 = null;
        for(int j=0;j<nItems;j++) {
            pl3 = new Irm3PL(parscale_param[j][0], parscale_param[j][1], parscale_param[j][2], 1.7);
            pl3.setName(new VariableName("Item" + (j+1)));
            irm[j] = pl3;
        }

        //Estimate person ability using EAP
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(-4.0, 4.0, 40);

        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();

        System.out.println(irtObservedScoreDistribution.toString());

    }

    @Test
    public void partialCreditModelTest(){
        System.out.println("Partial Credit Model Observed score quadrature test");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/polytomous-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 10, false);

        double[][] iclResults = {
                {-1.615151,	-0.828272, -0.175557},
                {-0.840549,	 0.122161,  0.455346},
                {-0.275783,	 0.613754,	0.608290},
                { 1.746639,	 1.953948,	2.806882},
                {-1.027968,	-0.510072,	0.298653},
                {-1.144467,	-0.732601,	0.144386},
                {-0.983459,	-0.214507, -0.247833},
                {-0.334857,	 0.227483,	1.362519},
                {-1.150395,	-0.197041,	0.153197},
                {-1.169602,	-0.829530, -0.360586}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[10];
        IrmPCM2 pcm = null;
        double[] initialStep = {0,0,0,0};
        for(int j=0;j<10;j++) {
            pcm = new IrmPCM2(iclResults[j], 1.0);
            pcm.setName(new VariableName("Item" + (j+1)));
            irm[j] = pcm;
        }

        //ICL default quadrature
        UniformQuadratureRule latentDistribution = new UniformQuadratureRule(-6.0, 6.0, 49);

        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();

        System.out.println(irtObservedScoreDistribution.toString());


    }

    /**
     * Test using 2PL item parameters ob tained from flexmirt example 2_1
     * IRT observed score quadrature also obtained from flexmirt.
     *
     * <Project>
     * Title = "2PLM example";
     * Description = "12 items 1 Factor, 1 Group 2PLM Calibration";
     *
     * <Options>
     * Mode = Calibration;
     * saveSCO = Yes;
     * Score = SSC;
     * savePRM = Yes;
     * SlopeThreshold = Yes;
     *
     * <Groups>
     * %Group1%
     * File = ".\Example_2-1\g341-19.dat";
     * Varnames = v1,v2,v3,v4,v5,v6,v7,v8,v9,v10,v11,v12;
     * N = 2844;
     * Ncats(v1-v12) = 2;
     * Model(v1-v12) = Graded(2);
     *
     * <Constraints>
     *
     */
    @Test
    public void flexmirtTest1(){
        System.out.println("IRT observed scores flexmirt test 1");

        //parameter estimates obtained via flexmirt
        double[][] param = {
                {-0.2868420, 1.0513570},
                {-2.3489300, 1.2258278},
                {0.0627025, 0.8372044},
                {-0.9001585, 1.0418483},
                {-0.3859039, 0.8465276},
                {-1.5557775, 1.3406445},
                {-2.2584127, 1.9003622},
                {-1.5556672, 0.9700110},
                {-1.7775588, 1.8872104},
                {-1.8187792, 1.3189691},
                {0.2978713, 0.8674706},
                {-0.1531911, 1.0099521}
        };

        double[][] flexmirt = {
                {-2.761,0.0012846},
                {-2.409,0.0041742},
                {-2.086,0.0091247},
                {-1.779,0.0171040},
                {-1.478,0.0296780},
                {-1.176,0.0488287},
                {-0.869,0.0759942},
                {-0.555,0.1100612},
                {-0.230,0.1453947},
                {0.114,0.1714536},
                {0.484,0.1743647},
                {0.889,0.1411944},
                {1.338,0.0713431}
        };

        ItemResponseModel[] irm = new ItemResponseModel[12];
        IrmBinary item = null;
        for(int i=0;i<12;i++){
            item = new IrmBinary(param[i][1], param[i][0]);
            irm[i] = item;
        }

        //flexmirt default quadrature
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(-6.0, 6.0, 49);

        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();

        System.out.println(irtObservedScoreDistribution.toString());

        for(int i=0;i<12;i++){
            assertEquals(" EAP for score " + (i+1) + ": ", flexmirt[i][0], irtObservedScoreDistribution.getEAP(i), 1e-3);
            assertEquals(" Prob for score " + (i+1) + ": ", flexmirt[i][1], irtObservedScoreDistribution.getDensityAt(i), 1e-3);
        }

    }

    /**
     * Test using GPCM and compare to flexmirt
     * flexmirt results obtained via:
     *
     * <Project>
     * Title = "jalabert example data";
     * Description = "All polytomous items with four categories";
     *
     * <Options>
     * Mode = Calibration;
     * saveSCO = Yes;
     * Score = SSC;
     * savePRM = Yes;
     * saveINF = Yes;
     *
     * <Groups>
     * %Group1%
     * File = "jalabert-jan15.csv";
     * Varnames = v1-v20;
     * N = 1000;
     * Ncats(v1-v20) = 4;
     * Model(v1-v20) = GPC(4);
     *
     * <Constraints>
     *
     */
    @Test
    public void flexmirtTest2(){
        System.out.println("IRT observed scores flexmirt test 1");

        //parameter estimates obtained via flexmirt
        double[] discrim = {0.8394110,0.7102702,1.7039103,1.4830470,0.8997456,0.8253240,0.9683615,1.2983270,1.2805537,
                0.8031567,0.6862014,1.2226806,1.0817097,1.0218629,1.1329739,2.3774277,0.9701314,1.1953485,0.8509222,1.3877313};

        //parameter estimates obtained via flexmirt
        double[][] steps = {
                {0,-1.3060559,0.56056889,2.22146303},
                {0,-1.40989994,-0.35107515,2.44159988},
                {0,-2.37941184,-0.54161875,1.89071227},
                {0,-2.20242392,0.10156236,2.4350894},
                {0,-2.15299582,0.3582214,2.41711733},
                {0,-1.07979372,-0.44671346,2.2774479},
                {0,-2.33621766,-0.85396376,2.45347958},
                {0,-2.24003252,0.17744523,1.49137449},
                {0,-2.13580827,-0.275253,1.52042755},
                {0,-1.57293473,0.53992416,1.95820337},
                {0,-1.34337148,0.61110075,2.27082559},
                {0,-2.1696931,-0.347896,2.32252908},
                {0,-1.23592978,0.83746712,1.39476601},
                {0,-2.01038227,0.75318445,1.17156804},
                {0,-1.70110802,0.20741073,1.63270323},
                {0,-1.13278944,0.80443177,2.12201171},
                {0,-1.06952395,0.59690567,2.36683879},
                {0,-2.02145233,0.16970583,1.29051001},
                {0,-1.05601695,-0.32656962,1.33890878},
                {0,-1.10822402,-0.38249974,1.27934624}
        };

        //from flexmirt
        //col1=score, col2=eap, col3=sd, col4=prob
        double[][] flexmirt = {
                {0,-3.468,0.496,0.0000483},
                {1,-3.228,0.464,0.0001892},
                {2,-3.015,0.436,0.0004516},
                {3,-2.823,0.414,0.0008536},
                {4,-2.648,0.396,0.0014089},
                {5,-2.486,0.38,0.002128},
                {6,-2.337,0.368,0.0030187},
                {7,-2.196,0.357,0.0040848},
                {8,-2.063,0.347,0.0053247},
                {9,-1.937,0.339,0.0067309},
                {10,-1.817,0.332,0.0082901},
                {11,-1.701,0.326,0.0099845},
                {12,-1.589,0.321,0.0117932},
                {13,-1.481,0.316,0.0136941},
                {14,-1.376,0.312,0.0156649},
                {15,-1.273,0.309,0.0176842},
                {16,-1.172,0.306,0.0197305},
                {17,-1.074,0.303,0.021782},
                {18,-0.977,0.301,0.0238152},
                {19,-0.881,0.299,0.0258045},
                {20,-0.787,0.298,0.0277214},
                {21,-0.694,0.296,0.0295351},
                {22,-0.602,0.295,0.0312127},
                {23,-0.511,0.294,0.0327208},
                {24,-0.421,0.293,0.0340267},
                {25,-0.331,0.293,0.0351002},
                {26,-0.242,0.292,0.0359153},
                {27,-0.153,0.292,0.0364518},
                {28,-0.065,0.291,0.0366969},
                {29,0.023,0.291,0.0366459},
                {30,0.111,0.29,0.0363029},
                {31,0.198,0.29,0.0356807},
                {32,0.286,0.29,0.0347997},
                {33,0.374,0.29,0.0336865},
                {34,0.461,0.29,0.0323719},
                {35,0.549,0.29,0.0308888},
                {36,0.638,0.29,0.0292702},
                {37,0.726,0.29,0.0275473},
                {38,0.815,0.291,0.0257487},
                {39,0.905,0.292,0.0238997},
                {40,0.995,0.293,0.0220225},
                {41,1.087,0.295,0.0201367},
                {42,1.179,0.296,0.0182604},
                {43,1.273,0.298,0.0164107},
                {44,1.368,0.301,0.0146046},
                {45,1.465,0.304,0.0128591},
                {46,1.565,0.307,0.0111907},
                {47,1.667,0.311,0.0096152},
                {48,1.772,0.316,0.0081464},
                {49,1.881,0.321,0.0067952},
                {50,1.993,0.327,0.0055692},
                {51,2.111,0.335,0.0044721},
                {52,2.234,0.343,0.0035042},
                {53,2.363,0.353,0.0026634},
                {54,2.499,0.365,0.0019464},
                {55,2.645,0.38,0.0013499},
                {56,2.8,0.397,0.0008711},
                {57,2.968,0.417,0.0005069},
                {58,3.151,0.44,0.0002522},
                {59,3.353,0.467,0.0000966},
                {60,3.577,0.499,0.0000217}
        };

        int ni = discrim.length;

        ItemResponseModel[] irm = new ItemResponseModel[ni];
        IrmGPCM item = null;
        for(int i=0;i<ni;i++){
            item = new IrmGPCM(discrim[i], steps[i], 1.0);
            irm[i] = item;
        }

        //flexmirt default quadrature
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(-6.0, 6.0, 49);

        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();

        //System.out.println(irtObservedScoreDistribution.toString());
        for(int i=0;i<ni;i++){
            assertEquals(" EAP for score " + (i+1) + ": ", flexmirt[i][1], irtObservedScoreDistribution.getEAP(i), 1e-3);
            assertEquals(" Prob for score " + (i+1) + ": ", flexmirt[i][3], irtObservedScoreDistribution.getDensityAt(i), 1e-5);
        }

    }



}
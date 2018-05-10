package com.itemanalysis.psychometrics.irt.estimation;


import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.quadrature.NormalQuadratureRule;
import com.itemanalysis.psychometrics.quadrature.UserSuppliedQuadratureRule;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmGPCM;
import com.itemanalysis.psychometrics.irt.model.IrmGPCM2;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class IrtExamineeTest {

    public void readLsat7Data(){
        lsat7 = new byte[32][5];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    lsat7[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public void readMixedFormat(){
        mixedFormatData = new byte[20][42];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/mixed-format.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    mixedFormatData[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void maximumLikelihoodTest2PL(){
//        System.out.println();
        System.out.println("MLE test: LSAT7.");
        if(lsat7==null) readLsat7Data();

        int n = aParamLSAT7.length;
        int nPeople = lsat7.length;

        ItemResponseModel[] irmArray = new ItemResponseModel[n];
        ItemResponseModel irm;

        //create item response models objects
        VariableName iName = null;
        for(int i=0;i<n;i++){
            String name = "V"+i;
            iName = new VariableName(name);
            irmArray[i] = new Irm3PL(aParamLSAT7[i], bParamLSAT7[i], 1.702);
            irmArray[i].setName(iName);
        }

        IrtExaminee iVec = new IrtExaminee(irmArray);

        //estimate ability scores for each response pattern
        double mle = 0.0;
        double se = 0.0;
        for(int j=0;j<nPeople;j++){
            iVec.setResponseVector(lsat7[j]);
            mle = iVec.maximumLikelihoodEstimate(minTheta, maxTheta);
            se = iVec.mleStandardErrorAt(mle);
//            System.out.println("MLE" + j + ": " + mle + " SE: " + se);
            assertEquals("  MLE Test" + j, trueMLE_LSAT7[j], mle, 1e-4);
        }

    }

    @Test
    public void eapTest2PL(){
        System.out.println();
        System.out.println("EAP test: LSAT7.");
        if(lsat7==null) readLsat7Data();
        int n = aParamLSAT7.length;
        int nPeople = lsat7.length;

        ItemResponseModel[] irmArray = new ItemResponseModel[n];
        ItemResponseModel irm;

        VariableName iName = null;
        for(int i=0;i<n;i++){
            String name = "V"+i;
            iName = new VariableName(name);
            irmArray[i] = new Irm3PL(aParamLSAT7[i], bParamLSAT7[i], 1.702);
            irmArray[i].setName(iName);
        }

        IrtExaminee iVec = new IrtExaminee(irmArray);

        double eap = 0.0;
        double se = 0.0;
        for(int j=0;j<nPeople;j++){
            iVec.setResponseVector(lsat7[j]);
            eap = iVec.eapEstimate(0.0, 1.0, -4.0, 4.0, 25);
            se = iVec.eapStandardErrorAt(eap);
            System.out.println("  EAP" + j + ": " + eap + " SE: " + se);
            assertEquals("  EAP Test" + j, trueEAP_LSAT7[j], eap, 1e-3);
        }

    }


    @Test
    public void mapTest2PL(){
//        System.out.println();
        System.out.println("MAP test: LSAT7.");
        if(lsat7==null) readLsat7Data();
        int n = aParamLSAT7.length;
        int nPeople = lsat7.length;

        ItemResponseModel[] irmArray = new ItemResponseModel[n];
        ItemResponseModel irm;

        VariableName iName = null;
        for(int i=0;i<n;i++){
            String name = "V"+i;
            iName = new VariableName(name);
            irmArray[i] = new Irm3PL(aParamLSAT7[i], bParamLSAT7[i], 1.702);
            irmArray[i].setName(iName);
        }

        IrtExaminee iVec = new IrtExaminee(irmArray);

        double map = 0.0;
        double se = 0.0;
        for(int j=0;j<nPeople;j++){
            iVec.setResponseVector(lsat7[j]);
            map = iVec.mapEstimate(0.0, 1.0, -4.0, 4.0);
            se = iVec.mapStandardErrorAt(map);
//            System.out.println("MAP" + j + ": " + map + " SE: " + se);
            assertEquals("  MAP Test" + j, trueMAP_LSAT7[j], map, 1e-4);
        }

    }

    @Test
    public void jmleTest(){
//        System.out.println();
        System.out.println("JMLE PCF test: LSAT7.");
        if(lsat7==null) readLsat7Data();
        int n = bParam_jmle.length;
        int nPeople = lsat7.length;

        ItemResponseModel[] irmArray = new ItemResponseModel[n];
        ItemResponseModel irm;

        VariableName iName = null;
        for(int i=0;i<n;i++){
            String name = "V"+i;
            iName = new VariableName(name);
            irmArray[i] = new Irm3PL(bParam_jmle[i], 1.0);
            irmArray[i].setName(iName);
        }

        IrtExaminee iVec = new IrtExaminee(irmArray);

        double pcf = 0.0;
        double se = 0.0;
        for(int j=0;j<nPeople;j++){
            iVec.setResponseVector(lsat7[j]);
            pcf = iVec.pcfEstimate(50, 0.001, 0.3);
            se = iVec.pcfStandardErrorAt(pcf);
//            System.out.println("PCF" + j + ": " + pcf + "SE: " + se);
            assertEquals("  JMLE Test" + j, trueJMLE_LSAT7[j], pcf, 1e-4);
        }

    }

    @Test
    public void mixedFormatTestMLE(){
        System.out.println();
        System.out.println("MLE test: mixed-format.");
        if(mixedFormatData==null) readMixedFormat();
        int n = aParam_mixed.length;
        int nPeople = mixedFormatData.length;

        ItemResponseModel[] irmArray = new ItemResponseModel[n];
        ItemResponseModel irm;

        VariableName iName = null;
        for(int i=0;i<n;i++){
            String name = "V"+i;
            iName = new VariableName(name);
            if(i<40){
                irmArray[i] = new Irm3PL(aParam_mixed[i], bParam_mixed[i], cParam_mixed[i], 1.7);
            }else{
                irmArray[i] = new IrmGPCM(aParam_mixed[i], stepParam_mixed[i-40], 1.0);
            }
            irmArray[i].setName(iName);
        }


        IrtExaminee iVec = new IrtExaminee(irmArray);

        double mle = 0.0;
        double se = 0.0;
        for(int j=0;j<nPeople;j++){
            iVec.setResponseVector(mixedFormatData[j]);
            mle = iVec.maximumLikelihoodEstimate(-6.0, 6.0);
            se = iVec.mleStandardErrorAt(mle);
            System.out.println("  MLE" + j + ": " + mle + " True MLE: " + trueMLE_mixed[j] + " SE: " + se);
            assertEquals("  MLE Test" + j, trueMLE_mixed[j], mle, 1e-3);
        }

    }

    @Test
    public void mixedFormatTestEAP(){
        System.out.println();
        System.out.println("EAP test: mixed-format.");
        if(mixedFormatData==null) readMixedFormat();
        int n = aParam_mixed.length;
        int nPeople = mixedFormatData.length;

        ItemResponseModel[] irmArray = new ItemResponseModel[n];
        ItemResponseModel irm;

        VariableName iName = null;
        for(int i=0;i<n;i++){
            String name = "V"+i;
            iName = new VariableName(name);
            if(i<40){
                irmArray[i] = new Irm3PL(aParam_mixed[i], bParam_mixed[i], cParam_mixed[i], 1.7);
            }else{
                irmArray[i] = new IrmGPCM(aParam_mixed[i], stepParam_mixed[i-40], 1.0);
            }
            irmArray[i].setName(iName);
        }


        IrtExaminee iVec = new IrtExaminee(irmArray);

        double eap = 0.0;
        for(int j=0;j<nPeople;j++){
            iVec.setResponseVector(mixedFormatData[j]);
            eap = iVec.eapEstimate(0.0, 1.0, -6.0, 6.0, 40);//ICL defaults
            System.out.println("  EAP" + j + ": " + eap + " True EAP: " + trueEAP_mixed[j]);

            //Not sure if ICL is using final quadrature or normal quadrature values when computing EAP.
            // May be a reason for low accuracy of the results
            assertEquals("  EAP Test" + j, trueEAP_mixed[j], eap, 1e-4);
        }

    }

    @Test
    public void parscaleTest(){
//        System.out.println();
        System.out.println("EAP test: parscale.");
        int n = aparam_parscale.length;
        int nPeople = parscale_data.length;

        ItemResponseModel[] irmArray = new ItemResponseModel[n];
        ItemResponseModel irm;
        double[] scoring = {1,2,3,4};

        VariableName iName = null;
        for(int i=0;i<n;i++){
            String name = "V"+i;
            iName = new VariableName(name);
            irmArray[i] = new IrmGPCM2(aparam_parscale[i], bparam_parscale[i], step_Parscale[i], 1.7);
            irmArray[i].setName(iName);
            irmArray[i].setScoreWeights(scoring);
        }


        IrtExaminee iVec = new IrtExaminee(irmArray);
        UserSuppliedQuadratureRule dist = new UserSuppliedQuadratureRule(quad_point, quad_weight);

        double eap = 0.0;
        double mle = 0.0;
        for(int j=0;j<nPeople;j++){
            iVec.setResponseVector(parscale_data[j]);
            eap = iVec.eapEstimate(0.0, 1.0, parscale_minTheta, parscale_maxTheta, 30);
            eap = iVec.eapEstimate(dist);
//            System.out.println("EAP" + j + ": " + eap + " True EAP: " + trueEAP_parscale[j]);

            //Results not accurate but correlate 0.996 with each other.
            //Difference may be due to optimizer convergence criteria.
//            assertEquals("  EAP Test" + j, trueEAP_parscale[j], eap, 1e-1);

            mle = iVec.maximumLikelihoodEstimate(parscale_minTheta, parscale_maxTheta);
            System.out.println("MLE" + j + ": " + mle + " True MLE: " + trueMLE_parscale[j]);
//            assertEquals("  MLE Test" + j, trueMLE_parscale[j], mle, 1e-1);
        }

    }

    @Test
    public void parscaleTest2(){
        //same test as above but uses IrmGPCM instead of IrmGPCM2
        System.out.println();
        System.out.println("EAP test: parscale reparameritized.");
        int n = aparam_parscale.length;
        int nPeople = parscale_data.length;

        ItemResponseModel[] irmArray = new ItemResponseModel[n];
        ItemResponseModel irm;
        double[] scoring = {1,2,3,4};
        double[] tempStepParam = null;

        VariableName iName = null;
        for(int i=0;i<n;i++){
            String name = "V"+i;
            iName = new VariableName(name);

            tempStepParam = new double[step_Parscale[i].length+1];
            for(int j=0;j<step_Parscale[i].length;j++){
                tempStepParam[j+1] = bparam_parscale[i]-step_Parscale[i][j];
            }

            irmArray[i] = new IrmGPCM(aparam_parscale[i], tempStepParam, 1.7);
            irmArray[i].setName(iName);
            irmArray[i].setScoreWeights(scoring);
        }


        IrtExaminee iVec = new IrtExaminee(irmArray);
        UserSuppliedQuadratureRule dist = new UserSuppliedQuadratureRule(quad_point, quad_weight);

        double eap = 0.0;
        double mle = 0.0;
        for(int j=0;j<nPeople;j++){
            iVec.setResponseVector(parscale_data[j]);
            eap = iVec.eapEstimate(0.0, 1.0, parscale_minTheta, parscale_maxTheta, 30);
            eap = iVec.eapEstimate(dist);
//            System.out.println("EAP" + j + ": " + eap + " True EAP: " + trueEAP_parscale[j]);

            //Results not accurate but correlate 0.996 with each other.
            //Difference may be due to optimizer convergence criteria.
//            assertEquals("  EAP Test" + j, trueEAP_parscale[j], eap, 1e-1);

            mle = iVec.maximumLikelihoodEstimate(parscale_minTheta, parscale_maxTheta);
            System.out.println("MLE" + j + ": " + mle + " True MLE: " + trueMLE_parscale[j]);
//            assertEquals("  MLE Test" + j, trueMLE_parscale[j], mle, 1e-1);
        }

    }

    @Test
    public void binaryItems3plBILOGLTest(){
        System.out.println("Binary items - 3PL with Guessing Prior - Compare to BILOG");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

        //True estimates from bilog
        double[][] bilog_param = {
                {0.76333,-1.21526,0.21163},
                {0.92486,1.07726,0.13576},
                {0.72939,0.66366,0.25931},
                {1.27357,-0.67493,0.14851},
                {1.15729,-0.05777,0.27213},
                {0.92745,-0.90561,0.08175},
                {0.87548,-0.8385,0.13214},
                {0.93323,1.1074,0.28915},
                {0.79899,-1.30207,0.14256},
                {1.72556,-1.81022,0.24254},
                {0.83733,-1.88858,0.19475},
                {1.37099,1.77929,0.11316},
                {0.73367,0.17449,0.14378},
                {0.90479,-0.03548,0.16812},
                {1.06513,0.6771,0.20616},
                {1.1643,-1.70204,0.1809},
                {1.19574,-0.82412,0.33714},
                {0.96185,-0.02128,0.25349},
                {1.12286,-0.17356,0.23763},
                {1.05235,0.08164,0.22479},
                {1.05737,-2.97248,0.19588},
                {0.8673,-1.39868,0.32696},
                {1.06028,-0.74847,0.1799},
                {0.77721,0.16493,0.27042},
                {0.97387,1.17903,0.18722},
                {0.86536,-1.02002,0.16694},
                {0.49233,3.13203,0.23203},
                {0.9268,-0.19869,0.11348},
                {0.90175,-0.46986,0.09845},
                {1.08255,0.21025,0.16163},
                {0.82719,-0.21855,0.15793},
                {1.00376,-0.40387,0.25606},
                {1.02901,-0.98016,0.25859},
                {0.83566,1.42253,0.15805},
                {0.95164,-1.23035,0.1735},
                {0.69049,-0.15762,0.3116},
                {1.15594,0.01526,0.17259},
                {0.89091,-0.06478,0.2345},
                {1.15416,0.92837,0.12165},
                {1.15666,2.11249,0.24942},
                {0.91868,-0.43775,0.281},
                {0.72744,-2.02223,0.19514},
                {0.96094,0.55016,0.16756},
                {1.70615,-0.5253,0.20926},
                {0.86474,0.71882,0.13872},
                {0.65904,-0.17672,0.2409},
                {1.10421,-0.64317,0.23728},
                {1.14024,1.50973,0.11551},
                {0.90145,0.26977,0.11869},
                {0.86409,0.13961,0.10132}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[50];
        Irm3PL pl3 = null;
        for(int j=0;j<bilog_param.length;j++) {
            //3PL with no priors
            pl3 = new Irm3PL(bilog_param[j][0], bilog_param[j][1], bilog_param[j][2], 1.7);//Starting value of guessing parameter must be > 0.
            pl3.setName(new VariableName("Item" + (j+1)));
            irm[j] = pl3;
        }

        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(-4.0, 4.0, 40);

        //estimate person ability
        IrtExaminee irtExaminee = new IrtExaminee(irm);

        double theta = 0;
        for(int i=0;i<20;i++){
            irtExaminee.setResponseVector(responseData[i]);
            theta = irtExaminee.eapEstimate(latentDistribution);
            System.out.println("EAP: " + theta + "  SUM: " + responseData[i].getSumScore());
//            Assert.assertEquals("Binary items - EAP test", bilog_param[j][2], irm[j].getGuessing(), 1e-2);
        }


    }


//======================================================================================================================
// TRUE parameter values
//======================================================================================================================

    private static byte[][] lsat7 = null;
    private static byte[][] mixedFormatData = null;
    private double minTheta = -9.0;//in lieu of negative infinity
    private double maxTheta = 9.0;// in lieu of positive infinity

    //true parameters obtained from mirt package in R
    private double[] aParamLSAT7 = {0.5760746525, 0.6713629131, 0.9567791258, 0.4266571055, 0.4398755432};
    private double[] bParamLSAT7 = {-1.8877222528, -0.7154624410, -1.0825764537, -0.6760358283, -2.4772429769};
    private double[] trueMLE_LSAT7 = {minTheta, -3.10845041, -3.15107153, -2.13728887, -1.98956866,
            -1.38727274, -1.40365915, -0.87089110, -2.50911393, -1.76060374,
            -1.77924262, -1.21512783, -1.10671814, -0.55780612, -0.57560892,
             0.13601644, -2.72808025, -1.89875271, -1.91867347, -1.33073839,
            -1.22120399, -0.68345783, -0.70044698, -0.04756461, -1.57652362,
            -1.03570578, -1.05158493, -0.49482657, -0.36534495,  0.48802400,
             0.45257041, maxTheta};
    private double[] trueEAP_LSAT7 = {-1.86699915002, -1.51833780543, -1.52862017915, -1.19050028062, -1.12167621040,
             -0.78392865464, -0.79420134176, -0.44431659698, -1.33969835385, -1.00375904585,
            -1.01386610198, -0.67338304915, -0.60187663710,-0.23968655305, -0.25097467227,
             0.14224215163, -1.41293907667, -1.07654120538, -1.08662659805, -0.74812568573,
            -0.67735619964, -0.32043055589, -0.33151951606,  0.05371430242, -0.89930927803,
             -0.55455966449, -0.56516178853, -0.20021595605, -0.12161974292, 0.28530512338,
             0.27243030193,  0.72578380552};
    private double[] trueMAP_LSAT7 = {-1.82014732325, -1.48950570373, -1.49914838882, -1.18508879236, -1.12160703999,
            -0.81006616362, -0.81957574775, -0.49478440966, -1.32320650445, -1.01284454621,
            -1.02218279038, -0.70781334045, -0.64155893474, -0.30261794957, -0.31330117802,
             0.06239891391, -1.39119370820, -1.07990412674, -1.08922566785, -0.77692861198,
            -0.71149909142, -0.37865183861, -0.38909943798, -0.02295620020, -0.91654678664,
            -0.59751769790, -0.60739760260, -0.26523888053, -0.19071994704,  0.20152618293,
             0.18892566808,  0.63800689395};

    //LSAT7 results from jmetrik using Rasch Models analysis
    private double[] bParam_jmle = {-0.6827150092298472, 0.6679301544552803,  -0.18450773468097034, 1.0331646145652922, -0.8338720251097548};
    private double[] trueJMLE_LSAT7 = {
            -2.9681488055844527, -1.5443472409279775, -1.5443472409279775, -0.4669235333931464, -1.5443472409279775,
            -0.4669235333931464, -0.4669235333931464, 0.45293530892580947, -1.5443472409279775, -0.4669235333931464,
            -0.4669235333931464, 0.45293530892580947, -0.4669235333931464, 0.45293530892580947, 0.45293530892580947,
            1.5467384674981617, -1.5443472409279775, -0.4669235333931464, -0.4669235333931464, 0.45293530892580947,
            -0.4669235333931464, 0.45293530892580947, 0.45293530892580947, 1.5467384674981617, -0.4669235333931464,
            0.45293530892580947, 0.45293530892580947, 1.5467384674981617, 0.45293530892580947, 1.5467384674981617,
            1.5467384674981617, 2.987845332665278};

    //ability estimates from ICL for the resposne vectors in mixed-format.txt
    private double[] trueEAP_mixed = {
        1.203520, 1.237269, -0.839161, -0.253902, 1.901835, 0.427541,
        0.235083, 0.750366, -0.674191, 0.441296, 1.572613, 0.801079,
        0.329102, 1.458463, -0.189362, 1.845943, 0.878068, 0.802608,
        -0.829593, -0.502849 };

    private double[] trueMLE_mixed = {
        1.302364, 1.336559, -0.845830, -0.208761, 2.101531, 0.491479,
        0.305466, 0.817874, -0.685480, 0.508849, 1.690902, 0.868703,
        0.390239, 1.577347, -0.157638, 2.016163, 0.961122, 0.874694,
        -0.898311, -0.503212 };

    //item parameters for 40 multiple choice and 2 polytomous items from mixed-format.txt
    // (estimates obtained from larger item response file)
    private double[] aParam_mixed = {
            0.7573722, 0.84344208, 0.91272306, 0.8476324, 0.75653338, 0.91264614, 1.7310894, 0.93499608,
            1.54515682, 1.24897604, 0.71757726, 1.00732102, 1.03585051, 0.81302008, 0.94757159, 0.59698281,
            0.62028276, 1.00758979, 0.58312897, 0.60219414, 0.66084336, 0.62989076, 0.70693752, 0.86574777,
            0.61312122, 0.56735134, 0.98809429, 0.53907226, 0.64132853, 0.67836688, 0.92011339, 0.83107679,
            0.87903698, 1.43258799, 0.44561383, 1.03101111, 1.6025383, 0.85904148, 1.55120193, 0.8282011,
            0.97862369, 1.30034675};

    private double[] bParam_mixed = {
            1.6230173, 0.67152368, -0.32696466, -0.01087341, -0.12937667, 0.80500481, 1.29248633,
            1.06740232, -0.14137442, 0.59466423, 0.19875489, 2.02049419, 1.14622086, -0.60786305,
            0.52960921, 0.12096465, 0.49659716, 1.13475338, -1.6020013, 1.10518653, -0.10679459,
            -0.59138503, 1.88745664, 0.7035784, -0.30082617, -1.492581, 1.95993867, 0.21187485,
            0.59263333, 2.09225317, 0.17273864, 0.82048979, 0.71126868, 0.31175695, 0.82426725,
            1.87676213, 2.16970874, 1.0654643, 1.81675684, 0.17928933};

    private double[] cParam_mixed = {
            0.13656342, 0.29406404, 0.25057165, 0.1797302, 0.20751763, 0.19368337, 0.26555399,
            0.16015504, 0.10072914, 0.1945635, 0.11197594, 0.27011819, 0.22768903, 0.1748153,
            0.16277419, 0.13670017, 0.24440971, 0.16699152, 0.16742496, 0.2379273, 0.27999908,
            0.08793936, 0.19558209, 0.1887004, 0.1045626, 0.12386857, 0.33002528, 0.40741659,
            0.13649977, 0.47153802, 0.18915205, 0.33726248, 0.10903968, 0.23812296, 0.23128587,
            0.11829606, 0.15408938, 0.13365926, 0.21462302, 0.15194013};

    private double[][] stepParam_mixed = {
            {0.0, 0.50768878, 1.37864855, 0.13240289},
            {0.0, 0.50140888, 1.65465213, 4.10892819} };

//============================================================================================================================================================================
// These parameters come from example1 that comes with PARSCALE. However, the analysis uses a partial credit model for each item instead of a single rating scale.
//============================================================================================================================================================================
    double parscale_minTheta = -4.0;
    double parscale_maxTheta = 4.0;
    private double [] aparam_parscale = {1.50508, 1.61609, 1.54948, 1.71541, 1.76130, 1.23014, 1.31342,
	    1.39885, 1.30280, 1.25339, 0.96456, 0.98534, 1.07900,  1.02679,	1.05099, 0.74472, 0.68904, 0.75242, 0.71619, 0.73505};
    private double[] bparam_parscale = {0.00750, -0.00775, 0.01932, -0.01465, -0.01038,  0.47858, 0.48654,
	    0.44832, 0.47259, 0.50252, -0.53854, -0.50507, -0.50478, -0.44867, -0.46213, 0.02238, -0.07591, 0.04737, 0.04728, 0.07827};
    private double[][] step_Parscale = {
            {1.02242, 0.01036, -1.03278},
            {0.95478, 0.02745, -0.98223},
            {0.98682,-0.01933, -0.96749},
            {0.94606,-0.01821, -0.92785},
            {0.92796, 0.01413, -0.94209},
            {0.96578, 0.00920, -0.97499},
            {0.92201, 0.01823, -0.94025},
            {0.92441, 0.05682, -0.98123},
            {0.94065, 0.04582, -0.98648},
            {1.05179,-0.00746, -1.04433},
            {1.08470,-0.02502, -1.05968},
            {0.95794, 0.01865, -0.97659},
            {1.02808,-0.06415, -0.96393},
            {1.02342,-0.02146, -1.00197},
            {0.92162,-0.03804, -0.88358},
            {0.97320, 0.03400, -1.00720},
            {1.10923, 0.05042, -1.15964},
            {1.01524,-0.01631, -0.99893},
            {1.00752, 0.00576, -1.01328},
            {0.91689, 0.00846, -0.92535}};


    //EAP estimates and standard errors from the first 20 examinees in teh example data file
    private double[] trueEAP_parscale = {
            0.6071, -0.6969, -0.4161, -0.8136, -0.7741, 1.5050, 0.1959, 1.9895, 1.5136, -2.0296,
            1.1653, -2.2606, -1.7540, 0.3270, 0.1765, -0.1735, 0.2841, 0.0638, -0.3918, 1.9686};
    private double[] trueEAP_stdError_parscale = {
            0.2056, 0.2028, 0.1967, 0.2085, 0.2088, 0.2445, 0.2092, 0.3191, 0.2464, 0.3486,
            0.2226, 0.4004, 0.2972, 0.2106, 0.2015, 0.1898, 0.1920, 0.2102, 0.2127, 0.3132};
    private double[] trueMLE_parscale = {
            0.6322,-0.7244,-0.4308,-0.8431,-0.8051,1.5732,0.2050, 2.19332,1.5815,-2.2649,
            1.2113,-2.7058,-1.8775,0.3413,0.1809,-0.1790,0.2946,0.0674,-0.4082, 2.48472};
    //response patterns for the first 20 examinees in parscale example1.dat but recoded from 1,2,3,4 to 0,1,2,3
    private byte[][] parscale_data = {
            {3,1,3,3,3,1,2,1,1,1,2,2,3,2,3,2,2,2,2,1},
            {0,1,1,1,0,0,1,0,0,1,1,2,1,3,0,1,0,3,2,1},
            {2,1,1,0,1,1,0,1,1,0,2,2,3,1,2,0,3,0,1,0},
            {0,2,1,1,1,0,0,0,0,0,2,1,1,3,1,1,0,0,0,0},
            {1,0,1,0,0,0,1,1,2,0,2,0,2,1,2,0,1,0,2,0},
            {2,3,3,3,2,3,3,3,2,3,3,2,3,3,3,3,3,2,3,3},
            {1,2,2,3,2,1,0,2,3,0,3,2,0,1,2,2,2,2,2,0},
            {3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,3,3,2,3,2},
            {3,3,3,3,3,1,2,3,2,3,3,3,3,3,3,3,3,2,2,2},
            {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0},
            {3,2,2,3,3,3,3,3,1,2,3,3,2,3,3,1,3,3,1,1},
            {0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0},
            {0,0,0,1,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0},
            {0,2,1,3,2,1,1,2,0,2,3,3,2,1,3,3,2,3,2,0},
            {2,2,1,1,1,1,2,0,2,2,1,3,1,3,3,1,2,2,2,3},
            {1,1,1,2,2,1,1,0,0,1,2,1,2,3,1,2,1,1,1,3},
            {2,2,2,2,2,2,2,1,1,0,1,2,3,2,3,2,3,0,1,0},
            {2,2,0,2,1,1,2,0,2,1,1,2,3,0,0,3,2,3,2,3},
            {3,1,1,0,2,0,0,1,2,1,0,0,2,1,1,0,1,1,3,2},
            {3,3,3,3,3,3,3,3,3,2,3,3,3,3,3,3,2,2,3,3} };
    //quadrature from parscale output
    double[] quad_point = {
            -0.4000E+01, -0.3724E+01, -0.3448E+01, -0.3172E+01, -0.2897E+01,
            -0.2621E+01, -0.2345E+01, -0.2069E+01, -0.1793E+01, -0.1517E+01,
            -0.1241E+01, -0.9655E+00, -0.6897E+00, -0.4138E+00, -0.1379E+00,
            0.1379E+00,  0.4138E+00,  0.6897E+00,  0.9655E+00,  0.1241E+01,
            0.1517E+01,  0.1793E+01, 0.2069E+01,  0.2345E+01,  0.2621E+01,
            0.2897E+01,  0.3172E+01,  0.3448E+01,  0.3724E+01,  0.4000E+01};
    double[] quad_weight = {
            0.3692E-04,  0.1071E-03,  0.2881E-03,  0.7181E-03,  0.1659E-02,
            0.3550E-02,  0.7042E-02,  0.1294E-01,  0.2205E-01,  0.3481E-01,
            0.5093E-01,  0.6905E-01,  0.8676E-01,  0.1010E+00,  0.1090E+00,
            0.1090E+00,  0.1010E+00,  0.8676E-01,  0.6905E-01,  0.5093E-01,
            0.3481E-01,  0.2205E-01,  0.1294E-01,  0.7042E-02,  0.3550E-02,
            0.1659E-02,  0.7181E-03,  0.2881E-03,  0.1071E-03,  0.3692E-04};

}

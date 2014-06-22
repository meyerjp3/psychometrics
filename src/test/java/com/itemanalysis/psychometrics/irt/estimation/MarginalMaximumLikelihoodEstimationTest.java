package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.distribution.NormalDistributionApproximation;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class MarginalMaximumLikelihoodEstimationTest {


//    @Test
    public void LSAT6Test(){
        System.out.println("Testing summary of LSAT6 data");

        byte[][] u = { // read canned data
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1},
                {0, 0, 0, 1, 0},
                {0, 0, 0, 1, 1},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 1},
                {0, 0, 1, 1, 0},
                {0, 0, 1, 1, 1},
                {0, 1, 0, 0, 0},
                {0, 1, 0, 0, 1},
                {0, 1, 0, 1, 0},
                {0, 1, 0, 1, 1},
                {0, 1, 1, 0, 0},
                {0, 1, 1, 0, 1},
                {0, 1, 1, 1, 0},
                {0, 1, 1, 1, 1},
                {1, 0, 0, 0, 0},
                {1, 0, 0, 0, 1},
                {1, 0, 0, 1, 0},
                {1, 0, 0, 1, 1},
                {1, 0, 1, 0, 0},
                {1, 0, 1, 0, 1},
                {1, 0, 1, 1, 0},
                {1, 0, 1, 1, 1},
                {1, 1, 0, 0, 0},
                {1, 1, 0, 0, 1},
                {1, 1, 0, 1, 0},
                {1, 1, 0, 1, 1},
                {1, 1, 1, 0, 0},
                {1, 1, 1, 0, 1},
                {1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1}
        };
        double[] fpt = { 3.0, 6.0, 2.0, 11.0, 1.0, 1.0, 3.0, 4.0, 1.0,
                8.0, 0.0, 16.0, 0.0, 3.0, 2.0, 15.0, 10.0, 29.0, 14.0, 81.0,
                3.0, 28.0, 15.0, 80.0, 16.0, 56.0, 21.0, 173.0, 11.0, 61.0,
                28.0, 298.0 };

        int nItems = 5;

        //Create response vectors
        ItemResponseVector[] responseData = new ItemResponseVector[u.length];
        for(int i=0;i<u.length;i++){
            responseData[i] = new ItemResponseVector(u[i], fpt[i]);
        }

        //Create array of 2PL item response models
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        for(int j=0;j<nItems;j++){
            irm[j] = new Irm3PL(1.0, 0.0, 1.0);
        }

        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 10);

        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        mmle.estimateParameters(1e-6, 250);
        System.out.println(mmle.printItemParameters());

    }

//    @Test
    public void LSAT7Test(){
        System.out.println("LSAT7 data - 3PL no priors");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, true);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[5];
        Irm3PL pl3 = null;
        for(int j=0;j<5;j++){
            //3PL
            pl3 = new Irm3PL(1.0, 0.0, 0.0, 1.0);//3PL
//            pl3.setguessingPrior(new ItemParamPriorBeta4(1, 1, 0.0, 1.0));//vague prior for guessing parameter
            irm[j] = pl3;
        }

        //computation of quadrature points as done in the mirt R package
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(min, max, (int)quadPoints);

        StartingValues startingValues = new StartingValues(responseData, irm);
        irm = startingValues.computeStartingValues();

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-3, 500);
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

    /**
     * Test that computation of marginal posterior mode is correct when compared to ICL
     */
//    @Test
    public void lsat7loglikelihoodTest(){
        System.out.println("LSAT 7 - loglikelihood test");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, true);

        //parameter estimates from ICL
        double[][] icl_param = {
                {0.657949,-1.414405,0.209010},
                {0.970411,-0.134787,0.260093},
                {1.148129,-0.792045,0.174937},
                {0.502746,-0.228255,0.140548},
                {0.497510,-1.819153,0.238453}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[5];
        Irm3PL pl3 = null;
        for(int j=0;j<5;j++) {
            //3PL with all item parameters fixed to ICL values
            pl3 = new Irm3PL(icl_param[j][0], icl_param[j][1], icl_param[j][2], 1.7);
            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));
            pl3.setguessingPrior(new ItemParamPriorBeta4(2.0, 4.0, 0.0, 1.0));
            pl3.setFixed(true);
            irm[j] = pl3;
        }

        //set latent distribution
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);;
        mmle.estimateParameters(1e-3, 250);
        assertEquals("LSAT 7 Loglikelihood test", -2663.5739, mmle.completeDataLogLikelihood(), 1e-4);
    }

//    @Test
    public void lsat7Test(){
        System.out.println("LSAT 7 - ICL test");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, true);

        //parameter estimates from ICL
        double[][] icl_param = {
                {0.657949,-1.414405,0.209010},
                {0.970411,-0.134787,0.260093},
                {1.148129,-0.792045,0.174937},
                {0.502746,-0.228255,0.140548},
                {0.497510,-1.819153,0.238453}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[5];
        Irm3PL pl3 = null;
        for(int j=0;j<5;j++) {
            //3PL with all item parameters fixed to ICL values
//            pl3 = new Irm3PL(icl_param[j][0], icl_param[j][1], icl_param[j][2], 1.7);//will get exact results if use ICL estimates as starting values
            pl3 = new Irm3PL(1.0, 0.0, 0.2, 1.7);
            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));
            pl3.setguessingPrior(new ItemParamPriorBeta4(2.0, 4.0, 0.0, 1.0));
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        //set latent distribution
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-3, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());

        for(int j=0;j<5;j++){
            assertEquals("LSAT 7 discrimination test", icl_param[j][0], irm[j].getDiscrimination(), 1e-2);
            assertEquals("LSAT 7 difficulty test", icl_param[j][1], irm[j].getDifficulty(), 1e-2);
            assertEquals("LSAT 7 guessing test", icl_param[j][2], irm[j].getGuessing(), 1e-2);
        }

    }

//    @Test
    public void binaryItemsNoPrior(){
        System.out.println("Binary items - 3PL no priors");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[50];
        Irm3PL pl3 = null;
        for(int j=0;j<50;j++) {
            //3PL with no priors
            pl3 = new Irm3PL(1.0, 0.0, 0.0, 1.7);
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-3, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

//    @Test
    public void binaryItemsICLStartValuesTest(){
        System.out.println("Binary items - 3PL no priors");

        double[][] icl_start_values = {
                {0.820369,-1.282356,0.136368},
                {0.823571,1.019577,0.108432},
                {0.644488,0.417793,0.186284},
                {1.258120,-0.786632,0.082428},
                {1.072820,-0.220014,0.213792},
                {0.990607,-0.932971,0.051665},
                {0.904843,-0.924794,0.073775},
                {0.774064,0.964676,0.241353},
                {0.873584,-1.302571,0.096758},
                {1.868962,-1.722388,0.217062},
                {0.946892,-1.795795,0.148339},
                {1.023787,1.947222,0.099358},
                {0.705871,0.010238,0.087140},
                {0.871423,-0.188793,0.110041},
                {0.951599,0.550470,0.164958},
                {1.303065,-1.625691,0.149257},
                {1.189412,-0.967495,0.254854},
                {0.920077,-0.187425,0.194837},
                {1.052252,-0.344697,0.168656},
                {0.994443,-0.073448,0.170382},
                {1.304364,-2.570287,0.183529},
                {0.914120,-1.499732,0.227628},
                {1.095045,-0.816477,0.140232},
                {0.729564,-0.039467,0.205767},
                {0.842082,1.107421,0.154952},
                {0.928375,-1.044977,0.132639},
                {0.399819,2.977028,0.187604},
                {0.925520,-0.309122,0.069430},
                {0.935486,-0.550800,0.058937},
                {1.004663,0.076083,0.114624},
                {0.825140,-0.355393,0.102776},
                {0.977649,-0.568999,0.183767},
                {1.048947,-1.125444,0.153027},
                {0.691997,1.356874,0.117002},
                {1.019891,-1.240543,0.136254},
                {0.655583,-0.502028,0.190880},
                {1.091913,-0.113541,0.126144},
                {0.860510,-0.233230,0.173410},
                {1.005762,0.872636,0.095446},
                {0.801280,2.254579,0.224330},
                {0.896642,-0.623631,0.200919},
                {0.818614,-1.921006,0.147626},
                {0.871693,0.410965,0.120352},
                {1.561474,-0.658139,0.143224},
                {0.782197,0.586001,0.092590},
                {0.643544,-0.431697,0.148489},
                {1.086715,-0.789503,0.159016},
                {0.919572,1.561791,0.094797},
                {0.865507,0.153900,0.080379},
                {0.859232,0.034074,0.066889}
        };

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[50];
        Irm3PL pl3 = null;
        for(int j=0;j<50;j++) {
            //3PL with no priors
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.7);
//
//            pl3 = new Irm3PL(icl_start_values[j][0], icl_start_values[j][1], icl_start_values[j][2], 1.7);
            pl3.setguessingPrior(new ItemParamPriorBeta4(5, 17, 0.0, 1.0));
//            pl3.setguessingPrior(new ItemParamPriorBeta(5, 17));
            irm[j] = pl3;
        }


//        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //mirt R package default quadrature
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(min, max, (int)quadPoints);

        StartingValues startingValues = new StartingValues(responseData, irm);
        irm = startingValues.computeStartingValues();

        //Gauss-Hermite quadrature
//        HermiteRuleFactory gaussHermite = new HermiteRuleFactory();
//        Pair<double[], double[]> dist = gaussHermite.getRule(41);
//        UserSuppliedDistributionApproximation latentDistribution = new UserSuppliedDistributionApproximation(dist.getKey(), dist.getValue());

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-3, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

    /**
     * Test compares results to those obtained from ICL. The ICL command file was
     *   output -log_file binary-items-ICL-results.log
     *   options -D 1.7
     *   options -default_prior_a none
     *   options -default_prior_b none
     *   options -default_prior_c {beta 5 17 0 1}
     *   allocate_items_dist 50
     *   read_examinees binary-items-for-ICL.txt 50i1
     *   starting_values_dichotomous
     *   EM_steps -max_iter 550 -crit 0.00000001
     *   print -item_param -latent_dist
     *   write_item_param binary-items-output.txt
     *   release_items_dist
     *
     *
     */
//    @Test
    public void binaryItems3plICLtest(){
        System.out.println("Binary items - 3PL with Guessing Prior - Compare to ICL");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

        //True loglikelihood from ICL
        double icl_loglike = -124969.8947;

        //True estimates from ICL
        double[][] icl_param = {
                {0.76347,-1.220144,0.212395},
                {0.924354,1.071646,0.135753},
                {0.729147,0.657936,0.259344},
                {1.273566,-0.680918,0.148698},
                {1.156938,-0.063871,0.272147},
                {0.92746,-0.911331,0.082136},
                {0.875503,-0.844159,0.132513},
                {0.932577,1.101763,0.289127},
                {0.798808,-1.308274,0.142742},
                {1.726537,-1.81536,0.243826},
                {0.837149,-1.89469,0.195161},
                {1.369644,1.774273,0.113152},
                {0.733356,0.168418,0.143773},
                {0.904579,-0.041443,0.168195},
                {1.064602,0.671262,0.206159},
                {1.1643,-1.707713,0.181641},
                {1.195676,-0.830128,0.337304},
                {0.961585,-0.027297,0.253533},
                {1.122585,-0.179624,0.237685},
                {1.052078,0.075657,0.224839},
                {1.056582,-2.980166,0.196181},
                {0.867291,-1.40431,0.327393},
                {1.060104,-0.754592,0.180022},
                {0.776832,0.158786,0.270386},
                {0.973238,1.173471,0.187204},
                {0.865314,-1.025846,0.167251},
                {0.491908,3.128096,0.232012},
                {0.92658,-0.20472,0.113559},
                {0.901685,-0.475711,0.098661},
                {1.082212,0.204264,0.16166},
                {0.826917,-0.224698,0.157947},
                {1.003709,-0.409727,0.256233},
                {1.029246,-0.985538,0.259123},
                {0.835143,1.41714,0.158039},
                {0.951621,-1.236146,0.173898},
                {0.690575,-0.162861,0.311894},
                {1.155624,0.009218,0.17263},
                {0.89074,-0.070704,0.234589},
                {1.153326,0.922644,0.121614},
                {1.15512,2.107876,0.249394},
                {0.918532,-0.443734,0.28112},
                {0.727258,-2.028257,0.195629},
                {0.96052,0.544287,0.167568},
                {1.70593,-0.531446,0.209331},
                {0.864322,0.713006,0.138718},
                {0.658931,-0.182462,0.241043},
                {1.10429,-0.648928,0.237559},
                {1.139427,1.504423,0.115497},
                {0.901024,0.263703,0.118666},
                {0.863821,0.133621,0.101356}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[50];
        Irm3PL pl3 = null;
        for(int j=0;j<50;j++) {
            //3PL with no priors
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.7);
            pl3.setguessingPrior(new ItemParamPriorBeta4(5, 17, 0.0, 1.0));
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(0.000001, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());

        for(int j=0;j<50;j++){
            assertEquals("Binary items - discrimination test", icl_param[j][0], irm[j].getDiscrimination(), 1e-3);
            assertEquals("Binary items - difficulty test", icl_param[j][1], irm[j].getDifficulty(), 1e-3);
            assertEquals("Binary items - guessing test", icl_param[j][2], irm[j].getGuessing(), 1e-3);
        }
        assertEquals("Binary items - loglikelihood", icl_loglike, mmle.completeDataLogLikelihood(), 1e-2);

    }

    /**
     * BILOG estimates obtained with the following commands:
     *
     * >GLOBAL DFName = 'binary-items.txt',
     *  NPArm = 3, SAVE;
     * >SAVE PARM='binary-items-ipar.txt';
     * >LENGTH NITEMS=50;
     * >INPUT NTOtal=50, NIDCHAR = 4;
     * >ITEMS INAMES=(ITEM01(1)ITEM50);
     * >TEST1 TNAme = 'BINITEMS', INUMBER = (1(1)50);
     * (4A1, 50A1)
     * >CALIB NQPT=40, CYCLES=250, NEWTON=100, CRIT=0.000001, ACCel = 1.0000,
     * CHI=15, NOTPRIOR, NOSPRIOR, GPRIOR;
     * >SCORE ;
     *
     */
    @Test
    public void binaryItems3plBILOGLtest(){
        System.out.println("Binary items - 3PL with Guessing Prior - Compare to BILOG");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

        //True loglikelihood from ICL
        double bilog_loglike = -124456.3;

        //True estimates from bilog
        double[][] bilog_param = {
                {0.76133,-1.22509,0.2113},
                {0.92189,1.07408,0.13571},
                {0.72703,0.65897,0.25918},
                {1.27024,-0.68308,0.14836},
                {1.15387,-0.06442,0.27201},
                {0.92518,-0.91416,0.08166},
                {0.87321,-0.84708,0.13198},
                {0.93018,1.1043,0.2891},
                {0.79706,-1.3116,0.14243},
                {1.72203,-1.8205,0.24238},
                {0.83544,-1.89931,0.19459},
                {1.36628,1.77853,0.11315},
                {0.73148,0.16842,0.14364},
                {0.90213,-0.0421,0.16798},
                {1.06181,0.67268,0.20611},
                {1.16179,-1.71223,0.18074},
                {1.19246,-0.83291,0.33688},
                {0.95898,-0.02788,0.25335},
                {1.11951,-0.1806,0.23747},
                {1.04921,0.07539,0.22469},
                {1.05543,-2.9846,0.19586},
                {0.86494,-1.40935,0.32644},
                {1.05751,-0.75684,0.17975},
                {0.77482,0.15875,0.27027},
                {0.97071,1.17619,0.18718},
                {0.86315,-1.02906,0.16676},
                {0.49058,3.13578,0.23198},
                {0.92417,-0.20571,0.11334},
                {0.89932,-0.47753,0.09831},
                {1.07933,0.20442,0.16155},
                {0.82483,-0.22567,0.15778},
                {1.00078,-0.41169,0.25581},
                {1.02615,-0.98954,0.25819},
                {0.83288,1.42046,0.158},
                {0.94931,-1.23983,0.17329},
                {0.68824,-0.16529,0.31126},
                {1.15258,0.00888,0.1725},
                {0.8883,-0.07148,0.23437},
                {1.15039,0.92475,0.1216},
                {1.15253,2.11288,0.2494},
                {0.916,-0.44564,0.28078},
                {0.72577,-2.03332,0.19499},
                {0.95793,0.5453,0.16748},
                {1.70146,-0.53311,0.20913},
                {0.86201,0.71445,0.13864},
                {0.65701,-0.18418,0.24062},
                {1.10115,-0.65145,0.23706},
                {1.13652,1.50801,0.11549},
                {0.89878,0.2641,0.1186},
                {0.86153,0.1335,0.10119}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[50];
        Irm3PL pl3 = null;
        for(int j=0;j<50;j++) {
            //3PL with no priors
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.7);
            pl3.setguessingPrior(new ItemParamPriorBeta4(5, 17, 0.0, 1.0));
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(0.000001, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());

        for(int j=0;j<50;j++){
            assertEquals("Binary items - discrimination test", bilog_param[j][0], irm[j].getDiscrimination(), 1e-2);
            assertEquals("Binary items - difficulty test", bilog_param[j][1], irm[j].getDifficulty(), 1e-2);
            assertEquals("Binary items - guessing test", bilog_param[j][2], irm[j].getGuessing(), 1e-2);
        }
//        assertEquals("Binary items - loglikelihood", bilog_loglike, mmle.completeDataLogLikelihood(), 1e-2);

    }

//    @Test
    public void binaryItems2plICLtest(){
        System.out.println("Binary items - 2PL - Compare to ICL");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[50];
        Irm3PL pl3 = null;
        for(int j=0;j<50;j++) {
            //2PL with no priors
            pl3 = new Irm3PL(1.0, 0.0, 1.7);
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-4, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());

    }

//    @Test
    public void sim5Kmodel3plNoPrior(){
        System.out.println("Sim data 5K - 3PL no priors");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/sim-3pl-5K.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, false);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[30];
        Irm3PL pl3 = null;
        for(int j=0;j<30;j++) {
            //3PL with no priors
            pl3 = new Irm3PL(1.0, 0.0, 0.0, 1.0);
            irm[j] = pl3;
        }

        //mirt R package default quadrature
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(min, max, (int)quadPoints);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-4, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

//    @Test
    public void sim5Kmodel3plGuessPrior(){
        System.out.println("Sim data 5K - 3PL guessing prior");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/sim-3pl-5K.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, false);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[30];
        Irm3PL pl3 = null;
        for(int j=0;j<30;j++) {
            //3PL with item priors
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.0);
            pl3.setguessingPrior(new ItemParamPriorBeta4(1.0, 1.0, 0.0, 1.0));//vague prior to keep c parameter within acceptable range
            irm[j] = pl3;
        }

        //mirt R package default quadrature
//        double quadPoints = 41;
//        double min = -.8 * Math.sqrt(quadPoints);
//        double max = -1*min;
//        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(min, max, (int)quadPoints);

        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //Estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-4, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());

    }

//    @Test
    public void sim5Kmodel3plICLdefaultPrior(){
        System.out.println("Sim data 5K - 3PL ICL default priors");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/sim-3pl-5K.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, false);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[30];
        Irm3PL pl3 = null;
        for(int j=0;j<30;j++) {
            //3PL with ICL defaults
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.7);//3PL
            pl3.setguessingPrior(new ItemParamPriorBeta4(3.5, 4.0, 0.0, 0.5));
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6, 6));
            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            irm[j] = pl3;
        }

        //ICL default quadrature
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-3, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

//    @Test
    public void tapDataTest(){
        System.out.println("TAP Data test - 3PL no priors");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/tap-data.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, false);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[18];
        Irm3PL pl3 = null;
        for(int j=0;j<18;j++) {
            //1PL with no priors
            pl3 = new Irm3PL(0.0, 1.0);
            //use difficulty priors because some items answered correctly by everyone
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6, 6));
            irm[j] = pl3;
        }

        //mirt R package default quadrature
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(min, max, (int)quadPoints);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-4, 250);
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

//    @Test
    public void simulation2PL(){
        System.out.println("Running simulation...");
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();

        int nPeople = 1500;
        int nItems = 40;
        int nrep = 200;

        String inputPath = "C:\\Users\\Patrick\\Dropbox\\jmetrik\\jmetrik-3pl\\simdata\\c1";
        String outputPath = inputPath + "\\jmetrik-output";
        String dataFile = "";

        for(int r=0;r<nrep;r++){
            dataFile = "\\c1rep" + (r+1) + ".txt";
            ItemResponseVector[] responseData = fileSummary.getResponseVectors(inputPath + dataFile, false);


            //Create array of 2PL item response models
            ItemResponseModel[] irm = new ItemResponseModel[nItems];
            Irm3PL pl3 = null;
            for(int j=0;j<nItems;j++) {
                pl3 = new Irm3PL(1.0, 0.0, 1.0);
                irm[j] = pl3;
            }

            //computation of quadrature points as done in the mirt R package
            double quadPoints = 41;
            double min = -.8 * Math.sqrt(quadPoints);
            double max = -1*min;
            NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(min, max, (int)quadPoints);

            //compute start values
            StartingValues startValues = new StartingValues(responseData, irm);
            irm = startValues.computeStartingValues();

            //estimate parameters
            MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
//            DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
//            mmle.addEMStatusListener(emStatus);
            mmle.estimateParameters(1e-4, 250);
            System.out.println("Replication " + (r+1) +  " complete");


            try{
                File f = new File(outputPath + dataFile);
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                for(int j=0;j<nItems;j++){
                    writer.write(Precision.round(irm[j].getDiscrimination(),6) + "," + Precision.round(irm[j].getDifficulty(), 6));
                    writer.newLine();
                }
                writer.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }


        }//end loop over replications



    }


}
package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.*;
import com.itemanalysis.psychometrics.quadrature.ContinuousQuadratureRule;
import com.itemanalysis.psychometrics.quadrature.NormalQuadratureRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class MarginalMaximumLikelihoodEstimationTest {

    /**
     * True results obtained from mirt package in R.
     *
     * library(mirt)
     * x<-expand.table(LSAT6)
     * model2<-mirt(data=x, model=1, itemtype='2PL', TOL=1e-3)
     * coef(model2, IRTpars=TRUE)
     */
//    @Ignore
    @Test
    public void LSAT6TestComparison(){
        System.out.println("LSAT6 data 2PL model");

        byte[][] u = {
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

        double[] mirtDiscrimination = {0.825, 0.724, 0.888, 0.689, 0.659};
        double[] mirtDifficulty = {-3.362,-1.368,-0.280,-1.864,-3.117};

        int nItems = 5;

        //Create response vectors
        ItemResponseVector[] responseData = new ItemResponseVector[u.length];
        for(int i=0;i<u.length;i++){
            responseData[i] = new ItemResponseVector(u[i], fpt[i]);
        }

        //Create array of 2PL item response models
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        ItemResponseModel pl2 = null;
        for(int j=0;j<nItems;j++){
            pl2 = new Irm3PL(1.0, 0.0, 1.0);
            pl2.setName(new VariableName("item"+(j+1)));
            irm[j] = pl2;
        }

        //mirt R package default quadrature points and weights
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule((int)quadPoints, min, max, 0, 1);

        StartingValues startingValues = new StartingValues(responseData, irm);
        irm = startingValues.computeStartingValues();

        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        EMStatusListener statusListener = new DefaultEMStatusListener();
        mmle.addEMStatusListener(statusListener);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-3, 250);
        mmle.computeItemStandardErrors();
        System.out.println(mmle.printItemParameters());
        System.out.println();

        for(int j=0;j<5;j++){
            assertEquals("  LSAT 6 discrimination test", mirtDiscrimination[j], Precision.round(irm[j].getDiscrimination(), 3), 1e-2);
            assertEquals("  LSAT 6 difficulty test", mirtDifficulty[j], Precision.round(irm[j].getDifficulty(),3), 1e-2);
        }

    }

    /**
     * This example can be difficult to analyze. It can produce negative quessing parameter estimates.
     * The results are close to mirt. ICL will not converge with these data and no prior distributions.
     *
     * library(mirt)
     * x<-expand.table(LSAT7)
     * (model2<-mirt(data=x, model=1, itemtype='3PL', TOL=1e-4))
     * coef(model2, IRTpars=TRUE)
     *
     */
//    @Ignore
    @Test
    public void LSAT7mirtTest(){
        System.out.println("LSAT7 data - mirt test 3PL");

        double[] mirtDiscrimination = {1.010,1.947,1.684,0.737,0.771};
        double[] mirtDifficulty = {-1.837,-0.042,-1.047,-0.648,-2.393};
        double[] mirtGuessing = {0.008,0.298,0.014,0.003,0.019};

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, true);

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[5];
        Irm3PL pl3 = null;
        for(int j=0;j<5;j++){
            //3PL
            pl3 = new Irm3PL(1.0, 0.0, 0.0, 1.0);//3PL - note that initial guessing parameter should not be zero
            pl3.setName(new VariableName("item" + (j+1)));
            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));
            pl3.setGuessingPrior(new ItemParamPriorBeta4(2.0, 4.0, 0.0, 1.0));
            irm[j] = pl3;
        }

//        computation of quadrature points as done in the mirt R package
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule((int)quadPoints, min, max, 0, 1);

        StartingValues startingValues = new StartingValues(responseData, irm);
        irm = startingValues.computeStartingValues();

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 500);
        mmle.computeItemStandardErrors();

        System.out.println();
        System.out.println(mmle.printItemParameters());
        mmle.computeG2ItemFit(10, 5);
//        mmle.computeSX2ItemFit(1);
        System.out.println(mmle.printItemFitStatistics());

        for(int j=0;j<5;j++){
//            assertEquals("  LSAT 7 discrimination test", mirtDiscrimination[j], Precision.round(irm[j].getDiscrimination(), 2), 1e-1);
//            assertEquals("  LSAT 7 difficulty test", mirtDifficulty[j], Precision.round(irm[j].getDifficulty(),2), 1e-1);
//            assertEquals("  LSAT 7 guessing test", mirtGuessing[j], Precision.round(irm[j].getGuessing(),2), 1e-1);
        }
    }

    /**
     * True results obtained using ICL with the following syntax.
     *
     * options -D 1.7
     * options -default_prior_a {beta 1.75 3.0 0.0 3.0}
     * options -default_prior_b {beta 1.01 1.01 -6.0 6.0}
     * options -default_prior_c {beta 2 4 0 1}
     * allocate_items_dist 5
     * read_examinees lsat7-icl-data.txt 5i1
     * output -log_file lsat7-ICL-results.log
     * starting_values_dichotomous
     * EM_steps -max_iter 500 -crit 0.0001
     * print -item_param -latent_dist
     * release_items_dist
     *
     */
//    @Ignore
    @Test
    public void lsat7Test(){
        System.out.println("LSAT 7 - ICL test 3PL");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, true);

        //parameter estimates from ICL
        double[][] iclParam = {
                {0.656618,-1.416491,0.208918},
                {0.976487,-0.130782,0.261682},
                {1.153071,-0.790021,0.175383},
                {0.501697,-0.228392,0.140632},
                {0.497099,-1.820799,0.238240}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[5];
        Irm3PL pl3 = null;
        for(int j=0;j<5;j++) {
            pl3 = new Irm3PL(1.0, 0.0, 0.1, 1.7);
            pl3.setName(new VariableName("item"+(j+1)));
            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));
            pl3.setGuessingPrior(new ItemParamPriorBeta4(2.0, 4.0, 0.0, 1.0));
            irm[j] = pl3;
        }

        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();

        StartingValues startValues = new StartingValues(responseData, irm);
        startValues.addEMStatusListener(emStatus);
        irm = startValues.computeStartingValues();

        //set latent quadrature
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);

        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());

        for(int j=0;j<5;j++){
            assertEquals("  LSAT 7 discrimination test", iclParam[j][0], irm[j].getDiscrimination(), 1e-2);
            assertEquals("  LSAT 7 difficulty test", iclParam[j][1], irm[j].getDifficulty(), 1e-2);
            assertEquals("  LSAT 7 guessing test", iclParam[j][2], irm[j].getGuessing(), 1e-2);
        }

    }


    /**
     * True results obtained using ICL with the following syntax.
     *
     * options -D 1.7
     * options -default_prior_a {beta 1.75 3.0 0.0 3.0}
     * options -default_prior_b {beta 1.01 1.01 -6.0 6.0}
     * options -default_prior_c {beta 2 4 0 1}
     * allocate_items_dist 5
     * #2PL model
     * for {set i 1} {$i <= 5} {incr i} {
     *     item_set_model $i 2PL
     *     }
     * read_examinees lsat7-icl-data.txt 5i1
     * output -log_file lsat7-latent-dist-ICL-results.log
     * starting_values_dichotomous
     *
     * EM_steps -max_iter 1000 -crit 0.001
     * print -item_param -latent_dist
     * release_items_dist
     *
     */
//    @Ignore
    @Test
    public void lsat72PLTest(){
        System.out.println("LSAT 7 - ICL test 2PL");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, true);

        //parameter estimates from ICL
        double[][] iclParam = {
                {0.590420,	-1.857013},
                {0.637055,	-0.746405},
                {0.984018,	-1.067461},
                {0.457501,	-0.626973},
                {0.441581,	-2.478917}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[5];
        Irm3PL pl3 = null;
        for(int j=0;j<5;j++) {
            pl3 = new Irm3PL(1.0, 0.0, 1.7);
            pl3.setName(new VariableName("item"+(j+1)));
            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        //set latent quadrature
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-3, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());
//        System.out.println();
//        System.out.println(mmle.printLatentDistribution());
        mmle.computeG2ItemFit(10, 5);
//        mmle.computeSX2ItemFit(1);
        System.out.println(mmle.printItemFitStatistics());

        for(int j=0;j<5;j++){
            assertEquals("  LSAT 7 discrimination test", iclParam[j][0], irm[j].getDiscrimination(), 1e-2);
            assertEquals("  LSAT 7 difficulty test", iclParam[j][1], irm[j].getDifficulty(), 1e-2);
        }

    }

    @Test
    public void lemond3plTest(){
        System.out.println("Lemond data 3PL comparison to flexMIRT");

        int nItems = 40;

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lemond-jan15.csv"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 2, nItems, true);

        //parameter estimates from flexMIRT
        double[][] flexMIRTParam = {

        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        Irm3PL pl3 = null;
        for(int j=0;j<nItems;j++) {
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.0);
            pl3.setName(new VariableName("item"+(j+1)));
//            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
//            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));
            pl3.setGuessingPrior(new ItemParamPriorBeta4(2.0, 4.0, 0.0, 1.0));

//            pl3.setDiscriminationPrior(new ItemParamPriorLogNormal(0, 0.25));
//            pl3.setGuessingPrior(new ItemParamPriorBeta4(1.0, 4.0, 0.0, 1.0));
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        //set latent quadrature
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(49, -6, 6, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());
//        System.out.println();
//        System.out.println(mmle.printLatentDistribution());
//        mmle.computeSX2ItemFit(1);
//        System.out.println(mmle.printItemFitStatistics());

        for(int j=0;j<5;j++){
//            assertEquals("  LSAT 7 discrimination test", flexMIRTParam[j][0], irm[j].getDiscrimination(), 1e-2);
//            assertEquals("  LSAT 7 difficulty test", flexMIRTParam[j][1], irm[j].getDifficulty(), 1e-2);
        }

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
            pl3.setGuessingPrior(new ItemParamPriorBeta4(5, 17, 0.0, 1.0));
//            pl3.setGuessingPrior(new ItemParamPriorBeta(5, 17));
            irm[j] = pl3;
        }


        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);



        StartingValues startingValues = new StartingValues(responseData, irm);
        irm = startingValues.computeStartingValues();

        //Gauss-Hermite quadrature
//        HermiteRuleFactory gaussHermite = new HermiteRuleFactory();
//        Pair<double[], double[]> dist = gaussHermite.getRule(41);
//        UserSuppliedQuadratureRule latentDistribution = new UserSuppliedQuadratureRule(dist.getKey(), dist.getValue());

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-3, 150);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

    /**
     * This example uses a 3PL with the guessing parameter fixed to 0.2 for all items.
     *
     * Test compares results to those obtained from ICL. The ICL command file was
     *
     * options -D 1.7
     * options -default_prior_a {beta 1.01 1.01 -6.0 6.0}
     * options -default_prior_b none
     * options -default_prior_c {beta 2 4 0 1}
     * allocate_items_dist 50
     * #3PL model with a fixed c parameter
     * for {set i 1} {$i <= 50} {incr i} {
     * item_set_model $i 2PL
     * item_set_all_params $i {1.0 0.0 0.2}
     * }
     * read_examinees binary-items-for-ICL.txt 50i1
     * output -log_file binary-items-ICL-fixed-c-results.log
     * starting_values_dichotomous
     * EM_steps -max_iter 150 -crit 0.000001
     * print -item_param -latent_dist
     * release_items_dist
     *
     */
//    @Ignore
    @Test
    public void binaryItems3plICLfixedCtest(){
        System.out.println("Binary items - 3PL with fixed guessing parameter - Compare to ICL");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

        //True estimates from ICL
        double[][] iclParam = {
                {0.753243,-1.25146,0.2},
                {1.172005,1.178959,0.2},
                {0.621761,0.499494,0.2},
                {1.334299,-0.609813,0.2},
                {1.005957,-0.210655,0.2},
                {1.004226,-0.723519,0.2},
                {0.922973,-0.722835,0.2},
                {0.642407,0.917317,0.2},
                {0.819275,-1.211188,0.2},
                {1.691908,-1.86196,0.2},
                {0.833922,-1.895036,0.2},
                {2.542468,1.828651,0.2},
                {0.8098,0.302514,0.2},
                {0.946368,0.02652,0.2},
                {1.047066,0.663787,0.2},
                {1.177395,-1.685991,0.2},
                {1.031839,-1.089033,0.2},
                {0.867024,-0.147639,0.2},
                {1.046935,-0.254322,0.2},
                {0.993559,0.02616,0.2},
                {1.063248,-2.96568,0.2},
                {0.800373,-1.664188,0.2},
                {1.072514,-0.724804,0.2},
                {0.675943,-0.029493,0.2},
                {1.032605,1.19516,0.2},
                {0.883897,-0.96857,0.2},
                {0.357122,3.45154,0.2},
                {1.04355,-0.038921,0.2},
                {1.004331,-0.288815,0.2},
                {1.162701,0.277854,0.2},
                {0.870907,-0.13353,0.2},
                {0.926115,-0.529088,0.2},
                {0.973634,-1.098898,0.2},
                {1.028151,1.468799,0.2},
                {0.960964,-1.198908,0.2},
                {0.586454,-0.499466,0.2},
                {1.202391,0.05982,0.2},
                {0.834715,-0.14947,0.2},
                {1.568002,1.041551,0.2},
                {0.622932,2.328366,0.2},
                {0.823863,-0.629523,0.2},
                {0.72739,-2.024093,0.2},
                {1.049989,0.612172,0.2},
                {1.665784,-0.548054,0.2},
                {1.027447,0.835562,0.2},
                {0.619778,-0.298944,0.2},
                {1.050077,-0.721784,0.2},
                {1.716188,1.601999,0.2},
                {1.053867,0.427054,0.2},
                {1.037644,0.33425,0.2}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[50];
        Irm3PL pl3 = null;
        for(int j=0;j<50;j++) {
            //3PL with no priors
            pl3 = new Irm3PL(1.0, 0.0, 1.7);//2PL model
            pl3.setGuessing(0.2);//set guessing parameter to 0.2
            pl3.setName(new VariableName("item"+(j+1)));
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-6, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());

        for(int j=0;j<50;j++){
            assertEquals("Binary items - discrimination test", iclParam[j][0], irm[j].getDiscrimination(), 1e-3);
            assertEquals("Binary items - difficulty test", iclParam[j][1], irm[j].getDifficulty(), 1e-3);
            assertEquals("Binary items - guessing test", iclParam[j][2], irm[j].getGuessing(), 1e-8);
        }

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
//    @Ignore
    @Test
    public void binaryItems3plICLtest(){
        System.out.println("Binary items - 3PL with Guessing Prior - Compare to ICL");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

        System.out.println("Unique response vectors: " + responseData.length);

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
            pl3.setName(new VariableName("item"+(j+1)));
            pl3.setGuessingPrior(new ItemParamPriorBeta4(5, 17, 0.0, 1.0));
            irm[j] = pl3;
        }

        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();

        StartingValues startValues = new StartingValues(responseData, irm);
        startValues.addEMStatusListener(emStatus);
        irm = startValues.computeStartingValues();

        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(0.0001, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());

        for(int j=0;j<50;j++){
            assertEquals("Binary items - discrimination test", icl_param[j][0], irm[j].getDiscrimination(), 1e-3);
            assertEquals("Binary items - difficulty test", icl_param[j][1], irm[j].getDifficulty(), 1e-3);
            assertEquals("Binary items - guessing test", icl_param[j][2], irm[j].getGuessing(), 1e-3);
        }
//        assertEquals("Binary items - loglikelihood", icl_loglike, mmle.completeDataLogLikelihood(), 1e-2);

    }

    /**
     * BILOG estimates obtained with the following commands:
     *
     * >GLOBAL DFName = 'binary-items.txt',
     * NPArm = 3, SAVE;
     * >SAVE PARM='binary-items-ipar.txt';
     * >LENGTH NITEMS=50;
     * >INPUT NTOtal=50, NIDCHAR = 4;
     * >ITEMS INAMES=(ITEM01(1)ITEM50);
     * >TEST1 TNAme = 'BINITEMS', INUMBER = (1(1)50);
     * (4A1, 50A1)
     * >CALIB NQPT=40, CYCLES=250, NEWTON=100, CRIT=0.001, ACCel = 1.0000,
     *  CHI=15, NOTPRIOR, NOSPRIOR, GPRIOR, NOADJUST;
     * >SCORE ;
     *
     */
//    @Ignore
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
        for(int j=0;j<50;j++) {
            //3PL with no priors
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.7);//Starting value of guessing parameter must be > 0.
            pl3.setName(new VariableName("Item" + (j+1)));
            pl3.setGuessingPrior(new ItemParamPriorBeta4(5, 17, 0.0, 1.0));
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(0.000001, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());
        System.out.println();
        System.out.println();
        System.out.println(mmle.printLatentDistribution());

        mmle.computeG2ItemFit(20, 5);
//        mmle.computeSX2ItemFit(1);
        System.out.println(mmle.printItemFitStatistics());

        mmle.computeRaschItemFit();


        for(int j=0;j<50;j++){
            assertEquals("Binary items - discrimination test", bilog_param[j][0], irm[j].getDiscrimination(), 1e-2);
            assertEquals("Binary items - difficulty test", bilog_param[j][1], irm[j].getDifficulty(), 1e-2);
            assertEquals("Binary items - guessing test", bilog_param[j][2], irm[j].getGuessing(), 1e-2);
        }

    }



    /**
     * ICL results obtained with the syntax below
     *
     * options -D 1.7
     * options -default_prior_a none
     * options -default_prior_b none
     * options -default_prior_c none
     * allocate_items_dist 50
     *
     * #2PL model
     * for {set i 1} {$i <= 50} {incr i} {
     *   item_set_model $i 2PL
     * }
     *
     * read_examinees binary-items-for-ICL.txt 50i1
     * output -log_file binary-items-ICL-results-2PL.log
     * starting_values_dichotomous
     *
     * EM_steps -max_iter 500 -crit 0.0001
     * print -item_param -latent_dist
     * release_items_dist
     *
     */
//    @Ignore
    @Test
    public void binaryItems2plICLtest(){
        System.out.println("Binary items - 2PL - Compare to ICL");

        double[][] iclResults = {
                {0.751109,-1.523248},
                {0.538252,0.848959},
                {0.435915,-0.119467},
                {1.181007,-0.902456},
                {0.777044,-0.653776},
                {0.946688,-1.018769},
                {0.849118,-1.054501},
                {0.381066,0.322156},
                {0.821167,-1.458939},
                {1.930503,-1.787354},
                {0.910643,-1.961909},
                {0.426441,2.280098},
                {0.591198,-0.204478},
                {0.719904,-0.428744},
                {0.588193,0.196856},
                {1.309048,-1.712498},
                {0.996928,-1.338184},
                {0.680659,-0.619818},
                {0.829899,-0.671139},
                {0.728285,-0.436629},
                {1.352144,-2.585487},
                {0.834645,-1.827876},
                {0.974762,-1.035047},
                {0.529633,-0.584465},
                {0.460035,0.845607},
                {0.840473,-1.267442},
                {0.135966,3.723481},
                {0.815774,-0.454808},
                {0.854626,-0.670895},
                {0.77507,-0.1596},
                {0.69961,-0.585164},
                {0.797688,-0.920876},
                {0.958384,-1.341814},
                {0.421079,1.219961},
                {0.961903,-1.416994},
                {0.512207,-1.029688},
                {0.85889,-0.360972},
                {0.660225,-0.626941},
                {0.672394,0.729608},
                {0.192848,2.733837},
                {0.727908,-1.031053},
                {0.783747,-2.118898},
                {0.630837,0.1525},
                {1.353108,-0.85117},
                {0.588214,0.39676},
                {0.52289,-0.837342},
                {0.946251,-1.044967},
                {0.497776,1.615799},
                {0.714291,-0.0189},
                {0.73233,-0.111437}
        };

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
            pl3.setName(new VariableName("item"+(j+1)));
            irm[j] = pl3;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());

        for(int j=0;j<50;j++){
            assertEquals("Binary items - discrimination test", iclResults[j][0], irm[j].getDiscrimination(), 1e-2);
            assertEquals("Binary items - difficulty test", iclResults[j][1], irm[j].getDifficulty(), 1e-2);
        }

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
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(min, max, (int)quadPoints);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-4, 250);
        mmle.computeItemStandardErrors();
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
            pl3.setGuessingPrior(new ItemParamPriorBeta4(1.0, 1.0, 0.0, 1.0));//vague prior to keep c parameter within acceptable range
            irm[j] = pl3;
        }

        //mirt R package default quadrature
//        double quadPoints = 41;
//        double min = -.8 * Math.sqrt(quadPoints);
//        double max = -1*min;
//        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(min, max, (int)quadPoints);

        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //Estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-4, 250);
        mmle.computeItemStandardErrors();
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
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.7);//3PL - Starting value of guessing parameter must be > 0.
            pl3.setGuessingPrior(new ItemParamPriorBeta4(3.5, 4.0, 0.0, 0.5));
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6, 6));
            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            pl3.setName(new VariableName("V"+(j+1)));
            irm[j] = pl3;
        }

        //ICL default quadrature
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-3, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());
//        System.out.println();
//        mmle.computeItemFit(10, 5);
//        System.out.println(mmle.printItemFitStatistics());
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
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule((int)quadPoints, min, max, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-4, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

    /**
     * For running simulations
     */
//    @Test
    public void simulationRun(){
        System.out.println("Running simulation...");
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();

        int nPeople = 5000;
        int nItems = 60;
        int nrep = 1000;

        String inputPath = "S:\\2014-3pl-simulation\\simdata\\condition6";
//        String outputPath = inputPath + "\\jmetrik-output";
        String outputPath = inputPath + "\\jmetrik-output-default-priors";
        String dataFile = "";

        for(int r=0;r<nrep;r++){
            dataFile = "\\cond6_" + (r+1) + ".csv";
            ItemResponseVector[] responseData = fileSummary.getResponseVectors(new File(inputPath + dataFile), 1, nItems, false);

            //Create array of item response models
            ItemResponseModel[] irm = new ItemResponseModel[nItems];
            Irm3PL pl3 = null;
            for(int j=0;j<nItems;j++) {
                pl3 = new Irm3PL(1.0, 0.0, 1.0);
                pl3.setName(new VariableName("item"+(j+1)));

                //Parscale priors
//                pl3.setDiscriminationPrior(new ItemParamPriorLogNormal(0, 0.5));

                //Default priors
                pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3, 0, 3));
                pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6, 6));//uninformative
                irm[j] = pl3;
            }

            //computation of quadrature points as done in the mirt R package
            double quadPoints = 40;
            NormalQuadratureRule latentDistribution = new NormalQuadratureRule(-4.0, 4.0, (int)quadPoints);

            //compute start values
            StartingValues startValues = new StartingValues(responseData, irm);
            irm = startValues.computeStartingValues();

            //estimate parameters
            MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
            mmle.estimateParameters(0.001, 500);
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
            NormalQuadratureRule latentDistribution = new NormalQuadratureRule(min, max, (int)quadPoints);

            //compute start values
            StartingValues startValues = new StartingValues(responseData, irm);
            irm = startValues.computeStartingValues();

            //estimate parameters
            MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
//            DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
//            mmle.addEMStatusListener(emStatus);
            mmle.estimateParameters(1e-4, 250);
            mmle.computeItemStandardErrors();
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

    //@Test
    public void pl4Test(){
        System.out.println("4PL Model with priors");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 50, true);

//        for(int i=0;i<responseData.length;i++){
//            System.out.println(responseData[i].toString());
//        }


        //Create item response model objects
        int nItems = 50;
        Irm4PL model = null;
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        for(int j=0;j<nItems;j++){
            model = new Irm4PL(1.0, 0.0, 0.05, 1.0, 1.7);//Starting value of guessing parameter must be > 0.
            model.setName(new VariableName("Item"+(j+1)));
            model.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            model.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));
            model.setGuessingPrior(new ItemParamPriorBeta4(3.5, 4.0, 0.0, 0.5));
            model.setSlippingPrior(new ItemParamPriorBeta4(8.0, 2.0, 0.6, 1.0));
            irm[j] = model;
        }

        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

//        System.out.println(startValues.toString());

        //ICL default quadrature
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-3, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());

    }


    /**
     * Data from a 10 items test. Each item has four categories (m = 4).
     *
     * "True" results obtained from ICL using the syntax below.
     *
     * output -log_file gpcm-items-ICL-results.log
     * options -D 1.0
     * #options -default_prior_a none
     * options -default_prior_b none
     *
     * set model [rep 4 10]
     * allocate_items_dist 10 -models $model
     *
     * #In next line, remove the backslash before the at symbol. It is only here as an escape character.
     * read_examinees gpcm-data1-for-ICL.txt {\@6 10i1}
     *
     * EM_steps -max_iter 500 -crit 0.0001
     * print -item_param -latent_dist
     * release_items_dist
     *
     */
//    @Ignore
    @Test
    public void gpcmTest(){
        System.out.println("Generalized Partial Credit Model Test: ICL priors");

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/polytomous-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, 10, false);


        double[][] iclResults = {
                {0.927822,	-1.763684,	-0.898556,	-0.181566},
                {0.620401,	-0.972255,	0.224909,	0.385900},
                {1.313718,	-0.379549,	0.544892,	0.765030},
                {1.250918,	1.492902,	1.949191,	2.804106},
                {0.817850,	-1.135393,	-0.585898,	0.302453},
                {0.776403,	-1.273724,	-0.870685,	0.108329},
                {1.295521,	-1.068966,	-0.257613,	-0.052401},
                {0.593552,	-0.246737,	0.270074,	1.736404},
                {0.557360,	-1.442603,	-0.198555,	-0.076345},
                {1.081145,	-1.281159,	-0.850170,	-0.285623}
        };

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[10];
        IrmGPCM gpcm = null;
        double[] initialStep = {0,0,0,0};
        for(int j=0;j<10;j++) {
            gpcm = new IrmGPCM(1.0, initialStep, 1.0);
            gpcm.setName(new VariableName("Item" + (j+1)));
            for(int k=0;k<initialStep.length;k++){
                gpcm.setStepPriorAt(new ItemParamPriorBeta4(1.01, 1.01, -6, 6), k);
            }

            irm[j] = gpcm;
        }

        //ICL default quadrature
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());

        double[] step = null;
        for(int j=0;j<iclResults.length;j++){
            step = irm[j].getStepParameters();
            assertEquals("GPCM items - discrimination test", iclResults[j][0], irm[j].getDiscrimination(), 1e-2);
            assertEquals("GPCM items - step 1 test", iclResults[j][1], step[1], 1e-2);
            assertEquals("GPCM items - Step 2 test", iclResults[j][2], step[2], 1e-2);
            assertEquals("GPCM items - Step 3 test", iclResults[j][3], step[3], 1e-2);
        }

        mmle.computeG2ItemFit(10, 0);
//        mmle.computeSX2ItemFit(1);
        System.out.println(mmle.printItemFitStatistics());

    }

    /**
     * True results obtained from ICL using teh following syntax.
     *
     * output -log_file pcm-items-ICL-results.log
     * options -D 1.0
     * options -default_model_polytomous PCM
     * set model [rep 4 10]
     * allocate_items_dist 10 -models $model
     * read_examinees gpcm-data1-for-ICL.txt {\@6 10i1}
     * EM_steps -max_iter 500 -crit 0.0001
     * print -item_param -latent_dist
     * write_item_param pcm-items-output.txt
     * release_items_dist
     *
     */
//    @Ignore
    @Test
    public void pcmTest(){
        System.out.println("Partial Credit Model Test: ICL priors");

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
            pcm = new IrmPCM2(initialStep, 1.0);
            pcm.setName(new VariableName("Item" + (j+1)));
            for(int k=0;k<initialStep.length;k++){
                pcm.setStepPriorAt(new ItemParamPriorBeta4(1.01, 1.01, -6, 6), k);
            }
            irm[j] = pcm;
        }

        //ICL default quadrature
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(40, -4, 4, 0, 1);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 250);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());
//        System.out.println();
//        System.out.println(mmle.printLatentDistribution());
//        System.out.println();
        mmle.computeG2ItemFit(10, 5);
//        mmle.computeSX2ItemFit(1);
        System.out.println(mmle.printItemFitStatistics());

        double[] step = null;
        for(int j=0;j<iclResults.length;j++){
            step = irm[j].getStepParameters();
            assertEquals("PCM items - step 1 test", iclResults[j][0], step[1], 1e-3);
            assertEquals("PCM items - Step 2 test", iclResults[j][1], step[2], 1e-3);
            assertEquals("PCM items - Step 3 test", iclResults[j][2], step[3], 1e-3);
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
    public void flexmirtTest1(){

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/jalabert-jan15.csv"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, true);

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

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[20];
        IrmGPCM gpcm = null;
        double[] initialStep = {0,0,0,0};
        for(int j=0;j<20;j++) {
            gpcm = new IrmGPCM(1.0, initialStep, 1.0);
            gpcm.setName(new VariableName("Item" + (j+1)));
            irm[j] = gpcm;
        }

        //flexmirt default quadrature
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(-6.0, 6.0, 49);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 500);
        mmle.computeItemStandardErrors();
        System.out.println();
        System.out.println(mmle.printItemParameters());
//        System.out.println();
//        System.out.println(mmle.printLatentDistribution());
//        System.out.println();
//        mmle.computeG2ItemFit(10, 5);
//        mmle.computeSX2ItemFit(1);
//        System.out.println(mmle.printItemFitStatistics());

        double[] step = null;
        for(int j=0;j<discrim.length;j++){
            step = irm[j].getStepParameters();
            assertEquals("GPCM items - disc   test", discrim[j], irm[j].getDiscrimination(), 1e-3);
            assertEquals("GPCM items - step 1 test", steps[j][1], step[1], 1e-3);
            assertEquals("GPCM items - Step 2 test", steps[j][2], step[2], 1e-3);
            assertEquals("GPCM items - Step 3 test", steps[j][3], step[3], 1e-3);
        }


    }


}
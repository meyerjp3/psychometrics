package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.distribution.NormalDistributionApproximation;
import com.itemanalysis.psychometrics.distribution.UserSuppliedDistributionApproximation;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmPCM2;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
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
        DistributionApproximation latentDistribution = new UserSuppliedDistributionApproximation(points, density);

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
        DistributionApproximation latentDistribution = new UserSuppliedDistributionApproximation(points, density);

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
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();

        System.out.println(irtObservedScoreDistribution.toString());

    }

    @Test
    public void partialCreditModelTest(){
        System.out.println("Partial Credit Model Observed score distribution test");

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
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);

        IrtObservedScoreDistribution irtObservedScoreDistribution = new IrtObservedScoreDistribution(irm, latentDistribution);
        irtObservedScoreDistribution.compute();

        System.out.println(irtObservedScoreDistribution.toString());


    }



}
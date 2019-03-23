/**
 * Copyright 2016 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.quadrature.ContinuousQuadratureRule;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.quadrature.NormalQuadratureRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class MMLELatentDistributionTests {

    /**
     * Uses LSAT6 data to estimate the latent density.
     * Items use a Rasch model with difficulty parameters fixed to values from Baker and Kim (2004).
     * The EM algorithm is forced to use 50 iterations, but only the latent density is updated.
     */
    @Test
    public void LSAT6EstimateLatentDensityTest(){
        System.out.println("LSAT6 data 2PL model with fixed item parameters; estimate latent density");

        //LSAT 6 response patterns and frequency weights
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

        //Create response vectors
        ItemResponseVector[] responseData = new ItemResponseVector[u.length];
        for(int i=0;i<u.length;i++){
            responseData[i] = new ItemResponseVector(u[i], fpt[i]);
        }

        //Create array of Rasch item response models
        ItemResponseModel[] irm = new ItemResponseModel[5];

        //From Baker and Kim. Uses Rasch model. Fixed item difficulty values
        irm[0] = new Irm3PL(-1.256, 1.0);
        irm[0].setFixed(true);
        irm[0].setName(new VariableName("item1"));

        irm[1] = new Irm3PL(0.475, 1.0);
        irm[1].setFixed(true);
        irm[1].setName(new VariableName("item2"));

        irm[2] = new Irm3PL(1.236, 1.0);
        irm[2].setFixed(true);
        irm[2].setName(new VariableName("item3"));

        irm[3] = new Irm3PL(0.168, 1.0);
        irm[3].setFixed(true);
        irm[3].setName(new VariableName("item4"));

        irm[4] = new Irm3PL(-0.623, 1.0);
        irm[4].setFixed(true);
        irm[4].setName(new VariableName("item5"));

        //Twenty evenly spaced quadrature points from -4.75 to 4.75 as done by Baker and Kim.
        double quadPoints = 20;
        double min = -4.75;
        double max =  4.75;
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(min, max, (int)quadPoints);

        //Setup estimation object
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        EMStatusListener statusListener = new DefaultEMStatusListener();
        mmle.addEMStatusListener(statusListener);

        //NOTE: setting convergence criterion to a negative number will force the algorithm to iterate until the maximum number of iterations.
        //To check results with Baker and Kim, force the program to use 50 iterations of the EM algorithm. Only latent density is estimated.
        mmle.estimateParameters(-1, 50, DensityEstimationType.EMPIRICAL_HISTOGRAM_FREE);
        mmle.computeItemStandardErrors();

        //Display the results
        System.out.println(mmle.printItemParameters());

        //Display estimated mean and SD of latent quadrature
        QuadratureRule dist = mmle.getLatentDistribution();
        System.out.println(" Latent mean = " + Precision.round(dist.getMean(), 4));
        System.out.println(" Latent s.d. = " + Precision.round(dist.getStandardDeviation(), 4));
        System.out.println();

        //True values from Baker and Kim
        assertEquals("  LSAT 6 latent mean test", 1.47518, dist.getMean(), 1e-2);
        assertEquals("  LSAT 6 latent s.d. test", 0.75601, dist.getStandardDeviation(), 1e-1);

    }

    /**
     * Data simulated using a bimodal latent density. Details of this file are listed in the README.txt file.
     * Latent density is standardized. Therefore, estimates should have a mean of 0 and a standard deviation of 1.
     * Item parameter estimates compared to estimates from flexMIRT. The flexMIRT syntax was:
     *
     * <Project>
     * Title = "2PLM example bimodal latent density";
     * Description = "1 Group Calibration
     * saving parameter estimates";
     * <Options>
     * Mode = Calibration;
     * savePRM = YES;
     * <Groups>
     * %Group1%
     * File = "hampsten-feb16-flexmirt.csv";
     * Varnames = v1-v31;
     * Select = v2-v31;
     * N = 2000;
     * Ncats(v2-v31) = 2;
     * Model(v2-v31) = Graded(2);
     * EmpHist = Yes;
     * <Constraints>
     *
     */
    @Test
    public void simBimodalLatentDensityTest(){
        System.out.println("Bimodal latent density test; 2PL model with estimated latent density");
        int nItems = 30;

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/hampsten-feb16.csv"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 2, nItems, true);


        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        Irm3PL pl3 = null;
        for(int j=0;j<nItems;j++){
            pl3 = new Irm3PL(0.0, 1.0, 1.0);//2PL model
            pl3.setName(new VariableName("item" + (j+1)));
            irm[j] = pl3;
        }

        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(49, -6, 6, 0, 1);

        StartingValues startingValues = new StartingValues(responseData, irm);
        irm = startingValues.computeStartingValues();

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 250, DensityEstimationType.EMPIRICAL_HISTOGRAM_STANDARDIZED_KEEP_POINTS);
        mmle.computeItemStandardErrors();

//        System.out.println();
//        System.out.println(mmle.printItemParameters());
//        System.out.println(mmle.printLatentDistribution());

        assertEquals("  Bimodal latent mean test", 0.0, mmle.getLatentDistribution().getMean(), 1e-4);
        assertEquals("  Bimodal latent SD test", 1.0, mmle.getLatentDistribution().getStandardDeviation(), 1e-4);

        double[] aparamFlexMIRT = {
                1.14,1.21,0.35,1.67,0.97,0.52,1.72,1.13,0.65,1.14,
                2.55,1.57,1.98,1.08,1.37,0.20,0.75,0.89,0.63,0.89,
                1.09,0.60,1.13,2.17,1.88,2.25,2.07,0.89,0.91,0.52};

        double[] bparamFlexMIRT = {
                1.92,-1.53,-0.32,2.72,1.12,0.29,1.01,-0.24,-0.12,0.41,
                -0.33,-0.37,0.75,-0.64,1.66,-0.79,0.36,-1.60,-1.34,-0.79,
                0.30,-0.55,1.12,-0.43,-0.99,0.29,-0.27,0.16,-1.36,1.4};


        for(int j=0;j<nItems;j++){
            assertEquals("  Bimodal discrimination test", aparamFlexMIRT[j], Precision.round(irm[j].getDiscrimination(), 2), 1e-2);
            assertEquals("  Bimodal difficulty test", bparamFlexMIRT[j], Precision.round(irm[j].getDifficulty(),2), 1e-2);
        }

    }

    /**
     * Data simulated using a negatively skewed latent density. Details of this file are listed in the README.txt file.
     * Latent density is standardized. Therefore, estimates should have a mean of 0 and a standard deviation of 1.
     * Item parameter estimates compared to estimates from flexMIRT. The flexMIRT syntax was:
     *
     * <Project>
     * Title = "2PLM example";
     * Description = "1 Group Calibration
     * saving parameter estimates";
     * <Options>
     * Mode = Calibration;
     * savePRM = YES;
     * <Groups>
     * %Group1%
     * File = "phinney-feb16-flexmirt.csv";
     * Varnames = v1-v31;
     * Select = v2-v31;
     * N = 2000;
     * Ncats(v2-v31) = 2;
     * Model(v2-v31) = Graded(2);
     * EmpHist = Yes;
     * <Constraints>
     *
     */
    @Test
    public void simNegativeSkewLatentDensityTest(){
        System.out.println("Negative skew latent density test; 2PL model with estimated latent density");
        int nItems = 30;

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/phinney-feb16.csv"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 2, nItems, true);


        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        Irm3PL pl3 = null;
        for(int j=0;j<nItems;j++){
            pl3 = new Irm3PL(0.0, 1.0, 1.0);//2PL model
            pl3.setName(new VariableName("item" + (j+1)));
            irm[j] = pl3;
        }

        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(49, -6, 6, 0, 1);

        StartingValues startingValues = new StartingValues(responseData, irm);
        irm = startingValues.computeStartingValues();

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 250, DensityEstimationType.EMPIRICAL_HISTOGRAM_STANDARDIZED_KEEP_POINTS);
        mmle.computeItemStandardErrors();

        System.out.println();
        System.out.println(mmle.printItemParameters());
        System.out.println(mmle.printLatentDistribution());

        assertEquals("  Bimodal latent mean test", 0.0, mmle.getLatentDistribution().getMean(), 1e-4);
        assertEquals("  Bimodal latent SD test", 1.0, mmle.getLatentDistribution().getStandardDeviation(), 1e-4);

        double[] aparamFlexMIRT = {
                0.58,2.15,1.21,0.46,0.80,0.59,0.58,2.37,0.90,1.37,
                0.90,1.33,1.58,1.01,0.64,0.80,1.01,1.53,0.94,0.58,
                1.38,0.95,0.67,0.97,0.89,0.42,0.83,0.92,0.48,0.57};

        double[] bparamFlexMIRT = {
                0.68,0.73,0.88,-1.03,0.13,0.39,-0.09,-0.68,0.77,-0.05,
                -1.22,1.38,1.35,-0.94,-0.79,0.63,-0.69,0.11,1.02,-0.14,
                -0.21,0.64,0.25,0.03,0.06,-0.45,-0.89,-0.37,0.97,-2.04};


        for(int j=0;j<nItems;j++){
            assertEquals("  Negative skew discrimination test", aparamFlexMIRT[j], Precision.round(irm[j].getDiscrimination(), 2), 1e-1);
            assertEquals("  Negative skew difficulty test", bparamFlexMIRT[j], Precision.round(irm[j].getDifficulty(),2), 1e-1);
        }

    }


}

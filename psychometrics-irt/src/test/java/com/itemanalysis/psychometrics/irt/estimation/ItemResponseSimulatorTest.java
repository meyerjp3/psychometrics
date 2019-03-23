package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmGPCM;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.irt.simulation.ItemResponseSimulator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class ItemResponseSimulatorTest {

    @Test
    public void binaryResponseTest1(){
        System.out.println("ItemResponseSimulatorTest: binary items");
        int nPeople = 10;
        int nItems = 5;

        //Generate examinee ability parameters
        double[] theta = new double[nPeople];
        RandomDataGenerator random = new RandomDataGenerator();
        for(int i=0;i<nPeople;i++){
            theta[i] = random.nextGaussian(0, 1);
        }

        //Generate item response models
        ItemResponseModel model = null;
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] aparam = new double[nItems];
        double[] bparam = new double[nItems];
        for(int j=0;j<nItems;j++){
            aparam[j] = random.nextUniform(0.2, 2.2);
            bparam[j] = random.nextGaussian(0, 1);
            model = new Irm3PL(aparam[j], bparam[j], 1.0);
            irm[j] = model;
        }

        //Generate data
        ItemResponseSimulator simulator = new ItemResponseSimulator(theta, irm);
        byte[][] x = simulator.generateData();

        //Display results
        System.out.println("Item Discrimination: " + Arrays.toString(aparam));
        System.out.println("Item Difficulty: " + Arrays.toString(aparam));
        System.out.println("Person Theta:" + Arrays.toString(theta));
        System.out.println("Data:");
        for(int i=0;i<nPeople;i++){
            for(int j=0;j<nItems;j++){
                System.out.print(x[i][j]);
            }
            System.out.println();
        }

    }

//    @Test
    public void simulateDataToFileTest(){
        System.out.println("ItemResponseSimulatorTest: binary items to file");
        int nPeople = 10;
        int nItems = 5;

        //Generate examinee ability parameters
        double[] theta = new double[nPeople];
        RandomDataGenerator random = new RandomDataGenerator();
        for(int i=0;i<nPeople;i++){
            theta[i] = random.nextGaussian(0, 1);
        }

        //Generate item response models
        ItemResponseModel model = null;
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] aparam = new double[nItems];
        double[] bparam = new double[nItems];
        for(int j=0;j<nItems;j++){
            aparam[j] = random.nextUniform(0.2, 2.2);
            bparam[j] = random.nextGaussian(0, 1);
            model = new Irm3PL(aparam[j], bparam[j], 1.0);
            irm[j] = model;
        }

        //Generate data and write to a CSV file
        ItemResponseSimulator simulator = new ItemResponseSimulator(theta, irm);
        try{
            simulator.generateData(".\\junittest-ItemResponseSimulator.csv", true, true);
        }catch(IOException ex){
            ex.printStackTrace();
        }

    }

    @Test
    public void simulateMixedFormat(){
        System.out.println("ItemResponseSimulatorTest: mixed format test");
        int nPeople = 500;
        int nItems = 6;

        //Generate examinee ability parameters
        double[] theta = new double[nPeople];
        RandomDataGenerator random = new RandomDataGenerator();
        for(int i=0;i<nPeople;i++){
            theta[i] = random.nextGaussian(0, 1);
        }

        //Use 2005 GRade 8 NAEP Science test item parameters for blocks S3 and S4
        ItemResponseModel[] irm = new ItemResponseModel[nItems];

        irm[0] = new Irm3PL(0.59, -0.30, 1.7);
        irm[1] = new Irm3PL(0.71, -0.73, 1.7);
        irm[2] = new Irm3PL(0.71,  0.25, 1.7);

        //NOTE: For an M category item, the first step parameter is always 0
        //NAEP uses PARSCALE parameterization (difficulty - threshold). Convert to step parameters
        double[] step3 = {0, (0.31-0.1), (0.31+0.1)};
        irm[3] = new IrmGPCM(0.47,  step3, 1.7);

        double[] step4 = {0, (-0.12-1.78), (-0.12+1.78)};
        irm[4] = new IrmGPCM(0.96,  step4, 1.7);

        double[] step5 = {0, (1.13+0.23), (1.13-0.23)};
        irm[5] = new IrmGPCM(0.68,  step5, 1.7);

        //Generate data
        ItemResponseSimulator simulator = new ItemResponseSimulator(theta, irm);
        byte[][] x = simulator.generateData();

        //Display results
        for(int i=0;i<nPeople;i++){
            for(int j=0;j<nItems;j++){
                System.out.print(x[i][j]);
            }
            System.out.println();
        }



    }

//    @Test
    public void simulateMultipleDataFiles(){
        System.out.println("ItemResponseSimulatorTest: multiple data files");
        int nPeople = 200;
        int nItems = 6;
        int nreps = 5;

        //Generate examinee ability parameters
        double[] theta = new double[nPeople];
        RandomDataGenerator random = new RandomDataGenerator();
        for(int i=0;i<nPeople;i++){
            theta[i] = random.nextGaussian(0, 1);
        }

        //Use 2005 Grade 8 NAEP Science test item parameters for blocks S3 and S4
        ItemResponseModel[] irm = new ItemResponseModel[nItems];

        //S3===============================================================================
        irm[0] = new Irm3PL(0.59, -0.30, 1.7);
        irm[1] = new Irm3PL(0.71, -0.73, 1.7);
        irm[2] = new Irm3PL(0.71,  0.25, 1.7);

        //NAEP uses PARSCALE parameterization (difficulty - threshold).
        //Convert to step parameters.
        double[] step3 = {0, (0.31-0.1), (0.31- -0.1)};
        irm[3] = new IrmGPCM(0.47,  step3, 1.7);

        double[] step4 = {0, (-0.12-1.78), (-0.12- -1.78)};
        irm[4] = new IrmGPCM(0.96,  step4, 1.7);

        double[] step5 = {0, (1.13+0.23), (1.13-0.23)};
        irm[5] = new IrmGPCM(0.68,  step5, 1.7);

        //S4===============================================================================
        double[] step6 = {0, (1.09+0.23), (1.09-0.23)};
        irm[5] = new IrmGPCM(0.65,  step6, 1.7);






        //Generate data
        ItemResponseSimulator simulator = new ItemResponseSimulator(theta, irm);
        try{
            simulator.generateData(".\\simdata", "sim", false, false, nreps);
        }catch(IOException ex){
            ex.printStackTrace();
        }



    }


}
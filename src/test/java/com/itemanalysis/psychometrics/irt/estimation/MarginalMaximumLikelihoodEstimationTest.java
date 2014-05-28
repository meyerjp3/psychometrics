package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.distribution.NormalDistributionApproximation;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.Frequency;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.*;

public class MarginalMaximumLikelihoodEstimationTest {

    public ItemResponseVector[] readSim5k(){
        int nPeople = 5000;
        byte[][] data = new byte[nPeople][30];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/sim-3pl-5K.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
//            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    data[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }

        Frequency freq = new Frequency();
        for(int i=0;i<data.length;i++){
            freq.addValue(Arrays.toString(data[i]));
        }

        ItemResponseVector[] responseData = new ItemResponseVector[freq.getUniqueCount()];
        ItemResponseVector irv = null;
        Iterator<Comparable<?>> iter = freq.valuesIterator();
        int index = 0;

        //create array of ItemResponseVector objects
        while(iter.hasNext()){
            //get response string from frequency summary and convert to byte array
            Comparable<?> value = iter.next();
            String s = value.toString();
            s = s.substring(1,s.lastIndexOf("]"));
            String[] sa = s.split(",");
            byte[] rv = new byte[sa.length];
            for(int i=0;i<sa.length;i++){
                rv[i] = Byte.parseByte(sa[i].trim());
            }

            //create response vector objects
            irv = new ItemResponseVector(rv, Long.valueOf(freq.getCount(value)).doubleValue());
            responseData[index] = irv;
            index++;
        }
//        //display results of summary
//        for(int i=0;i<responseData.length;i++){
//            System.out.println(responseData[i].toString() + ": " + responseData[i].getFrequency());
//        }

        return responseData;
    }

    @Test
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

        byte[][] u = {
                {0,0,0,0,0},
                {0,0,0,0,1},
                {0,0,0,1,0},
                {0,0,0,1,1},
                {0,0,1,0,0},
                {0,0,1,0,1},
                {0,0,1,1,0},
                {0,0,1,1,1},
                {0,1,0,0,0},
                {0,1,0,0,1},
                {0,1,0,1,0},
                {0,1,0,1,1},
                {0,1,1,0,0},
                {0,1,1,0,1},
                {0,1,1,1,0},
                {0,1,1,1,1},
                {1,0,0,0,0},
                {1,0,0,0,1},
                {1,0,0,1,0},
                {1,0,0,1,1},
                {1,0,1,0,0},
                {1,0,1,0,1},
                {1,0,1,1,0},
                {1,0,1,1,1},
                {1,1,0,0,0},
                {1,1,0,0,1},
                {1,1,0,1,0},
                {1,1,0,1,1},
                {1,1,1,0,0},
                {1,1,1,0,1},
                {1,1,1,1,0},
                {1,1,1,1,1}
        };

        double[] n = {12,19,1,7,3,19,3,17,10,5,3,7,7,23,8,28,7,39,11,34,14,51,15,90,6,25,7,35,18,136,32,308};

        //Create response vectors
        ItemResponseVector[] responseData = new ItemResponseVector[u.length];
        for(int i=0;i<u.length;i++){
            responseData[i] = new ItemResponseVector(u[i], n[i]);
        }

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[5];
        Irm3PL pl3 = null;
        for(int j=0;j<5;j++){
            //3PL with item priors
            pl3 = new Irm3PL(1.0, 0.0, 0.0, 1.0);//3PL
            irm[j] = pl3;
        }

        //computation of quadrature points as done in the mirt R package
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(min, max, (int)quadPoints);

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(1e-4, 150);
        System.out.println();
        System.out.println(mmle.printItemParameters());
    }

    @Test
    public void sim5Kmodel3plNoPrior(){
        System.out.println("Sim data 5K - 3PL no priors");
        ItemResponseVector[] responseData = readSim5k();

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

    @Test
    public void sim5Kmodel3plGuessPrior(){
        System.out.println("Sim data 5K - 3PL guessing prior");
        ItemResponseVector[] responseData = readSim5k();

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[30];
        Irm3PL pl3 = null;
        for(int j=0;j<30;j++) {
            //3PL with item priors
            pl3 = new Irm3PL(1.0, 0.0, 0.0, 1.0);
            pl3.setguessingPrior(new ItemParamPriorBeta4(1.0, 1.0, 0.0, 1.0));//vague prior to keep c parameter within acceptable range
            irm[j] = pl3;
        }

        //mirt R package default quadrature
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalDistributionApproximation latentDistribution = new NormalDistributionApproximation(min, max, (int)quadPoints);

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
        ItemResponseVector[] responseData = readSim5k();

        //Create array of item response models
        ItemResponseModel[] irm = new ItemResponseModel[30];
        Irm3PL pl3 = null;
        for(int j=0;j<30;j++) {
            //3PL with ICL defaults
            pl3 = new Irm3PL(1.0, 0.0, 0.0, 1.7);//3PL
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


}
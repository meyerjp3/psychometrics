/**
 * Copyright 2014 J. Patrick Meyer
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

import com.itemanalysis.psychometrics.quadrature.NormalQuadratureRule;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.math3.util.Precision;

import java.io.*;

public class MMLEsimulation {

    public MMLEsimulation(){

    }

    private void processReplication(int rep){
        String inputPath = "S:\\2014-3pl-simulation\\simdata\\c3";
        String outputPath = inputPath + "\\jmetrik-output";
        String dataFile = "";

        int nItems = 40;

        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();

        dataFile = "\\c3rep" + (rep+1) + ".txt";
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(inputPath + dataFile, false);

        //Create array of 2PL item response models
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        Irm3PL pl3 = null;
        for(int j=0;j<nItems;j++) {
            pl3 = new Irm3PL(1.0, 0.0, 0.05, 1.0);
            pl3.setDiscriminationPrior(new ItemParamPriorLogNormal(0.13, 0.6));
            pl3.setDifficultyPrior(new ItemParamPriorNormal(0.0, 2.0));

//            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));//ICL default
//            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));//default ICL prior
            pl3.setGuessingPrior(new ItemParamPriorBeta4(3.75, 4, 0.0, 0.5));//default ICL prior
            irm[j] = pl3;
        }

        //computation of quadrature points as done in the mirt R package
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(min, max, (int)quadPoints);

//        HermiteRuleFactory gaussHermite = new HermiteRuleFactory();
//        Pair<double[], double[]> dist = gaussHermite.getRule(41);
//        UserSuppliedQuadratureRule latentDistribution = new UserSuppliedQuadratureRule(dist.getKey(), dist.getValue());

        //compute start values
        StartingValues startValues = new StartingValues(responseData, irm);
        irm = startValues.computeStartingValues();

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
//        mmle.setVerbose(true);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.estimateParameters(0.0001, 250);
        System.out.println("Replication " + (rep+1) +  " complete");

//        System.out.println(mmle.printItemParameters());


        try{
            File f = new File(outputPath + dataFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            for(int j=0;j<nItems;j++){
                writer.write(Precision.round(irm[j].getDiscrimination(), 6) + "," +
                        Precision.round(irm[j].getDifficulty(), 6) + "," +
                        Precision.round(irm[j].getGuessing(), 6));
                writer.newLine();
            }
            writer.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }

    }

    public synchronized void runICL(int nrep){
        String inputPath = "S:/2014-3pl-simulation/simdata/c3";
        String[] commands = new String[nrep];

        //create all syntax files
        for(int r=0;r<nrep;r++){
            String fileName = "c3rep" + (r+1);
            //Template without priors
//            String template =
//                    "output -log_file " + inputPath + "/icl-sim-log.txt \n" +
//                            "set_default_model_dichtomous 3PL \n" +
//                            "options -default_prior_b none\n" +
//                            "options -default_prior_a none\n" +
//                            "options -default_prior_c {beta 5 17 0.0 1.0}\n" +
//                            "options -D 1.0\n" +
//                            "allocate_items_dist 40 -num_latent_dist_points 41 -latent_dist_range {-5.1225 5.1225} \n" +
//                            "read_examinees " + inputPath + "/" + fileName + "-icl.txt 40i1 \n" +
//                            "starting_values_dichotomous\n" +
//                            "EM_steps -max_iter 2000 -crit 0.0001\n" +
//                            "write_item_param " + inputPath + "/icl-output/" + fileName + "-icl-output.txt \n" +
//                            "release_items_dist\n";

            //Template with priors
            String template =
                    "output -log_file " + inputPath + "/icl-sim-log.txt \n" +
                            "set_default_model_dichtomous 3PL \n" +
                            "options -default_prior_a {lognormal 0.13 0.6}\n" +
                            "options -default_prior_b {normal 0 2}\n" +
                            "options -default_prior_c {beta 3.5 4.0 0.0 0.5}\n" +
                            "options -D 1.0\n" +
                            "allocate_items_dist 40 -num_latent_dist_points 41 -latent_dist_range {-5.1225 5.1225} \n" +
                            "read_examinees " + inputPath + "/" + fileName + "-icl.txt 40i1 \n" +
                            "starting_values_dichotomous\n" +
                            "EM_steps -max_iter 2000 -crit 0.0001\n" +
                            "write_item_param " + inputPath + "/icl-output/" + fileName + "-icl-output.txt \n" +
                            "release_items_dist\n";

            commands[r] = "icl " + inputPath + "/icl-syntax/" + fileName + "-icl-syntax.txt";

            File f = new File(inputPath + "/icl-syntax/" + fileName + "-icl-syntax.txt");

            try{
                f.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                writer.write(template);
                writer.close();

            }catch(IOException ex){
                ex.printStackTrace();
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }

        try{
            File fBat = new File(inputPath + "/icl-syntax/icl-batch.bat");
            fBat.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(fBat));
            for(int r=0;r<nrep;r++){
                writer.write(commands[r]);writer.newLine();
            }
            writer.close();

        }catch(IOException ex){
            ex.printStackTrace();
        }


    }

    public static void main(String[] args){
        System.out.println("Running simulation...");
        MMLEsimulation sim = new MMLEsimulation();

        int nrep = 1000;

//        sim.runICL(nrep);


        for(int r=0;r<nrep;r++){
            sim.processReplication(r);
        }

//        sim.processReplication(55);

    }

}

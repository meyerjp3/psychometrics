/**
 * Copyright 2015 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.irt.simulation;

import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.io.*;

/**
 * ItemResponseSimulator.java creates data that contain item responses. This class supports all unidimensional
 * item response models.
 */
public class ItemResponseSimulator {

    private double[] theta = null;

    private ItemResponseModel[] irm = null;

    private int nPeople = 0;

    private int nItems = 0;

    private RandomDataGenerator random = null;

    /**
     * Allows users to provide their own array of examinee ability values and item response models.
     *
     * @param theta an array of examinee ability parameters.
     * @param irm an array of item response models.
     */
    public ItemResponseSimulator(double[] theta, ItemResponseModel[] irm){
        this.theta = theta;
        this.irm = irm;
        this.nPeople = this.theta.length;
        this.nItems = this.irm.length;
        this.random = new RandomDataGenerator();
    }

    /**
     * Allows users to specify the number of examinee ability parameters that will be drawn from
     * a standrad normal quadrature. It also allows the user to provide the item response models.
     *
     * @param nPeople number of examinee ability parameter to create.
     * @param irm an array of item response models.
     */
    public ItemResponseSimulator(int nPeople, ItemResponseModel[] irm){
        this.nPeople = nPeople;
        this.irm = irm;
        this.nPeople = this.theta.length;
        this.nItems = this.irm.length;
        this.random = new RandomDataGenerator();
        drawSimulees();
    }

    /**
     * Creates an array of examinee ability parameters using random draws from a standard normal quadrature.
     */
    private void drawSimulees(){
        theta = new double[nPeople];
        for(int i=0;i<nPeople;i++){
            theta[i] = random.nextGaussian(0, 1);
        }
    }

    /**
     * Creates a two-dimensional array of item responses.
     *
     * @return a two-dimensional array of item responses.
     */
    public byte[][] generateData(){
        byte[][] x = new byte[nPeople][nItems];

        for(int i=0;i<nPeople;i++){
            for(int j=0;j<nItems;j++){
                x[i][j] = drawItemResponse(theta[i], irm[j]);
            }
        }
        return x;
    }

    /**
     * Generates a comma separated file (CSV file) of item responses.
     *
     * @param outputFile complete path and file name of output file
     * @param includeID include an examinee ID number in the first column if true. Omits the ID if false.
     * @param includeHeader if true will include variable names in first row of CSV file.
     * @throws IOException
     */
    public void generateData(String outputFile, boolean includeID, boolean includeHeader)throws IOException{
        byte[][] x = generateData();
        int baseID = nPeople*10+1;

        Writer writer = null;
        CSVPrinter printer = null;
        File file = new File(outputFile);

        try{
            writer = new OutputStreamWriter(new FileOutputStream(file));
            printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withCommentMarker('#'));

            if(includeHeader){
                if(includeID) printer.print("ID");
                for(int j=0;j<nItems;j++){
                    printer.print("V"+(j+1));
                }
                printer.println();
            }

            for(int i=0;i<nPeople;i++){
                if(includeID) printer.print(baseID);
                for(int j=0;j<nItems;j++){
                    printer.print(x[i][j]);
                }
                printer.println();
                baseID++;
            }
        }catch(IOException ex){
            throw(ex);
        }finally{
            if(writer!=null) writer.close();
            if(printer!=null) printer.close();
        }

    }

    /**
     * Creates multiple CSV files of item response data. Each file will have a unique names.
     * For example, if path = "C:/mydata" and baseName = "itemdata" and replications  = 2;
     * the file names will be "C:/mydata/itemdata-rep1.csv" and "C:/mydata/itemdata-rep2.csv"
     *
     * @param path location where files will be stored
     * @param baseName a base name for the CSV files. The will be appended to the path and a unique
     *                 string will be added to the end.
     * @param includeID include an examinee ID number in the first column if true. Omits the ID if false.
     * @param includeHeader if true will include variable names in first row of CSV file.
     * @param replications number of replications (and number of data files) to create
     * @throws IOException
     */
    public void generateData(String path, String baseName, boolean includeID, boolean includeHeader, int replications)throws IOException{
        String cleanPath = path.replaceAll("\\\\", "/");
        String outputFile = "";

        for(int r=0;r<replications;r++){
            outputFile = cleanPath + "/" + baseName + "-rep" + (r+1) + ".csv";
            generateData(outputFile, includeID, includeHeader);
        }
    }

    /**
     * Generates an item response using cumulative probabilities computed from
     * the model. This method works for binary and polytomous models.
     *
     * @param theta an examinee ability parameter
     * @param model an item response model
     * @return an item response
     */
    private byte drawItemResponse(double theta, ItemResponseModel model){
        byte itemScore = 0;
        double cumProb = 0;
        double randomDraw = random.nextUniform(0, 1);

        for(int k=0;k<model.getNcat();k++){
            itemScore = (byte)k;
            cumProb += model.probability(theta, k);
            if(randomDraw <= cumProb){
                return itemScore;
            }
        }
        return itemScore;
    }


}

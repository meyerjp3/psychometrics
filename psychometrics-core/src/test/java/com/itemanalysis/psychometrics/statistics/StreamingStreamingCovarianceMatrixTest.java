/*
 * Copyright 2012 J. Patrick Meyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.statistics;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class StreamingStreamingCovarianceMatrixTest {

    public StreamingStreamingCovarianceMatrixTest() {
    }

    /**
     * 
     */
    @Test
    public void testCovariance() {
        System.out.println("Testing covariance matrix");
        double[][] x = getData();
        double[][] trueCov = getCovariance();
        StreamingCovarianceMatrix S = new StreamingCovarianceMatrix(50);
        for(int i=0;i<1000;i++){
            for(int j=0;j<50;j++){
                for(int k=0;k<50;k++){
                    S.increment(j, k, x[i][j], x[i][k]);
                }
            }
        }
        
        double[][] obsCov = S.value();

        for(int i=0;i<50;i++){
            for(int j=0;j<50;j++){
                assertEquals("Testing covariance", trueCov[i][j], obsCov[i][j], 1e-15);
            }
        }
    }

    @Test
    public void testCorrelation() {
        System.out.println("Testing correlation matrix");
        double[][] x = getData();
        double[][] trueCor = getCorrelation();
        StreamingCovarianceMatrix S = new StreamingCovarianceMatrix(50);
        for(int i=0;i<1000;i++){
            for(int j=0;j<50;j++){
                for(int k=0;k<50;k++){
                    S.increment(j, k, x[i][j], x[i][k]);
                }
            }
        }

        double[][] obsCor = S.correlation();

        for(int i=0;i<50;i++){
            for(int j=0;j<50;j++){
                assertEquals("Testing covariance", trueCor[i][j], obsCor[i][j], 1e-14);
            }
        }
    }


    public double[][] getData(){
        double[][] x = new double[1000][50];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/scaling.txt"));

            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<50;j++){
                    x[row][j] = Double.parseDouble(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

    public double[][] getCovariance(){
        double[][] x = new double[50][50];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/covariance-scaling-data.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<50;j++){
                    x[row][j] = Double.parseDouble(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

    public double[][] getCorrelation(){
        double[][] x = new double[50][50];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/correlation-scaling-data.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<50;j++){
                    x[row][j] = Double.parseDouble(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

    

}
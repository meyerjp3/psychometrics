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
package com.itemanalysis.psychometrics.scaling;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RawScoreTest {

    public RawScoreTest() {
    }

    /**
     * Test of increment method, of class RawScore.
     */
    @Test
    public void testRawScore() {
        double[][] x = this.getData();
        double[] trueRaw = this.getRawScore();
        RawScore[] obsRaw = new RawScore[1000];

        for(int i=0;i<1000;i++){
            obsRaw[i] = new RawScore(50);
            for(int j=0;j<50;j++){
                obsRaw[i].increment(x[i][j]);
            }
        }

        System.out.println("Testing raw score");
        for(int i=0;i<1000;i++){
            assertEquals("Testing Raw Score", trueRaw[i], obsRaw[i].value(), 1e-15);
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

    public double[] getRawScore(){
        double[] x = new double[1000];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/scaling.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[50]);//sum score is in column 51
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

}
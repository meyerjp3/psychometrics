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

import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class NormalizedScoreTest {

    public NormalizedScoreTest() {
    }

//    @Test
    public void testNormScore(){
        double[][] trueNormScore = this.getNormScore();
        double[][] x = this.getData();
        double[] sum = new double[1000];
        PercentileRank prank = new PercentileRank();

        for(int i=0;i<x.length;i++){
            sum[i] = 0.0;
            for(int j=0;j<50;j++){
                sum[i]+=x[i][j];
            }
            prank.addValue(sum[i]);
        }
        prank.createLookupTable();
        System.out.println("Normalized Score Test");

        NormalizedScore nscore = new NormalizedScore();
        DefaultLinearTransformation l1 = new DefaultLinearTransformation();
        NormalizedScore nscore2 = new NormalizedScore();
        DefaultLinearTransformation l2 = new DefaultLinearTransformation(100, 15);
        nscore.createLookupTable(prank, l1);
        nscore2.createLookupTable(prank, l2);

        for(int i=0;i<trueNormScore.length;i++){
            assertEquals("Normalized score test N(0,1)", trueNormScore[i][0], nscore.getNormalizedScoreAt(sum[i]), 1e-5);
            assertEquals("Normalized score test N(100,15)", trueNormScore[i][1], nscore2.getNormalizedScoreAt(sum[i]), 1e-5);
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

    public double[][] getNormScore(){
        double[][] x = new double[1000][2];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/scaling.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row][0] = Double.parseDouble(s[53]);//percentile rank is in column 54
                x[row][1] = Double.parseDouble(s[54]);//percentile rank on IQ scale is in column 55
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

}
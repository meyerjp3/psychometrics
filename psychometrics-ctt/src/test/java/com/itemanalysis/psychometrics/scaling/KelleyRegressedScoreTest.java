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

import com.itemanalysis.psychometrics.statistics.StreamingCovarianceMatrix;
import com.itemanalysis.psychometrics.reliability.CoefficientAlpha;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class KelleyRegressedScoreTest {

    public KelleyRegressedScoreTest() {
    }

    /**
     * Test of rho method, of class KelleyRegressedScore.
     */
    //@Test
    public void testValue() {
        System.out.println("Kelley score test");
        double[][] x = getData();
        double[] sum = new double[1000];
        Mean mean = new Mean();
        StreamingCovarianceMatrix S = new StreamingCovarianceMatrix(50);
        for(int i=0;i<x.length;i++){
            sum[i] = 0.0;
            for(int j=0;j<50;j++){
                for(int k=0;k<50;k++){
                    S.increment(j, k, x[i][j], x[i][k]);
                }
                sum[i]+=x[i][j];
            }
        }

        CoefficientAlpha alpha = new CoefficientAlpha(S.value());
        KelleyRegressedScore kscore = new KelleyRegressedScore(mean.evaluate(sum), alpha);

        double[] kscores = this.getKelleyScores();
        double kelley = 0.0;
        for(int i=0;i<kscores.length;i++){
            kelley = kscore.value(sum[i]);
            assertEquals(kscores[i], kelley, 1e-5);
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

    public double[] getKelleyScores(){
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
                x[row] = Double.parseDouble(s[52]);//kelley scores in column 53
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }


}
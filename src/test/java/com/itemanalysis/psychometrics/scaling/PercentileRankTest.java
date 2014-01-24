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

public class PercentileRankTest {

    public PercentileRankTest() {
    }

    /**
     * True values computed using Brian Habing's R function
     * Px<-function(x,pop){
     * xstar<-floor(x+.5)
     * max(0,min(100, 100*(Fx(xstar-1,pop)+(x-(xstar-.5))*(Fx(xstar,pop)-Fx(xstar-1,pop)))))}
     */
//    @Test
    public void testPercentileRank() {
        double[] truePercentileRank = this.getPercentileRank();
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
        System.out.println("Incremental percentile rank test");
        //only using a precision of 1e-2 because that's all that's given by R output

        for(int i=0;i<truePercentileRank.length;i++){
            assertEquals("Incremental test", truePercentileRank[i], prank.getPercentileRankAt((int)sum[i]), 1e-2);
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

    public double[] getPercentileRank(){
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
                x[row] = Double.parseDouble(s[51]);//percentile rank is in column 52
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

    

}
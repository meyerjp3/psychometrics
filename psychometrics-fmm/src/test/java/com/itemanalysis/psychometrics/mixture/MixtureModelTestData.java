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
package com.itemanalysis.psychometrics.mixture;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MixtureModelTestData {

    public MixtureModelTestData(){

    }

    /**
     * The file univariate-mixture-twogroups.txt contains two columns of data.
     * The first column contains double values and the second column contains
     * the group indicators. Data were generated with the R functions:
     *
     * x1<-round(rnorm(n=400, mean=100, sd=15),2)
     * x2<-round(rnorm(n=600, mean=75, sd=15),2)
     * x<-rbind(x1,x2)
     * x<-cbind(x,c(rep(1,400), rep(2,600)))
     *
     * @return
     */
    public double[][] getMixtureData1(){
        String dataName = "./src/test/resources/testdata/univariate-mixture-twogroups.txt";
        double[][] x = new double[1000][2];
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(new File(dataName)));
            String line = "";
            String[] l = null;
            int index = 0;
            while((line=reader.readLine())!=null){
                l = line.split(",");
                x[index][0] = Double.parseDouble(l[0]);
                x[index][1] = Double.parseDouble(l[1]);
                index++;
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

    /**
     * The file multivariate-mixture-twogroups.txt contains three columns of data.
     * The first two columns are data and the last column is a group indicator.
     * Data generate in R using:
     *
     * #group1
     * n1<-800
     * m1<-c(-1, -1)
     * S1<-matrix(c(1, 0.0, 0.0, 1), nrow=2, byrow=TRUE)
     *
     * #group2
     * n2<-1200
     * m2<-c(1.5, 1.5)
     * S2<-matrix(c(1, 0.0, 0.0, 1), nrow=2, byrow=TRUE)
     *
     * x1<-round(rmvnorm(n=n1, mean=m1, sigma=S1),2)
     * x2<-round(rmvnorm(n=n2, mean=m2, sigma=S2),2)
     * group<-c(rep(1,n1), rep(2,n2))
     *
     * #combined data
     * x<-rbind(x1,x2)
     * x<-cbind(x,group)
     *
     *
     * @return
     */
    public double[][] getMixtureData2(){
        String dataName = "./src/test/resources/testdata/multivariate-mixture-twogroups.txt";
        double[][] x = new double[2000][3];
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(new File(dataName)));
            String line = "";
            String[] l = null;
            int index = 0;
            while((line=reader.readLine())!=null){
                l = line.split(",");
                x[index][0] = Double.parseDouble(l[0]);
                x[index][1] = Double.parseDouble(l[1]);
                x[index][2] = Double.parseDouble(l[2]);
                index++;
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

    /**
     * Iris data from R. Last column is the actual group membership.
     *
     * @return
     */
    public double[][] getMixtureData3(){
        String dataName = "./src/test/resources/testdata/iris.txt";
        double[][] x = new double[150][5];
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(new File(dataName)));
            String line = "";
            String[] l = null;
            int index = 0;
            while((line=reader.readLine())!=null){
                l = line.split(",");
                x[index][0] = Double.parseDouble(l[0]);
                x[index][1] = Double.parseDouble(l[1]);
                x[index][2] = Double.parseDouble(l[2]);
                x[index][3] = Double.parseDouble(l[3]);
                x[index][4] = Double.parseDouble(l[4]);
                index++;
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }


}

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
package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class GuttmanLambdaTest {

    public GuttmanLambdaTest() {
    }

    /**
     * True values computed by Brian Habing's R function:
     * alpha<-function(testdata){
     * n<-ncol(testdata)
     * nexmn<-nrow(testdata)
     * x<-apply(testdata,1,sum)
     * s2y<-diag(var(testdata))*(nexmn-1)/nexmn
     * s2x<-var(x)*(nexmn-1)/nexmn
     * alpha<-(n/(n-1))*(1-sum(s2y)/s2x)
     * s2yy<-(((var(testdata)-diag(diag(var(testdata))))*(nexmn-1)/nexmn))^2
     * lambda2<-1-sum(s2y)/s2x+sqrt((n/(n-1))*sum(s2yy))/s2x
     * list(alpha=alpha,lambda2=lambda2)}
     */
    @Test
    public void testGuttmanLambda() {
        double[][] x = getData();
        CovarianceMatrix S = new CovarianceMatrix(50);
        double trueAlpha = 0.9033415; //from Brian Habing's R function;
        for(int i=0;i<1000;i++){
            for(int j=0;j<50;j++){
                for(int k=0;k<50;k++){
                    S.increment(j, k, x[i][j], x[i][k]);
                }
            }
        }
        GuttmanLambda lambda = new GuttmanLambda(S);
        System.out.println("Guttman's lambda2: " + lambda.value(false));
        assertEquals("Testing lambda", trueAlpha, lambda.value(false), 1e-6);
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

   

}
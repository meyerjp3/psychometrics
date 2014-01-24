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

import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;

import java.util.ArrayList;
import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class ReliabilitySummary {

    private CovarianceMatrix matrix = null;
    private CronbachAlpha alpha = null;
    private GuttmanLambda lambda = null;
    private FeldtGilmer feldtGilmer = null;
    private FeldtBrennan feldtBrennan = null;
    private RajuBeta raju = null;
    private double numberOfExaminees = 0.0;
    private double[] alphaDeleted = null;
    private double[] lambdaDeleted = null;
    private double[] fgDeleted = null;
    private double[] fbDeleted = null;
    private double[] rajuDeleted = null;
    private ArrayList<VariableInfo> var = null;
    private boolean itemDeleted = false;
    private boolean unbiased = false;


    public ReliabilitySummary(CovarianceMatrix matrix, double numberOfExaminees, ArrayList<VariableInfo> var,
            boolean unbiased, boolean itemDeleted){
        this.numberOfExaminees = numberOfExaminees;
        this.matrix = matrix;
        this.unbiased = unbiased;
        this.itemDeleted = itemDeleted;
        alpha = new CronbachAlpha(matrix);
        lambda = new GuttmanLambda(matrix);
        feldtGilmer = new FeldtGilmer(matrix);
        feldtBrennan = new FeldtBrennan(matrix);
        raju = new RajuBeta(matrix);
        this.confInt();
        if(itemDeleted) this.itemDeleted();
        this.var = var;
    }

    public ScoreReliability value(){
        return lambda;
    }

    private void confInt(){
        alpha.confidenceInterval(numberOfExaminees, unbiased);
        lambda.confidenceInterval(numberOfExaminees, unbiased);
        feldtGilmer.confidenceInterval(numberOfExaminees, unbiased);
        feldtBrennan.confidenceInterval(numberOfExaminees, unbiased);
        raju.confidenceInterval(numberOfExaminees, unbiased);
    }

    private void itemDeleted(){
        CovarianceMatrix subMat;
        int n=matrix.getNumberOfVariables();
        CronbachAlpha alpha2 = null;
        GuttmanLambda lambda2 = null;
        FeldtGilmer feldtGilmer2 = null;
        FeldtBrennan feldtBrennan2 = null;
        RajuBeta raju2 = null;

		for(int item=0;item<n;item++){
			subMat = matrix.matrixSansVariable(item, true);
            alpha2 = new CronbachAlpha(subMat);
            lambda2 = new GuttmanLambda(subMat);
            feldtGilmer2 = new FeldtGilmer(subMat);
            feldtBrennan2 = new FeldtBrennan(subMat);
            raju2 = new RajuBeta(subMat);
			alpha.incrementValueIfItemDeleted(item, alpha2.value(unbiased));
            lambda.incrementValueIfItemDeleted(item, lambda2.value(unbiased));
            feldtGilmer.incrementValueIfItemDeleted(item, feldtGilmer2.value(unbiased));
            feldtBrennan.incrementValueIfItemDeleted(item, feldtBrennan2.value(unbiased));
            raju.incrementValueIfItemDeleted(item, raju2.value(unbiased));
		}

        alphaDeleted = alpha.valueIfItemDeleted();
        lambdaDeleted = lambda.valueIfItemDeleted();
        fgDeleted = feldtGilmer.valueIfItemDeleted();
        fbDeleted = feldtBrennan.valueIfItemDeleted();
        rajuDeleted = raju.valueIfItemDeleted();
    }

    

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%n");
        f.format("%-20s", "                            RELIABILIY ANALYSIS");f.format("%n");
        f.format("%-60s", "===========================================================================");f.format("%n");
        f.format("%-20s",  " Method             ");f.format("%10s",  "Estimate");f.format("%5s",  ""); f.format("%20s",  "95% Conf. Int. ");f.format("%5s",  ""); f.format("%10s",  "SEM"); f.format("%n");
        f.format("%-60s", "---------------------------------------------------------------------------");f.format("%n");
        f.format("%-20s",  " Guttman's L2 ");f.format("%10.4f", lambda.value(unbiased));f.format("%5s",  "");
            f.format("%20s", lambda.confidenceIntervalToString());f.format("%5s",  ""); f.format("% 10.4f", lambda.sem(unbiased));f.format("%n");
        f.format("%-20s",  " Coefficient Alpha  ");f.format("%10.4f", alpha.value(unbiased));f.format("%5s",  "");
            f.format("%20s", alpha.confidenceIntervalToString());f.format("%5s",  ""); f.format("% 10.4f", alpha.sem(unbiased));f.format("%n");
        f.format("%-20s",  " Feldt-Gilmer       ");f.format("%10.4f", feldtGilmer.value(unbiased));f.format("%5s",  "");
            f.format("%20s", feldtGilmer.confidenceIntervalToString());f.format("%5s",  ""); f.format("% 10.4f", feldtGilmer.sem(unbiased));f.format("%n");
        f.format("%-20s",  " Feldt-Brennan      ");f.format("%10.4f", feldtBrennan.value(unbiased));f.format("%5s",  "");
            f.format("%20s", feldtBrennan.confidenceIntervalToString());f.format("%5s",  ""); f.format("% 10.4f", feldtBrennan.sem(unbiased));f.format("%n");
        f.format("%-20s",  " Raju's Beta        ");f.format("%10.4f", raju.value(unbiased));f.format("%5s",  "");
            f.format("%20s", raju.confidenceIntervalToString());f.format("%5s",  ""); f.format("% 10.4f", raju.sem(unbiased));f.format("%n");
        f.format("%-60s", "---------------------------------------------------------------------------");f.format("%n");
        return f.toString();
    }

    public String itemDeletedString(){
        if(!itemDeleted) return "";
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%n");
        f.format("%-20s", "                    RELIABILIY IF ITEM DELTED");f.format("%n");
        f.format("%-60s", "=================================================================");f.format("%n");
        f.format("%-10s",  " Item");f.format("%10s",  "L2");f.format("%10s",  "Alpha");f.format("%10s",  "F-G"); f.format("%10s",  "F-B"); f.format("%10s",  "Raju");f.format("%n");
        f.format("%-60s", "-----------------------------------------------------------------");f.format("%n");
        for(int i=0;i<lambdaDeleted.length;i++){
            f.format("%-10s",  var.get(i).getName());
            f.format("%4s",  ""); f.format("%6.4f", lambdaDeleted[i]);
            f.format("%4s",  ""); f.format("%6.4f", alphaDeleted[i]);
            f.format("%4s",  ""); f.format("%6.4f", fgDeleted[i]);
            f.format("%4s",  ""); f.format("%6.4f", fbDeleted[i]);
            f.format("%4s",  ""); f.format("%6.4f", rajuDeleted[i]);
            f.format("%n");
        }
        f.format("%-60s", "-----------------------------------------------------------------");f.format("%n");
        return f.toString();
    }





}

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

import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedHashMap;

/**
 * A wrapper for multiple ScoreReliability objects. It allows the Score Reliability objects
 * to be incremented simultaneously and it also formats output for all of the objects.
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class ReliabilitySummary {

    private CoefficientAlpha alpha = null;
    private GuttmanLambda lambda = null;
    private FeldtGilmer feldtGilmer = null;
    private FeldtBrennan feldtBrennan = null;
    private RajuBeta raju = null;
    private int nItems = 0;
    private ArrayList<VariableAttributes> var = null;
    private boolean itemDeleted = false;

    public ReliabilitySummary(CovarianceMatrix matrix, ArrayList<VariableAttributes> var, boolean unbiased, boolean itemDeleted){
        this.nItems = matrix.getNumberOfVariables();
        this.itemDeleted = itemDeleted;
        alpha = new CoefficientAlpha(matrix, unbiased);
        lambda = new GuttmanLambda(matrix, unbiased);
        feldtGilmer = new FeldtGilmer(matrix, unbiased);
        feldtBrennan = new FeldtBrennan(matrix, unbiased);
        raju = new RajuBeta(matrix, unbiased);
        this.var = var;
    }

    public ReliabilitySummary(CovarianceMatrix matrix, LinkedHashMap<VariableName, VariableAttributes> variableAttributeMap,
            boolean unbiased, boolean itemDeleted){
        this.nItems = matrix.getNumberOfVariables();
        this.itemDeleted = itemDeleted;
        alpha = new CoefficientAlpha(matrix, unbiased);
        lambda = new GuttmanLambda(matrix, unbiased);
        feldtGilmer = new FeldtGilmer(matrix, unbiased);
        feldtBrennan = new FeldtBrennan(matrix, unbiased);
        raju = new RajuBeta(matrix, unbiased);

        this.var = new ArrayList<VariableAttributes>();
        for(VariableName v : variableAttributeMap.keySet()){
            this.var.add(variableAttributeMap.get(v));
        }

    }

    public ScoreReliability value(){
        return lambda;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double totalVariance = lambda.totalVariance();

        double gl = lambda.value();
        double ca = alpha.value();
        double fg = feldtGilmer.value();
        double fb = feldtBrennan.value();
        double rj = raju.value();

        double[] glCI = lambda.confidenceInterval();
        double[] caCI = alpha.confidenceInterval();
        double[] fgCI = feldtGilmer.confidenceInterval();
        double[] fbCI = feldtBrennan.confidenceInterval();
        double[] rjCI = raju.confidenceInterval();

        StandardErrorOfMeasurement sem = new StandardErrorOfMeasurement();

        f.format("%n");
        f.format("%-20s", "                            RELIABILIY ANALYSIS");f.format("%n");
        f.format("%-60s", "===========================================================================");f.format("%n");
        f.format("%-20s",  " Method             ");f.format("%10s",  "Estimate");f.format("%5s",  ""); f.format("%20s",  "95% Conf. Int. ");f.format("%5s",  ""); f.format("%10s",  "SEM"); f.format("%n");
        f.format("%-60s", "---------------------------------------------------------------------------");f.format("%n");
        f.format("%-20s",  " Guttman's L2 ");f.format("%10.4f", gl);f.format("%5s",  "");
            f.format("%20s", lambda.confidenceIntervalToString(glCI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, gl));f.format("%n");
        f.format("%-20s",  " Coefficient Alpha  ");f.format("%10.4f", ca);f.format("%5s",  "");
            f.format("%20s", alpha.confidenceIntervalToString(caCI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, ca));f.format("%n");
        f.format("%-20s",  " Feldt-Gilmer       ");f.format("%10.4f", fg);f.format("%5s",  "");
            f.format("%20s", feldtGilmer.confidenceIntervalToString(fgCI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, fg));f.format("%n");
        f.format("%-20s",  " Feldt-Brennan      ");f.format("%10.4f", fb);f.format("%5s",  "");
            f.format("%20s", feldtBrennan.confidenceIntervalToString(fbCI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, fb));f.format("%n");
        f.format("%-20s",  " Raju's Beta        ");f.format("%10.4f", rj);f.format("%5s",  "");
            f.format("%20s", raju.confidenceIntervalToString(rjCI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, rj));f.format("%n");
        f.format("%-60s", "===========================================================================");f.format("%n");
        return f.toString();
    }

    public String itemDeletedString(){
        if(!itemDeleted) return "";
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double[] glDeleted = lambda.itemDeletedReliability();
        double[] caDeleted = alpha.itemDeletedReliability();
        double[] fgDeleted = feldtGilmer.itemDeletedReliability();
        double[] fbDeleted = feldtBrennan.itemDeletedReliability();
        double[] rjDeleted = raju.itemDeletedReliability();

        f.format("%n");
        f.format("%-20s", "                    RELIABILIY IF ITEM DELTED");f.format("%n");
        f.format("%-60s", "=================================================================");f.format("%n");
        f.format("%-10s",  " Item");f.format("%10s",  "L2");f.format("%10s",  "Alpha");f.format("%10s",  "F-G"); f.format("%10s",  "F-B"); f.format("%10s",  "Raju");f.format("%n");
        f.format("%-60s", "-----------------------------------------------------------------");f.format("%n");
        for(int i=0;i<nItems;i++){
            f.format("%-10s",  var.get(i).getName());
            f.format("%4s",  ""); f.format("%6.4f", glDeleted[i]);
            f.format("%4s",  ""); f.format("%6.4f", caDeleted[i]);
            f.format("%4s",  ""); f.format("%6.4f", fgDeleted[i]);
            f.format("%4s",  ""); f.format("%6.4f", fbDeleted[i]);
            f.format("%4s",  ""); f.format("%6.4f", rjDeleted[i]);
            f.format("%n");
        }
        f.format("%-60s", "=================================================================");f.format("%n");
        f.format("%-30s", "L2: Guttman's lambda-2");f.format("%n");
        f.format("%-30s", "Alpha: Coefficient alpha");f.format("%n");
        f.format("%-30s", "F-G: Feldt-Gilmer coefficient");f.format("%n");
        f.format("%-30s", "F-B: Feldt-Brennan coefficient");f.format("%n");
        f.format("%-30s", "Raju: Raju's beta coefficient");f.format("%n");
        return f.toString();
    }





}

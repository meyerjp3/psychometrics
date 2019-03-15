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
import com.itemanalysis.psychometrics.statistics.StreamingCovarianceMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;

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
    private GuttmanLambda1 guttmanLambda1 = null;
    private GuttmanLambda2 guttmanLambda2 = null;
    private GuttmanLambda3 guttmanLambda3 = null;
    private GuttmanLambda4 guttmanLambda4 = null;
    private GuttmanLambda5 guttmanLambda5 = null;
    private GuttmanLambda6 guttmanLambda6 = null;
    private FeldtGilmer feldtGilmer = null;
    private FeldtBrennan feldtBrennan = null;
    private RajuBeta raju = null;
    private double nPeople = 0;
    private int nItems = 0;
    private ArrayList<VariableAttributes> var = null;
    private boolean itemDeleted = false;

    public ReliabilitySummary(StreamingCovarianceMatrix matrix, ArrayList<VariableAttributes> var, boolean itemDeleted){
        this.nItems = matrix.getNumberOfVariables();
        this.itemDeleted = itemDeleted;

        double[][] cov = matrix.value();
        this.nPeople = matrix.getMinSampleSize();

        alpha = new CoefficientAlpha(cov);
        guttmanLambda1 = new GuttmanLambda1(cov);
        guttmanLambda2 = new GuttmanLambda2(cov);
        guttmanLambda3 = new GuttmanLambda3(cov);
        guttmanLambda4 = new GuttmanLambda4(cov);
        guttmanLambda5 = new GuttmanLambda5(cov);
        guttmanLambda6 = new GuttmanLambda6(cov);
        feldtGilmer = new FeldtGilmer(cov);
        feldtBrennan = new FeldtBrennan(cov);
        raju = new RajuBeta(cov);
        this.var = var;
    }

    public ReliabilitySummary(StreamingCovarianceMatrix matrix, LinkedHashMap<VariableName, VariableAttributes> variableAttributeMap, boolean itemDeleted){
        this.nItems = matrix.getNumberOfVariables();
        this.itemDeleted = itemDeleted;

        double[][] cov = matrix.value();
        this.nPeople = matrix.getMaxSampleSize();

        alpha = new CoefficientAlpha(cov);
        guttmanLambda1 = new GuttmanLambda1(cov);
        guttmanLambda2 = new GuttmanLambda2(cov);
        guttmanLambda3 = new GuttmanLambda3(cov);
        guttmanLambda4 = new GuttmanLambda4(cov);
        guttmanLambda5 = new GuttmanLambda5(cov);
        guttmanLambda6 = new GuttmanLambda6(cov);
        feldtGilmer = new FeldtGilmer(cov);
        feldtBrennan = new FeldtBrennan(cov);
        raju = new RajuBeta(cov);

        this.var = new ArrayList<VariableAttributes>();
        for(VariableName v : variableAttributeMap.keySet()){
            this.var.add(variableAttributeMap.get(v));
        }

    }

    public ScoreReliability value(){
        return guttmanLambda2;
    }

    public double getSampleSize(){
        return nPeople;
    }

    public ScoreReliability getReliability(ScoreReliabilityType type){
        switch(type){
            case GUTTMAN_LAMBDA1: return guttmanLambda1;
            case GUTTMAN_LAMBDA2: return guttmanLambda2;
            case GUTTMAN_LAMBDA3: return guttmanLambda3;
            case GUTTMAN_LAMBDA4: return guttmanLambda4;
            case GUTTMAN_LAMBDA5: return guttmanLambda5;
            case GUTTMAN_LAMBDA6: return guttmanLambda6;
            case FELDT_GILMER:    return feldtGilmer;
            case FELDT_CLASSICAL_CONGENERIC: return feldtBrennan;
            case RAJU_BETA: return raju;
        }
        return alpha;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double totalVariance = guttmanLambda2.totalVariance();

        double g1 = guttmanLambda1.value();
        double g2 = guttmanLambda2.value();
        double g3 = guttmanLambda3.value();
        double g4 = guttmanLambda4.value();
        double g5 = guttmanLambda5.value();
        double g6 = Double.NaN;

        try{
            g6 = guttmanLambda6.value();
        }catch(SingularMatrixException ex){
            ex.printStackTrace();
        }
        double ca = alpha.value();
        double fg = feldtGilmer.value();
        double fb = feldtBrennan.value();
        double rj = raju.value();

        double[] g1CI = guttmanLambda1.confidenceInterval(nPeople);
        double[] g2CI = guttmanLambda2.confidenceInterval(nPeople);
        double[] g3CI = guttmanLambda3.confidenceInterval(nPeople);
        double[] g4CI = guttmanLambda4.confidenceInterval(nPeople);
        double[] g5CI = guttmanLambda5.confidenceInterval(nPeople);
        double[] g6CI = guttmanLambda6.confidenceInterval(nPeople);
        double[] caCI = alpha.confidenceInterval(nPeople);
        double[] fgCI = feldtGilmer.confidenceInterval(nPeople);
        double[] fbCI = feldtBrennan.confidenceInterval(nPeople);
        double[] rjCI = raju.confidenceInterval(nPeople);

        StandardErrorOfMeasurement sem = new StandardErrorOfMeasurement();

        f.format("%n");
        f.format("%-20s", "                            RELIABILITY ANALYSIS");f.format("%n");
        f.format("%-60s", "===========================================================================");f.format("%n");
        f.format("%-20s",  " Method             ");f.format("%10s",  "Estimate");f.format("%5s",  ""); f.format("%20s",  "95% Conf. Int. ");f.format("%5s",  ""); f.format("%10s",  "SEM"); f.format("%n");
        f.format("%-60s", "---------------------------------------------------------------------------");f.format("%n");
        f.format("%-20s",  " Guttman's L1 ");f.format("%10.4f", g1);f.format("%5s",  "");
        f.format("%20s", guttmanLambda1.confidenceIntervalToString(g1CI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, g1));f.format("%n");
        f.format("%-20s",  " Guttman's L2 ");f.format("%10.4f", g2);f.format("%5s",  "");
        f.format("%20s", guttmanLambda2.confidenceIntervalToString(g2CI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, g2));f.format("%n");
        f.format("%-20s",  " Guttman's L3 ");f.format("%10.4f", g3);f.format("%5s",  "");
        f.format("%20s", guttmanLambda3.confidenceIntervalToString(g3CI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, g3));f.format("%n");
        f.format("%-20s",  " Guttman's L4 ");f.format("%10.4f", g4);f.format("%5s",  "");
        f.format("%20s", guttmanLambda4.confidenceIntervalToString(g4CI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, g4));f.format("%n");
        f.format("%-20s",  " Guttman's L5 ");f.format("%10.4f", g5);f.format("%5s",  "");
        f.format("%20s", guttmanLambda5.confidenceIntervalToString(g5CI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, g5));f.format("%n");

        if(Double.isNaN(g6)){
            f.format("%-20s",  " Guttman's L6 "); f.format("%-20s", "Singular maxtrix"); f.format("%n");
        }else{
            f.format("%-20s",  " Guttman's L6 ");f.format("%10.4f", g6);f.format("%5s",  "");
            f.format("%20s", guttmanLambda6.confidenceIntervalToString(g6CI));f.format("%5s",  ""); f.format("% 10.4f", sem.value(totalVariance, g6));f.format("%n");
        }


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

        double[] glDeleted = guttmanLambda2.itemDeletedReliability();
        double[] caDeleted = alpha.itemDeletedReliability();
        double[] fgDeleted = feldtGilmer.itemDeletedReliability();
        double[] fbDeleted = feldtBrennan.itemDeletedReliability();
        double[] rjDeleted = raju.itemDeletedReliability();

        f.format("%n");
        f.format("%-20s", "                    RELIABILITY IF ITEM DELETED");f.format("%n");
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
        f.format("%-30s", "L2: Guttman's guttmanLambda2-2");f.format("%n");
        f.format("%-30s", "Alpha: Coefficient alpha");f.format("%n");
        f.format("%-30s", "F-G: Feldt-Gilmer coefficient");f.format("%n");
        f.format("%-30s", "F-B: Feldt-Brennan coefficient");f.format("%n");
        f.format("%-30s", "Raju: Raju's beta coefficient");f.format("%n");
        return f.toString();
    }





}

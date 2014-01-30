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
package com.itemanalysis.psychometrics.cfa;

import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class CfaSummary{
    
    private Array2DRowRealMatrix cfaMatrix = null;
    
    private double numberOfExaminees = 0.0;

    private ArrayList<VariableInfo> items = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public CfaSummary(ArrayList<VariableInfo> items, CovarianceMatrix matrix, double numberOfExaminees){
        this.items = items;
        cfaMatrix = new Array2DRowRealMatrix(matrix.value(true));
//        cfaMatrix = new Array2DRowRealMatrix(matrix.correlation(true));
        this.numberOfExaminees = numberOfExaminees;
    }

    public String multipleCfa(int estimationMethod, int optimizationMethod){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        ConfirmatoryFactorAnalysis congeneric = new ConfirmatoryFactorAnalysis(
                cfaMatrix,
                numberOfExaminees,
                ConfirmatoryFactorAnalysisModel.CONGENERIC,
                estimationMethod);
        congeneric.optimize(optimizationMethod);
        logger.info("Congeneric Model CFA completed\n" + congeneric.printOptimizationSummary());

        ConfirmatoryFactorAnalysis tauEquivalent = new ConfirmatoryFactorAnalysis(
                cfaMatrix,
                numberOfExaminees,
                ConfirmatoryFactorAnalysisModel.TAU_EQUIVALENT,
                estimationMethod);
        tauEquivalent.optimize(optimizationMethod);
        logger.info("Tau-equivalent Model CFA completed\n" + tauEquivalent.printOptimizationSummary());

        ConfirmatoryFactorAnalysis parallel = new ConfirmatoryFactorAnalysis(
                cfaMatrix,
                numberOfExaminees,
                ConfirmatoryFactorAnalysisModel.PARALLEL,
                estimationMethod);
        parallel.optimize(optimizationMethod);
        logger.info("Parallel Model CFA completed\n" + parallel.printOptimizationSummary());

        f.format("%-60s", "            CONGENERIC, TAU-EQUIVALENT, AND PARALLEL");f.format("%n");
        f.format("%-60s", "                  CONFIRMATORY FACTOR ANLAYSIS");f.format("%n");f.format("%n");
        f.format("%n");
        f.format("%-60s", "   ****CAUTION CONFIRMATORY FACTOR ANALYSIS IS STILL IN DEVELOPMENT****");f.format("%n");
        f.format("%-60s", "            ****IT HAS NOT BEEN THOROUGHLY TESTED****");f.format("%n");f.format("%n");
        f.format("%-25s", "                         MODEL SUMMARY");f.format("%n");
        f.format("%-60s", "=================================================================");f.format("%n");
        f.format("%11s", "Statistic"); f.format("%5s", "");
            f.format("%10s", "Congeneric");f.format("%5s", "");
            f.format("%14s", "Tau-Equivalent");f.format("%5s", "");
            f.format("%10s", "Parallel");f.format("%n");
       f.format("%-60s", "-----------------------------------------------------------------");f.format("%n");
       f.format("%11s",  "Fmin");f.format("%5s",  "");
            f.format("% 10.4f", congeneric.getEstimator().fMin()); f.format("%5s",  "");
            f.format("% 10.4f", tauEquivalent.getEstimator().fMin()); f.format("%9s",  "");
            f.format("% 10.4f", parallel.getEstimator().fMin()); f.format("%n");
        f.format("%11s",  "Chi^2");f.format("%5s",  "");
            f.format("% 10.4f", congeneric.getEstimator().chisquare()); f.format("%5s",  "");
            f.format("% 10.4f", tauEquivalent.getEstimator().chisquare()); f.format("%9s",  "");
            f.format("% 10.4f", parallel.getEstimator().chisquare()); f.format("%n");
        f.format("%11s",  "df");f.format("%5s",  "");
            f.format("% 10.4f", congeneric.getEstimator().degreesOfFreedom()); f.format("%5s",  "");
            f.format("% 10.4f", tauEquivalent.getEstimator().degreesOfFreedom()); f.format("%9s",  "");
            f.format("% 10.4f", parallel.getEstimator().degreesOfFreedom()); f.format("%n");
        f.format("%11s",  "p-rho");f.format("%5s",  "");
            f.format("% 10.4f", congeneric.getEstimator().pvalue()); f.format("%5s",  "");
            f.format("% 10.4f", tauEquivalent.getEstimator().pvalue()); f.format("%9s",  "");
            f.format("% 10.4f", parallel.getEstimator().pvalue()); f.format("%n");
        f.format("%11s",  "GFI");f.format("%5s",  "");
            f.format("% 10.4f", congeneric.getEstimator().gfi()); f.format("%5s",  "");
            f.format("% 10.4f", tauEquivalent.getEstimator().gfi()); f.format("%9s",  "");
            f.format("% 10.4f", parallel.getEstimator().gfi()); f.format("%n");
        f.format("%11s",  "AGFI");f.format("%5s",  "");
            f.format("% 10.4f", congeneric.getEstimator().agfi()); f.format("%5s",  "");
            f.format("% 10.4f", tauEquivalent.getEstimator().agfi()); f.format("%9s",  "");
            f.format("% 10.4f", parallel.getEstimator().agfi()); f.format("%n");
        f.format("%11s",  "RMR");f.format("%5s",  "");
            f.format("% 10.4f", Math.sqrt(congeneric.getEstimator().meanSquaredResidual())); f.format("%5s",  "");
            f.format("% 10.4f", Math.sqrt(tauEquivalent.getEstimator().meanSquaredResidual())); f.format("%9s",  "");
            f.format("% 10.4f", Math.sqrt(parallel.getEstimator().meanSquaredResidual())); f.format("%n");
        f.format("%11s",  "RMSEA");f.format("%5s",  "");
            f.format("% 10.4f", congeneric.getEstimator().rmsea()); f.format("%5s",  "");
            f.format("% 10.4f", tauEquivalent.getEstimator().rmsea()); f.format("%9s",  "");
            f.format("% 10.4f", parallel.getEstimator().rmsea()); f.format("%n");
        f.format("%11s",  "Reliability");f.format("%5s",  "");
            f.format("% 10.4f", congeneric.getEstimator().mcdonaldOmega()); f.format("%5s",  "");
            f.format("% 10.4f", tauEquivalent.getEstimator().mcdonaldOmega()); f.format("%9s",  "");
            f.format("% 10.4f", parallel.getEstimator().mcdonaldOmega()); f.format("%n");
       f.format("%-60s", "-----------------------------------------------------------------");f.format("%n");f.format("%n");
       f.format("%n");
       f.format("%n");
       sb.append(congeneric.getEstimator().printEstimates(items));f.format("%n");f.format("%n");
       f.format("%n");
       sb.append(tauEquivalent.getEstimator().printEstimates(items));f.format("%n");f.format("%n");
       f.format("%n");
       sb.append(parallel.getEstimator().printEstimates(items));f.format("%n");f.format("%n");

       return f.toString();
    }

}

/**
 * Copyright 2015 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.statistics.StreamingCovarianceMatrix;
import com.itemanalysis.psychometrics.reliability.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;

/**
 * A newer and more clear version of TestSummary.java. It does not include the part-test reliability statistics.
 */
public class StreamingTestScoreAnalysis {

    private ArrayList<VariableAttributes> variableAttributes = null;

    private DescriptiveStatistics stats = null;

    private StandardDeviation stdDev = null;

    private ReliabilitySummary reliability = null;

    private StreamingCovarianceMatrix relMatrix = null;

    private int reliabilitySampleSize = 0;

    private KR21 kr21 = null;

    private int numberOfItems = 0;

    private boolean unbiased = true;

    private boolean deletedReliability = false;

    private ConditionalSEM CSEM = null;

    private boolean computeCSEM = false;

    private int[] cutScores = null;

    public StreamingTestScoreAnalysis(ArrayList<VariableAttributes> variableAttributes, boolean unbiased, boolean deletedReliability, boolean computeCSEM, int[] cutScores){
        this.variableAttributes = variableAttributes;
        this.numberOfItems = variableAttributes.size();
        this.unbiased = unbiased;
        this.deletedReliability = deletedReliability;
        this.computeCSEM = computeCSEM;
        this.cutScores = cutScores;
        stats = new DescriptiveStatistics();
        stdDev = new StandardDeviation(unbiased);

        ArrayList<VariableName> vNames = new ArrayList<VariableName>();
        for(VariableAttributes v : variableAttributes){
            vNames.add(v.getName());
        }

        relMatrix = new StreamingCovarianceMatrix(vNames, unbiased);
    }


    public void increment(double score){
        stats.addValue(score);
        stdDev.increment(score);
    }

    public void incrementReliability(int xIndex, int yIndex, double x, double y){
        relMatrix.increment(xIndex, yIndex, x, y);
        reliabilitySampleSize++;
    }

    public Huynh computeDecisionConsistency(){
        Huynh huynh = new Huynh((int)this.numberOfItems(),
                (int)stats.getN(),
                cutScores.length,
                stats.getMean(),
                stats.getStandardDeviation(),
                cutScores
        );
        return huynh;
    }

    public ConditionalSEM computeCSEM(ScoreReliability reliability, boolean unbiased){
        Integer[] scores = getAllScores();
        kr21 = new KR21(this.numberOfItems(),stats.getMean(), stdDev.getResult(), unbiased);
        CSEM = new ConditionalSEM(scores, this.computeMaximumPossibleTestScore(), reliability, this.kr21, unbiased);
        return CSEM;
    }

    public double kr21(){
        kr21 = new KR21(this.numberOfItems(), stats.getMean(), stdDev.getResult(), unbiased);
        return kr21.value();
    }

    public int numberOfItems(){
        return numberOfItems;
    }

    public ReliabilitySummary getReliabilitySummary(){
        return reliability;
    }

    private double computeMaximumPossibleTestScore(){
        Iterator<VariableAttributes> iter = variableAttributes.iterator();
        double sum = 0.0;
        while(iter.hasNext()){
            sum+=iter.next().getMaximumPossibleItemScore();
        }
        return sum;
    }

    private Integer[] getAllScores(){
        int m = (int)Math.ceil(computeMaximumPossibleTestScore());
        Integer[] s = new Integer[m+1];
        for(int i=0;i<=m;i++){
            s[i]=i;
        }
        return s;
    }

    /**
     * Computes the sum score values at the pth and 100-pth percentile.
     *
     * @param percentile a number between 1 and 99.
     * @return upper and lower bounds for the D-index
     */
    public double[] getDIndexBounds(int percentile){
        int p = Math.max(Math.min(99,percentile), 1);
        double[] bounds = new double[2];
        bounds[0] = stats.getPercentile(p);
        bounds[1] = stats.getPercentile(100-p);
        return bounds;
    }

    public String print(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String f2="%.4f";

        f.format("%n");
        f.format("%n");
        f.format("%-21s", "          TEST LEVEL STATISTICS           "); f.format("%n");
        f.format("%-42s", "==========================================");
        f.format("%n");
        f.format("%-18s", "Number of Items = "); f.format("%-10d", this.numberOfItems()); f.format("%n");
        f.format("%-22s", "Number of Examinees = "); f.format("%-10d", stats.getN()); f.format("%n");
        f.format("%-6s", "Min = "); f.format(f2, stats.getMin()); f.format("%n");
        f.format("%-6s", "Max = "); f.format(f2, stats.getMax()); f.format("%n");
        f.format("%-7s", "Mean = "); f.format(f2, stats.getMean()); f.format("%n");
        f.format("%-9s", "Median = "); f.format(f2, stats.getPercentile(50)); f.format("%n");
        f.format("%-21s", "Standard Deviation = "); f.format(f2, stdDev.getResult()); f.format("%n");
        f.format("%-22s", "Interquartile Range = "); f.format(f2, stats.getPercentile(75)-stats.getPercentile(25)); f.format("%n");
        f.format("%-11s", "Skewness = "); f.format(f2, stats.getSkewness()); f.format("%n");
        f.format("%-11s", "Kurtosis = "); f.format(f2, stats.getKurtosis()); f.format("%n");
        f.format("%-7s", "KR21 = "); f.format(f2, this.kr21()); f.format("%n");
        f.format("%-42s", "==========================================");f.format("%n");
        f.format("%n");
        f.format("%n");

        if(reliabilitySampleSize>0){
            reliability = new ReliabilitySummary(relMatrix, variableAttributes, deletedReliability);
            sb.append(reliability.toString());
            if(deletedReliability){
                f.format("%n");
                f.format("%n");
                f.format(reliability.itemDeletedString());
            }
        }

        if(cutScores!=null){
            f.format("%n");
            f.format("%n");
            sb.append(this.computeDecisionConsistency().toString());
        }

        if(computeCSEM){
            f.format("%n");
            f.format("%n");
            sb.append(this.computeCSEM(reliability.value(), unbiased).print());
        }

        f.format("%n");
        f.format("%n");
        f.format("%n");

        return f.toString();

    }

}

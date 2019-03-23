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
package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.statistics.StreamingCovarianceMatrix;
import com.itemanalysis.psychometrics.reliability.*;
import com.itemanalysis.psychometrics.scaling.RawScore;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.*;


/**
 * Currently this class stores the test score data in memory. Item level data is not stored in memory.
 * Rather, the inter-item covariance matrix is incrementally updated.
 *
 * @author J. Patrick Meyer, meyerjp at itemanalysis.com
 */
public class TestSummary {
    
    private int numberOfItems = 0;

    private ReliabilitySummary reliability = null;

    private int reliabilitySampleSize = 0;

    private DescriptiveStatistics stats = null;

    private StandardDeviation stdDev = null;

    private KR21 kr21 = null;

    private StreamingCovarianceMatrix relMatrix = null;

    private StreamingCovarianceMatrix partRelMatrix = null;

    private int[] cutScores = null;

    private ArrayList<VariableAttributes> variableAttributes = null;

    private boolean deletedReliability = false;

    private ConditionalSEM CSEM = null;

    private int numberOfSubscales = 0;

    private boolean showCsem = false;

    private boolean unbiased = false;

    public TestSummary(int numberOfItems, int numberOfSubscales, ArrayList<Integer> cutScores,
                       ArrayList<VariableAttributes> variableAttributes, boolean unbiased, boolean deletedReliability, boolean showCsem){
        this.numberOfItems = numberOfItems;

        if(cutScores!=null){
            this.cutScores = new int[cutScores.size()];
            int i=0;
            for(Integer intgr : cutScores){
                this.cutScores[i] = intgr.intValue();
                i++;
            }
        }

        this.variableAttributes = variableAttributes;
        this.unbiased = unbiased;
        this.deletedReliability = deletedReliability;
        this.showCsem = showCsem;
        stats = new DescriptiveStatistics();
        stdDev = new StandardDeviation(unbiased);
        relMatrix = new StreamingCovarianceMatrix(extractVariableNames(variableAttributes));
        this.numberOfSubscales = numberOfSubscales;
        if(numberOfSubscales>1) partRelMatrix = new StreamingCovarianceMatrix(numberOfSubscales);
    }

    public TestSummary(int numberOfItems, int numberOfSubscales, int[] cutScores,
                       ArrayList<VariableAttributes> variableAttributes, boolean unbiased, boolean deletedReliability, boolean showCsem){
        this.numberOfItems = numberOfItems;
        this.cutScores = cutScores;
        this.variableAttributes = variableAttributes;
        this.unbiased = unbiased;
        this.deletedReliability = deletedReliability;
        this.showCsem = showCsem;
        stats = new DescriptiveStatistics();
        stdDev = new StandardDeviation(unbiased);
        relMatrix = new StreamingCovarianceMatrix(extractVariableNames(variableAttributes));
        this.numberOfSubscales = numberOfSubscales;
        if(numberOfSubscales>1) partRelMatrix = new StreamingCovarianceMatrix(numberOfSubscales);
    }

    public TestSummary(int numberOfItems, int numberOfSubscales, int[] cutScores,
                       LinkedHashMap<VariableName, VariableAttributes> variableAttributeMap, boolean unbiased,
                       boolean deletedReliability, boolean showCsem){

       this.variableAttributes = new ArrayList<VariableAttributes>();
        for(VariableName v : variableAttributeMap.keySet()){
            this.variableAttributes.add(variableAttributeMap.get(v));
        }

        this.unbiased = unbiased;
        this.numberOfItems = numberOfItems;
        this.cutScores = cutScores;
        this.deletedReliability = deletedReliability;
        this.showCsem = showCsem;
        stats = new DescriptiveStatistics();
        stdDev = new StandardDeviation(unbiased);
        relMatrix = new StreamingCovarianceMatrix(extractVariableNames(variableAttributes));
        this.numberOfSubscales = numberOfSubscales;
        if(numberOfSubscales>1) partRelMatrix = new StreamingCovarianceMatrix(numberOfSubscales);

    }

    public void increment(RawScore rawScore){
        double rawScoreValue = rawScore.value();
        increment(rawScoreValue);
    }

    public void increment(double score){
        stats.addValue(score);
        stdDev.increment(score);
    }

    public void incrementReliability(int xIndex, int yIndex, double x, double y){
        relMatrix.increment(xIndex, yIndex, x, y);
        reliabilitySampleSize++;
    }

    public void incrementPartTestReliability(int xIndex, int yIndex, double x, double y){
        partRelMatrix.increment(xIndex, yIndex, x, y);
    }

    public void incrementPartTestReliability(RawScore rawScore){
        Set<String> keys = rawScore.getSubscaleScore().keySet();
        Iterator<String> outerIter = keys.iterator();
        Iterator<String> innerIter = keys.iterator();
        String temp = "", temp2 = "";
        int i=0, j=0;
        while(outerIter.hasNext()){
            temp = outerIter.next();
            innerIter = keys.iterator();
            j=0;
            while(innerIter.hasNext()){
                temp2 = innerIter.next();
                incrementPartTestReliability(i, j, rawScore.getSubscaleScoreAt(temp), rawScore.getSubscaleScoreAt(temp2));
                j++;
            }
            i++;
        }
    }

    public double computeMaximumPossibleTestScore(){
        Iterator<VariableAttributes> iter = variableAttributes.iterator();
        double sum = 0.0;
        while(iter.hasNext()){
            sum+=iter.next().getMaximumPossibleItemScore();
        }
        return sum;
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

    public Integer[] getAllScores(){
        int m = (int)Math.ceil(computeMaximumPossibleTestScore());
        Integer[] s = new Integer[m+1];
        for(int i=0;i<=m;i++){
            s[i]=i;
        }
        return s;
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

    public StreamingCovarianceMatrix getCovarianceMatrix(){
        return relMatrix;
    }

    public double[] getDIndexBounds(){
        double[] bounds = new double[2];
        bounds[0] = stats.getPercentile(27);
        bounds[1] = stats.getPercentile(73);
        return bounds;
    }

    public double getMean(){
        return stats.getMean();
    }

    public double getStandardDeviation(){
        return stats.getStandardDeviation();
    }

    public double getVariance(){
        return stats.getVariance();
    }

    /**
     *
     * @param p a value between 0 and 100.
     * @return pth percentile value
     */
    public double getPercentile(double p){
        return stats.getPercentile(p);
    }

    public double getSkewness(){
        return stats.getSkewness();
    }

    public double getKurtosis(){
        return stats.getKurtosis();
    }

    public double getSampleSize(){
        return stats.getN();
    }

    public double getMin(){
        return stats.getMin();
    }

    public double getMax(){
        return stats.getMax();
    }

    public double getGeometricMean(){
        return stats.getGeometricMean();
    }

    private ArrayList<VariableName> extractVariableNames(ArrayList<VariableAttributes> variableAttributes){
        ArrayList<VariableName> vNames = new ArrayList<VariableName>();
        for(VariableAttributes v : variableAttributes){
            vNames.add(v.getName());
        }
        return vNames;
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
		f.format("%-22s", "Number of Examinees = "); f.format("%10d", stats.getN()); f.format("%n");
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

        if(showCsem){
            f.format("%n");
            f.format("%n");
            sb.append(this.computeCSEM(reliability.value(), unbiased).print());
        }

        //reliability of subscale defined part tests
        if(numberOfSubscales>1){
            reliability = new ReliabilitySummary(partRelMatrix, variableAttributes, false);
            f.format("%n");
            f.format("%n");
            f.format("%n");
            f.format("%59s", "                   ITEM GROUP DEFINED PART-TEST RELIABILITY");
            f.format("%n");
            sb.append(reliability.toString());
            f.format("%-6s", "Number of part-tests = " + partRelMatrix.getNumberOfVariables());f.format("%n");
        }

        f.format("%n");
        f.format("%n");
        f.format("%n");

        return f.toString();

    }

}

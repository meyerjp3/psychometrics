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
package com.itemanalysis.psychometrics.measurement;

import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;
import com.itemanalysis.psychometrics.reliability.*;
import com.itemanalysis.psychometrics.scaling.RawScore;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Set;



/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class TestSummary {
    
    private int numberOfItems = 0;

    private ReliabilitySummary reliability = null;

    private int reliabilitySampleSize = 0;

    private DescriptiveStatistics stats = null;

    private StandardDeviation stdDev = null;

    private KR21 kr21 = null;

    private CovarianceMatrix relMatrix = null;

    private CovarianceMatrix partRelMatrix = null;

    private ArrayList<Integer> cutScores = null;

    private ArrayList<VariableInfo> vInfo = null;

    private boolean deletedReliability = false;

    private ConditionalSEM CSEM = null;

    private int numberOfSubscales = 0;

    private boolean showCsem = false;

    public TestSummary(int numberOfItems, ArrayList<VariableInfo> vInfo){
        this(numberOfItems, 1, null, vInfo, false, false, false);
    }

    public TestSummary(int numberOfItems, int numberOfSubscales, ArrayList<Integer> cutScores,
            ArrayList<VariableInfo> vInfo, boolean unbiased, boolean deletedReliability, boolean showCsem){
        this.numberOfItems = numberOfItems;
        this.cutScores = cutScores;
        this.vInfo = vInfo;
        this.deletedReliability = deletedReliability;
        this.showCsem = showCsem;
        stats = new DescriptiveStatistics();
        stdDev = new StandardDeviation(unbiased);
        relMatrix = new CovarianceMatrix(vInfo);
        this.numberOfSubscales = numberOfSubscales;
        if(numberOfSubscales>1) partRelMatrix = new CovarianceMatrix(numberOfSubscales);
    }

    public void increment(RawScore rawScore){
        double rawScoreValue = rawScore.value();
        stats.addValue(rawScoreValue);
        stdDev.increment(rawScoreValue);
    }

    public void incrementReliability(int xIndex, int yIndex, Double x, Double y){
        relMatrix.increment(xIndex, yIndex, x, y);
        reliabilitySampleSize++;
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
                partRelMatrix.increment(i, j, rawScore.getSubscaleScoreAt(temp), rawScore.getSubscaleScoreAt(temp2));
                j++;
            }
            i++;
        }
    }

    public double computeMaximumPossibleTestScore(){
        Iterator<VariableInfo> iter = vInfo.iterator();
        double sum = 0.0;
        while(iter.hasNext()){
            sum+=iter.next().getMaximumPossibleItemScore();
        }
        return sum;
    }

    public Huynh computeDecisionConsistency(){
        Integer[] cs = new Integer[cutScores.size()];
        cs = cutScores.toArray(cs);
        
        Huynh huynh = new Huynh((int)this.numberOfItems(),
                                (int)stats.getN(),
                                cutScores.size(),
                                stats.getMean(),
                                stats.getStandardDeviation(),
                                cs
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

    public ConditionalSEM computeCSEM(ScoreReliability reliability){
        Integer[] scores = getAllScores();
        kr21 = new KR21(this.numberOfItems(),stats.getMean(), stdDev.getResult());
        CSEM = new ConditionalSEM(scores, this.computeMaximumPossibleTestScore(), reliability, this.kr21);
        return CSEM;
    }

    public double kr21(boolean unbiased){
		kr21 = new KR21(this.numberOfItems(), stats.getMean(), stdDev.getResult());
		return kr21.value(unbiased);
	}

    public int numberOfItems(){
        return numberOfItems;
    }

    public CovarianceMatrix getCovarianceMatrix(){
        return relMatrix;
    }

    public String print(boolean unbiased){
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
		f.format("%-7s", "KR21 = "); f.format(f2, this.kr21(unbiased)); f.format("%n");
        f.format("%-42s", "------------------------------------------");f.format("%n");
        f.format("%n");
        f.format("%n");
        
        if(reliabilitySampleSize>0){
            reliability = new ReliabilitySummary(relMatrix, stats.getN(), vInfo, unbiased, deletedReliability);
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
            sb.append(this.computeCSEM(reliability.value()).print(unbiased));
        }

        //reliability of subscale defined part tests
        if(numberOfSubscales>1){
            reliability = new ReliabilitySummary(partRelMatrix, stats.getN(), vInfo, unbiased, false);
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

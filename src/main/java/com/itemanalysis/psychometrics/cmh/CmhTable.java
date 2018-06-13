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
package com.itemanalysis.psychometrics.cmh;

import org.apache.commons.math3.stat.Frequency;

import java.util.Formatter;
import java.util.Iterator;

/**
 * A single 2 x C table for computing Cochran-Mantel-Haenszel DIF statistics.
 *
 * 
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class CmhTable {

    private CmhTableRow focalRow = null;

    private CmhTableRow referenceRow = null;

    private Frequency columnMargin = null;

    //FIXME what happens when a score rho is observed from one row but not another?

    public CmhTable(Object focalCode, Object referenceCode){
        this.focalRow = new CmhTableRow(focalCode);
        this.referenceRow = new CmhTableRow(referenceCode);
        columnMargin = new Frequency();
    }

    public void increment(Object groupValue, Double itemScoreValue){
        focalRow.count(groupValue, itemScoreValue);
        referenceRow.count(groupValue, itemScoreValue);
        if(focalRow.validCode(groupValue) || referenceRow.validCode(groupValue)){
            columnMargin.addValue(itemScoreValue);
        }
    }

    public double focalSumOfScores(){
        return focalRow.sumOfScores();
    }

    public double referenceSumOfScores(){
        return referenceRow.sumOfScores();
    }

    public double focalSize(){
        return focalRow.rowTotal();
    }

    public double referenceSize(){
        return referenceRow.rowTotal();
    }

    public int numberOfScoreLevels(){
        return columnMargin.getUniqueCount();
    }

    /**
     * Table calculation for standardized p-dif
     *
     * @return
     */
    public double meanDifference(){
        double nFoc = (double)focalRow.size();
        double nRef = (double)referenceRow.size();
        if(nFoc==0.0 || nRef==0.0) return 0.0;
        return (focalRow.getMean() - referenceRow.getMean());
    }

    /**
     * Computes the marginal sum of scores. This computation
     * is needed for the Cochran-Mantel-Haenszel statistic.
     *
     * @return sum of scores for the column margin.
     */
    public double sumOfScores(){
        Iterator<Comparable<?>> iter = columnMargin.valuesIterator();
        Double d = null;
        double sum = 0.0;
        while(iter.hasNext()){
            d = (Double)iter.next();
            sum += d*columnMargin.getCount(d);
        }
        return sum;
    }

    /**
     * Computes the marginal sum of squared scores. This computation
     * is needed for the Cochran-Mantel-Haenszel statistic.
     *
     * @return sum of scores for the column margin
     */
    public double sumOfSquaredScores(){
        Iterator<Comparable<?>> iter = columnMargin.valuesIterator();
        Double d = null;
        double sum = 0.0;
         while(iter.hasNext()){
            d = (Double)iter.next();
            sum += Math.pow(d,2)*columnMargin.getCount(d);
        }
        return sum;
    }

    public double expectedValue(){
        double fTotal = focalRow.rowTotal();
        double rTotal = referenceRow.rowTotal();
        double tableTotal = fTotal + rTotal;
        double prop=0.0;
        double ss=0.0;
        if(tableTotal>0){
            prop = fTotal/tableTotal;
            ss = sumOfScores();
            return prop*ss;
        }
        return 0.0;//FIXME should an empty table have an expected rho of zero?
    }

    public double variance(){
        double fTotal = focalRow.rowTotal();
        double rTotal = referenceRow.rowTotal();
        double tableTotal = fTotal + rTotal;
        double prop = 0.0;
        double p1 = 0.0;
        double p2 = 0.0;
        double var = 0.0;
        if(tableTotal>1){
            prop = (rTotal*fTotal)/(Math.pow(tableTotal,2)*(tableTotal-1.0));
            p1 = tableTotal*sumOfSquaredScores();
            p2 = Math.pow(sumOfScores(),2);
            var = prop*(p1-p2);
            return var;
        }
        return 0.0;
    }

    public double commonOddsRatioNumerator(){
        double rr = referenceRow.rightFrequecy();
        double fw = focalRow.wrongFrequency();
        double tableTotal = focalRow.rowTotal() + referenceRow.rowTotal();
        if(tableTotal==0.0) return 0.0;
        return rr*fw/tableTotal;

    }

    public double commonOddsRatioDenominator(){
        double rw = referenceRow.wrongFrequency();
        double fr = focalRow.rightFrequecy();
        double tableTotal = focalRow.rowTotal() + referenceRow.rowTotal();
        if(tableTotal==0.0) return 0.0;
        return rw*fr/tableTotal;
    }

    public double referenceRight(){
        return referenceRow.rightFrequecy();
    }

    public double referenceWrong(){
        return referenceRow.wrongFrequency();
    }

    public double focalRight(){
        return focalRow.rightFrequecy();
    }

    public double focalWrong(){
        return focalRow.wrongFrequency();
    }

    public double total(){
        return focalRow.rowTotal() + referenceRow.rowTotal();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double score = 0.0;

        Iterator<Comparable<?>> iter = columnMargin.valuesIterator();
        f.format("%26s", " ");
        while(iter.hasNext()){
            score = ((Double)iter.next()).doubleValue();
            f.format("%10.0f", score); f.format("%5s", " ");
        }
        f.format("%n");
        f.format("%51s", "-------------------------"); 

        iter = columnMargin.valuesIterator();
        int index=0;
        while(iter.hasNext()){
            score = ((Double)iter.next()).doubleValue();
            if(index>1) f.format("%15s", "---------------");
            index++;
        }
        f.format("%n");

        f.format("%11s", "Reference: "); f.format("%-100s", referenceRow.toString()); f.format("%n");
        f.format("%11s", "Focal:     "); f.format("%-100s", focalRow.toString()); f.format("%n");

        return f.toString();
    }

}

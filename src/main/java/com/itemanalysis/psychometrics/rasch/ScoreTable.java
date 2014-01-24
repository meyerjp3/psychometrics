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
package com.itemanalysis.psychometrics.rasch;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.scaling.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat.OutputAlignment;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.util.Precision;

import java.util.LinkedHashMap;

/**
 * This class uses calibrated items to produce a score table that is based on the nonextreme items.
 * It shows the
 *
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class ScoreTable {

    private RatingScaleModel rsm = null;

    private LinkedHashMap<VariableName, RatingScaleItem> items = null;

    /**
     * Two-way array of raw to theta transformation.
     * The first column is the raw score, the second is theta.
     */
    private double[][] scoreTable = null;
    
    private double[] stdError = null;

    boolean[] extremeScore = null;

    public ScoreTable(LinkedHashMap<VariableName, RatingScaleItem> items){
        rsm = new RatingScaleModel();
        this.items = items;
    }

    /**
     * Computes the overall Valid Maximum Raw Score using nonextreme items.
     * It does not take into account specific examinee response and missing
     * data patterns. This method is only used to create the score table.
     *
     * @return
     */
    private double validMRS(){
        double vmprs = 0.0;
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            if(!rsi.extremeItem() && !rsi.droppedItem()) vmprs += rsi.getMaximumPossibleScore();
        }
        return vmprs;
    }

    public void updateScoreTable(int globalMaxIter, double globalConvergence, double adjust){
        double DELTA = globalConvergence+1.0; //LCONV
        Max maxDelta = new Max();
        int iter = 0;

        //initialize raw score and extreme score arrays
        double maxRaw = validMRS();
        int n = (int)maxRaw;
        int np1 = n+1;
        scoreTable = new double[np1][2];
        extremeScore = new boolean[np1];
        stdError = new double[np1];
        for(int i=0;i<np1;i++){
            scoreTable[i][0] = i;
            scoreTable[i][1] = scoreTableProx(scoreTable[i][0], maxRaw, adjust);
            stdError[i] = 0.0;
            if(i==0 || i==n){
                extremeScore[i] = true;
            }else{
                extremeScore[i] = false;
            }
        }

        double previousTheta = 0.0;
        //update nonextreme persons
        while(DELTA >= globalConvergence && iter<globalMaxIter){
            //Change in person parameter is not counted toward delta.
            for(int i=0;i<np1;i++){
                if(!extremeScore[i]){
                    previousTheta = scoreTable[i][1];
                    scoreTable[i][1] = personUpdate(scoreTable[i][1], scoreTable[i][0],  maxRaw, 0.0, DELTA);
                    maxDelta.increment(Math.abs(previousTheta-scoreTable[i][1]));
                }
            }
            DELTA = maxDelta.getResult();
            maxDelta.clear();
            iter++;
        }

        int iter2=0;
        double DELTA2 = 1.0;
        double adjustedRaw = 0.0;
        int index=0;
        //update lowest and highest (extreme) scores - convergence = 0.01 and maximum of 25 iterations
        while(DELTA2 >= 0.01 && iter2<25){
            index=0;
            previousTheta = scoreTable[index][1];
            adjustedRaw = adjust;
            scoreTable[index][1] = personUpdate(scoreTable[index][1], adjustedRaw,  maxRaw, 0.0, DELTA);
            maxDelta.increment(Math.abs(previousTheta-scoreTable[index][1]));
            index=n;
            previousTheta = scoreTable[index][1];
            adjustedRaw = scoreTable[index][0]-adjust;
            scoreTable[index][1] = personUpdate(scoreTable[index][1], adjustedRaw,  maxRaw, 0.0, DELTA);
            
            maxDelta.increment(Math.abs(previousTheta-scoreTable[index][1]));

            DELTA2 = maxDelta.getResult();
            maxDelta.clear();
            iter2++;
        }
    }

    private double personUpdate(double personTheta, double rawScore, double MPRS, double xMin, double d){
        double TCC1 = 0.0; //this is the TCC at current theta rho
        double TCC2 = 0.0; //this is the TCC at current theta rho + d

        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            if(!rsi.extremeItem() && !rsi.droppedItem()){
                TCC1 += rsm.expectedValue(personTheta, rsi.getDifficulty(), rsi.getThresholds());
                TCC2+= rsm.expectedValue(personTheta+d, rsi.getDifficulty(), rsi.getThresholds());
            }
        }
        double slope = d/(logisticOgive(TCC2, xMin, MPRS)-logisticOgive(TCC1, xMin, MPRS));
        double intercept = personTheta - slope*logisticOgive(TCC1, xMin, MPRS);
        double tempTheta = slope*logisticOgive(rawScore, xMin, MPRS)+intercept;
        //do not change theta by more than one logit per iteration - from WINSTEPS documents
        personTheta = Math.max(Math.min(personTheta+1,tempTheta),personTheta-1);
        return personTheta;
    }

    /**
     * Local logistic ogive from WINSTEPS documentation.
     *
     * @param x observed score
     * @param xMin minimum possible score
     * @param xMax maximum possible score
     * @return
     */
    private double logisticOgive(double x, double xMin, double xMax){
        return Math.log((x-xMin)/(xMax-x));
    }

    private double scoreTableProx(double raw, double MPRS, double adjust){
        if(raw==0.0){
            raw+=adjust;
        }else if(raw==MPRS){
            raw-=adjust;
        }
        double thetaProx = Math.log(raw/(MPRS-raw));
        return thetaProx;
    }

    public void computePersonStandardErrors(){
        double sum=0.0;
        RatingScaleItem rsi = null;

        for(int i=0;i<scoreTable.length;i++){
            sum = 0.0;
            if(extremeScore[i]) stdError[i] = Double.NaN;//FIXME change to actual computation
            for(VariableName v : items.keySet()){
                rsi = items.get(v);
                if(!rsi.extremeItem()){
                    sum+=rsm.denomInf(scoreTable[i][1], rsi.getDifficulty(), rsi.getThresholds());
                }
            }
            if(sum==0.0){
                stdError[i] = Double.NaN;
            }else{
                stdError[i] = 1/Math.sqrt(sum);
            }
        }
    }

    public String printScoreTable(){
        TextTableColumnFormat[] cformats = new TextTableColumnFormat[3];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setDoubleFormat(8, 2, OutputAlignment.RIGHT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setDoubleFormat(8, 2, OutputAlignment.RIGHT);
        cformats[2] = new TextTableColumnFormat();
        cformats[2].setDoubleFormat(8, 2, OutputAlignment.RIGHT);

        int n = scoreTable.length;

        TextTable table = new TextTable();
        table.addAllColumnFormats(cformats, n+5);
        table.getRowAt(0).addHeader(0, 3, "SCORE TABLE", TextTablePosition.CENTER);
        table.getRowAt(1).addHorizontalRule(0, 3, "=");
        table.getRowAt(2).addHeader(0, 1, "Score", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(1, 1, "Theta", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(2, 1, "Std. Error", TextTablePosition.CENTER);
        table.getRowAt(3).addHorizontalRule(0, 6, "-");
        for(int i=0;i<n;i++){
            table.getRowAt(i+4).addDoubleAt(0, scoreTable[i][0]);
            table.getRowAt(i+4).addDoubleAt(1, scoreTable[i][1]);
            table.getRowAt(i+4).addDoubleAt(2, stdError[i]);
        }
        table.getRowAt(n+4).addHorizontalRule(0, 3, "=");

        return table.toString();
    }

    public void linearTransformation(DefaultLinearTransformation lt, int precision){
        for(int i=0;i<scoreTable.length;i++){
            scoreTable[i][1] = Precision.round(lt.transform(scoreTable[i][1]), precision);
            stdError[i] *= lt.getScale();
        }
    }

}

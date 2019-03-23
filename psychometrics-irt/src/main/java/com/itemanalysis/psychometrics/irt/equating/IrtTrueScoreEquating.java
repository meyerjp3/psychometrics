/*
 * Copyright 2013 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import org.apache.commons.math3.util.Precision;

import java.util.LinkedHashMap;

public class IrtTrueScoreEquating implements IrtEquating{

    private LinkedHashMap<VariableName, ItemResponseModel> itemFormX = null;

    private LinkedHashMap<VariableName, ItemResponseModel> itemFormY = null;

    private double minScoreX = 0.0;

    private double minScoreY = 0.0;

    private double maxScore = 0.0;

    private double[] rawScore = null;

    private double[] xTheta = null;

    private double[] yEquivTrueScore = null;

    private int[] iterations = null;

    private char[] status = null;

    private int itemIndex = 0;

    private double lowScale = 1.0;


    public IrtTrueScoreEquating(LinkedHashMap<VariableName, ItemResponseModel> itemFormX, LinkedHashMap<VariableName, ItemResponseModel> itemFormY){
        this.itemFormX = itemFormX;
        this.itemFormY = itemFormY;
        computeScoreBounds();
    }

    /**
     * Compute minimum and maximum possible true scores.
     *
     */
    private void computeScoreBounds(){
        ItemResponseModel irm;
        double c = 0.0;
        double max = 0.0;

        for(VariableName v : itemFormX.keySet()){
            irm = itemFormX.get(v);
            c = irm.getGuessing();
            max = irm.getMaxScoreWeight();
            minScoreX += irm.getMinScoreWeight()*(1.0-c) + max*c;
            maxScore += max;
        }

        for(VariableName v : itemFormY.keySet()){
            irm = itemFormY.get(v);
            c = irm.getGuessing();
            max = irm.getMaxScoreWeight();
            minScoreY += irm.getMinScoreWeight()*(1.0-c) + max*c;
        }

        //Used for scaling raw scores that exceed true score bounds.
        if(minScoreY>0) lowScale = minScoreY/minScoreX;

    }

    /**
     * Find Form X theta values that corresponds to a particular true score.
     * This method uses a Newton-Rhapson procedure to find the theta value.
     *
     * @param trueScore value for which form X theta is sought
     * @return
     */
    public double xTrueScoreToTheta(double trueScore){
        ItemResponseModel irm;
        double tcc = 0.0;
        double converge = 0.0001;
        double maxIter = 150;
        double delta = 1.0;
        int iter = 0;
        double theta = 0.0;
        double thetaOld = theta;

        while(delta > converge && iter < maxIter){
            thetaOld = theta;
            delta = -(trueScore-xTccAt(theta))/xTccDerivAt(theta);
            theta = thetaOld - delta;

            //restrict theta to be between -99 and 99
            theta = Math.max(Math.min(theta, 99), -99);
            delta = Math.abs(theta-thetaOld);
            iter++;
        }

//        System.out.println(itemIndex + " TO:" + thetaOld + " -> TN: " + theta + " delta: " + delta + " iter: " + iter);

        iterations[itemIndex] = iter;

        if(delta<converge){
            status[itemIndex] = 'Y';
        }else{
            status[itemIndex] = 'N';
        }

        return theta;
    }

    /**
     * Compute Form X TCC at theta.
     * @param theta value at which TCC is computed
     * @return
     */
    private double xTccAt(double theta){
        ItemResponseModel irm;
        double tcc = 0.0;
        for(VariableName v : itemFormX.keySet()){
            irm = itemFormX.get(v);
            tcc += irm.expectedValue(theta);
        }

        return tcc;
    }

    /**
     * First derivative of the Form X TCC. This method is needed for the
     * Newton-Rhapson procedure in xTrueScoreToTheta().
     *
     * @param theta value at which TCC derivative is computed
     * @return
     */
    private double xTccDerivAt(double theta){
        ItemResponseModel irm;
        double tccDeriv = 0.0;
        for(VariableName v : itemFormX.keySet()){
            irm = itemFormX.get(v);
            tccDeriv += irm.derivTheta(theta);
        }
        return tccDeriv;
    }

    /**
     * Compute Form Y TCC at theta.
     * @param theta value at which TCC is computed
     * @return
     */
    private double yTccAt(double theta){
        ItemResponseModel irm;
        double tcc = 0.0;

        for(VariableName v : itemFormY.keySet()){
            irm = itemFormY.get(v);
            tcc += irm.expectedValue(theta);
        }
        return tcc;
    }

    /**
     * Primary method for conducting the IRT true score equating.
     */
    public void equateScores(){
        int size = (int)(maxScore  + 1.0);

        rawScore = new double[size];
        xTheta = new double[size];
        yEquivTrueScore = new double[size];
        iterations = new int[size];
        status = new char[size];

        double score = 0.0;

        for(int i=0;i<size;i++){
            rawScore[i] = score;

            if(score<=minScoreX){
                xTheta[i] = -99;
                yEquivTrueScore[i] = lowScale*score;
                status[i] = '-';
            }else if(score==maxScore){
                xTheta[i] = 99;
                yEquivTrueScore[i] = yTccAt(xTheta[i]);
                status[i] = '-';
            }else{
                xTheta[i] = xTrueScoreToTheta(score);
                yEquivTrueScore[i] = yTccAt(xTheta[i]);
            }
            score += 1.0;
            itemIndex++;
        }

    }

    /**
     * Score points at which y equivalent is sought
     * @return
     */
    public double[] getFormXScores(){
        return rawScore;
    }

    /**
     *
     * @return Form X theta values that correspond to raw scores.
     */
    public double[] getFormXThetaValues(){
        return xTheta;
    }

    /**
     *
     * @return Form X true scores that are equivalent to Form Y true scores.
     */
    public double[] getYEquivalentScores(){
        return yEquivTrueScore;
    }

    /**
     * Newton-Rhapson convergence status
     * @return
     */
    public char[] getStatus(){
        return status;
    }

    /**
     * Display results in text format with this method.
     *
     * @return
     */
    @Override
    public String toString(){

        TextTableColumnFormat[] cformats = new TextTableColumnFormat[5];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setIntFormat(8, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setDoubleFormat(8, 4, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[2] = new TextTableColumnFormat();
        cformats[2].setDoubleFormat(8, 4, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[3] = new TextTableColumnFormat();
        cformats[3].setIntFormat(6, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[4] = new TextTableColumnFormat();
        cformats[4].setStringFormat(4, TextTableColumnFormat.OutputAlignment.RIGHT);

        int n = rawScore.length;

        TextTable table = new TextTable();
        table.addAllColumnFormats(cformats, n+5);
        table.getRowAt(0).addHeader(0, 5, "SCORE TABLE", TextTablePosition.CENTER);
        table.getRowAt(1).addHorizontalRule(0, 5, "=");
        table.getRowAt(2).addHeader(0, 1, "Score", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(1, 1, "Theta", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(2, 1, "Y-Equiv", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(3, 1, "Round", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(4, 1, "Conv", TextTablePosition.CENTER);
        table.getRowAt(3).addHorizontalRule(0, 6, "-");
        for(int i=0;i<n;i++){
            table.getRowAt(i+4).addIntAt(0, Double.valueOf(rawScore[i]).intValue());
            table.getRowAt(i+4).addDoubleAt(1, xTheta[i]);
            table.getRowAt(i+4).addDoubleAt(2, yEquivTrueScore[i]);
            table.getRowAt(i+4).addIntAt(3, Double.valueOf(Precision.round(yEquivTrueScore[i], 0)).intValue());
            table.getRowAt(i + 4).addStringAt(4, String.valueOf(status[i]).toString());
        }
        table.getRowAt(n+4).addHorizontalRule(0, 5, "=");

        return table.toString();


    }


}

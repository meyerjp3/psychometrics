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
package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.statistics.RobustZ;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat.OutputAlignment;
import com.itemanalysis.psychometrics.texttable.TextTableRow;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.ArrayList;

/**
 *
 * @author J. Patrick Meyer
 */
public class RobustZEquatingTest {

    private double slope = 1.0;

    private double intercept = 0.0;

    private RobustZ[] za = null;

    private RobustZ[] zb = null;

    private double[] aX = null;

    private double[] aY = null;

    private double[] bX = null;

    private double[] bY = null;

    private ArrayList<VariableName> varnamesA = null;

    private ArrayList<VariableName> varnamesB = null;

    private double significanceLevel = 0.05;

    private int modelParams  = 1;

    private int nA = 0;

    private int nB = 0;

    private Percentile percentile = null;

    /**
     * Constructor for use with 2PL and 3PL models.
     *
     * @param aX
     * @param aY
     * @param bX
     * @param bY
     * @param significanceLevel
     * @throws IllegalArgumentException
     */
    public RobustZEquatingTest(double[] aX, double[] aY, double[] bX, double[] bY, double significanceLevel)throws IllegalArgumentException{
        if(aX.length!=aY.length || bX.length!=bY.length){
            throw new IllegalArgumentException("Item parameter arrays must be the same length");
        }

        this.aX = aX;
        this.aY = aY;
        this.bX = bX;
        this.bY = bY;
        nA = aX.length;
        nB = bX.length;
        this.significanceLevel = significanceLevel/2.0;

        percentile = new Percentile();
        if(hasDiscriminationParameters()){
            modelParams = 2;
            testA();
        }else{
            modelParams = 1;
        }
        testB();
    }

    /**
     * Constructor for use with Rasch or 1PL model
     * @param bX
     * @param bY
     * @param significanceLevel
     * @throws IllegalArgumentException
     */
    public RobustZEquatingTest(double[] bX, double[] bY, double significanceLevel)throws IllegalArgumentException{
        nB = bX.length;
        if(nB!=bX.length || nB!=bY.length){
            throw new IllegalArgumentException("Item parameter arrays must be the same length");
        }
        this.bX = bX;
        this.bY = bY;
        this.significanceLevel = significanceLevel;
        modelParams = 1;
        percentile = new Percentile();
        testB();
    }

    /**
     * Check that discrimination parameters are not all one.
     * @return
     */
    private boolean hasDiscriminationParameters(){
        for(int i=0;i<nA;i++){
            if(aX[i]!=1.0 || aY[i]!=1.0) return true;
        }
        return false;
    }

    public void setNames(ArrayList<VariableName> varnamesA, ArrayList<VariableName> varnamesB){
        this.varnamesA = varnamesA;
        this.varnamesB = varnamesB;
    }

    private void testA()throws IllegalArgumentException{
        double[] aDiff = new double[nA];
        za = new RobustZ[nA];

        for(int i=0;i<nA;i++){
            aDiff[i] = Math.log(aX[i]) - Math.log(aY[i]);
        }
        
        double median = percentile.evaluate(aDiff, 50);
        double q3 = percentile.evaluate(aDiff, 75);
        double q1 = percentile.evaluate(aDiff, 25);
        double iqr = q3 - q1;
        Mean mean = new Mean();

        for(int i=0;i<nA;i++){
            za[i] = new RobustZ(aDiff[i], median, iqr);
            if(!za[i].significant(significanceLevel)){
                mean.increment(aDiff[i]);
            }
        }
        slope = Math.exp(mean.getResult());
    }

    private void testB(){
        double[] bDiff = new double[nB];
        zb = new RobustZ[nB];

        for(int i=0;i<nB;i++){
            bDiff[i] = bY[i] - slope*bX[i];
        }

        double median = percentile.evaluate(bDiff, 50);
        double q3 = percentile.evaluate(bDiff, 75);
        double q1 = percentile.evaluate(bDiff, 25);
        double iqr = q3 - q1;
        Mean mean = new Mean();

        for(int i=0;i<nB;i++){
            zb[i] = new RobustZ(bDiff[i], median, iqr);
            if(!zb[i].significant(significanceLevel)){
                mean.increment(bDiff[i]);
            }
        }
        intercept = mean.getResult();
    }

    public RobustZ[] getDiscrimintationZ(){
        return za;
    }

    public RobustZ[] getDifficultyZ(){
        return zb;
    }

    public double getSlope(){
        return slope;
    }

    public double getIntercept(){
        return intercept;
    }

    public String print(boolean discriminationTable){
        if(modelParams==1 && discriminationTable) return "";
        TextTable table = new TextTable();
        int numCols = 4;
        TextTableColumnFormat[] cFormats = new TextTableColumnFormat[numCols];
        TextTableColumnFormat c0 = new TextTableColumnFormat();
        c0.setStringFormat(25, OutputAlignment.LEFT);
        cFormats[0] = c0;

        TextTableColumnFormat c1 = new TextTableColumnFormat();
        c1.setDoubleFormat(8, 4, OutputAlignment.RIGHT);
        cFormats[1] = c1;

        TextTableColumnFormat c2 = new TextTableColumnFormat();
        c2.setDoubleFormat(8, 4, OutputAlignment.RIGHT);
        cFormats[2] = c2;

        TextTableColumnFormat c3 = new TextTableColumnFormat();
        c3.setStringFormat(3, OutputAlignment.LEFT);
        cFormats[3] = c3;

        int numRows = 0;
        if(discriminationTable){
            numRows = varnamesA.size() + 5;
        }else{
            numRows = varnamesB.size() + 5;
        }

        table.addAllColumnFormats(cFormats, numRows);

        String title = "Robust z Test ";
        if(discriminationTable){
            title += "for Item Discrimination";
        }else{
            title += "for Item (step) Difficulty";
        }

        table.getRowAt(0).addHeader(0, numCols, title, TextTablePosition.CENTER);

        table.getRowAt(1).addHorizontalRule(0, numCols, "=");

        table.getRowAt(2).addHeader(0, 1, "Item Pair", TextTablePosition.LEFT);
        table.getRowAt(2).addHeader(1, 1, "z", TextTablePosition.RIGHT);
        table.getRowAt(2).addHeader(2, 1, "pvalue", TextTablePosition.RIGHT);
        table.getRowAt(2).addHeader(3, 1, "Significant", TextTablePosition.RIGHT);

        table.getRowAt(3).addHorizontalRule(0, numCols, "-");

        TextTableRow row = null;
        RobustZ[] rz = null;
        int index = 0;
        if(discriminationTable){
            rz = za;
        }else{
            rz = zb;
        }
        for(RobustZ z : rz){
            if(discriminationTable){
                table.getRowAt(4+index).addStringAt(0, varnamesA.get(index).toString());
            }else{
                table.getRowAt(4+index).addStringAt(0, varnamesB.get(index).toString());
            }

            table.getRowAt(4+index).addDoubleAt(1, z.getRobustZ());
            table.getRowAt(4+index).addDoubleAt(2, z.getPvalue());
            if(z.significant(significanceLevel)){
                table.getRowAt(4+index).addStringAt(3, "*");
            }else{
                table.getRowAt(4+index).addStringAt(3, "");
            }
            index++;
        }

        table.getRowAt(4+index).addHorizontalRule(0, numCols, "=");

        return table.toString();
    }


}

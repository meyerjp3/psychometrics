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
package com.itemanalysis.psychometrics.polycor;

/**
 *
 * @author J. Patrick Meyer
 */
public class MixedCorrelationMatrix {

    //TODO this class needs a lot of work

//    private ArrayList<DefaultVariableAttributes> variables = null;
//
//    private Object[][] matrix = null;
//
//    private CorrelationType[][] corTypes = null;
//
//    private int numberOfVariables = 0;
//
//    private boolean maximumLikelihood = false;
//
//    public enum CorrelationType{
//        PEARSON,        //pearson correlation
//        POLYSERIAL1,    //polyserial correlation with X as continuous variable as it should be
//        POLYSERIAL2,    //polyserial correlation with Y as continuous variable and will be switched to X
//        POLYCHORIC      //polychoric correlation
//    }
//
//    public MixedCorrelationMatrix(ArrayList<DefaultVariableAttributes> variables, boolean maximumLikelihood){
//        this.variables = variables;
//        this.maximumLikelihood = maximumLikelihood;
//        this.numberOfVariables = variables.size();
//        matrix = new Object[numberOfVariables][numberOfVariables];
//        corTypes = new CorrelationType[numberOfVariables][numberOfVariables];
//        this.initializeMatrix();
//    }
//
//    /**
//     * Identifies the type of correlation and creates the appropriate object.
//     * Objects are only created for the upper diagonal. Therefore, the first
//     * index cannot be less than the second index (i.e. i<j is not allowed).
//     *
//     */
//    private void initializeMatrix(){
//        for(int i=0;i<numberOfVariables;i++){
//            for(int j=i;j<numberOfVariables;j++){
//                if((variables.get(i).getType().getItemType()== ItemType.BINARY_ITEM || variables.get(i).getType().getItemType()==ItemType.POLYTOMOUS_ITEM) &&
//                        (variables.get(j).getType().getItemType()== ItemType.BINARY_ITEM || variables.get(j).getType().getItemType()==ItemType.POLYTOMOUS_ITEM)){
//                    if(maximumLikelihood){
//                        matrix[i][j] = new PolychoricML();
//                    }else{
//                        matrix[i][j] = new PolychoricTwoStepOLD();
//                    }
//                    corTypes[i][j] = CorrelationType.POLYCHORIC;
//                }else if((variables.get(i).getType().getItemType()== ItemType.BINARY_ITEM || variables.get(i).getType().getItemType()==ItemType.POLYTOMOUS_ITEM) &&
//                        (variables.get(j).getType().getItemType()==ItemType.CONTINUOUS_ITEM)){
//                    matrix[i][j] = new PolyserialPlugin();
//                    corTypes[i][j] = CorrelationType.POLYSERIAL2;//indicates the continuous variable is Y when it should be X
//                }else if((variables.get(i).getType().getItemType()==ItemType.CONTINUOUS_ITEM) &&
//                        (variables.get(j).getType().getItemType()== ItemType.BINARY_ITEM || variables.get(j).getType().getItemType()==ItemType.POLYTOMOUS_ITEM)){
//                    matrix[i][j] = new PolyserialPlugin();
//                    corTypes[i][j] = CorrelationType.POLYSERIAL1;//indicates teh continuous variable is X as it should be
//                }else if((variables.get(i).getType().getItemType()== ItemType.BINARY_ITEM || variables.get(i).getType().getItemType()==ItemType.POLYTOMOUS_ITEM) &&
//                        (variables.get(j).getType().getItemType()== ItemType.NOT_ITEM && variables.get(j).getType().getDataType()== DataType.DOUBLE)){
//                    matrix[i][j] = new PolyserialPlugin();
//                    corTypes[i][j] = CorrelationType.POLYSERIAL2;//indicates the continuous variable is Y when it should be X
//                }else if((variables.get(i).getType().getItemType()== ItemType.NOT_ITEM && variables.get(i).getType().getDataType()==DataType.DOUBLE) &&
//                        (variables.get(j).getType().getItemType()== ItemType.BINARY_ITEM || variables.get(j).getType().getItemType()==ItemType.POLYTOMOUS_ITEM)){
//                    matrix[i][j] = new PolyserialPlugin();
//                    corTypes[i][j] = CorrelationType.POLYSERIAL1;//indicates teh continuous variable is X as it should be
//                }else if(variables.get(i).getType().getItemType()== ItemType.NOT_ITEM && variables.get(j).getType().getDataType()==DataType.DOUBLE){
//                    matrix[i][j] = new PearsonCorrelation();
//                    corTypes[i][j] = CorrelationType.PEARSON;
//                }
//            }
//        }
//    }
//
//    /**
//     * Increments the appropriate correlation object. Enforces restriction that
//     * the index for x cannot be less than the index for y. this restriction is
//     * due to using only the upper diagonal.
//     *
//     * @param xIndex
//     * @param yIndex
//     * @param x
//     * @param y
//     */
//    public void increment(int xIndex, int yIndex, double x, double y){
//        int i = xIndex;
//        int j = yIndex;
//        if(yIndex<xIndex){
//            i = yIndex;
//            j = xIndex;
//        }
//        Object o = matrix[i][j];
//
//        if(corTypes[i][j]==CorrelationType.PEARSON){
//            ((PearsonCorrelation)o).increment(x, y);
//        }else if(corTypes[i][j]==CorrelationType.POLYSERIAL1){
//            ((PolyserialPlugin)o).increment(x, (int)y);
//        }else if(corTypes[i][j]==CorrelationType.POLYSERIAL2){
//            ((PolyserialPlugin)o).increment(y, (int)x);
//        }else{
//            if(maximumLikelihood){
//                ((PolychoricML)o).addValue((int)x, (int)y);
//            }else{
//                ((PolychoricTwoStepOLD)o).addValue((int)x, (int)y);
//            }
//        }
//    }
//
//    public double[][] getCorrelationMatrix(){
//        double[][] r = new double[numberOfVariables][numberOfVariables];
//        Object o = null;
//        for(int i=0;i<numberOfVariables;i++){
//            for(int j=i;j<numberOfVariables;j++){
//                o = matrix[i][j];
//                if(corTypes[i][j]==CorrelationType.PEARSON){
//                    r[i][j] = ((PearsonCorrelation)o).value();
//                    if(i!=j) r[j][i] = r[i][j];
//                }else if(corTypes[i][j]==CorrelationType.POLYSERIAL1){
//                    r[i][j] = ((PolyserialPlugin)o).value();
//                    if(i!=j) r[j][i] = r[i][j];
//                }else if(corTypes[i][j]==CorrelationType.POLYSERIAL2){
//                    r[i][j] = ((PolyserialPlugin)o).value();
//                    if(i!=j) r[j][i] = r[i][j];
//                }else{
//                    if(maximumLikelihood){
//                        r[i][j] = ((PolychoricML)o).getResult();
//                        if(i!=j) r[j][i] = r[i][j];
//                    }else{
//                        r[i][j] = ((PolychoricTwoStepOLD)o).getResult();
//                        if(i!=j) r[j][i] = r[i][j];
//                    }
//                }
//            }
//        }
//        return r;
//    }
//
//    public double getCorrelationAt(int i, int j){
//        Object o = null;
//        if(j<i){
//            o = matrix[j][i];
//        }else{
//            o = matrix[i][j];
//        }
//        double r = 0.0;
//        if(corTypes[i][j]==CorrelationType.PEARSON){
//            r = ((PearsonCorrelation)o).value();
//        }else if(corTypes[i][j]==CorrelationType.POLYSERIAL1){
//            r = ((PolyserialPlugin)o).value();
//        }else if(corTypes[i][j]==CorrelationType.POLYSERIAL2){
//            r = ((PolyserialPlugin)o).value();
//        }else{
//            if(maximumLikelihood){
//                r = ((PolychoricML)o).getResult();
//            }else{
//                r = ((PolychoricTwoStepOLD)o).getResult();
//            }
//        }
//        return r;
//    }
//
//    public double getStandardErrorAt(int i, int j){
//        Object o = null;
//        if(j<i){
//            o = matrix[j][i];
//        }else{
//            o = matrix[i][j];
//        }
//        double r = Double.NaN;//polyserial will return NaN because standard error not yet implemented
//        if(corTypes[i][j]==CorrelationType.PEARSON){
//            r = ((PearsonCorrelation)o).value(true);
//        }else{
//            if(maximumLikelihood){
//                r = ((PolychoricML)o).getCorrelationStandardError();
//            }else{
//                r = ((PolychoricTwoStepOLD)o).getCorrelationStandardError();
//            }
//        }
//        return r;
//    }
//
//    public String printCorrelationMatrix(boolean showStdError){
//        TextTable table = new TextTable();
//        TextTableColumnFormat[] cformats = new TextTableColumnFormat[numberOfVariables+1];
//        cformats[0] = new TextTableColumnFormat();
//        cformats[0].setStringFormat(10, OutputAlignment.LEFT);
//        for(int i=0;i<numberOfVariables;i++){
//            cformats[i+1] = new TextTableColumnFormat();
//            cformats[i+1].setDoubleFormat(10, 4, OutputAlignment.RIGHT);
//        }
//        double extraRowFactor = 1.0;
//        if(showStdError)extraRowFactor = 2.0;
//        int nrows = numberOfVariables;
//        nrows =  (int)((double)nrows*extraRowFactor+5.0);
//
//        table.addAllColumnFormats(cformats, nrows);
//        table.setAllCellPadding(2);
//
//        table.getRowAt(0).addHeader(0, numberOfVariables+1, "MIXED CORRELATION MATRIX", TextTablePosition.LEFT);
//        table.getRowAt(1).addHorizontalRule(0, numberOfVariables+1, "=");
//
//        for(int i=0;i<numberOfVariables;i++){
//            table.getRowAt(2).addHeader(i+1, 1, variables.get(i).getName().toString(), TextTablePosition.RIGHT);
//        }
//        table.getRowAt(3).addHorizontalRule(0, numberOfVariables+1, "-");
//
//        int row = 0;
//        for(int i=0;i<numberOfVariables;i++){
//            row = 4+i*(int)extraRowFactor;
//            table.getRowAt(row).addStringAt(0, variables.get(i).getName().toString());
//            for(int j=0;j<numberOfVariables;j++){
//                table.getRowAt(row).addDoubleAt(j+1, getCorrelationAt(i,j));
//                if(showStdError){
//                    table.getRowAt(row+1).addDoubleAt(j+1, this.getStandardErrorAt(i, j));
//                }
//            }
//        }
//        table.getRowAt(nrows-1).addHorizontalRule(0, numberOfVariables+1, "=");
//
//        return table.toString();
//    }
//
//    public String printCorrelationTypes(){
//        TextTable table = new TextTable();
//        TextTableColumnFormat[] cformats = new TextTableColumnFormat[numberOfVariables+1];
//        cformats[0] = new TextTableColumnFormat();
//        cformats[0].setStringFormat(10, OutputAlignment.LEFT);
//        for(int i=0;i<numberOfVariables;i++){
//            cformats[i+1] = new TextTableColumnFormat();
//            cformats[i+1].setStringFormat(10, OutputAlignment.RIGHT);
//        }
//        table.addAllColumnFormats(cformats, numberOfVariables+5);
//        table.setAllCellPadding(2);
//
//        table.getRowAt(0).addHeader(0, numberOfVariables+1, "TYPE OF CORRELATION", TextTablePosition.LEFT);
//        table.getRowAt(1).addHorizontalRule(0, numberOfVariables+1, "=");
//
//        for(int i=0;i<numberOfVariables;i++){
//            table.getRowAt(2).addHeader(i+1, 1, variables.get(i).getName().toString(), TextTablePosition.RIGHT);
//        }
//        table.getRowAt(3).addHorizontalRule(0, numberOfVariables+1, "-");
//
//        for(int i=0;i<numberOfVariables;i++){
//            table.getRowAt(i+4).addStringAt(0, variables.get(i).getName().toString());
//            for(int j=0;j<numberOfVariables;j++){
//                if(j<i){
//                    table.getRowAt(i+4).addStringAt(j+1, corTypes[j][i].toString());
//                }else{
//                    table.getRowAt(i+4).addStringAt(j+1, corTypes[i][j].toString());
//                }
//            }
//        }
//        table.getRowAt(numberOfVariables+4).addHorizontalRule(0, numberOfVariables+1, "=");
//
//        return table.toString();
//    }
//
//    public String printPolychoricThresholds(){
//        PolychoricML ml = null;
//        PolychoricTwoStepOLD ts = null;
//        int nPolychoric = 0;
//        for(int i=0;i<corTypes.length;i++){
//            if(corTypes[i][i]==CorrelationType.POLYCHORIC) nPolychoric++;
//        }
//
//        double[][] t = new double[nPolychoric][];
//        double[][] se = new double[nPolychoric][];
//        String[] names = new String[nPolychoric];
//
//        Object o = null;
//        Max max = new Max();
//        int index=0;
//        int pIndex=0;
//        for(DefaultVariableAttributes v : variables){
//            o = matrix[index][index];
//            if(corTypes[index][index]==CorrelationType.POLYCHORIC){
//                if(maximumLikelihood){
//                    ml = (PolychoricML)o;
//                    max.increment(ml.getNumberOfValidRowThresholds());
//                    t[pIndex] = ml.getValidRowThresholds();
//                    se[pIndex] = ml.getValidRowThresholdStandardErrors();
//                }else{
//                    ts = (PolychoricTwoStepOLD)o;
//                    max.increment(ts.getNumberOfValidRowThresholds());
//                    t[pIndex] = ts.getValidRowThresholds();
//                }
//                names[pIndex] = v.getName().toString();
//                pIndex++;
//            }
//            index++;
//        }
//
//        TextTable table = new TextTable();
//        int nCol = (int)max.getResult();
//        if(maximumLikelihood) nCol *=2;
//
//        TextTableColumnFormat[] cformats = new TextTableColumnFormat[nCol+1];
//        cformats[0] = new TextTableColumnFormat();
//        cformats[0].setStringFormat(10, OutputAlignment.LEFT);
//        for(int i=0;i<nCol;i++){
//            cformats[i+1] = new TextTableColumnFormat();
//            cformats[i+1].setDoubleFormat(8, 4, OutputAlignment.RIGHT);
//        }
//        table.addAllColumnFormats(cformats, nPolychoric+5);
//        table.setAllCellPadding(2);
//
//        table.getRowAt(0).addHeader(0, nCol+1, "POLYCHORIC THRESHOLD", TextTablePosition.LEFT);
//        table.getRowAt(1).addHorizontalRule(0, nCol+1, "=");
//        table.getRowAt(2).addHeader(0,1, "Name", TextTablePosition.LEFT);
//        for(int i=0;i<(int)max.getResult();i++){
//            String tName = "Thr." + (i+1);
//            table.getRowAt(2).addHeader(i+1,1, tName, TextTablePosition.RIGHT);
//            if(maximumLikelihood){
//                String seName = "S.E." + (i+1);
//                table.getRowAt(2).addHeader(i+2,1, seName, TextTablePosition.RIGHT);
//            }
//        }
//        table.getRowAt(3).addHorizontalRule(0, nCol+1, "-");
//
//        for(int i=0;i<nPolychoric;i++){
//            table.getRowAt(i+4).addStringAt(0, names[i]);
//            for(int j=0;j<t[i].length;j++){
//                table.getRowAt(i+4).addDoubleAt(j+1, t[i][j]);
//                if(maximumLikelihood) table.getRowAt(i+4).addDoubleAt(j+2, se[j][j]);
//            }
//
//        }
//        table.getRowAt(nPolychoric+4).addHorizontalRule(0, nCol+1, "=");
//
//
//        return table.toString();
//    }

}

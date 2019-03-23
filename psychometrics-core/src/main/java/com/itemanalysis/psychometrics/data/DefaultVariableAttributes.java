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
package com.itemanalysis.psychometrics.data;

import com.itemanalysis.psychometrics.exceptions.ItemScoringException;

/**
 * This class holds descriptive information about the variables in the database.
 * It corresponds to entries in the Variable View of the graphical user interface.
 *
 * It also serves as a wrapper to the DefaultItemScoring object.
 *
 */
public class DefaultVariableAttributes implements VariableAttributes, Comparable<VariableAttributes> {

    /**
     * Variable name
     */
    private VariableName name = null;

    /**
     * Descriptive label for variable
     */
    private VariableLabel label = null;

    /**
     * Type of variable
     */
    private VariableType type = null;

    /**
     * This records the position of the variable in the database
     */
    private int order = -1;

    /**
     * String rho describing the corresponding itemGroup
     */
    private String itemGroup = "";

    /**
     * Position of item in test - set in DataKeyTableModel
     */
    private int testItemOrder = 0;

    /**
     * size of varchar for use with derby database table creation
     */
    private int varcharSize = 50;

    /**
     * Item scoring
     */
    private ItemScoring itemScoring = null;

    private SpecialDataCodes specialDataCodes = null;

    /**
     *
     *
     * @param name name of variable
     * @param label label for variable
     * @param itemType type of item (see VariableType class)
     * @param dataType type of data (see VariableType class)
     * @param order column position of teh variable in the database
     * @param itemGroup a code indicating the item group to which the item belongs
     */
    public DefaultVariableAttributes(String name, String label, ItemType itemType, DataType dataType, int order, String itemGroup){
        this.name = new VariableName(name.trim());
        this.label = new VariableLabel(label.trim());
        this.type = new VariableType(itemType, dataType);
        this.order = order;
        this.itemGroup = itemGroup;
        itemScoring = new DefaultItemScoring();
        specialDataCodes = new DefaultSpecialDataCodes();//Use default codes
    }

    public DefaultVariableAttributes(VariableName variableName, VariableLabel variableLabel, ItemType itemType, DataType dataType, int order, String itemGroup){
        this.name = variableName;
        this.label = variableLabel;
        this.type = new VariableType(itemType, dataType);
        this.order = order;
        this.itemGroup = itemGroup;
        specialDataCodes = new DefaultSpecialDataCodes();//Use default codes
    }

    public DefaultVariableAttributes(VariableName variableName, VariableLabel variableLabel, DataType dataType, int order){
        this.name = variableName;
        this.label = variableLabel;
        this.type = new VariableType(ItemType.NOT_ITEM, dataType);
        this.order = order;
        this.itemGroup = null;
        specialDataCodes = new DefaultSpecialDataCodes();//Use default codes
    }

    public DefaultVariableAttributes(VariableName variableName, int order){
        this.name = variableName;
        this.label = new VariableLabel("");
        this.type = new VariableType(ItemType.NOT_ITEM, DataType.INTEGER);
        this.order = order;
        this.itemGroup = null;
        specialDataCodes = new DefaultSpecialDataCodes();//Use default codes
    }

    public DefaultVariableAttributes(String variableName, int order){
        this.name = new VariableName(variableName);
        this.label = new VariableLabel("");
        this.type = new VariableType(ItemType.NOT_ITEM, DataType.INTEGER);
        this.order = order;
        this.itemGroup = null;
        specialDataCodes = new DefaultSpecialDataCodes();//Use default codes
    }

    /**
     *
     * @return name of variable
     */
    public VariableName getName(){
        return name;
    }

    public void setName(VariableName name){
        this.name = name;
    }

    public VariableLabel getLabel(){
        return label;
    }

    public void setLabel(String label){
        this.label = new VariableLabel(label);
    }

    public VariableType getType(){
        return type;
    }

    public DataType getDataType(){
        return type.getDataType();
    }

    public ItemType getItemType(){
        return type.getItemType();
    }

    public void setItemType(ItemType itemType){
        type.setItemType(itemType);
    }

    public void setDataType(DataType dataType){
        type.setDataType(dataType);
    }

    public boolean hasScoring(){
        return itemScoring!=null;
    }

    public void setItemScoring(ItemScoring itemScoring){
        if(this.name.equals(itemScoring.getName())){
            this.itemScoring = itemScoring;
            type.setItemType(itemScoring.getItemType());
        }
    }

    /**
     * Only implemente din version 5 or later
     * @param missingDataCodes
     */
    public void setMissingDataCodes(MissingDataCodes missingDataCodes){
        throw new UnsupportedOperationException();
    }

    /**
     * Mostly used for ordering variables according to their order in the database or result set
     * @return
     */
    public int positionInDb(){
        return order;
    }

    public String getItemGroup(){
        return itemGroup;
    }

    public void setSpecialDataCodes(SpecialDataCodes specialDataCodes){
        this.specialDataCodes = specialDataCodes;
    }

    public boolean isMissing(String response){
        return specialDataCodes.isMissing(response);
    }

    /**
     * First checks for missing data. If missing then the missing response is scored.
     * Otherwise, an item score is computed. IF the item scoring object it is assumed that the data are
     * already scored and are integers. It will parse the response as an integer and return it.
     *
     * If the response is a String and no scoring or special data codes are associated with it,
     * then a NumberFormatException will occur.
     *
     * @param response an item response.
     * @return a score for the response
     */
    public double computeItemScore(String response)throws ItemScoringException {
        if(specialDataCodes.isMissing(response)){
            return specialDataCodes.computeMissingScore(response);
        }else if(itemScoring==null){
            //Response is a String but has no item scoring or special data codes.
            //It cannot be parsed as Double and NumberFormatException will occur.
            return Double.parseDouble(response);
        }else{
            return itemScoring.computeItemScore(response);
        }
    }

    public double computeItemScore(double response)throws ItemScoringException{
        if(Double.NaN==response) return specialDataCodes.computeMissingScore(DefaultSpecialDataCodes.PERMANENT_MISSING_DATA_CODE);
        String s = Double.valueOf(response).toString();
        return computeItemScore(s);
    }

    public double computeItemScore(int response) throws ItemScoringException{
        String s = Integer.valueOf(response).toString();
        return computeItemScore(s);
    }

    public double getMaxItemScore(){
        if(itemScoring!=null) return itemScoring.maximumPossibleScore();
        return 0;
    }

    public String[] getAttributeArray(){
        String[] s = new String[6];
        s[0] = name.toString();
        s[1] = type.getDataType().toString();
        if(this.hasScoring()){
            s[2] = itemScoring.printOptionScoreKey();
        }else{
            s[2] = "";
        }
        s[3] = specialDataCodes.toString();
        s[4] = itemGroup;
        s[5] = label.toString();
        return s;
    }

    /**
     * This version of getItemGroup is needed for com.itemanalysis.psychometrics.rasch.JMLE.
     * Items that have no itemGroup are treated as their own itemGroup. Therefore, when
     * useItemNameWhenEmpty is true, the item name will be used as the itemGroup ID so that
     * each item has its own itemGroup.
     *
     * @param useItemNameWhenEmpty
     * @return
     */
    public String getSubscale(boolean useItemNameWhenEmpty){
        if(itemGroup.equals("") && useItemNameWhenEmpty) return name.toString();
        return itemGroup;
    }

    public void setItemGroup(String itemGroup){
        this.itemGroup = itemGroup;
    }

    public void setTestItemOrder(int testItemOrder){
		this.testItemOrder=testItemOrder;
	}

    public int getTestItemOrder(){
        return testItemOrder;
    }

    public void setVarcharSize(int varcharSize){
        this.varcharSize = varcharSize;
    }

    public int getVarcharSize(){
        return this.varcharSize;
    }

    public MissingDataCodes getMissingDataCodes(){
        return specialDataCodes;
    }

    /**
     * For use when creating database columns from this object
     *
     * @return
     */
    public String getDatabaseTypeString(){
        if(type.getDataType()==DataType.DOUBLE){
            return "DOUBLE";
        }else{
            return "VARCHAR(" + varcharSize + ")";
        }
    }

    /**
     * compare by item name.
     *
     * @param o another VariableInfo object
     * @return
     */
    public int compareTo(VariableAttributes o){
		return this.name.compareTo(o.getName());
	}

    @Override
    public boolean equals(Object o){
        return (o instanceof VariableAttributes) && (this.compareTo((VariableAttributes)o)==0);
    }

    @Override
    public int hashCode(){
		return name.hashCode();
	}

    @Override
	public String toString(){
		return name.toString();
	}

    public SpecialDataCodes getSpecialDataCodes(){
        return specialDataCodes;
    }

    public boolean isPresent(String response){
        return specialDataCodes.isPresent(response);
    }

    public String printAttributes(){
        StringBuilder sb = new StringBuilder();
        sb.append(name.toString()+",");
        sb.append(type.getDataType().toString() +",");
        if(this.hasScoring()){
            sb.append(itemScoring.toString() + ",");
        }else{
            sb.append(",");
        }
        sb.append(specialDataCodes.toString() + ",");
        sb.append(itemGroup + ",");
        sb.append(label);
        return sb.toString();
    }

//===============================================================================================
// THE FOLLOWING METHODS ARE A WRAPPER TO THE DefaultItemScoring object
//===============================================================================================
    public void addAllCategories(String optionScoreKey){
        ItemType added = itemScoring.addAllCategories(optionScoreKey, type.getDataType());
        type.setItemType(added);
    }

    public ItemScoring getItemScoring(){
        return itemScoring;
    }

    public double getMinimumPossibleItemScore(){
        return itemScoring.minimumPossibleScore();
    }

    public Double getMaximumPossibleItemScore(){
        return itemScoring.maximumPossibleScore();
    }

    public void clearCategory(){
        itemScoring.clearCategory();
    }

    public double[] scoreArray(){
        return itemScoring.scoreArray();
    }

    public String printOptionScoreKey(){
        if(this.type.getItemType()==ItemType.NOT_ITEM) return "";
        return itemScoring.printOptionScoreKey();
    }
//===============================================================================================
    
}

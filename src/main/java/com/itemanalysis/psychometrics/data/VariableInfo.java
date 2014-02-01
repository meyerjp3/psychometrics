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

import com.itemanalysis.psychometrics.measurement.DefaultItemScoring;

/**
 * This class holds descriptive information about the variables in the database.
 * It corresponds to entries in the Variable View of the graphical user interface.
 *
 * It also serves as a wrapper to the DefaultItemScoring object.
 *
 */
public class VariableInfo implements Comparable<VariableInfo> {

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
    private int positionInDb = -1;

    /**
     * String rho describing the corresponding subscale
     */
    private String subscale = null;

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
    private DefaultItemScoring itemScoring = null;

    /**
     *
     *
     * @param name name of variable
     * @param label label for variable
     * @param itemType type of item (see VariableType class)
     * @param dataType type of data (see VariableType class)
     * @param positionInDb column position of teh variable in the database
     * @param subscale a code indicating the item group to which the item belongs
     */
    public VariableInfo(String name, String label, int itemType, int dataType, int positionInDb, String subscale){
        this.name = new VariableName(name.trim());
        this.label = new VariableLabel(label.trim());
        this.type = new VariableType(itemType, dataType);
        this.positionInDb = positionInDb;
        this.subscale = subscale;
        itemScoring = new DefaultItemScoring();
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

    /**
     * Mostly used for ordering variables according to their order in the database or result set
     * @return
     */
    public int positionInDb(){
        return positionInDb;
    }

    public String getSubscale(){
        if(subscale.equals("")) return "";
        return subscale;
    }

    /**
     * This version of getSubscale is needed for com.itemanalysis.psychometrics.rasch.JMLE.
     * Items that have no subscale are treated as their own subscale. Therefore, when
     * useItemNameWhenEmpty is true, the item name will be used as the subscale ID so that
     * each item has its own subscale.
     *
     * @param useItemNameWhenEmpty
     * @return
     */
    public String getSubscale(boolean useItemNameWhenEmpty){
        if(subscale.equals("") && useItemNameWhenEmpty) return name.toString();
        return subscale;
    }

    public void setSubscale(String subscale){
        this.subscale = subscale;
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

    /**
     * For use when creating database columns from this object
     *
     * @return
     */
    public String getDatabaseTypeString(){
        if(type.getDataType()==VariableType.DOUBLE){
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
    public int compareTo(VariableInfo o){
		return this.name.compareTo(o.name);
	}

    @Override
    public boolean equals(Object o){
        return (o instanceof VariableInfo) && (this.compareTo((VariableInfo)o)==0);
    }

    @Override
    public int hashCode(){
		return name.hashCode();
	}

    @Override
	public String toString(){
		return name.toString();
	}

//===============================================================================================
// THE FOLLOWING METHODS ARE A WRAPPER TO THE DefaultItemScoring object
//===============================================================================================
    public void addAllCategories(String optionScoreKey){
        int added = itemScoring.addAllCategories(optionScoreKey, type);
        type.setItemType(added);
    }

    public DefaultItemScoring getItemScoring(){
        return itemScoring;
    }

    public void setOmitCode(Object omitCode){
        itemScoring.setOmitCode(omitCode, type);
    }

    public void setNotReachedCode(Object notReachedCode){
        itemScoring.setNotReachedCode(notReachedCode, type);
    }

    public void clearSpecialDataCodes(){
        itemScoring.clearOmittedAndNotReachedCodes();
    }

    public Object getOmitCode(){
        return itemScoring.getOmitCode();
    }

    public Object getNotReachedCode(){
        return itemScoring.getNotReachedCode();
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

    public Double[] scoreArray(){
        return itemScoring.scoreArray();
    }

    public String printOptionScoreKey(){
        if(this.type.getItemType()==VariableType.NOT_ITEM) return "";
        return itemScoring.printOptionScoreKey();
    }
//===============================================================================================
    
}

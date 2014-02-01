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

/**
 *
 * The methods countDouble, countString, countMissing, getTotalCount,
 * getValidCount, and isDouble are needed for importing data and
 * management of the JTable. The JTable needs to know the class
 * for a column data. Missing responses can cause a problem and
 * possibly appear as a mix of Double and String. These methods
 * are used to avoid this problem and determine whether a column
 * is of class Double (i.e. isDouble) or String (i.e. not isDouble).
 *
 *
 * @author J. Patrick Meyer
 */
public class VariableType {

    public final static int NO_FILTER=-1;		//no filter
        
    public final static int NOT_ITEM=1;			//variable that is not a test item
    public final static int BINARY_ITEM=2; 		//binary test item
    public final static int POLYTOMOUS_ITEM=3; 	//polytomous test item
    public final static int DOUBLE=4;			//double data type
    public final static int STRING=5;           //string data type
    public final static int CONTINUOUS_ITEM=6;  //continuous item - scoring not needed

    public final static String NOT_ITEM_STRING="Not Item";			//variable that is not a test item
    public final static String BINARY_ITEM_STRING="Binary Item"; 		//binary test item
    public final static String POLYTOMOUS_ITEM_STRING="Polytomous Item"; 	//polytomous test item
    public final static String DOUBLE_STRING="Double";			//double data type
    public final static String STRING_STRING="String";           //string data type
    public final static String CONTINUOUS_ITEM_STRING="Continuous Item";  //continuous item - scoring not needed
    
    private int itemType = NOT_ITEM;
    
    private int dataType = DOUBLE;

    /**
     * Constructor needed for importing data files. Uses default types.
     */
    public VariableType(){

    }
    
    public VariableType(int itemType, int dataType){
        this.itemType = itemType;
        this.dataType = dataType;
    }

    public int getItemType(){
        return itemType;
    }

    public String getItemTypeString(){
        switch(this.itemType){
            case NOT_ITEM: return "Not Item";
            case BINARY_ITEM: return "Binary Item";
            case POLYTOMOUS_ITEM: return "Polytomous Item";
            case CONTINUOUS_ITEM: return "Continuous Item";
        }
        return "Not Item";
    }

    public void setItemType(int itemType){
        this.itemType = itemType;

        //continuous item type must be double data type
        if(CONTINUOUS_ITEM==itemType){
            if(DOUBLE==dataType){
                this.itemType = itemType;
            }else{
                this.itemType = NOT_ITEM;
            }
        }
    }
    
    public void setItemType(String itemType){
        if(NOT_ITEM_STRING.equals(itemType)){
            setItemType(NOT_ITEM);
        }else if(BINARY_ITEM_STRING.equals(itemType)){
            setItemType(BINARY_ITEM);
        }else if(POLYTOMOUS_ITEM_STRING.equals(itemType)){
            setItemType(POLYTOMOUS_ITEM);
        }else if(CONTINUOUS_ITEM_STRING.equals(itemType)){
            setItemType(CONTINUOUS_ITEM);
        }
    }

    public int getDataType(){
        return dataType;
    }
    
    public String getDataTypeString(){
        switch(this.dataType){
            case DOUBLE: return "Double";
            case STRING: return "Text";
        }
        return "Double";
    }

    public void setDataType(int dataType){
        this.dataType = dataType;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof VariableType)) return false;
		if(o==this) return true;
		VariableType t = (VariableType)o;
        boolean result = (t.getItemType()==this.getItemType() && t.getDataType()==this.getDataType());
        return result;
    }

    @Override
    public int hashCode(){
		return toString().hashCode();
	}

    @Override
	public String toString(){
		return getItemTypeString() + ", " + getDataTypeString();
	}

}

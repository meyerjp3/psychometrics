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

//NOTE: The static values below are not part of DataType and ItemType Enums.
//    public final static int NO_FILTER=-1;		//no filter
//
//    public final static int NOT_ITEM=1;			//variable that is not a test item
//    public final static int BINARY_ITEM=2; 		//binary test item
//    public final static int POLYTOMOUS_ITEM=3; 	//polytomous test item
//    public final static int DOUBLE=4;			//double data type
//    public final static int STRING=5;           //string data type
//    public final static int CONTINUOUS_ITEM=6;  //continuous item - scoring not needed
//    public final static int INTEGER = 7;
//
//    public final static String NOT_ITEM_STRING="Not Item";			//variable that is not a test item
//    public final static String BINARY_ITEM_STRING="Binary Item"; 		//binary test item
//    public final static String POLYTOMOUS_ITEM_STRING="Polytomous Item"; 	//polytomous test item
//    public final static String DOUBLE_STRING="Double";			//double data type
//    public final static String STRING_STRING="String";           //string data type
//    public final static String CONTINUOUS_ITEM_STRING="Continuous Item";  //continuous item - scoring not needed
//
//    private int itemType = NOT_ITEM;
//
//    private int dataType = DOUBLE;

    private ItemType itemType = ItemType.NOT_ITEM;
    private DataType dataType = DataType.INTEGER;
    
    public VariableType(ItemType itemType, DataType dataType){
        this.itemType = itemType;
        this.dataType = dataType;
    }

    /**
     * In jMetrik, item types are stored in database as integers. This constructor covnerts the integers to their Enums.
     * @param itemType
     * @param dataType
     */
    public VariableType(int itemType, int dataType){
        if(1==itemType){
            this.itemType = ItemType.NOT_ITEM;
        }else if(2==itemType){
            this.itemType = ItemType.BINARY_ITEM;
        }else if(3==itemType){
            this.itemType = ItemType.POLYTOMOUS_ITEM;
        }else if(6==itemType){
            this.itemType = ItemType.CONTINUOUS_ITEM;
        }

        if(4==dataType){
            this.dataType = DataType.DOUBLE;
        }else if(5==dataType){
            this.dataType = DataType.STRING;
        }

    }

    public VariableType(DataType dataType){
        this.dataType = dataType;
    }

    public ItemType getItemType(){
        return itemType;
    }

    public String getItemTypeString(){
        switch(this.itemType){
            case NOT_ITEM: return ItemType.NOT_ITEM.toString();
            case BINARY_ITEM: return ItemType.BINARY_ITEM.toString();
            case POLYTOMOUS_ITEM: return ItemType.POLYTOMOUS_ITEM.toString();
        }
        return ItemType.NOT_ITEM.toString();
    }

    public void setItemType(ItemType itemType){
        this.itemType = itemType;

//        //continuous item type must be double data type
//        if(CONTINUOUS_ITEM==itemType){
//            if(DOUBLE==dataType){
//                this.itemType = itemType;
//            }else{
//                this.itemType = NOT_ITEM;
//            }
//        }
    }
    
    public void setItemType(String itemType){
        if(ItemType.NOT_ITEM.toString().equals(itemType)){
            setItemType(ItemType.NOT_ITEM);
        }else if(ItemType.BINARY_ITEM.toString().equals(itemType)){
            setItemType(ItemType.BINARY_ITEM);
        }else if(ItemType.POLYTOMOUS_ITEM.toString().equals(itemType)){
            setItemType(ItemType.POLYTOMOUS_ITEM);
        }
        setItemType(ItemType.NOT_ITEM);
    }

    public DataType getDataType(){
        return dataType;
    }
    
    public String getDataTypeString(){
        switch(this.dataType){
            case DOUBLE: return "Double";
            case STRING: return "Text";
        }
        return "Double";
    }

    public void setDataType(DataType dataType){
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

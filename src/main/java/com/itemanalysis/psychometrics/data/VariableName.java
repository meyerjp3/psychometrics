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
 * This class represents a variable name. It allows you to easily switch between the display name
 * and teh name needed by a database. The database name adds a prefix and suffix of "x" to the
 * variable name. This convention prevents confusion with reserved SQL words when interacting
 * with a database.
 */
public class VariableName implements Comparable<VariableName>{

    /**
     * The display name of the variable
     */
    private String variableName = "";

    /**
     * The original name of the variable before special characters and white spaces have been removed
     */
	private String originalVariableName = "";
	private static int NAME_LENGTH = 50; //limit variable names to 20 characters
    private int index = 0;//used for VariableNameListModel

	public VariableName(String variableName){
        if(variableName.toLowerCase().startsWith("x") && variableName.toLowerCase().endsWith("x")){
            String temp = variableName.substring(1,variableName.length()-1);
            int length=Math.min(temp.length(), NAME_LENGTH);
            this.originalVariableName=temp.substring(0,length);

            String temp2 = checkVariableName(temp);
            length=Math.min(temp2.length(), NAME_LENGTH);
            this.variableName = temp2.substring(0,length);
        }

        else{
            int length=Math.min(variableName.length(), NAME_LENGTH);
            this.originalVariableName=variableName.substring(0,length);
            String temp = checkVariableName(variableName);
            length=Math.min(temp.length(), NAME_LENGTH);
            this.variableName=temp.substring(0,length);
        }

//        //String lcVarName = variableName.trim().toLowerCase(); removed Sept 29, 2018
//
//		if(lcVarName.toLowerCase().startsWith("x") && lcVarName.toLowerCase().endsWith("x")){
//			String temp = lcVarName.substring(1,lcVarName.length()-1);
//			int length=Math.min(temp.length(), NAME_LENGTH);
//			this.originalVariableName=temp.substring(0,length);
//
//            String temp2 = checkVariableName(temp);
//            length=Math.min(temp2.length(), NAME_LENGTH);
//            this.variableName = temp2.substring(0,length);
//        }
//
//        else{
//			int length=Math.min(lcVarName.length(), NAME_LENGTH);
//			this.originalVariableName=lcVarName.substring(0,length);
//            String temp = checkVariableName(lcVarName);
//            length=Math.min(temp.length(), NAME_LENGTH);
//			this.variableName=temp.substring(0,length);
//		}

	}

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

	private String checkVariableName(String variableName){
        String a = variableName.replaceAll("\\s+", "");
        String newVariableName = a.trim().replaceAll("(?:(?!_)\\p{Punct})+", "");//remove special characters except underscore
		return newVariableName;
	}

    /**
     * Returns the variable name after stripping special characters and spaces from teh name but without the
     * "x" prefix and suffix. It is teh display name of teh variable.
     *
     * @return name of variable
     */
    @Override
	public String toString(){
		return variableName;
	}

    @Deprecated
    public String getText(){
        return variableName;
    }

    /**
     * Leading and trailing "x" added for database security and prevent use of reserved words
     *
     * @return the name of teh variable used by calls to the database.
     */
	public String nameForDatabase(){
		return "x" + variableName + "x";
	}

    /**
     * A particular format of the name often used for databases.
     *
     * @return
     */
    public String quotedNameForDatabase(){
		return "'x" + variableName + "x'";
	}

    public String quotedName(){
		return "'" + variableName + "'";
	}

	public String getOriginalVariableName(){
		return originalVariableName;
	}

    public boolean nameChanged(){
        return !originalVariableName.equals(variableName);
    }

	public String printNameChangeInformation(){
        return "Variable name: " + originalVariableName + " changed to: " + variableName;
	}

    @Override
    public boolean equals(Object o){
        return (o instanceof VariableName) && (this.compareTo((VariableName)o)==0);
    }

    @Override
    public int hashCode(){
		return variableName.hashCode();
	}

    public int compareTo(VariableName o){
        return this.variableName.compareToIgnoreCase(o.variableName);//Case insensitive

	}
    
}

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
 */
public class VariableName implements Comparable<VariableName>{

    private String variableName = "";
	private String originalVariableName = "";
	private static int NAME_LENGTH = 20; //limit variable names to 20 characters
    private int index = 0;//used for VariableNameListModel

	public VariableName(String variableName){
        String lcVarName = variableName.trim().toLowerCase();
//        String lcVarName = variableName.trim();//old way
		if(lcVarName.startsWith("x") && lcVarName.endsWith("x")){
			String temp = lcVarName.substring(1,lcVarName.length()-1);
			int length=Math.min(temp.length(), NAME_LENGTH);
			this.originalVariableName=temp.substring(0,length);

            String temp2 = checkVariableName(temp);
            length=Math.min(temp2.length(), NAME_LENGTH);
            this.variableName = temp2.substring(0,length);


		}else{
			int length=Math.min(lcVarName.length(), NAME_LENGTH);
			this.originalVariableName=lcVarName.substring(0,length);
            String temp = checkVariableName(lcVarName);
            length=Math.min(temp.length(), NAME_LENGTH);
			this.variableName=temp.substring(0,length);
		}

	}

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

	private String checkVariableName(String variableName){
        String a = variableName.replaceAll("\\s+", "");
//        String newVariableName = a.trim().replaceAll("\\p{Punct}+", "");//removes all special characters
        String newVariableName = a.trim().replaceAll("(?:(?!_)\\p{Punct})+", "");//remove special characters except underscore
		return newVariableName;
	}

    @Override
	public String toString(){
		return variableName;
	}

    public String getText(){
        return variableName;
    }

	//leading and trainling X added for database security and prevent use of reserved words
	public String nameForDatabase(){
		return "x" + variableName + "x";
	}

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
//        if(!(o instanceof VariableName)) return false;
//		if(o==this) return true;
//		VariableName that = (VariableName)o;
//        return this.variableName.equals(that.variableName);
    }

    @Override
    public int hashCode(){
		return variableName.hashCode();
	}

    public int compareTo(VariableName o){
//        if(!(o instanceof VariableName))
//            throw new ClassCastException("VariableName object expected");
//        VariableName that = (VariableName)o;

        return this.variableName.compareTo(o.variableName);

	}
    
}

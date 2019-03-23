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
 * This class represent a variable label. The length of the label is the only restriction.
 *
 * @author J. Patrick Meyer
 */
public class VariableLabel implements Comparable<Object>{

    private int length = 125;

    private String label = "";

    public VariableLabel(String label, int length){
        this.label = label;
        this.length = length;
    }

    public VariableLabel(String label){
        this.label = label;
    }

    public String getText(){
        return label.substring(0, 50);
    }

    public void setLength(int length){
        this.length = length;
    }

    public int compareTo(Object o){
        if(!(o instanceof VariableLabel))
            throw new ClassCastException("VariableLabel object expected");
        VariableLabel that = (VariableLabel)o;
		return this.label.compareTo(that.label);
	}

    @Override
    public boolean equals(Object o){
        if(!(o instanceof VariableLabel)) return false;
		if(o==this) return true;
		VariableLabel that = (VariableLabel)o;
        return this.label.equals(that.label);
    }

    @Override
    public int hashCode(){
		return label.hashCode();
	}

    @Override
	public String toString(){
		return label.toString();
	}

}

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

import java.util.Formatter;

/**
 * CategoryScoring defines the scores assigned to values in the data. For example,
 * a score of 1 may be assigned for data values of A, and B. Multiple CategoryScoring
 * comprise a VariableScoring. That is, a VariableScoring defines scores for
 * all possible responses.
 *
 *
 * @author J. Patrick Meyer
 */
public class Category implements Comparable<Object>{

    /**
     * Numeric score assigned to category. This rho is forced to be positive.
     */
    private double scoreValue = 0.0;

    protected Object responseValue = null;

    public Category(Object originalValue, double scoreValue){
        this.responseValue = originalValue;
        this.scoreValue = scoreValue;
    }

    public double scoreValue(){
        return scoreValue;
    }

    public Object responseValue(){
        return responseValue;
    }

//    public boolean isInCategory(Object response){
//        return responseValue.equals(response);
//    }
//
//    public double categoryScore(Object response){
//        if(responseValue.equals(response)) return 1.0;
//        return 0.0;
//    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%4s", responseValue.toString()); f.format("%2s", " "); f.format("%4s", scoreValue);
        return f.toString();
    }

    public int compareTo(Object o){
        if(!(o instanceof Category))
            throw new ClassCastException("Category object expected");
        Category that = (Category)o;
//        if(this.originalValue instanceof Double){
//            return ((Double)this.originalValue).compareTo((Double)that.originalValue);
//        }
        
        return this.responseValue.toString().compareTo(that.responseValue.toString());
		
	}

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Category)) return false;
		if(o==this) return true;
		Category that = (Category)o;
        return this.responseValue.equals(that.responseValue);
    }

    @Override
    public int hashCode(){
		return responseValue.hashCode();
	}



}

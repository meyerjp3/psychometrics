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
package com.itemanalysis.psychometrics.texttable;

public class TextTableColumnFormat {

    private int size = 0;

    private int precision = -1;

    private String format = "";

    public enum OutputAlignment{
        LEFT, RIGHT
    }

    public TextTableColumnFormat(){
       
    }

    public void setDoubleFormat(int size, int precision, OutputAlignment alignment){
        this.size = size;
        this.precision = precision;

        if(precision>size){
            format += "% " + size + "." + (size-1) + "f";
        }else{
            format += "% " + size + "." + precision + "f";
        }
    }

    public void setIntFormat(int size, OutputAlignment alignment){
        if(alignment==OutputAlignment.LEFT){
            format += "%-" + size + "d";
        }else{
            format += "%" + size + "d";
        }
        this.size = size;
    }

    public void setStringFormat(int size, OutputAlignment alignment){
        if(alignment==OutputAlignment.LEFT){
            format += "%-" + size + "s";
        }else{
            format += "%" + size + "s";
        }
        this.size = size;
    }

    public String getFormat(){
        return format;
    }

    public int getSize(){
        return size;
    }

    public int getPrecision(){
        return precision;
    }

}

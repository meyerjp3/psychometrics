package com.itemanalysis.psychometrics.data;

/**
 * Copyright 2012 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public enum DataType {

    DOUBLE{
        @Override
        public String toString(){
            return "DOUBLE";
        }
    },

    STRING {
        @Override
        public String toString(){
            return "STRING";
        }
    },

    INTEGER {
        @Override
        public String toString(){
            return "INTEGER";
        }
    },

    NO_DATATYPE_FILTER{
        @Override
        public String toString(){
            return "No Data Type Filter";
        }
    };



    public static int toInt(DataType dataType){
        if(dataType==DOUBLE) return 4;
        if(dataType==STRING) return 5;
        if(dataType==INTEGER) return 7;
        if(dataType==NO_DATATYPE_FILTER) return -1;
        return 4;
    }

}

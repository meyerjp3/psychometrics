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
public enum ItemType {

    NOT_ITEM{
        @Override
        public String toString(){
            return "Not Item";
        }

        public int toInt(){
            return 1;
        }

    },

    BINARY_ITEM{
        @Override
        public String toString(){
            return "Binary Item";
        }

        public int toInt(){
            return 2;
        }

    },

    POLYTOMOUS_ITEM{
        @Override
        public String toString(){
            return "Polytomous Item";
        }

        public int toInt(){
            return 3;
        }

    },

    //TODO depreciate this type
    CONTINUOUS_ITEM{
        @Override
        public String toString(){
            return "Continuous Item";
        }

        public int toInt(){
            return 6;
        }

    },

    NO_ITEMTYPE_FILTER{
        @Override
        public String toString(){
            return "No Item Type Filter";
        }

        public int toInt(){
            return -1;
        }

    };

    public static int toInt(ItemType itemType){
        if(itemType==NOT_ITEM) return 1;
        if(itemType==BINARY_ITEM) return 2;
        if(itemType==POLYTOMOUS_ITEM) return 3;
        if(itemType==CONTINUOUS_ITEM) return 6;
        if(itemType==NO_ITEMTYPE_FILTER) return -1;
        return 1;
    }



}

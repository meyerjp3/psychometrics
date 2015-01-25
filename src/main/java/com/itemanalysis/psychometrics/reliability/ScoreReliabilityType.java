package com.itemanalysis.psychometrics.reliability;

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
public enum ScoreReliabilityType {

    GUTTMAN_LAMBDA{
        @Override
        public String toString(){
            return "LAMBDA";
        }
    },

    CRONBACH_ALPHA{
        @Override
        public String toString(){
            return "ALPHA";
        }
    },

    FELDT_GILMER{
        @Override
        public String toString(){
            return "FG";
        }
    },

    FELDT_CLASSICAL_CONGENERIC{
        @Override
        public String toString(){
            return "FB";
        }
    },

    RAJU_BETA{
        @Override
        public String toString(){
            return "RAJU";
        }
    },

    STRATIFIED_ALPHA{
        @Override
        public String toString(){
            return "STRAT";
        }
    },

    KR21{
        @Override
        public String toString(){
            return "KR21";
        }
    }

}

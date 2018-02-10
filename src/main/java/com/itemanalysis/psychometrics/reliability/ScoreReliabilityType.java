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

    GUTTMAN_LAMBDA1 {
        @Override
        public String toString(){
            return "Guttman's L1";
        }
    },

    GUTTMAN_LAMBDA2 {
        @Override
        public String toString(){
            return "Guttman's L2";
        }
    },

    GUTTMAN_LAMBDA3 {
        @Override
        public String toString(){
            return "Guttman's L3";
        }
    },

    GUTTMAN_LAMBDA4 {
        @Override
        public String toString(){
            return "Guttman's L4";
        }
    },

    GUTTMAN_LAMBDA5 {
        @Override
        public String toString(){
            return "Guttman's L5";
        }
    },

    GUTTMAN_LAMBDA6 {
        @Override
        public String toString(){
            return "Guttman's L6";
        }
    },

    CRONBACH_ALPHA{
        @Override
        public String toString(){
            return "Coefficient Alpha";
        }
    },

    FELDT_GILMER{
        @Override
        public String toString(){
            return "Feldt-Gilmer";
        }
    },

    FELDT_CLASSICAL_CONGENERIC{
        @Override
        public String toString(){
            return "Feldt-Brennan";
        }
    },

    RAJU_BETA{
        @Override
        public String toString(){
            return "Raju Beta";
        }
    },

    STRATIFIED_ALPHA{
        @Override
        public String toString(){
            return "Stratified Alpha";
        }
    },

    KR21{
        @Override
        public String toString(){
            return "KR21";
        }
    }

}

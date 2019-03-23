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
package com.itemanalysis.psychometrics.irt.model;

/**
 *
 */
public enum IrmType {

    //to indicate use of the object Irm3PL
    L3{
        public String toString(){
            return "L3";
        }

    },

    //to indicate use of Irm4PL
    L4{
        public String toString(){
            return "L4";
        }
    },

    //to indicate use of the object IrmGPCM (step parameterization of GPCM)
    //Corresponds to IrmGPCM.class
    GPCM{
        public String toString(){
            return "GPCM";
        }
    },

    //to indicate use of the object IrmGPCM2 (difficulty plus threshold  parameterization of GPCM as in PARSCALE)
    GPCM2{
        public String toString(){
            return "GPCM2";
        }
    },

    //to indicate use of the object IrmPCM (difficulty plus threshold parameterization of PCM)
    //Corresponds to IrmPCM.class
    //Corresponds to text code
    PCM{
        public String toString(){
            return "PCM";
        }
    },

    //to indicate use of the object IrmPCM (step parameterization of PCM)
    //Corresponds to IrmPCM2.class
    PCM2{
        public String toString(){
            return "PCM2";
        }
    },

    //to indicate use of the object IrmGRM
    GRM{
        public String toString(){
            return "GRM";
        }
    }

}

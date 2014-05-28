/**
 * Copyright 2014 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.factoranalysis;

public enum RotationMethod {

    //====================================================================
    //Orthogonal rotations
    //====================================================================
    NONE{
        public String toString(){
            return "No rotation";
        }
    },

    VARIMAX{
        public String toString(){
            return "Varimax rotation";
        }
    },

    QUARTIIMAX{
        public String toString(){
            return "Quartimax rotation";
        }
    },

    BENTLER_T{
        public String toString(){
            return "Bentler orthogonal rotation";
        }
    },

    GEOMIN_T{
        public String toString(){
            return "Geomin orthogonal rotation";
        }
    },

    BIFACTOR{
        public String toString(){
            return "Bifactor rotation";
        }
    },

    //====================================================================
    //Oblique rotations
    //====================================================================
    OBLIMIN{
        public String toString(){
            return "Oblimin rotation";
        }
    },

    GEOMIN_Q{
        public String toString(){
            return "Geomin oblique rotation";
        }
    },

    QUARTIMIN{
        public String toString(){
            return "Quartimin rotation";
        }
    },

    BIQUARTIMIN{
        public String toString(){
            return "Biquartimin rotation";
        }
    },

    SIMPLIMAX{
        public String toString(){
            return "Simplimax rotation";
        }
    },

    BENTLER_Q{
        public String toString(){
            return "Bentler oblique rotation";
        }
    },

    CLUSTER{
        public String toString(){
            return "Cluster rotation";
        }
    },



}

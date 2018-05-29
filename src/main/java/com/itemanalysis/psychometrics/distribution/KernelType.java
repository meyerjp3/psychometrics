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
package com.itemanalysis.psychometrics.distribution;

public enum KernelType {

    GAUSSIAN{
        public String toString(){
            return "Gaussian";
        }
    },
    EPANECHNIKOV{
        public String toString(){
            return "Epanechnikov";
        }
    },
    RECTANGULAR{
        public String toString(){
            return "Rectangular";
        }
    },
    UNIFORM{
        public String toString(){
            return "Uniform";
        }
    },
    TRIANGULAR{
        public String toString(){
            return "Triangular";
        }
    },
    BIWEIGHT{
        public String toString(){
            return "Biweight";
        }
    },
    TRIWEIGHT{
        public String toString(){
            return "Triweight";
        }
    },
    COSINE{
        public String toString(){
            return "Cosine";
        }
    },
    OPTCOSINE{
        public String toString(){
            return "Optcosine";
        }
    }

}

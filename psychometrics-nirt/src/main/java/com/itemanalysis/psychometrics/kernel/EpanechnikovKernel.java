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
package com.itemanalysis.psychometrics.kernel;

public class EpanechnikovKernel implements KernelFunction{

    public EpanechnikovKernel(){

    }

    public double value(double u){
        double k=0.0;
		if(Math.abs(u)<=1.0){
            k=(3.0/4.0)*(1.0-Math.pow(u,2));
        }
		return k;
    }

}

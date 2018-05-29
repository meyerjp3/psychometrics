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

import com.itemanalysis.psychometrics.distribution.KernelType;

/**
 * A factory class for returning either the kernel class or the name of the type of kernel
 */
@Deprecated
public class KernelFactory {

    private KernelType type = KernelType.GAUSSIAN;

    public KernelFactory(KernelType type){
        this.type = type;
    }

    public KernelFactory(String type){
        String checkType = type.trim().toLowerCase();
        if("epanechnikov".equals(checkType)){
            this.type = KernelType.EPANECHNIKOV;
        }else if("uniform".equals(checkType)){
            this.type = KernelType.RECTANGULAR;
        }else if("rectangular".equals(checkType)){
            this.type = KernelType.RECTANGULAR;
        }else if("triangle".equals(checkType)){
            this.type = KernelType.TRIANGULAR;
        }else if("biweight".equals(checkType)){
            this.type = KernelType.BIWEIGHT;
        }else if("cosine".equals(checkType)){
            this.type = KernelType.COSINE;
        }else{
            this.type = KernelType.GAUSSIAN;
        }
    }

    public KernelFunction getKernelFunction(){
        switch(type){
            case BIWEIGHT: return new BiweightKernel();
            case COSINE: return new CosineKernel();
            case EPANECHNIKOV: return new EpanechnikovKernel();
            case TRIANGULAR: return new TriangularKernel();
            case RECTANGULAR: return new UniformKernel();
        }
        return new GaussianKernel();//default
    }

    public String toString(){
        switch(type){
            case BIWEIGHT: return "Biweight Kernel";
            case COSINE: return "Cosine Kernel";
            case EPANECHNIKOV: return "Epanechnikov Kernel";
            case TRIANGULAR: return "Triangle Kernel";
            case RECTANGULAR: return "Uniform Kernel";
        }
        return "Gaussian Kernel";
    }


}

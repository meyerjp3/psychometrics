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

/**
 * Bandwidth for nonparametric item characteristic curves via KernelRegression.
 */
public class NonparametricIccBandwidth implements Bandwidth {

    private double sampleSize = 0;

    private double adjustmentFactor = 1.0;

    public NonparametricIccBandwidth(double sampleSize, double adjustmentFactor){
        this.sampleSize = sampleSize;
        this.adjustmentFactor = adjustmentFactor;
    }

    public double getAdjustmentFactor(){
        return adjustmentFactor;
    }

    public double value(){
        return  1.1*Math.pow(sampleSize, -1.0/5.0)*adjustmentFactor;
    }

}

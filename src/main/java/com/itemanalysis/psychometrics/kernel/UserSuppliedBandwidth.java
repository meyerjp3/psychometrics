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
 * This class is for processing a user supplied bandwidth. In other cases the
 * bandwith is computed but in some applications a user provides teh bandwith directly.
 */
public final class UserSuppliedBandwidth implements Bandwidth{

    private double h = 1.0;

    private double adjustmentFactor = 1.0;

    public UserSuppliedBandwidth(double h){
        this.h = h;
    }

    public UserSuppliedBandwidth(double h, double adjustmentFactor){
        this.h = h;
        this.adjustmentFactor = adjustmentFactor;
    }

    public double value(){
        return h;
    }

    public void setValue(double h){
        this.h=h;
    }

    public double getAdjustmentFactor(){
        return adjustmentFactor;
    }

}

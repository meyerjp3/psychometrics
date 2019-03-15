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
package com.itemanalysis.psychometrics.polycor;

import com.itemanalysis.psychometrics.statistics.PearsonCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class PolyserialTwoStep {
    
    private Mean meanX = null;
    
    private StandardDeviation sdX = null;
    
    private double[] dataX = null;
    
    private double[] freqDataY = null;
    
    private PearsonCorrelation rxy = null;

    public PolyserialTwoStep(Mean meanX, StandardDeviation sdX, double[] dataX, double[] freqDataY, PearsonCorrelation rxy){
        this.meanX = meanX;
        this.sdX = sdX;
        this.dataX = dataX;
        this.freqDataY = freqDataY;
        this.rxy = rxy;
    }



}

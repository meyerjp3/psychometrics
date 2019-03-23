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


import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * See Silverman (1986, p. 45).
 *
 * Silverman, B. W. (1986). Density estimation for statistics and data analysis. Boca Raton, FL: Chapman Hall.
 *
 */
public final class SimplePluginBandwidth implements Bandwidth{

    private StandardDeviation sd = null;

    private double adjustmentFactor = 1.0;

    public SimplePluginBandwidth(StandardDeviation sd){
        this(sd, 1.0);
    }

    public SimplePluginBandwidth(StandardDeviation sd, double adjustmentFactor){
        this.sd = sd;
        this.adjustmentFactor = adjustmentFactor;
    }

    public double value(){
        return 1.06*sd.getResult()*Math.pow(sd.getN(),-0.2)*adjustmentFactor;
    }

    public double getAdjustmentFactor(){
        return adjustmentFactor;
    }

}

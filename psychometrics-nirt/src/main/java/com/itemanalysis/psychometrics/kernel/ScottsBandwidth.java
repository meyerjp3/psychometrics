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
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 * See Silverman (1986, p. 45).
 *
 * Silverman, B. W. (1986). Density estimation for statistics and data analysis. Boca Raton, FL: Chapman Hall.
 *
 */
public final class ScottsBandwidth implements Bandwidth {

    private double[] x = null;

    private double adjustmentFactor = 1.0;

    private Percentile pcntl = null;

    public ScottsBandwidth(double[] x){
        this(x, 1.0);
    }

    public ScottsBandwidth(double[] x, double adjustmentFactor){
        this.x = x;
        this.adjustmentFactor = adjustmentFactor;
        pcntl = new Percentile();
    }

    public double value(){
        StandardDeviation sd = new StandardDeviation();
        double q3 = pcntl.evaluate(x, 75.0);
        double q1 = pcntl.evaluate(x, 25.0);
        double IQR = (q3-q1)/1.34;
        double s = sd.evaluate(x);
        double N = (double)x.length;
        double m = Math.min(s, IQR);
        return 1.06*m*Math.pow(N, -1.0/5.0)*adjustmentFactor;
    }

    public double getAdjustmentFactor(){
        return adjustmentFactor;
    }

}

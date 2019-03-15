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
package com.itemanalysis.psychometrics.statistics;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class Bootstrap {

    private Percentile percentile = null;

    private double lower = 2.5;

    private double upper = 97.5;

    private StandardDeviation stdDev = null;

    public Bootstrap(double lower, double upper){
        this.lower = lower;
        this.upper = upper;
        percentile = new Percentile();
        stdDev = new StandardDeviation();
    }

    /**
     * Default 95% confidence interval
     */
    public Bootstrap(){
        this(2.5, 97.5);
    }

    public double[] evaluate(double[] x){
        double[] ci = new double[2];
        ci[0] = percentile.evaluate(x, lower);
        ci[1] = percentile.evaluate(x, upper);
        return ci;
    }

    public double standardError(double[] x){
        return stdDev.evaluate(x);
    }

}

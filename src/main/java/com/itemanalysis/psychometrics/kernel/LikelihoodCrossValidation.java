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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.univariate.BrentOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariatePointValuePair;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * See Silverman (1986, p. 53).
 *
 * Silverman, B. W. (1986). Density estimation for statistics and data analysis. Boca Raton, FL: Chapman Hall.
 *
 */
@Deprecated
public class LikelihoodCrossValidation implements Bandwidth, UnivariateFunction {

    private KernelDensity density = null;

    private KernelFunction kernel = null;

    private double[] x = null;

    private double max = 0.0;

    private int n = 0;

    public LikelihoodCrossValidation(KernelFunction kernel, double[] x){
        this.kernel = kernel;
        this.x = x;
        this.n = x.length;
        computeBounds();
    }

    private void computeBounds(){
        StandardDeviation sd = new StandardDeviation();
        this.max = sd.evaluate(x);
    }

    private double densityAt(int index, double bandwidth){
        double z = 0.0;
        double sum = 0.0;
        for(int j=0;j<n;j++){
            if(j!=index){
                z = (x[index]-x[j])/bandwidth;
                sum += kernel.value(z);
            }
        }
        return sum/(((double)n-1.0)*bandwidth);
    }

    public double value(double h){
        double sum = 0.0;
        for(int i=0;i<n;i++){
            sum += Math.log(densityAt(i, h));
        }
        return sum /= (double)n;
    }

    public double value(){
        BrentOptimizer brent = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result;
        result = brent.optimize(400, this, GoalType.MAXIMIZE, 0.001, max);
        return result.getPoint();
    }

    public double getAdjustmentFactor(){
        return 1.0;
    }



}

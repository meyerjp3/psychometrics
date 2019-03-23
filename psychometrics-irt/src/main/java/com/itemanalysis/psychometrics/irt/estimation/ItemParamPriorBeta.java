/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.irt.estimation;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.util.Precision;

public class ItemParamPriorBeta implements ItemParamPrior {

    private double alpha = 1;

    private double beta = 1;

    private BetaDistribution betaDistribution = null;

    private final double EPSILON = Precision.EPSILON;

    public ItemParamPriorBeta(double alpha, double beta){
        this.alpha = alpha;
        this.beta = beta;
        betaDistribution  = new BetaDistribution(alpha, beta);
    }

    public boolean zeroDensity(double x){
        if(x<0 || x > 1) return true;
        return false;
    }

    public double nearestNonZero(double x){
        if(x>1) return 1.0-EPSILON;
        if(x<0) return EPSILON;
        return x;
    }

    public double logDensity(double x){
        if(x<0) return Double.MIN_VALUE;
        if(x>1) return Double.MIN_VALUE;
        return betaDistribution.logDensity(x);
    }

    public double logDensityDeriv1(double p){
        //Outside limits of quadrature density does not change, so derivative is zero
        if (zeroDensity(p)) return 0.0;
        double value = (alpha - 1.0) / (p - 0);
        value -= (beta - 1.0) / (1 - p);
        return value;
    }

    /**
     * Second derivative of log density. Only compute part of the log of the density that depends on the parameter.
     *
     * @param p
     * @return
     */
    public double logDensityDeriv2(double p){
        //Outside limits of quadrature density does not change so derivative is zero
        if (zeroDensity(p)){
            return 0.0;
        }

        double denom = p - 0;
        double value = (1.0 - alpha);
        value /= denom*denom;
        denom = 1 - p;
        value -= (beta - 1.0) / (denom*denom);

        return value;

    }

    public String distributionName(){
        return "beta quadrature";
    }

    public int getNumberOfParameters(){
        return 2;
    }

}

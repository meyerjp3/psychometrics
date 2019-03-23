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

import org.apache.commons.math3.util.Precision;

/**
 * Implementation of an ItemParamPrior for the LogNormal quadrature. This class is a translation
 * of ItemParamPriorLogNormal.cpp in Brad Hanson's ETIRM library, Copyright (c) 2000, Bradley A. Hanson.
 */
public class ItemParamPriorLogNormal implements ItemParamPrior {

    private double[] parameters = null;
    private double variance = 0.0;


    /**
     * Constructor for the standard lognormal quadrature
     */
    public ItemParamPriorLogNormal(){
        this(0.0, 1.0);
    }

    /**
     * Constructor takes an array as the input parameters. The first element is the mean and
     * the second element is the standard deviation.
     * @param param an array with the meanLog as the first argument and sdLog as the second.
     */
    public ItemParamPriorLogNormal(double[] param){
        this.parameters = param;
        variance = param[1]*param[1];
    }

    /**
     * Returns true if density at p is zero
     * @param p
     * @return
     */
    public boolean zeroDensity(double p){
        return (p <= 0.0) ? true : false;
    }

    /**
     * If density of x is zero (x <= 0) then return small slightly greater than zero that has a non-zero density
     * @param x
     * @return
     */
    public double nearestNonZero(double x){
        return (x <= 0.0) ? Precision.EPSILON : x;
    }

    /**
     * Constructor taking the mean and standard deviation as arguments.
     * @param mean mean on the logarithmic scale (i.e. meanLog)
     * @param sd standard deviation on the logarithmic scale (i.e. sdLog)
     */
    public ItemParamPriorLogNormal(double mean, double sd){
        if(sd <= 0.0) throw new IllegalArgumentException("Negative sd not allowed in ItemParamPriorLogNormal");
        parameters = new double[2];
        parameters[0] = mean;
        parameters[1] = sd;
        variance = sd*sd;
    }

    /**
     * Only compute part of the log of the density that depends on the parameter
     * @param p Argument of log density function (an item parameter value)
     * @return
     */
    public double logDensity(double p){
        //Check for value outside limits of quadrature
        if (zeroDensity(p)){
            return Double.MIN_VALUE;
        }
        double value = Math.log(p) - parameters[0];
        value *= value;
        value /= -2.0 * variance;
        value -= Math.log(p);

        return value;
    }

    /**
     * First derivative of log density. Only compute part of the log of the density that depends on the parameter.
     *
     * @param p
     * @return
     */
    public double logDensityDeriv1(double p){
       //Outside limits of quadrature density does not change, so derivative is zero
        if (zeroDensity(p)) return 0.0;
        double value = Math.log(p) - parameters[0] + variance;
        value /= variance * p;
        return -value;
    }

    /**
     * Second derivative of log density. Only compute part of the log of the density that depends on the parameter.
     *
     * @param p
     * @return
     */
    public double logDensityDeriv2(double p){
       //Outside limits of quadrature density does not change, so derivative is zero
        if (zeroDensity(p)) return 0.0;
        double value = Math.log(p) - parameters[0] + variance - 1.0;
        value /= p * p * variance;
        return value;
    }

    public int getNumberOfParameters(){
        return parameters.length;
    }

    public String distributionName(){
        return "LogNormal(" + parameters[0] +", " + parameters[1] + ")";
    }

}

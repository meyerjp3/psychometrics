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

/**
 * Implementation of an ItemParamPrior for the Beta4 quadrature. This class is a translation
 * of ItemParamPriorBeta4.cpp in Brad Hanson's ETIRM library, Copyright (c) 2000, Bradley A. Hanson.
 *
 */
public class ItemParamPriorBeta4 implements ItemParamPrior {

    private double[] parameters = null;

    public ItemParamPriorBeta4(){
        this(1.0, 1.0, 0.0, 1.0);
    }

    public ItemParamPriorBeta4(double a, double b, double l, double u)throws IllegalArgumentException{
        if (a <= 0.0 || b <= 0.0) throw new IllegalArgumentException("Parameters out of range in Beta4 item prior");
        if(l >= u) throw new IllegalArgumentException("Lower bound is greater than the upper bound in Beta4 item prior");
        parameters = new double [4];
        parameters[0] = a;
        parameters[1] = b;
        parameters[2] = l;
        parameters[3] = u;
    }

    /**
     * If the density of x is zero then return the point nearest x that has a non-zero density.
     * @param x
     * @return
     */
    public double nearestNonZero(double x){
        // Density at lower and upper limits has zero
        // density, so return a value that is offset
        // from the lower and upper limits by the following amount
        double offset = 0.001;
        double pp = 0;

        if (x <= parameters[2]){
            pp = parameters[2]+offset;
        }
        else if (x >= parameters[3]){
            pp = parameters[3]-offset;
        }else{
            pp = x;
        }

        return pp;
    }

    /**
     * Returns true if density at p is zero
     * @param p
     * @return
     */
    public boolean zeroDensity(double p){
        return (p <= parameters[2] || p >= parameters[3]);
    }

    /**
     * Only compute part of the log of the density that depends on the parameter
     *
     * @param p Argument of log density function (an item parameter value)
     * @return
     */
    public double logDensity(double p){
	    //Check for value outside limits of quadrature
        if (zeroDensity(p)){
            return Double.MIN_VALUE;
        }
        double value = (parameters[0] - 1.0) * Math.log(p - parameters[2]);
        value += (parameters[1] - 1.0) * Math.log(parameters[3] - p);
        return value;
    }

    /**
     * First derivative of log density. Only compute part of the log of the density that depends on the parameter.
     * @param p
     * @return
     */
    public double logDensityDeriv1(double p){
        //Outside limits of quadrature density does not change, so derivative is zero
        if (zeroDensity(p)) return 0.0;
        double value = (parameters[0] - 1.0) / (p - parameters[2]);
        value -= (parameters[1] - 1.0) / (parameters[3] - p);
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

        double denom = p - parameters[2];
        double value = (1.0 - parameters[0]);
        value /= denom*denom;
        denom = parameters[3] - p;
        value -= (parameters[1] - 1.0) / (denom*denom);

        return value;

    }

    public int getNumberOfParameters(){
        return parameters.length;
    }

    public String distributionName(){
        return "Beta4(" + parameters[0] +", " + parameters[1] + ", " + parameters[2] + ", " + parameters[0] + ")";
    }

}

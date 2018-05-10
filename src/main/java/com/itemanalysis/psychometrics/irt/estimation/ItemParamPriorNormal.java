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
 *
 * Implementation of an ItemParamPrior for the Normal quadrature. This class is a translation
 * of ItemParamPriorNormal.cpp in Brad Hanson's ETIRM library, Copyright (c) 2000, Bradley A. Hanson.
 */
public class ItemParamPriorNormal implements ItemParamPrior {

    private double[] parameters = null;
    private double variance = 0;

    /**
     * Constructor for the standard normal quadrature
     */
    public ItemParamPriorNormal(){
        this(0.0, 1.0);
    }

    /**
     * A constructor for an array of parameters. The first element must be the mean and the second element
     * must be the standard deviation.
     *
     * @param parameters parameters for the normal quadrature
     */
    public ItemParamPriorNormal(double[] parameters){
        this.parameters = parameters;
        variance = parameters[1]*parameters[1];
    }

    public ItemParamPriorNormal(double mean, double sd){
        if(sd <= 0.0) throw new IllegalArgumentException("Negtive sd not allowed in ItemParamPriorNormal");
        this.parameters = new double[2];
        this.parameters[0] = mean;
        this.parameters[1] = sd;
        variance = sd*sd;
    }

    /**
     * Only compute part of the log of the density that depends on the parameter
     * @param p Argument of log density function (an item parameter value)
     * @return
     */
    public double logDensity(double p){
        double value = parameters[0] - p;
        value /= variance;
        return value;
    }

    /**
     * First derivative of log density. Only compute part of the log of the density that depends on the parameter
     *
     * @param p
     * @return
     */
    public double logDensityDeriv1(double p){
        return -1.0/variance;
    }

    public double logDensityDeriv2(double p){
        return -1.0/variance;
    }

    public int getNumberOfParameters(){
        return 2;
    }

    public String distributionName(){
        return "Normal(" + parameters[0] +", " + parameters[1] + ")";
    }

    /**
     * Empty method
     *
     * @param p
     * @return
     */
    public boolean zeroDensity(double p){
        return false;
    }

    /**
     * Empty method
     *
     * @param x
     * @return
     */
    public double nearestNonZero(double x){
        return x;
    }


}

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
package com.itemanalysis.psychometrics.distribution;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Formatter;

/**
 * An immutable object for creating evaluation points and associated density values from a normal distribution.
 * This class creates a set of evenly spaced evaluation points between the the minimum and maximum values.
 * Normal density values for each point are also computed. By default the distribution is a standard normal
 * distribution (mean=0, sd=1), but the user cna optionally provide a different mean and standard deviation.
 *
 */
public final class NormalDistributionApproximation extends AbstractDistributionApproximation {

    /**
     * Creates a numerical approximation to the standard normal distribution. Evaluation points will lie between
     * the minimum and maximum values. The number of evaluation points is specified as an argument.
     *
     * @param min minimum evaluation point.
     * @param max maximum evaluation point.
     * @param numberOfPoints number of evaluation points (and corresponding number of density values).
     */
    public NormalDistributionApproximation(double min, double max, int numberOfPoints)throws IllegalArgumentException{
        this(0.0, 1.0, min, max, numberOfPoints);
    }

    /**
     * Creates a numerical approximation to a normal distribution with the specified mean and standard deviation.
     * Evaluation points will lie between the minimum and maximum values. The number of evaluation points is
     * specified as an argument.
     *
     * @param mean mean of the normal distribution.
     * @param sd standard deviation of the normal distribution.
     * @param min minimum evaluation point.
     * @param max maximum evaluation point.
     * @param numberOfPoints number of evaluation points (and corresponding number of density values).
     */
    public NormalDistributionApproximation(double mean, double sd, double min, double max, int numberOfPoints)throws IllegalArgumentException{
        initialize(numberOfPoints, min, max);

        //If mean is outside min or max value or is on the boundary (an invalid case),
        // make the mean the midpoint between min and max.
        if(mean<=min || mean>=max){
            throw new IllegalArgumentException("Invalid parameters for normal distribution approximation");
        }

        this.setNormalPointsAndWeights(mean, sd);

    }

    private void initialize(int numberOfPoints, double min, double max){
        this.numberOfPoints = numberOfPoints;
        this.min = min;
        this.max = max;

        //Enforce that min <= max
        if(this.min>this.max){
            double temp = this.min;
            this.min = this.max;
            this.max = temp;
        }

        range = this.max-this.min;
        points = new double[numberOfPoints];
        weights = new double[numberOfPoints];
        step = range/((double)numberOfPoints - 1.0);
    }

    public void setDensityAt(int index, double value){//TODO should only allow densities from a normal distribution
        weights[index] = value;
    }

    public void setPointAt(int index, double value){
        points[index] = value;
    }



    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-35s", "Normal Distribution Approximation"); f.format("%n");
        f.format("%35s", "==================================="); f.format("%n");
        f.format("%10s", "Value");f.format("%5s", "");f.format("%10s", "Density");f.format("%10s", "");f.format("%n");
        f.format("%35s", "-----------------------------------"); f.format("%n");

        for(int i=0;i<numberOfPoints;i++){
            f.format("%10.6f", points[i]);f.format("%5s", "");f.format("%10.8e", weights[i]);f.format("%10s", "");f.format("%n");
        }

        f.format("%35s", "==================================="); f.format("%n");

        return f.toString();
    }

}

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

import org.apache.commons.math3.distribution.NormalDistribution;
/**
 * An immutable object for creating evaluation points and associated density values from a normal distribution.
 * This class creates a set of evenly spaced evaluation points between the the minimum and maximum values.
 * Normal density values for each point are also computed. By default the distribution is a standard normal
 * distribution (mean=0, sd=1), but the user cna optionally provide a different mean and standard deviation.
 *
 */
public final class NormalDistributionApproximation implements DistributionApproximation {

//    private double mean = 0.0;
//
//    private double sd = 1.0;
//
//    private double min = 0.0;
//
//    private double max = 0.0;
//
//    private double range = 0.0;

    private int numberOfPoints = 0;

    private double[] points = null;

    private double[] density = null;

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
        this.numberOfPoints = numberOfPoints;

        //force ordering of min and max from lowest to highest
        if(max<min){
            double temp = min;
            min = max;
            max = temp;
        }

        //If mean is outside min or max value or is on the boundary (an invalid case),
        // make the mean the midpoint between min and max.
        if(mean<=min || mean>=max){
            throw new IllegalArgumentException("Invalid parameters for normal distribution approximation");
        }

        initialize(min, max, mean, sd);

    }

    private void initialize(double min, double max, double mean, double sd){
        //create points
        double range = max-min;
        points = new double[numberOfPoints];
        double step = range/((double)numberOfPoints - 1.0);
        points[0] = min;
        for(int i=1;i<numberOfPoints;i++){
            points[i] = points[i-1]+step;
        }

        //compute density
        NormalDistribution normal = new NormalDistribution(mean, sd);
        density = new double[numberOfPoints];
        double densitySum = 0.0;
        for(int i=0;i<numberOfPoints;i++){
            density[i] = normal.density(points[i]);
            densitySum += density[i];
        }

        //make sure probabilities sum to unity
        for(int i=0;i<numberOfPoints;i++){
            density[i] = density[i]/densitySum;
        }

    }

    /**
     * Gets the array of evaluation points.
     *
     * @return evaluation points.
     */
    public double[] getPoints(){
        return points;
    }

    /**
     * Gets the array of density values.
     *
     * @return density values.
     */
    public double[] evaluate(){
        return density;
    }

    /**
     * Gets an evaluation points at the specified index of the array.
     *
     * @param index array index of evaluation point.
     * @return an evaluation point.
     */
    public double getPointAt(int index){
        return points[index];
    }

    /**
     * Gets a density value at the specified index of the array.
     *
     * @param index array index of density value.
     * @return a density value.
     */
    public double getDensityAt(int index){
        return density[index];
    }

    public void setDensityAt(int index, double value){
        density[index] = value;
    }

    public void setPointAt(int index, double value){
        points[index] = value;
    }

    public double getMean(){
        double m = 0.0;
        for(int i=0;i<numberOfPoints;i++){
            m += points[i]*density[i];
        }
        return m;
    }

    public double getStandardDeviation(){
        double m = getMean();
        double m2 = 0;
        for(int i=0;i<numberOfPoints;i++){
            m2 += (points[i]-m)*(points[i]-m)*density[i];
        }
        return Math.sqrt(m2);
    }

    /**
     * Gets the number of evaluation points (and corresponding number of density values).
     *
     * @return number of evaluation points.
     */
    public int getNumberOfPoints(){
        return numberOfPoints;
    }

}

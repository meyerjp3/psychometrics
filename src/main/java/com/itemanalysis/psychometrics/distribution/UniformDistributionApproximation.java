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
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An immutable object for creating evaluation points and associated density values. This class creates a set of
 * evenly spaced evaluation points between the the minimum and maximum values. The density for each point is also
 * computed.
 *
 */
public final class UniformDistributionApproximation implements DistributionApproximation {

    private double min = 0.0;

    private double max = 0.0;

    private int numberOfPoints = 0;

    private double[] points = null;

    private double[] density = null;

    /**
     * Create a uniform distribution with a specified number of evaluation points between the min and max values.
     *
     * @param min minimum value of the distribution.
     * @param max maximum value of the distribution.
     * @param numberOfPoints number of points in the distribution.
     */
    public UniformDistributionApproximation(double min, double max, int numberOfPoints){
        this.numberOfPoints = numberOfPoints;
        this.min = min;
        this.max = max;
    }

    /**
     * The number of getPoints return is specified by numberOfPoints.
     * @return equally spaced disjoint getPoints from the interval [min, max]
     */
    public double[] getPoints(){
        if(points!=null) return points;

        double difference = max - min;
        if(max<min){
            difference = min - max;
        }

        //create evenly spaced points from min to max
        points = new double[numberOfPoints];
        double step = difference/((double)numberOfPoints - 1.0);
        points[0] = min;
        for(int i=1;i<numberOfPoints;i++){
            points[i] = points[i-1]+step;
        }
        return points;
    }

    /**
     * Gets an array of density values at each point in the distribution.
     *
     * @return density values.
     */
    public double[] evaluate(){
        if(density!=null) return density;
        if(points==null) getPoints();

        density = new double[numberOfPoints];
        for(int i=0;i<numberOfPoints;i++){
            density[i] = 1.0/numberOfPoints;
        }

        return density;
    }

    /**
     * Gets a single point value at index.
     *
     * @param index the array index for the point value.
     * @return evaluation point.
     */
    public double getPointAt(int index){
        if(points==null) getPoints();
        return points[index];
    }

    /**
     * Gets a single density point at index.
     *
     * @param index array index of density point.
     * @return density point.
     */
    public double getDensityAt(int index){
        if(density==null) evaluate();
        return density[index];
    }

    /**
     * Gets the number of evaluation points in this distribution approximation.
     *
     * @return number of evaluation points.
     */
    public int getNumberOfPoints(){
        return numberOfPoints;
    }

    /**
     * Gets the smallest evaluation point.
     *
     * @return minimum evaluation point.
     */
    public double getMin(){
        return min;
    }

    /**
     * Gets the largest evaluation point.
     *
     * @return largest evaluation point.
     */
    public double getMax(){
        return max;
    }

}

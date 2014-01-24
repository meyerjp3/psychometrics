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
 * An immutable object for creating evaluation getPoints and associated evaluate.
 * Used in kernel evaluate estimation and equating.
 *
 */
public final class UniformDistributionApproximation implements DistributionApproximation {

    private double min = 0.0;

    private double max = 0.0;

    private int numberOfPoints = 0;

    private double[] points = null;

    private double[] density = null;

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

    public double[] evaluate(){
        if(density!=null) return density;
        if(points==null) getPoints();

//        double densitySum = 0.0;
        density = new double[numberOfPoints];
        for(int i=0;i<numberOfPoints;i++){
            density[i] = 1.0/numberOfPoints;
//            densitySum += density[i];
        }

//        for(int i=0;i<numberOfPoints;i++){
//            density[i] = density[i]/densitySum;
//        }

        return density;
    }

    public double getPointAt(int index){
        if(points==null) getPoints();
        return points[index];
    }

    public double getDensityAt(int index){
        if(density==null) evaluate();
        return density[index];
    }

    public int getNumberOfPoints(){
        return numberOfPoints;
    }

    public double getMin(){
        return min;
    }

    public double getMax(){
        return max;
    }

}

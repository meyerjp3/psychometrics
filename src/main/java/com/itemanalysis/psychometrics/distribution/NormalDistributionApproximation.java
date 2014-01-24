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
 * An immutable object for creating evaluation getPoints and associated evaluate.
 * Used in kernel evaluate estimation and equating.
 *
 */
public final class NormalDistributionApproximation implements DistributionApproximation {

    private double mean = 0.0;

    private double sd = 1.0;

    private double min = 0.0;

    private double max = 0.0;

    private double range = 0.0;

    private int numberOfPoints = 0;

    private double[] points = null;

    private double[] density = null;

    public NormalDistributionApproximation(double min, double max, int numberOfPoints){
        this(0.0, 1.0, min, max, numberOfPoints);
    }

    public NormalDistributionApproximation(double mean, double sd, double min, double max, int numberOfPoints){
        this.mean = mean;
        this.sd = sd;
        this.min = min;
        this.max = max;
        this.numberOfPoints = numberOfPoints;

        //force ordering of min and max from lowest to highest
        if(max<min){
            this.min = max;
            this.max = min;
        }
        range = this.max - this.min;

        //If mean is outside min or max value or is on teh boundary (an invalid case),
        // make the mean the midpoint between min and max.
        if(mean<=this.min || mean>=this.max){
            this.mean = this.min+range/2;
        }

    }

    public double[] getPoints(){
        if(points!=null) return points;

        points = new double[numberOfPoints];
        double step = range/((double)numberOfPoints - 1.0);
        points[0] = min;
        for(int i=1;i<numberOfPoints;i++){
            points[i] = points[i-1]+step;
        }
        return points;
    }

    public double[] evaluate(){
        if(density!=null) return density;
        if(points==null) getPoints();

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

}

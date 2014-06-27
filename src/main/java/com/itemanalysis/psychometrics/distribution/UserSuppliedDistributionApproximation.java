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

import org.apache.commons.math3.util.ResizableDoubleArray;

/**
 * A distribution approximation that allows the user to provide the evaluation points and the density values.
 * This is a class that can be used for numeric integration with user provided quadrature points and weights.
 */
public final class UserSuppliedDistributionApproximation implements DistributionApproximation{

    private ResizableDoubleArray pointsStore = new ResizableDoubleArray();
    private ResizableDoubleArray densityStore = new ResizableDoubleArray();
    private double[] points = null;
    private double[] density = null;
    private int numberOfPoints = 0;
    private double weightSum = 0;

    /**
     * Create the distribution approximation with an array of evaluation points and an array of density values
     * provided by the user.
     *
     * @param points array of evaluation points.
     * @param density array of density values.
     */
    public UserSuppliedDistributionApproximation(double[] points, double[] density){
        this.pointsStore.addElements(points);
        this.densityStore.addElements(density);
        this.numberOfPoints = points.length;
    }

    /**
     * Creates the distribution approximation with no evaluation points or density values. This constructor
     * is useful when the evaluation points and weights are provided incrementally with {@link #increment(double)}
     * or {@link #increment(double)};
     */
    public UserSuppliedDistributionApproximation(){

    }

    /**
     * Increment the array of evaluation points and weights with the provided values.
     * 
     * @param point an evaluation point.
     * @param density a density value.
     */
    public void increment(double point, double density){
        this.pointsStore.addElement(point);
        this.densityStore.addElement(density);
        numberOfPoints++;
    }

    /**
     * An evaluation points. This method will result in uniform density values.
     * 
     * @param point
     */
    public void increment(double point){
        this.pointsStore.addElement(point);
        numberOfPoints++;
        weightSum += 1.0;
    }

    /**
     * Gets the number of evaluation points (and corresponding number of density values).
     * 
     * @return number of evaluation points.
     */
    public int getNumberOfPoints(){
        return numberOfPoints;
    }

    /**
     * Gets the array of evaluation points.
     * 
     * @return evaluation points.
     */
    public double[] getPoints(){
        if(points!=null) return points;
        points = pointsStore.getElements();
        return points;
    }

    /**
     * Gets the array of density values.
     * 
     * @return density values.
     */
    public double[] evaluate(){
        if(density!=null) return density;
        if(points==null) getPoints();
        density = densityStore.getElements();
        if(weightSum>0){
            for(int i=0;i<density.length;i++){
                density[i] = density[i]/weightSum;
            }
        }
        return density;
    }

    /**
     * Gets an evaluation point from the array at position given by the index.
     * 
     * @param index array index of evaluation point.
     * @return an evaluation point.
     */
    public double getPointAt(int index){
        if(points==null) getPoints();
        return points[index];
    }

    /**
     * Gets the density value from the array at the psition given by index.
     * 
     * @param index array index of density value.
     * @return a density value.
     */
    public double getDensityAt(int index){
        if(density==null) evaluate();
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


}

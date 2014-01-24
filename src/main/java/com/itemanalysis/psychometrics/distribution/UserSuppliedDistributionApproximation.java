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


public final class UserSuppliedDistributionApproximation implements DistributionApproximation{

    private ResizableDoubleArray pointsStore = new ResizableDoubleArray();
    private ResizableDoubleArray densityStore = new ResizableDoubleArray();
    private double[] points = null;
    private double[] density = null;
    private int numberOfPoints = 0;
    private double weightSum = 0;

    public UserSuppliedDistributionApproximation(double[] points, double[] density){
        this.pointsStore.addElements(points);
        this.densityStore.addElements(density);
        this.numberOfPoints = points.length;
    }

    public UserSuppliedDistributionApproximation(){

    }

    public void increment(double point, double density){
        this.pointsStore.addElement(point);
        this.densityStore.addElement(density);
        numberOfPoints++;
    }

    public void increment(double point){
        this.pointsStore.addElement(point);
        numberOfPoints++;
        weightSum += 1.0;
    }

    public int getNumberOfPoints(){
        return numberOfPoints;
    }

    public double[] getPoints(){
        if(points!=null) return points;
        points = pointsStore.getElements();
        return points;
    }

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

    public double getPointAt(int index){
        if(points==null) getPoints();
        return points[index];
    }

    public double getDensityAt(int index){
        if(density==null) evaluate();
        return density[index];
    }


}

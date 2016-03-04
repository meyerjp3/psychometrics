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

import java.util.Formatter;

/**
 * A distribution approximation that allows the user to provide the evaluation points and the density values.
 * This is a class that can be used for numeric integration with user provided quadrature points and weights.
 */
@Deprecated
public final class UserSuppliedDistributionApproximation extends AbstractDistributionApproximation{

    private ResizableDoubleArray pointsStore = new ResizableDoubleArray();
    private ResizableDoubleArray densityStore = new ResizableDoubleArray();

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
        this.points = new double[numberOfPoints];
        this.weights = new double[numberOfPoints];
    }

    /**
     * Creates the distribution approximation with no evaluation points or density values. This constructor
     * is useful when the evaluation points and weights are provided incrementally with {@link #increment(double)}
     * or {@link #increment(double)};
     */
    public UserSuppliedDistributionApproximation(){
        points = new double[numberOfPoints];
        weights = new double[numberOfPoints];
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
    }



    public void setDensityAt(int index, double value){
        weights[index] = value;
    }

    public void setPointAt(int index, double value){
        points[index] = value;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-35s","Distribution Approximation         "); f.format("%n");
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

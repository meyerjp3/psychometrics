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
package com.itemanalysis.psychometrics.quadrature;

import java.util.Formatter;

/**
 * A quadrature approximation that allows the user to provide the evaluation points and the density values.
 * This is a class that can be used for numeric integration with user provided quadrature points and weights.
 */
public final class UserSuppliedQuadratureRule extends AbstractQuadratureRule {

    /**
     * Create the quadrature approximation with an array of evaluation points and an array of density values
     * provided by the user.
     *
     * @param points array of evaluation points.
     * @param density array of density values.
     */
    public UserSuppliedQuadratureRule(double[] points, double[] density){
        this.numberOfPoints = points.length;
        this.points = points;
        this.weights = density;
    }

    /**
     * Constructor when input is a two-dimensional array. The first column gives the
     * poinhts and the second column gives the weights.
     *
     * @param pointsAndWeights two dimensional array with only two columns
     */
    public UserSuppliedQuadratureRule(double[][] pointsAndWeights){
        this.numberOfPoints = pointsAndWeights.length;
        this.points = new double[numberOfPoints];
        this.weights = new double[numberOfPoints];

        for(int i=0;i<numberOfPoints;i++){
            this.points[i] = pointsAndWeights[i][0];
            this.weights[i] = pointsAndWeights[i][1];
        }
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

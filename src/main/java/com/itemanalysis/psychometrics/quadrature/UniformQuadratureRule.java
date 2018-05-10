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
 * An immutable object for creating evaluation points and associated density values. This class creates a set of
 * evenly spaced evaluation points between the the minimum and maximum values. The density for each point is also
 * computed.
 *
 */
public final class UniformQuadratureRule extends AbstractQuadratureRule {

    /**
     * Create a uniform quadrature with a specified number of evaluation points between the min and max values.
     *
     * @param min minimum value of the quadrature.
     * @param max maximum value of the quadrature.
     * @param numberOfPoints number of points in the quadrature.
     */
    public UniformQuadratureRule(double min, double max, int numberOfPoints){
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

        this.setUniformPointsAndWeights();

    }

    public void setDensityAt(int index, double value){//TODO this method should be change. It should only allow for uniform weights
        weights[index] = value;
    }

    public void setPointAt(int index, double value){
        points[index] = value;
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-35s", "Uniform Quadrature                "); f.format("%n");
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

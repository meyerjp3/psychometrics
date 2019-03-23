/**
 * Copyright 2016 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.quadrature;

import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import java.util.Formatter;

/**
 * A class that provides a discrete approximation of a univariate continuous probability quadrature.
 * Initial values may be from a normal quadrature or a uniform quadrature. The points and weights
 * may be updated, which may result in something other than a normal or uniform quadrature (i.e. an
 * empirical histogram). Methods allow the quadrature to be standardized to have a mean of zero and
 * a standard deviaiton of one.
 *
 */
public class ContinuousQuadratureRule extends AbstractQuadratureRule {

    /**
     * Constructor for using starting values from a uniform quadrature. Points and weight may be updated to
     * something other than normal.
     *
     * @param numberOfPoints number of qudrature points
     * @param min minimum quadrature point
     * @param max maximum qudrature point
     */
    public ContinuousQuadratureRule(int numberOfPoints, double min, double max){
        initialize(numberOfPoints, min, max);
        this.setUniformPointsAndWeights();
    }

    /**
     * Constructor for using starting values from a normal quadrature. Points and weight may be updated to
     * something other than normal.
     *
     * @param numberOfPoints number of qudrature points
     * @param min minimum quadrature point
     * @param max maximum qudrature point
     * @param mean mean of normal quadrature for starting values.
     * @param standardDeviation standard deviation of normal quadrature for starting values.
     */
    public ContinuousQuadratureRule(int numberOfPoints, double min, double max, double mean, double standardDeviation){
        initialize(numberOfPoints, min, max);
        this.setNormalPointsAndWeights(mean, standardDeviation);
    }

    /**
     * Construct the object using the supplied arrays of points and weights.
     *
     * @param points discrete real points
     * @param weights weights for the points
     */
    public ContinuousQuadratureRule(double[] points, double[] weights){
        Min min = new Min();
        Max max = new Max();

        this.numberOfPoints = points.length;
        this.min = min.evaluate(points);
        this.max = max.evaluate(points);

        //Enforce that min <= max
        if(this.min>this.max){
            double temp = this.min;
            this.min = this.max;
            this.max = temp;
        }

        range = this.max-this.min;
        step = range/((double)numberOfPoints - 1.0);

        this.points = points;
        this.weights = weights;
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

        f.format("%-35s", "  Continuous Points and Weights  "); f.format("%n");
        f.format("%35s", "==================================="); f.format("%n");
        f.format("%10s", "Point");f.format("%5s", "");f.format("%10s", "Weight");f.format("%10s", "");f.format("%n");
        f.format("%35s", "-----------------------------------"); f.format("%n");

        for(int i=0;i<numberOfPoints;i++){
            f.format("%10.6f", points[i]);f.format("%5s", "");f.format("%10.8e", weights[i]);f.format("%10s", "");f.format("%n");
        }

        f.format("%35s", "==================================="); f.format("%n");

        return f.toString();
    }


}

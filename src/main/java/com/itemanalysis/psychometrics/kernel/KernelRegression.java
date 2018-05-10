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
package com.itemanalysis.psychometrics.kernel;

import com.itemanalysis.psychometrics.quadrature.UniformQuadratureRule;

/**
 *
 * Nadaraya-Watson kernel regression estimator. Data values are not
 * stored in this object. Only the evaluation getPoints (x-values) and their
 * corresponding y-values are stored.
 *
 * This class is designed to incrementally compute the regression.
 *
 */
public class KernelRegression{

    /**
     * Evaluation points
     */
    private double[] points = null;

    /**
     * Holds the numerator value of teh kernel regression estimator
     */
    private double[] numerator = null;

    /**
     * Holds the denominator value of teh kernel regression estimator
     */
    private double[] denominator = null;

    /**
     * Kernel function
     */
    private KernelFunction kernel = null;

    /**
     * Bandwith of teh function (i.e. smoothing parameter)
     */
    private double bandwidth = 1.0;

    /**
     * Sample size
     */
    private int n = 0;

    /**
     * Number of evaluation points
     */
    private int numPoints = 50;

    /**
     * Default constructor for the class.
     *
     * @param kernel type of kernel function
     * @param bandwidth bandwidth
     * @param uniform quadrature representing the evaluation points
     */
    public KernelRegression(KernelFunction kernel, Bandwidth bandwidth, UniformQuadratureRule uniform){
        this.kernel = kernel;
        this.bandwidth = bandwidth.value();
        this.points = uniform.getPoints();
        this.numPoints = uniform.getNumberOfPoints();
        numerator = new double[numPoints];
        denominator = new double[numPoints];
    }

    /**
     * Increment the estimate by x and y
     * @param x value of teh independent variable
     * @param y value of the dependent variable
     */
    public void increment(double x, double y){
        double k = 0.0;
        n+=1;
        for(int i=0;i<numPoints;i++){
            k = kernel.value((x-points[i])/bandwidth);
            numerator[i]+=k*y;
            denominator[i]+=k;
        }
    }

    public void increment(double x, double y, double weight){
        double k = 0.0;
        n+=1;
        for(int i=0;i<numPoints;i++){
            k = kernel.value((x-points[i])/bandwidth);
            numerator[i]+=weight*k*y;
            denominator[i]+=weight*k;
        }
    }

    /**
     * Computes teh kernel regression estimate at teh evaluation points.
     * @return predicted values of y
     */
    public double[] value(){
		double[] v = new double[numPoints];
		for(int i=0;i<v.length;i++){
            v[i] = numerator[i]/denominator[i];
		}
		return v;
	}

    /**
     *
     * @return evaluae pints points
     */
    public double[] getPoints(){
        return points;
    }

    /**
     *
     * @return bandwidth of the kernel regression estimator
     */
    public double getBandwidth(){
        return bandwidth;
    }

    /**
     *
     * @return sample size use din teh computation of teh regression
     */
    public int getSampleSize(){
        return n;
    }


}

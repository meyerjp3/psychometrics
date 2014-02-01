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

import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.distribution.UniformDistributionApproximation;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

public class KernelDensity implements DistributionApproximation {

	private KernelFunction kernel = null;

    private Bandwidth bandwidth = null;

    private double h = 1.0;

    /**
     * Points at which the kernel is evaluated (not the data points)
     */
    private double[] points = null;

    /**
     * Density values at the evaluation points
     */
    private double[] density = null;

    private double[] sum = null;

    /**
     * Sample size
     */
    private int n = 0;

    /**
     * number of evaluation points
     */
    private int numPoints = 0;

	public KernelDensity(KernelFunction kernel, Bandwidth h, UniformDistributionApproximation uniform){
        this.kernel = kernel;
        this.bandwidth = h;
        this.h = h.value();
        this.points = uniform.getPoints();
        this.numPoints = uniform.getNumberOfPoints();
        sum = new double[numPoints];
    }

    /**
     * Incremental computation of the density. This method is called for each data point.
     *
     * @param x a data point
     */
	public void increment(double x){
		double z=0.0;
        n+=1;
        for(int i=0;i<numPoints;i++){
            z=(points[i]-x)/h;
            sum[i] += kernel.value(z);
        }

	}

    /**
     * Call this method when using the incremental update to the density estimator.
     *
     * @return an array representing the density (y-axis) at the evaluation points.
     */
    public double[] evaluate(){
        if(density!=null) return density;
        density = new double[numPoints];
        for(int i=0;i<numPoints;i++){
			density[i] = sum[i]/(n*h);
		}
        return density;
    }

    /**
     * Call tihs method when not using the incremental update. It will compute the density from an
     * array of data points.
     *
     * @param x an array of data points
     * @return density of the points
     */
    public double[] evaluate(double[] x){
        int sampleSize = x.length;
        density = new double[numPoints];
        double z = 0.0;
        for(int i=0; i<numPoints;i++){
            for(int j=0;j<sampleSize;j++){
                z=(points[i]-x[j])/h;
                density[i] += kernel.value(z);
            }
            density[i] /= (sampleSize*h);
        }
        return density;
    }

    /**
     * Leave one out kernel evaluate estimate. Omitted data point is given by index.
     * This method is primarily used by the least squares cross validation method
     * of choosing the smoothing parameter.
     *
     * @param x
     * @param index
     * @return
     */
    public double[] evaluate(double[] x, int index){
        double[] density = new double[numPoints];
        int sampleSize = x.length;
        double z = 0.0;
        for(int i=0; i<numPoints;i++){
            for(int j=0;j<sampleSize;j++){
                if(j!=index){
                    z=(points[i]-x[j])/h;
                    density[i] += kernel.value(z);
                }
            }
            density[i] /= (sampleSize*h);
        }
        return density;
    }

    public double[] getPoints(){
        return points;
    }

    public int getNumberOfPoints(){
        return numPoints;
    }

    /**
     * Bandwith
     *
     * @return bandwith used for the kernel estimator
     */
    public Bandwidth getBandwidth(){
        return bandwidth;
    }

    public UnivariateFunction getInterpolater(){
        UnivariateInterpolator interpolator = new LinearInterpolator();
        UnivariateFunction func = interpolator.interpolate(points, evaluate());
        return func;
    }

    /**
     * Returns interpolator for squared evaluate
     *
     * @return
     */
    public UnivariateFunction getInterpolater2(){
        UnivariateInterpolator interpolator = new LinearInterpolator();
        double[] d = evaluate();
        for(int i=0;i<d.length;i++){
            d[i] = d[i]*d[i];
        }
        UnivariateFunction func = interpolator.interpolate(points, d);
        return func;
    }

    public double getPointAt(int index){
        return points[index];
    }

    public double getDensityAt(int index){
        if(density==null) evaluate();
        return density[index];
    }

}

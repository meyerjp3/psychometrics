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

import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.quadrature.UniformQuadratureRule;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

/**
 * @deprecated as of May 18, 2018. Replaced by {@link com.itemanalysis.psychometrics.distribution.KernelDensity}.
 */
@Deprecated
public class KernelDensity implements QuadratureRule {

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

    protected boolean interpolatorUpdateNeeded = false;

    //Cubic spline interpolator
    private UnivariateFunction interpolationFunction = null;

	public KernelDensity(KernelFunction kernel, Bandwidth h, UniformQuadratureRule uniform){
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

    public void setDensityAt(int index, double value){
        density[index] = value;
    }

    public void setPointAt(int index, double value){
        points[index] = value;
    }

    public double getMinimum(){
        return points[0];
    }

    public double getMaximum(){
        return points[numPoints-1];
    }

    public double getMean(){
        double m = 0.0;
        for(int i=0;i<numPoints;i++){
            m += points[i]*density[i];
        }
        return m;
    }

    public double getStandardDeviation(){
        double m = getMean();
        double m2 = 0;
        for(int i=0;i<numPoints;i++){
            m2 += (points[i]-m)*(points[i]-m)*density[i];
        }
        return Math.sqrt(m2);
    }

    /**
     * Uses current quadrature points and weights to compute the mean and standard deviation of the
     * density, and tehn standardizes the quadrature to have a mean of zero and a standard deviation of one.
     * It achieves standardization in one of two possible ways. If keepPoints is true, the original points are
     * retained and linear interpolation of the empiricial cumulative quadrature is used to obtain the new
     * weights. That is, the points are never changed, but the quadrature is standardized. If keepPoints is
     * false, the original weights are retained, but the points are transformed to standardize the quadrature.
     *
     * @param keepPoints if true original points are retained and weights are computed at these points using
     *                   linear interpolation of the empirical cumulative quadrature. If false, original weights
     *                   are retained and standardization is achieved by linearly transforming the original points.
     * @return transformation coefficients as a double array with two values used for the linear transformation of
     * the points. The first value is the slope and the second value is the intercept.
     */
    public double[] standardize(boolean keepPoints){

        //Compute current mean and standard deviation
        double newMean = this.getMean();
        double newSD = this.getStandardDeviation();

        //Compute transformation coefficients
        double slope = 1.0/newSD;
        double intercept = -slope*newMean;
        double[] coef = {slope, intercept};

        //Keep points and change weights to standardize the quadrature.
        if(keepPoints){
            //Transform points and compute cumulative sum of weights (i.e. cumulative probabilities)
            double[] x = new double[numPoints+2];
            double[] w = new double[numPoints+2];
            double cumSum = 0;
            for(int i=0;i<numPoints;i++){
                x[i+1] = points[i]*slope+intercept;
                cumSum += density[i];
                w[i+1] = cumSum;
            }

            //To interpolate at the boundary, set lower bound just below the minimum and
            //the upper bound just above the maximum.
            double lower = Math.min(getMinimum(), x[1]);
            double upper = Math.max(getMaximum(), x[numPoints]);
            x[0] = lower-0.05;
            x[numPoints+1] = upper+0.05;

            //Set weights to zero for the lower boundary and 1.0 for the upper boundary
            w[0] = 0.0;
            w[numPoints+1] = 1.0;

            //Interpolator object
            UnivariateFunction interpolationFunction = null;
            UnivariateInterpolator interpolator = new LinearInterpolator();
            interpolationFunction = interpolator.interpolate(x, w);
            interpolatorUpdateNeeded = false;

            density[0] = interpolationFunction.value(points[0]);
            for(int i=1;i<numPoints;i++){
                density[i] = interpolationFunction.value(points[i])-interpolationFunction.value(points[i-1]);
            }
        }
        //Keep weights and linearly transform points to standardize the quadrature
        else{
            for(int i=0;i<numPoints;i++){
                points[i] = points[i]*slope+intercept;
            }
        }

        return coef;
    }

}

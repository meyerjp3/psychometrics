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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.distribution.NormalDistribution;

public abstract class AbstractQuadratureRule implements QuadratureRule {

    protected int numberOfPoints = 49;
    protected double min = -Double.MAX_VALUE;
    protected double max = Double.MAX_VALUE;
    protected double range = max-min;
    protected double step = 0;
    protected double[] points = null;//quadrature point
    protected double[] weights = null;//quadrature weight
    protected NormalDistribution normal = null;

    public void setNormalPointsAndWeights(double mean, double sd){
        points[0] = min;
        for(int i=1;i<numberOfPoints;i++){
            points[i] = points[i-1]+step;
        }

        //compute weights
        normal = new NormalDistribution(mean, sd);
        weights = new double[numberOfPoints];
        double densitySum = 0.0;
        for(int i=0;i<numberOfPoints;i++){
            weights[i] = normal.density(points[i]);
            densitySum += weights[i];
        }

        //make sure probabilities sum to unity
        for(int i=0;i<numberOfPoints;i++){
            weights[i] = weights[i]/densitySum;
        }
    }

    public void setUniformPointsAndWeights(){
        //create evenly spaced points from min to max
        points = new double[numberOfPoints];
        double step = range/((double)numberOfPoints - 1.0);
        points[0] = min;
        for(int i=1;i<numberOfPoints;i++){
            points[i] = points[i-1]+step;
        }

        for(int i=0;i<numberOfPoints;i++){
            weights[i] = 1.0/numberOfPoints;
        }
    }

    public double[] getPoints(){
        return points;
    }

    public double[] evaluate(){
        return weights;
    }

    /**
     * Gets an evaluation points at the specified index of the array.
     *
     * @param index array index of evaluation point.
     * @return an evaluation point.
     */
    public double getPointAt(int index){
        return points[index];
    }

    /**
     * Gets a density value at the specified index of the array.
     *
     * @param index array index of density value.
     * @return a density value.
     */
    public double getDensityAt(int index){
        return weights[index];
    }

    public double getMinimum(){
        return points[0];
    }

    public double getMaximum(){
        return points[numberOfPoints-1];
    }

    public double getMean(){
        double m = 0.0;
        for(int i=0;i<numberOfPoints;i++){
            m += points[i]*weights[i];
        }
        return m;
    }

    public double getVariance(){
        double m = 0;
        double meanOfSquares = 0;
        for(int i=0;i<numberOfPoints;i++){
            m += points[i]*weights[i];
            meanOfSquares += points[i]*points[i]*weights[i];
        }

        return meanOfSquares-m*m;
    }

    public double getStandardDeviation(){
        return Math.sqrt(getVariance());
    }

//    /**
//     * Constant interpolation of ECDF.
//     *
//     * @param value
//     * @param x array of points in ECDF
//     * @param w array of cumulative probabilities in ECDF
//     * @return
//     */
//    private double ecdfToProbability(double value, double[] x, double[] w){
//        double prob = 0;
//        double cumProbLast = 0;
//        int last = x.length-1;
//
//        if(value < points[0]) return 0;
//        if(value > points[last]) return 0;
//
//
////        System.out.println("LI: "+ linearInterpolation(value, x[last], (w[last] - w[last-1]), points[last]+1e-8, 0));
//
//
//
//        if(value < x[0]) return linearInterpolation(value, points[0]-1e-8, 0, x[0], w[0]);
//        if(value > x[last]) return linearInterpolation(value, x[last], (w[last] - w[last-1]), points[last]+1e-8, 0);
//
//        for(int i=0;i<x.length;i++){
//            if(x[i]<=value){
//                prob = w[i] - cumProbLast;
//                cumProbLast = w[i];
//            }else{
//                break;
//            }
//        }
//
//
//
//        return prob;
//    }
//
//    private double linearInterpolation(double x, double x0, double y0, double x1, double y1){
//        double value = y0 + (y1-y0)*((x-x0)/(x1-x0));
//        return value;
//    }
//
//    public double[] standardize(boolean keepPoints){
//        //Compute current mean and standard deviation
//        double newMean = this.getMean();
//        double newSD = this.getStandardDeviation();
//
//        //Compute transformation coefficients
//        double slope = 1.0/newSD;
//        double intercept = -slope*newMean;
//        double[] coef = {slope, intercept};
//        double cumSum = 0;
//
//        //Keep points and change weights to standardize the quadrature.
//        if(keepPoints) {
//            //Compute empirical cumulative quadrature function (ECDF)
//            double[] x = new double[numberOfPoints];
//            double[] w = new double[numberOfPoints];
//            for (int i = 0; i < numberOfPoints; i++) {
//                x[i] = points[i] * slope + intercept;
//                cumSum += weights[i];
//                w[i] += cumSum;
//            }
//
//            for (int i = 0; i < numberOfPoints; i++) {
//                weights[i] = ecdfToProbability(points[i], x, w);
//            }
//        }
//        //Keep weights and linearly transform points to standardize the quadrature
//        else{
//            for(int i=0;i<numberOfPoints;i++){
//                points[i] = points[i]*slope+intercept;
//            }
//        }
//
//        return coef;
//    }


    /**
     * Uses current quadrature points and weights to compute the mean and standard deviation of the
     * density, and then standardizes the quadrature to have a mean of zero and a standard deviation of one.
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
        double[] coef = {intercept, slope};

        //Keep points and change weights to standardize the quadrature.
        if(keepPoints){
            //Transform points and compute cumulative sum of weights (i.e. cumulative probabilities)
            double[] x = new double[numberOfPoints+2];
            double[] w = new double[numberOfPoints+2];
            double cumSum = 0;
            for(int i=0;i<numberOfPoints;i++){
                x[i+1] = points[i]*slope+intercept;
                cumSum += weights[i];
                w[i+1] = cumSum;
            }

            //To interpolate at the boundary, set lower bound just below the minimum and
            //the upper bound just above the maximum.
            double lower = Math.min(getMinimum(), x[1]);
            double upper = Math.max(getMaximum(), x[numberOfPoints]);
            x[0] = lower-0.05;
            x[numberOfPoints+1] = upper+0.05;

            //Set weights to zero for the lower boundary and 1.0 for the upper boundary
            w[0] = 0.0;
            w[numberOfPoints+1] = 1.0;

            //Interpolator object
            UnivariateFunction interpolationFunction = null;
            UnivariateInterpolator interpolator = new LinearInterpolator();
            interpolationFunction = interpolator.interpolate(x, w);

            weights[0] = interpolationFunction.value(points[0]);
            for(int i=1;i<numberOfPoints;i++){
                weights[i] = interpolationFunction.value(points[i])-interpolationFunction.value(points[i-1]);
            }
        }
        //Keep weights and linearly transform points to standardize the quadrature
        else{
            for(int i=0;i<numberOfPoints;i++){
                points[i] = points[i]*slope+intercept;
            }
        }

        return coef;
    }

    /**
     * Gets the number of evaluation points (and corresponding number of density values).
     *
     * @return number of evaluation points.
     */
    public int getNumberOfPoints(){
        return numberOfPoints;
    }

}

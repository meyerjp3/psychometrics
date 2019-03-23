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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.univariate.BrentOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariatePointValuePair;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

/**
 * A class for computing the bandwidth by least squares cross validation kernel. This clas
 * is still in development. It needs more work and testing. Not ready for use.
 *
 **/
@Deprecated
 public class LeastSquaresCrossValidation implements Bandwidth, UnivariateFunction {

    private KernelFunction kernel = null;

    private double[] x = null;

    private double sd = 0.0;

    private int numPoints = 500;

    private double[] points = null;

    public LeastSquaresCrossValidation(double[] x)throws Exception{
        this.kernel = new GaussianKernel();
        this.x = x;
        computeBounds();
    }

    private void computeBounds() throws Exception{
        StandardDeviation stdev = new StandardDeviation();
        this.sd = stdev.evaluate(x);
        Min min = new Min();
        double from = min.evaluate(x);
        Max max = new Max();
        double to = max.evaluate(x);

    }

    public double[] density(double bandwidth){
        double[] density = new double[numPoints];
        int sampleSize = x.length;
        double z = 0.0;
        for(int i=0; i<numPoints;i++){
            for(int j=0;j<sampleSize;j++){
                z=(points[i]-x[j])/bandwidth;
                density[i] += kernel.value(z);
            }
            density[i] /= (sampleSize*bandwidth);
        }
        return density;
    }

    public double[] density(double bandwidth, int index){
        double[] density = new double[numPoints];
        int sampleSize = x.length;
        double z = 0.0;
        for(int i=0; i<numPoints;i++){
            for(int j=0;j<sampleSize;j++){
                if(j!=index){
                    z=(points[i]-x[j])/bandwidth;
                    density[i] += kernel.value(z);
                }
            }
            density[i] /= (sampleSize*bandwidth);
        }
        return density;
    }

    public double leaveOutDensityAt(double bandwidth, int index){
        int n = x.length;
        int nM1 = n-1;
        double z = 0.0;
        double sum = 0.0;
        for(int i=0;i<n;i++){
            if(i!=index){
                z=(x[index]-x[i])/bandwidth;
                sum += kernel.value(z);
            }
        }
        sum /= (nM1*bandwidth);
        return sum;
    }

    public double cvSum(double bandwidth){
        double sum = 0.0;
        int n = x.length;
        for(int i=0;i<n;i++){
            sum += leaveOutDensityAt(bandwidth, i);
        }
        sum = 2.0*sum/n;
        return sum;
    }

    public double squaredIntegral(){
        return 0;
    }

    public double value(double h){
        return 1.0;
    }


//    private double gaussianConvolution(double u, double h){
//        double var = 2.0;
//        double c = 1.0/Math.sqrt(2.0*Math.PI*var);
//        double prob = c*Math.exp(-u*u/(2.0*var));
//        return prob;
//    }

//    /**
//     * See equations 3.37 and 3.38 on page 50 in
//     * Silverman B. W. (1986) Density Estimation for statistics and data analysis.
//     * Boca Raton, FL: Chapman Hall.
//     *
//     * final computation and returned evaluate is equation 3.35 page 49 in Silverman.
//     *
//     * @param h
//     * @return
//     */
//    public double evaluate(double h){
//        double N = (double)n;
//        double sum1 = 0.0;
//        double sum2 = 0.0;
//        for(int i=0;i<n;i++){
//            for(int j=0;j<n;j++){
//                sum1 += gaussianConvolution((x[j]-x[i])/h, h);
//                sum2 += kernel.evaluate((x[i]-x[j])/h)/h;
//            }
//        }
//        double crossValidation = sum2/(N*(N-1.0))-kernel.evaluate(0)/((N-1.0)*h);
//        double integralF2 = sum1/(N*N*h);
//        double cvh = integralF2-2.0*crossValidation;
//        return cvh;
//    }

    public double value() {
        BrentOptimizer brent = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair pair;
        try{
            pair = brent.optimize(400, this, GoalType.MINIMIZE, 0.01, sd);
        }catch(Exception ex){
            return Double.NaN;
        }
        return pair.getPoint();
    }

    public double getAdjustmentFactor(){
        return 1.0;
    }

}

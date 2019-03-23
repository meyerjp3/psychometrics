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
import org.apache.commons.math3.exception.DimensionMismatchException;

public class LocalLinearRegression {

    private double[] points = null;

    private KernelFunction kernel = null;

    private double bandwidth = 1.0;

    private int n = 0;

    private int numPoints = 50;

    public LocalLinearRegression(KernelFunction kernel, Bandwidth bandwidth, UniformQuadratureRule uniform){
        this.kernel = kernel;
        this.bandwidth = bandwidth.value();
        this.points = uniform.getPoints();
        this.numPoints = uniform.getNumberOfPoints();
    }

    public double[] evaluate(double[] x, double[] y)throws DimensionMismatchException{
        if(x.length!=y.length) throw new DimensionMismatchException(x.length, y.length);
        n = x.length;
        double[] yprime = new double[n];
        double d = 0;
        double kernD = 0;
        double s0=0, s1=0, s2=0;

        for(int j=0;j<numPoints;j++){
            for(int i=0;i<n;i++){
                d = x[i]-points[j];
                kernD = kernel.value(d/bandwidth);
                s0 += kernD;
                s1 += d*kernD;
                s2 += d*d*kernD;
            }
            s0 /= n;
            s1 /= n;
            s2 /= n;

            for(int i=0;i<n;i++){
                yprime[j] += ((s2-s1*d)*kernD*y[i])/(s2*s0-Math.pow(s1,2));
            }
            yprime[j] /=n;
        }
        return yprime;
    }

    public double[] evaluate(double[] x, byte[] y)throws DimensionMismatchException{
        if(x.length!=y.length) throw new DimensionMismatchException(x.length, y.length);
        n = x.length;
        double[] yprime = new double[n];
        double d = 0;
        double kernD = 0;
        double s0=0, s1=0, s2=0;

        for(int j=0;j<numPoints;j++){
            for(int i=0;i<n;i++){
                d = x[i]-points[j];
                kernD = kernel.value(d/bandwidth);
                s0 += kernD;
                s1 += d*kernD;
                s2 += d*d*kernD;
            }
            s0 /= n;
            s1 /= n;
            s2 /= n;

            for(int i=0;i<n;i++){
                yprime[j] += ((s2-s1*d)*kernD*y[i])/(s2*s0-Math.pow(s1,2));
            }
            yprime[j] /=n;
        }
        return yprime;
    }

    public double[] getPoints(){
        return points;
    }

    public double getBandwidth(){
        return bandwidth;
    }

    public double getSampleSize(){
        return n;
    }

}

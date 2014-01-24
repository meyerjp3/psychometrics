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

import com.itemanalysis.psychometrics.distribution.UniformDistributionApproximation;

/**
 *
 * Nadaraya-Watson kernel regression estimator. Data values are not
 * stored in this object. Only the evaluation getPoints (x-values) and their
 * corresponding y-values are stored.
 *
 */
public class KernelRegression{

    private double[] points = null;

    private double[] numerator = null;

    private double[] denominator = null;

    private KernelFunction kernel = null;

    private double bandwidth = 1.0;

    private int n = 0;

    private int numPoints = 50;

    public KernelRegression(KernelFunction kernel, Bandwidth bandwidth, UniformDistributionApproximation uniform){
        this.kernel = kernel;
        this.bandwidth = bandwidth.value();
        this.points = uniform.getPoints();
        this.numPoints = uniform.getNumberOfPoints();
        numerator = new double[numPoints];
        denominator = new double[numPoints];
    }
    
    public void increment(double x, double y){
        double k = 0.0;
        n+=1;
        for(int i=0;i<numPoints;i++){
            k = kernel.value((x-points[i])/bandwidth);
            numerator[i]+=k*y;
            denominator[i]+=k;
        }
    }

    public double[] value(){
		double[] v = new double[numPoints];
		for(int i=0;i<v.length;i++){
            v[i] = numerator[i]/denominator[i];
		}
		return v;
	}

    public double[] getPoints(){
        return points;
    }

    public double getBandwidth(){
        return bandwidth;
    }

    public int getSampleSize(){
        return n;
    }


}

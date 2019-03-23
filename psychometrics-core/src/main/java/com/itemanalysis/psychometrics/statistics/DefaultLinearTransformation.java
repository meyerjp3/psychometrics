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
package com.itemanalysis.psychometrics.statistics;

import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer
 */
public class DefaultLinearTransformation implements LinearTransformation{

    private double scale = 1.0;

    private double intercept = 0.0;

    public DefaultLinearTransformation(Double oldMean, Double newMean, Double oldSd, Double newSd){
        this(oldMean, newMean, oldSd, newSd, LinearTransformationType.MEAN_STANDARD_DEVIATION);
    }

    /**
     * Constructor for two different ways of creating the transformation coefficients.
     * When linearTransformationType==MEAN_STANDARD_DEVIATION the transformation uses the new and old mean
     * and standard deviation to compute the coefficients. When linearTransformationType==MIN_MAX the transformation
     * uses the new and old min and max values t compute the transformation coefficients.
     *
     * The new mean and new standard deviation are the desired mean and standard deviation of the scale. Values
     * will be transformed to have this mean and standard deviation. Likewise, the new min and new max are the
     * desired minimum and desired maximum values of the scale. Original values will be transformed to have these
     * min and max values.
     *
     * @param oldMeanOrMin either the old mean or the old minimum
     * @param newMeanOrMin either the new mean or the new minimum
     * @param oldSdOrMax either the old standard deviation or the old maximum
     * @param newSdOrMax either the new standard deviation or the new maximum
     * @param linearTransformationType method of computing the linear transformation
     */
    public DefaultLinearTransformation(double oldMeanOrMin, double newMeanOrMin, double oldSdOrMax, double newSdOrMax, LinearTransformationType linearTransformationType){
        if(LinearTransformationType.MEAN_STANDARD_DEVIATION==linearTransformationType){
            transformFromMeanSd(oldMeanOrMin, newMeanOrMin, oldSdOrMax, newSdOrMax);

        }else if(LinearTransformationType.MIN_MAX==linearTransformationType){
            transformFromMinMax(oldMeanOrMin, newMeanOrMin, oldSdOrMax, newSdOrMax);
        }
    }

    private void transformFromMeanSd(double oldMeanOrMin, double newMeanOrMin, double oldSdOrMax, double newSdOrMax){
        scale = newSdOrMax/oldSdOrMax;
        intercept = newMeanOrMin - scale*oldMeanOrMin;
    }

    private void transformFromMinMax(double oldMeanOrMin, double newMeanOrMin, double oldSdOrMax, double newSdOrMax){
        scale = (newSdOrMax-newMeanOrMin)/(oldSdOrMax-oldMeanOrMin);
        intercept = newMeanOrMin - oldMeanOrMin*scale;
    }

    public DefaultLinearTransformation(double intercept, double scale){
        this.intercept = intercept;
        this.scale = scale;
    }

    public DefaultLinearTransformation(){
        this(0.0, 1.0);
    }

    public void setScale(double scale){
        this.scale = scale;
    }

    public void setScaleAndIntercept(double oldMean, double newMean, double oldSd, double newSd){
        scale = newSd/oldSd;
        intercept = newMean - scale*oldMean;
    }

    public void setIntercept(double intercept){
        this.intercept = intercept;
    }
    
    public double getIntercept(){
        return intercept;
    }

    public double getScale(){
        return scale;
    }

    public double transform(double x){
        return x*scale+intercept;

    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%23s", "Linear Transformation: ");
        f.format("%4s",  "Y = ");
        f.format("%.2f", scale);
        f.format("%4s", "X + ");
        f.format("%.2f", intercept);
        f.format("%n");
        return f.toString();
    }

}

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
package com.itemanalysis.psychometrics.scaling;

import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer
 */
public class DefaultLinearTransformation implements LinearTransformation{

    private double scale = 1.0;

    private double intercept = 0.0;

    public DefaultLinearTransformation(Double oldMean, Double newMean, Double oldSd, Double newSd){
        if(oldSd==0.0){
            scale = Double.NaN;
            intercept = Double.NaN;
        }else{
            scale = newSd/oldSd;
            intercept = newMean - scale*oldMean;
        }
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

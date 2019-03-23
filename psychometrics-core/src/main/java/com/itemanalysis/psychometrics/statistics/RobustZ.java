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

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 *
 * @author J. Patrick Meyer
 */
public class RobustZ {
    
    private double z = Double.NaN;
    
    private double pvalue = 0.0;

    private String id = "";

    public RobustZ(double x, double median, double iqr){
        compute(x, median, iqr);
    }

    public void setId(String id){
        this.id = id;
    }

    private void compute(double x, double median, double iqr){
        NormalDistribution normal = new NormalDistribution();
        if(iqr>0.0){
            z = (x - median)/(0.74*iqr);
            pvalue = normal.cumulativeProbability(z);
            if(z>0.0){
                pvalue = 1.0-pvalue;
            }
        }
    }

    public double getRobustZ(){
        return z;
    }

    public double getPvalue(){
        return pvalue;
    }

    public boolean significant(double significanceLevel){
        return pvalue<=significanceLevel;
    }

    public String getId(){
        return id;
    }

}

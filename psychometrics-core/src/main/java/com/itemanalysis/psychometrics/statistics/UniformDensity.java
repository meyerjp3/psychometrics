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

/**
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class UniformDensity {

    public UniformDensity(){

    }

    @Deprecated
    public double[][] value(double min, double max, int numPoints){
        numPoints = Math.max(1, numPoints);
        double[][] density = new double[numPoints][numPoints];
		double increment=(max-min)/(numPoints-1);
		double val=min;
		for(int i=0;i<numPoints;i++){
            density[i][0] = val;
            density[i][1] = 1/numPoints;
			val+=increment;
		}
        return density;
	}

}

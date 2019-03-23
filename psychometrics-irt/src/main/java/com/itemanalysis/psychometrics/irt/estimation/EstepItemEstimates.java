/**
 * Copyright 2014 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.irt.estimation;

/**
 * Stores the count of the expected number of responses to category k for item j.
 * For k = 0, 1, 2, ..., ncat categories and t = 0, 1, 2, ..., nPoints quadrature points.
 *
 */
public class EstepItemEstimates {

    private int ncat = 2;
    private int nPoints = 0;
    private double[][] rjkt = null;

    public EstepItemEstimates(int ncat, int nPoints){
        this.ncat = ncat;
        this.nPoints = nPoints;
        rjkt = new double[ncat][nPoints];
    }

//    public int getNumberOfCategories(){
//        return ncat;
//    }

    public void incrementRjkt(int k, int t, double value){
        rjkt[k][t]+=value;
    }

//    public void incrementRj(double[][] value){
//        for(int k=0;k<value.length;k++){
//            for(int t=0;t<value[0].length;t++){
//                rjkt[k][t] += value[k][t];
//            }
//        }
//
//    }

//    public double[][] getRjkt(){
//        return rjkt;
//    }

    public double getRjktAt(int k, int t){
        return rjkt[k][t];
    }




}

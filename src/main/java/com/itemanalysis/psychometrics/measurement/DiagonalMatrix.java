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
package com.itemanalysis.psychometrics.measurement;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class DiagonalMatrix  extends Array2DRowRealMatrix {

    int cols = 0;
    int rows = 0;

    public DiagonalMatrix(double[] x){
        super(x.length, x.length);
        this.cols = x.length;
        this.rows = x.length;
//        data = new double[cols][cols];
        setMatrix(x);
    }

    public void setMatrix(double[] x){
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                if(i==j) this.setEntry(i, j, x[i]);
                else this.setEntry(i, j, 0.0);
            }
        }
    }

}

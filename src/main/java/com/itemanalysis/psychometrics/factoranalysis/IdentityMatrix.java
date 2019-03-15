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
package com.itemanalysis.psychometrics.factoranalysis;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class IdentityMatrix extends Array2DRowRealMatrix {

    int cols = 0;
    int rows = 0;

    public IdentityMatrix(int cols){
        super(cols, cols);
        this.cols = cols;
        this.rows = cols;
//        data = new double[cols][cols];
        setMatrix();
    }

    public void setMatrix(){
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                if(i==j) this.setEntry(i, j, 1.0);
                else this.setEntry(i, j, 0.0);
            }
        }
    }



}

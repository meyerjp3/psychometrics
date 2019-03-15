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
public class IdentityVector extends Array2DRowRealMatrix {

    int cols = 1;
    int rows = 0;

    public IdentityVector(int rows){
        super(rows, 1);
        this.cols = 1;
        this.rows = rows;
//        data = new double[rows][1];
        setMatrix();
    }

    public void setMatrix(){
        for(int i=0;i<rows;i++){
            this.setEntry(i, 0, 1.0);
        }
    }

}

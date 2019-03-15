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
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class MatrixToVector extends Array2DRowRealMatrix {
    
    private RealMatrix realVector = null;
    
    private RealMatrix matrix = null;
    
    int numEelements = 0;

    public MatrixToVector(RealMatrix matrix){
        this.matrix = matrix;
        numEelements = (int)((matrix.getColumnDimension()*(matrix.getColumnDimension()+1))/2.0);
        realVector = new Array2DRowRealMatrix(numEelements, 1);
    }

    public RealMatrix value(){
        for(int i=0;i<matrix.getRowDimension();i++){
            for(int j=0;j<matrix.getColumnDimension();j++){
                if(j<=i){
                    realVector.setEntry(i, 0, matrix.getEntry(i, j));
                }
            }
        }
        return realVector;
    }

}

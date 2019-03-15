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
 * A diagonal matrix. Off-diagonal elements are set to zero.
 *
 * @author J. Patrick Meyer
 */
public class DiagonalMatrix  extends Array2DRowRealMatrix {

    int cols = 0;
    int rows = 0;

    /**
     * Creates a matrix with diagonal elements set to x. Off diagonal elements are zero.
     * @param x diagonal values of the matrix
     */
    public DiagonalMatrix(double[] x){
        super(x.length, x.length);
        this.cols = x.length;
        this.rows = x.length;
        setMatrix(x);
    }

    /**
     * Changes the diagonal of the matrix to the values in x.
     *
     * @param x diagonal values of the matrix
     */
    public void setMatrix(double[] x){//TODO check that x.length==cols
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                if(i==j) this.setEntry(i, j, x[i]);
                else this.setEntry(i, j, 0.0);
            }
        }
    }

    /**
     * Extracts the diagonal elements from a matrix.
     *
     * @param matrix a matrix from which teh diagonal elements are extracted.
     */
    public DiagonalMatrix(RealMatrix matrix){
        super(matrix.getColumnDimension(), matrix.getColumnDimension());
        int ncol = matrix.getColumnDimension();
        for(int i=0;i<ncol;i++){
            for(int j=0;j<ncol;j++){
                if(i==j){
                    this.setEntry(i,j,matrix.getEntry(i,j));
                }else{
                    this.setEntry(i,j, 0.0);
                }
            }
        }


    }

}

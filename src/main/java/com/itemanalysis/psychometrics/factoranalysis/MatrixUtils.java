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
package com.itemanalysis.psychometrics.factoranalysis;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public class MatrixUtils {


    /**
     * Elementwise multiplication of elements in two arrays. This is equivalent to
     * using A*B in R when both A and B are matrices.
     *
     * @param A a matrix
     * @param B a matrix of the same dimension as A
     * @return a matrix with elements that are the produce of elements in A and B.
     * @throws org.apache.commons.math3.exception.DimensionMismatchException
     */
    public static RealMatrix multiplyElements(RealMatrix A, RealMatrix B)throws DimensionMismatchException {
        int nrow = A.getRowDimension();
        int ncol = A.getColumnDimension();
        if(nrow!= B.getRowDimension()){
            throw new DimensionMismatchException(nrow, B.getRowDimension());
        }
        if(ncol!= B.getColumnDimension()){
            throw new DimensionMismatchException(ncol, B.getColumnDimension());
        }

        RealMatrix M = new Array2DRowRealMatrix(nrow, ncol);
        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                M.setEntry(i,j, A.getEntry(i,j)*B.getEntry(i,j));
            }
        }
        return M;
    }

    /**
     * Elementwise multiplication of two matrices.
     *
     * @param A a matrix that is multiplied by the elements of B
     * @param B another matrix
     * @throws DimensionMismatchException
     */
    public static void multiplyElementsBy(RealMatrix A, RealMatrix B)throws DimensionMismatchException {
        int nrow = A.getRowDimension();
        int ncol = A.getColumnDimension();
        if(nrow!= B.getRowDimension()){
            throw new DimensionMismatchException(nrow, B.getRowDimension());
        }
        if(ncol!= B.getColumnDimension()){
            throw new DimensionMismatchException(ncol, B.getColumnDimension());
        }

        RealMatrix M = new Array2DRowRealMatrix(nrow, ncol);
        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                A.multiplyEntry(i,j,B.getEntry(i,j));
            }
        }
    }

    /**
     * Creates a vector (1 X size matrix) with all elements set to value.
     *
     * @param size size of vector
     * @param value initial value of elements.
     */
//    public static RealMatrix getVector(int size, double value){
//        RealMatrix vec = new Array2DRowRealMatrix(size, 1);
//        for(int i=0;i<size;i++){
//            vec.setEntry(i,0,value);
//        }
//        return vec;
//    }

    /**
     * Creates a vector (1 X size matrix) with all elements set to value.
     *
     * @param size
     * @param value
     * @return
     */
    public static RealMatrix getVector(int size, double value){
        RealMatrix vec = new Array2DRowRealMatrix(1, size);
        for(int i=0;i<size;i++){
            vec.setEntry(0,i,value);
        }
        return vec;
    }

    public static double sumMatrix(RealMatrix X){
        double sum = 0.0;
        for(int i=0;i<X.getRowDimension();i++){
            for(int j=0;j<X.getColumnDimension();j++){
                sum += X.getEntry(i,j);
            }
        }
        return sum;
    }

}

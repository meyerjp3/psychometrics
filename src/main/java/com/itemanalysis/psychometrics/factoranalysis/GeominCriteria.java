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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.RealMatrix;

public class GeominCriteria implements RotationCriteria {

    private double delta = 0.01;
    private double functionValue = 0;
    private RealMatrix gradient = null;

    public GeominCriteria(){

    }

    public GeominCriteria(double delta){
        this.delta = delta;
    }

    public double getValue(){
        return functionValue;
    }

    public RealMatrix getGradient(){
        return gradient;
    }

    public void computeValues(RealMatrix L){
//        vgQ.geomin <- function(L, delta=.01){
//            k <- ncol(L)
//            p <- nrow(L)
//            L2 <- L^2 + delta
//            pro <- exp(rowSums(log(L2))/k)
//            list(Gq=(2/k)*(L/L2)*matrix(rep(pro,k),p),
//                    f= sum(pro),
//                    Method="Geomin")
//        }

        int p = L.getRowDimension();
        int k = L.getColumnDimension();
        final RealMatrix L2 = MatrixUtils.multiplyElements(L,L).scalarAdd(delta);
        final double[] rowSums = new double[p];

        L2.walkInRowOrder(new DefaultRealMatrixPreservingVisitor(){
            @Override
            public void visit(int row, int column, double value) {
                rowSums[row] += Math.log(value);
            }
        });

        double sum = 0.0;
        for(int i=0;i<p;i++){
            rowSums[i] = Math.exp(rowSums[i]/(double)k);
            sum += rowSums[i];
        }
        functionValue = sum;

        final RealMatrix M = new Array2DRowRealMatrix(p, k);
        M.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
            @Override
            public double visit(int row, int column, double value) {
                return rowSums[row];
            }
        });

        final double c = (2.0/(double)k);
        gradient = L.copy();
        gradient.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
            @Override
            public double visit(int row, int column, double value) {
                return c*(value/L2.getEntry(row, column))*M.getEntry(row, column);
            }
        });


    }

}

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
import org.apache.commons.math3.linear.RealMatrix;

public class QuartiminCriteria implements RotationCriteria {

    private double functionValue = 0;
    private RealMatrix gradient = null;

    public QuartiminCriteria(){

    }

    public double getValue(){
        return functionValue;
    }

    public RealMatrix getGradient(){
        return gradient;
    }

    public void computeValues(RealMatrix L){
        int k = L.getColumnDimension();
        int p = L.getRowDimension();
        RealMatrix I = new IdentityMatrix(p);
        RealMatrix L2 = MatrixUtils.multiplyElements(L,L);

        RealMatrix N = new Array2DRowRealMatrix(k, k);
        N.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
            @Override
            public double visit(int row, int column, double value) {
                if(row==column) return 0.0;
                return 1.0;
            }
        });

        RealMatrix X = L2.multiply(N);
        double sum = MatrixUtils.sumMatrix(MatrixUtils.multiplyElements(L2,X));
        functionValue = sum/4.0;
        gradient = MatrixUtils.multiplyElements(L, X);

    }

}

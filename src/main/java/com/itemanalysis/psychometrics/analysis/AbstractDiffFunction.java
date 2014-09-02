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
package com.itemanalysis.psychometrics.analysis;

import com.itemanalysis.psychometrics.optimization.DiffFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Arrays;

public abstract class AbstractDiffFunction implements DiffFunction {

    private double EPSILON = 1e-8;

    public AbstractDiffFunction(){

    }

    public AbstractDiffFunction(double EPSILON){
        this.EPSILON = EPSILON;
    }

    public double machineEpsilon(){
        double dTemp = 0.5;
        double epsilon = 0.5;
        while(1+dTemp>1){
            dTemp /=2;
            epsilon = dTemp;
        }
        return epsilon*2.0;
    }

    public MultivariateVectorFunction gradient() {
        return new MultivariateVectorFunction() {
            public double[] value(double[] point){
                return gradient(point);
            }
        };
    }

    public MultivariateFunction partialDerivative(final int k) {
        return new MultivariateFunction() {
            public double value(double[] point){
                return gradient(point)[k];
            }
        };
    }

    public double value(double[] x){
        return valueAt(x);
    }

    /**
     * Numerically compute gradientAt by the central difference method. Override this method
     * when the analytic gradientAt is available.
     *
     *
     * @param x
     * @return
     */
    public double[] gradient(double[] x){
        int n = x.length;
        double[] grd = new double[n];
        double[] u1 = new double[n];
        double[] u2 = new double[n];
        double f1 = 0.0;
        double f2 = 0.0;
        double stepSize = 0.0001;

        for(int i=0;i<n;i++){
            u1 = Arrays.copyOfRange(x, 0, x.length);
            u2 = Arrays.copyOfRange(x, 0, x.length);

//            stepSize = Math.sqrt(EPSILON)*(Math.abs(x[i])+1.0);//from SAS manual on nlp procedure
            u1[i] = (x[i] + stepSize);
            u2[i] = (x[i] - stepSize);

            f1 = value(u1);
            f2 = value(u2);
            grd[i] = (f1-f2)/(2.0*stepSize);
        }
        return grd;
    }

    public double[] derivativeAt(double[] x){
        return gradient(x);
    }

    /**
     * Numerically compute Hessian using a finite difference method. Override this
     * method when the analytic Hessian is available.
     *
     * @param x
     * @return
     */
    public RealMatrix hessianAt(double[] x){
        int n = x.length;
        double[][] hessian = new double[n][n];
        double[] gradientAtXpls = null;
        double[] gradientAtX = gradient(x);
        double xtemp = 0.0;
        double stepSize = 0.0001;

        for(int j=0;j<n;j++){
            stepSize = Math.sqrt(EPSILON)*(Math.abs(x[j])+1.0);//from SAS manual on nlp procedure
            xtemp = x[j];
            x[j] = xtemp + stepSize;
            double [] x_copy = Arrays.copyOfRange(x, 0, x.length);
            gradientAtXpls = gradient(x_copy);
            x[j] = xtemp;
            for(int i=0;i<n;i++){
                hessian[i][j] = (gradientAtXpls[i]-gradientAtX[i])/stepSize;
            }
        }
        RealMatrix m = new Array2DRowRealMatrix(hessian);
        return m;
    }

}

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

import com.itemanalysis.psychometrics.statistics.IdentityMatrix;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * This class is a translation of the GPArotation package in R. Alternative translations for
 * SAS, SPSS, matlab, Gauss, and Stata are available at http://www.stat.ucla.edu/research/gpa/.
 * <br /><br />
 * For more information see:
 * <p>
 * Bernaards, C.A., & Jennrich, R.I. (2005). Gradient Projection Algorithms and Software for Arbitrary <br />
 *  &nbsp;&nbsp;&nbsp;&nbsp;Rotation Criteria in Factor Analysis. Educational and Psychological <br />
 *  &nbsp;&nbsp;&nbsp;&nbsp;Measurement, 65 (5), 676-696.
 * </p>
 *
 */
public class GPArotation {

    private RotationMethod rotationMethod = RotationMethod.NONE;
    private RotationCriteria gpFunction = null;

    public GPArotation(){

    }

    /**
     * Users should call this method. It will decide whether to use
     * {@link #GPForth(org.apache.commons.math3.linear.RealMatrix, int, double)}
     * or {@link #GPFoblq(org.apache.commons.math3.linear.RealMatrix)} according to the type of rotation method
     * specified in the method call.
     *
     * @param A a matrix of unrotated, orthogonal factor loadings.
     * @param rotationMethod the type of rotation to conduct.
     * @param maxIter maximum number of iterations (e.g. maxIter = 1000)
     * @param eps convergence criterion (e.g. eps = 1e-5)
     * @return a matrix of rotated factor loadings.
     * @throws ConvergenceException
     */
    public RealMatrix rotate(RealMatrix A, RotationMethod rotationMethod, int maxIter, double eps)throws ConvergenceException{
        this.rotationMethod = rotationMethod;

        if(rotationMethod==RotationMethod.VARIMAX){
            gpFunction = new VarimaxCriteria();
            return GPForth(A, maxIter, eps);
        }else{
            //varimax is the default
            gpFunction = new VarimaxCriteria();
            return GPForth(A, maxIter, eps);
        }

//        return GPForth(A, maxIter, eps);
    }

    /**
     * Implement rotation using default values.
     *
     * @param A a matrix of unrotated, orthogonal factor loadings.
     * @param method the type of rotation to conduct.
     * @return a matrix of rotated factor loadings.
     * @throws ConvergenceException
     */
    public RealMatrix rotate(RealMatrix A, RotationMethod method)throws ConvergenceException{
        return rotate(A, method, 1000, 1e-5);
    }


    /**
     * Conducts orthogonal rotation of factor loadings.
     *
     * @param A matrix of orthogonal factor loadings
     * @return a matrix of rotated factor loadings.
     * @throws ConvergenceException
     */
    private RealMatrix GPForth(RealMatrix A, int maxIter, double eps)throws ConvergenceException{
        int nrow = A.getRowDimension();
        int ncol = A.getColumnDimension();

        RealMatrix Tmat = new IdentityMatrix(ncol);
        double alpha = 1;
        RealMatrix L = A.multiply(Tmat);

        gpFunction.computeValues(L);

        double f = gpFunction.getValue();
        RealMatrix VgQ = gpFunction.getGradient();
        RealMatrix G = A.transpose().multiply(VgQ);
        double VgQtF = gpFunction.getValue();
        RealMatrix VgQt = gpFunction.getGradient();
        RealMatrix Tmatt = null;

        int iter = 0;
        double s = eps+0.5;
        double s2 = 0;
        int innnerIter=11;

        while(iter< maxIter){
            RealMatrix M = Tmat.transpose().multiply(G);
            RealMatrix S = (M.add(M.transpose()));
            S = S.scalarMultiply(0.5);
            RealMatrix Gp = G.subtract(Tmat.multiply(S));
            s = Math.sqrt((Gp.transpose().multiply(Gp)).getTrace());
            s2 = Math.pow(s,2);

            if(s<eps) break;
            alpha *= 2.0;

            for(int j=0;j<innnerIter;j++){
                Gp = Gp.scalarMultiply(alpha);
                RealMatrix X = (Tmat.subtract(Gp));
                SingularValueDecomposition SVD = new SingularValueDecomposition(X);

                Tmatt = SVD.getU().multiply(SVD.getV().transpose());
                L = A.multiply(Tmatt);
                gpFunction.computeValues(L);
                VgQt = gpFunction.getGradient();
                VgQtF = gpFunction.getValue();

                if(VgQtF < f-0.5*s2*alpha){
                    break;
                }
                alpha /= 2.0;
            }

            Tmat = Tmatt;
            f = VgQtF;
            G = A.transpose().multiply(VgQt);
            iter++;
        }

        boolean convergence = s<eps;
        if(!convergence){
            System.out.println("Convergence not reaced: " + s + " iter: " + iter);
            throw new ConvergenceException();
        }

        return L;

    }

    private RealMatrix GPFoblq(RealMatrix A)throws ConvergenceException{
        //empty method
        return new Array2DRowRealMatrix(2,2);
    }

    /**
     * For debugging
     *
     * @param x a matrix to print
     * @param title title for output
     */
    private void printMatrix(RealMatrix x, String title){
        System.out.println("PRINTING MATRIX: " + title);
        for(int i=0;i<x.getRowDimension();i++){
            for(int j=0;j<x.getColumnDimension();j++){
                System.out.print(x.getEntry(i,j) + "  ");
            }
            System.out.println();
        }
    }



}

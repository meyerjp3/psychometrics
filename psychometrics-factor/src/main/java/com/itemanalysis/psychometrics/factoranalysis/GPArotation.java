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

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.linear.*;

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

    private RotationMethod rotationMethod = RotationMethod.OBLIMIN;
    private RotationCriteria gpFunction = null;

    public GPArotation(){

    }

    /**
     * Users should call this method. It will decide whether to use
     * {@link #GPForth(org.apache.commons.math3.linear.RealMatrix, boolean, int, double)}
     * or {@link #GPFoblq(org.apache.commons.math3.linear.RealMatrix, boolean, int, double)} )}
     * according to the type of rotation method specified in the method call.
     *
     * @param A a matrix of unrotated, orthogonal factor loadings.
     * @param rotationMethod the type of rotation to conduct.
     * @param maxIter maximum number of iterations (e.g. maxIter = 1000)
     * @param eps convergence criterion (e.g. eps = 1e-5)
     * @return a matrix of rotated factor loadings.
     * @throws ConvergenceException
     */
    public RotationResults rotate(RealMatrix A, RotationMethod rotationMethod, boolean normalize, int maxIter, double eps)throws ConvergenceException{
        this.rotationMethod = rotationMethod;

        if(rotationMethod==RotationMethod.VARIMAX){
            gpFunction = new VarimaxCriteria();
            return GPForth(A, normalize, maxIter, eps);
        }else if(rotationMethod==RotationMethod.OBLIMIN){
            gpFunction = new ObliminCriteria();
            return GPFoblq(A, normalize, maxIter, eps);
        }else if(rotationMethod==RotationMethod.QUARTIMIN){
            gpFunction = new QuartiminCriteria();
            return GPFoblq(A, normalize, maxIter, eps);
        }else if(rotationMethod==RotationMethod.GEOMIN_T){
            gpFunction = new GeominCriteria();
            return GPForth(A, normalize, maxIter, eps);
        }else if(rotationMethod==RotationMethod.GEOMIN_Q){
            gpFunction = new GeominCriteria();
            return GPFoblq(A, normalize, maxIter, eps);
        }
        else{
            //oblimin is the default
            gpFunction = new ObliminCriteria();
            return GPFoblq(A, normalize, maxIter, eps);
        }

    }

    /**
     * Implement rotation using default values.
     *
     * @param A a matrix of unrotated, orthogonal factor loadings.
     * @param method the type of rotation to conduct.
     * @return a matrix of rotated factor loadings.
     * @throws ConvergenceException
     */
    public RotationResults rotate(RealMatrix A, RotationMethod method)throws ConvergenceException{
        return rotate(A, method, false, 1000, 1e-5);
    }


    /**
     * Conducts orthogonal rotation of factor loadings.
     *
     * @param A matrix of orthogonal factor loadings
     * @return a matrix of rotated factor loadings.
     * @throws ConvergenceException
     */
    private RotationResults GPForth(RealMatrix A, boolean normalize, int maxIter, double eps)throws ConvergenceException{
        int ncol = A.getColumnDimension();

        if(normalize){
            //elementwise division by normalizing weights
            final RealMatrix W = getNormalizingWeights(A, true);
            A.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
                @Override
                public double visit(int row, int column, double value) {
                    return value/W.getEntry(row, column);
                }
            });
        }

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
            throw new ConvergenceException();
        }

        if(normalize){
            //elementwise multiplication by normalizing weights
            final RealMatrix W = getNormalizingWeights(A, true);
            A.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
                @Override
                public double visit(int row, int column, double value) {
                    return value*W.getEntry(row, column);
                }
            });
        }

        RealMatrix Phi = Tmat.transpose().multiply(Tmat);
        RotationResults result = new RotationResults(gpFunction.getValue(), L, Phi, Tmat, rotationMethod);
        return result;

    }

    private RealMatrix randomStart(int ncol){
        NormalDistribution norm = new NormalDistribution(0.0, 1.0);
        RealMatrix T = new Array2DRowRealMatrix(ncol, ncol);
        for(int i=0;i<ncol;i++){
            for(int j=0;j<ncol;j++){
                T.setEntry(i,j,norm.sample());
            }
        }
        QRDecomposition qr = new QRDecomposition(T);
        return qr.getQ();
    }

    private RotationResults GPFoblq(RealMatrix A, boolean normalize, int maxIter, double eps)throws ConvergenceException{
        int ncol = A.getColumnDimension();

        RealMatrix Tinner = null;
        RealMatrix TinnerInv = null;
        RealMatrix Tmat = new IdentityMatrix(ncol);

        if(normalize){
            //elementwise division by normalizing weights
            final RealMatrix W = getNormalizingWeights(A, true);
            A.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
                @Override
                public double visit(int row, int column, double value) {
                    return value/W.getEntry(row, column);
                }
            });
        }


        RealMatrix TmatInv = new LUDecomposition(Tmat).getSolver().getInverse();
        RealMatrix L = A.multiply(TmatInv.transpose());

        //compute gradientAt and function value
        gpFunction.computeValues(L);
        RealMatrix VgQ = gpFunction.getGradient();
        RealMatrix VgQt = VgQ;
        double f = gpFunction.getValue();
        double ft = f;
        RealMatrix G = ((L.transpose().multiply(VgQ).multiply(TmatInv)).transpose()).scalarMultiply(-1.0);

        int iter = 0;
        double alpha = 1.0;
        double s = eps+0.5;
        double s2 = Math.pow(s,2);
        int innerMaxIter=10;
        int innerCount = 0;

        IdentityMatrix I = new IdentityMatrix(G.getRowDimension());
        RealMatrix V1 = MatrixUtils.getVector(ncol,1.0);

        while(iter< maxIter){
            RealMatrix M = MatrixUtils.multiplyElements(Tmat, G);
            RealMatrix diagP = new com.itemanalysis.psychometrics.factoranalysis.DiagonalMatrix(V1.multiply(M).getRow(0));
            RealMatrix Gp = G.subtract(Tmat.multiply(diagP));
            s = Math.sqrt(Gp.transpose().multiply(Gp).getTrace());
            s2 = Math.pow(s,2);

            if(s<eps){
                break;
            }
            alpha = 2.0*alpha;

            innerCount = 0;
            for(int i=0;i<innerMaxIter;i++){
                RealMatrix X = Tmat.subtract(Gp.scalarMultiply(alpha));
                RealMatrix X2 = MatrixUtils.multiplyElements(X,X);
                RealMatrix V = V1.multiply(X2);
                V.walkInRowOrder(new DefaultRealMatrixChangingVisitor() {
                    @Override
                    public double visit(int row, int column, double value) {
                        return 1.0 / Math.sqrt(value);
                    }
                });

                //compute new value of T, its inverse, and the rotated loadings
                RealMatrix diagV = new DiagonalMatrix(V.getRow(0));
                Tinner = X.multiply(diagV);
                TinnerInv = new LUDecomposition(Tinner).getSolver().getInverse();
                L = A.multiply(TinnerInv.transpose());

                //compute new values of the gradientAt and the rotation criteria
                gpFunction.computeValues(L);
                VgQt = gpFunction.getGradient();
                ft = gpFunction.getValue();

                innerCount++;
                if(ft < f - 0.5*s2*alpha){
                    break;
                }
                alpha = alpha/2.0;
            }

//            System.out.println(iter + "  " + f + "  " + s + "  " + Math.log10(s) + "  " + alpha + "  " + innerCount);

            Tmat = Tinner;
            f = ft;
            G = (L.transpose().multiply(VgQt).multiply(TinnerInv)).transpose().scalarMultiply(-1.0);
            iter++;
        }

        boolean convergence = s<eps;
        if(!convergence){
            throw new ConvergenceException();
        }

        if(normalize){
            //elementwise multiplication by normalizing weights
            final RealMatrix W = getNormalizingWeights(A, true);
            A.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
                @Override
                public double visit(int row, int column, double value) {
                    return value*W.getEntry(row, column);
                }
            });
        }

        RealMatrix Phi = Tmat.transpose().multiply(Tmat);
        RotationResults result = new RotationResults(gpFunction.getValue(), L, Phi, Tmat, rotationMethod);
        return result;

    }

    private RealMatrix getNormalizingWeights(RealMatrix A, boolean normalize){
        int nrow = A.getRowDimension();
        int ncol = A.getColumnDimension();
        final double[] w = new double[nrow];

        RealMatrix W = new Array2DRowRealMatrix(nrow, ncol);
        if(!normalize){
            W.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
                @Override
                public double visit(int row, int column, double value) {
                    return 1.0;
                }
            });
            return W;
        }

        //compute row sum of squared loadings
        A.walkInRowOrder(new DefaultRealMatrixPreservingVisitor(){
            @Override
            public void visit(int row, int column, double value) {
                w[row] += value*value;
            }
        });

        //compute normalizing weights for the matrix
        W.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
            @Override
            public double visit(int row, int column, double value) {
                return Math.sqrt(w[row]);
            }
        });
        return W;
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

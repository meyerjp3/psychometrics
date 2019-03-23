/*
 * Copyright 2018 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.distribution;

import com.itemanalysis.psychometrics.statistics.StreamingCovarianceMatrix;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.util.Precision;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Translation of MultivariateGaussianDistribution from the smile library,
 * see http://haifengl.github.io/smile/.
 * This version uses linear algebra classes from commons.math. It also corrects
 * an error in their code for the cdf() method.
 *
 *
 */
public class MultivariateNormalDistribution extends AbstractMultivariateDistribution implements MultivariateFunction {

    private static final double LOG2PIE = Math.log(2 * Math.PI * Math.E);
    private double[] mu;
    private RealMatrix sigma;
    boolean diagonal;
    private int dim=1;
    private RealMatrix sigmaInv;
    private RealMatrix sigmaL;
    private double sigmaDet;
    private double pdfConstant;
    private int numParameters;
    private NormalDistribution normalDistribution = null;
    private Random random = new SecureRandom();
    private double idfTargetProb = 0.0;

    /**
     * Constructor. The quadrature will have a diagonal covariance matrix of
     * the same variance.
     *
     * @param mean mean vector.
     * @param var variance.
     */
    public MultivariateNormalDistribution(double[] mean, double var) {
        if (var <= 0) {
            throw new IllegalArgumentException("Variance is not positive: " + var);
        }

        mu = new double[mean.length];
        sigma = zeroMatrix(mu.length, mu.length);
        for (int i = 0; i < mu.length; i++) {
            mu[i] = mean[i];
            sigma.setEntry(i, i, var);
        }

        diagonal = true;
        numParameters = mu.length + 1;

        init();
    }

    /**
     * Creates a standard multivariate normal quadrature with a diagonal covariance matrix
     * @param dim number of dimensions
     */
    public MultivariateNormalDistribution(int dim) {
        mu = new double[dim];
        sigma = zeroMatrix(dim, dim);

        for(int i=0;i<dim;i++){
            mu[i] = 0.0;
            sigma.setEntry(i,i, 1.0);
        }

        diagonal = true;
        numParameters = mu.length + 1;

        init();

    }

    private RealMatrix zeroMatrix(int row, int col){
        Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(row, col);
        for(int i=0;i<row;i++){
            for(int j=0;j<col;j++){
                matrix.setEntry(i, j, 0.0);
            }
        }
        return matrix;
    }

    /**
     * Constructor. The quadrature will have a diagonal covariance matrix.
     * Each element has different variance.
     *
     * @param mean mean vector.
     * @param var variance vector.
     */
    public MultivariateNormalDistribution(double[] mean, double[] var) {
        if (mean.length != var.length) {
            throw new IllegalArgumentException("Mean vector and covariance matrix have different dimension");
        }

        mu = new double[mean.length];
        sigma = new DiagonalMatrix(var);
        for (int i = 0; i < mu.length; i++) {
            if (var[i] <= 0) {
                throw new IllegalArgumentException("Variance is not positive: " + var[i]);
            }

            mu[i] = mean[i];
        }

        diagonal = true;
        numParameters = 2 * mu.length;

        init();
    }

    /**
     * Constructor.
     *
     * @param mean mean vector.
     * @param cov covariance matrix.
     */
    public MultivariateNormalDistribution(double[] mean, double[][] cov) {
        if (mean.length != cov.length) {
            throw new IllegalArgumentException("Mean vector and covariance matrix have different dimension");
        }

        mu = new double[mean.length];
        sigma = new Array2DRowRealMatrix(cov);
        for (int i = 0; i < mu.length; i++) {
            mu[i] = mean[i];
        }

        diagonal = false;
        numParameters = mu.length + mu.length * (mu.length + 1) / 2;

        init();
    }

    /**
     * Constructor. Mean and covariance will be estimated from the data by MLE.
     * @param data the training data.
     */
    public MultivariateNormalDistribution(double[][] data) {
        this(data, false);
    }

    /**
     * Constructor. Mean and covariance will be estimated from the data by MLE.
     * @param data the training data.
     * @param diagonal true if covariance matrix is diagonal.
     */
    public MultivariateNormalDistribution(double[][] data, boolean diagonal) {
        this.diagonal = diagonal;
        mu = colMeans(data);
        double temp = 0.0;

        if (diagonal) {
            sigma = (new StreamingCovarianceMatrix(data)).diagonalMatrixAsMatrix();
        } else {
            sigma = (new StreamingCovarianceMatrix(data)).valueAsMatrix();
        }

        numParameters = mu.length + mu.length * (mu.length + 1) / 2;

        init();
    }

    /**
     * Initialize the object.
     */
    private void init() {
        dim = mu.length;
        CholeskyDecomposition cholesky = new CholeskyDecomposition(sigma);
        DecompositionSolver solver = cholesky.getSolver();
        sigmaInv = solver.getInverse();
        sigmaDet = cholesky.getDeterminant();
        sigmaL = cholesky.getL();
        pdfConstant = (dim * Math.log(2 * Math.PI) + Math.log(sigmaDet)) / 2.0;
        normalDistribution = new NormalDistribution();
    }

    private double[] colMeans(double[][] data){
        int ncol = data[0].length;
        double[] m = new double[ncol];
        Mean[] mean = new Mean[ncol];

        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[0].length;j++){
                if(i==0) mean[j] = new Mean();
                mean[j].increment(data[i][j]);
            }
        }
        for(int j=0;j<data[0].length;j++){
            m[j] = mean[j].getResult();
        }

        return m;
    }

    /**
     * Returns true if the covariance matrix is diagonal.
     * @return true if the covariance matrix is diagonal
     */
    public boolean isDiagonal() {
        return diagonal;
    }

    @Override
    public int npara() {
        return numParameters;
    }

    @Override
    public double entropy() {
        return (dim * LOG2PIE + Math.log(sigmaDet)) / 2;
    }

    @Override
    public double[] mean() {
        return mu;
    }

    @Override
    public double[][] cov() {
        return sigma.getData();
    }

    /**
     * Returns the scatter of quadrature, which is defined as |&Sigma;|.
     */
    public double scatter() {
        return sigmaDet;
    }

    @Override
    public double logp(double[] x) {
        if (x.length != dim) {
            throw new IllegalArgumentException("Sample has different dimension.");
        }

        double[] v = x.clone();
        minus(v, mu);
        double result = xax(v) / -2.0;
        return result - pdfConstant;
    }

//    Returns x' * A * x.
    private double xax(double[] x){
        RealMatrix X = new Array2DRowRealMatrix(x);
        RealMatrix V = X.transpose().multiply(sigmaInv).multiply(X);
        return V.getEntry(0,0);
    }

    @Override
    public double pdf(double[] x) {
        return Math.exp(logp(x));
    }

    /**
     * Algorithm from Alan Genz (1992) Numerical Computation of
     * Multivariate Normal Probabilities, Journal of Computational and
     * Graphical Statistics, pp. 141-149.
     *
     * The difference between returned value and the true value of the
     * CDF is less than 0.001 in 99.9% time (assuming errMax is .001).
     *
     * Based on the cdf() function in the smile package, but it corrects an error
     * in their code. It also uses a different library for linear algebra.
     *
     * Multidimensional integral is computed from a to b.
     *
     * @param a lower bounds of integration
     * @param b upper bounds of integration
     * @param errMax absolute error
     * @param nMax maximum number of iterations
     * @return a probability
     */
    public double cdf(double[] a, double[] b, double errMax, int nMax){
        if (b.length != dim) {
            throw new IllegalArgumentException("Sample has different dimension.");
        }

        int N = 0;
        double intSum = 0.0;
        double delta = 0.0;
        double varSum = 0.0;
        double error = 2*errMax;
        double[] d = new double[dim];
        double[] e = new double[dim];
        double[] f = new double[dim];
        double[] y = new double[dim];

        double[] b2 = b.clone();
        minus(b2, mu);

        double[] a2 = a.clone();
        minus(a2, mu);

        double alpha = normalDistribution.inverseCumulativeProbability(1.0-errMax);
        d[0] = normalDistribution.cumulativeProbability(a2[0]/sigmaL.getEntry(0,0));
        e[0] = normalDistribution.cumulativeProbability(b2[0]/sigmaL.getEntry(0,0));
        f[0] = e[0]-d[0];

        for (N = 1; error > errMax && N <= nMax; N++) {
            double[] w = uniformRandom(dim - 1);

            for(int i=1;i<dim;i++){
                y[i-1] = normalDistribution.inverseCumulativeProbability(d[i-1] + w[i-1] * (e[i-1] - d[i-1]));

                double sumCY = 0.0;
                for(int j=0;j<i;j++){
                    sumCY += sigmaL.getEntry(i,j) * y[j];
                }
                d[i] = normalDistribution.cumulativeProbability((a2[i]-sumCY)/sigmaL.getEntry(i,i));
                e[i] = normalDistribution.cumulativeProbability((b2[i]-sumCY)/sigmaL.getEntry(i,i));
                f[i] = (e[i] - d[i]) * f[i-1];
            }

            delta = (f[dim - 1] - intSum) / N;
            intSum += delta;
            varSum = (N - 2) * varSum / N + delta * delta;
            error = alpha * Math.sqrt(varSum);

            //for debugging
//            System.out.println("Iter: " +N + "  delta = " + delta +  "  varSum = " + varSum + "  Error = " + error);
        }

        if(error>errMax) System.out.println("  MVN CDF failed to converge. Error = " + Precision.round(error, 8));
        return intSum;
    }

    /**
     * Shortcut to main cdf() function, where the lower bounds are assumed to be
     * negative infinity. For computation the lower bounds are set to -Double.MAX_VALUE.
     *
     * @param b upper bounds of integration
     * @param errMax absolute error
     * @param nMax maximum number of iteration
     * @return probability
     */
    public double cdf(double[] b, double errMax, int nMax){
        double[] a = new double[b.length];
        for(int i=0;i<a.length;i++){
            a[i] = -Double.MAX_VALUE;
        }
        return cdf(a, b, errMax, nMax);
    }

    /**
     * Another shortcut to the main cdf() function. IT only requires
     * the upper bounds of integration. It uses default values for
     * absolute error and maximum number of iterations.
     *
     * @param b upper bounds of integration
     * @return probability
     */
    public double cdf(double[] b){
        return cdf(b, 0.001, 10000*dim*dim);
    }

    public double cdf(double[] a, double[] b){
        return cdf(a, b, 0.001, 10000*dim*dim);
    }

    private double[] expandArray(double x, int size){
        double[] y = new double[size];
        for(int i=0;i<size;i++){
            y[i] = x;
        }
        return y;
    }

    /**
     * Finds the equi-coordinate quantile for p. Specifically, it finds the quantile x
     * such that P(X <= x) = p.
     *
     * It implements a modified bisection algorithm as done in Michael Grayling's
     * (mjg211 at cam.ac.uk) stata module.
     *
     * Grayling, Michael J. and Mander, Adrian, (2017), MVTNORM: Stata module to work with the multivariate
     * normal and multivariate t distributions, https://EconPapers.repec.org/RePEc:boc:bocode:s458043.
     *
     * @param prob probability
     * @param errMax absolute error tolerance
     * @param nMax maximum number of iterations
     * @return equi-coordinate quantile
     */
    public double idf(double prob, double errMax, int nMax){
        if(prob <= 0 || prob >= 1) throw new IllegalArgumentException("Probability must be strictly between 0 and 1.");

        //Find bounds of search
        double largestScale = sigmaL.getEntry(0,0);
        for(int i=1;i<dim;i++){
            largestScale = Math.min(largestScale, sigmaL.getEntry(i,i));
        }
        double sd = 6.0;
        double a = -sd*largestScale;
        double b = sd*largestScale;

        double c = 0.0;
        int iter = 0;

        double fa = 0.0;
        double fb = 0.0;
        double fc = 0.0;
        double mid = 0.0;

        fa = cdf(expandArray(a, dim))-prob;
        fb = cdf(expandArray(b, dim))-prob;

        while(iter<nMax){
            c = a - ((b - a)/(fb - fa))*fa;
            fc = cdf(expandArray(c, dim))-prob;
            mid = (b-a)/2.0;

            if(fc==0 || mid < errMax){
                break;
            }else{
                if((fa<0 && fc<0) || (fa>0 && fc>0)){
                    a = c;
                    fa = fc;
                }else {
                    b = c;
                    fb = fc;
                }
                iter++;
            }

        }

        if(mid > errMax) System.out.println("  MVN IDF failed to converge. Error = " + Precision.round(mid, 8));
        return c;

    }

    public double idf(double prob){
        return idf(prob, 0.001, 10000*dim*dim);
    }

    public double value(double[] thresholds){

        double[] c = thresholds.clone();
        for(int i=0;i<c.length;i++){
            c[i] = c[0];
        }

        double d = idfTargetProb-cdf(c);
        d *= d;
        return d;
    }

    /**
     * Generate n uniformRandom numbers in [0, 1).
     */
    private double[] uniformRandom(int n) {
        double[] x = new double[n];
        for(int i=0;i<n;i++){
            x[i] = random.nextDouble();
        }
        return x;
    }

    /**
     * Generate a uniformRandom multivariate Gaussian sample.
     */
    public double[] rand() {
        double[] spt = new double[mu.length];

        for (int i = 0; i < mu.length; i++) {
            double u, v, q;
            do {
                u = Math.random();
                v = 1.7156 * (Math.random() - 0.5);
                double x = u - 0.449871;
                double y = Math.abs(v) + 0.386595;
                q = x * x + y * (0.19600 * y - 0.25472 * x);
            } while (q > 0.27597 && (q > 0.27846 || v * v > -4 * Math.log(u) * u * u));

            spt[i] = v / u;
        }

        double[] pt = new double[sigmaL.getRowDimension()];

        // pt = sigmaL * spt
        for (int i = 0; i < pt.length; i++) {
            for (int j = 0; j <= i; j++) {
                pt[i] += sigmaL.getEntry(i, j) * spt[j];
            }
        }

        plus(pt, mu);

        return pt;
    }

    /**
     * Elementwise subtraction. x = x-y
     *
     * @param x value for x
     * @param y value for y
     */
    private void minus(double[] x, double[] y){
        for(int i=0;i<x.length;i++){
            x[i] -= y[i];
        }
    }

    /**
     * Elementwise addition x = x+y
     *
     * @param x value for x
     * @param y value for y
     */
    private void plus(double[] x, double[] y){
        for(int i=0;i<x.length;i++){
            x[i] += y[i];
        }
    }

//    @Override
//    public MultivariateMixture.Component M(double[][] x, double[] posteriori) {
//        int n = x[0].length;
//
//        double alpha = 0.0;
//        double[] mean = new double[n];
//        double[][] cov = new double[n][n];
//
//        for (int k = 0; k < x.length; k++) {
//            alpha += posteriori[k];
//            for (int i = 0; i < n; i++) {
//                mean[i] += x[k][i] * posteriori[k];
//            }
//        }
//
//        for (int i = 0; i < mean.length; i++) {
//            mean[i] /= alpha;
//        }
//
//        if (diagonal) {
//            for (int k = 0; k < x.length; k++) {
//                for (int i = 0; i < n; i++) {
//                    cov[i][i] += (x[k][i] - mean[i]) * (x[k][i] - mean[i]) * posteriori[k];
//                }
//            }
//
//            for (int i = 0; i < cov.length; i++) {
//                cov[i][i] /= alpha;
//            }
//        } else {
//            for (int k = 0; k < x.length; k++) {
//                for (int i = 0; i < n; i++) {
//                    for (int j = 0; j < n; j++) {
//                        cov[i][j] += (x[k][i] - mean[i]) * (x[k][j] - mean[j]) * posteriori[k];
//                    }
//                }
//            }
//
//            for (int i = 0; i < cov.length; i++) {
//                for (int j = 0; j < cov[i].length; j++) {
//                    cov[i][j] /= alpha;
//                }
//
//                // make sure the covariance matrix is positive definite.
//                cov[i][i] *= 1.00001;
//            }
//        }
//
//        MultivariateMixture.Component c = new MultivariateMixture.Component();
//        c.priori = alpha;
//        MultivariateGaussianDistribution g = new MultivariateGaussianDistribution(mean, cov);
//        g.diagonal = diagonal;
//        c.quadrature = g;
//
//        return c;
//    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Multivariate Gaussian Distribution:\nmu = [");
        for (int i = 0; i < mu.length; i++) {
            builder.append(mu[i]).append(" ");
        }
        builder.setCharAt(builder.length() - 1, ']');
        builder.append("\nSigma = [\n");
        for (int i = 0; i < sigma.getRowDimension(); i++) {
            builder.append('\t');
            for (int j = 0; j < sigma.getColumnDimension(); j++) {
                builder.append(sigma.getEntry(i, j)).append(" ");
            }
            builder.append('\n');
        }
        builder.append("\t]");
        return builder.toString();
    }

}

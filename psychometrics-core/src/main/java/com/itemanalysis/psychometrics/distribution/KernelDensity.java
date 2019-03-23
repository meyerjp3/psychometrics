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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Random;

/**
 * Kernel density estimator. This class is a translation of the R function density.default().
 * It uses Fast Fourier Transforms to estimate the density very quickly. Additional methods
 * allow for calling the pdf(), cdf(), and idf() and also for generating random numbers
 * with rand().
 *
 */
public class KernelDensity implements UnivariateDistribution {

    /**
     * Type of bandwidth calculation
     */
    public enum BandwidthType{
        BW_NRD{
            @Override
            public String toString() {
                return "bwnrd";
            }
        }, //Silverman's simple plugin bandwidth
        BW_NRD0{
            @Override
            public String toString() {
                return "bwnrd0";
            }
        } //Scott's plugin bandwidth. The default bandwidth here and in R
    }

    /**
     * The data
     */
    private double[] x;

    /**
     * Weights
     */
    private double[] w;

    /**
     * Number of evaluation points when estimating density
     */
    private int nPoints = 512;

    private DescriptiveStatistics stats = null;

    /**
     * Distribution mean
     */
    private double mean;

    /**
     * Distributin variance
     */
    private double variance;

    /**
     * Distribution standard deviation
     */
    private double sd;

    /**
     * Bandwidth type
     */
    private BandwidthType bandwidthType = BandwidthType.BW_NRD0;

    /**
     * Kernel type
     */
    private KernelType kernelType = KernelType.GAUSSIAN;

    /**
     * Bandwidth
     */
    private double h;

    /**
     * Bandwidth adjustment factor
     */
    private double adjust = 1;

    private double[] quadPoints = null;

    /**
     * Estimated Kernel Density function
     */
    private UnivariateFunction densityFunction = null;

    /**
     * For computing CDF
     */
    private UnivariateIntegrator integrator = new RombergIntegrator();

    /**
     * For computing IDF
     */
    private UnivariateSolver solver = new BisectionSolver();

    /**
     * For random number generating
     */
    private Random random = new Random();

    /**
     * Maximum number of function evaluations when computing integral in CDF
     * and when finding root in IDF.
     */
    public int maxEvaluations = 500000;

    /**
     * Constructor provides no options for creating the kernel density. Data points assumed to be
     * equaly weighted (1/N), where N is the sample size. Uses Scott's plugin rule as the default
     * bandwidth calculation. Kernel is estimated at 512 equally spaced points. No adjustment is
     * applied to the bandwidth.
     *
     * @param x an array of data values.
     */
    public KernelDensity(double[] x){
        this(x, 1.0, 512);
    }

    /**
     * Constructor provides few options for creating the kernel density. Data points assumed to be
     * equaly weighted (1/N), where N is the sample size. Uses Scott's plugin rule as the default
     * bandwidth calculation. Kernel is estimated at 512 equally spaced points.
     *
     * @param x an array of data values.
     * @param adjust an adjustment factor applied to the bandwidth.
     */
    public KernelDensity(double[] x, double adjust){
        this(x, adjust, 512);
    }

    /**
     * Constructor provides some options for creating the kernel density. Data points assumed to be
     * equaly weighted (1/N), where N is the sample size. Uses Scott's plugin rule as the default
     * bandwidth calculation.
     *
     * @param x an array of data values.
     * @param adjust an adjustment factor applied to the bandwidth.
     * @param nPoints number of evaluation points.
     */
    public KernelDensity(double[] x, double adjust, int nPoints){
        this.x = x;
        this.adjust = adjust;
        this.nPoints = nPoints;
        this.w = new double[x.length];
        Arrays.fill(w, 1.0/(double)x.length);
        initialize();
    }

    /**
     * Constructor provides some options for creating the kernel density. Data points assumed to be
     * equaly weighted (1/N), where N is the sample size. Uses Scott's plugin rule as the default
     * bandwidth calculation.
     *
     * @param x an array of data values.
     * @param adjust an adjustment factor applied to the bandwidth.
     * @param nPoints number of evaluation points.
     */
    public KernelDensity(double[] x, KernelType kernelType, double adjust, int nPoints){
        this.x = x;
        this.kernelType = kernelType;
        this.adjust = adjust;
        this.nPoints = nPoints;
        this.w = new double[x.length];
        Arrays.fill(w, 1.0/(double)x.length);
        initialize();
    }

    /**
     * Constructor provides many options for creating the kernel density. Data points assumed to be
     * equaly weighted (1/N), where N is the sample size.
     *
     * @param x an array of data values.
     * @param kernelType type of kernel.
     * @param bandwidthType type of bandwidth.
     * @param adjust an adjustment factor applied to the bandwidth.
     * @param nPoints number of evaluation points.
     */
    public KernelDensity(double[] x, KernelType kernelType, BandwidthType bandwidthType, double adjust, int nPoints){
        this.x = x;
        this.kernelType = kernelType;
        this.bandwidthType = bandwidthType;
        this.adjust = adjust;
        this.nPoints = nPoints;
        this.w = new double[x.length];
        Arrays.fill(w, 1.0/(double)x.length);
        initialize();
    }

    /**
     * Constructor provides the most options for creating the kernel density.
     *
     * @param x an array of data values.
     * @param kernelType type of kernel.
     * @param bandwidthType type of bandwidth.
     * @param adjust an adjustment factor applied to the bandwidth.
     * @param nPoints number of evaluation points.
     * @param maxEvaluations maximum number of funciton evaluations in computing integral in CDF and finding root in
     *                       IDF. The default is 500,000.
     */
    public KernelDensity(double[] x, KernelType kernelType, BandwidthType bandwidthType, double adjust, int nPoints, int maxEvaluations){
        this.x = x;
        this.kernelType = kernelType;
        this.bandwidthType = bandwidthType;
        this.adjust = adjust;
        this.nPoints = nPoints;
        this.maxEvaluations = maxEvaluations;
        this.w = new double[x.length];
        Arrays.fill(w, 1.0/(double)x.length);
        initialize();
    }

    /**
     * Constructor provides the most options for creating the kernel density.
     *
     * @param x an array of data values.
     * @param w an array of weights. Weights must sum to 1 for a proper density.
     * @param kernelType type of kernel.
     * @param bandwidthType type of bandwidth.
     * @param adjust an adjustment factor applied to the bandwidth.
     * @param nPoints number of evaluation points.
     * @param maxEvaluations maximum number of funciton evaluations in computing integral in CDF and finding root in
     *                       IDF. The default is 500,000.
     */
    public KernelDensity(double[] x, double[] w, KernelType kernelType, BandwidthType bandwidthType, double adjust, int nPoints, int maxEvaluations){
        if(x.length!=w.length) throw new IllegalArgumentException("Length of data not equal to length of weights");
        this.x = x;
        this.w = w;
        this.kernelType = kernelType;
        this.bandwidthType = bandwidthType;
        this.adjust = adjust;
        this.nPoints = nPoints;
        this.maxEvaluations = maxEvaluations;
        initialize();
    }

    private void initialize(){
        if(nPoints>512){
            double l2 = Math.log(nPoints)/Math.log(2);
            this.nPoints = (int)Math.pow(2, Math.ceil(l2));
        }

        computeBandwidth();
        density();
    }

    /**
     * Computes the bandwidth
     */
    private void computeBandwidth(){
        double n = (double)x.length;
        stats = new DescriptiveStatistics(x);
        stats.setPercentileImpl(new Percentile().withEstimationType(Percentile.EstimationType.R_7));//Use the same percentile method as R.
        double observedSd = stats.getStandardDeviation();
        double observedQ1 = stats.getPercentile(25);
        double observedQ3 = stats.getPercentile(75);
        double observedIqr = observedQ3-observedQ1;

        if(bandwidthType==BandwidthType.BW_NRD){
            //Scott's plugin bandwidth (bw.nrd in R)
            h = 1.06*Math.min(observedSd, observedIqr/1.34)*Math.pow(n, -1.0/5.0);
        }else{
            //Silverman's rule of thumb (bw.nrd0 is the default in R and the default here.)
            h = 0.9*Math.min(observedSd, observedIqr/1.34)*Math.pow(n, -1.0/5.0);
        }

        //apply adjustment factor
        h *= adjust;
    }

    /**
     * computes the kernel values
     *
     * @param kords values where kernel is to be computed.
     *
     * @return array of kernel values
     */
    private double[] computeKernel(double[] kords){
        double[] k = new double[kords.length];

        if(kernelType==KernelType.RECTANGULAR){
            double a = h*Math.sqrt(3);
            for(int i=0;i<kords.length;i++){
                double ax = Math.abs(kords[i]);
                if(ax<a){
                    k[i] = 0.5/a;
                }else{
                    k[i] = 0;
                }
            }
        }
        else if(kernelType==KernelType.TRIANGULAR){
            double a = h*Math.sqrt(6);
            for(int i=0;i<kords.length;i++){
                double ax = Math.abs(kords[i]);
                if(ax<a){
                    k[i] = (1.0 - ax/a)/a;
                }else{
                    k[i] = 0;
                }
            }
        }
        else if(kernelType==KernelType.EPANECHNIKOV){
            double a = h*Math.sqrt(5);
            for(int i=0;i<kords.length;i++){
                double ax = Math.abs(kords[i]);
                if(ax<a){
                    k[i] = 3.0/4.0*(1.0 - Math.pow(ax/a, 2))/a;
                }else{
                    k[i] = 0;
                }
            }
        }
        else if(kernelType==KernelType.BIWEIGHT){
            double a = h*Math.sqrt(7);
            for(int i=0;i<kords.length;i++){
                double ax = Math.abs(kords[i]);
                if(ax<a){
                    k[i] = 15.0/16.0*Math.pow((1.0 - Math.pow(ax/a, 2)), 2)/a;
                }else{
                    k[i] = 0;
                }
            }
        }
        else if(kernelType==KernelType.COSINE){
            double a = h/Math.sqrt(1.0/3.0 - 2.0/Math.pow(Math.PI,2));
            for(int i=0;i<kords.length;i++){
                double ax = Math.abs(kords[i]);
                if(ax<a){
                    k[i] = (1 + Math.cos(Math.PI*kords[i]/a))/(2.0 * a);
                }else{
                    k[i] = 0;
                }
            }
        }
        else if(kernelType==KernelType.OPTCOSINE){
            double a = h/Math.sqrt(1.0 - 8.0/Math.pow(Math.PI,2));
            for(int i=0;i<kords.length;i++){
                double ax = Math.abs(kords[i]);
                if(ax<a){
                    k[i] = Math.PI/4.0*Math.cos(Math.PI*kords[i]/(2*a))/a;
                }else{
                    k[i] = 0;
                }
            }
        }
        else{
            //Gaussian kernel is the default
            NormalDistribution normalDistribution = new NormalDistribution(0, h);
            for(int i=0;i<kords.length;i++){
                k[i] = normalDistribution.density(kords[i]);
            }
        }

        return k;
    }

    /**
     * Compute density. This is the primary method for estimating the density.
     */
    private void density(){
        double observedMin = stats.getMin();
        double observedMax = stats.getMax();
        double from = observedMin-3.0*h;
        double to = observedMax+3.0*h;
        double lo = from - 4.0*h;
        double up = to + 4.0*h;
        double step = (2*(up-lo))/(2*nPoints-1);
        double[] kords = new double[2*nPoints];

        for(int i=0;i<2*nPoints;i++){
            kords[i] = step*i;
        }

        for(int i=nPoints+1; i<2*nPoints; i++){
            kords[i] = -kords[2*nPoints-i];
        }

        kords = computeKernel(kords);

        double[] y = bindist(x, w, lo, up, nPoints);

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftY = fft.transform(y, TransformType.FORWARD);
        Complex[] fftOrd = fft.transform(kords, TransformType.FORWARD);
        Complex[] result = new Complex[fftY.length];
        for(int i=0;i<fftY.length;i++){
            result[i] = fftY[i].multiply(fftOrd[i].conjugate());
        }
        result = fft.transform(result, TransformType.INVERSE);

        //Final ordinates (y-axis)
        kords = new double[nPoints];
        for(int i=0;i<nPoints;i++){
            kords[i] = Math.max(0, result[i].getReal());
        }

        //Final grid Points (x-axis)
        double[] xords = new double[nPoints];
        step = (up-lo)/(nPoints-1);
        double step2 = (to-from)/(nPoints-1);

        quadPoints = new double[nPoints];
        for(int i=0;i<nPoints;i++){
            xords[i] = lo+step*i;
            quadPoints[i] = from+step2*i;
        }

        //Use linear interpolation for final density
        UnivariateInterpolator interpolator = new LinearInterpolator();
        densityFunction = interpolator.interpolate(xords, kords);

    }

    public int numberOfParameters(){
        return 0;
    }

    public double bandwidth(){
        return h;
    }

    public double mean(){
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return (x*densityFunction.value(x));
            }
        };

        return integrator.integrate(5000, f, quadPoints[0]-3*h, quadPoints[nPoints-1]+3*h);
    }

    public double variance(){
        final double dMean = mean();

        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                return Math.pow(x-dMean,2)*densityFunction.value(x);
            }
        };

        return integrator.integrate(5000, f, quadPoints[0]-3*h, quadPoints[nPoints-1]+3*h);
    }

    public double sd(){
        return Math.sqrt(variance());
    }

    public double pdf(double x){
        if(x<quadPoints[0]-3*h) return 0;
        if(x>quadPoints[nPoints-1]+3*h) return 0;
        return densityFunction.value(x);
    }

    public double cdf(double x){
        if(x<=quadPoints[0]) return 0.0;
        if(x>=quadPoints[nPoints-1]) return 1.0;
        double prob = integrator.integrate(500000, densityFunction, quadPoints[0], x);
        return Math.min(prob, 1.0);
    }

    public double idf(final double p){
        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double x) {
                double v = p-cdf(x);
                return v;
            }
        };

        return solver.solve(5000, f, quadPoints[0], quadPoints[nPoints-1]);
    }

    public double rand(){
        return idf(random.nextDouble());
    }

    public double logp(double x){
        return Math.log(densityFunction.value(x));
    }

    public double getSupportLowerBound(){
        return Double.NEGATIVE_INFINITY;
    }

    public double getSupportUpperBound(){
        return Double.POSITIVE_INFINITY;
    }

    public double likelihood(double[] x){
        double v = 1;
        for(int i=0;i<x.length;i++){
            v *= pdf(x[i]);
        }
        return v;
    }

    public double loglikelihood(double[] x){
        double v = 0;
        for(int i=0;i<x.length;i++){
            v += Math.log(pdf(x[i]));
        }
        return v;
    }


    /**
     * From R.
     * Notes:
     * User provided weights:
     *   weights <- weights[x.finite]
     *   totMass <- sum(weights)/wsum
     *
     * Default weights:
     *   weights <- rep.int(1/nx, nx)
     *   totMass <- nx/N
     *
     * @param x array of data points
     * @param w array of weights
     * @param xlo lower bound (from - 4 * bw)
     * @param xhi upper bound (to + 4 * bw)
     * @param n number of bins (Default is 512)
     * @return array of density values
     */
    private double[] bindist(double[] x, double[] w, double xlo, double xhi, int n){
        int ixmin = 0;
        int ixmax = n - 2;
        double xdelta = (xhi - xlo) / (n - 1);
        double[] y = new double[2 * n];
        Arrays.fill(y, 0);
        int wLength = w.length;

        for (int i = 0; i < x.length; i++) {
            if(Double.isInfinite(x[i])){
                continue;
            }

            double xpos = (x[i] - xlo) / xdelta;

            if(Double.isInfinite(xpos)){
                continue;
            }

            int ix = (int) Math.floor(xpos);
            double fx = xpos - ix;
            double wi = w[i % wLength];
            if (ixmin <= ix && ix <= ixmax) {
                y[ix] += (1 - fx) * wi;
                y[ix + 1] += fx * wi;
            } else if (ix == -1) {
                y[0] += fx * wi;
            } else if (ix == ixmax + 1) {
                y[ix] += (1 - fx) * wi;
            }
        }
        return y;
    }

    public double[] getPoints(){
        return quadPoints;
    }

    public double[] getDensity(){
        double[] d = new double[quadPoints.length];
        for(int i=0;i<quadPoints.length;i++){
            d[i] = densityFunction.value(quadPoints[i]);
        }
        return d;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-25s", "Kernel Density Estimate");f.format("%n");
        f.format("%-12s", "Bandwidth = "); f.format("%8.4f", h);f.format("%n");
        f.format("%-12s", "Kernel Type = "); f.format("%-20s", kernelType.toString());f.format("%n");
        f.format("%-35s", "===================================");f.format("%n");
        f.format("%8s", "Point"); f.format("%10s", "PDF");f.format("%10s", "CDF");f.format("%n");
        f.format("%-35s", "-----------------------------------");f.format("%n");

        for(int i=0;i<nPoints;i++){
            f.format("%8.4f", quadPoints[i]);
            f.format("%2s", "  ");
            f.format("%8.4f", densityFunction.value(quadPoints[i]));
            f.format("%2s", "  ");
            f.format("%8.4f", cdf(quadPoints[i]));
            f.format("%n");
        }
        f.format("%-35s", "===================================");f.format("%n");


        return f.toString();
    }


}

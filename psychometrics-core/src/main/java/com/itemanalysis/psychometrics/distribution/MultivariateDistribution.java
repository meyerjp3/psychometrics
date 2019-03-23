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

public interface MultivariateDistribution {

    /**
     * The number of parameters of the quadrature.
     */
    public int npara();

    /**
     * Shannon entropy of the quadrature.
     */
    public double entropy();

    /**
     * The mean vector of quadrature.
     */
    public double[] mean();

    /**
     * The covariance matrix of quadrature.
     */
    public double[][] cov();

    /**
     * The probability density function for continuous quadrature
     * or probability mass function for discrete quadrature at x.
     */
    public double pdf(double[] x);

    /**
     * The density at x in log scale, which may prevents the underflow problem.
     */
    public double logp(double[] x);

    /**
     * Cumulative quadrature function. That is the probability to the left of x.
     */
    public double cdf(double[] x);

    /**
     * The likelihood of the sample set following this quadrature.
     *
     * @param x sample set. Each row is a sample.
     */
    public double likelihood(double[][] x);

    /**
     * The log likelihood of the sample set following this quadrature.
     *
     * @param x sample set. Each row is a sample.
     */
    public double logLikelihood(double[][] x);


}

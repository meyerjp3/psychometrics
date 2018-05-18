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

public interface UnivariateDistribution {

    /**
     * Number of parameters in this distribution.
     *
     * @return number of parameters
     */
    public int numberOfParameters();

    /**
     * Distribution mean
     * @return mean
     */
    public double mean();

    /**
     * Distribution variance
     * @return variance
     */
    public double variance();

    /**
     * Distribution standard deviation
     *
     * @return standard deviation
     */
    public double sd();

    /**
     * Probability density function for continuous variables. Probability mass function
     * for discrete variables.
     *
     * @param x value where pdf is computed.
     * @return density function value
     */
    public double pdf(double x);

    /**
     * Cumulative distribution function. Provides P(X <= q).
     *
     * @param q value where cdf is computed.
     * @return cumulative distribution function value
     */
    public double cdf(double q);

    /**
     * Inverse distribution function. Provides q such that P(X <= q) = p.
     *
     * @param p cumulative probability (between 0 and 1).
     * @return quantile at p.
     */
    public double idf(double p);

    /**
     * DEnsity at x in log scale.
     *
     * @param x value where pdf is computed.
     * @return natural log of the density.
     */
    public double logp(double x);

    /**
     * Generates a random number form this distribution.
     *
     * @return random number
     */
    public double rand();

    /**
     * Lower bound of the support
     *
     * @return lower bound
     */
    public double getSupportLowerBound();

    /**
     * Upper bound of the support
     *
     * @return uper bound
     */
    public double getSupportUpperBound();

    /**
     * Likelihood of a given aray of values.
     *
     * @param x array of values
     * @return likelihood
     */
    public double likelihood(double[] x);

    /**
     * Log-likelihood of a given array of values
     *
     * @param x array of values
     * @return log-likelihood
     */
    public double loglikelihood(double[] x);



}

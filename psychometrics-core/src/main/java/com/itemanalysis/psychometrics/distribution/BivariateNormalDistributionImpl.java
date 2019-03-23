/*
 * Copyright 2012 J. Patrick Meyer
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

/**
 * 
 * Computes probabilities from the Bivariate normal quadrature function (i.e. bivariate normal CDF)
 * Adpated from http://vadim.kutsyy.com/
 *
 */
public final class BivariateNormalDistributionImpl {

    public BivariateNormalDistributionImpl(){

    }

    /**
     *  A function for computing bivariate normal probabilities. <BR>
     *
     *
     *@param  lower  lower limits of integrations
     *@param  upper  upper limits of integration
     *@param  sigma  covariance matrix
     *@return        probability
     */
    public double cumulativeProbability(double[] lower, double[] upper, double[][] sigma) {
        if (lower.length == 1 || upper.length == 1 || sigma.length == 1 ||
                sigma[0].length == 1) {
            return nor(upper[0] / Math.sqrt(sigma[0][0])) - nor(lower[0] / Math.sqrt(sigma[0][0]));
        }
        if (lower.length != 2 || upper.length != 2 || sigma.length != 2 ||
                sigma[0].length != 2) {
            throw new IllegalArgumentException("all matrix are not length of 2");
        }
        double[] c = (double[]) lower.clone();
        double[] d = (double[]) upper.clone();
        double[] sd = {
                Math.sqrt(sigma[0][0]), Math.sqrt(sigma[1][1])
                };
        for (int i = 0; i < 2; i++) {
            c[i] /= sd[i];
            d[i] /= sd[i];
        }
        return cumulativeProbability(c, d, sigma[1][0] / (sd[0] * sd[1]));
    }


    /**
     *  A function for computing bivariate normal probabilities. <BR>
     *
     *
     *@param  lower  lower limits of integrations
     *@param  upper  upper limits of integration
     *@param  infin  integration limits, provided in order to work with
     *      Mvndstpack
     *@param  cor    correlation coeffitient
     *@return        probability
     *@return        result
     */
    public double cumulativeProbability(double[] lower, double[] upper, int[] infin,
                                        double cor) {
        for (int i = 0; i < 2; i++) {
            if (infin[i] < 1) {
                lower[i] = Double.NEGATIVE_INFINITY;
            }
            if (infin[i] < 0 || infin[i] == 1) {
                upper[i] = Double.POSITIVE_INFINITY;
            }
        }
        double[][] cov = {
                {
                1, cor
                }, {
                cor, 1
                }
                };
        return cumulativeProbability(lower, upper, cov);
    }


    /**
     *  A function for computing bivariate normal probabilities. <BR>
     *
     *
     *@param  lower  lower limit of integration
     *@param  upper  upper limit of integration
     *@param  rho    correlation coeffitient
     *@return        probability
     *@author:       <A href="http://www.kutsyy.com>Vadum Kutsyy <\A>
     */
    public double cumulativeProbability(double[] lower, double[] upper, double rho) {
        if (lower.length == 1 && upper.length == 1) {
            return nor(upper[0]) - nor(lower[0]);
        }
        if (lower.length != 2 || upper.length != 2) {
            throw new IllegalArgumentException("all matrix are not length of 2");
        }
        if (lower[0] == upper[0] || lower[1] == upper[1]) {
            return 0;
        }
        if (lower[0] > upper[0] || lower[1] > upper[1]) {
            throw new IllegalArgumentException("lower limit bigger than upper");
        }
        int[] inf = {
                3, 3
                };
        for (int i = 0; i < 2; i++) {
            if (lower[i] == Double.NEGATIVE_INFINITY) {
                if (upper[i] != Double.POSITIVE_INFINITY) {
                    inf[i] = 0;
                }
            }
            else if (upper[i] == Double.POSITIVE_INFINITY) {
                inf[i] = 1;
            }
            else {
                inf[i] = 2;
            }
        }
        switch (inf[0]) {
            case 0:
                switch (inf[1]) {
                    case 0:
                        return cumulativeProbability(upper[0], upper[1], rho);
                    case 1:
                        return cumulativeProbability(upper[0], -lower[1], -rho);
                    case 2:
                        return cumulativeProbability(upper[0], upper[1], rho) - cumulativeProbability(upper[0],
                                lower[1], rho);
                    default:
                        return nor(upper[1]);
                }
            case 1:
                switch (inf[1]) {
                    case 0:
                        return cumulativeProbability(-lower[0], upper[1], -rho);
                    case 1:
                        return cumulativeProbability(-lower[0], -lower[1], rho);
                    case 2:
                        return cumulativeProbability(-lower[0], -lower[1], rho) - cumulativeProbability(-lower[0],
                                -upper[1], rho);
                    default:
                        return 1 - nor(lower[1]);
                }
            case 2:
                switch (inf[1]) {
                    case 0:
                        return cumulativeProbability(upper[0], upper[1], rho) - cumulativeProbability(lower[0],
                                upper[1], rho);
                    case 1:
                        return cumulativeProbability(-lower[0], -lower[1], rho) - cumulativeProbability(-upper[0],
                                -lower[1], rho);
                    case 2:
                    {
                        double t = cumulativeProbability(upper[0], upper[1], rho) + cumulativeProbability(lower[0],
                                lower[1], rho) - cumulativeProbability(upper[0], lower[1],
                                rho) - cumulativeProbability(lower[0], upper[1], rho);
                        if (t < 0 || t > 1) {
                            t = cumulativeProbability(-upper[0], -upper[1], rho) + cumulativeProbability(-lower[0],
                                    -lower[1], rho) - cumulativeProbability(-upper[0],
                                    -lower[1], rho) - cumulativeProbability(-lower[0],
                                    -upper[1], rho);
                        }
                        if (t < 0 || t > 1) {
                            throw new IllegalStateException("result of probability is outside the reagen");
                        }
                        return t;
                    }
                    default:
                        return nor(upper[1]) - nor(lower[1]);
                }
            default:
                switch (inf[1]) {
                    case 0:
                        return nor(upper[0]);
                    case 1:
                        return 1 - nor(lower[0]);
                    case 2:
                        return nor(upper[0]) - nor(lower[0]);
                    default:
                        return 1;
                }
        }
    }


    /**
     *  A function for computing bivariate normal probabilities. <BR>
     *  Based on algorithms by <BR>
     *  Yihong Ge, Department of Computer Science and Electrical Engineering,
     *  Washington State University, Pullman, WA 99164-2752 <BR>
     *  and <BR>
     *  Alan Genz <a href="http://www.sci.wsu.edu/math/faculty/genz/homepage">
     *  http://www.sci.wsu.edu/math/faculty/genz/homepage</a> <BR>
     *  Department of Mathematics, Washington State University, Pullman, WA
     *  99164-3113, Email : alangenz@wsu.edu <BR>
     *  <BR>
     *
     *
     *@param  sh  integration limit
     *@param  sk  integration limit
     *@param  r   correlation coefficient
     *@return     result
     */
    public double cumulativeProbability(double sh, double sk, double r) {
        sh = -sh;
        sk = -sk;
        if (sh == Double.POSITIVE_INFINITY) {
            return nor(sk);
        }
        if (sh == Double.NEGATIVE_INFINITY || sk == Double.NEGATIVE_INFINITY) {
            return 0;
        }
        if (sk == Double.POSITIVE_INFINITY) {
            return nor(sh);
        }
        double[][] w = {
                {
                0.1713244923791705e+00, 0.4717533638651177e-01, 0.1761400713915212e-01
                }, {
                0.3607615730481384e+00, 0.1069393259953183e+00, 0.4060142980038694e-01
                }, {
                0.4679139345726904e+00, 0.1600783285433464e+00, 0.6267204833410906e-01
                }, {
                0, 0.2031674267230659e+00, 0.8327674157670475e-01
                }, {
                0, 0.2334925365383547e+00, 0.1019301198172404e+00
                }, {
                0, 0.2491470458134029e+00, 0.1181945319615184e+00
                }, {
                0, 0, 0.1316886384491766e+00
                }, {
                0, 0, 0.1420961093183821e+00
                }, {
                0, 0, 0.1491729864726037e+00
                }, {
                0, 0, 0.1527533871307259e+00
                }
                };
        double[][] x = {
                {
                -0.9324695142031522e+00, -0.9815606342467191e+00, -0.9931285991850949e+00
                }, {
                -0.6612093864662647e+00, -0.9041172563704750e+00, -0.9639719272779138e+00
                }, {
                -0.2386191860831970e+00, -0.7699026741943050e+00, -0.9122344282513259e+00
                }, {
                0, -0.5873179542866171e+00, -0.8391169718222188e+00
                }, {
                0, -0.3678314989981802e+00, -0.7463319064601508e+00
                }, {
                0, -0.1252334085114692e+00, -0.6360536807265150e+00
                }, {
                0, 0, -0.5108670019508271e+00
                }, {
                0, 0, -0.3737060887154196e+00
                }, {
                0, 0, -0.2277858511416451e+00
                }, {
                0, 0, -0.7652652113349733e-01
                }
                };
        double bvn;
        double as;
        double a;
        double b;
        double c;
        double d;
        double rs;
        double xs;
        double mvnphi;
        double sn;
        double asr;
        double h;
        double k;
        double bs;
        double hs;
        double hk;
        int lg = 10;
        int ng = 2;
        if (Math.abs(r) < 0.3) {
            ng = 0;
            lg = 3;
        }
        else if (Math.abs(r) < 0.75) {
            ng = 1;
            lg = 6;
        }
        ;
        h = sh;
        k = sk;
        hk = h * k;
        bvn = 0;
        if (Math.abs(r) < 0.925) {
            hs = (h * h + k * k) / 2;
            asr = Math.asin(r);
            for (int i = 0; i < lg; i++) {
                sn = Math.sin(asr * (x[i][ng] + 1) / 2.0);
                bvn += w[i][ng] * Math.exp((sn * hk - hs) / (1 - sn * sn));
                sn = Math.sin(asr * (-x[i][ng] + 1) / 2);
                bvn += w[i][ng] * Math.exp((sn * hk - hs) / (1 - sn * sn));
            }
            bvn = bvn * asr / (4 * Math.PI) + nor(-h) * nor(-k);
        }
        else {
            if (r < 0) {
                k = -k;
                hk = -hk;
            }
            if (Math.abs(r) < 1) {
                as = (1 - r) * (1 + r);
                a = Math.sqrt(as);
                bs = (h - k) * (h - k);
                c = (4 - hk) / 8;
                d = (12 - hk) / 16;
                bvn = a * Math.exp(-(bs / as + hk) / 2) * (1 - c * (bs - as) * (1 - d * bs / 5) / 3
                         + c * d * as * as / 5);
                if (hk > -160) {
                    b = Math.sqrt(bs);
                    bvn -= Math.exp(-hk / 2) * Math.sqrt(2 * Math.PI) * nor(-b / a) * b * (
                            1 - c * bs * (1 - d * bs / 5) / 3);
                }
                a /= 2;
                for (int i = 0; i < lg; i++) {
                    xs = a * (x[i][ng] + 1) * a * (x[i][ng] + 1);
                    rs = Math.sqrt(1 - xs);
                    bvn += a * w[i][ng] * (Math.exp(-bs / (2 * xs) - hk / (1 + rs)) / rs
                             - Math.exp(-(bs / xs + hk) / 2) * (1 + c * xs * (1 + d * xs)));
                    xs = as * (-x[i][ng] + 1) * (-x[i][ng] + 1) / 4;
                    rs = Math.sqrt(1 - xs);
                    bvn += a * w[i][ng] * Math.exp(-(bs / xs + hk) / 2) * (Math.exp(-hk * (
                            1 - rs) / (2 * (1 + rs))) / rs - (1 + c * xs * (1 + d * xs)));
                }
                bvn = -bvn / (Math.PI * 2);
            }
            if (r > 0) {
                bvn += nor(-Math.max(h, k));
            }
            if (r < 0) {
                bvn = -bvn + Math.max(0, nor(-h) - nor(-k));
            }
        }
        return bvn;
    }

    /**
     *  Compute Cdf of standart normal
     *
     *@param  x  upper limit
     *@return    probability
     */
    private double nor(double x) {
        //return VisualNumerics.math.Statistics.normalCdf(x);
        if (Double.isNaN(x)) {
            return 0;
        }
        if (Double.isInfinite(x)) {
            return x > 0 ? 1 : 0;
        }
        //return (1 + Stat.erf(x / Math.sqrt(2))) / 2;
        double zabs = Math.abs(x);
        if (zabs > 37) {
            return x > 0 ? 1 : 0;
        }
        double expntl = Math.exp(-(zabs * zabs) / 2);
        double p = 0;
        if (zabs < 7.071067811865475) {
            p = expntl * ((((((zabs * .03526249659989109 + .7003830644436881) * zabs
                     + 6.37396220353165) * zabs + 33.912866078383) * zabs + 112.0792914978709) * zabs
                     + 221.2135961699311) * zabs + 220.2068679123761) / (((((((zabs * .08838834764831844
                     + 1.755667163182642) * zabs + 16.06417757920695) * zabs + 86.78073220294608) * zabs
                     + 296.5642487796737) * zabs + 637.3336333788311) * zabs + 793.8265125199484) * zabs
                     + 440.4137358247522);
        }
        else {
            p = expntl / (zabs + 1 / (zabs + 2 / (zabs + 3 / (zabs + 4 / (zabs + .65))))) / 2.506628274631001;
        }
        return x > 0 ? 1 - p : p;
    }


    /**
     *  Compute Cdf of normal
     *
     *@param  x   upper limit
     *@param  mu  mean
     *@param  sd  stand diviation
     *@return     probability
     *@see        #nor(double)
     */
    private double nor(double x, double mu, double sd) {
        return nor((x - mu) / sd);
    }





}

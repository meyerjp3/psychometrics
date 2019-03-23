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

import com.itemanalysis.psychometrics.distribution.BivariateNormalDistributionImpl;
import com.itemanalysis.psychometrics.distribution.MultivariateNormalDistribution;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * NOTE: some tests may occasionally fail because there is a random aspect to computing the cdf.
 *
 */
public class MultivariateNormalDistributionTest {

    /**
     * Tolerance set to 3 digits because algorithm convergence criterion is only .001.
     * Two digits of accuracy is probably more likely with the defail convergence
     * criterion.
     */
    private final double TOL = 0.001;

    /**
     * Comparing results to values from mvtnorm package in R.
     * Examples are:
     * library(mvtnorm)
     * options(digits=15)
     * M<-c(0,0)
     * S<-matrix(c(1,0,0,1), nrow=2, byrow=TRUE)
     * pmvnorm(upper=c(0,0), mean=M, sigma=S)
     * pmvnorm(upper=c(1,1), mean=M, sigma=S)
     */
    @Test
    public void bivariateCDFTest(){
        System.out.println("MVN bivariate CDF test");
        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(2);

        double[] v1 = {0, 0};
        double prob = mvn.cdf(v1);
        assertEquals("  Two dimension test 1: ", 0.25, prob, TOL);

        double[] v2 = {1.0, 1.0};
        prob = mvn.cdf(v2);
        assertEquals("  Two dimension test 2: ", 0.707860981737141, prob, TOL);

        double[] v3 = {-1.0, 1.0};
        prob = mvn.cdf(v3);
        assertEquals("  Two dimension test 3: ", 0.133483764331402, prob, TOL);

        double[] v4 = {-1.7234,2.789};
        prob = mvn.cdf(v4);
        assertEquals("  Two dimension test 4: ", 0.042295999368299, prob, TOL);

        double[] v5 = {-6,6};
        prob = mvn.cdf(v5);
        assertEquals("  Two dimension test 5: ", 9.86587644064347e-10, prob, TOL);

    }

    /**
     * Comparing results to values from mvtnorm package in R.
     * Examples are:
     * library(mvtnorm)
     * options(digits=15)
     * M<-c(0,0)
     * S<-matrix(c(1,0,0,1), nrow=2, byrow=TRUE)
     * pmvnorm(upper=c(0,0), mean=M, sigma=S)
     * pmvnorm(upper=c(1,1), mean=M, sigma=S)
     */
    @Test
    public void fiveDimensionCDFTest(){
        System.out.println("MVN five dimension CDF test");
        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(5);

        double[] v1 = {0, 0, 0, 0, 0};
        double prob = mvn.cdf(v1);
        assertEquals("  Test 1: ", 0.03125, prob, TOL);

        double[] v2 = {
                1.801144592657363, 0.463917742000370, -0.189227016333841,
                -1.122749722828534, -0.169359947380119};
        prob = mvn.cdf(v2);
        assertEquals("  Test 2: ", 0.0157360799258813, prob, TOL);

        double[] v3 = {-0.802153887265151, -0.731490508705540, 0.200401145373060,
                -1.479459014315799, -0.102296579640889};
        prob = mvn.cdf(v3);
        assertEquals("  Test 3: ", prob, 0.000907374290771627, TOL);

        double[] v4 = {0.7599082478778414, -0.5300135054794117, 1.6948173499627912,
                -0.6712274078250637, -0.0184137901596998};
        prob = mvn.cdf(v4);
        assertEquals("  Test 4: ", 0.0273278700656185, prob, TOL);

        double[] v5 = {-3, -3, 0, 3, 3};
        prob = mvn.cdf(v5);
        assertEquals("  Test 5: ", 9.08654190620778e-07, prob, TOL);

    }

    /**
     * Tested against R
     * library(mvtnorm)
     * X<-c(0.713919336274493, 0.584408785741822, 0.263119200077829, 0.732513610871908)
     * M<-c(-0.683477474844462, 1.480296478403701, 1.008431991316523,0.448404211078558)
     * S<-matrix(c(
     * 3.260127902272362, 2.343938296424249, 0.1409050254343716, -0.1628775438743266,
     * 2.343938296424249, 4.213034991388330, 1.3997210599608563,  0.3373448510018783,
     * 0.1409050254343716, 1.3997210599608563, 4.6042485263677939,  0.0807267064408651,
     * -0.1628775438743266, 0.3373448510018783, 0.0807267064408651,  5.4950949215890672), nrow=4, byrow=TRUE)
     * pmvnorm(upper=X, mean=M, sigma=S)
     */
    @Test
    public void multiDimTest(){
        System.out.println("MVN multidimensional CDF test");
        double[][] S = {
                {3.260127902272362, 2.343938296424249, 0.1409050254343716, -0.1628775438743266},
                {2.343938296424249, 4.213034991388330, 1.3997210599608563,  0.3373448510018783},
                {0.1409050254343716, 1.3997210599608563, 4.6042485263677939,  0.0807267064408651},
                {-0.1628775438743266, 0.3373448510018783, 0.0807267064408651,  5.4950949215890672}
        };

        double[] M = {-0.683477474844462,  1.480296478403701,  1.008431991316523,  0.448404211078558};

        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(M, S);

        double[] X1 = {0.713919336274493, 0.584408785741822, 0.263119200077829, 0.732513610871908};

        double prob = mvn.cdf(X1);
        assertEquals("  Test 1: ", 0.0904191282120575, prob, TOL);
    }

    /**
     * Test of cdf method, of class MultivariateGaussian.
     * This test is from teh smile library.
     */
    //@Test
    public void smileTestCdf() {
        System.out.println("MVN smile cdf test");

        double[] mu = {1.0, 0.0, -1.0};
        double[][] sigma = {
                {0.9000, 0.4000, 0.7000},
                {0.4000, 0.5000, 0.3000},
                {0.7000, 0.3000, 0.8000}
        };

        double[][] x = {
                {1.2793, -0.1029, -1.5852},
                {-0.2676, -0.1717, -1.8695},
                {1.6777, 0.7642, -1.0226},
                {2.5402, 1.0887, 0.8989},
                {0.3437, 0.4407, -1.9424},
                {1.8140, 0.7413, -0.1129},
                {2.1897, 1.2047, 0.0128},
                {-0.5119, -1.3545, -2.6181},
                {-0.3670, -0.6188, -3.1594},
                {1.5418, 0.1519, -0.6054}
        };
        double[] pdf = {
                0.0570, 0.0729, 0.0742, 0.0178, 0.0578,
                0.1123, 0.0511, 0.0208, 0.0078, 0.1955
        };
        double[] cdfSmile = {
                0.1752, 0.0600, 0.4545, 0.9005, 0.1143,
                0.6974, 0.8178, 0.0050, 0.0051, 0.4419
        };

        //Results from R
        double[] cdfR = {0.175151972651665, 0.0599740084870407, 0.454506071317056,
                0.900614371069787, 0.114312641623954, 0.697450174761431,
                0.817816237124512, 0.00505963578875454, 0.00512904468757106, 0.441916904631481};

        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(mu, sigma);


        for (int i = 0; i < x.length; i++) {
            //Fails because of error in smile algorithm
//            assertEquals("  Test " +(i+1), cdfSmile[i], instance.cdf(x[i]), TOL);
            assertEquals("  Test " +(i+1), cdfR[i], mvn.cdf(x[i], .001, 500000), 1e-2);
        }
    }

    @Test
    public void fiveDimTest2(){
        System.out.println("MVN five dimension CDF test2");
        double[] M = {1.26, 1.46, 1.70, 1.45, 1.40};

        double[][] S = {
                {0.7712, 0.2139, 0.2020, 0.1932, 0.1735},
                {0.2139, 0.7991, 0.2003, 0.1748, 0.1889},
                {0.2020, 0.2003, 0.4597, 0.1918, 0.1669},
                {0.1932, 0.1748, 0.1918, 0.4704, 0.1999},
                {0.1735, 0.1889, 0.1669, 0.1999, 0.6176}
        };

        double[] X = {1.65145825044713, -1.42463857838554,  1.93211381367847,  1.23859114149269,  2.34996101409890};

        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(M, S);
        double prob = mvn.cdf(X);


        assertEquals("  Test 2", 0.000431981044177644, prob, TOL);

    }

    @Test
    public void lowerUpperTest(){
        System.out.println("MVN four dimension CDF test with lower and upper bounds");

        double[] M = {0, 0, 0, 0};
        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(M, 1.0);

        double[] lower = {-1, -1, -1, -1};
        double[] upper = {1.5, 1.5, 1.5, 1.5};

        double prob = mvn.cdf(lower, upper);

        //System.out.println(prob);

        assertEquals("  Test 2", 0.359890098354227, prob, TOL);

    }

    /**
     * Compare MultivariateNormalDistribution to BivariateNormalDistributionImpl and R
     */
    @Test
    public void testBvnor1() {
        System.out.print("MVN bvnor1");
        double sh = 0.0;
        double sk = 0.0;
        double r = 0.5;
        double trueResult = 0.333333333333333;//from R

        BivariateNormalDistributionImpl bvnorm = new BivariateNormalDistributionImpl();

        double[] M = {0.0, 0.0};
        double[][] S = {
                {1.0,r},
                {r, 1.0}
        };

        double[] X = {sh, sk};

        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(M, S);

        double bivResult = bvnorm.cumulativeProbability(sh, sk, r);
        double mvnResult = mvn.cdf(X);
        //System.out.println(bivResult);
        //System.out.println(mvnResult);

        assertEquals("bvnorm test 1", 0.333333333333333, mvnResult, 1e-3);
        assertEquals("bvnorm test 1", bivResult, mvnResult, 1e-3);
    }


    /**
     * IDF tested against R.
     *
     * library(mvtnorm)
     * M<-c(0, 0)
     * S<-matrix(c(1, 0, 0, 1), nrow=2, byrow=TRUE)
     * qmvnorm(p=.75, mean=M, sigma=S)
     *
     */
    @Test
    public void bivariateIDFTest1(){
        System.out.println("MVN bivariate IDF test 1");
        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(2);

        double q = mvn.idf(0.75, .001, 20000);
        //System.out.println(q);

        assertEquals("  Test 1", 1.10785119419916, q, TOL);

    }

    /**
     * IDF tested against R.
     *
     * library(mvtnorm)
     *
     * M<-c(100, 0)
     * S<-matrix(c(15*15, 0, 0, 1), nrow=2, byrow=TRUE)
     * qmvnorm(p=.75, mean=M, sigma=S)
     *
     */
    @Test
    public void bivariateIDFTest2(){
        System.out.println("MVN bivariate IDF test 2");

        double[] M = {100, 0};
        double[][] S = {
                {225,0},
                {0,1}
        };

        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(M, S);
        double q = mvn.idf(0.75, .001, 20000);
        //System.out.println(q);

        assertEquals("  Test 2", 110.1173, q, TOL);

    }

    /**
     * IDF tested against R.
     *
     * library(mvtnorm)
     *
     * M<-c(100, 0, -50)
     * S<-matrix(c(
     * 15*15, 0, 0,
     * 0, 1, 0,
     * 0, 0, 25), nrow=3, byrow=TRUE)
     * qmvnorm(p=.25, mean=M, sigma=S)
     *
     */
    @Test
    public void bivariateIDFTest3(){
        System.out.println("MVN trivariate IDF test 3");

        double[] M = {100, 0, -50};
        double[][] S = {
                {225,0,0},
                {0,1,0},
                {0,0,25}
        };

        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(M, S);
        double q = mvn.idf(0.25, .001, 20000);
        //System.out.println(q);

        assertEquals("  Test 3", 89.88265, q, TOL);

    }

    /**
     * IDF tested against R.
     *
     * library(mvtnorm)
     *
     * M<-c(-100, 0)
     * S<-matrix(c(15*15, 0, 0, 1), nrow=2, byrow=TRUE)
     * qmvnorm(p=.0001, mean=M, sigma=S)
     *
     */
    @Test
    public void bivariateIDFTest4(){
        System.out.println("MVN bivariate IDF test 4");

        double[] M = {-100, 0};
        double[][] S = {
                {225,0},
                {0,1}
        };

        MultivariateNormalDistribution mvn = new MultivariateNormalDistribution(M, S);
        double q = mvn.idf(0.0001);
        //System.out.println(q);

        assertEquals("  Test 4", -3.719016, q, TOL);

    }

}
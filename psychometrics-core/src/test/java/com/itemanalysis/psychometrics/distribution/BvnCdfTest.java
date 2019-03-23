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

import com.itemanalysis.psychometrics.distribution.BivariateNormalDistributionImpl;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * True values computed in R with:
 * setOption(digits=15)
 * library(mvtnorm)
 * x<-c(0,0)
 * r<-0.5
 * m<-c(0,0)
 * S<-matrix(c(1,r,r,1),nrow=2, byrow=TRUE)
 * pmvnorm(upper=x, mean=m, corr=S)
 *
 * Value of x and r changed for each test.
 *
 *
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class BvnCdfTest {

    private BivariateNormalDistributionImpl bvnorm = null;

    public BvnCdfTest() {
        bvnorm = new BivariateNormalDistributionImpl();
    }

    /**
     * Test of bvnor method, of class BvnCdfTest.
     */
    @Test
    public void testBvnor1() {
        System.out.print("bvnor1: ");
        double sh = 0.0;
        double sk = 0.0;
        double r = 0.5;
        double expResult = 0.333333333333333;//from R
        double obsResult = bvnorm.cumulativeProbability(sh, sk, r);
        System.out.println(obsResult);
        assertEquals("bvnorm test 1", expResult, obsResult, 1e-15);
    }

    @Test
    public void testBvnor2() {
        System.out.print("bvnor2: ");
        double sh = 0.0;
        double sk = 0.0;
        double r = 0.99999;
        double expResult = 0.499288236863448;//from R
        double obsResult = bvnorm.cumulativeProbability(sh, sk, r);
        System.out.println(obsResult);
        assertEquals("bvnorm test 2", expResult, obsResult, 1e-15);
    }

    @Test
    public void testBvnor3() {
        System.out.print("bvnor3: ");
        double sh = 4.0;
        double sk = -4.0;
        double r = 0.5126;
        double expResult = 3.16712418331054e-05;//from R
        double obsResult = bvnorm.cumulativeProbability(sh, sk, r);
        System.out.println(obsResult);
        assertEquals("bvnorm test 3", expResult, obsResult, 1e-15);
    }

    @Test
    public void testBvnor4() {
        System.out.print("bvnor4: ");
        double sh = 4.0;
        double sk = 0.0;
        double r = 0.25;
        double expResult = 0.499995629490009;//from R
        double obsResult = bvnorm.cumulativeProbability(sh, sk, r);
        System.out.println(obsResult);
        assertEquals("bvnorm test 4", expResult, obsResult, 1e-15);
    }

    @Test
    public void testBvnor5() {
        System.out.print("bvnor5: ");
        double sh = 10.0;
        double sk = 10.0;
        double r = 0.000;
        double expResult = 1.000;//from R
        double obsResult = bvnorm.cumulativeProbability(sh, sk, r);
        System.out.println(obsResult);
        assertEquals("bvnorm test 5", expResult, obsResult, 1e-15);
    }

    @Test
    public void testBvnor6() {
        System.out.print("bvnor6: ");
        double sh = 1.96;
        double sk = 1.96;
        double r = 0.0000123;
        double expResult = 0.950629146475124;//from R
        double obsResult = bvnorm.cumulativeProbability(sh, sk, r);
        System.out.println(obsResult);
        assertEquals("bvnorm test 6", expResult, obsResult, 1e-15);
    }



}
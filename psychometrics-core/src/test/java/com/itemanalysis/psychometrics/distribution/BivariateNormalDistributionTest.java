package com.itemanalysis.psychometrics.distribution;

import com.itemanalysis.psychometrics.distribution.BivariateNormalDistribution;
import org.junit.Test;

import static org.junit.Assert.*;

public class BivariateNormalDistributionTest {


    @Test
    public void bvnTest0(){
        BivariateNormalDistribution bvn = new BivariateNormalDistribution();
        double prob = bvn.bivnor(0.5, 0.5, 0.2);
        System.out.println(prob);
    }

    @Test
    public void testBvnor1() {
        System.out.print("bvnor1: ");
        double sh = 0.0;
        double sk = 0.0;
        double r = 0.5;
        double expResult = 0.333333333333333;//from R
//        double obsResult = bvnorm.cumulativeProbability(sh, sk, r);

        BivariateNormalDistribution bvn = new BivariateNormalDistribution();
        double prob = bvn.bivnor(0, 0, 0.5);

        System.out.println(prob);
        assertEquals("bvnorm test 1", expResult, prob, 1e-15);
    }


}
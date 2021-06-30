package com.itemanalysis.psychometrics.statistics;

import org.junit.Test;

import static org.junit.Assert.*;

public class WeightedOnlineCorrelationTest {


    @Test
    public void test1(){
        double[][] x = {
                {96,97,1},
                {97,97,4},
                {98,97,10},
                {99,97,2},
                {96,98,1},
                {97,98,8},
                {98,98,47},
                {99,98,48},
                {100,98,10},
                {98,99,60},
                {99,99,246},
                {100,99,140},
                {101,99,17},
                {98,100,12},
                {99,100,156},
                {100,100,419},
                {101,100,171},
                {102,100,11},
                {99,101,13},
                {100,101,162},
                {101,101,255},
                {102,101,57},
                {103,101,3},
                {100,102,11},
                {101,102,69},
                {102,102,50},
                {103,102,10},
                {102,103,5},
                {103,103,2}};

        //true value = 0.7262193
        //true value s1 = 1.063128
        //true value s2 = 1.045201

        WeightedOnlineCorrelation r = new WeightedOnlineCorrelation(true);
        WeightedOnlineCovariance cov = new WeightedOnlineCovariance(true);
        WeightedOnlineStandardDeviation s1 = new WeightedOnlineStandardDeviation(true);
        WeightedOnlineStandardDeviation s2 = new WeightedOnlineStandardDeviation(true);

        for(int i=0; i < x.length;i++){
            r.increment(x[i][0], x[i][1], x[i][2]);
            cov.increment(x[i][0], x[i][1], x[i][2]);
            s1.increment(x[i][0], x[i][2]);
            s2.increment(x[i][1], x[i][2]);
        }

        System.out.println("Correlation: " + r.getResult() + "   " + r.getN());
        System.out.println(" Covariance: " + cov.getResult() + "   " + r.getN());

        assertEquals("Weighted OnlineStandard Deviation Test of x", 1.063128, s1.getResult(), 1e-6);
        assertEquals("Weighted OnlineStandard Deviation Test of x N", 2000, s1.getN(), 1e-6);
        assertEquals("Weighted OnlineStandard Deviation Test of y", 1.045201, s2.getResult(), 1e-6);
        assertEquals("Weighted OnlineStandard Deviation Test of y N", 2000, s2.getN(), 1e-6);
        assertEquals("Weighted OnlineStandard Correlation", 0.7262193, r.getResult(), 1e-6);
        assertEquals("Weighted OnlineStandard Covariance", 0.8069625, cov.getResult(), 1e-6);





    }

}
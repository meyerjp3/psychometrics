package com.itemanalysis.psychometrics.irt.model;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class IrmPCMTest {

    //values in these arrays are from a function written in R
    double[] theta1 = {-4.0, -3.5, -3.0, -2.5, -2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0,  2.5, 3.0, 3.5, 4.0};
    double[] p1 = {9.676697e-01, 9.273983e-01, 8.447371e-01, 6.975695e-01, 4.916785e-01,
            2.834124e-01, 1.324147e-01, 4.930711e-02, 1.356202e-02, 2.505329e-03, 3.161041e-04,
            3.105329e-05, 2.685617e-06, 2.191983e-07, 1.744436e-08, 1.373238e-09, 1.075995e-10};
    double[] p2 = {3.229430e-02, 7.241280e-02, 1.543196e-01, 2.981516e-01, 4.916785e-01, 6.630849e-01,
            7.248313e-01, 6.314813e-01, 4.063739e-01, 1.756371e-01, 5.184800e-02, 1.191680e-02,
            2.411274e-03, 4.604578e-04, 8.573502e-05, 1.579063e-05, 2.894769e-06};
    double[] p3 = {3.596859e-05, 1.886962e-04, 9.408472e-04, 4.252905e-03, 1.640892e-02, 5.177478e-02,
            1.324147e-01, 2.699045e-01, 4.063739e-01, 4.109288e-01, 2.838132e-01, 1.526197e-01,
            7.225165e-02, 3.228059e-02, 1.406242e-02, 6.059706e-03, 2.599060e-03};
    double[] p4 = {1.712264e-08, 2.101652e-07, 2.451698e-06, 2.592888e-05, 2.340607e-04, 1.727894e-03,
            1.033916e-02, 4.930711e-02, 1.736903e-01, 4.109288e-01, 6.640227e-01, 8.354324e-01, 9.253344e-01,
            9.672587e-01, 9.858518e-01, 9.939245e-01, 9.973980e-01};

    //parameter values
    double difficulty = 0.0;
    double[] threshold = {-2.0, 0.0, 0.5};
    int[] response = {0, 1, 2, 3};

     @Test
    public void probTest1(){
        System.out.println("Probability Test1");
        IrmPCM irm = new IrmPCM(difficulty, threshold, 1.7);
        assertEquals("Probability Test, Cat 1, at theta: 0", 0.01356202, irm.probability(0.0, 0), 1e-6);
        assertEquals("Probability Test, Cat 2, at theta: 0", 0.4063739, irm.probability(0.0, 1), 1e-6);
        assertEquals("Probability Test, Cat 3, at theta: 0", 0.4063739, irm.probability(0.0, 2), 1e-6);
        assertEquals("Probability Test, Cat 4, at theta: 0", 0.1736903, irm.probability(0.0, 3), 1e-6);

    }


    @Test
    public void probTest2(){
        System.out.println("Probability Test2");
        IrmPCM irm = new IrmPCM(difficulty, threshold, 1.7);
        for(int i=0;i<theta1.length;i++){
            assertEquals("Probability Test, Cat 1, at theta: " + (i+1), p1[i], irm.probability(theta1[i], response[0]), 1e-6);
        }
        for(int i=0;i<theta1.length;i++){
            assertEquals("Probability Test, Cat 2, at theta: " + (i+1), p2[i], irm.probability(theta1[i], response[1]), 1e-6);
        }
        for(int i=0;i<theta1.length;i++){
            assertEquals("Probability Test, Cat 3, at theta: " + (i+1), p3[i], irm.probability(theta1[i], response[2]), 1e-6);
        }
        for(int i=0;i<theta1.length;i++){
            assertEquals("Probability Test, Cat 4, at theta: " + (i+1), p4[i], irm.probability(theta1[i], response[3]), 1e-6);
        }

    }

    @Test
    public void expectedValueTest1(){
        System.out.println("Expected Value Test1");
        IrmPCM irm = new IrmPCM(difficulty, threshold, 1.7);
        assertEquals("Expected value test 1", 1.740193, irm.expectedValue(0.0), 1e-5);
    }



    @Test
    public void derivThetaTest(){
        System.out.println("Derivative wrt Theta test PCM: Test 1");

        //true value for derivative obtained numerically using numDeriv package in R
        double[] threshold = {-1.850498,1.850498};
        IrmPCM irm = new IrmPCM(2.825801, threshold, 1.7);
        double d1 = irm.derivTheta(0.0);

        System.out.println("    deriv wrt theta: " + d1);
        assertEquals("Derivative comparison 1", 0.2288251, d1, 1e-5);


        System.out.println("Derivative wrt Theta test PCM: Test 2");
        //true value for derivative obtained numerically using numDeriv package in R
        double[] step2y = {-0.09975,0.09975};
        irm = new IrmPCM(0.856264,step2y, 1.7);
        d1 = irm.derivTheta(0.0);
        System.out.println("    deriv wrt theta: " + d1);
        assertEquals("Derivative comparison 2", 0.4886251, d1, 1e-5);


    }

}

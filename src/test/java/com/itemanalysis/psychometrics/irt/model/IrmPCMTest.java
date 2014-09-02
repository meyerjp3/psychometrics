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

    /**
     * True result obtained from plink package in R.
     *
     * library(plink)
     * theta<-seq(-4,4, by=.1)
     * iparam<-matrix(c(1.0, 0.8, -0.5, 0.5), nrow=1, byrow=TRUE)
     * pr<-gpcm(x=iparam, cat=3, theta=theta, D=1.0, location=TRUE)
     * pr@prob
     *
     */
    @Test
    public void probabilityTest1(){
        System.out.println("Probability test with 3 categories.");

        double[] theta = {
                -4.0, -3.9, -3.8, -3.7, -3.6, -3.5, -3.4, -3.3, -3.2, -3.1, -3.0, -2.9, -2.8, -2.7, -2.6,
                -2.5, -2.4, -2.3, -2.2, -2.1, -2.0, -1.9, -1.8, -1.7, -1.6, -1.5, -1.4, -1.3, -1.2, -1.1,
                -1.0, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1,  0.0,  0.1,  0.2,  0.3,  0.4,
                0.5,  0.6,  0.7,  0.8,  0.9,  1.0,  1.1, 1.2,  1.3,  1.4,  1.5,  1.6,  1.7,  1.8,  1.9,
                2.0,  2.1,  2.2,  2.3,  2.4,  2.5,  2.6,  2.7,  2.8,  2.9,  3.0,  3.1,  3.2,  3.3,  3.4,
                3.5,  3.6,  3.7,  3.8,  3.9,  4.0};
        double[][] prob = {
                {0.986547159, 0.01338602, 6.681759e-05},
                {0.985145677, 0.01477283, 8.149526e-05},
                {0.983599738, 0.01630088, 9.938233e-05},
                {0.981894794, 0.01798403, 1.211754e-04},
                {0.980014904, 0.01983737, 1.477207e-04},
                {0.977942624, 0.02187733, 1.800449e-04},
                {0.975658878, 0.02412173, 2.193938e-04},
                {0.973142838, 0.02658988, 2.672772e-04},
                {0.970371788, 0.02930269, 3.255235e-04},
                {0.967320990, 0.03228266, 3.963452e-04},
                {0.963963554, 0.03555403, 4.824169e-04},
                {0.960270298, 0.03914273, 5.869679e-04},
                {0.956209626, 0.04307648, 7.138925e-04},
                {0.951747406, 0.04738471, 8.678813e-04},
                {0.946846862, 0.05209856, 1.054575e-03},
                {0.941468498, 0.05725076, 1.280744e-03},
                {0.935570033, 0.06287546, 1.554503e-03},
                {0.929106387, 0.06900806, 1.885557e-03},
                {0.922029709, 0.07568481, 2.285483e-03},
                {0.914289469, 0.08294247, 2.768061e-03},
                {0.905832623, 0.09081773, 3.349646e-03},
                {0.896603875, 0.09934654, 4.049584e-03},
                {0.886546049, 0.10856326, 4.890688e-03},
                {0.875600595, 0.11849965, 5.899750e-03},
                {0.863708249, 0.12918365, 7.108100e-03},
                {0.850809873, 0.14063793, 8.552201e-03},
                {0.836847490, 0.15287825, 1.027426e-02},
                {0.821765549, 0.16591160, 1.232285e-02},
                {0.805512412, 0.17973411, 1.475347e-02},
                {0.788042101, 0.19432879, 1.762911e-02},
                {0.769316273, 0.20966314, 2.102058e-02},
                {0.749306433, 0.22568676, 2.500681e-02},
                {0.727996336, 0.24232893, 2.967474e-02},
                {0.705384513, 0.25949646, 3.511903e-02},
                {0.681486860, 0.27707188, 4.144126e-02},
                {0.656339152, 0.29491219, 4.874866e-02},
                {0.629999337, 0.31284841, 5.715225e-02},
                {0.602549461, 0.33068616, 6.676438e-02},
                {0.574096993, 0.34820743, 7.769558e-02},
                {0.544775379, 0.36517386, 9.005076e-02},
                {0.514743611, 0.38133145, 1.039249e-01},
                {0.484184661, 0.39641687, 1.193985e-01},
                {0.453302657, 0.41016521, 1.365321e-01},
                {0.422318798, 0.42231880, 1.553624e-01},
                {0.391466058, 0.43263690, 1.758970e-01},
                {0.360982891, 0.44090550, 1.981116e-01},
                {0.331106219, 0.44694665, 2.219471e-01},
                {0.302064114, 0.45062671, 2.473092e-01},
                {0.274068619, 0.45186276, 2.740686e-01},
                {0.247309180, 0.45062671, 3.020641e-01},
                {0.221947136, 0.44694665, 3.311062e-01},
                {0.198111611, 0.44090550, 3.609829e-01},
                {0.175897038, 0.43263690, 3.914661e-01},
                {0.155362403, 0.42231880, 4.223188e-01},
                {0.136532137, 0.41016521, 4.533027e-01},
                {0.119398467, 0.39641687, 4.841847e-01},
                {0.103924943, 0.38133145, 5.147436e-01},
                {0.090050764, 0.36517386, 5.447754e-01},
                {0.077695579, 0.34820743, 5.740970e-01},
                {0.066764383, 0.33068616, 6.025495e-01},
                {0.057152250, 0.31284841, 6.299993e-01},
                {0.048748657, 0.29491219, 6.563392e-01},
                {0.041441259, 0.27707188, 6.814869e-01},
                {0.035119027, 0.25949646, 7.053845e-01},
                {0.029674735, 0.24232893, 7.279963e-01},
                {0.025006806, 0.22568676, 7.493064e-01},
                {0.021020584, 0.20966314, 7.693163e-01},
                {0.017629110, 0.19432879, 7.880421e-01},
                {0.014753474, 0.17973411, 8.055124e-01},
                {0.012322848, 0.16591160, 8.217655e-01},
                {0.010274261, 0.15287825, 8.368475e-01},
                {0.008552201, 0.14063793, 8.508099e-01},
                {0.007108100, 0.12918365, 8.637082e-01},
                {0.005899750, 0.11849965, 8.756006e-01},
                {0.004890688, 0.10856326, 8.865460e-01},
                {0.004049584, 0.09934654, 8.966039e-01},
                {0.003349646, 0.09081773, 9.058326e-01},
                {0.002768061, 0.08294247, 9.142895e-01},
                {0.002285483, 0.07568481, 9.220297e-01},
                {0.001885557, 0.06900806, 9.291064e-01},
                {0.001554503, 0.06287546, 9.355700e-01}
        };

        double[] step = {-0.5, 0.5};
        IrmPCM model = new IrmPCM(0.8, step, 1.0);

        assertEquals("  Number of categories test",  3, model.getNcat());
        assertEquals("  Number of parameters test",  3, model.getNumberOfParameters());

        for(int i=0;i<theta.length;i++){
            assertEquals("  Probability test at theta " + i, prob[i][0], model.probability(theta[i], 0), 1e-6);
            assertEquals("  Probability test at theta " + i, prob[i][1], model.probability(theta[i], 1), 1e-6);
            assertEquals("  Probability test at theta " + i, prob[i][2], model.probability(theta[i], 2), 1e-6);
        }

    }

}

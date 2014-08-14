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
package com.itemanalysis.psychometrics.irt.model;

import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

public class IrmGPCMTest {

    //values in these arrays are from a function written in R
    double[] theta1 = {-4.0, -3.5, -3.0, -2.5, -2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0,  2.5, 3.0, 3.5, 4.0};
    double[] p1 = {9.939400e-01, 9.786447e-01, 9.275415e-01, 7.813066e-01, 4.984779e-01,
            2.147186e-01, 6.743578e-02, 1.649079e-02, 2.667544e-03, 2.087998e-04,
            7.981910e-06, 2.098732e-07, 4.838244e-09, 1.072253e-10, 2.349772e-12, 5.133128e-14, 1.120353e-15};

    double[] p2 = {6.059800e-03, 2.135250e-02, 7.242399e-02, 2.183213e-01, 4.984779e-01,
            7.684136e-01, 8.636571e-01, 7.558191e-01, 4.375357e-01, 1.225624e-01,
            1.676716e-02, 1.577739e-03, 1.301642e-04, 1.032349e-05, 8.096183e-07, 6.329396e-08, 4.943793e-09};

    double[] p3 = {2.252447e-07, 2.840340e-06, 3.447701e-05, 3.719364e-04, 3.039093e-03,
            1.676558e-02, 6.743578e-02, 2.111993e-01, 4.375357e-01, 4.386144e-01,
            2.147388e-01, 7.231220e-02, 2.134978e-02, 6.059739e-03, 1.700721e-03, 4.758176e-04, 1.330038e-04};

    double[] p4 = {2.339513e-12, 1.055764e-10, 4.586183e-09, 1.770582e-07, 5.177459e-06,
            1.022155e-04, 1.471343e-03, 1.649079e-02, 1.222610e-01, 4.386144e-01,
            7.684861e-01, 9.261099e-01, 9.785201e-01, 9.939299e-01, 9.982985e-01, 9.995241e-01, 9.998670e-01};

    double[] ev = {0.006060251, 0.021358177, 0.072492956, 0.219065670, 0.504571583, 0.802251448, 1.002942687,
            1.227690066, 1.679390226, 2.315634339, 2.751702926, 2.924531692, 2.978389880, 2.993919614,
            2.998297660, 2.999524056, 2.999866986};

    //parameter values
    double discrimination = 1.5;
    double[] step = {-2.0, 0.0, 0.5};
    int[] response = {0, 1, 2, 3};

    @Test
    public void probstepTest(){
        System.out.println("Step Test");
        IrmGPCM irm = new IrmGPCM(discrimination, step, 1.7);
        double[] s = irm.getStepParameters();
        for(int i=0;i<s.length;i++){
            assertEquals("Step Test: " + (i+1), step[i], s[i], 1e-15);
        }
    }

     @Test
    public void probTest1(){
        System.out.println("Probability Test1");
        IrmGPCM irm = new IrmGPCM(discrimination, step, 1.7);
        assertEquals("Probability Test, Cat 1, at theta: 0", 0.002667544, irm.probability(0.0, 0), 1e-6);
        assertEquals("Probability Test, Cat 2, at theta: 0", 0.4375357, irm.probability(0.0, 1), 1e-6);
        assertEquals("Probability Test, Cat 3, at theta: 0", 0.4375357, irm.probability(0.0, 2), 1e-6);
        assertEquals("Probability Test, Cat 4, at theta: 0", 0.122261, irm.probability(0.0, 3), 1e-6);

    }


    @Test
    public void probTest2(){
        System.out.println("Probability Test2");
        IrmGPCM irm = new IrmGPCM(discrimination, step, 1.7);
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
        IrmGPCM irm = new IrmGPCM(discrimination, step, 1.7);
        assertEquals("Expected value test 1", 1.67939, irm.expectedValue(0.0), 1e-5);
    }

    @Test
    public void expectedValueTest2(){
        System.out.println("Expected Value Test2");
        IrmGPCM irm = new IrmGPCM(discrimination, step, 1.7);

        for(int i=0;i<theta1.length;i++){
            assertEquals("Expected value test at theta: " + (i+1), ev[i], irm.expectedValue(theta1[i]), 1e-5);
        }

    }

    @Test
    public void transformationTest1(){
        System.out.println("Transformation test: X->Y Linear transformation");
        IrmGPCM irm = new IrmGPCM(discrimination, step, 1.7);
        double prob1 = 0;
        double prob2 = 0;
        double prob3 = 0;
        double prob4 = 0;
        for(int i=0;i<theta1.length;i++){
            prob1 = irm.tStarProbability(theta1[i], response[0], 0.5, 1.2);
            prob2 = irm.tStarProbability(theta1[i], response[1], 0.5, 1.2);
            prob3 = irm.tStarProbability(theta1[i], response[2], 0.5, 1.2);
            prob4 = irm.tStarProbability(theta1[i], response[3], 0.5, 1.2);
            irm.scale(0.5, 1.2);
            assertEquals("Probability Test, Cat 1, at theta: " + (i+1), irm.probability(theta1[i], 0), prob1, 1e-6);
            assertEquals("Probability Test, Cat 2, at theta: " + (i+1), irm.probability(theta1[i], 1), prob2, 1e-6);
            assertEquals("Probability Test, Cat 3, at theta: " + (i+1), irm.probability(theta1[i], 2), prob3, 1e-6);
            assertEquals("Probability Test, Cat 4, at theta: " + (i+1), irm.probability(theta1[i], 3), prob4, 1e-6);
        }
    }

    @Test
    public void transformationTest2(){
        System.out.println("Transformation test: X->Y->X undo transformation");
        IrmGPCM irm = new IrmGPCM(discrimination, step, 1.7);
        double prob1 = 0;
        double prob2 = 0;
        double prob3 = 0;
        double prob4 = 0;
        for(int i=0;i<theta1.length;i++){
            prob1 = irm.probability(theta1[i], response[0]);
            prob2 = irm.probability(theta1[i], response[1]);
            prob3 = irm.probability(theta1[i], response[2]);
            prob4 = irm.probability(theta1[i], response[3]);
            irm.scale(0.5, 1.2);
            assertEquals("Probability Test, Cat 1, at theta: " + (i+1), irm.tSharpProbability(theta1[i], response[0], 0.5, 1.2), prob1, 1e-6);
            assertEquals("Probability Test, Cat 2, at theta: " + (i+1), irm.tSharpProbability(theta1[i], response[1], 0.5, 1.2), prob2, 1e-6);
            assertEquals("Probability Test, Cat 3, at theta: " + (i+1), irm.tSharpProbability(theta1[i], response[2], 0.5, 1.2), prob3, 1e-6);
            assertEquals("Probability Test, Cat 4, at theta: " + (i+1), irm.tSharpProbability(theta1[i], response[3], 0.5, 1.2), prob4, 1e-6);
        }
    }

    /**
     * True values of the gradient were computed numerically using the numDeriv package in R.
     * The calls were made using the code below.
     *
     * library(numDeriv)
     *
     * probNumerator<-function(theta, a, b, D){
     *     ncat<-length(b)
     *     v<-vector(length=ncat)
     *     for(k in 1:ncat){
     *        v[k]<-exp(D*a*(k*theta-sum(b[1:k])))
     *     }
     *     v
     * }
     *
     * gpcm<-function(theta, x, k, D){
     *     a<-x[1]
     *     b<-x[2:length(x)]
     *     f<-probNumerator(theta, a, b, D)
     *     bot<-sum(f)
     *     prob<-f[k]/bot
     *     prob
     * }
     *
     * theta<-1
     * a<-1.2
     * b<-c(0, -0.5, 0.5)
     * grad(gpcm, x=c(a,b), theta=theta, k=1, D=1.0)
     * grad(gpcm, x=c(a,b), theta=theta, k=2, D=1.0)
     * grad(gpcm, x=c(a,b), theta=theta, k=3, D=1.0)
     *
     */
    @Test
    public void gradientTest1(){
        System.out.println("Gradient test 1: Three categories");
        double[] step = {-0.5, 0.5};
        IrmGPCM irm = new IrmGPCM(1.2, step, 1.0);

        double[] grad = null;

        //Gradient for first response category
        grad = irm.gradient(1.0, 0);
        //System.out.println(Arrays.toString(grad));
        assertEquals("First category, Derivative wrt a", -9.527941e-02, grad[0], 1e-7);
        assertEquals("First category, Derivative wrt b1", 8.595784e-13, grad[1], 1e-7);
        assertEquals("First category, Derivative wrt b2", 6.272412e-02, grad[2], 1e-7);
        assertEquals("First category, Derivative wrt b3", 4.049822e-02, grad[3], 1e-7);

        //Gradient for second response category
        grad = irm.gradient(1.0, 1);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Second category, Derivative wrt  a", -7.430095e-02, grad[0], 1e-7);
        assertEquals("Second category, Derivative wrt b1", -3.318921e-14, grad[1], 1e-7);
        assertEquals("Second category, Derivative wrt b2", -2.222590e-02, grad[2], 1e-7);
        assertEquals("Second category, Derivative wrt b3", 2.450000e-01, grad[3], 1e-7);

        //Gradient for third response category
        grad = irm.gradient(1.0, 2);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Third category, Derivative wrt a", 1.695804e-01 , grad[0], 1e-7);
        assertEquals("Third category, Derivative wrt b1", 3.328711e-14, grad[1], 1e-7);
        assertEquals("Third category, Derivative wrt b2", -4.049822e-02, grad[2], 1e-7);
        assertEquals("Third category, Derivative wrt b3", -2.854982e-01, grad[3], 1e-7);

    }

    /**
     * True values of the gradient were computed numerically using the numDeriv package in R.
     * The calls were made using the code below.
     *
     * library(numDeriv)
     *
     * probNumerator<-function(theta, a, b, D){
     *     ncat<-length(b)
     *     v<-vector(length=ncat)
     *     for(k in 1:ncat){
     *        v[k]<-exp(D*a*(k*theta-sum(b[1:k])))
     *     }
     *     v
     * }
     *
     * gpcm<-function(theta, x, k, D){
     *     a<-x[1]
     *     b<-x[2:length(x)]
     *     f<-probNumerator(theta, a, b, D)
     *     bot<-sum(f)
     *     prob<-f[k]/bot
     *     prob
     * }
     *
     * theta<- -0.5
     * a<- 0.7
     * b<-c(0, -1.2, -0.8, 0.2, -0.4)
     * grad(gpcm, x=c(a,b), theta=theta, k=1, D=1.7)
     * grad(gpcm, x=c(a,b), theta=theta, k=2, D=1.7)
     * grad(gpcm, x=c(a,b), theta=theta, k=3, D=1.7)
     * grad(gpcm, x=c(a,b), theta=theta, k=4, D=1.7)
     * grad(gpcm, x=c(a,b), theta=theta, k=5, D=1.7)
     *
     */
    @Test
    public void gradientTest2(){
        System.out.println("Gradient test 2: Five categories");
        double[] step = {-1.2, -0.8, 0.2, -0.4};
        IrmGPCM irm = new IrmGPCM(0.7, step, 1.7);

        double theta = -0.5;
        double[] grad = null;

        //Gradient for first response category
        grad = irm.gradient(theta, 0);
//        System.out.println(Arrays.toString(grad));
        assertEquals("First category, Derivative wrt a", -1.100249e-01, grad[0], 1e-7);
        assertEquals("First category, Derivative wrt b1", 2.713634e-13, grad[1], 1e-7);
        assertEquals("First category, Derivative wrt b2", 1.143600e-01, grad[2], 1e-7);
        assertEquals("First category, Derivative wrt b3", 8.260977e-02, grad[3], 1e-7);
        assertEquals("First category, Derivative wrt b4", 3.723752e-02, grad[4], 1e-7);
        assertEquals("First category, Derivative wrt b5", 1.751225e-02, grad[5], 1e-7);

        //Gradient for second response category
        grad = irm.gradient(theta, 1);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Second category, Derivative wrt  a", 4.172185e-02, grad[0], 1e-7);
        assertEquals("Second category, Derivative wrt b1", -4.895163e-17, grad[1], 1e-7);
        assertEquals("Second category, Derivative wrt b2", -3.175025e-02, grad[2], 1e-7);
        assertEquals("Second category, Derivative wrt b3", 1.900197e-01, grad[3], 1e-7);
        assertEquals("Second category, Derivative wrt b4", 8.565408e-02, grad[4], 1e-7);
        assertEquals("Second category, Derivative wrt b5", 4.028183e-02, grad[5], 1e-7);

        //Gradient for third response category
        grad = irm.gradient(theta, 2);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Third category, Derivative wrt a", 2.401718e-01, grad[0], 1e-7);
        assertEquals("Third category, Derivative wrt b1", -5.096844e-13, grad[1], 1e-7);
        assertEquals("Third category, Derivative wrt b2", -4.537225e-02, grad[2], 1e-7);
        assertEquals("Third category, Derivative wrt b3", -1.497379e-01, grad[3], 1e-7);
        assertEquals("Third category, Derivative wrt b4", 1.224028e-01, grad[4], 1e-7);
        assertEquals("Third category, Derivative wrt b5", 5.756419e-02, grad[5], 1e-7);

        //Gradient for fourth response category
        grad = irm.gradient(theta, 3);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Fourth category, Derivative wrt a", -0.07873679, grad[0], 1e-7);
        assertEquals("Fourth category, Derivative wrt b1", 0.00000000, grad[1], 1e-7);
        assertEquals("Fourth category, Derivative wrt b2", -0.01972527, grad[2], 1e-7);
        assertEquals("Fourth category, Derivative wrt b3", -0.06509752, grad[3], 1e-7);
        assertEquals("Fourth category, Derivative wrt b4", -0.12993608, grad[4], 1e-7);
        assertEquals("Fourth category, Derivative wrt b5", 0.02502563, grad[5], 1e-7);

        //Gradient for fifth response category
        grad = irm.gradient(theta, 4);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Fifth category, Derivative wrt a", -9.313197e-02, grad[0], 1e-7);
        assertEquals("Fifth category, Derivative wrt b1", 4.895163e-17, grad[1], 1e-7);
        assertEquals("Fifth category, Derivative wrt b2", -1.751225e-02, grad[2], 1e-7);
        assertEquals("Fifth category, Derivative wrt b3", -5.779408e-02, grad[3], 1e-7);
        assertEquals("Fifth category, Derivative wrt b4", -1.153583e-01, grad[4], 1e-7);
        assertEquals("Fifth category, Derivative wrt b5", -1.403839e-01, grad[5], 1e-7);

    }

//    @Test
//    public void derivThetaTest(){
//        System.out.println("Derivative wrt Theta test 1");
//        IrmGPCM irm = new IrmGPCM(discrimination, step, 1.7);
//
//        double d1 = irm.derivTheta(0.0, 1);
//        double d2 = irm.derivTheta2(0.0, 1);
//
//        System.out.println("    d1: " + d1);
//        System.out.println("    d2: " + d2);
//
//        assertEquals("Derivative comparison 1", 0.0, d1-d2, 1e-10);
//
//    }


}

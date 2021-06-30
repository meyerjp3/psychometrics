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

import com.itemanalysis.psychometrics.irt.estimation.ItemResponseFileSummary;
import com.itemanalysis.psychometrics.irt.estimation.ItemResponseVector;
import org.junit.Test;

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
    double[] step = {0.0, -2.0, 0.0, 0.5};
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
    public void probTest1b(){
        System.out.println("Probability Test1 with different call");
        double[] initialValue = {0,0,0,0};
        double[] iparam = {1.5, 0.0, -2.0, 0.0, 0.5};
        IrmGPCM irm = new IrmGPCM(1.0, initialValue, 1.7);
        assertEquals("Probability Test, Cat 1, at theta: 0", 0.002667544, irm.probability(0.0, iparam, 0), 1e-6);
        assertEquals("Probability Test, Cat 2, at theta: 0", 0.4375357, irm.probability(0.0, iparam, 1), 1e-6);
        assertEquals("Probability Test, Cat 3, at theta: 0", 0.4375357, irm.probability(0.0, iparam, 2), 1e-6);
        assertEquals("Probability Test, Cat 4, at theta: 0", 0.122261, irm.probability(0.0, iparam, 3), 1e-6);

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
        System.out.println("Transformation test: X->Y Linear transformation");
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
     * True values of the gradientAt were computed numerically using the numDeriv package in R.
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
        double[] step = {0, -0.5, 0.5};
        IrmGPCM irm = new IrmGPCM(1.2, step, 1.0);

        double[] grad = null;

        //Gradient for first response category
        grad = irm.gradient(1.0, 0);
//        System.out.println(Arrays.toString(grad));
        assertEquals("First category, Derivative wrt a", -9.527941e-02, grad[0], 1e-7);
        assertEquals("First category, Derivative wrt b1", 8.595784e-13, grad[1], 1e-7);
        assertEquals("First category, Derivative wrt b2", 6.272412e-02, grad[2], 1e-7);
        assertEquals("First category, Derivative wrt b3", 4.049822e-02, grad[3], 1e-7);

        //Gradient for second response category
        grad = irm.gradient(1.0, 1);
//        System.out.println(Arrays.toString(grad));
        assertEquals("Second category, Derivative wrt  a", -7.430095e-02, grad[0], 1e-7);
        assertEquals("Second category, Derivative wrt b1", -3.318921e-14, grad[1], 1e-7);
        assertEquals("Second category, Derivative wrt b2", -2.222590e-02, grad[2], 1e-7);
        assertEquals("Second category, Derivative wrt b3", 2.450000e-01, grad[3], 1e-7);

        //Gradient for third response category
        grad = irm.gradient(1.0, 2);
//        System.out.println(Arrays.toString(grad));
        assertEquals("Third category, Derivative wrt a", 1.695804e-01 , grad[0], 1e-7);
        assertEquals("Third category, Derivative wrt b1", 3.328711e-14, grad[1], 1e-7);
        assertEquals("Third category, Derivative wrt b2", -4.049822e-02, grad[2], 1e-7);
        assertEquals("Third category, Derivative wrt b3", -2.854982e-01, grad[3], 1e-7);

    }

    /**
     * True values of the gradientAt were computed numerically using the numDeriv package in R.
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
        double[] step = {0, -1.2, -0.8, 0.2, -0.4};
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

    /**
     * Same test as above but call to gradientAt is done differently.
     */
    @Test
    public void gradientTest3(){
        System.out.println("Gradient test 3: Five categories with different call");
        double[] step = {0.0, -1.2, -0.8, 0.2, -0.4};
        IrmGPCM irm = new IrmGPCM(0.7, step, 1.7);

        double[] iparam = new double[step.length+1];
        iparam[0] = 0.7;
        for(int k=0;k<step.length;k++){
            iparam[k+1] = step[k];
        }

        double theta = -0.5;
        double[] grad = null;

        //Gradient for first response category
        grad = irm.gradient(theta, iparam, 0, 1.7);
//        System.out.println(Arrays.toString(grad));
        assertEquals("First category, Derivative wrt a", -1.100249e-01, grad[0], 1e-7);
        assertEquals("First category, Derivative wrt b1", 2.713634e-13, grad[1], 1e-7);
        assertEquals("First category, Derivative wrt b2", 1.143600e-01, grad[2], 1e-7);
        assertEquals("First category, Derivative wrt b3", 8.260977e-02, grad[3], 1e-7);
        assertEquals("First category, Derivative wrt b4", 3.723752e-02, grad[4], 1e-7);
        assertEquals("First category, Derivative wrt b5", 1.751225e-02, grad[5], 1e-7);

        //Gradient for second response category
        grad = irm.gradient(theta, iparam, 1, 1.7);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Second category, Derivative wrt  a", 4.172185e-02, grad[0], 1e-7);
        assertEquals("Second category, Derivative wrt b1", -4.895163e-17, grad[1], 1e-7);
        assertEquals("Second category, Derivative wrt b2", -3.175025e-02, grad[2], 1e-7);
        assertEquals("Second category, Derivative wrt b3", 1.900197e-01, grad[3], 1e-7);
        assertEquals("Second category, Derivative wrt b4", 8.565408e-02, grad[4], 1e-7);
        assertEquals("Second category, Derivative wrt b5", 4.028183e-02, grad[5], 1e-7);

        //Gradient for third response category
        grad = irm.gradient(theta, iparam, 2, 1.7);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Third category, Derivative wrt a", 2.401718e-01, grad[0], 1e-7);
        assertEquals("Third category, Derivative wrt b1", -5.096844e-13, grad[1], 1e-7);
        assertEquals("Third category, Derivative wrt b2", -4.537225e-02, grad[2], 1e-7);
        assertEquals("Third category, Derivative wrt b3", -1.497379e-01, grad[3], 1e-7);
        assertEquals("Third category, Derivative wrt b4", 1.224028e-01, grad[4], 1e-7);
        assertEquals("Third category, Derivative wrt b5", 5.756419e-02, grad[5], 1e-7);

        //Gradient for fourth response category
        grad = irm.gradient(theta, iparam, 3, 1.7);
        //System.out.println(Arrays.toString(grad));
        assertEquals("Fourth category, Derivative wrt a", -0.07873679, grad[0], 1e-7);
        assertEquals("Fourth category, Derivative wrt b1", 0.00000000, grad[1], 1e-7);
        assertEquals("Fourth category, Derivative wrt b2", -0.01972527, grad[2], 1e-7);
        assertEquals("Fourth category, Derivative wrt b3", -0.06509752, grad[3], 1e-7);
        assertEquals("Fourth category, Derivative wrt b4", -0.12993608, grad[4], 1e-7);
        assertEquals("Fourth category, Derivative wrt b5", 0.02502563, grad[5], 1e-7);

        //Gradient for fifth response category
        grad = irm.gradient(theta, iparam, 4, 1.7);
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

    /**
     * True results obtained from plink package in R.
     *
     * library(plink)
     * theta<-seq(-4,4, by=.1)
     * iparam<-matrix(c(1.3, 0.3, 1.3), nrow=1, byrow=TRUE)
     * pr<-gpcm(x=iparam, cat=3, theta=theta, D=1.7, location=FALSE)
     *
     */
    @Test
    public void probabilityTest(){
        System.out.println("Probability test");

        double[] theta = {-4.0, -3.9, -3.8, -3.7, -3.6, -3.5, -3.4, -3.3, -3.2, -3.1, -3.0, -2.9, -2.8, -2.7, -2.6,
                -2.5, -2.4, -2.3, -2.2, -2.1, -2.0, -1.9, -1.8, -1.7, -1.6, -1.5, -1.4, -1.3, -1.2, -1.1,
                -1.0, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1,  0.0,  0.1,  0.2,  0.3,  0.4,
                0.5,  0.6,  0.7,  0.8,  0.9,  1.0,  1.1, 1.2,  1.3,  1.4,  1.5,  1.6,  1.7,  1.8,  1.9,
                2.0,  2.1,  2.2,  2.3,  2.4,  2.5,  2.6,  2.7,  2.8,  2.9,  3.0,  3.1,  3.2,  3.3,  3.4,
                3.5,  3.6,  3.7,  3.8,  3.9,  4.0};
        double[][] prob = {
                {9.99925e-01,7.46220e-05,6.10908e-10},
                {9.99907e-01,9.30761e-05,9.50443e-10},
                {9.99884e-01,1.16093e-04,1.47868e-09},
                {9.99855e-01,1.44802e-04,2.30049e-09},
                {9.99819e-01,1.80608e-04,3.57901e-09},
                {9.99775e-01,2.25267e-04,5.56803e-09},
                {9.99719e-01,2.80965e-04,8.66234e-09},
                {9.99650e-01,3.50430e-04,1.34761e-08},
                {9.99563e-01,4.37061e-04,2.09645e-08},
                {9.99455e-01,5.45098e-04,3.26133e-08},
                {9.99320e-01,6.79822e-04,5.07335e-08},
                {9.99152e-01,8.47815e-04,7.89187e-08},
                {9.98943e-01,1.05728e-03,1.22757e-07},
                {9.98681e-01,1.31842e-03,1.90938e-07},
                {9.98356e-01,1.64396e-03,2.96967e-07},
                {9.97950e-01,2.04972e-03,4.61838e-07},
                {9.97444e-01,2.55537e-03,7.18170e-07},
                {9.96814e-01,3.18535e-03,1.11663e-06},
                {9.96028e-01,3.97004e-03,1.73591e-06},
                {9.95050e-01,4.94706e-03,2.69810e-06},
                {9.93833e-01,6.16303e-03,4.19261e-06},
                {9.92318e-01,7.67558e-03,6.51299e-06},
                {9.90434e-01,9.55575e-03,1.01138e-05},
                {9.88093e-01,1.18909e-02,1.56980e-05},
                {9.85187e-01,1.47882e-02,2.43513e-05},
                {9.81584e-01,1.83782e-02,3.77476e-05},
                {9.77122e-01,2.28194e-02,5.84614e-05},
                {9.71607e-01,2.83025e-02,9.04418e-05},
                {9.64805e-01,3.50553e-02,1.39726e-04},
                {9.56438e-01,4.33461e-02,2.15502e-04},
                {9.46182e-01,5.34868e-02,3.31686e-04},
                {9.33658e-01,6.58323e-02,5.09213e-04},
                {9.18445e-01,8.07761e-02,7.79332e-04},
                {9.00073e-01,9.87386e-02,1.18824e-03},
                {8.78051e-01,1.20146e-01,1.80346e-03},
                {8.51883e-01,1.45394e-01,2.72223e-03},
                {8.21114e-01,1.74803e-01,4.08231e-03},
                {7.85378e-01,2.08547e-01,6.07490e-03},
                {7.44466e-01,2.46575e-01,8.95907e-03},
                {6.98397e-01,2.88527e-01,1.30761e-02},
                {6.47487e-01,3.33652e-01,1.88610e-02},
                {5.92393e-01,3.80760e-01,2.68474e-02},
                {5.34123e-01,4.28216e-01,3.76611e-02},
                {4.74001e-01,4.74001e-01,5.19982e-02},
                {4.13565e-01,5.15850e-01,7.05849e-02},
                {3.54440e-01,5.51443e-01,9.41170e-02},
                {2.98175e-01,5.78640e-01,1.23184e-01},
                {2.46105e-01,5.95712e-01,1.58184e-01},
                {1.99234e-01,6.01532e-01,1.99234e-01},
                {1.58184e-01,5.95712e-01,2.46105e-01},
                {1.23184e-01,5.78640e-01,2.98175e-01},
                {9.41170e-02,5.51443e-01,3.54440e-01},
                {7.05849e-02,5.15850e-01,4.13565e-01},
                {5.19982e-02,4.74001e-01,4.74001e-01},
                {3.76611e-02,4.28216e-01,5.34123e-01},
                {2.68474e-02,3.80760e-01,5.92393e-01},
                {1.88610e-02,3.33652e-01,6.47487e-01},
                {1.30761e-02,2.88527e-01,6.98397e-01},
                {8.95907e-03,2.46575e-01,7.44466e-01},
                {6.07490e-03,2.08547e-01,7.85378e-01},
                {4.08231e-03,1.74803e-01,8.21114e-01},
                {2.72223e-03,1.45394e-01,8.51883e-01},
                {1.80346e-03,1.20146e-01,8.78051e-01},
                {1.18824e-03,9.87386e-02,9.00073e-01},
                {7.79332e-04,8.07761e-02,9.18445e-01},
                {5.09213e-04,6.58323e-02,9.33658e-01},
                {3.31686e-04,5.34868e-02,9.46182e-01},
                {2.15502e-04,4.33461e-02,9.56438e-01},
                {1.39726e-04,3.50553e-02,9.64805e-01},
                {9.04418e-05,2.83025e-02,9.71607e-01},
                {5.84614e-05,2.28194e-02,9.77122e-01},
                {3.77476e-05,1.83782e-02,9.81584e-01},
                {2.43513e-05,1.47882e-02,9.85187e-01},
                {1.56980e-05,1.18909e-02,9.88093e-01},
                {1.01138e-05,9.55575e-03,9.90434e-01},
                {6.51299e-06,7.67558e-03,9.92318e-01},
                {4.19261e-06,6.16303e-03,9.93833e-01},
                {2.69810e-06,4.94706e-03,9.95050e-01},
                {1.73591e-06,3.97004e-03,9.96028e-01},
                {1.11663e-06,3.18535e-03,9.96814e-01},
                {7.18170e-07,2.55537e-03,9.97444e-01}
        };

        double[] step = {0.0, 0.3, 1.3};
        IrmGPCM model = new IrmGPCM(1.3, step, 1.7);

        assertEquals("  Number of categories test",  3, model.getNcat());
        assertEquals("  Number of parameters test",  4, model.getNumberOfParameters());

        for(int i=0;i<theta.length;i++){
            assertEquals("  Probability test at theta " + i, prob[i][0], model.probability(theta[i], 0), 1e-6);
            assertEquals("  Probability test at theta " + i, prob[i][1], model.probability(theta[i], 1), 1e-6);
            assertEquals("  Probability test at theta " + i, prob[i][2], model.probability(theta[i], 2), 1e-6);
        }


    }




}

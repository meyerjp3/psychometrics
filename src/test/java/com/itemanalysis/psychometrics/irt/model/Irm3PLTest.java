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
import static junit.framework.Assert.assertFalse;

public class Irm3PLTest {


    @Test
    public void probTest1(){
        Irm3PL irm = new Irm3PL(1.0, 0.0, 0.0);
        System.out.println("Rasch Test");
        assertEquals("Rasch test", 0.5, irm.probability(0.0, 1), 1e-5);

        System.out.println("Expected Value Test");
        assertEquals("Expected Value Test", irm.expectedValue(0.0), irm.probability(0.0, 1), 1e-5);
    }

    @Test
    public void transformTest(){
        System.out.println("3PL transform test");

        Irm3PL irm = new Irm3PL(1.2, 0.8, 0.2);

        double prob1 = irm.expectedValue(0.5);
        irm.scale(0.5, 1.2);
        double prob2 = irm.tSharpExpectedValue(0.5, 0.5, 1.2);

//        System.out.println("  prob1=" + prob1 + " prob2=" + prob2 + " equal? " + (prob1==prob2));
        assertEquals("Transformation Test 1", prob1-prob2, 0.0, 1e-5);

        double prob3 = irm.expectedValue(0.5);

//        System.out.println("  prob1=" + prob1 + " prob3=" + prob3 + " equal? " + (prob1==prob3));
        assertFalse("Transformation Test 2", prob3==prob1);

    }

    @Test
    public void derivThetaTest(){
        System.out.println("Derivative wrt theta test 3PLM: Test 1");
        Irm3PL model1 = new Irm3PL(1.2, 0.8, 0.2, 1.0);

        double d1 = model1.derivTheta(0.0);
        System.out.println("    deriv wrt theta: " + d1);

        //test value from EquatingRecipes function. Numerical value from mathematica is 0.192208
        assertEquals("Deriv theta test 1", 0.1922079936749124, d1, 1e-15);

        System.out.println("Derivative wrt theta test 3PLM: Test 2");
        model1 = new Irm3PL(1.0, -2.0, 0.1, 1.7);

        d1 = model1.derivTheta(0.0);
        System.out.println("    deriv wrt theta: " + d1);

        //test value from EquatingRecipes function.
        assertEquals("Deriv theta test 2", 0.047816275517293, d1, 1e-15);

    }

    @Test
    public void gradientTest1(){
        System.out.println("Gradient Test 1");
        Irm3PL model = new Irm3PL(0.9, 0.0, 0.2, 1.7);

        double[] g1 = model.gradient(0.0, 0);
//        System.out.println(Arrays.toString(g1));
        assertEquals("First category, Derivative wrt a", 0.0, g1[0], 1e-7);
        assertEquals("First category, Derivative wrt b", 0.306, g1[1], 1e-7);
        assertEquals("First category, Derivative wrt c", -0.50, g1[2], 1e-7);

        g1 = model.gradient(0.0, 1);
//        System.out.println(Arrays.toString(g1));
        assertEquals("First category, Derivative wrt a", 0.0, g1[0], 1e-7);
        assertEquals("First category, Derivative wrt b", -0.306, g1[1], 1e-7);
        assertEquals("First category, Derivative wrt c", 0.50, g1[2], 1e-7);

    }

    @Test
    public void gradientTest2(){
        System.out.println("Gradient Test 2");
        Irm3PL model = new Irm3PL(1.5, -0.3, 0.15, 1.0);

        double[] g1 = model.gradient(0.8, 0);
//        System.out.println(Arrays.toString(g1));
        assertEquals("First category, Derivative wrt a", -0.1263679, g1[0], 1e-7);
        assertEquals("First category, Derivative wrt b", 0.1723199, g1[1], 1e-7);
        assertEquals("First category, Derivative wrt c", -0.1611089, g1[2], 1e-7);

        g1 = model.gradient(0.8, 1);
//        System.out.println(Arrays.toString(g1));
        assertEquals("First category, Derivative wrt a", 0.1263679, g1[0], 1e-7);
        assertEquals("First category, Derivative wrt b", -0.1723199, g1[1], 1e-7);
        assertEquals("First category, Derivative wrt c", 0.1611089, g1[2], 1e-7);

    }

    /**
     * True results obtained with plink package in R.
     *
     * library(plink)
     * theta<-seq(-4,4, by=.1)
     * iparam<-matrix(c(0.8, -1.2, 0.1), nrow=1, byrow=TRUE)
     * pr<-drm(x=iparam, theta=theta, D=1.0)
     * pr@prob
     *
     */
    @Test
    public void probabilityTest2(){
        System.out.println("Probability test D = 1.0");

        double[] theta = {
                -4.0, -3.9, -3.8, -3.7, -3.6, -3.5, -3.4, -3.3, -3.2, -3.1, -3.0, -2.9, -2.8, -2.7, -2.6,
                -2.5, -2.4, -2.3, -2.2, -2.1, -2.0, -1.9, -1.8, -1.7, -1.6, -1.5, -1.4, -1.3, -1.2, -1.1,
                -1.0, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1,  0.0,  0.1,  0.2,  0.3,  0.4,
                 0.5,  0.6,  0.7,  0.8,  0.9,  1.0,  1.1, 1.2,  1.3,  1.4,  1.5,  1.6,  1.7,  1.8,  1.9,
                 2.0,  2.1,  2.2,  2.3,  2.4,  2.5,  2.6,  2.7,  2.8,  2.9,  3.0,  3.1,  3.2,  3.3,  3.4,
                 3.5,  3.6,  3.7,  3.8,  3.9,  4.0};
        double[] prob = {
                0.1865940, 0.1930604, 0.1999504, 0.2072826, 0.2150754, 0.2233462, 0.2321113,
                0.2413859, 0.2511835, 0.2615154, 0.2723908, 0.2838163, 0.2957952, 0.3083277,
                0.3214102, 0.3350350, 0.3491904, 0.3638600, 0.3790230, 0.3946537, 0.4107219,
                0.4271927, 0.4440269, 0.4611811, 0.4786082, 0.4962577, 0.5140766, 0.5320096,
                0.5500000, 0.5679904, 0.5859234, 0.6037423, 0.6213918, 0.6388189, 0.6559731,
                0.6728073, 0.6892781, 0.7053463, 0.7209770, 0.7361400, 0.7508096, 0.7649650,
                0.7785898, 0.7916723, 0.8042048, 0.8161837, 0.8276092, 0.8384846, 0.8488165,
                0.8586141, 0.8678887, 0.8766538, 0.8849246, 0.8927174, 0.9000496, 0.9069396,
                0.9134060, 0.9194679, 0.9251446, 0.9304550, 0.9354182, 0.9400528, 0.9443769,
                0.9484082, 0.9521640, 0.9556606, 0.9589139, 0.9619392, 0.9647508, 0.9673627,
                0.9697877, 0.9720384, 0.9741264, 0.9760627, 0.9778578, 0.9795215, 0.9810628,
                0.9824904, 0.9838124, 0.9850363, 0.9861691};

        Irm3PL model = new Irm3PL(0.8, -1.2, 0.1, 1.0);

        for(int i=0;i<theta.length;i++){
            assertEquals("  Probability test at theta " + i, prob[i], model.probability(theta[i], 1), 1e-6);
        }

        //Check probability using item parameter array that includes the discrimination and difficulty values.
        //Guessing is fixed to the value set during instantiation of the object, 0.1.
        double[] iparam = {0.8, -1.2};
        for(int i=0;i<theta.length;i++){
            assertEquals("  Probability test at theta using alternate call" + i, prob[i], model.probability(theta[i], iparam, 1, 1.0), 1e-6);
        }

    }

    /**
     * True results obtained with plink package in R.
     *
     * library(plink)
     * theta<-seq(-4,4, by=.1)
     * iparam<-matrix(c(0.8, -1.2, 0.1), nrow=1, byrow=TRUE)
     * pr<-drm(x=iparam, theta=theta, D=1.7)
     * pr@prob
     *
     */
    @Test
    public void probabilityTest3(){
        System.out.println("Probability test D = 1.7");

        double[] theta = {
                -4.0, -3.9, -3.8, -3.7, -3.6, -3.5, -3.4, -3.3, -3.2, -3.1, -3.0, -2.9, -2.8, -2.7, -2.6,
                -2.5, -2.4, -2.3, -2.2, -2.1, -2.0, -1.9, -1.8, -1.7, -1.6, -1.5, -1.4, -1.3, -1.2, -1.1,
                -1.0, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1,  0.0,  0.1,  0.2,  0.3,  0.4,
                0.5,  0.6,  0.7,  0.8,  0.9,  1.0,  1.1, 1.2,  1.3,  1.4,  1.5,  1.6,  1.7,  1.8,  1.9,
                2.0,  2.1,  2.2,  2.3,  2.4,  2.5,  2.6,  2.7,  2.8,  2.9,  3.0,  3.1,  3.2,  3.3,  3.4,
                3.5,  3.6,  3.7,  3.8,  3.9,  4.0};
        double[] prob = {
                0.1195396, 0.1223156, 0.1254746, 0.1290659, 0.1331444, 0.1377703, 0.1430097,
                0.1489348, 0.1556231, 0.1631578, 0.1716264, 0.1811206, 0.1917338, 0.2035601,
                0.2166908, 0.2312121, 0.2472009, 0.2647206, 0.2838163, 0.3045100, 0.3267956,
                0.3506345, 0.3759521, 0.4026352, 0.4305318, 0.4594526, 0.4891745, 0.5194471,
                0.5500000, 0.5805529, 0.6108255, 0.6405474, 0.6694682, 0.6973648, 0.7240479,
                0.7493655, 0.7732044, 0.7954900, 0.8161837, 0.8352794, 0.8527991, 0.8687879,
                0.8833092, 0.8964399, 0.9082662, 0.9188794, 0.9283736, 0.9368422, 0.9443769,
                0.9510652, 0.9569903, 0.9622297, 0.9668556, 0.9709341, 0.9745254, 0.9776844,
                0.9804604, 0.9828978, 0.9850363, 0.9869114, 0.9885545, 0.9899937, 0.9912537,
                0.9923564, 0.9933211, 0.9941649, 0.9949027, 0.9955476, 0.9961113, 0.9966039,
                0.9970344, 0.9974104, 0.9977388, 0.9980257, 0.9982763, 0.9984951, 0.9986862,
                0.9988530, 0.9989987, 0.9991259, 0.9992370
        };

        Irm3PL model = new Irm3PL(0.8, -1.2, 0.1, 1.7);

        for(int i=0;i<theta.length;i++){
            assertEquals("  Probability test at theta " + i, prob[i], model.probability(theta[i], 1), 1e-6);
        }

    }

    @Test
    public void codeTest(){
        IrmGPCM gpcm = new IrmGPCM(0.9, new double[]{0.0, -1.2, 0.5, 1.8}, 1.0);
        System.out.println(gpcm.probability(-0.8, 2));
    }

}

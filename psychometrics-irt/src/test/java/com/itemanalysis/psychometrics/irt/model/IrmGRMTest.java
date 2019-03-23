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

import static junit.framework.Assert.assertEquals;

public class IrmGRMTest {

    double[] theta1 = {-4.0, -3.5, -3.0, -2.5, -2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0,  2.5, 3.0, 3.5, 4.0};

    double[] prob1 = {9.717312e-01, 9.299330e-01, 8.367169e-01, 6.642608e-01, 4.330741e-01,
            2.277641e-01, 1.022345e-01, 4.211597e-02, 1.669254e-02, 6.511713e-03, 2.524260e-03,
            9.761284e-04, 3.771087e-04, 1.456352e-04, 5.623473e-05, 2.171295e-05, 8.383474e-06};
    double[] prob2 = {0.0278098011, 0.0688790630, 0.1602120788, 0.3278238581, 0.5466799549,
            0.7214341739, 0.7760224886, 0.6937063633, 0.5014761604, 0.2868830409, 0.1356409824,
            0.0573135226, 0.0229637298, 0.0089972605, 0.0034937588, 0.0013519317, 0.0005224277};
    double[] prob3 = {0.0003170923, 0.0008204866, 0.0021198199, 0.0054554155, 0.0138995747,
            0.0345288135, 0.0806590264, 0.1642946405, 0.2585879912, 0.2798751527, 0.2033711377,
            0.1085608250, 0.0484316875, 0.0198457181, 0.0078452895, 0.0030570646, 0.0011845495};
    double[] prob4 = {0.0001419143, 0.0003674760, 0.0009512091, 0.0024599165, 0.0063463748,
            0.0162729391, 0.0410839920, 0.0998830289, 0.2232433059, 0.4267300937, 0.6584636197,
            0.8331495239, 0.9282274739, 0.9710113862, 0.9886047169, 0.9955692908, 0.9982846393};


    @Test
    public void cumulativeProbabilityTest(){
        System.out.println("GRM cumulative probability test");
        double a =  1.1196;
        double[] b = {-2.1415, 0.0382, 0.6551};
        double theta = 0.0;

        IrmGRM irmGRM = new IrmGRM(a, b, 1.7);
        double prob1 = irmGRM.cumulativeProbability(theta, 0);
        double prob2 = irmGRM.cumulativeProbability(theta, 1);
        double prob3 = irmGRM.cumulativeProbability(theta, 2);
        double prob4 = irmGRM.cumulativeProbability(theta, 3);

        assertEquals("GRM cumulative probability test for category 1: ", 1.0000000, prob1, 1e-7);
        assertEquals("GRM cumulative probability test for category 2: ", 0.9833075, prob2, 1e-7);
        assertEquals("GRM cumulative probability test for category 3: ", 0.4818313, prob3, 1e-7);
        assertEquals("GRM cumulative probability test for category 4: ", 0.2232433, prob4, 1e-7);

    }

    @Test
    public void probabilityTest(){
        System.out.println("GRM category probability test");
        double a =  1.1196;
        double[] b = {-2.1415, 0.0382, 0.6551};
        double theta = 0.0;

        IrmGRM irmGRM = new IrmGRM(a, b, 1.7);
        double prob1 = irmGRM.probability(theta, 0);
        double prob2 = irmGRM.probability(theta, 1);
        double prob3 = irmGRM.probability(theta, 2);
        double prob4 = irmGRM.probability(theta, 3);

        assertEquals("GRM probability test for category 1: ", 0.01669254, prob1, 1e-7);
        assertEquals("GRM probability test for category 2: ", 0.50147620, prob2, 1e-7);
        assertEquals("GRM probability test for category 3: ", 0.25858800, prob3, 1e-7);
        assertEquals("GRM probability test for category 4: ", 0.22324330, prob4, 1e-7);

    }

    @Test
    public void probabilityTestMultipleTheta(){
        System.out.println("GRM category probability test using multiple thetas");
        double a =  1.1196;
        double[] b = {-2.1415, 0.0382, 0.6551};

        IrmGRM irmGRM = new IrmGRM(a, b, 1.7);
        int n = theta1.length;
        double prob = 0;

//        System.out.println("p1: " + irmGRM.probability(0.0, 0));
//        System.out.println("p2: " + irmGRM.probability(0.0, 1));
//        System.out.println("p3: " + irmGRM.probability(0.0, 2));
//        System.out.println("p4: " + irmGRM.probability(0.0, 3));

        for(int i=0;i<n;i++){
            prob = irmGRM.probability(theta1[i], 0);
            assertEquals("GRM probability test for category 1: ", prob1[i], prob, 1e-7);
        }
        for(int i=0;i<n;i++){
            prob = irmGRM.probability(theta1[i], 1);
            assertEquals("GRM probability test for category 2: ", prob2[i], prob, 1e-7);
        }
        for(int i=0;i<n;i++){
            prob = irmGRM.probability(theta1[i], 2);
            assertEquals("GRM probability test for category 3: ", prob3[i], prob, 1e-7);
        }
        for(int i=0;i<n;i++){
            prob = irmGRM.probability(theta1[i], 3);
            assertEquals("GRM probability test for category 4: ", prob4[i], prob, 1e-7);
        }


    }

    @Test
    public void dericThetaTest(){
        System.out.println("Derivative wrt theta test GRM: Test 1");
        double a =  1.1196;
        double[] b = {-2.1415, 0.0382, 0.6551};
        IrmGRM irmGRM = new IrmGRM(a, b, 1.7);
        double d1 = irmGRM.derivTheta(0.0);
        System.out.println("    deriv wrt theta: " + d1);
        assertEquals("GRM deriv theta test 1: ", 0.8364892, d1, 1e-5);

        System.out.println("Derivative wrt theta test GRM: Test 2");
        a =  0.9;
        double[] b2 = {-1.5, 1.1};
        irmGRM = new IrmGRM(a, b2, 1.7);
        d1 = irmGRM.derivTheta(0.0);
        System.out.println("    deriv wrt theta: " + d1);
        assertEquals("GRM deriv theta test 1: ", 0.3294134, d1, 1e-5);

    }

}

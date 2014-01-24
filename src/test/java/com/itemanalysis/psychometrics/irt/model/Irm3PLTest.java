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

        System.out.println("  prob1=" + prob1 + " prob2=" + prob2 + " equal? " + (prob1==prob2));
        assertEquals("Transformation Test 1", prob1-prob2, 0.0, 1e-5);

        double prob3 = irm.expectedValue(0.5);

        System.out.println("  prob1=" + prob1 + " prob3=" + prob3 + " equal? " + (prob1==prob3));
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

}

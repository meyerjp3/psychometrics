/*
 * Copyright 2013 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.analysis;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class ExampleMultivariateFunctionTest {

    @Test
    public void numericalGradientTest1(){
        System.out.println("Exaple gradientAt test 1");
        ExampleMultivariateFunction function = new ExampleMultivariateFunction(1);

        double[] x = {5.0, 8.0};

        double[] grad = function.gradientAt(x);

        double[] exact = {2*x[0]+x[1],  x[0]};

        assertEquals("  Gradient Test param: 1", exact[0], grad[0], 1e-10);
        assertEquals("  Gradient Test param: 2", exact[1], grad[1], 1e-10);

    }

    @Test
    public void numericalGradientTest2(){
        System.out.println("Exaple gradientAt test 2");
        ExampleMultivariateFunction function = new ExampleMultivariateFunction(2);

        double[] x = {5.0, 8.0};

        double[] grad = function.gradientAt(x);

        double[] exact = new double[2];
        exact[0] = -400*x[0]*(x[1]-x[0]*x[0]) - 2*(1-x[0]); // derivative of rosen() with respect to x
        exact[1] = 200*(x[1]-x[0]*x[0]);

//        System.out.println(exact[0] + "  " + exact[1]);

        assertEquals("  Gradient Test param: 1", exact[0], grad[0], 1e-3);
        assertEquals("  Gradient Test param: 2", exact[1], grad[1], 1e-3);

    }

}

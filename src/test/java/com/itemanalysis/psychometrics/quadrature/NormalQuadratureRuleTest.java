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
package com.itemanalysis.psychometrics.quadrature;

import org.junit.Test;
import static org.junit.Assert.*;

public class NormalQuadratureRuleTest {



    @Test
    public void testQuadrature() {


        NormalQuadratureRule norm = new NormalQuadratureRule(-4.0, 4.0, 31);
        double[] density = norm.evaluate();
        double[] points = norm.getPoints();
        double n = norm.getNumberOfPoints();

        //accuracy determined by precision of values from BILOG-MG
        double[] precisionDensity = {
                1e-8,
                1e-7, 1e-7, 1e-7,
                1e-6, 1e-6, 1e-6,
                1e-5, 1e-5, 1e-5, 1e-5, 1e-5, 1e-5, 1e-5,
                1e-4, 1e-4, 1e-4,
                1e-5, 1e-5, 1e-5, 1e-5, 1e-5, 1e-5, 1e-5,
                1e-6, 1e-6, 1e-6,
                1e-7, 1e-7, 1e-7,
                1e-8};

        //true values from BILOG-MG
        double[] truePoints = {-0.4000E+01, -0.3733E+01, -0.3467E+01, -0.3200E+01, -0.2933E+01,
            -0.2667E+01, -0.2400E+01, -0.2133E+01, -0.1867E+01, -0.1600E+01,
            -0.1333E+01, -0.1067E+01, -0.8000E+00, -0.5333E+00, -0.2667E+00,
            -0.7772E-15,  0.2667E+00,  0.5333E+00,  0.8000E+00,  0.1067E+01,
             0.1333E+01,  0.1600E+01,  0.1867E+01,  0.2133E+01,  0.2400E+01,
             0.2667E+01,  0.2933E+01,  0.3200E+01,  0.3467E+01,  0.3733E+01,
             0.4000E+01};

        //true values from BILOG-MG
        double[] trueDensity = {0.3569E-04, 0.1001E-03, 0.2614E-03, 0.6358E-03, 0.1440E-02,
                0.3039E-02, 0.5972E-02, 0.1093E-01, 0.1863E-01, 0.2958E-01,
                0.4374E-01, 0.6023E-01, 0.7725E-01, 0.9228E-01, 0.1027E+00,
                0.1064E+00, 0.1027E+00, 0.9228E-01, 0.7725E-01, 0.6023E-01,
                0.4374E-01, 0.2958E-01, 0.1863E-01, 0.1093E-01, 0.5972E-02,
                0.3039E-02, 0.1440E-02, 0.6358E-03, 0.2614E-03, 0.1001E-03,
                0.3569E-04};

        //accuracy of this test is limited by values from BILOG-MG
        System.out.println("Testing quad getPoints: ");
        for(int i=0;i<n;i++){
            assertEquals("getPoints test", truePoints[i], points[i], 1e-3);
        }

        //accuracy of this test is limited by values from BILOG-MG
        System.out.println("Testing quad evaluate: ");
        for(int i=0;i<n;i++){
            assertEquals("evaluate test", trueDensity[i], density[i], precisionDensity[i]);
        }


    }

    @Test
    public void testMoments() {

        NormalQuadratureRule norm = new NormalQuadratureRule(100, 15, 0, 200, 501);
        assertEquals("Mean test", 100, norm.getMean(), 1e-8);
        assertEquals("Std. Dev. test", 15, norm.getStandardDeviation(), 1e-8);

        norm = new NormalQuadratureRule(-6, 6, 501);
        assertEquals("Mean test", 0, norm.getMean(), 1e-7);
        assertEquals("Std. Dev. test", 1, norm.getStandardDeviation(), 1e-7);

    }


}

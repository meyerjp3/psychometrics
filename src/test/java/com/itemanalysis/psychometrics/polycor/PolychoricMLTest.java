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
package com.itemanalysis.psychometrics.polycor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PolychoricMLTest {

    public PolychoricMLTest() {
    }

    /**
     * Test of rho method, of class PolychoricML.
     */
    @Test
    public void testAll() throws Exception {
        
        double[][] data = {{155,441,150,21}, {5,94,94,40}};
        double[] expectedRowThresh = {0.7290, 10};
        double[] expectedColThresh = {-0.9947, 0.5100, 1.5450, 10};
        double expectedRowThreshSe = 0.04368;
        double[] expectedColthreshSe = {0.04767, 0.04145, 0.06265};
        double expectedRho = 0.5364;
        double expectedRhoSe = 0.03775;

        PolychoricML p = new PolychoricML();
        p.compute(data);
        double observedRho = p.value();
        double[][] se = p.getVariance();

        System.out.println(p.printVerbose());

        assertEquals("Testing rho: ", expectedRho, observedRho, 1E-3);
        assertEquals("Testing rho se: ", expectedRhoSe, Math.sqrt(se[0][0]), 1E-3);

        //TODO test thresholds
    }

}
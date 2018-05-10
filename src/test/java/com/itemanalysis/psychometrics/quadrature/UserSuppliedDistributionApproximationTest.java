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

import static junit.framework.Assert.assertEquals;

@Deprecated
public class UserSuppliedDistributionApproximationTest {

    double[] points = {-4.0000, -3.1110, -2.2220, -1.3330, -0.4444, 0.4444, 1.3330, 2.2220, 3.1110, 4.0000};
    double[] xDensity = {0.0001008, 0.002760, 0.03021, 0.1420, 0.3149, 0.3158, 0.1542, 0.03596, 0.003925, 0.0001862};
    double[] yDensity = {0.0001173, 0.003242, 0.03449, 0.1471, 0.3148, 0.3110, 0.1526, 0.03406, 0.002510, 0.0001116};

//    @Test
    public void distributionTest1(){
        System.out.println("Density Test 1");
        UserSuppliedQuadratureRule dist = new UserSuppliedQuadratureRule(points, xDensity);
        for(int i=0;i<dist.getNumberOfPoints();i++){
            assertEquals("  Density test at point: " + i, xDensity[i], dist.getDensityAt(i), 1e-5);
        }
    }

//    @Test
    public void distributionTest2(){
        System.out.println("Density Test 2");
        UserSuppliedQuadratureRule dist = new UserSuppliedQuadratureRule(points, yDensity);
        for(int i=0;i<dist.getNumberOfPoints();i++){
            assertEquals("  Density test at point: " + i, yDensity[i], dist.getDensityAt(i), 1e-5);
        }
    }

}

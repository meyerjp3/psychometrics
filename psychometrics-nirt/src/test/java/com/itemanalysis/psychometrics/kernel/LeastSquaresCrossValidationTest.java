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
package com.itemanalysis.psychometrics.kernel;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class LeastSquaresCrossValidationTest {


    /**
     * True values obtained from R function bw.ucv(x)
     *
     * @throws Exception
     */
    @Test
    public void testValue() throws Exception {
        System.out.println("Least squares cross validation test 1");
        double[] x = {103.36,86.85,96.57,100.06,127.03,
            105.2,109.78,121.87,94.71,101.17,
            93.93,110.45,103.3,88.52,119.2,
            108.45,90.39,118.2,117.28,88.95,
            88.97,105.29,118.8,107.22,101.47,
            95.92,105.33,119.37,98.37,127.59,
            102.69,91.83,90.93,106.3,100.18,
            102.22,103.14,102.18,96.12,127.27,
            98.67,108.68,84.45,89.31,87.09,
            134.82,117.41,93.35,95.36,82.95};

        ScottsBandwidth scott = new ScottsBandwidth(x);
        System.out.println("    Scott: " + scott.value());

        LeastSquaresCrossValidation lscv = new LeastSquaresCrossValidation(x);
        System.out.println("    LSCV: " + lscv.value());

//        assertEquals("LSCV test 1", 6.431987000748, lscv.evaluate(), 1e-5);

    }

    /**
     * True values obtained from R function bw.ucv(x)
     *
     * @throws Exception
     */
    @Test
    public void testValue2()throws Exception{
        System.out.println("Least squares cross validation test 2");
        double[] x = {0.65, 0.89, -0.20, 2.33, -0.67, 0.28, 1.06, 1.53, 0.32, -0.18, -1.16, 0.15,
                0.95, -0.88, 0.65, -1.03, 0.26, 0.42, 0.40, 0.25, -0.43, 0.00, 0.07, 0.84,
                0.42, 0.72, -0.73, -1.98, 0.88, -1.60};
        ScottsBandwidth scott = new ScottsBandwidth(x);
        System.out.println("    Scott: " + scott.value());

        LeastSquaresCrossValidation lscv = new LeastSquaresCrossValidation(x);
        System.out.println("    LSCV: " + lscv.value());

//        assertEquals("LSCV test 2", 0.487795421968898, lscv.evaluate(), 1e-5);
    }
}

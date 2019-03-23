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

import com.itemanalysis.psychometrics.quadrature.UniformQuadratureRule;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * True evaluate computed using evaluate(x, from=80, to=136, n=50, bw=1) in R
 */
public class KernelDensityTest {

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

    double[] trueGrid = {80,81.1428571428571,82.2857142857143,83.4285714285714,84.5714285714286,85.7142857142857,
            86.8571428571429,88,89.1428571428571,90.2857142857143,91.4285714285714,92.5714285714286,
            93.7142857142857,94.8571428571429,96,97.1428571428571,98.2857142857143,99.4285714285714,
            100.571428571429,101.714285714286,102.857142857143,104,105.142857142857,106.285714285714,
            107.428571428571,108.571428571429,109.714285714286,110.857142857143,112,113.142857142857,
            114.285714285714,115.428571428571,116.571428571429,117.714285714286,118.857142857143,
            120,121.142857142857,122.285714285714,123.428571428571,124.571428571429,125.714285714286,
            126.857142857143,128,129.142857142857,130.285714285714,131.428571428571,132.571428571429,
            133.714285714286,134.857142857143,136};

    double[] trueDensity = {0.000106055943823094,0.00160713442411849,0.00717008148939428,0.0118729357819649,0.0110083062446829,
            0.0113509675370038,0.0203582831645598,0.0304340517613523,0.0371706044829635,0.0302973953581597,
            0.0224617154367057,0.0190749909272957,0.0250356861316071,0.0327706510270003,0.0344989666546600,
            0.0238665082236908,0.0217607148789736,0.0264615788405506,0.0341952884943624,0.0456428704937417,
            0.0493075870242431,0.0366278889149721,0.0335557329064161,0.0290174239337724,0.0233961876763816,
            0.0249938335307775,0.0226903162168762,0.0130216613248955,0.0031539383844525,0.000249689859099997,
            0.000166565634552182,0.00280078762250182,0.0150361260445911,0.0310662726395317,0.0340861870767526,
            0.0197056658926455,0.00965941725785273,0.00750856928575942,0.00240760362351230,0.000909909915414422,
            0.00714906657758102,0.0212566747848107,0.0184219115821315,0.00467163374877514,0.00034559424553365,
            3.40323324507272e-05,0.000649033888000387,0.00433774288948667,0.00795541129287286,0.00398644267497938};

    private UniformQuadratureRule points = null;

    private UserSuppliedBandwidth bw = null;

    public KernelDensityTest(){
        bw = new UserSuppliedBandwidth(1.0);
        points = new UniformQuadratureRule(80.0, 136.0, 50);

    }

    /**
     * Test kernel evaluate estimate using the Root Mean Squared Error (RMSE).
     * The true values for the evaluate are from the R evaluate() which uses
     * a Fast Fourier Transform followed by linear interpolation to obtain
     * the evaluate at a point. Therefore, the computations are slightly different.
     *
     * @throws Exception
     */
    @Test
    public void testValue() throws Exception {
        System.out.println("Testing kernel evaluate RMSE: ");
        GaussianKernel kernel = new GaussianKernel();
        KernelDensity den = new KernelDensity(kernel, bw, points);
        for(int i=0;i<x.length;i++){
            den.increment(x[i]);
        }

        double[] v = den.evaluate();
        double MSE = 0.0;

        double n = (double)v.length;
        for(int i=0;i<v.length;i++){
            MSE += Math.pow(v[i] - trueDensity[i], 2);
        }
        double RMSE =  Math.sqrt(MSE/n);
        System.out.println(RMSE);
        assertEquals("Testing kernel evaluate RMSE", 0.0, RMSE, 1e-4);
    }

    @Test
    public void testStrictValue() throws Exception {
        System.out.print("Testing kernel evaluate strict criterion: ");
        GaussianKernel kernel = new GaussianKernel();
        KernelDensity den = new KernelDensity(kernel, bw, points);
        for(int i=0;i<x.length;i++){
            den.increment(x[i]);
        }

        double[] v = den.evaluate();

        double n = (double)v.length;
        double m = 0.0;
        double d = 0.0;
        for(int i=0;i<v.length;i++){
            d = v[i] - trueDensity[i];
            m = Math.max(m, d);
            assertEquals("Strict kernel evaluate test", 0.00, d, 1e-4);
        }
        System.out.println("   max Delta: " + m);
    }

    @Test
    public void testDensity()throws Exception{
        System.out.print("Testing kernel evaluate strict criterion, non-incremental");
        GaussianKernel kernel = new GaussianKernel();
        KernelDensity den = new KernelDensity(kernel, bw, points);
        double[] d = den.evaluate(x);
        for(int i=0;i<d.length;i++){
            assertEquals("Strict kernel evaluate test non-incremental", 0.00, d[i] - trueDensity[i], 1e-4);
        }
    }

    @Test
    public void testGetPoints() throws Exception {
        System.out.println("Testing kernel gridpoints");
        GaussianKernel kernel = new GaussianKernel();
        KernelDensity den = new KernelDensity(kernel, bw, points);
        for(int i=0;i<x.length;i++){
            den.increment(x[i]);
        }
        double[] p = den.getPoints();
        for(int i=0;i<p.length;i++){
            assertEquals("Testing kernel gridpoints", trueGrid[i], p[i], 1e-10);
        }

    }

    @Test
    public void testGetBandwidth() throws Exception {
        System.out.println("Testing kernel bandwidth");
        assertEquals("Testing kernel bandwidth", 1.0, bw.value(), 1e-15);

    }

//    @Test
//    public void interpolationTest(){
//        System.out.println("Testing kernel interpolation");
//        GaussianKernel kernel = new GaussianKernel();
//        KernelDensity kde = new KernelDensity(kernel, bw, points);
//        for(int i=0;i<x.length;i++){
//            kde.increment(x[i]);
//        }
//        double[] p = kde.getPoints();
//        double[] g = kde.evaluate();
//
//        UnivariateFunction interpol = kde.getInterpolater();
//        LegendreGaussIntegrator lgi = new LegendreGaussIntegrator(5, 1.0e-14, 1.0e-10);
//
//        double value = lgi.integrate(500, interpol, p[0], p[p.length-1]);
//        System.out.println("integration: " + value);
//        assertEquals("Kernel integration test: ", 1.0, value, 1e-2);
//
//    }
}

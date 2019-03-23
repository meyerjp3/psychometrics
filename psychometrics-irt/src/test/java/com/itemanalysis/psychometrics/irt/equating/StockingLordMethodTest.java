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
package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.*;
import com.itemanalysis.psychometrics.quadrature.ContinuousQuadratureRule;
import com.itemanalysis.psychometrics.quadrature.NormalQuadratureRule;
import com.itemanalysis.psychometrics.optimization.BOBYQAOptimizer;
import com.itemanalysis.psychometrics.quadrature.UniformQuadratureRule;
import com.itemanalysis.psychometrics.uncmin.DefaultUncminOptimizer;
import com.itemanalysis.psychometrics.uncmin.UncminException;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.*;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import org.apache.commons.math3.random.*;
import org.junit.Test;

import java.util.LinkedHashMap;

import static junit.framework.Assert.assertEquals;

public class StockingLordMethodTest {

    double[] aX = {0.455118, 0.583871, 0.754398, 0.663274, 1.068977, 0.967194, 0.347868, 1.457918, 0.701952, 1.407967, 1.299285};
    double[] bX = {-0.710086, -0.856669, 0.021221, 0.050618, 0.961047, 0.194976, 2.276794, 1.024128, 2.240131, 1.555634, 2.158933};
    double[] cX = {0.208748, 0.203834, 0.159961, 0.123961, 0.298628, 0.053538, 0.148927, 0.24527, 0.08529, 0.078897, 0.10753};
    double[] aY = {0.441595, 0.572995, 0.598719, 0.604125, 0.990164, 0.808079, 0.413973, 1.355437, 0.633562, 1.134661, 0.925521};
    double[] bY = {-1.334933, -1.321004, -0.709831, -0.353942, 0.531956, -0.115649, 2.553812, 0.581109, 1.896027, 1.079013, 2.133706};
    double[] cY = {0.155883, 0.191298, 0.117663, 0.081759, 0.302443, 0.064791, 0.240967, 0.224322, 0.079396, 0.063009, 0.125873};

    double[] points = {-4.0000, -3.1110, -2.2220, -1.3330, -0.4444, 0.4444, 1.3330, 2.2220, 3.1110, 4.0000};
    double[] xDensity = {0.0001008, 0.002760, 0.03021, 0.1420, 0.3149, 0.3158, 0.1542, 0.03596, 0.003925, 0.0001862};
    double[] yDensity = {0.0001173, 0.003242, 0.03449, 0.1471, 0.3148, 0.3110, 0.1526, 0.03406, 0.002510, 0.0001116};

    /**
     * Item parameters, quadrature points and weights, and "true" linking coefficients were obtained
     * from example1 in Kolen's STUIRT program. However, the Stocking-Lord procedure was changed in
     * the STUIRT example to produce symmetric optimization of the criterion function (i.e. SY BI BI
     * instead of SY BI NO).
     *
     * This test runs the Stocking-Lord procedure twice: One with the UNCMIN optimizer and once with
     * the BOBYQA optimizer. Results from each are compared to teh STUIRT results and to each other.
     *
     */
    @Test
    public void stockingLordTest0(){
        System.out.println("StockingLordMethod Test 0: Actual Distribution");

        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        irmX.put(new VariableName("i1"), new Irm3PL(0.4551, -0.7101, 0.2087, 1.7));
        irmX.put(new VariableName("i2"), new Irm3PL(0.5839, -0.8567, 0.2038, 1.7));
        irmX.put(new VariableName("i3"), new Irm3PL(0.7544, 0.0212, 0.1600, 1.7));
        irmX.put(new VariableName("i4"), new Irm3PL(0.6633, 0.0506, 0.1240, 1.7));
        irmX.put(new VariableName("i5"), new Irm3PL(1.0690, 0.9610, 0.2986, 1.7));
        irmX.put(new VariableName("i6"), new Irm3PL(0.9672, 0.1950, 0.0535, 1.7));
        irmX.put(new VariableName("i7"), new Irm3PL(0.3479, 2.2768, 0.1489, 1.7));
        irmX.put(new VariableName("i8"), new Irm3PL(1.4579, 1.0241, 0.2453, 1.7));
        irmX.put(new VariableName("i9"), new Irm3PL(1.8811, 1.4062, 0.1992, 1.7));
        irmX.put(new VariableName("i10"), new Irm3PL(0.7020, 2.2401, 0.0853, 1.7));
        irmX.put(new VariableName("i11"), new Irm3PL(1.4080, 1.5556, 0.0789, 1.7));
        irmX.put(new VariableName("i12"), new Irm3PL(1.2993, 2.1589, 0.1075, 1.7));

        irmY.put(new VariableName("i1"), new Irm3PL(0.4416, -1.3349, 0.1559, 1.7));
        irmY.put(new VariableName("i2"), new Irm3PL(0.5730, -1.3210, 0.1913, 1.7));
        irmY.put(new VariableName("i3"), new Irm3PL(0.5987, -0.7098, 0.1177, 1.7));
        irmY.put(new VariableName("i4"), new Irm3PL(0.6041, -0.3539, 0.0818, 1.7));
        irmY.put(new VariableName("i5"), new Irm3PL(0.9902,  0.5320, 0.3024, 1.7));
        irmY.put(new VariableName("i6"), new Irm3PL(0.8081, -0.1156, 0.0648, 1.7));
        irmY.put(new VariableName("i7"), new Irm3PL(0.4140,  2.5538, 0.2410, 1.7));
        irmY.put(new VariableName("i8"), new Irm3PL(1.3554,  0.5811, 0.2243, 1.7));
        irmY.put(new VariableName("i9"), new Irm3PL(1.0417,  0.9392, 0.1651, 1.7));
        irmY.put(new VariableName("i10"), new Irm3PL(0.6336,  1.8960, 0.0794, 1.7));
        irmY.put(new VariableName("i11"), new Irm3PL(1.1347,  1.0790, 0.0630, 1.7));
        irmY.put(new VariableName("i12"), new Irm3PL(0.9255,  2.1337, 0.1259, 1.7));

        double[] points = {-4.0000, -3.1110, -2.2220, -1.3330, -0.4444, 0.4444, 1.3330, 2.2220, 3.1110, 4.0000};
        double[] xDensity = {0.0001008, 0.002760, 0.03021, 0.1420, 0.3149, 0.3158, 0.1542, 0.03596, 0.003925, 0.0001862};
        double[] yDensity = {0.0001173, 0.003242, 0.03449, 0.1471, 0.3148, 0.3110, 0.1526, 0.03406, 0.002510, 0.0001116};
        ContinuousQuadratureRule distX = new ContinuousQuadratureRule(points, xDensity);
        ContinuousQuadratureRule distY = new ContinuousQuadratureRule(points, yDensity);


        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, distX, distY, EquatingCriterionType.Q1Q2);
        sl.setPrecision(6);
        double[] startValues = {0,1};
        double f = 0;

        //Run test with UNCMIN optimizer
        DefaultUncminOptimizer optimizer = new DefaultUncminOptimizer();
        double[] param1 = null;
        double[] param2 = null;
        try{
            optimizer.minimize(sl, startValues);
            param1 = optimizer.getParameters();
            f = optimizer.getFunctionValue();
            sl.setIntercept(param1[0]);
            sl.setScale(param1[1]);

        }catch(UncminException ex){
            ex.printStackTrace();
        }

        //Check UNCMIN values against results from STUIRT.
        System.out.println("  UNCMIN Optimization");
        System.out.println("  Iterations: ");
        System.out.println("  fmin: " + f);
        System.out.println("  B = " + sl.getIntercept() + "  A = " + sl.getScale());

        assertEquals("  Intercept test", -0.487619, sl.getIntercept(), 1e-6);
        assertEquals("  Scale test", 1.083417, sl.getScale(), 1e-6);


        //Run test with BOBYQA optimizer
        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer2 = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer2.optimize(new MaxEval(1000),
                new ObjectiveFunction(sl),
                GoalType.MINIMIZE,
                SimpleBounds.unbounded(2),
                new InitialGuess(startValues));

        param2 = optimum.getPoint();
        sl.setIntercept(param2[0]);
        sl.setScale(param2[1]);

        //Check BOBYQA values against results from STUIRT.
        System.out.println();
        System.out.println("  BOBYQA Optimization");
        System.out.println("  Iterations: " + optimizer2.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + param2[0] + "  A = " + param2[1]);

        assertEquals("  Intercept test", -0.487619, sl.getIntercept(), 1e-6);
        assertEquals("  Scale test", 1.083417, sl.getScale(), 1e-6);

        //Compare results from each optimizer
        assertEquals("  Intercept test", param1[0], param2[0], 1e-6);
        assertEquals("  Scale test", param1[1], param2[1], 1e-6);


        System.out.println();

    }

    @Test
    public void stockingLordTest1(){
        System.out.println("StockingLordMethod Test 1: Actual Distribution");
        int n = aX.length;
        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();
        ItemResponseModel irm;

        for(int i=0;i<n;i++){
            VariableName name = new VariableName("V"+i);
            irm = new Irm3PL(aX[i], bX[i], cX[i], 1.0);
            irmX.put(name, irm);

            irm = new Irm3PL(aY[i], bY[i], cY[i], 1.0);
            irmY.put(name, irm);
        }

        ContinuousQuadratureRule distX = new ContinuousQuadratureRule(points, xDensity);
        ContinuousQuadratureRule distY = new ContinuousQuadratureRule(points, yDensity);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, distX, distY, EquatingCriterionType.Q1Q2);
        sl.setPrecision(4);
        double[] startValues = {0,1};

        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer.optimize(new MaxEval(1000),
                           new ObjectiveFunction(sl),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(2),
                           new InitialGuess(startValues));

        double[] slCoefficients = optimum.getPoint();
        sl.setIntercept(slCoefficients[0]);
        sl.setScale(slCoefficients[1]);

        System.out.println("  Iterations: " + optimizer.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + slCoefficients[0] + "  A = " + slCoefficients[1]);

        assertEquals("  Intercept test", -0.4788, sl.getIntercept(), 1e-4);
        assertEquals("  Scale test", 1.0911, sl.getScale(), 1e-4);

        System.out.println();

    }

    @Test
    public void stockingLordTest2(){
        System.out.println("StockingLordMethod Test 2: Uniform Distribution");
        int n = aX.length;
        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();
        ItemResponseModel irm;

        for(int i=0;i<n;i++){
            VariableName name = new VariableName("V"+i);
            irm = new Irm3PL(aX[i], bX[i], cX[i], 1.0);
            irmX.put(name, irm);

            irm = new Irm3PL(aY[i], bY[i], cY[i], 1.0);
            irmY.put(name, irm);
        }

        UniformQuadratureRule uniform = new UniformQuadratureRule(-4, 4, 10);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, uniform, uniform, EquatingCriterionType.Q1Q2);
        sl.setPrecision(4);
        double[] startValues = {0, 1};

        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer.optimize(new MaxEval(1000),
                           new ObjectiveFunction(sl),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(2),
                           new InitialGuess(startValues));

        double[] slCoefficients = optimum.getPoint();
        sl.setIntercept(slCoefficients[0]);
        sl.setScale(slCoefficients[1]);

        System.out.println("  Iterations: " + optimizer.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + slCoefficients[0] + "  A = " + slCoefficients[1]);

        assertEquals("  Intercept test", -0.4532, sl.getIntercept(), 1e-4);
        assertEquals("  Scale test", 1.0962, sl.getScale(), 1e-4);

        System.out.println();

    }

    @Test
    public void stockingLordTest3(){
        System.out.println("StockingLordMethod Test 3: Normal Distribution");
        int n = aX.length;
        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();
        ItemResponseModel irm;

        for(int i=0;i<n;i++){
            VariableName name = new VariableName("V"+i);
            irm = new Irm3PL(aX[i], bX[i], cX[i], 1.0);
            irmX.put(name, irm);

            irm = new Irm3PL(aY[i], bY[i], cY[i], 1.0);
            irmY.put(name, irm);
        }

        NormalQuadratureRule normal = new NormalQuadratureRule(0, 1, -4, 4, 10);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, normal, normal, EquatingCriterionType.Q1Q2);
        sl.setPrecision(4);
        double[] startValues = {0, 1};

        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer.optimize(new MaxEval(1000),
                           new ObjectiveFunction(sl),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(2),
                           new InitialGuess(startValues));

        double[] slCoefficients = optimum.getPoint();
        sl.setIntercept(slCoefficients[0]);
        sl.setScale(slCoefficients[1]);

        System.out.println("  Iterations: " + optimizer.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + slCoefficients[0] + "  A = " + slCoefficients[1]);

        assertEquals("  Intercept test", -0.4788, sl.getIntercept(), 1e-4);
        assertEquals("  Scale test", 1.0901, sl.getScale(), 1e-4);

        System.out.println();

    }

    @Test
    public void stockingLordTest4(){
        System.out.println("StockingLordMethod Test 4: Actual Distribution -backwards");
        int n = aX.length;
        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();
        ItemResponseModel irm;

        for(int i=0;i<n;i++){
            VariableName name = new VariableName("V"+i);
            irm = new Irm3PL(aX[i], bX[i], cX[i], 1.0);
            irmX.put(name, irm);

            irm = new Irm3PL(aY[i], bY[i], cY[i], 1.0);
            irmY.put(name, irm);
        }

        ContinuousQuadratureRule distX = new ContinuousQuadratureRule(points, xDensity);
        ContinuousQuadratureRule distY = new ContinuousQuadratureRule(points, yDensity);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, distX, distY, EquatingCriterionType.Q1);
        sl.setPrecision(4);
        double[] startValues = {0,1};

        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer.optimize(new MaxEval(1000),
                           new ObjectiveFunction(sl),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(2),
                           new InitialGuess(startValues));

        double[] slCoefficients = optimum.getPoint();
        sl.setIntercept(slCoefficients[0]);
        sl.setScale(slCoefficients[1]);

        System.out.println("  Iterations: " + optimizer.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + slCoefficients[0] + "  A = " + slCoefficients[1]);

        assertEquals("  Intercept test", -0.4834, sl.getIntercept(), 1e-4);
        assertEquals("  Scale test", 1.0967, sl.getScale(), 1e-4);

        System.out.println();

    }

    /**
     * Item parameters and "true" linking coefficients are from example2 in Kolen's STUIRT program.
     *
     */
    @Test
    public void stockingLordTestMixedFormatPARSCALE(){
        System.out.println("StockingLordMethod Test 4: STUIRT example 2 with PARSCALE parameters");

        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        //Form X
        irmX.put(new VariableName("v1"), new Irm3PL(0.751335, -0.897391, 0.244001, 1.7));
        irmX.put(new VariableName("v2"), new Irm3PL(0.955947, -0.811477, 0.242883, 1.7));
        irmX.put(new VariableName("v3"), new Irm3PL(0.497206, -0.858681, 0.260893, 1.7));
        irmX.put(new VariableName("v4"), new Irm3PL(0.724000, -0.123911, 0.243497, 1.7));
        irmX.put(new VariableName("v5"), new Irm3PL(0.865200,  0.205889, 0.319135, 1.7));
        irmX.put(new VariableName("v6"), new Irm3PL(0.658129,  0.555228, 0.277826, 1.7));
        irmX.put(new VariableName("v7"), new Irm3PL(1.082118,  0.950549, 0.157979, 1.7));
        irmX.put(new VariableName("v8"), new Irm3PL(0.988294,  1.377501, 0.084828, 1.7));
        irmX.put(new VariableName("v9"), new Irm3PL(1.248923,  1.614355, 0.181874, 1.7));
        irmX.put(new VariableName("v10"), new Irm3PL(1.116682,  2.353932, 0.246856, 1.7));
        irmX.put(new VariableName("v11"), new Irm3PL(0.438171, 3.217965, 0.309243, 1.7));
        irmX.put(new VariableName("v12"), new Irm3PL(1.082206, 4.441864, 0.192339, 1.7));

        double[] step1 = {1.097268, -1.097268};
        irmX.put(new VariableName("v13"), new IrmGPCM2(0.269994, 0.003998, step1, 1.7));

        double[] step2 = {0.106514, -0.106514};
        irmX.put(new VariableName("v14"), new IrmGPCM2(0.972506, 1.632662, step2, 1.7));

        double[] step3 = {2.102301, -2.102301};
        irmX.put(new VariableName("v15"), new IrmGPCM2(0.378812, 3.464657, step3, 1.7));

        double[] step4 = {-0.476513,  1.081282, -0.604770};
        irmX.put(new VariableName("v16"), new IrmGPCM2(0.537706, 1.010053, step4, 1.7));

        double[] step5 = {1.007525, -0.197767, -0.809758};
        irmX.put(new VariableName("v17"), new IrmGPCM2(0.554506, 2.432938, step5, 1.7));


        //Form Y
        irmY.put(new VariableName("v1"), new Irm3PL(0.887276, -1.334798, 0.134406, 1.7));
        irmY.put(new VariableName("v2"), new Irm3PL(1.184412, -1.129004, 0.237765, 1.7));
        irmY.put(new VariableName("v3"), new Irm3PL(0.609412, -1.464546, 0.151393, 1.7));
        irmY.put(new VariableName("v4"), new Irm3PL(0.923812, -0.576435, 0.240097, 1.7));
        irmY.put(new VariableName("v5"), new Irm3PL(0.822776, -0.476357, 0.192369, 1.7));
        irmY.put(new VariableName("v6"), new Irm3PL(0.707818, -0.235189, 0.189557, 1.7));
        irmY.put(new VariableName("v7"), new Irm3PL(1.306976,  0.242986, 0.165553, 1.7));
        irmY.put(new VariableName("v8"), new Irm3PL(1.295471,  0.598029, 0.090557, 1.7));
        irmY.put(new VariableName("v9"), new Irm3PL(1.366841,  0.923206, 0.172993, 1.7));
        irmY.put(new VariableName("v10"), new Irm3PL(1.389624,  1.380666, 0.238008, 1.7));
        irmY.put(new VariableName("v11"), new Irm3PL(0.293806,  2.028070, 0.203448, 1.7));
        irmY.put(new VariableName("v12"), new Irm3PL(0.885347,  3.152928, 0.195473, 1.7));

        double[] step1Y = {0.893232, -0.893232};
        irmY.put(new VariableName("v13"), new IrmGPCM2(0.346324, -0.494115, step1Y, 1.7));

        double[] step2Y = {0.099750, -0.099750};
        irmY.put(new VariableName("v14"), new IrmGPCM2(1.252012, 0.856264, step2Y, 1.7));

        double[] step3Y = {1.850498, -1.850498};
        irmY.put(new VariableName("v15"), new IrmGPCM2(0.392282, 2.825801, step3Y, 1.7));

        double[] step4Y = {-0.300428,  0.761846, -0.461417};
        irmY.put(new VariableName("v16"), new IrmGPCM2(0.660841, 0.342977, step4Y, 1.7));

        double[] step5Y = {1.001974, -0.107221, -0.894753};
        irmY.put(new VariableName("v17"), new IrmGPCM2(0.669612, 1.643267, step5Y, 1.7));

//        LinkedHashMap<String, ItemResponseModel> irmX = new LinkedHashMap<String, ItemResponseModel>();
//        LinkedHashMap<String, ItemResponseModel> irmY = new LinkedHashMap<String, ItemResponseModel>();
//
//        //Form X
//        irmX.put("v1", new Irm3PL(0.751335, -0.897391, 0.244001, 1.7));
//        irmX.put("v2", new Irm3PL(0.955947, -0.811477, 0.242883, 1.7));
//        irmX.put("v3", new Irm3PL(0.497206, -0.858681, 0.260893, 1.7));
//        irmX.put("v4", new Irm3PL(0.724000, -0.123911, 0.243497, 1.7));
//        irmX.put("v5", new Irm3PL(0.865200,  0.205889, 0.319135, 1.7));
//        irmX.put("v6", new Irm3PL(0.658129,  0.555228, 0.277826, 1.7));
//        irmX.put("v7", new Irm3PL(1.082118,  0.950549, 0.157979, 1.7));
//        irmX.put("v8", new Irm3PL(0.988294,  1.377501, 0.084828, 1.7));
//        irmX.put("v9", new Irm3PL(1.248923,  1.614355, 0.181874, 1.7));
//        irmX.put("v10", new Irm3PL(1.116682,  2.353932, 0.246856, 1.7));
//        irmX.put("v11", new Irm3PL(0.438171, 3.217965, 0.309243, 1.7));
//        irmX.put("v12", new Irm3PL(1.082206, 4.441864, 0.192339, 1.7));
//
//        double[] step1 = {1.097268, -1.097268};
//        irmX.put("v13", new IrmGPCM2(0.269994, 0.003998, step1, 1.7));
//
//        double[] step2 = {0.106514, -0.106514};
//        irmX.put("v14", new IrmGPCM2(0.972506, 1.632662, step2, 1.7));
//
//        double[] step3 = {2.102301, -2.102301};
//        irmX.put("v15", new IrmGPCM2(0.378812, 3.464657, step3, 1.7));
//
//        double[] step4 = {-0.476513,  1.081282, -0.604770};
//        irmX.put("v16", new IrmGPCM2(0.537706, 1.010053, step4, 1.7));
//
//        double[] step5 = {1.007525, -0.197767, -0.809758};
//        irmX.put("v17", new IrmGPCM2(0.554506, 2.432938, step5, 1.7));
//
//
//        //Form Y
//        irmY.put("v1", new Irm3PL(0.887276, -1.334798, 0.134406, 1.7));
//        irmY.put("v2", new Irm3PL(1.184412, -1.129004, 0.237765, 1.7));
//        irmY.put("v3", new Irm3PL(0.609412, -1.464546, 0.151393, 1.7));
//        irmY.put("v4", new Irm3PL(0.923812, -0.576435, 0.240097, 1.7));
//        irmY.put("v5", new Irm3PL(0.822776, -0.476357, 0.192369, 1.7));
//        irmY.put("v6", new Irm3PL(0.707818, -0.235189, 0.189557, 1.7));
//        irmY.put("v7", new Irm3PL(1.306976,  0.242986, 0.165553, 1.7));
//        irmY.put("v8", new Irm3PL(1.295471,  0.598029, 0.090557, 1.7));
//        irmY.put("v9", new Irm3PL(1.366841,  0.923206, 0.172993, 1.7));
//        irmY.put("v10", new Irm3PL(1.389624,  1.380666, 0.238008, 1.7));
//        irmY.put("v11", new Irm3PL(0.293806,  2.028070, 0.203448, 1.7));
//        irmY.put("v12", new Irm3PL(0.885347,  3.152928, 0.195473, 1.7));
//
//        double[] step1Y = {0.893232, -0.893232};
//        irmY.put("v13", new IrmGPCM2(0.346324, -0.494115, step1Y, 1.7));
//
//        double[] step2Y = {0.099750, -0.099750};
//        irmY.put("v14", new IrmGPCM2(1.252012, 0.856264, step2Y, 1.7));
//
//        double[] step3Y = {1.850498, -1.850498};
//        irmY.put("v15", new IrmGPCM2(0.392282, 2.825801, step3Y, 1.7));
//
//        double[] step4Y = {-0.300428,  0.761846, -0.461417};
//        irmY.put("v16", new IrmGPCM2(0.660841, 0.342977, step4Y, 1.7));
//
//        double[] step5Y = {1.001974, -0.107221, -0.894753};
//        irmY.put("v17", new IrmGPCM2(0.669612, 1.643267, step5Y, 1.7));

        UniformQuadratureRule uniform = new UniformQuadratureRule(-3.0, 3.0, 25);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, uniform, uniform, EquatingCriterionType.Q1Q2);
        sl.setPrecision(6);
        double[] startValues = {0,1};

        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer.optimize(
                new MaxEval(1000),
                new ObjectiveFunction(sl),
                GoalType.MINIMIZE,
                SimpleBounds.unbounded(2),
                new InitialGuess(startValues));

        double[] slCoefficients = optimum.getPoint();
        sl.setIntercept(slCoefficients[0]);
        sl.setScale(slCoefficients[1]);

        System.out.println("  Iterations: " + optimizer.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + slCoefficients[0] + "  A = " + slCoefficients[1]);

        assertEquals("  Intercept test", -0.456487, sl.getIntercept(), 1e-6);
        assertEquals("  Scale test", 0.815445, sl.getScale(), 1e-6);

        System.out.println();

    }

    /**
     * Item parameter and "true" linking coefficients are based on example 2 in Kolen's STUIRT program.
     * For polytomous items, the difficulty and threshold parameters were combined to be step parameters
     * as done with Brad Hanson's GPCM. That is, teh parameters are configured to conform to the "IS"
     * version of the GPCM as specified in the STUIRT manual.
     */
    @Test
    public void stockingLordTestMixedICL(){
        System.out.println("StockingLordMethod Test 4: STUIRT example 2 with ICL parameters");

        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        //Form X
        irmX.put(new VariableName("v1"), new Irm3PL(0.751335, -0.897391, 0.244001, 1.7));
        irmX.put(new VariableName("v2"), new Irm3PL(0.955947, -0.811477, 0.242883, 1.7));
        irmX.put(new VariableName("v3"), new Irm3PL(0.497206, -0.858681, 0.260893, 1.7));
        irmX.put(new VariableName("v4"), new Irm3PL(0.724000, -0.123911, 0.243497, 1.7));
        irmX.put(new VariableName("v5"), new Irm3PL(0.865200,  0.205889, 0.319135, 1.7));
        irmX.put(new VariableName("v6"), new Irm3PL(0.658129,  0.555228, 0.277826, 1.7));
        irmX.put(new VariableName("v7"), new Irm3PL(1.082118,  0.950549, 0.157979, 1.7));
        irmX.put(new VariableName("v8"), new Irm3PL(0.988294,  1.377501, 0.084828, 1.7));
        irmX.put(new VariableName("v9"), new Irm3PL(1.248923,  1.614355, 0.181874, 1.7));
        irmX.put(new VariableName("v10"), new Irm3PL(1.116682,  2.353932, 0.246856, 1.7));
        irmX.put(new VariableName("v11"), new Irm3PL(0.438171, 3.217965, 0.309243, 1.7));
        irmX.put(new VariableName("v12"), new Irm3PL(1.082206, 4.441864, 0.192339, 1.7));

        double[] step1 = {0.0, 1.101266, -1.09327};
        irmX.put(new VariableName("v13"), new IrmGPCM(0.269994, step1, 1.7));

        double[] step2 = {0.0, 1.739176, 1.526148};
        irmX.put(new VariableName("v14"), new IrmGPCM(0.972506, step2, 1.7));

        double[] step3 = {0.0, 5.566958, 1.362356};
        irmX.put(new VariableName("v15"), new IrmGPCM(0.378812, step3, 1.7));

        double[] step4 = {0.0, 0.533540,  2.091335, 0.405283};
        irmX.put(new VariableName("v16"), new IrmGPCM(0.537706, step4, 1.7));

        double[] step5 = {0.0, 3.440463,  2.235171, 1.62318};
        irmX.put(new VariableName("v17"), new IrmGPCM(0.554506, step5, 1.7));


        //Form Y
        irmY.put(new VariableName("v1"), new Irm3PL(0.887276, -1.334798, 0.134406, 1.7));
        irmY.put(new VariableName("v2"), new Irm3PL(1.184412, -1.129004, 0.237765, 1.7));
        irmY.put(new VariableName("v3"), new Irm3PL(0.609412, -1.464546, 0.151393, 1.7));
        irmY.put(new VariableName("v4"), new Irm3PL(0.923812, -0.576435, 0.240097, 1.7));
        irmY.put(new VariableName("v5"), new Irm3PL(0.822776, -0.476357, 0.192369, 1.7));
        irmY.put(new VariableName("v6"), new Irm3PL(0.707818, -0.235189, 0.189557, 1.7));
        irmY.put(new VariableName("v7"), new Irm3PL(1.306976,  0.242986, 0.165553, 1.7));
        irmY.put(new VariableName("v8"), new Irm3PL(1.295471,  0.598029, 0.090557, 1.7));
        irmY.put(new VariableName("v9"), new Irm3PL(1.366841,  0.923206, 0.172993, 1.7));
        irmY.put(new VariableName("v10"), new Irm3PL(1.389624,  1.380666, 0.238008, 1.7));
        irmY.put(new VariableName("v11"), new Irm3PL(0.293806,  2.028070, 0.203448, 1.7));
        irmY.put(new VariableName("v12"), new Irm3PL(0.885347,  3.152928, 0.195473, 1.7));

        double[] step1Y = {0.0, 0.399117, -1.38735};
        irmY.put(new VariableName("v13"), new IrmGPCM(0.346324, step1Y, 1.7));

        double[] step2Y = {0.0, 0.956014,  0.756514};
        irmY.put(new VariableName("v14"), new IrmGPCM(1.252012, step2Y, 1.7));

        double[] step3Y = {0.0, 4.676299,  0.975303};
        irmY.put(new VariableName("v15"), new IrmGPCM(0.392282, step3Y, 1.7));

        double[] step4Y = {0.0, 0.042549,  1.104823, -0.118440};
        irmY.put(new VariableName("v16"), new IrmGPCM(0.660841, step4Y, 1.7));

        double[] step5Y = {0.0, 2.645241,  1.536046,  0.748514};
        irmY.put(new VariableName("v17"), new IrmGPCM(0.669612, step5Y, 1.7));

//        LinkedHashMap<String, ItemResponseModel> irmX = new LinkedHashMap<String, ItemResponseModel>();
//        LinkedHashMap<String, ItemResponseModel> irmY = new LinkedHashMap<String, ItemResponseModel>();
//
//        //Form X
//        irmX.put("v1", new Irm3PL(0.751335, -0.897391, 0.244001, 1.7));
//        irmX.put("v2", new Irm3PL(0.955947, -0.811477, 0.242883, 1.7));
//        irmX.put("v3", new Irm3PL(0.497206, -0.858681, 0.260893, 1.7));
//        irmX.put("v4", new Irm3PL(0.724000, -0.123911, 0.243497, 1.7));
//        irmX.put("v5", new Irm3PL(0.865200,  0.205889, 0.319135, 1.7));
//        irmX.put("v6", new Irm3PL(0.658129,  0.555228, 0.277826, 1.7));
//        irmX.put("v7", new Irm3PL(1.082118,  0.950549, 0.157979, 1.7));
//        irmX.put("v8", new Irm3PL(0.988294,  1.377501, 0.084828, 1.7));
//        irmX.put("v9", new Irm3PL(1.248923,  1.614355, 0.181874, 1.7));
//        irmX.put("v10", new Irm3PL(1.116682,  2.353932, 0.246856, 1.7));
//        irmX.put("v11", new Irm3PL(0.438171, 3.217965, 0.309243, 1.7));
//        irmX.put("v12", new Irm3PL(1.082206, 4.441864, 0.192339, 1.7));
//
//        double[] step1 = {0.0, 1.101266, -1.09327};
//        irmX.put("v13", new IrmGPCM(0.269994, step1, 1.7));
//
//        double[] step2 = {0.0, 1.739176, 1.526148};
//        irmX.put("v14", new IrmGPCM(0.972506, step2, 1.7));
//
//        double[] step3 = {0.0, 5.566958, 1.362356};
//        irmX.put("v15", new IrmGPCM(0.378812, step3, 1.7));
//
//        double[] step4 = {0.0, 0.533540,  2.091335, 0.405283};
//        irmX.put("v16", new IrmGPCM(0.537706, step4, 1.7));
//
//        double[] step5 = {0.0, 3.440463,  2.235171, 1.62318};
//        irmX.put("v17", new IrmGPCM(0.554506, step5, 1.7));
//
//
//        //Form Y
//        irmY.put("v1", new Irm3PL(0.887276, -1.334798, 0.134406, 1.7));
//        irmY.put("v2", new Irm3PL(1.184412, -1.129004, 0.237765, 1.7));
//        irmY.put("v3", new Irm3PL(0.609412, -1.464546, 0.151393, 1.7));
//        irmY.put("v4", new Irm3PL(0.923812, -0.576435, 0.240097, 1.7));
//        irmY.put("v5", new Irm3PL(0.822776, -0.476357, 0.192369, 1.7));
//        irmY.put("v6", new Irm3PL(0.707818, -0.235189, 0.189557, 1.7));
//        irmY.put("v7", new Irm3PL(1.306976,  0.242986, 0.165553, 1.7));
//        irmY.put("v8", new Irm3PL(1.295471,  0.598029, 0.090557, 1.7));
//        irmY.put("v9", new Irm3PL(1.366841,  0.923206, 0.172993, 1.7));
//        irmY.put("v10", new Irm3PL(1.389624,  1.380666, 0.238008, 1.7));
//        irmY.put("v11", new Irm3PL(0.293806,  2.028070, 0.203448, 1.7));
//        irmY.put("v12", new Irm3PL(0.885347,  3.152928, 0.195473, 1.7));
//
//        double[] step1Y = {0.0, 0.399117, -1.38735};
//        irmY.put("v13", new IrmGPCM(0.346324, step1Y, 1.7));
//
//        double[] step2Y = {0.0, 0.956014,  0.756514};
//        irmY.put("v14", new IrmGPCM(1.252012, step2Y, 1.7));
//
//        double[] step3Y = {0.0, 4.676299,  0.975303};
//        irmY.put("v15", new IrmGPCM(0.392282, step3Y, 1.7));
//
//        double[] step4Y = {0.0, 0.042549,  1.104823, -0.118440};
//        irmY.put("v16", new IrmGPCM(0.660841, step4Y, 1.7));
//
//        double[] step5Y = {0.0, 2.645241,  1.536046,  0.748514};
//        irmY.put("v17", new IrmGPCM(0.669612, step5Y, 1.7));

        UniformQuadratureRule uniform = new UniformQuadratureRule(-3.0, 3.0, 25);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, uniform, uniform, EquatingCriterionType.Q1Q2);
        sl.setPrecision(6);
        double[] startValues = {0,1};

        //Run test with UNCMIN optimizer
        DefaultUncminOptimizer optimizer = new DefaultUncminOptimizer();
        double[] param1 = null;
        double[] param2 = null;
        double f = 0;

        try{
            optimizer.minimize(sl, startValues);
            param1 = optimizer.getParameters();
            f = optimizer.getFunctionValue();
            sl.setIntercept(param1[0]);
            sl.setScale(param1[1]);

        }catch(UncminException ex){
            ex.printStackTrace();
        }

        //Check UNCMIN values against results from STUIRT.
        System.out.println("  UNCMIN Optimization");
        System.out.println("  Iterations: ");
        System.out.println("  fmin: " + f);
        System.out.println("  B = " + sl.getIntercept() + "  A = " + sl.getScale());

        assertEquals("  Intercept test", -0.452240, sl.getIntercept(), 1e-6);
        assertEquals("  Scale test", 0.820927, sl.getScale(), 1e-6);


        //Check BOBYQA values against STUIRT results.
        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer2 = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer2.optimize(new MaxEval(1000),
                           new ObjectiveFunction(sl),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(2),
                           new InitialGuess(startValues));

        param2 = optimum.getPoint();
        sl.setIntercept(param2[0]);
        sl.setScale(param2[1]);

        System.out.println();
        System.out.println("  BOBYQA Optimization");
        System.out.println("  Iterations: " + optimizer2.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + param2[0] + "  A = " + param2[1]);

        assertEquals("  Intercept test", -0.452240, sl.getIntercept(), 1e-6);
        assertEquals("  Scale test", 0.820927, sl.getScale(), 1e-6);

        //Compare results from each optimizer
        assertEquals("  Intercept test", param1[0], param2[0], 1e-6);
        assertEquals("  Scale test", param1[1], param2[1], 1e-6);

    }

    @Test
    public void stockingLordTestMixedFormatGradedResponse(){
        System.out.println("StockingLordMethod Test 5: Mixed format test, symmetric, Graded Response");
        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        //3pl items
        irmX.put(new VariableName("v1"), new Irm3PL(1.0755, -1.8758, 0.1240, 1.7));
        irmX.put(new VariableName("v2"), new Irm3PL(0.6428, -0.9211, 0.1361, 1.7));
        irmX.put(new VariableName("v3"), new Irm3PL(0.6198, -1.3362, 0.1276, 1.7));
        irmX.put(new VariableName("v4"), new Irm3PL(0.6835, -1.8967, 0.1619, 1.7));
        irmX.put(new VariableName("v5"), new Irm3PL(0.9892, -0.6427, 0.2050, 1.7));
        irmX.put(new VariableName("v6"), new Irm3PL(0.5784, -0.8181, 0.1168, 1.7));
        irmX.put(new VariableName("v7"), new Irm3PL(0.9822, -0.9897, 0.1053, 1.7));
        irmX.put(new VariableName("v8"), new Irm3PL(1.6026, -1.2382, 0.1202, 1.7));
        irmX.put(new VariableName("v9"), new Irm3PL(0.8988, -0.5180, 0.1320, 1.7));
        irmX.put(new VariableName("v10"), new Irm3PL(1.2525, -0.7164, 0.1493, 1.7));

        //gpcm items
        double[] step1 = {-2.1415,  0.0382,  0.6551};
        irmX.put(new VariableName("v11"), new IrmGRM(1.1196, step1, 1.7));

        double[] step2 = {-1.7523, -1.0660,  0.3533};
        irmX.put(new VariableName("v12"), new IrmGRM(1.2290, step2, 1.7));

        double[] step3 = {-2.3126, -1.8816,  0.7757};
        irmX.put(new VariableName("v13"), new IrmGRM(0.6405, step3, 1.7));

        double[] step4 = {-1.9728, -0.2810,  1.1387};
        irmX.put(new VariableName("v14"), new IrmGRM(1.1622 , step4, 1.7));

        double[] step5 = {-2.2207, -0.8252,  0.9702};
        irmX.put(new VariableName("v15"), new IrmGRM(1.2249, step5, 1.7));

        //3pl items
        irmY.put(new VariableName("v1"), new Irm3PL(0.7444, -1.5617,  0.1609, 1.7));
        irmY.put(new VariableName("v2"), new Irm3PL(0.5562, -0.1031,  0.1753, 1.7));
        irmY.put(new VariableName("v3"), new Irm3PL(0.5262, -1.0676, 0.1602, 1.7));
        irmY.put(new VariableName("v4"), new Irm3PL(0.6388, -1.3880,  0.1676, 1.7));
        irmY.put(new VariableName("v5"), new Irm3PL(0.8793, -0.2051,  0.1422, 1.7));
        irmY.put(new VariableName("v6"), new Irm3PL(0.4105,  0.0555,  0.2120, 1.7));
        irmY.put(new VariableName("v7"), new Irm3PL(0.7686, -0.3800,  0.2090, 1.7));
        irmY.put(new VariableName("v8"), new Irm3PL(1.0539, -0.7570,  0.1270, 1.7));
        irmY.put(new VariableName("v9"), new Irm3PL(0.7400,  0.0667,  0.1543, 1.7));
        irmY.put(new VariableName("v10"), new Irm3PL(0.7479,  0.0281, 0.1489, 1.7));

        //gpcm items
        double[] step6 = {-1.7786,  0.7177, 1.45011};
        irmY.put(new VariableName("v11"), new IrmGRM(0.9171, step6, 1.7));

        double[] step7 = {-1.4115, -0.4946, 1.15969};
        irmY.put(new VariableName("v12"), new IrmGRM(0.9751, step7, 1.7));

        double[] step8 = {-1.8478, -1.4078, 1.51339};
        irmY.put(new VariableName("v13"), new IrmGRM(0.5890, step8, 1.7));

        double[] step9 = {-1.6151,  0.3002, 2.04728};
        irmY.put(new VariableName("v14"), new IrmGRM(0.9804, step9, 1.7));

        double[] step10 = {-1.9355, -0.2267, 1.88991};
        irmY.put(new VariableName("v15"), new IrmGRM(1.0117, step10, 1.7));

//        LinkedHashMap<String, ItemResponseModel> irmX = new LinkedHashMap<String, ItemResponseModel>();
//        LinkedHashMap<String, ItemResponseModel> irmY = new LinkedHashMap<String, ItemResponseModel>();
//
//        //3pl items
//        irmX.put("v1", new Irm3PL(1.0755, -1.8758, 0.1240, 1.7));
//        irmX.put("v2", new Irm3PL(0.6428, -0.9211, 0.1361, 1.7));
//        irmX.put("v3", new Irm3PL(0.6198, -1.3362, 0.1276, 1.7));
//        irmX.put("v4", new Irm3PL(0.6835, -1.8967, 0.1619, 1.7));
//        irmX.put("v5", new Irm3PL(0.9892, -0.6427, 0.2050, 1.7));
//        irmX.put("v6", new Irm3PL(0.5784, -0.8181, 0.1168, 1.7));
//        irmX.put("v7", new Irm3PL(0.9822, -0.9897, 0.1053, 1.7));
//        irmX.put("v8", new Irm3PL(1.6026, -1.2382, 0.1202, 1.7));
//        irmX.put("v9", new Irm3PL(0.8988, -0.5180, 0.1320, 1.7));
//        irmX.put("v10", new Irm3PL(1.2525, -0.7164, 0.1493, 1.7));
//
//        //gpcm items
//        double[] step1 = {-2.1415,  0.0382,  0.6551};
//        irmX.put("v11", new IrmGRM(1.1196, step1, 1.7));
//
//        double[] step2 = {-1.7523, -1.0660,  0.3533};
//        irmX.put("v12", new IrmGRM(1.2290, step2, 1.7));
//
//        double[] step3 = {-2.3126, -1.8816,  0.7757};
//        irmX.put("v13", new IrmGRM(0.6405, step3, 1.7));
//
//        double[] step4 = {-1.9728, -0.2810,  1.1387};
//        irmX.put("v14", new IrmGRM(1.1622 , step4, 1.7));
//
//        double[] step5 = {-2.2207, -0.8252,  0.9702};
//        irmX.put("v15", new IrmGRM(1.2249, step5, 1.7));
//
//        //3pl items
//        irmY.put("v1", new Irm3PL(0.7444, -1.5617,  0.1609, 1.7));
//        irmY.put("v2", new Irm3PL(0.5562, -0.1031,  0.1753, 1.7));
//        irmY.put("v3", new Irm3PL(0.5262, -1.0676, 0.1602, 1.7));
//        irmY.put("v4", new Irm3PL(0.6388, -1.3880,  0.1676, 1.7));
//        irmY.put("v5", new Irm3PL(0.8793, -0.2051,  0.1422, 1.7));
//        irmY.put("v6", new Irm3PL(0.4105,  0.0555,  0.2120, 1.7));
//        irmY.put("v7", new Irm3PL(0.7686, -0.3800,  0.2090, 1.7));
//        irmY.put("v8", new Irm3PL(1.0539, -0.7570,  0.1270, 1.7));
//        irmY.put("v9", new Irm3PL(0.7400,  0.0667,  0.1543, 1.7));
//        irmY.put("v10", new Irm3PL(0.7479,  0.0281, 0.1489, 1.7));
//
//        //gpcm items
//        double[] step6 = {-1.7786,  0.7177, 1.45011};
//        irmY.put("v11", new IrmGRM(0.9171, step6, 1.7));
//
//        double[] step7 = {-1.4115, -0.4946, 1.15969};
//        irmY.put("v12", new IrmGRM(0.9751, step7, 1.7));
//
//        double[] step8 = {-1.8478, -1.4078, 1.51339};
//        irmY.put("v13", new IrmGRM(0.5890, step8, 1.7));
//
//        double[] step9 = {-1.6151,  0.3002, 2.04728};
//        irmY.put("v14", new IrmGRM(0.9804, step9, 1.7));
//
//        double[] step10 = {-1.9355, -0.2267, 1.88991};
//        irmY.put("v15", new IrmGRM(1.0117, step10, 1.7));

        UniformQuadratureRule uniform = new UniformQuadratureRule(-3.0, 3.0, 25);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, uniform, uniform, EquatingCriterionType.Q1Q2);
        sl.setPrecision(4);
        double[] startValues = {0,1};

        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer.optimize(new MaxEval(1000),
                           new ObjectiveFunction(sl),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(2),
                           new InitialGuess(startValues));

        double[] slCoefficients = optimum.getPoint();
        sl.setIntercept(slCoefficients[0]);
        sl.setScale(slCoefficients[1]);

        System.out.println("  Iterations: " + optimizer.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + slCoefficients[0] + "  A = " + slCoefficients[1]);

        assertEquals("  Intercept test", 0.730924, sl.getIntercept(), 1e-4);
        assertEquals("  Scale test",1.218034 , sl.getScale(), 1e-4);

        System.out.println();



    }

    @Test
    public void stockingLordTestMixedFormatGradedResponse2(){
        System.out.println("StockingLordMethod Test 5: Mixed format test, symmetric, Graded Response 2");
        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        //3pl items
        irmX.put(new VariableName("v1"), new Irm3PL(1.0755, -1.8758, 0.1240, 1.7));
        irmX.put(new VariableName("v2"), new Irm3PL(0.6428, -0.9211, 0.1361, 1.7));
        irmX.put(new VariableName("v3"), new Irm3PL(0.6198, -1.3362, 0.1276, 1.7));
        irmX.put(new VariableName("v4"), new Irm3PL(0.6835, -1.8967, 0.1619, 1.7));
        irmX.put(new VariableName("v5"), new Irm3PL(0.9892, -0.6427, 0.2050, 1.7));
        irmX.put(new VariableName("v6"), new Irm3PL(0.5784, -0.8181, 0.1168, 1.7));
        irmX.put(new VariableName("v7"), new Irm3PL(0.9822, -0.9897, 0.1053, 1.7));
        irmX.put(new VariableName("v8"), new Irm3PL(1.6026, -1.2382, 0.1202, 1.7));
        irmX.put(new VariableName("v9"), new Irm3PL(0.8988, -0.5180, 0.1320, 1.7));
        irmX.put(new VariableName("v10"), new Irm3PL(1.2525, -0.7164, 0.1493, 1.7));

        //gpcm items
        double[] step1 = {-2.1415,  0.0382,  0.6551};
        irmX.put(new VariableName("v11"), new IrmGRM(1.1196, step1, 1.7));

        double[] step2 = {-1.7523, -1.0660,  0.3533};
        irmX.put(new VariableName("v12"), new IrmGRM(1.2290, step2, 1.7));

        double[] step3 = {-2.3126, -1.8816,  0.7757};
        irmX.put(new VariableName("v13"), new IrmGRM(0.6405, step3, 1.7));

        double[] step4 = {-1.9728, -0.2810,  1.1387};
        irmX.put(new VariableName("v14"), new IrmGRM(1.1622 , step4, 1.7));

        double[] step5 = {-2.2207, -0.8252,  0.9702};
        irmX.put(new VariableName("v15"), new IrmGRM(1.2249, step5, 1.7));

        //3pl items
        irmY.put(new VariableName("v1"), new Irm3PL(0.7444, -1.5617,  0.1609, 1.7));
        irmY.put(new VariableName("v2"), new Irm3PL(0.5562, -0.1031,  0.1753, 1.7));
        irmY.put(new VariableName("v3"), new Irm3PL(0.5262, -1.0676, 0.1602, 1.7));
        irmY.put(new VariableName("v4"), new Irm3PL(0.6388, -1.3880,  0.1676, 1.7));
        irmY.put(new VariableName("v5"), new Irm3PL(0.8793, -0.2051,  0.1422, 1.7));
        irmY.put(new VariableName("v6"), new Irm3PL(0.4105,  0.0555,  0.2120, 1.7));
        irmY.put(new VariableName("v7"), new Irm3PL(0.7686, -0.3800,  0.2090, 1.7));
        irmY.put(new VariableName("v8"), new Irm3PL(1.0539, -0.7570,  0.1270, 1.7));
        irmY.put(new VariableName("v9"), new Irm3PL(0.7400,  0.0667,  0.1543, 1.7));
        irmY.put(new VariableName("v10"), new Irm3PL(0.7479,  0.0281, 0.1489, 1.7));

        //gpcm items
        double[] step6 = {-1.7786,  0.7177, 1.45011};
        irmY.put(new VariableName("v11"), new IrmGRM(0.9171, step6, 1.7));

        double[] step7 = {-1.4115, -0.4946, 1.15969};
        irmY.put(new VariableName("v12"), new IrmGRM(0.9751, step7, 1.7));

        double[] step8 = {-1.8478, -1.4078, 1.51339};
        irmY.put(new VariableName("v13"), new IrmGRM(0.5890, step8, 1.7));

        double[] step9 = {-1.6151,  0.3002, 2.04728};
        irmY.put(new VariableName("v14"), new IrmGRM(0.9804, step9, 1.7));

        double[] step10 = {-1.9355, -0.2267, 1.88991};
        irmY.put(new VariableName("v15"), new IrmGRM(1.0117, step10, 1.7));

//        LinkedHashMap<String, ItemResponseModel> irmX = new LinkedHashMap<String, ItemResponseModel>();
//        LinkedHashMap<String, ItemResponseModel> irmY = new LinkedHashMap<String, ItemResponseModel>();
//
//        //3pl items
//        irmX.put("v1", new Irm3PL(1.0755, -1.8758, 0.1240, 1.7));
//        irmX.put("v2", new Irm3PL(0.6428, -0.9211, 0.1361, 1.7));
//        irmX.put("v3", new Irm3PL(0.6198, -1.3362, 0.1276, 1.7));
//        irmX.put("v4", new Irm3PL(0.6835, -1.8967, 0.1619, 1.7));
//        irmX.put("v5", new Irm3PL(0.9892, -0.6427, 0.2050, 1.7));
//        irmX.put("v6", new Irm3PL(0.5784, -0.8181, 0.1168, 1.7));
//        irmX.put("v7", new Irm3PL(0.9822, -0.9897, 0.1053, 1.7));
//        irmX.put("v8", new Irm3PL(1.6026, -1.2382, 0.1202, 1.7));
//        irmX.put("v9", new Irm3PL(0.8988, -0.5180, 0.1320, 1.7));
//        irmX.put("v10", new Irm3PL(1.2525, -0.7164, 0.1493, 1.7));
//
//        //gpcm items
//        double[] step1 = {-2.1415,  0.0382,  0.6551};
//        irmX.put("v11", new IrmGRM(1.1196, step1, 1.7));
//
//        double[] step2 = {-1.7523, -1.0660,  0.3533};
//        irmX.put("v12", new IrmGRM(1.2290, step2, 1.7));
//
//        double[] step3 = {-2.3126, -1.8816,  0.7757};
//        irmX.put("v13", new IrmGRM(0.6405, step3, 1.7));
//
//        double[] step4 = {-1.9728, -0.2810,  1.1387};
//        irmX.put("v14", new IrmGRM(1.1622 , step4, 1.7));
//
//        double[] step5 = {-2.2207, -0.8252,  0.9702};
//        irmX.put("v15", new IrmGRM(1.2249, step5, 1.7));
//
//        //3pl items
//        irmY.put("v1", new Irm3PL(0.7444, -1.5617,  0.1609, 1.7));
//        irmY.put("v2", new Irm3PL(0.5562, -0.1031,  0.1753, 1.7));
//        irmY.put("v3", new Irm3PL(0.5262, -1.0676, 0.1602, 1.7));
//        irmY.put("v4", new Irm3PL(0.6388, -1.3880,  0.1676, 1.7));
//        irmY.put("v5", new Irm3PL(0.8793, -0.2051,  0.1422, 1.7));
//        irmY.put("v6", new Irm3PL(0.4105,  0.0555,  0.2120, 1.7));
//        irmY.put("v7", new Irm3PL(0.7686, -0.3800,  0.2090, 1.7));
//        irmY.put("v8", new Irm3PL(1.0539, -0.7570,  0.1270, 1.7));
//        irmY.put("v9", new Irm3PL(0.7400,  0.0667,  0.1543, 1.7));
//        irmY.put("v10", new Irm3PL(0.7479,  0.0281, 0.1489, 1.7));
//
//        //gpcm items
//        double[] step6 = {-1.7786,  0.7177, 1.45011};
//        irmY.put("v11", new IrmGRM(0.9171, step6, 1.7));
//
//        double[] step7 = {-1.4115, -0.4946, 1.15969};
//        irmY.put("v12", new IrmGRM(0.9751, step7, 1.7));
//
//        double[] step8 = {-1.8478, -1.4078, 1.51339};
//        irmY.put("v13", new IrmGRM(0.5890, step8, 1.7));
//
//        double[] step9 = {-1.6151,  0.3002, 2.04728};
//        irmY.put("v14", new IrmGRM(0.9804, step9, 1.7));
//
//        double[] step10 = {-1.9355, -0.2267, 1.88991};
//        irmY.put("v15", new IrmGRM(1.0117, step10, 1.7));

        ContinuousQuadratureRule distX = new ContinuousQuadratureRule(points, xDensity);
        ContinuousQuadratureRule distY = new ContinuousQuadratureRule(points, yDensity);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, distX, distY, EquatingCriterionType.Q1Q2);
        sl.setPrecision(4);
        double[] startValues = {0,1};

        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer.optimize(new MaxEval(1000),
                           new ObjectiveFunction(sl),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(2),
                           new InitialGuess(startValues));

        double[] slCoefficients = optimum.getPoint();
        sl.setIntercept(slCoefficients[0]);
        sl.setScale(slCoefficients[1]);

        System.out.println("  Iterations: " + optimizer.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + slCoefficients[0] + "  A = " + slCoefficients[1]);

        assertEquals("  Intercept test", 0.735177, sl.getIntercept(), 1e-4);
        assertEquals("  Scale test", 1.213684, sl.getScale(), 1e-4);

        System.out.println();

    }

    @Test
    public void stockingLordTestMixedFormatGradedResponse3(){
        System.out.println("StockingLordMethod Test 5: Mixed format test, backwards, Graded Response 3");

        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        //3pl items
        irmX.put(new VariableName("v1"), new Irm3PL(1.0755, -1.8758, 0.1240, 1.7));
        irmX.put(new VariableName("v2"), new Irm3PL(0.6428, -0.9211, 0.1361, 1.7));
        irmX.put(new VariableName("v3"), new Irm3PL(0.6198, -1.3362, 0.1276, 1.7));
        irmX.put(new VariableName("v4"), new Irm3PL(0.6835, -1.8967, 0.1619, 1.7));
        irmX.put(new VariableName("v5"), new Irm3PL(0.9892, -0.6427, 0.2050, 1.7));
        irmX.put(new VariableName("v6"), new Irm3PL(0.5784, -0.8181, 0.1168, 1.7));
        irmX.put(new VariableName("v7"), new Irm3PL(0.9822, -0.9897, 0.1053, 1.7));
        irmX.put(new VariableName("v8"), new Irm3PL(1.6026, -1.2382, 0.1202, 1.7));
        irmX.put(new VariableName("v9"), new Irm3PL(0.8988, -0.5180, 0.1320, 1.7));
        irmX.put(new VariableName("v10"), new Irm3PL(1.2525, -0.7164, 0.1493, 1.7));

        //gpcm items
        double[] step1 = {-2.1415,  0.0382,  0.6551};
        irmX.put(new VariableName("v11"), new IrmGRM(1.1196, step1, 1.7));

        double[] step2 = {-1.7523, -1.0660,  0.3533};
        irmX.put(new VariableName("v12"), new IrmGRM(1.2290, step2, 1.7));

        double[] step3 = {-2.3126, -1.8816,  0.7757};
        irmX.put(new VariableName("v13"), new IrmGRM(0.6405, step3, 1.7));

        double[] step4 = {-1.9728, -0.2810,  1.1387};
        irmX.put(new VariableName("v14"), new IrmGRM(1.1622 , step4, 1.7));

        double[] step5 = {-2.2207, -0.8252,  0.9702};
        irmX.put(new VariableName("v15"), new IrmGRM(1.2249, step5, 1.7));

        //3pl items
        irmY.put(new VariableName("v1"), new Irm3PL(0.7444, -1.5617,  0.1609, 1.7));
        irmY.put(new VariableName("v2"), new Irm3PL(0.5562, -0.1031,  0.1753, 1.7));
        irmY.put(new VariableName("v3"), new Irm3PL(0.5262, -1.0676, 0.1602, 1.7));
        irmY.put(new VariableName("v4"), new Irm3PL(0.6388, -1.3880,  0.1676, 1.7));
        irmY.put(new VariableName("v5"), new Irm3PL(0.8793, -0.2051,  0.1422, 1.7));
        irmY.put(new VariableName("v6"), new Irm3PL(0.4105,  0.0555,  0.2120, 1.7));
        irmY.put(new VariableName("v7"), new Irm3PL(0.7686, -0.3800,  0.2090, 1.7));
        irmY.put(new VariableName("v8"), new Irm3PL(1.0539, -0.7570,  0.1270, 1.7));
        irmY.put(new VariableName("v9"), new Irm3PL(0.7400,  0.0667,  0.1543, 1.7));
        irmY.put(new VariableName("v10"), new Irm3PL(0.7479,  0.0281, 0.1489, 1.7));

        //gpcm items
        double[] step6 = {-1.7786,  0.7177, 1.45011};
        irmY.put(new VariableName("v11"), new IrmGRM(0.9171, step6, 1.7));

        double[] step7 = {-1.4115, -0.4946, 1.15969};
        irmY.put(new VariableName("v12"), new IrmGRM(0.9751, step7, 1.7));

        double[] step8 = {-1.8478, -1.4078, 1.51339};
        irmY.put(new VariableName("v13"), new IrmGRM(0.5890, step8, 1.7));

        double[] step9 = {-1.6151,  0.3002, 2.04728};
        irmY.put(new VariableName("v14"), new IrmGRM(0.9804, step9, 1.7));

        double[] step10 = {-1.9355, -0.2267, 1.88991};
        irmY.put(new VariableName("v15"), new IrmGRM(1.0117, step10, 1.7));

//        LinkedHashMap<String, ItemResponseModel> irmX = new LinkedHashMap<String, ItemResponseModel>();
//        LinkedHashMap<String, ItemResponseModel> irmY = new LinkedHashMap<String, ItemResponseModel>();
//
//        //3pl items
//        irmX.put("v1", new Irm3PL(1.0755, -1.8758, 0.1240, 1.7));
//        irmX.put("v2", new Irm3PL(0.6428, -0.9211, 0.1361, 1.7));
//        irmX.put("v3", new Irm3PL(0.6198, -1.3362, 0.1276, 1.7));
//        irmX.put("v4", new Irm3PL(0.6835, -1.8967, 0.1619, 1.7));
//        irmX.put("v5", new Irm3PL(0.9892, -0.6427, 0.2050, 1.7));
//        irmX.put("v6", new Irm3PL(0.5784, -0.8181, 0.1168, 1.7));
//        irmX.put("v7", new Irm3PL(0.9822, -0.9897, 0.1053, 1.7));
//        irmX.put("v8", new Irm3PL(1.6026, -1.2382, 0.1202, 1.7));
//        irmX.put("v9", new Irm3PL(0.8988, -0.5180, 0.1320, 1.7));
//        irmX.put("v10", new Irm3PL(1.2525, -0.7164, 0.1493, 1.7));
//
//        //gpcm items
//        double[] step1 = {-2.1415,  0.0382,  0.6551};
//        irmX.put("v11", new IrmGRM(1.1196, step1, 1.7));
//
//        double[] step2 = {-1.7523, -1.0660,  0.3533};
//        irmX.put("v12", new IrmGRM(1.2290, step2, 1.7));
//
//        double[] step3 = {-2.3126, -1.8816,  0.7757};
//        irmX.put("v13", new IrmGRM(0.6405, step3, 1.7));
//
//        double[] step4 = {-1.9728, -0.2810,  1.1387};
//        irmX.put("v14", new IrmGRM(1.1622 , step4, 1.7));
//
//        double[] step5 = {-2.2207, -0.8252,  0.9702};
//        irmX.put("v15", new IrmGRM(1.2249, step5, 1.7));
//
//        //3pl items
//        irmY.put("v1", new Irm3PL(0.7444, -1.5617,  0.1609, 1.7));
//        irmY.put("v2", new Irm3PL(0.5562, -0.1031,  0.1753, 1.7));
//        irmY.put("v3", new Irm3PL(0.5262, -1.0676, 0.1602, 1.7));
//        irmY.put("v4", new Irm3PL(0.6388, -1.3880,  0.1676, 1.7));
//        irmY.put("v5", new Irm3PL(0.8793, -0.2051,  0.1422, 1.7));
//        irmY.put("v6", new Irm3PL(0.4105,  0.0555,  0.2120, 1.7));
//        irmY.put("v7", new Irm3PL(0.7686, -0.3800,  0.2090, 1.7));
//        irmY.put("v8", new Irm3PL(1.0539, -0.7570,  0.1270, 1.7));
//        irmY.put("v9", new Irm3PL(0.7400,  0.0667,  0.1543, 1.7));
//        irmY.put("v10", new Irm3PL(0.7479,  0.0281, 0.1489, 1.7));
//
//        //gpcm items
//        double[] step6 = {-1.7786,  0.7177, 1.45011};
//        irmY.put("v11", new IrmGRM(0.9171, step6, 1.7));
//
//        double[] step7 = {-1.4115, -0.4946, 1.15969};
//        irmY.put("v12", new IrmGRM(0.9751, step7, 1.7));
//
//        double[] step8 = {-1.8478, -1.4078, 1.51339};
//        irmY.put("v13", new IrmGRM(0.5890, step8, 1.7));
//
//        double[] step9 = {-1.6151,  0.3002, 2.04728};
//        irmY.put("v14", new IrmGRM(0.9804, step9, 1.7));
//
//        double[] step10 = {-1.9355, -0.2267, 1.88991};
//        irmY.put("v15", new IrmGRM(1.0117, step10, 1.7));

        ContinuousQuadratureRule distX = new ContinuousQuadratureRule(points, xDensity);
        ContinuousQuadratureRule distY = new ContinuousQuadratureRule(points, yDensity);

        StockingLordMethod sl = new StockingLordMethod(irmX, irmY, distX, distY, EquatingCriterionType.Q1);
        sl.setPrecision(4);
        double[] startValues = {0,1};

        int numIterpolationPoints = 2 * 2;//two dimensions A and B
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum = optimizer.optimize(new MaxEval(1000),
                           new ObjectiveFunction(sl),
                           GoalType.MINIMIZE,
                           SimpleBounds.unbounded(2),
                           new InitialGuess(startValues));

        double[] slCoefficients = optimum.getPoint();
        sl.setIntercept(slCoefficients[0]);
        sl.setScale(slCoefficients[1]);

        System.out.println("  Iterations: " + optimizer.getEvaluations());
        System.out.println("  fmin: " + optimum.getValue());
        System.out.println("  B = " + slCoefficients[0] + "  A = " + slCoefficients[1]);

        assertEquals("  Intercept test", 0.739394, sl.getIntercept(), 1e-4);
        assertEquals("  Scale test", 1.218372, sl.getScale(), 1e-4);

        System.out.println();

    }

    /**
     * This test uses Rasch model items.
     *
     *
     */
    @Test
    public void raschModelTest(){
        System.out.println("Stocking-Lord test: Rasch model");

        LinkedHashMap<VariableName, ItemResponseModel> itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("Item1"), new Irm3PL(-3.188047976, 1.0));
        itemFormX.put(new VariableName("Item2"), new Irm3PL(1.031760328, 1.0));
        itemFormX.put(new VariableName("Item3"), new Irm3PL(0.819040914, 1.0));
        itemFormX.put(new VariableName("Item4"), new Irm3PL(-2.706947360, 1.0));
        itemFormX.put(new VariableName("Item5"), new Irm3PL(-0.094527077, 1.0));
        itemFormX.put(new VariableName("Item6"), new Irm3PL(0.689697135, 1.0));
        itemFormX.put(new VariableName("Item7"), new Irm3PL(-0.551837153, 1.0));
        itemFormX.put(new VariableName("Item8"), new Irm3PL(-0.359559276, 1.0));

        itemFormY.put(new VariableName("Item1"), new Irm3PL(-3.074599226, 1.0));
        itemFormY.put(new VariableName("Item2"), new Irm3PL(1.012824350, 1.0));
        itemFormY.put(new VariableName("Item3"), new Irm3PL(0.868538408, 1.0));
        itemFormY.put(new VariableName("Item4"), new Irm3PL(-2.404483603, 1.0));
        itemFormY.put(new VariableName("Item5"), new Irm3PL(0.037402866, 1.0));
        itemFormY.put(new VariableName("Item6"), new Irm3PL(0.700747420, 1.0));
        itemFormY.put(new VariableName("Item7"), new Irm3PL(-0.602555046, 1.0));
        itemFormY.put(new VariableName("Item8"), new Irm3PL(-0.350426446, 1.0));

//        LinkedHashMap<String, ItemResponseModel> itemFormX = new LinkedHashMap<String, ItemResponseModel>();
//        LinkedHashMap<String, ItemResponseModel> itemFormY = new LinkedHashMap<String, ItemResponseModel>();
//
//        itemFormX.put("Item1", new Irm3PL(-3.188047976, 1.0));
//        itemFormX.put("Item2", new Irm3PL(1.031760328, 1.0));
//        itemFormX.put("Item3", new Irm3PL(0.819040914, 1.0));
//        itemFormX.put("Item4", new Irm3PL(-2.706947360, 1.0));
//        itemFormX.put("Item5", new Irm3PL(-0.094527077, 1.0));
//        itemFormX.put("Item6", new Irm3PL(0.689697135, 1.0));
//        itemFormX.put("Item7", new Irm3PL(-0.551837153, 1.0));
//        itemFormX.put("Item8", new Irm3PL(-0.359559276, 1.0));
//
//        itemFormY.put("Item1", new Irm3PL(-3.074599226, 1.0));
//        itemFormY.put("Item2", new Irm3PL(1.012824350, 1.0));
//        itemFormY.put("Item3", new Irm3PL(0.868538408, 1.0));
//        itemFormY.put("Item4", new Irm3PL(-2.404483603, 1.0));
//        itemFormY.put("Item5", new Irm3PL(0.037402866, 1.0));
//        itemFormY.put("Item6", new Irm3PL(0.700747420, 1.0));
//        itemFormY.put("Item7", new Irm3PL(-0.602555046, 1.0));
//        itemFormY.put("Item8", new Irm3PL(-0.350426446, 1.0));

       double f = 0;
        double[] param1 = new double[2];
        double[] param2 = new double[2];

        UniformQuadratureRule distX = new UniformQuadratureRule(-4.0, 4.0, 161);//plink default
        UniformQuadratureRule distY = new UniformQuadratureRule(-4.0, 4.0, 161);//plink default

        StockingLordMethod sl = new StockingLordMethod(itemFormX, itemFormY, distX, distY, EquatingCriterionType.Q1Q2);
        sl.setPrecision(6);
        double[] startValues = {0};//Using a start value for the intercept only for the Rasch family of models.

        DefaultUncminOptimizer optimizer = new DefaultUncminOptimizer();

        try{
            optimizer.minimize(sl, startValues);
            double[] r = optimizer.getParameters();
            param1[0] = r[0];
            param1[1] = 1;
            f = optimizer.getFunctionValue();
            sl.setIntercept(param1[0]);
            sl.setScale(param1[1]);

        }catch(UncminException ex){
            ex.printStackTrace();
        }

        //Check Brent Optimizer values against results from plink.
        System.out.println("  UNCMIN Optimization");
        System.out.println("  Iterations: ");
        System.out.println("  fmin: " + f);
        System.out.println("  B = " + sl.getIntercept() + "  A = " + sl.getScale());

        assertEquals("  Intercept test", 0.057566, sl.getIntercept(), 1e-3);//true result from plink package in R
        assertEquals("  Scale test", 1.0, sl.getScale(), 1e-4);


        BrentOptimizer brentOptimizer = new BrentOptimizer(1e-8, 1e-8);
        UnivariatePointValuePair pair = brentOptimizer.optimize(new MaxEval(200),
                new UnivariateObjectiveFunction(sl),
                GoalType.MINIMIZE,
                new SearchInterval(-3, 3));

        param2 = new double[2];
        sl.setIntercept(pair.getPoint());
        sl.setScale(1.0);
        f = pair.getValue();

        param2[0] = pair.getPoint();
        param2[1] = 1;
        sl.setIntercept(param2[0]);
        sl.setScale(param2[1]);
        f = pair.getValue();

        //Check Brent results against plink results.
        System.out.println();
        System.out.println("  Brent Optimization");
        System.out.println("  Iterations: ");
        System.out.println("  fmin: " + f);
        System.out.println("  B = " + sl.getIntercept() + "  A = " + sl.getScale());

        assertEquals("  Intercept test", 0.057566, sl.getIntercept(), 1e-3);//true result from plink package in R
        assertEquals("  Scale test", 1.0, sl.getScale(), 1e-4);


        //Compare UMNCMIN and Brent optimizer results.
        assertEquals("  UNCMIN/Brent intercept test", param1[0], param2[0], 1e-6);
        assertEquals("  UNCMIN/Brent slope test", param1[1], param2[1], 1e-6);

    }

    /**
     * This test uses a combination of 3PL and PCM items. Results compared to plink
     *
     * plink code:
     *
     * library(plink)
     *
     * fX<-matrix(c(
     * 1, -3.188047976, 0,NA,NA,
     * 1,  1.031760328, 0,NA,NA,
     * 1,  0.819040914, 0,NA,NA,
     * 1, -2.706947360, 0,NA,NA,
     * 1, -0.094527077, 0,NA,NA,
     * 1,  0.689697135, 0,NA,NA,
     * 1, -0.551837153, 0,NA,NA,
     * 1, -0.359559276, 0,NA,NA,
     * 1, -1.451470831, -0.146619694, -0.636399040, 0.783018734),
     * nrow=9, byrow=TRUE)
     * fX<-as.data.frame(fX)
     * names(fX)<-c("aparam", "bparam","cparam","s1","s2")
     *
     * fY<-matrix(c(
     * 1,-3.074599226,0,NA,NA,
     * 1,1.01282435,0,NA,NA,
     * 1,0.868538408,0,NA,NA,
     * 1,-2.404483603,0,NA,NA,
     * 1,0.037402866,0,NA,NA,
     * 1,0.70074742,0,NA,NA,
     * 1,-0.602555046,0,NA,NA,
     * 1,-0.350426446,0,NA,NA,
     * 1,-1.267744832,-0.185885988,-0.61535623,0.801242218),
     * nrow=9, byrow=TRUE)
     * fY<-as.data.frame(fY)
     * names(fY)<-c("aparam", "bparam","cparam","s1","s2")
     *
     * common<-cbind(1:9, 1:9)
     * cat<-c(rep(2,8),4)
     *
     * pmX <- as.poly.mod(9,c("drm","gpcm"),list(1:8,9))
     * pmY <- as.poly.mod(9,c("drm","gpcm"),list(1:8,9))
     *
     * pars <- as.irt.pars(list(fx=fX,fy=fY), common, cat=list(fx=cat,fy=cat),
     * poly.mod=list(pmX,pmY), location=c(TRUE,TRUE))
     *
     * plink(pars, startvals=c(1,0), rescale="SL", base.grp=2, D=1.0, symmetric=TRUE)
     *
     *
     */
    @Test
    public void mixedFormtRaschPCMTest(){
        System.out.println("Mixed format Stocking-Lord test: Rasch and PCM");

        LinkedHashMap<VariableName, ItemResponseModel> itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("Item1"), new Irm3PL(-3.188047976, 1.0));
        itemFormX.put(new VariableName("Item2"), new Irm3PL(1.031760328, 1.0));
        itemFormX.put(new VariableName("Item3"), new Irm3PL(0.819040914, 1.0));
        itemFormX.put(new VariableName("Item4"), new Irm3PL(-2.706947360, 1.0));
        itemFormX.put(new VariableName("Item5"), new Irm3PL(-0.094527077, 1.0));
        itemFormX.put(new VariableName("Item6"), new Irm3PL(0.689697135, 1.0));
        itemFormX.put(new VariableName("Item7"), new Irm3PL(-0.551837153, 1.0));
        itemFormX.put(new VariableName("Item8"), new Irm3PL(-0.359559276, 1.0));
        double[] step1x = {-0.146619694, -0.636399040, 0.783018734};
        itemFormX.put(new VariableName("Item9"), new IrmPCM(-1.451470831, step1x, 1.0));

        itemFormY.put(new VariableName("Item1"), new Irm3PL(-3.074599226, 1.0));
        itemFormY.put(new VariableName("Item2"), new Irm3PL(1.012824350, 1.0));
        itemFormY.put(new VariableName("Item3"), new Irm3PL(0.868538408, 1.0));
        itemFormY.put(new VariableName("Item4"), new Irm3PL(-2.404483603, 1.0));
        itemFormY.put(new VariableName("Item5"), new Irm3PL(0.037402866, 1.0));
        itemFormY.put(new VariableName("Item6"), new Irm3PL(0.700747420, 1.0));
        itemFormY.put(new VariableName("Item7"), new Irm3PL(-0.602555046, 1.0));
        itemFormY.put(new VariableName("Item8"), new Irm3PL(-0.350426446, 1.0));
        double[] step1y = {-0.185885988, -0.61535623, 0.801242218};
        itemFormY.put(new VariableName("Item9"), new IrmPCM(-1.267744832, step1y, 1.0));

//        LinkedHashMap<String, ItemResponseModel> itemFormX = new LinkedHashMap<String, ItemResponseModel>();
//        LinkedHashMap<String, ItemResponseModel> itemFormY = new LinkedHashMap<String, ItemResponseModel>();
//
//        itemFormX.put("Item1", new Irm3PL(-3.188047976, 1.0));
//        itemFormX.put("Item2", new Irm3PL(1.031760328, 1.0));
//        itemFormX.put("Item3", new Irm3PL(0.819040914, 1.0));
//        itemFormX.put("Item4", new Irm3PL(-2.706947360, 1.0));
//        itemFormX.put("Item5", new Irm3PL(-0.094527077, 1.0));
//        itemFormX.put("Item6", new Irm3PL(0.689697135, 1.0));
//        itemFormX.put("Item7", new Irm3PL(-0.551837153, 1.0));
//        itemFormX.put("Item8", new Irm3PL(-0.359559276, 1.0));
//        double[] step1x = {-0.146619694, -0.636399040, 0.783018734};
//        itemFormX.put("Item9", new IrmPCM(-1.451470831, step1x, 1.0));
//
//        itemFormY.put("Item1", new Irm3PL(-3.074599226, 1.0));
//        itemFormY.put("Item2", new Irm3PL(1.012824350, 1.0));
//        itemFormY.put("Item3", new Irm3PL(0.868538408, 1.0));
//        itemFormY.put("Item4", new Irm3PL(-2.404483603, 1.0));
//        itemFormY.put("Item5", new Irm3PL(0.037402866, 1.0));
//        itemFormY.put("Item6", new Irm3PL(0.700747420, 1.0));
//        itemFormY.put("Item7", new Irm3PL(-0.602555046, 1.0));
//        itemFormY.put("Item8", new Irm3PL(-0.350426446, 1.0));
//        double[] step1y = {-0.185885988, -0.61535623, 0.801242218};
//        itemFormY.put("Item9", new IrmPCM(-1.267744832, step1y, 1.0));

        double f = 0;
        double[] param1 = new double[2];
        double[] param2 = new double[2];

        UniformQuadratureRule distX = new UniformQuadratureRule(-4.0, 4.0, 161);//plink default
        UniformQuadratureRule distY = new UniformQuadratureRule(-4.0, 4.0, 161);//plink default

        StockingLordMethod sl = new StockingLordMethod(itemFormX, itemFormY, distX, distY, EquatingCriterionType.Q1Q2);
        sl.setPrecision(6);
        double[] startValues = {0.0};//Using a start value for the intercept only for the Rasch family of models.

        DefaultUncminOptimizer optimizer = new DefaultUncminOptimizer();

        try{
            optimizer.minimize(sl, startValues);
            double[] r = optimizer.getParameters();
            param1[0] = r[0];
            param1[1] = 1.0;
            f = optimizer.getFunctionValue();
            sl.setIntercept(param1[0]);
            sl.setScale(param1[1]);

        }catch(UncminException ex){
            ex.printStackTrace();
        }


        //Check UNCMIN values against results from plink.
        System.out.println("  UNCMIN Optimization");
        System.out.println("  Iterations: ");
        System.out.println("  fmin: " + f);
        System.out.println("  B = " + sl.getIntercept() + "  A = " + sl.getScale());

        assertEquals("  Intercept test", 0.101388, sl.getIntercept(), 1e-4);//True results from plink package in R
        assertEquals("  Scale test", 1.0, sl.getScale(), 1e-4);

        //Check Brent optimizer values against plink
        BrentOptimizer brentOptimizer = new BrentOptimizer(1e-8, 1e-8);
        UnivariatePointValuePair pair = brentOptimizer.optimize(new MaxEval(200),
                new UnivariateObjectiveFunction(sl),
                GoalType.MINIMIZE,
                new SearchInterval(-3, 3));
        param2[0] = pair.getPoint();
        param2[1] = 1.0;
        sl.setIntercept(param2[0]);
        sl.setScale(param2[1]);
        f = pair.getValue();

        System.out.println();
        System.out.println("  Brent Optimization");
        System.out.println("  Iterations: ");
        System.out.println("  fmin: " + f);
        System.out.println("  B = " + sl.getIntercept() + "  A = " + sl.getScale());

        //Check UNCMIN values against results from plink.
        assertEquals("  Intercept test", 0.101388, sl.getIntercept(), 1e-4);
        assertEquals("  Scale test", 1.0, sl.getScale(), 1e-4);


        //Compare UMNCMIN and Brent optimizer results.
        assertEquals("  UNCMIN/Brent intercept test", param1[0], param2[0], 1e-6);
        assertEquals("  UNCMIN/Brent slope test", param1[1], param2[1], 1e-6);



    }


}

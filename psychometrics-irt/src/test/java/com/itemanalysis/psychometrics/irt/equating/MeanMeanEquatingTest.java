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
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmGPCM;
import com.itemanalysis.psychometrics.irt.model.IrmGPCM2;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.junit.Test;

import java.util.LinkedHashMap;

import static junit.framework.Assert.assertEquals;

public class MeanMeanEquatingTest {

    double[] aX = {0.455118, 0.583871, 0.754398, 0.663274, 1.068977, 0.967194, 0.347868, 1.457918, 0.701952, 1.407967, 1.299285};
    double[] bX = {-0.710086, -0.856669, 0.021221, 0.050618, 0.961047, 0.194976, 2.276794, 1.024128, 2.240131, 1.555634, 2.158933};
    double[] cX = {0.208748, 0.203834, 0.159961, 0.123961, 0.298628, 0.053538, 0.148927, 0.24527, 0.08529, 0.078897, 0.10753};
    double[] aY = {0.441595, 0.572995, 0.598719, 0.604125, 0.990164, 0.808079, 0.413973, 1.355437, 0.633562, 1.134661, 0.925521};
    double[] bY = {-1.334933, -1.321004, -0.709831, -0.353942, 0.531956, -0.115649, 2.553812, 0.581109, 1.896027, 1.079013, 2.133706};
    double[] cY = {0.155883, 0.191298, 0.117663, 0.081759, 0.302443, 0.064791, 0.240967, 0.224322, 0.079396, 0.063009, 0.125873};


    @Test
    public void meanMeanTest1(){
        System.out.println("Mean Mean Test 3PL");

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

        MeanMeanMethod mm = new MeanMeanMethod(irmX, irmY);
        mm.setPrecision(4);

        assertEquals("  Intercept test", -0.4790, mm.getIntercept(), 1e-4);
        assertEquals("  Scale test", 1.1449, mm.getScale(), 1e-4);

    }

    /**
     * Tests the calculations needed for mean/mean and mean/sigma scale linking.
     * Item parameters and true values obtained from example 2 from the STUIRT
     * program by Michael Kolen and colleagues. Note that the original example
     * used teh PARSCALE version of item parameters. These were converted to
     * ICL type parameters by subtracting a step from the item difficulty.
     *
     */
    @Test
    public void mixedFormatMeanMeanTest(){
        System.out.println("Mixed format mean/mean test");

        LinkedHashMap<VariableName, ItemResponseModel> itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("Item1"), new Irm3PL(0.751335,-0.897391, 0.244001, 1.7));
        itemFormX.put(new VariableName("Item2"), new Irm3PL(0.955947,-0.811477, 0.242883, 1.7));
        itemFormX.put(new VariableName("Item3"), new Irm3PL(0.497206,-0.858681, 0.260893, 1.7));
        itemFormX.put(new VariableName("Item4"), new Irm3PL(0.724000,-0.123911, 0.243497, 1.7));
        itemFormX.put(new VariableName("Item5"), new Irm3PL(0.865200, 0.205889, 0.319135, 1.7));
        itemFormX.put(new VariableName("Item6"), new Irm3PL(0.658129, 0.555228, 0.277826, 1.7));
        itemFormX.put(new VariableName("Item7"), new Irm3PL(1.082118, 0.950549, 0.157979, 1.7));
        itemFormX.put(new VariableName("Item8"), new Irm3PL(0.988294, 1.377501, 0.084828, 1.7));
        itemFormX.put(new VariableName("Item9"), new Irm3PL(1.248923, 1.614355, 0.181874, 1.7));
        itemFormX.put(new VariableName("Item10"), new Irm3PL(1.116682, 2.353932, 0.246856, 1.7));
        itemFormX.put(new VariableName("Item11"), new Irm3PL(0.438171, 3.217965, 0.309243, 1.7));
        itemFormX.put(new VariableName("Item12"), new Irm3PL(1.082206, 4.441864, 0.192339, 1.7));
        double[] step1 = {0, -1.09327,1.101266};
        itemFormX.put(new VariableName("Item13"), new IrmGPCM(0.269994, step1, 1.7));
        double[] step2 = {0, 1.526148,1.739176};
        itemFormX.put(new VariableName("Item14"), new IrmGPCM(0.972506, step2, 1.7));
        double[] step3 = {0, 1.362356,5.566958};
        itemFormX.put(new VariableName("Item15"), new IrmGPCM(0.378812, step3, 1.7));
        double[] step4 = {0, 1.486566,-0.071229,1.614823};
        itemFormX.put(new VariableName("Item16"), new IrmGPCM(0.537706, step4, 1.7));
        double[] step5 = {0, 1.425413,2.630705,3.242696};
        itemFormX.put(new VariableName("Item17"), new IrmGPCM(0.554506, step5, 1.7));


        itemFormY.put(new VariableName("Item1"), new Irm3PL(0.887276,-1.334798,0.134406, 1.7));
        itemFormY.put(new VariableName("Item2"), new Irm3PL(1.184412,-1.129004,0.237765, 1.7));
        itemFormY.put(new VariableName("Item3"), new Irm3PL(0.609412,-1.464546,0.15139, 1.7));
        itemFormY.put(new VariableName("Item4"), new Irm3PL(0.923812,-0.576435,0.240097, 1.7));
        itemFormY.put(new VariableName("Item5"), new Irm3PL(0.822776,-0.476357,0.192369, 1.7));
        itemFormY.put(new VariableName("Item6"), new Irm3PL(0.707818,-0.235189,0.189557, 1.7));
        itemFormY.put(new VariableName("Item7"), new Irm3PL(1.306976,0.242986,0.165553, 1.7));
        itemFormY.put(new VariableName("Item8"), new Irm3PL(1.295471,0.598029,0.090557, 1.7));
        itemFormY.put(new VariableName("Item9"), new Irm3PL(1.366841,0.923206,0.172993, 1.7));
        itemFormY.put(new VariableName("Item10"), new Irm3PL(1.389624,1.380666,0.238008, 1.7));
        itemFormY.put(new VariableName("Item11"), new Irm3PL(0.293806,2.02807,0.203448, 1.7));
        itemFormY.put(new VariableName("Item12"), new Irm3PL(0.885347,3.152928,0.195473, 1.7));
        double[] step1y = {0, -1.387347,0.399117};
        itemFormY.put(new VariableName("Item13"), new IrmGPCM(0.346324, step1y, 1.7));
        double[] step2y = {0, 0.756514,0.956014};
        itemFormY.put(new VariableName("Item14"), new IrmGPCM(1.252012, step2y, 1.7));
        double[] step3y = {0, 0.975303,4.676299};
        itemFormY.put(new VariableName("Item15"), new IrmGPCM(0.392282, step3y, 1.7));
        double[] step4y = {0, 0.643405,-0.418869,0.804394};
        itemFormY.put(new VariableName("Item16"), new IrmGPCM(0.660841, step4y, 1.7));
        double[] step5y = {0, 0.641293,1.750488,2.53802};
        itemFormY.put(new VariableName("Item17"), new IrmGPCM(0.669612, step5y, 1.7));


        MeanMeanMethod meanMeanMethod = new MeanMeanMethod(itemFormX, itemFormY);
        meanMeanMethod.setPrecision(6);
        double B = meanMeanMethod.getIntercept();
        double A = meanMeanMethod.getScale();

//        System.out.println("Slope: " + A);
//        System.out.println("Intercept: " + B);

        assertEquals("Mean/mean slope test", 0.875095, A, 1e-6);
        assertEquals("Mean/mean intercept test", -0.543611, B, 1e-6);

    }

    @Test
    public void mixedFormatMeanMeanTestPARSCALE(){
        System.out.println("Mixed format mean/mean test: Parscale parameters");

        LinkedHashMap<VariableName, ItemResponseModel> itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("Item1"), new Irm3PL(0.751335,-0.897391, 0.244001, 1.7));
        itemFormX.put(new VariableName("Item2"), new Irm3PL(0.955947,-0.811477, 0.242883, 1.7));
        itemFormX.put(new VariableName("Item3"), new Irm3PL(0.497206,-0.858681, 0.260893, 1.7));
        itemFormX.put(new VariableName("Item4"), new Irm3PL(0.724000,-0.123911, 0.243497, 1.7));
        itemFormX.put(new VariableName("Item5"), new Irm3PL(0.865200, 0.205889, 0.319135, 1.7));
        itemFormX.put(new VariableName("Item6"), new Irm3PL(0.658129, 0.555228, 0.277826, 1.7));
        itemFormX.put(new VariableName("Item7"), new Irm3PL(1.082118, 0.950549, 0.157979, 1.7));
        itemFormX.put(new VariableName("Item8"), new Irm3PL(0.988294, 1.377501, 0.084828, 1.7));
        itemFormX.put(new VariableName("Item9"), new Irm3PL(1.248923, 1.614355, 0.181874, 1.7));
        itemFormX.put(new VariableName("Item10"), new Irm3PL(1.116682, 2.353932, 0.246856, 1.7));
        itemFormX.put(new VariableName("Item11"), new Irm3PL(0.438171, 3.217965, 0.309243, 1.7));
        itemFormX.put(new VariableName("Item12"), new Irm3PL(1.082206, 4.441864, 0.192339, 1.7));
        double[] step1 = {1.097268, -1.097268};
        itemFormX.put(new VariableName("Item13"), new IrmGPCM2(0.269994, 0.003998, step1, 1.7));
        double[] step2 = {0.106514, -0.106514};
        itemFormX.put(new VariableName("Item14"), new IrmGPCM2(0.972506, 1.632662, step2, 1.7));
        double[] step3 = {2.102301, -2.102301};
        itemFormX.put(new VariableName("Item15"), new IrmGPCM2(0.378812, 3.464657, step3, 1.7));
        double[] step4 = {-0.476513, 1.081282, -0.604770};
        itemFormX.put(new VariableName("Item16"), new IrmGPCM2(0.537706, 1.010053, step4, 1.7));
        double[] step5 = {1.00752, -0.197767, -0.809758};
        itemFormX.put(new VariableName("Item17"), new IrmGPCM2(0.554506, 2.432938, step5, 1.7));

        itemFormY.put(new VariableName("Item1"), new Irm3PL(0.887276,-1.334798,0.134406, 1.7));
        itemFormY.put(new VariableName("Item2"), new Irm3PL(1.184412,-1.129004,0.237765, 1.7));
        itemFormY.put(new VariableName("Item3"), new Irm3PL(0.609412,-1.464546,0.15139, 1.7));
        itemFormY.put(new VariableName("Item4"), new Irm3PL(0.923812,-0.576435,0.240097, 1.7));
        itemFormY.put(new VariableName("Item5"), new Irm3PL(0.822776,-0.476357,0.192369, 1.7));
        itemFormY.put(new VariableName("Item6"), new Irm3PL(0.707818,-0.235189,0.189557, 1.7));
        itemFormY.put(new VariableName("Item7"), new Irm3PL(1.306976,0.242986,0.165553, 1.7));
        itemFormY.put(new VariableName("Item8"), new Irm3PL(1.295471,0.598029,0.090557, 1.7));
        itemFormY.put(new VariableName("Item9"), new Irm3PL(1.366841,0.923206,0.172993, 1.7));
        itemFormY.put(new VariableName("Item10"), new Irm3PL(1.389624,1.380666,0.238008, 1.7));
        itemFormY.put(new VariableName("Item11"), new Irm3PL(0.293806,2.02807,0.203448, 1.7));
        itemFormY.put(new VariableName("Item12"), new Irm3PL(0.885347,3.152928,0.195473, 1.7));
        double[] step1y = {0.893232, -0.893232};
        itemFormY.put(new VariableName("Item13"), new IrmGPCM2(0.346324, -0.494115, step1y, 1.7));
        double[] step2y = {0.099750, -0.099750};
        itemFormY.put(new VariableName("Item14"), new IrmGPCM2(1.252012, 0.856264, step2y, 1.7));
        double[] step3y = {1.850498, -1.850498};
        itemFormY.put(new VariableName("Item15"), new IrmGPCM2(0.392282, 2.825801, step3y, 1.7));
        double[] step4y = {-0.300428, 0.761846, -0.461417};
        itemFormY.put(new VariableName("Item16"), new IrmGPCM2(0.660841, 0.342977, step4y, 1.7));
        double[] step5y = {1.001974, -0.107221, -0.894753};
        itemFormY.put(new VariableName("Item17"), new IrmGPCM2(0.669612, 1.643267, step5y, 1.7));

        MeanMeanMethod meanMeanMethod = new MeanMeanMethod(itemFormX, itemFormY);
        meanMeanMethod.setPrecision(6);
        double B = meanMeanMethod.getIntercept();
        double A = meanMeanMethod.getScale();

//        System.out.println("Slope: " + A);
//        System.out.println("Intercept: " + B);

        assertEquals("Mean/sigma slope test", 0.875095, A, 1e-6);
        assertEquals("Mean/sigma intercept test", -0.543611, B, 1e-6);




    }




}

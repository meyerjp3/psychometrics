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


import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimplePluginBandwidthTest {

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

    public SimplePluginBandwidthTest(){

    }

    @Test
    public void testValue() throws Exception {
        System.out.print("SimplePluginBandwidthTest: ");
        StandardDeviation sd = new StandardDeviation();
        for(int i=0;i<x.length;i++){
            sd.increment(x[i]);
        }
        SimplePluginBandwidth bw = new SimplePluginBandwidth(sd, 1.0);
        System.out.println(bw.value());
        assertEquals("Reference bandwidth test", 6.09603513572289, bw.value(), 1.0e-10);
    }

}

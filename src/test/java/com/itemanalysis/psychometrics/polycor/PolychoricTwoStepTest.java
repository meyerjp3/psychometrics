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
import static org.junit.Assert.assertTrue;

/**
 *
 * @author J. Patrick Meyer
 */
public class PolychoricTwoStepTest {

    public PolychoricTwoStepTest() {
        
    }

    @Test
    public void twoStepPolychoricCorrelation() throws Exception {
        PolychoricTwoStep twoStep = new PolychoricTwoStep();
        double[][] data = {{155,441,150,21}, {5,94,94,40}};
//        double[] expectedRow = {0.7290, 10};
//        double[] expectedCol = {-0.9945, 0.5101, 1.5464, 10};

        double[] expectedRow = {0.7290, 10};
        double[] expectedCol = {-0.9945, 0.5101, 1.5464, 10};

        twoStep.compute(data);
        double r = twoStep.value();
        System.out.println("Two step r: " + r);
        assertEquals("Two-step correlation:" , 0.5365251, r, 1E-6);

        double[] actualRow = twoStep.getRowThresholds();
        assertTrue("Row threshold array length", expectedRow.length==actualRow.length);

        for(int i=0;i<expectedRow.length;i++){
            assertEquals("Two-step row thresholds for row " + i , expectedRow[i], actualRow[i], 1E-4);
        }

        double[] actualCol = twoStep.getColumnThresholds();
        assertTrue("Col threshold array length", expectedCol.length==actualCol.length);

        for(int i=0;i<expectedRow.length;i++){
            assertEquals("Two-step col thresholds for col " + i , expectedCol[i], actualCol[i], 1E-4);
        }
        
    }



}
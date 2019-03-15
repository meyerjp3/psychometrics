package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.data.VariableName;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <p>Copyright 2016 J. Patrick Meyer</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at</p>
 * <p>http://www.apache.org/licenses/LICENSE-2.0</p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Deprecated
public class ConditionalItemResponseSummaryTest {


    @Test
    public void propTest(){

        ConditionalNumericItemResponseSummary cIRS = new ConditionalNumericItemResponseSummary(new VariableName("item1"));

        cIRS.increment(10, 1);
        cIRS.increment(10, 1);
        cIRS.increment(10, 0);

        cIRS.increment(12, 1);
        cIRS.increment(12, 0);

        cIRS.increment(20, 1);
        cIRS.increment(20, 1);
        cIRS.increment(20, 1);
        cIRS.increment(20, 0);

//        System.out.println(cIRS.getProportionAt(10, 1));
//        System.out.println(cIRS.getProportionAt(12, 1));
//        System.out.println(cIRS.getProportionAt(20, 1));
//
//        System.out.println(cIRS.mean());
//        System.out.println(cIRS.standardDeviation());
//        System.out.println(cIRS.correlation());
//        System.out.println(cIRS.getMeanAt(10));
//        System.out.println(cIRS.getMeanAt(12));
//        System.out.println(cIRS.getMeanAt(20));

        assertEquals("CIRS Test mean", 14.88888888888889, cIRS.mean(), 1e-15);
        assertEquals("CIRS Test SD", 4.910306620885412, cIRS.standardDeviation(), 1e-15);
        assertEquals("CIRS Test cor", 0.13576884666042605, cIRS.correlation(), 1e-15);

        assertEquals("CIRS Test Prop1", 0.66666666666666667, cIRS.getProportionAt(10, 1), 1e-15);
        assertEquals("CIRS Test Prop1", 0.5, cIRS.getProportionAt(12, 1), 1e-15);
        assertEquals("CIRS Test Prop1", 0.75, cIRS.getProportionAt(20, 1), 1e-15);

        assertEquals("CIRS Test Prop0", 0.333333333333333, cIRS.getProportionAt(10, 0), 1e-15);
        assertEquals("CIRS Test Prop0", 0.5, cIRS.getProportionAt(12, 0), 1e-15);
        assertEquals("CIRS Test Prop0", 0.25, cIRS.getProportionAt(20, 0), 1e-15);

        assertEquals("CIRS Test Mean1", 0.66666666666666667, cIRS.getMeanAt(10), 1e-15);
        assertEquals("CIRS Test Mean1", 0.5, cIRS.getMeanAt(12), 1e-15);
        assertEquals("CIRS Test Mean1", 0.75, cIRS.getMeanAt(20), 1e-15);

    }


}
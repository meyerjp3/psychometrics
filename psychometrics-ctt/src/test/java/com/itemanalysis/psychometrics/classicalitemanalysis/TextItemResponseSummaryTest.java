package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.data.VariableName;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright 2016 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Deprecated
public class TextItemResponseSummaryTest {


    @Test
    public void incrementLettersTest(){

        TextItemResponseSummary irs = new TextItemResponseSummary(new VariableName("item1"));

        irs.increment("A");
        irs.increment("A");
        irs.increment("A");
        irs.increment("A");

        irs.increment("B");
        irs.increment("B");

        irs.increment("C");
        irs.increment("C");
        irs.increment("C");
        irs.increment("C");
        irs.increment("C");
        irs.increment("C");

        assertEquals("Test freq response of A", 4, irs.getFrequencyAt("A"), 1e-15);
        assertEquals("Test freq response of B", 2, irs.getFrequencyAt("B"), 1e-15);
        assertEquals("Test freq response of C", 6, irs.getFrequencyAt("C"), 1e-15);

        assertEquals("Test prop response of A", 0.333333333333333, irs.getProportionAt("A"), 1e-15);
        assertEquals("Test prop response of B", 0.166666666666666, irs.getProportionAt("B"), 1e-15);
        assertEquals("Test prop response of C", 0.500000000000000, irs.getProportionAt("C"), 1e-15);

    }

    @Test
    public void weightedIncrementLettersTest(){

        TextItemResponseSummary irs = new TextItemResponseSummary(new VariableName("item1"));

        irs.increment("A", 4);
        irs.increment("B", 2);
        irs.increment("C", 6);

        assertEquals("Test freq response of A", 4, irs.getFrequencyAt("A"), 1e-15);
        assertEquals("Test freq response of B", 2, irs.getFrequencyAt("B"), 1e-15);
        assertEquals("Test freq response of C", 6, irs.getFrequencyAt("C"), 1e-15);

        assertEquals("Test prop response of A", 0.333333333333333, irs.getProportionAt("A"), 1e-15);
        assertEquals("Test prop response of B", 0.166666666666666, irs.getProportionAt("B"), 1e-15);
        assertEquals("Test prop response of C", 0.500000000000000, irs.getProportionAt("C"), 1e-15);
    }

    @Test
    public void responseScoreTest(){
        TextItemResponseSummary irs = new TextItemResponseSummary(new VariableName("item1"));
        irs.increment("A", 4);
        irs.increment("B", 2);
        irs.increment("C", 6);

        irs.setScoreAt("A", 1.0);
        irs.setScoreAt("B", 0);
        irs.setScoreAt("C", 0.0);

        assertEquals("Test scored mean", irs.getProportionAt("A"), irs.mean(), 1e-15);
        assertEquals("Test scored remainder mean", (irs.getProportionAt("B")+irs.getProportionAt("C")), 1.0-irs.mean(), 1e-15);

//        System.out.println(irs.toString());
//        System.out.println();
//        System.out.println(irs.getOutputString());
//        System.out.println(irs.getOutputStringAt("A"));
//        System.out.println(irs.getOutputStringAt("B"));
//        System.out.println(irs.getOutputStringAt("C"));
//        System.out.println(irs.getTotalFrequencyOutputString());

    }

    @Test
    public void itemNameTest(){
        TextItemResponseSummary irs = new TextItemResponseSummary(new VariableName("item1"));
        assertEquals("Name test", "item1", irs.getName().toString());

    }

}
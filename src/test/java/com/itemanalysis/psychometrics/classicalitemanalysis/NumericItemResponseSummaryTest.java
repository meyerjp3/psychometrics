package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.data.VariableName;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <p>Copyright 2016 J. Patrick Meyer</p>
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at</p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0</p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.</p>
 */
@Deprecated
public class NumericItemResponseSummaryTest {

    @Test
    public void incrementNumbersTest(){

        NumericItemResponseSummary irs = new NumericItemResponseSummary(new VariableName("item1"));

        irs.increment(1);
        irs.increment(1);
        irs.increment(1);
        irs.increment(1);

        irs.increment(2);
        irs.increment(2);

        irs.increment(3);
        irs.increment(3);
        irs.increment(3);
        irs.increment(3);
        irs.increment(3);
        irs.increment(3);

        assertEquals("Test freq response of 1", 4, irs.getFrequencyAt(1.0), 1e-15);
        assertEquals("Test freq response of 2", 2, irs.getFrequencyAt(2), 1e-15);
        assertEquals("Test freq response of 3", 6, irs.getFrequencyAt(3.0), 1e-15);

        assertEquals("Test prop response of 1", 0.333333333333333, irs.getProportionAt(1), 1e-15);
        assertEquals("Test prop response of 2", 0.166666666666666, irs.getProportionAt(2.00), 1e-15);
        assertEquals("Test prop response of 3", 0.500000000000000, irs.getProportionAt(3), 1e-15);

//        System.out.println(irs.toString());

    }

    @Test
    public void weightedItemStatisticsTest(){
        NumericItemResponseSummary irs = new NumericItemResponseSummary(new VariableName("item1"));

        irs.increment(1, 6);
        irs.increment(0, 4);

        assertEquals("Test of sample mean", 0.6, irs.getMean(), 1e-15);
        assertEquals("Test of sample variance", 0.2666666666666667, irs.getVariance(), 1e-14);
        assertEquals("Test of sample mean", 0.5163977794943223, irs.getStandardDeviation(), 1e-15);
    }

    @Test
    public void weightedIncrementNumbersTest(){

        NumericItemResponseSummary irs = new NumericItemResponseSummary(new VariableName("item1"));

        irs.increment(1, 4);
        irs.increment(2, 2);
        irs.increment(3, 6);

        assertEquals("Test freq response of 1", 4, irs.getFrequencyAt(1.0), 1e-15);
        assertEquals("Test freq response of 2", 2, irs.getFrequencyAt(2), 1e-15);
        assertEquals("Test freq response of 3", 6, irs.getFrequencyAt(3.0), 1e-15);

        assertEquals("Test prop response of 1", 0.333333333333333, irs.getProportionAt(1), 1e-15);
        assertEquals("Test prop response of 2", 0.166666666666666, irs.getProportionAt(2.00), 1e-15);
        assertEquals("Test prop response of 3", 0.500000000000000, irs.getProportionAt(3), 1e-15);

//        System.out.println(irs.toString());

    }

    @Test
    public void itemStatisticsTest(){
        NumericItemResponseSummary irs = new NumericItemResponseSummary(new VariableName("item1"));

        irs.increment(1);
        irs.increment(1);
        irs.increment(1);
        irs.increment(1);
        irs.increment(1);
        irs.increment(1);

        irs.increment(0);
        irs.increment(0);
        irs.increment(0);
        irs.increment(0);

        assertEquals("Test of sample mean", 0.6, irs.getMean(), 1e-15);
        assertEquals("Test of sample variance", 0.2666666666666667, irs.getVariance(), 1e-15);
        assertEquals("Test of sample mean", 0.5163977794943223, irs.getStandardDeviation(), 1e-15);

//        System.out.println(irs.toString());
    }

    @Test
    public void itemStatisticsTest2(){
        NumericItemResponseSummary irs = new NumericItemResponseSummary(new VariableName("item1"));

        irs.increment(1);
        irs.increment(2.5);
        irs.increment(10.79);
        irs.increment(-1.09);
        irs.increment(2.089);
        irs.increment(-0.009);

        assertEquals("Test of sample mean", 2.546666666666667, irs.getMean(), 1e-15);
        assertEquals("Test of sample variance", 18.06262706666666, irs.getVariance(), 1e-14);
        assertEquals("Test of sample mean", 4.250014948993317, irs.getStandardDeviation(), 1e-15);

//        System.out.println(irs.toString());
    }

}
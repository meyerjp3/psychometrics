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
package com.itemanalysis.psychometrics.statistics;

import org.apache.commons.math3.stat.Frequency;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class FrequencyTest {

    public FrequencyTest() {
    }

    /**
     * Test of toString method, of class Frequency.
     */
    @Test
    public void testToString() {
        String[] data = {"a", "a", "a", "a", "a", "a", "b", "b", "b", "c", "c"};
        long[] expected = {6, 3, 2};
        
        Frequency f = new Frequency();
        for(int i=0;i<data.length;i++){
            f.addValue(data[i]);
        }

        Iterator<Comparable<?>> iter = f.valuesIterator();
        int index = 0;
        while(iter.hasNext()){
            assertEquals("[row " + index + "]", expected[index], f.getCount(iter.next()));
            index++;
        }
    }

   
}
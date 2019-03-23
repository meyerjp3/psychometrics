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

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class TwoWayTableTest {

    public TwoWayTableTest() {
    }

    @Test
    public void twoWayTest(){
        String[] row = {"A", "B"};
        String[][] data = {
            {"a", "b", "c", "a", "a", "a", "b", "b", "b", "c", "c", "c"},
            {"a", "a", "a", "b", "b", "b", "b", "b", "b", "c", "c", "c"}};

        long[][] actualCell = {
            {4,4,4},
            {3,6,3}
        };

        long[] actualRow = {12,12};
        long[] actualCol = {7,10,7};

        TwoWayTable table = new TwoWayTable();
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[0].length;j++){
                table.addValue(row[i], data[i][j]);
            }
        }

        //check cell counts
        Iterator<Comparable<?>> rowIter = table.rowValuesIterator();
        Iterator<Comparable<?>> colIter = null;
        Comparable<?> rv = null;
        Comparable<?> cv = null;
        int rIndex = 0;
        int cIndex = 0;
        while(rowIter.hasNext()){
            rv = rowIter.next();
            cIndex=0;
            colIter = table.colValuesIterator();
            while(colIter.hasNext()){
                cv = colIter.next();
                assertEquals("Cell [" + rIndex + ", " + cIndex + " ]",
                        actualCell[rIndex][cIndex], table.getCount(rv, cv));
                cIndex++;
            }
            rIndex++;
        }

        //check row margin counts
        rowIter = table.rowValuesIterator();
        rIndex = 0;
        while(rowIter.hasNext()){
            rv = rowIter.next();
            assertEquals("Row [" + rv.toString() + "]: ", actualRow[rIndex], table.getRowCount(rv));
            rIndex++;
        }

        //check col margin counts
        colIter = table.colValuesIterator();
        cIndex=0;
        while(colIter.hasNext()){
            cv = colIter.next();
            assertEquals("Col [" + cv.toString() + "]: ", actualCol[cIndex], table.getColCount(cv));
            cIndex++;
        }

    }

}
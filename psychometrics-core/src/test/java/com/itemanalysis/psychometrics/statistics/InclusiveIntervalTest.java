package com.itemanalysis.psychometrics.statistics;

import org.junit.Test;

import static org.junit.Assert.*;

public class InclusiveIntervalTest {

    @Test
    public void intervalIncludesTest(){
        InclusiveInterval interval = new InclusiveInterval(-1, 1);

        assertTrue(interval.includes(-1));
        assertTrue(interval.includes(-0.5));
        assertTrue(interval.includes(0));
        assertTrue(interval.includes(0.5));
        assertTrue(interval.includes(1));

        assertFalse(interval.includes(-1.00000001));
        assertFalse(interval.includes(1.00000001));
        assertFalse(interval.includes(10));
        assertFalse(interval.includes(-10));
    }

    @Test
    public void intervalExcludesTest(){
        InclusiveInterval interval = new InclusiveInterval(1, 10);

        assertTrue(interval.excludes(-1));
        assertTrue(interval.excludes(-0.5));
        assertTrue(interval.excludes(0));
        assertTrue(interval.excludes(11));
        assertTrue(interval.excludes(10.00001));
    }

}
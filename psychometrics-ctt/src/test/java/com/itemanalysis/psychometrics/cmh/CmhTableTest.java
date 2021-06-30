package com.itemanalysis.psychometrics.cmh;

import org.junit.Test;

import static org.junit.Assert.*;

public class CmhTableTest {


    @Test
    public void test1(){
        CmhTable table = new CmhTable("F", "M");
        table.increment("F", 1.0, 10);
        table.increment("F", 0.0, 20);
        table.increment("M", 1.0, 30);
        table.increment("M", 0.0, 40);

        assertEquals("CMH Table focal right mismatch.", table.focalRight(), 10, 1e-15);
        assertEquals("CMH Table focal wrong mismatch.", table.focalWrong(), 20, 1e-15);
        assertEquals("CMH Table reference right mismatch.", table.referenceRight(), 30, 1e-15);
        assertEquals("CMH Table reference wrong mismatch.", table.referenceWrong(), 40, 1e-15);
    }



}
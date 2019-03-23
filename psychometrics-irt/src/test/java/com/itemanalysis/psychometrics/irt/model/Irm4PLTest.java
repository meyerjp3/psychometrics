package com.itemanalysis.psychometrics.irt.model;

import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class Irm4PLTest {


    @Test
    public void probTest1(){
        System.out.println("4PL Model Test");
        Irm4PL irm = new Irm4PL(1.0, 0.0, 0.15, 0.95, 1.7);

        assertEquals("Theta = -2", 0.1758364, irm.probability(-2.0, 1), 1e-5);
        assertEquals("Theta = -1", 0.2735722, irm.probability(-1.0, 1), 1e-5);
        assertEquals("Theta =  0", 0.5500000, irm.probability( 0.0, 1), 1e-5);
        System.out.println(irm.probability( 0.0, 1));
        assertEquals("Theta =  1", 0.8264278, irm.probability(1.0, 1), 1e-5);
        assertEquals("Theta =  2", 0.9241636, irm.probability( 2.0, 1), 1e-5);
    }

    @Test
    public void gradientTest(){
        System.out.println("4PL Model Test");
        Irm4PL irm = new Irm4PL(1.0, 0.0, 0.15, 0.95, 1.7);

        double[] g = irm.gradient(0.0, 1);
//        System.out.println(Arrays.toString(g));

        assertEquals("Derivative a", 0.0, g[0], 1e-5);
        assertEquals("Derivative b", -0.34, g[1], 1e-5);
        assertEquals("Derivative c",  0.50, g[2], 1e-5);
        assertEquals("Derivative x",  0.50, g[3], 1e-5);

        g = irm.gradient(1.0, 0);
//        System.out.println(Arrays.toString(g));

        assertEquals("Derivative a", -0.1776238, g[0], 1e-5);
        assertEquals("Derivative b",  0.1776238, g[1], 1e-5);
        assertEquals("Derivative c", -0.1544653, g[2], 1e-5);
        assertEquals("Derivative x", -0.8455347, g[3], 1e-5);

        g = irm.gradient(1.0, 1);
//        System.out.println(Arrays.toString(g));

        assertEquals("Derivative a",  0.1776238, g[0], 1e-5);
        assertEquals("Derivative b", -0.1776238, g[1], 1e-5);
        assertEquals("Derivative c",  0.1544653, g[2], 1e-5);
        assertEquals("Derivative x",  0.8455347, g[3], 1e-5);

        double[] iparam = {1.5, -1.0, 0.10, 0.9};
        g = irm.gradient(0.0, iparam, 1, 1.7);
//        System.out.println(Arrays.toString(g));

        assertEquals("Derivative a",  0.09136601, g[0], 1e-5);
        assertEquals("Derivative b", -0.13704901, g[1], 1e-5);
        assertEquals("Derivative c",  0.07242649, g[2], 1e-5);
        assertEquals("Derivative x",  0.92757351, g[3], 1e-5);


    }

}
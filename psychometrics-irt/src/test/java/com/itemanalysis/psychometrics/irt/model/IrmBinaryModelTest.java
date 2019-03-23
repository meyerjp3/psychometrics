package com.itemanalysis.psychometrics.irt.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class IrmBinaryModelTest {


    @Test
    public void gradientTest(){

        System.out.println("4PL Gradient");
        IrmBinaryModel irm4 = new IrmBinaryModel(0.8, -1.0, 0.2, 0.95);
        System.out.println(Arrays.toString(irm4.gradient(0, 1)));

        Irm4PL pl4 = new Irm4PL(0.8, -1.0, 0.2, 0.95, 1.0);
        System.out.println(Arrays.toString(pl4.gradient(0, 1)));

        System.out.println();

        System.out.println("3PL Gradient");
        IrmBinaryModel irm3 = new IrmBinaryModel(0.8, -1.0, 0.2);
        System.out.println(Arrays.toString(irm3.gradient(0, 1)));

        Irm3PL pl3 = new Irm3PL(0.8, -1.0, 0.2, 1.0);
        System.out.println(Arrays.toString(pl3.gradient(0, 1)));

        System.out.println();

        System.out.println("2PL Gradient");
        IrmBinaryModel irm2 = new IrmBinaryModel(0.8, -1.0);
        System.out.println(Arrays.toString(irm2.gradient(0, 1)));

        Irm3PL pl2 = new Irm3PL(0.8, -1.0, 1.0);
        System.out.println(Arrays.toString(pl2.gradient(0, 1)));

        System.out.println();

        System.out.println("1PL Gradient");
        IrmBinaryModel irm1 = new IrmBinaryModel(-1.0);
        System.out.println(Arrays.toString(irm1.gradient(0, 1)));

        Irm3PL pl1 = new Irm3PL(-1.0, 1.0);
        System.out.println(Arrays.toString(pl1.gradient(0, 1)));

    }

}
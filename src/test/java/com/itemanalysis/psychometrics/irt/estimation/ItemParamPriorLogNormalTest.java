package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.distribution.UniformDistributionApproximation;
import org.junit.Test;

import static org.junit.Assert.*;

public class ItemParamPriorLogNormalTest {

//    @Test
    public void logDensityTest(){
        System.out.println("ItemParamPriorLogNormal: log density test");

        UniformDistributionApproximation uniform = new UniformDistributionApproximation(-6, 6, 50);
        ItemParamPriorLogNormal dlnorm = new ItemParamPriorLogNormal(0.0, 0.5);


        double[] x = uniform.getPoints();

        for(int i=0;i<x.length;i++){
            System.out.println(dlnorm.logDensity(x[i]));
        }


    }


}
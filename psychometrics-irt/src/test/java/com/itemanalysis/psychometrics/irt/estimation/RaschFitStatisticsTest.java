package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.quadrature.NormalQuadratureRule;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import org.junit.Test;

import static org.junit.Assert.*;

public class RaschFitStatisticsTest {

    @Test
    public void informationTest(){
        System.out.println("Testing item informaiton in RaschFitstatistics");
        Irm3PL rasch = new Irm3PL(0.0, 1.0);
        RaschFitStatistics fit = new RaschFitStatistics();

        NormalQuadratureRule norm = new NormalQuadratureRule(-4.0, 4.0, 150);
        double[] theta = norm.getPoints();

        for(int i=0;i<theta.length;i++){
            double v1 = fit.varianceOfResponse(rasch, theta[i]);
            double v2 = rasch.itemInformationAt(theta[i]);
//            System.out.println(v1 + " = " + v2 + ": " + (Math.abs(v1-v2)<1e-15));
            assertEquals("Testing information", v2, v1, 1e-15);
        }

    }


}
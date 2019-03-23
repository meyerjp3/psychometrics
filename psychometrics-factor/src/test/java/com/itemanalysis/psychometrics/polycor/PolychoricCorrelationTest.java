package com.itemanalysis.psychometrics.polycor;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

public class PolychoricCorrelationTest {

    /**
     * Test of rho method, of class PolychoricML.
     * True values from R using
     * library(polycor)
     * set.seed(12345)
     * data <- rmvnorm(1000, c(0, 0), matrix(c(1, .5, .5, 1), 2, 2))
     * x <- data[,1]
     * y <- data[,2]
     * cor(x, y)  # sample correlation
     * x <- cut(x, c(-Inf, .75, Inf))
     * y <- cut(y, c(-Inf, -1, .5, 1.5, Inf))
     * pc<-polychor(x, y)  # 2-step estimate
     * polychor(x, y, ML=TRUE, std.err=TRUE)  # ML estimate
     */
    @Test
    public void maximumLikelihoodTest1() throws Exception {
        System.out.println("Polychoric maximum likelihood test");
        double[][] data = {{155,437,162,21}, {7,88,84,46}};
        double[] expectedRowThresh = {0.7537};
        double[] expectedColThresh = {-0.9842, 0.4841, 1.5010};
        double expectedRowThreshSe = 0.04368;
        double[] expectedColthreshSe = {0.04767, 0.04145, 0.06265};
        double expectedRho = 0.5364;
        double expectedRhoSe = 0.03775;

        PolychoricMaximumLikelihood polycor = new PolychoricMaximumLikelihood(data);
        double rho = polycor.value();
        double[] rowThresholds = polycor.getRowThresholds();
        double[] colThresholds = polycor.getColumnThresholds();

        System.out.println(polycor.print());

        assertEquals("Testing rho: ", expectedRho, rho, 1E-1);

        for(int i=0;i<rowThresholds.length;i++){
            assertEquals("Testing row thresholds: ", expectedRowThresh[i], rowThresholds[i], 1);
        }

        for(int i=0;i<colThresholds.length;i++){
            assertEquals("Testing column thresholds: ", expectedColThresh[i], colThresholds[i], 1);
        }

        //TODO test thresholds
    }

    /**
     * Test of rho method, of class PolychoricML.
     * True values from R using
     * library(polycor)
     * set.seed(12345)
     * data <- rmvnorm(1000, c(0, 0), matrix(c(1, .5, .5, 1), 2, 2))
     * x <- data[,1]
     * y <- data[,2]
     * cor(x, y)  # sample correlation
     * x <- cut(x, c(-Inf, .75, Inf))
     * y <- cut(y, c(-Inf, -1, .5, 1.5, Inf))
     * pc<-polychor(x, y)  # 2-step estimate
     * polychor(x, y, ML=TRUE, std.err=TRUE)  # ML estimate
     */
    @Test
    public void twoStepTest1() throws Exception {
        System.out.println("Polychoric two-step test");
        double[][] data = {{155,437,162,21}, {7,88,84,46}};
        double[] expectedRowThresh = {0.7537, 10};
        double[] expectedColThresh = {-0.9842, 0.4841, 1.5010, 10};

        double expectedRhoTwoStep = 0.5230482;

        PolychoricTwoStep polycor = new PolychoricTwoStep(data);
        double rho = polycor.value();

        System.out.println(polycor.print());

        assertEquals("Testing rho: ", expectedRhoTwoStep, rho, 1E-3);
    }

    @Test
    public void incrementalMaxLikelihoodTest(){
        System.out.println("Incremental polychoric maximum likelihood test");
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/polychoric-data.txt"));
        PolychoricMaximumLikelihood polycor = new PolychoricMaximumLikelihood();

        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line = "";
            String[] s = null;
            int x = 0;
            int y = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                x = Integer.parseInt(s[0]);
                y = Integer.parseInt(s[1]);
                polycor.increment(x, y);
            }
        }catch(IOException ex) {
            ex.printStackTrace();
        }

        double[] expectedRowThresh = {0.7537};
        double[] expectedColThresh = {-0.9842, 0.4841, 1.5010};
        double expectedRowThreshSe = 0.04368;
        double[] expectedColthreshSe = {0.04767, 0.04145, 0.06265};
        double expectedRho = 0.5364;
        double expectedRhoSe = 0.03775;

        double rho = polycor.value();
        double[] rowThresholds = polycor.getRowThresholds();
        double[] colThresholds = polycor.getColumnThresholds();

        System.out.println(polycor.print());

        assertEquals("Testing rho: ", expectedRho, rho, 1E-1);

        for(int i=0;i<rowThresholds.length;i++){
            assertEquals("Testing row thresholds: ", expectedRowThresh[i], rowThresholds[i], 1);
        }

        for(int i=0;i<colThresholds.length;i++){
            assertEquals("Testing column thresholds: ", expectedColThresh[i], colThresholds[i], 1);
        }

    }

    @Test
    public void incrementalTwoStepTest(){
        System.out.println("Incremental polychoric two-step test");
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/polychoric-data.txt"));
        PolychoricMaximumLikelihood polycor = new PolychoricMaximumLikelihood();

        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line = "";
            String[] s = null;
            int x = 0;
            int y = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                x = Integer.parseInt(s[0]);
                y = Integer.parseInt(s[1]);
                polycor.increment(x, y);
            }
        }catch(IOException ex) {
            ex.printStackTrace();
        }

        double[] expectedRowThresh = {0.7537, 10};
        double[] expectedColThresh = {-0.9842, 0.4841, 1.5010, 10};

        double expectedRhoTwoStep = 0.5230482;
        double rho = polycor.value();

        System.out.println(polycor.print());

        assertEquals("Testing rho: ", expectedRhoTwoStep, rho, 1E-3);

    }

//    @Test
    public void emptyCasesTest1() {
	    	double[][] caseMatrix = new double[2][2];
	
				caseMatrix[0][0] = 0;
				caseMatrix[0][1] = 1;
				caseMatrix[1][0] = 0;
				caseMatrix[1][1] = 16;
	
	    	new PolychoricTwoStep(caseMatrix);
    }
    
//    @Test
    public void emptyCasesTest2() {
	    	double[][] caseMatrix = new double[2][2];
	
				caseMatrix[0][0] = 0;
				caseMatrix[0][1] = 0;
				caseMatrix[1][0] = 0;
				caseMatrix[1][1] = 16;
	
	    	new PolychoricTwoStep(caseMatrix);
    }
    
//    @Test
    public void emptyCasesTest3() {
	    	double[][] caseMatrix = new double[2][2];
	
				caseMatrix[0][0] = 0;
				caseMatrix[0][1] = 0;
				caseMatrix[1][0] = 1;
				caseMatrix[1][1] = 16;
	
				new PolychoricTwoStep(caseMatrix);
    }
    
//    @Test
    public void emptyCasesTest4() {
	    	double[][] caseMatrix = new double[2][2];
	
				caseMatrix[0][0] = 1;
				caseMatrix[0][1] = 0;
				caseMatrix[1][0] = 0;
				caseMatrix[1][1] = 16;
	
				new PolychoricTwoStep(caseMatrix);
    }
}

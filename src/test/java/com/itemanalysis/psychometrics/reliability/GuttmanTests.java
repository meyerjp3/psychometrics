package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.statistics.StreamingCovarianceMatrix;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

public class GuttmanTests {

    /**
     * Covariance matrix and "true" results come from Brennan and Feldt's chapter in the 1989
     * edition of the book "Educational Measurement."
     *
     * True values obtained from the guttman() function in the psych package of R.
     * Note that the guttman function conducts the computations on the correlation matrix, not
     * the covariance matrix.
     *
     */
    @Test
    public void guttmanTestFeldtBrennanData(){
        System.out.println("Guttman Reliability test with Brennan-Feldt Data");
//        double[][] cov = {
//                {7.2, 5.6, 3.5, 5.8},
//                {5.6, 6.4, 3.1, 4.1},
//                {3.5, 3.1, 2.4, 3.3},
//                {5.8, 4.1, 3.3, 6.2}
//        };

        double[][] cor = {
                {1.0000000, 0.8249579, 0.8419691, 0.8680923},
                {0.8249579, 1.0000000, 0.7909811, 0.6508757},
                {0.8419691, 0.7909811, 1.0000000, 0.8554851},
                {0.8680923, 0.6508757, 0.8554851, 1.0000000}
        };

        GuttmanLambda1 L1 = new GuttmanLambda1(cor);
        GuttmanLambda2 L2 = new GuttmanLambda2(cor);
        GuttmanLambda3 L3 = new GuttmanLambda3(cor);//Coefficient alpha
        GuttmanLambda4 L4 = new GuttmanLambda4(cor);//split-half
        GuttmanLambda5 L5 = new GuttmanLambda5(cor);
        GuttmanLambda6 L6 = new GuttmanLambda6(cor);


        System.out.println("  L1 = " + L1.value());
        System.out.println("  L2 = " + L2.value());
        System.out.println("  L3 = " + L3.value());
        System.out.println("  L4 = " + L4.value());
        System.out.println("  L5 = " + L5.value());
        System.out.println("  L6 = " + L6.value());

        //True values from Brennan and Feldt's book chapter
        assertEquals("  L1 test", 0.7072754, L1.value(), 1e-2);
        assertEquals("  L2 test", 0.9440085, L2.value(), 1e-2);
        assertEquals("  L3 test", 0.9430339, L3.value(), 1e-2);
        //assertEquals("  L4 test", 0.9775585, L4.value(), 1e-2);//Not tested because psych function tries a variety of split-halves and picks the best one
        assertEquals("  L5 test", 0.9215376, L5.value(), 1e-2);
        assertEquals("  L6 test", 0.9499394, L6.value(), 1e-2);

    }

    /**
     * Uses first 10 items from the exam1 data file. Compares results to SPSS.
     * SPSS uses the covariance matrix in its calculations.
     *
     * RELIABILITY
     * /VARIABLES=item1 item2 item3 item4 item5 item6 item7 item8 item9 item10
     * /SCALE('ALL VARIABLES') ALL
     * /MODEL=GUTTMAN.
     */
    @Test
    public void exam1Test(){
        System.out.println("Guttman test for 10 items from exam1 data");
        int nItems = 10;
        StreamingCovarianceMatrix covarianceMatrix = new StreamingCovarianceMatrix(nItems);

        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/exam1-items-scored.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double v1 = 0;
            double v2 = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int i=0;i<nItems;i++){
                    v1 = Double.parseDouble(s[i]);
                    for(int j=0;j<nItems;j++){
                        v2 = Double.parseDouble(s[j]);
                        covarianceMatrix.increment(i, j, v1, v2);
                    }
                }
            }
            br.close();

            double[][] cov = covarianceMatrix.value();

            GuttmanLambda1 L1 = new GuttmanLambda1(cov);
            GuttmanLambda2 L2 = new GuttmanLambda2(cov);
            GuttmanLambda3 L3 = new GuttmanLambda3(cov);
            GuttmanLambda4 L4 = new GuttmanLambda4(cov);
            GuttmanLambda5 L5 = new GuttmanLambda5(cov);
            GuttmanLambda6 L6 = new GuttmanLambda6(cov);

            System.out.println("  L1 = " + L1.value());
            System.out.println("  L2 = " + L2.value());
            System.out.println("  L3 = " + L3.value());
            System.out.println("  L4 = " + L4.value());
            System.out.println("  L5 = " + L5.value());
            System.out.println("  L6 = " + L6.value());

            //True results from SPSS
            assertEquals("  L1 test", 0.582046, L1.value(), 1e-6);
            assertEquals("  L2 test", 0.648079, L2.value(), 1e-6);
            assertEquals("  L3 test", 0.646718, L3.value(), 1e-6);
            assertEquals("  L4 test", 0.630416, L4.value(), 1e-6);
            assertEquals("  L5 test", 0.628291, L5.value(), 1e-6);
            assertEquals("  L6 test", 0.627509, L6.value(), 1e-6);

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    /**
     * Uses first 4 items from the exam1 data file. Compares results to SPSS.
     * SPSS uses the covariance matrix in its calculations.
     *
     * RELIABILITY
     * /VARIABLES=item1 item2 item3 item4 item5 item6 item7 item8 item9 item10
     * /SCALE('ALL VARIABLES') ALL
     * /MODEL=GUTTMAN.
     */
    @Test
    public void exam1ItemDeletedTest(){
        System.out.println("Guttman item deleted test for 4 items from exam1 data");
        int nItems = 4;
        StreamingCovarianceMatrix covarianceMatrix = new StreamingCovarianceMatrix(nItems);

        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/exam1-items-scored.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double v1 = 0;
            double v2 = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int i=0;i<nItems;i++){
                    v1 = Double.parseDouble(s[i]);
                    for(int j=0;j<nItems;j++){
                        v2 = Double.parseDouble(s[j]);
                        covarianceMatrix.increment(i, j, v1, v2);
                    }
                }
            }
            br.close();

            double[][] cov = covarianceMatrix.value();

            GuttmanLambda1 L1 = new GuttmanLambda1(cov);
            GuttmanLambda2 L2 = new GuttmanLambda2(cov);
            GuttmanLambda3 L3 = new GuttmanLambda3(cov);
            GuttmanLambda4 L4 = new GuttmanLambda4(cov);
            GuttmanLambda5 L5 = new GuttmanLambda5(cov);
            GuttmanLambda6 L6 = new GuttmanLambda6(cov);

            double[][] trueValue = {
                    {0.217323,0.248745,0.187663,0.251231},//L1
                    {0.327486,0.374182,0.281987,0.378470},//L2
                    {0.325985,0.373117,0.281495,0.376847},//L3
                    {0.260159,0.300953,0.252273,0.371907},//L4
                    {0.330716,0.372976,0.281516,0.382857},//L5
                    {0.247349,0.287155,0.207672,0.291736},//L6
            };

            double[] d1 = L1.itemDeletedReliability();
            for(int i=0;i<4;i++){
                assertEquals("  L1 test " + (i+1), trueValue[0][i], d1[i], 1e-6);
            }

            double[] d2 = L2.itemDeletedReliability();
            for(int i=0;i<4;i++){
                assertEquals("  L2 test " + (i+1), trueValue[1][i], d2[i], 1e-6);
            }

            double[] d3 = L3.itemDeletedReliability();
            for(int i=0;i<4;i++){
                assertEquals("  L3 test " + (i+1), trueValue[2][i], d3[i], 1e-6);
            }

            double[] d4 = L4.itemDeletedReliability();
            for(int i=0;i<4;i++){
                assertEquals("  L4 test " + (i+1), trueValue[3][i], d4[i], 1e-6);
            }

            double[] d5 = L5.itemDeletedReliability();
            for(int i=0;i<4;i++){
                assertEquals("  L5 test " + (i+1), trueValue[4][i], d5[i], 1e-6);
            }

            double[] d6 = L6.itemDeletedReliability();
            for(int i=0;i<4;i++){
                assertEquals("  L6 test " + (i+1), trueValue[5][i], d6[i], 1e-6);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void guttman4OddTest(){
        System.out.println("Guttman 4 odd length test");
        int nItems = 3;
        StreamingCovarianceMatrix covarianceMatrix = new StreamingCovarianceMatrix(nItems);

        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/exam1-items-scored.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double v1 = 0;
            double v2 = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int i=0;i<nItems;i++){
                    v1 = Double.parseDouble(s[i]);
                    for(int j=0;j<nItems;j++){
                        v2 = Double.parseDouble(s[j]);
                        covarianceMatrix.increment(i, j, v1, v2);
                    }
                }
            }
            br.close();

            double[][] cov = covarianceMatrix.value();
            GuttmanLambda4 L4 = new GuttmanLambda4(cov);

            assertEquals("  L4 test odd test length", 0.371907, L4.value(), 1e-6);//True value from SPSS


        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

}
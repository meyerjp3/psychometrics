/*
 * Copyright 2018 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.distribution;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class KernelDensityTest {

    @Test
    public void densityTest1(){
        System.out.println("Kernel density test 1");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[0]);
                d1[row] = Double.parseDouble(s[1]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x);
            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-7);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void densityTest2(){
        System.out.println("Kernel density test 2");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[1]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[2]);
                d1[row] = Double.parseDouble(s[3]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x);
            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-7);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void densityTest3(){
        System.out.println("Kernel density test 3");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[2]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[4]);
                d1[row] = Double.parseDouble(s[5]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x);
//            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-7);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void densityTest4(){
        System.out.println("Kernel density test with Epanechnikov kernel");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[0]);
                d1[row] = Double.parseDouble(s[6]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x, KernelType.EPANECHNIKOV, 1, 512);
            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-6);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void densityTest5(){
        System.out.println("Kernel density test with Rectangular kernel");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[0]);
                d1[row] = Double.parseDouble(s[7]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x, KernelType.RECTANGULAR, 1, 512);
            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-6);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void densityTest6(){
        System.out.println("Kernel density test with Triangular kernel");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[0]);
                d1[row] = Double.parseDouble(s[8]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x, KernelType.TRIANGULAR, 1, 512);
            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-6);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void densityTest7(){
        System.out.println("Kernel density test with Biweight kernel");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[0]);
                d1[row] = Double.parseDouble(s[9]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x, KernelType.BIWEIGHT, 1, 512);
            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-6);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void densityTest8(){
        System.out.println("Kernel density test with Cosine kernel");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[0]);
                d1[row] = Double.parseDouble(s[10]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x, KernelType.COSINE, 1, 512);
            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-6);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void densityTest9(){
        System.out.println("Kernel density test with Optcosine kernel");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();

            f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-results.csv"));
            br = new BufferedReader(new FileReader(f));
            double[] p1 = new double[512];
            double[] d1 = new double[512];
            row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                p1[row] = Double.parseDouble(s[0]);
                d1[row] = Double.parseDouble(s[11]);
                row++;
            }
            br.close();

            KernelDensity density = new KernelDensity(x, KernelType.OPTCOSINE, 1, 512);
            System.out.println(density.toString());

            for(int i=0;i<512;i++){
                Assert.assertEquals("  Density at point " + (i+1), d1[i], density.pdf(p1[i]), 1e-6);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void testPoints10(){
        System.out.println("Density testing 10 points");

        double[] fromR = {
                0.000000000 ,  1.473684211,  2.947368421,   4.421052632,   5.894736842,   7.368421053,   8.842105263,
                10.315789474,  11.789473684,  13.263157895,  14.736842105, -13.263157895, -11.789473684, -10.315789474,
                -8.842105263,  -7.368421053,  -5.894736842,  -4.421052632,  -2.947368421,  -1.473684211};

        double up = 7;
        double lo = -7;
        int nPoints = 10;

        double step = (2*(up-lo))/(2*nPoints-1);
        double[] p = new double[2*nPoints];

        for(int i=0;i<2*nPoints;i++){
            p[i] = step*i;
        }

        for(int i=nPoints+1; i<2*nPoints; i++){
            p[i] = -p[2*nPoints-i];
        }

        for(int i=0;i<p.length;i++){
            Assert.assertEquals("  Point at " + (i+1), fromR[i], p[i], 1e-9);
        }
    }

    @Test
    public void testPoints50(){
        System.out.println("Density testing 50 points");

        double[] fromR = {
                0.0000000000,   0.2828282828,   0.5656565657,   0.8484848485,   1.1313131313,   1.4141414141,   1.6969696970,
                1.9797979798,   2.2626262626,   2.5454545455,   2.8282828283,   3.1111111111,   3.3939393939,   3.6767676768,
                3.9595959596,   4.2424242424,   4.5252525253,   4.8080808081,   5.0909090909,   5.3737373737,   5.6565656566,
                5.9393939394,   6.2222222222,   6.5050505051,   6.7878787879,   7.0707070707,   7.3535353535,   7.6363636364,
                7.9191919192,   8.2020202020,   8.4848484848,   8.7676767677,   9.0505050505,   9.3333333333,   9.6161616162,
                9.8989898990,  10.1818181818,  10.4646464646,  10.7474747475,  11.0303030303,  11.3131313131,  11.5959595960,
                11.8787878788,  12.1616161616,  12.4444444444,  12.7272727273,  13.0101010101,  13.2929292929,  13.5757575758,
                13.8585858586,  14.1414141414, -13.8585858586, -13.5757575758, -13.2929292929, -13.0101010101, -12.7272727273,
                -12.4444444444, -12.1616161616, -11.8787878788, -11.5959595960, -11.3131313131, -11.0303030303, -10.7474747475,
                -10.4646464646, -10.1818181818,  -9.8989898990,  -9.6161616162,  -9.3333333333,  -9.0505050505,  -8.7676767677,
                -8.4848484848,  -8.2020202020,  -7.9191919192,  -7.6363636364,  -7.3535353535,  -7.0707070707,  -6.7878787879,
                -6.5050505051,  -6.2222222222,  -5.9393939394,  -5.6565656566,  -5.3737373737,  -5.0909090909,  -4.8080808081,
                -4.5252525253,  -4.2424242424,  -3.9595959596,  -3.6767676768,  -3.3939393939,  -3.1111111111,  -2.8282828283,
                -2.5454545455,  -2.2626262626,  -1.9797979798,  -1.6969696970,  -1.4141414141,  -1.1313131313,  -0.8484848485,
                -0.5656565657,  -0.2828282828 };

        double up = 7;
        double lo = -7;
        int nPoints = 50;

        double step = (2*(up-lo))/(2*nPoints-1);
        double[] p = new double[2*nPoints];

        for(int i=0;i<2*nPoints;i++){
            p[i] = step*i;
        }

        for(int i=nPoints+1; i<2*nPoints; i++){
            p[i] = -p[2*nPoints-i];
        }

        for(int i=0;i<p.length;i++){
            Assert.assertEquals("  Point at " + (i+1), fromR[i], p[i], 1e-9);
        }
    }

    //@Test
    public void testProbabilities(){
        System.out.println("Kernel probability test");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();


            KernelDensity density = new KernelDensity(x);
            System.out.println(density.cdf(50));
            System.out.println(density.idf(.75));

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    //@Test
    public void testRandom(){
        System.out.println("Kernel random number test");
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/density-test-data.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double[] x = new double[1000];
            int row = 0;
            br.readLine();//remove header
            while((line=br.readLine())!=null){
                s = line.split(",");
                x[row] = Double.parseDouble(s[0]);
                row++;
            }
            br.close();


            KernelDensity density = new KernelDensity(x);
            for(int i=0;i<25;i++){
                System.out.println(density.rand());
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

}
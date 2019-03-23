/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.factoranalysis;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class GPArotationTest {

    /**
     * MINRES loadings obtained from psychometrics package
     */
    private double[][] m255MINRESLoadings = {
        {0.681889, -0.391620, 0.122817},
        {0.698081, -0.370826, 0.160843},
        {0.718573, -0.221524, 0.128303},
        {0.625876, -0.185261, 0.169132},
        {0.760535, -0.019530, 0.116300},
        {0.719402, 0.344233, 0.181882},
        {0.594234, 0.431037, 0.239306},
        {0.519864, 0.177880, 0.112364},
        {0.709929, 0.137241, -0.002955},
        {0.590337, 0.265335, 0.005089},
        {0.846625, 0.013094, -0.326812},
        {0.696593, 0.028223, -0.278798}
    };


//    @Test
    public void genericTest(){
        //add test here
        double[][] x = {
                {2,3},
                {4,5}
        };
        RealMatrix X = new Array2DRowRealMatrix(x);
        X.walkInRowOrder(new DefaultRealMatrixChangingVisitor(){
            @Override
            public double visit(int row, int column, double value) {
                return value*value;
            }
        });

        printMatrix(X, "PP");

    }

    @Test
    public void testM255Varimax(){
        System.out.println("Varimax rotation test: m255 data");

        /**
         * True result obtained form R using GPArotation package
         */
        double[][] true_Varimax = {
            {0.2806153, 0.1303408, 0.7332729},
            {0.2620153, 0.1714751, 0.7434001},
            {0.3129422, 0.2735077, 0.6396424},
            {0.2277365, 0.2707234, 0.5740148},
            {0.3650465, 0.4339803, 0.5203103},
            {0.3193267, 0.7036671, 0.2683278},
            {0.2056908, 0.7280409, 0.1543441},
            {0.2398168, 0.4488439, 0.2356959},
            {0.4424600, 0.4637861, 0.3346261},
            {0.3748683, 0.4973271, 0.1762550},
            {0.7710955, 0.2932897, 0.3783441},
            {0.6437897, 0.2491322, 0.2953552},
        };

        RealMatrix L = new Array2DRowRealMatrix(m255MINRESLoadings);
        GPArotation gpa = new GPArotation();
        RotationResults R = gpa.rotate(L, RotationMethod.VARIMAX, false, 1000, 1e-5);
        RealMatrix Lr = R.getFactorLoadings();
//        System.out.println(R.toString());

        for(int i=0;i<Lr.getRowDimension();i++){
            for(int j=0;j<Lr.getColumnDimension();j++){
                assertEquals("  loading: ", Precision.round(true_Varimax[i][j],4), Precision.round(Lr.getEntry(i,j), 5), 1e-4);
            }

        }

    }

    @Test
    public void testM255Olimin(){
        System.out.println("Oblimin rotation test: m255 data");

        /**
         * True result obtained form R using GPArotation package
         */
        double[][] true_Oblimin = {
                {0.818982935, -0.086728237, -0.03014828},
                {0.836948168, -0.028356336,  0.02072749},
                {0.650083626,  0.102202784, -0.06494440},
                {0.609805424,  0.145551832,  0.03455997},
                {0.432053641,  0.310356386, -0.13539510},
                {0.071824049,  0.723712703, -0.07186957},
                {-0.022431888,  0.822701356,  0.05997316},
                {0.113162767,  0.434661061, -0.07100987},
                {0.129672215,  0.353260863, -0.33144976},
                {-0.052966793,  0.454048208, -0.28827799},
                {0.028011398, -0.006402917, -0.89108626},
                {-0.005289128,  0.004084979, -0.75198003}
        };

       RealMatrix L = new Array2DRowRealMatrix(m255MINRESLoadings);
       GPArotation gpa = new GPArotation();
       RotationResults R = gpa.rotate(L, RotationMethod.OBLIMIN, false, 60, 1e-5);
       RealMatrix Lr = R.getFactorLoadings();
//        System.out.println(R.toString());

        for(int i=0;i<Lr.getRowDimension();i++){
            for(int j=0;j<Lr.getColumnDimension();j++){
                assertEquals("  loading: ", Precision.round(true_Oblimin[i][j], 4), Precision.round(Lr.getEntry(i, j), 5), 1e-4);
            }

        }

    }

    @Test
    public void testM255Quartimin(){
        System.out.println("Quartimin rotation test: m255 data");

        /**
         * True result obtained form R using GPArotation package
         */
        double[][] true_Quartimin = {
                {0.818982935, -0.086728237, -0.03014828},
                {0.836948168, -0.028356336,  0.02072749},
                {0.650083626,  0.102202784, -0.06494440},
                {0.609805424,  0.145551832,  0.03455997},
                {0.432053641,  0.310356386, -0.13539510},
                {0.071824049,  0.723712703, -0.07186957},
                {-0.022431888,  0.822701356,  0.05997316},
                {0.113162767,  0.434661061, -0.07100987},
                {0.129672215,  0.353260863, -0.33144976},
                {-0.052966793,  0.454048208, -0.28827799},
                {0.028011398, -0.006402917, -0.89108626},
                {-0.005289128,  0.004084979, -0.75198003}
        };

        RealMatrix L = new Array2DRowRealMatrix(m255MINRESLoadings);
        GPArotation gpa = new GPArotation();
        RotationResults R = gpa.rotate(L, RotationMethod.QUARTIMIN, false, 500, 1e-5);
        RealMatrix Lr = R.getFactorLoadings();
//        System.out.println(R.toString());

        for(int i=0;i<Lr.getRowDimension();i++){
            for(int j=0;j<Lr.getColumnDimension();j++){
                assertEquals("  loading: ", Precision.round(true_Quartimin[i][j],4), Precision.round(Lr.getEntry(i,j), 5), 1e-4);
            }

        }

    }

    @Test
     public void testM255GeominOblique(){
        System.out.println("Oblique Geomin rotation test: m255 data");

        /**
         * True result obtained form R using GPArotation package
         */
        double[][] true_Geomin_oblique = {
                {0.814401032, -0.07246248, -0.02746737},
                {0.831474480, -0.01326628,  0.02443654},
                {0.646042722,  0.11864079, -0.05620512},
                {0.604841472,  0.16035608,  0.04246413},
                {0.429130847,  0.32919710, -0.11951339},
                {0.068651498,  0.74475428, -0.04555539},
                {-0.026759588,  0.84183379,  0.08633271},
                {0.111079049,  0.44919958, -0.05420552},
                {0.130427210,  0.37116164, -0.31191986},
                {-0.051932818,  0.47010788, -0.26735188},
                {0.036623600,  0.01140911, -0.87276177},
                {0.002120624,  0.01879531, -0.73637231},
        };

        double[][] true_Phi = {
                {1.0000000,  0.5565973, -0.7280542},
                {0.5565973,  1.0000000, -0.6808553},
                {-0.7280542, -0.6808553,  1.0000000}
        };

        RealMatrix L = new Array2DRowRealMatrix(m255MINRESLoadings);
        GPArotation gpa = new GPArotation();
        RotationResults R = gpa.rotate(L, RotationMethod.GEOMIN_Q, false, 500, 1e-5);
        RealMatrix Lr = R.getFactorLoadings();
//        System.out.println(R.toString());

        for(int i=0;i<Lr.getRowDimension();i++){
            for(int j=0;j<Lr.getColumnDimension();j++){
                assertEquals("  loading: ", Precision.round(true_Geomin_oblique[i][j],4), Precision.round(Lr.getEntry(i,j), 5), 1e-4);
            }
        }

        RealMatrix Phi = R.getPhi();
        for(int i=0;i<Phi.getRowDimension();i++){
            for(int j=0;j<Phi.getColumnDimension();j++){
                assertEquals("  factor correlation: ", Precision.round(true_Phi[i][j],4), Precision.round(Phi.getEntry(i,j), 5), 1e-4);
            }
        }

    }

    private void printMatrix(RealMatrix x, String title){
        System.out.println("PRINTING MATRIX: " + title);
        for(int i=0;i<x.getRowDimension();i++){
            for(int j=0;j<x.getColumnDimension();j++){
                System.out.print(x.getEntry(i,j) + "  ");
            }
            System.out.println();
        }
    }


}

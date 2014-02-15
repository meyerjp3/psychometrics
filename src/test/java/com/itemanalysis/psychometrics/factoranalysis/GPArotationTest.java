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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class GPArotationTest {


    @Test
    public void testVarimaxGradient(){

        double[][] load = {
                {-0.6835635, -0.39075389, -0.112952959},
                {-0.7002264, -0.37122612, -0.154215129},
                {-0.7200294, -0.22165241, -0.122725011},
                {-0.6284771, -0.18429200, -0.154465092},
                {-0.7622408, -0.01877837, -0.104474800},
                {-0.7216004,  0.34581080, -0.175903900},
                {-0.5962210,  0.42915611, -0.232550253},
                {-0.5214003,  0.17820452, -0.102642019},
                {-0.7102079,  0.13784441,  0.013923788},
                {-0.5910368,  0.26615956,  0.007638572},
                {-0.8388765,  0.01292753,  0.319745477},
                {-0.6957471,  0.02916702,  0.302869181}
        };

        GPArotation gpa = new GPArotation();

//        RealMatrix L = new Array2DRowRealMatrix(load);
//        RealMatrix G = gpa.varimaxGradient(L);
//        double f = gpa.varimaxFunction(L);
//        for(int i=0;i<G.getRowDimension();i++){
//            for(int j=0;j<G.getColumnDimension();j++){
//                System.out.print(G.getEntry(i,j) + "  ");
//            }
//            System.out.println();
//        }
//        System.out.println("fmin: " + f);

        RealMatrix LOAD = new Array2DRowRealMatrix(load);
        RealMatrix L = gpa.rotate(LOAD, RotationMethod.VARIMAX);
//        for(int i=0;i<L.getRowDimension();i++){
//            for(int j=0;j<L.getColumnDimension();j++){
//                System.out.print(L.getEntry(i,j) + "  ");
//            }
//            System.out.println();
//        }


//        assertEquals("  Factor loadings: ", true_loadings[i][j], Precision.round(factorMethod.getFactorLoadingAt(i, j), 4), 1e-3);
    }


}

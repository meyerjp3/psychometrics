/*
 * Copyright 2012 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.mixture;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Difficulties in running these tests include:
 * 1. Long running EM algorithm. IT takes a few seconds to several minutes
 *    for a single model to converge.
 * 2. The group labels are arbitrary. This phenomenon is called label switching
 *    and it makes it difficult to use assertEquals() to check teh result.
 *
 */
public class MvNormalComponentDistributionTest {

    MixtureModelTestData testData = null;

    public MvNormalComponentDistributionTest(){
        testData = new MixtureModelTestData();
    }



    /**
     * This test uses the iris data from R. It is a three group mixture model.
     * Results confirmed with the following R code:
     *
     * library(mixtools)
     * mvnormalmixEM(iris[,-5], arbvar=TRUE, k=3)
     *
     */
//    @Test
    public void mixModel1Test(){
        System.out.println("Mixture model test with Iris Data");
        MixtureModelTestData testData = new MixtureModelTestData();

        BlockRealMatrix allData = new BlockRealMatrix(testData.getMixtureData3());
        BlockRealMatrix group1Data = allData.getSubMatrix(0, 49, 0, 3);
        BlockRealMatrix group2Data = allData.getSubMatrix(50, 99, 0, 3);
        BlockRealMatrix group3Data = allData.getSubMatrix(100, 149, 0, 3);
        BlockRealMatrix data = allData.getSubMatrix(0,149,0,3);

//        System.out.println(Arrays.toString(allData.getRow(0)));

        VectorialMean trueMean1 = new VectorialMean(4);
        VectorialMean trueMean2 = new VectorialMean(4);
        VectorialMean trueMean3 = new VectorialMean(4);
        Covariance trueCov1 = new Covariance(group1Data);
        Covariance trueCov2 = new Covariance(group2Data);
        Covariance trueCov3 = new Covariance(group3Data);

        for(int i=0;i<group1Data.getRowDimension();i++){
            trueMean1.increment(group1Data.getRow(i));
        }
        for(int i=0;i<group2Data.getRowDimension();i++){
            trueMean2.increment(group2Data.getRow(i));
        }

        for(int i=0;i<group3Data.getRowDimension();i++){
            trueMean3.increment(group3Data.getRow(i));
        }

        double[] trueMeanGroup1 = trueMean1.getResult();
        double[] trueMeanGroup2 = trueMean2.getResult();
        double[] trueMeanGroup3 = trueMean3.getResult();


        int startGroup = 3;
        int endGroup = 3;
        int maxIter = 1000;
        double converge = 1e-8;
        int starts = 100;

        MvNormalMixtureModel model = null;

        model = new MvNormalMixtureModel(data, endGroup);
                    model.setModelConstraints(false, false, false, false);
                    model.setEmOptions(maxIter, converge, starts);
                    model.call();

                    System.out.println("    Testing convergence...");
                    assertTrue("Convergence test", model.converged());

                    double[] pi = new double[3];
                    pi[0] = model.getMixingProportion(0);
                    pi[1] = model.getMixingProportion(1);
                    pi[2] = model.getMixingProportion(2);

                    //check mixing proportions
        //            System.out.println("    Testing proportions...");
        //            assertEquals("Proportion 1 test: ", 0.40, pi[index[0]], 5e-2);
        //            assertEquals("Proportion 2 test: ", 0.60, pi[index[1]], 5e-2);

                    double[] estMean1 = model.getMean(0);
                    double[] estMean2 = model.getMean(1);
                    double[] estMean3 = model.getMean(2);
                    double[][] estCov1 = model.getCov(0);
                    double[][] estCov2 = model.getCov(1);
                    double[][] estCov3 = model.getCov(2);
                    double[][] trueCov1Array = trueCov1.getCovarianceMatrix().getData();
                    double[][] trueCov2Array = trueCov2.getCovarianceMatrix().getData();
                    double[][] trueCov3Array = trueCov3.getCovarianceMatrix().getData();

                    System.out.println(model.printResults());


                    /**
                     * Test means
                     */
        //            System.out.println("    Testing means...");
        //            for(int i=0;i<3;i++){
        //                assertEquals("Group 1 mean test", trueMeanGroup1[i], estMean1[i], 5e-2);
        //                assertEquals("Group 2 mean test", trueMeanGroup2[i], estMean2[i], 5e-2);
        //                assertEquals("Group 3 mean test", trueMeanGroup3[i], estMean3[i], 5e-2);
        //            }
        //
        //            /**
        //             * Test Covariance matrix
        //             */
        //            System.out.println("    Testing covariances...");
        //            for(int i=0;i<3;i++){
        //                for(int j=0;j<3;j++){
        //                    assertEquals("Cov[" +i + "," + j+"] test:", trueCov1Array[i][j], estCov1[i][j], 5e-2);
        //                }
        //            }
        //
        //            for(int i=0;i<3;i++){
        //                for(int j=0;j<3;j++){
        //                    assertEquals("Cov[" +i + "," + j+"] test:", trueCov2Array[i][j], estCov2[i][j], 5e-2);
        //                }
        //            }
        //
        //            for(int i=0;i<3;i++){
        //                for(int j=0;j<3;j++){
        //                    assertEquals("Cov[" +i + "," + j+"] test:", trueCov2Array[i][j], estCov2[i][j], 5e-2);
        //                }
        //            }

    }

    /**
     * This test computes mean vector and covariance matrix for a single component mixture model.
     * The estimates should be the same as the sample mean and covariance.
     */
//    @Test
    public void mixModel2Test(){
        System.out.println("Mixture model test with Two variables and two components");

        /**
         * x contains 2,000 cases and three variables.
         * The first two variables are continuous data. The last variable is a group indicator variable
         */
        double[][] x = testData.getMixtureData2();
        BlockRealMatrix allData = new BlockRealMatrix(x);
        int nrow = allData.getRowDimension();

        BlockRealMatrix group1Data = allData.getSubMatrix(0, 799, 0, 1);
        BlockRealMatrix group2Data = allData.getSubMatrix(800, 1999, 0, 1);

        VectorialMean trueMean1 = new VectorialMean(2);
        VectorialMean trueMean2 = new VectorialMean(2);
        Covariance trueCov1 = new Covariance(group1Data);
        Covariance trueCov2 = new Covariance(group2Data);

        for(int i=0;i<group1Data.getRowDimension();i++){
            trueMean1.increment(group1Data.getRow(i));
        }
        for(int i=0;i<group2Data.getRowDimension();i++){
            trueMean2.increment(group2Data.getRow(i));
        }

        double[] trueMeanGroup1 = trueMean1.getResult();
        double[] trueMeanGroup2 = trueMean2.getResult();

        /**
         * actual data in first two columns
         */
        BlockRealMatrix data = allData.getSubMatrix(0, nrow-1, 0, 1);

        //run two group mixture model
        int startGroup = 2;
        int endGroup = 2;
        int maxIter = 500;
        double converge = 1e-12;
        int starts = 100;

        MvNormalMixtureModel model = null;

        model = new MvNormalMixtureModel(data, endGroup);
        model.setModelConstraints(true, true, true, true);
        model.setEmOptions(maxIter, converge, starts);
        model.call();
//            System.out.println(model.printResults());

        double[] pi = new double[2];
        pi[0] = model.getMixingProportion(0);
        pi[1] = model.getMixingProportion(1);
        int[] index = {0, 1};

        //group 1 should be the smaller group. If not, reverse indexes.
        if(pi[0]>pi[1]){
            index[0]=1;
            index[1]=0;
        }

        //check convergence
        System.out.println("    Testing convergence...");
        assertTrue("Convergence test", model.converged());



        //check mixing proportions
        System.out.println("    Testing proportions...");
        assertEquals("Proportion 1 test: ", 0.40, pi[index[0]], 5e-2);
        assertEquals("Proportion 2 test: ", 0.60, pi[index[1]], 5e-2);

        //get estimated parameters
        double[] estMeanGroup1 = model.getMean(index[0]);
        double[] estMeanGroup2 = model.getMean(index[1]);
        double[][] estCovGroup1 = model.getCov(index[0]);
        double[][] estCovGroup2 = model.getCov(index[1]);


        //check mean vector for each group
        System.out.println("    Testing means...");
        assertEquals("Mean (1,1) test", trueMeanGroup1[index[0]], estMeanGroup1[index[0]], 5e-2);
        assertEquals("Mean (1,2) test", trueMeanGroup1[index[1]], estMeanGroup1[index[1]], 5e-2);
        assertEquals("Mean (2,1) test", trueMeanGroup2[index[0]], estMeanGroup2[index[0]], 5e-2);
        assertEquals("Mean (2,2) test", trueMeanGroup2[index[1]], estMeanGroup2[index[1]], 5e-2);

        //check covariance matrix for group 1
        System.out.println("    Testing covariances...");
        double[][] trueCovGroup1 = trueCov1.getCovarianceMatrix().getData();
        for(int i=0;i<2;i++){
            for(int j=0;j<2;j++){
                assertEquals("CovGroup1[" +i + "," + j+"] test:", trueCovGroup1[i][j], estCovGroup1[i][j], 5e-2);
            }
        }

        //check covariance matrix for group 2
        double[][] trueCovGroup2 = trueCov2.getCovarianceMatrix().getData();
        for(int i=0;i<2;i++){
            for(int j=0;j<2;j++){
                assertEquals("CovGroup2[" +i + "," + j+"] test:", trueCovGroup2[i][j], estCovGroup2[i][j], 5e-2);
            }
        }


    }


}

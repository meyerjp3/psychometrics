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
package com.itemanalysis.psychometrics.irt.estimation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MultivariateDiscreteDistributionTest {



    @Test
    public void testPermutations5(){

        int size = 5;

        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                for(int k=0;k<size;k++){
                    for(int l=0;l<size;l++){
                        for(int m=0;m<size;m++){
                            System.out.println("[" + (i+1) + " " + (j+1) + " " + (k+1) + " " + (l+1) + " " + (m+1) + "]");
                        }
                    }

                }
            }
        }


    }

    @Test
    public void testPermutations2(){

        int size = 21;
        double[] prob = new double[size];
        double sum = 0.0;
        for(int i=0;i<size;i++){
            prob[i] = (i+.001)*1.0/size;
            sum += prob[i];
        }
        for(int i=0;i<size;i++){
            prob[i] = prob[i]/sum;
        }

        double p = 0.0;
        double probSum = 0.0;
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                p = prob[i]*prob[j];
                probSum += p;
                System.out.println("Pr[" + (i+1) + " " + (j+1) +  "] = " + p);
            }
        }
        System.out.println(probSum);


    }

    @Test
    public void testPermutations3(){

        int size = 11;

        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                for(int k=0;k<size;k++){
                    System.out.println("[" + (i+1) + " " + (j+1) + " " + (k+1) + "]");

                }
            }
        }
    }

    @Test
    public void testPermutations4(){

        int size = 7;

        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                for(int k=0;k<size;k++){
                    for(int l=0;l<size;l++){
                        System.out.println("[" + (i+1) + " " + (j+1) + " " + (k+1) + " " + (l+1) + "]");
                    }

                }
            }
        }
    }


}

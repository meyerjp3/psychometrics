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
package com.itemanalysis.psychometrics.rasch;

/**
 * Computes probabilities and other quantities associated with Andrich's rating scale model.
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class RatingScaleModel {

    public RatingScaleModel(){

    }

    /**
     * Compute the numerator of the rating scale model
     *
     * @param theta
     * @param difficulty
     * @param step
     * @param category
     * @return
     */
    public double categoryNumerator(double theta, double difficulty, double[] step, int category){
        double sum = 0.0;
        int m=1;

        while(m<=category){
            sum+=(theta-difficulty-step[m]);
            m++;
        }
        return Math.exp(sum);
    }

    /**
     * Computes the denominator of the rating scale model
     *
     * @param theta
     * @param difficulty
     * @param step
     * @return
     */
    public double totalDenominator(double theta, double difficulty, double[] step){
        double total = 0.0;
        for(int m=0;m<step.length;m++){
            total+=categoryNumerator(theta, difficulty, step, m);
        }
        return total;
    }

    /**
     * Returns the probability of responding in category given theta.
     *
     * @param theta
     * @param difficulty
     * @param step
     * @param category
     * @return
     */
    public double value(double theta, double difficulty, double[] step, int category){
        return categoryNumerator(theta, difficulty, step, category)/totalDenominator(theta, difficulty, step);
    }

    /**
     * Sums probability of a response over all items in this group
     *
     * @param theta
     * @param difficulty
     * @param step
     * @param category
     * @return
     */
    public double valueForGroup(double theta, double[] difficulty, double[] step, int category){
        double total=0.0;
        for(int i=0;i<difficulty.length;i++){
            total += categoryNumerator(theta, difficulty[i], step, category)/totalDenominator(theta, difficulty[i], step);
        }
        return total;
    }

    /**
     * Computes an examinee's expected response rho.
     *
     * @param theta
     * @param difficulty
     * @param step
     * @return
     */
    public double expectedValue(double theta, double difficulty, double[] step){
        double ev = 0.0;
        for(int m=0;m<step.length;m++){
            ev+=((double)m)*this.value(theta, difficulty, step, m);
        }
        return ev;
    }

    /**
     * computes the expected squared rho. This method is used in calls to
     * denomInf(double theta, double difficulty, double[] step)
     *
     * @param theta
     * @param difficulty
     * @param step
     * @return
     */
    public double expectedSquaredValue(double theta, double difficulty, double[] step){
        double ev2 = 0.0;
        for(int m=0;m<step.length;m++){
            ev2+=Math.pow(((double)m),2)*this.value(theta, difficulty, step, m);
        }
        return ev2;
    }

    /**
     * Computes the probability of scoring in category or higher.
     *
     * @param theta
     * @param difficulty
     * @param step
     * @param category
     * @return
     */
    public double probOfCategoryOrHigher(double theta, double difficulty, double[] step, int category){
        double prob = 0.0;
        for(int m=category;m<step.length;m++){
            prob+=this.value(theta, difficulty, step, m);
        }

        return prob;
    }

    /**
     * Computes the item information.
     *
     * @param theta
     * @param difficulty
     * @param step
     * @return
     */
    public double denomInf(double theta, double difficulty, double[] step){
        double dif = expectedSquaredValue(theta, difficulty, step)-Math.pow(expectedValue(theta, difficulty, step), 2);
        return dif;
    }

    /**
     * Computes the total probability of scoring in category or higher.
     *
     * @param theta
     * @param difficulty
     * @param step
     * @param category
     * @return
     */
    public double totalProbOfCategoryOrHigher(double theta, double[] difficulty, double[] step, int category){
        double total=0.0;
        for(int i=0;i<difficulty.length;i++){
            total+=this.probOfCategoryOrHigher(theta, difficulty[i], step, category);
        }
        return total;
    }

    /**
     * A computation needed for a Newton update
     *
     * @param theta
     * @param difficulty
     * @param step
     * @param category
     * @return
     */
    public double totalDenomDifferenceForCategoryJmle(double theta, double[] difficulty, double[] step, int category){
        double dif=0.0;
        double temp=0.0;
        double total=0.0;
        for(int i=0;i<difficulty.length;i++){
            temp=this.probOfCategoryOrHigher(theta, dif, step, category);
            dif=temp-Math.pow(temp,2);
            total+=dif;
        }
        return total;
    }

    /**
     * Variance of response Xni. This method is needed for computation of fit statistics.
     * @param theta
     * @param difficulty
     * @param step
     * @return
     */
    public double varianceOfResponse(double theta, double difficulty, double[] step){
        double Wni = 0.0;
        double Eni = expectedValue(theta, difficulty, step);
        for(int m=0;m<step.length;m++){
            Wni += Math.pow(m-Eni, 2)*this.value(theta, difficulty, step, m);
        }
        return Wni;
    }

    /**
     * Kurtosis of response Xni.  This method is needed for computation of fit statistics.
     * @param theta
     * @param difficulty
     * @param step
     * @return
     */
    public double kurtosisOfResponse(double theta, double difficulty, double[] step){
        double Wni = 0.0;
        double Eni = expectedValue(theta, difficulty, step);
        for(int m=0;m<step.length;m++){
            Wni += Math.pow(m-Eni, 4)*this.value(theta, difficulty, step, m);
        }
        return Wni;
    }

}

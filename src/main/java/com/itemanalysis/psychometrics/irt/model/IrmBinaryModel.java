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
package com.itemanalysis.psychometrics.irt.model;

public class IrmBinaryModel {

    //Number of response categories. Always 2.
    private final int ncat = 2;

    //Scaling constant
    private double D = 1.0;

    //Total number of parameters (fixed plus free)
    private final int numberOfParameters = 4;

    //Score weights
    private double[] scoreWeight = {0,1};

    //Parameters
    private double discrimination = 1.0;
    private double difficulty = 0;
    private double guessing = 0;
    private double slipping = 1.0;

    //Mask fixed parameters.
    //Initially nothing is fixed and all parameters are freely estimated.
    private double[] fixedMask = {1,1,1,1};

    //Proposal parameters
    private double proposalDiscrimination = 1;
    private double proposalDifficulty = 0;
    private double proposalGuessing = 0.0;
    private double proposalSlipping = 1.0;

    //Standard errors (a negative value indicates it has not been computed or set.
    private double discriminationStdError = -1;
    private double difficultyStdError = -1;
    private double guessingStdError = -1;
    private double slippingStdError = -1;

    /**
     * Constructor for four-parameter logistic model (4PL)
     *
     * @param discrimination item discrimination parameter
     * @param difficulty item difficulty parameter
     * @param guessing lower-asymptote parameter
     * @param slipping upper-asymptote parameter
     */
    public IrmBinaryModel(double discrimination, double difficulty, double guessing, double slipping){
        this.discrimination = discrimination;
        this.difficulty = difficulty;
        this.guessing = guessing;
        this.slipping = slipping;
        this.proposalDiscrimination = discrimination;
        this.proposalDifficulty = difficulty;
        this.proposalGuessing = guessing;
        this.proposalSlipping = slipping;
    }

    /**
     * Constructor for the three-parameter logistic model (3PL)
     *
     * @param discrimination item discrimination parameter
     * @param difficulty item difficulty parameter
     * @param guessing lower-asymptote parameter
     */
    public IrmBinaryModel(double discrimination, double difficulty, double guessing){
        this(discrimination, difficulty, guessing, 1.0);
        fixedMask[3] = 0;//slipping masked
    }

    /**
     * Constructor for the two-parameter logistic model (2PL) or one-parameter logistic model (1PL)
     *
     * @param discrimination item discrimination parameter
     * @param difficulty item difficulty parameter
     */
    public IrmBinaryModel(double discrimination, double difficulty){
        this(discrimination, difficulty, 0.0, 1.0);
        fixedMask[2] = 0;//guessing masked
        fixedMask[3] = 0;//slipping masked
    }

    /**
     * Constructor for the Rasch
     *
     * @param difficulty item difficulty parameter
     */
    public IrmBinaryModel(double difficulty){
        this(1.0, difficulty, 0.0, 1.0);
        fixedMask[0] = 0;//discrimination masked
        fixedMask[2] = 0;//guessing masked
        fixedMask[3] = 0;//slipping masked
    }

    /**
     * Computes probability of a correct response using value provided to the method, not the parameters
     * stored in the object. Probability of a correct response. The order of the parameters in the iparam array is:
     * iparam[0] = discrimination, iparam[1] = difficulty, iparam[2] = guessing, iparam[3] = slipping.
     *
     * @param theta person ability parameter.
     * @param iparam array of item parameters. The order is important and will be unique to each implementation of the interface.
     * @param response an item response category.
     * @return
     */
    public double probability(double theta, double[] iparam, int response){
        if(response==1){
            return probRight(theta, iparam);
        }else{
            return probWrong(theta, iparam);
        }
    }

    /**
     * Computes the probability of a correct response given parameters stored in the object.
     *
     * @param theta a person ability value.
     * @param response an item response (i.e. a person's score on an item).
     * @return
     */
    public double probability(double theta, int response){
        double prob = 0.0;
        if(response==1){
            prob = probRight(theta);
        }else{
            prob = probWrong(theta);
        }
        return Math.min(1.0, Math.max(0.0, prob)); //always return value between 0 and 1
    }

    private double probRight(double theta, double[] iparam){
        if(iparam[0]<0) return 0;
        double z = Math.exp(D*iparam[0]*(theta-iparam[1]));
        return  iparam[2]+(iparam[3]-iparam[2])*z/(1+z);
    }

    private double probWrong(double theta, double[] iparam){
        if(iparam[0]<0) return 0;
        return 1.0 - probRight(theta, iparam);
    }

    private double probRight(double theta){
        if(guessing < 0) return 0;
        double top = Math.exp(D*discrimination*(theta-difficulty));
        double prob = guessing + (slipping-guessing)*top/(1+top);
        return prob;
    }

    private double probWrong(double theta){
        if(guessing < 0) return 0;
        return 1.0-probRight(theta);
    }

    /**
     * Computes the expected value, which is the same as the probability of a correct response.
     *
     * @param theta a person ability value.
     * @return
     */
    public double expectedValue(double theta){
        return scoreWeight[1]*probRight(theta);
    }

    /**
     * Not implemented. Only implemented for the graded response model.
     *
     * @param theta a person ability value
     * @param category response category
     * @return
     */
    public double cumulativeProbability(double theta, int category){
        return Double.NaN;
    }

    /**
     * Gradient computed using array of item parameters
     *
     * @param theta person ability
     * @param iparam array of item parameters (0=discrimination, 1=difficulty, 2=guessing, 3=slipping)
     * @param k item response (either 0 or 1)
     * @param D scaling constant
     * @return gradient
     */
    public double[] gradient(double theta, double[] iparam, int k, double D){
        double[] deriv = new double[numberOfParameters];

        double a = iparam[0]; //Discrimination
        double b = iparam[1]; //Difficulty
        double c = iparam[2]; //Guessing
        double u = iparam[3]; //Slipping

        double e1 = a * D;
        double e2 = theta - b;
        double e4 = Math.exp(-(e1 * e2));
        double e5 = 1 + e4;
        double e6 = e5*e5;
        double e7 = 1/e5;
        double e8 = u - c;

         deriv[0] =  D * e4 * e2 * e8/e6;
         deriv[1] = -(e1 * e4 * e8/e6);
         deriv[2] = 1.0 - e7;
         deriv[3] = e7;

        if(k==0){
            deriv[0] = -deriv[0];
            deriv[1] = -deriv[1];
            deriv[2] = -deriv[2];
            deriv[3] = -deriv[3];
        }

        //apply mask for fixed parameters
        deriv[0] *= fixedMask[0];
        deriv[1] *= fixedMask[1];
        deriv[2] *= fixedMask[2];
        deriv[3] *= fixedMask[3];

        return deriv;

    }

    /**
     * Computes gradientAt using item parameters stored in the object.
     *
     * @param theta person ability value
     * @param k response category
     * @return gradientAt
     */
    public double[] gradient(double theta, int k){
        double[] iparam = {discrimination, difficulty, guessing, slipping};
        return gradient(theta, iparam, k, D);
    }

    /**
     * Hessian or matrix of second derivatives. Computed using an array of item parameters.
     *
     * @param theta person ability value.
     * @return a two-way array containing the Hessian matrix values.
     */
    public double[][] hessian(double theta, double[] iparam){
        double[][] deriv = new double[numberOfParameters][numberOfParameters];

        double a = iparam[0]; //Discrimination
        double b = iparam[1]; //Difficulty
        double c = iparam[2]; //Guessing
        double u = iparam[3]; //Slipping

        double e1 = theta - b;
        double e2 = D * a;
        double e4 = Math.exp(-(e2 * e1));
        double e5 = 1 + e4;
        double e6 = e5*e5;
        double e7 = 2 * (e4/e5);
        double e9 = e2 * e4/e6;
        double e12 = D * e4 * e1/e6;
        double e13 = u - c;
        double e14 = -e9;
        double e15 = -e12;
        double e16 = 1 - e7;
        double e17 = e7 - 1;
        double e18 = D*D;

        deriv[0][0] = e18 * e17 * e4 * e1*e1 * e13/e6;
        deriv[0][1] = -(D * (1 + e2 * e17 * e1) * e4 * e13/e6);
        deriv[0][2] = e15;
        deriv[0][3] = e12;

        deriv[1][0] = D * (e2 * e16 * e1 - 1) * e4 * e13/e6;
        deriv[1][1] = -(deriv[1][0]*deriv[1][0] * e18 * e16 * e4 * e13/e6);
        deriv[1][2] = e9;
        deriv[1][3] = e14;

        deriv[2][0] = e15;
        deriv[2][1] = e9;
        deriv[2][2] = 0;
        deriv[2][3] = 0;

        deriv[3][0] = e12;
        deriv[3][1] = e14;
        deriv[3][2] = 0;
        deriv[3][3] = 0;

        //apply mask for fixed parameters
        for(int i=0;i<deriv.length;i++){
            for(int j=0;j<deriv.length;j++){
                deriv[i][j] *= fixedMask[i]*fixedMask[j];
            }
        }

        return deriv;
    }

    /**
     * Hessian using existing item parameters
     *
     * @param theta person ability
     * @return hessian
     */
    public double[][] hessian(double theta){
        double[] iparam = {discrimination, difficulty, guessing, slipping};
        return hessian(theta, iparam);
    }

    /**
     * First derivative of response function with respect to theta.
     *
     * @param theta a person ability value.
     * @return first derivative
     */
    public double derivTheta(double theta){
        double L = discrimination*(theta-difficulty);
        double top = (slipping-guessing)*D*discrimination;
        double bot = Math.exp(D*L) + 2.0 + Math.exp(-D*L);
        return top/bot;
    }

    /**
     * Second derivative wrt theta
     *
     * @param theta person ability
     * @return second derivative wrt theta
     */
    public double deriv2Theta(double theta){
        double L = discrimination*(theta-difficulty);
        double eDL = Math.exp(D*L);
        double eNDL = Math.exp(-D*L);
        double top = -(slipping-guessing)*D*D*discrimination*discrimination*(eDL-eNDL);
        double bot = eDL + 2.0 + eNDL;
        return top/(bot*bot);
    }

    public double itemInformationAt(double theta){
        double p = probRight(theta);
        double a2 = discrimination*discrimination;
        double top = D*D*a2*Math.pow(p-guessing, 2)*Math.pow(slipping-p, 2);
        double bot = Math.pow(slipping-guessing, 2)*p*(1-p);
        double info = top/bot;
        return info;
    }

    /**
     * Set scaling constant. Common choices are 1.0, 1.7, 1.702.
     *
     * @param D scaling constant
     */
    public void setScalingConstant(double D){
        this.D = D;
    }

    public void scale(double intercept, double slope){
        if(fixedMask[0]==1){
            //transform discrimination if it is free
            discrimination = discrimination/slope;
            discriminationStdError *= slope;
        }else if(fixedMask[1]==1){
            //transform difficulty if it is free
            difficulty = intercept + slope*difficulty;
            difficultyStdError *= slope;
        }
    }

    /**
     * Sets array of fixed parameter masks. Elements should be a 0 or 1.
     * If value is 0, the parameter is fixed to its start value.
     * If value is 1, the parameter is freely estimated.
     *
     * @param fixedMask array of mask indicators.
     */
    public void setFixedMask(double[] fixedMask){
        this.fixedMask = fixedMask;
    }

    /**
     * Gets number of free parameters
     *
     * @return number of free parameters
     */
    public int getNumberOfFreeParameters(){
        int sum = 0;

        for(int i=0;i<fixedMask.length;i++){
            sum += fixedMask[i];
        }

        return sum;
    }

}

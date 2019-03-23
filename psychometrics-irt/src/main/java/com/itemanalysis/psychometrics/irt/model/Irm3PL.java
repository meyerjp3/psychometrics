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
package com.itemanalysis.psychometrics.irt.model;

import com.itemanalysis.psychometrics.irt.estimation.ItemParamPrior;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.Formatter;

/**
 * An implementation of {@link AbstractItemResponseModel} that allows for the three-parameter logistic (3PL) model,
 * two-parameter logistic (2PL) model, one-parameter logistic (1PL) model, and the Rasch model. The particular
 * type of item response model is determined by the constructor used to create the object. See the constructors
 * for more details on which one to use for each type of model.
 */
public class Irm3PL extends AbstractItemResponseModelWithGradient {

    private boolean raschModel = false;
    private double discrimination = 1.0;
    private double difficulty = 0.0;
    private double guessing = 0.0;
    private double slipping = 1.0;
    private double D = 1.7;
    private double discriminationStdError = 0.0;
    private double difficultyStdError = 0.0;
    private double guessingStdError = 0.0;
    private int numberOfParameters = 1;
    private double proposalDiscrimination = 1.0;
    private double proposalDifficulty = 0.0;
    private double proposalGuessing = 0.0;
    private double proposalSlipping = 1.0;
    private ItemParamPrior discriminationPrior = null;
    private ItemParamPrior difficultyPrior = null;
    private ItemParamPrior guessingPrior = null;


    /**
     * Constructor for three parameter logistic model
     *
     * @param discrimination item discrimination parameter
     * @param difficulty item difficulty parameter
     * @param guessing lower-asymptote parameter
     * @param D scaling factor
     */
    public Irm3PL(double discrimination, double difficulty, double guessing, double D){
        this.discrimination = discrimination;
        this.difficulty = difficulty;
        this.guessing = guessing;
        this.proposalDiscrimination = discrimination;
        this.proposalDifficulty = difficulty;
        this.proposalGuessing = guessing;
        this.D = D;
        this.numberOfParameters = 3;
        this.ncat = 2;
        defaultScoreWeights();
    }

    /**
     * Constructor for two parameter logistic model.
     *
     * @param discrimination item discrimination parameter
     * @param difficulty item difficulty parameter
     * @param D scaling factor
     */
    public Irm3PL(double discrimination, double difficulty, double D){
        this.discrimination = discrimination;
        this.difficulty = difficulty;
        this.proposalDiscrimination = discrimination;
        this.proposalDifficulty = difficulty;
        this.proposalGuessing = 0.0;
        this.D = D;
        numberOfParameters = 2;
        this.ncat = 2;
        defaultScoreWeights();
    }

    /**
     * Constructor for one parameter logistic model
     *
     * @param difficulty item difficulty parameter
     * @param D scaling factor
     */
    public Irm3PL(double difficulty, double D){
        this.raschModel = true;
        this.difficulty = difficulty;
        this.proposalDiscrimination = 1.0;
        this.proposalDifficulty = difficulty;
        this.proposalGuessing = 0.0;
        this.D = D;
        numberOfParameters = 1;
        this.ncat = 2;
        defaultScoreWeights();
    }

    /**
     * Comute the probability of a correct response. This method uses a combination of stored
     * parameters and those passed in the iparam array. This arrangement allows for fixing
     * some item parameters during estimation. For example, instantiating this object as a 2PL
     * and setting the stored value of the guessing parameter will result in a 3PL with a fixed
     * guessing parameter. If you do not want to use the stored values of any parameter, then
     * the iparam array should be of length = 3 and contain the discrimination, difficulty,
     * and guessing parameters, respectively.
     *
     * The order of parameters in iparam is as follows:
     * 1. Rasch model: iparam[0] = difficulty
     * 2. 2PL: iparam[0] = discrimination, iparam[1] = difficulty
     * 3. 3PL: iparam[1] = discrimination, iparam[1] = difficulty, iparam[2] = guessing
     *
     * @param theta person ability parameter.
     * @param iparam item parameter array that is one to three in length.
     * @param D scaling constant.
     * @return probability of a correct answer.
     */
    public double probability(double theta, double[] iparam, int response, double D){
        double prob = 0.0;
        if(response==1){
            return probRight(theta, iparam, D);
        }else{
            return probWrong(theta, iparam, D);
        }
    }

    public double probability(double theta, int response){
        double prob = 0.0;
        if(response==1){
            prob = probRight(theta);
        }else{
            prob = probWrong(theta);
        }
        return prob;
    }

    /**
     * Comute the probability of a correct response. This method uses a combination of stored
     * parameters and those passed in the iparam array. This arrangement allows for fixing
     * some item parameters during estimation. For example, instantiating this object as a 2PL
     * and setting the stored value of the guessing parameter will result in a 3PL with a fixed
     * guessing parameter. If you do not want to use the stored values of any parameter, then
     * the iparam array should be of length = 3 and contain the discrimination, difficulty,
     * and guessing parameters, respectively.
     *
     * The order of parameters in iparam is as follows:
     * 1. Rasch model: iparam[0] = difficulty
     * 2. 2PL: iparam[0] = discrimination, iparam[1] = difficulty
     * 3. 3PL: iparam[1] = discrimination, iparam[1] = difficulty, iparam[2] = guessing
     *
     * @param theta person ability parameter.
     * @param iparam item parameter array that is one to three in length.
     * @param D scaling constant.
     * @return probability of a correct answer.
     */
    private double probRight(double theta, double[] iparam, double D){
        double z = 0;

        if(iparam.length==1){
            z = Math.exp(D*discrimination*(theta-iparam[0]));
            return  guessing+(slipping-guessing)*z/(1+z);
        }else if(iparam.length==2){
            z = Math.exp(D*iparam[0]*(theta-iparam[1]));
            return  guessing+(slipping-guessing)*z/(1+z);
        }else{
            z = Math.exp(D*iparam[0]*(theta-iparam[1]));
            return  iparam[2]+(slipping-iparam[2])*z/(1+z);
        }

    }

    private double probWrong(double theta, double[] iparam, double D){
        if(iparam.length==3 && iparam[2]<0) return 0;
        return 1.0 - probRight(theta, iparam, D);
    }

    private double probRight(double theta){
        double z = Math.exp(D*discrimination*(theta-difficulty));
        double prob = guessing + (slipping-guessing)*z/(1+z);
        return prob;
    }

    private double probWrong(double theta){
        return 1.0-probRight(theta);
    }

    /**
     * Computes the expected value
     * 
     * @param theta a person ability value.
     * @return
     */
    public double expectedValue(double theta){
        return scoreWeight[1]*probRight(theta);
    }


    /**
     * Compute probability of scoring at or above a response category
     *
     * @param theta a person ability value
     * @param category response category
     * @return
     */
    public double cumulativeProbability(double theta, int category){
        return this.probability(theta, category);
    }

//    public double[] gradientAt(double theta){
//        //Note: The second argument (-1) is not actually used by this class.
//        //It is here to satisfy the interface.
//        return gradientAt(theta, -1);
//    }

    /**
     * Computes the gradientAt (vector of first partial derivatives) with respect to the item parameters.
     * This method uses item parameters passed to the method. It does NOT use item parameters stored in the
     * object.
     *
     * Note: The second argument (int k) is not actually used by this class. It is here to satisfy the interface.
     *
     * @param theta person ability estimate.
     * @param iparam array of item parameters.
     * @param k response category
     * @param D scaling constant that is either 1 or 1.7
     * @return an array of first partial derivatives (i.e. the gradientAt).
     */
    public double[] gradient(double theta, double[] iparam, int k, double D){
        double a = 1;
        double b = 0;
        double c = 0;
        double u = slipping;
        double[] deriv = new double[numberOfParameters];

        //This gradient is based on the 3PL only
//        if(numberOfParameters==3){
//            a = iparam[0];
//            b = iparam[1];
//            c = iparam[2];
//        }else if(numberOfParameters==2){
//            a = iparam[0];
//            b = iparam[1];
//            c = guessing;
//        }else{
//            a = discrimination;
//            b = iparam[0];
//            c = guessing;
//        }
//
//        if(k==0){
//            double z = Math.exp(D*a*(theta-b));
//            double g = 1 + z;
//            double g2 = g*g;
//            double db = (1-c)*z*D*a/g2;//first derivative wrt difficulty
//
//            if(numberOfParameters==1){
//                deriv[0] = db;
//                return deriv;
//            }
//
//            deriv[0] = -(1-c)*z*D*(theta-b)/g2;//first derivative wrt discrimination
//            deriv[1] = db;
//
//            if(numberOfParameters==3){
//                deriv[2] = -1/(1+z); //first derivative wrt guessing
//            }
//
//        }else{
//
//            double t = Math.exp(-a*D*(theta-b));
//            double onept2 = 1.0 + t;
//            onept2 *= onept2;
//
//            //derivative with respect to b parameter
//            double derivb = -a*(1.0-c)*D*t;
//            derivb /= onept2;
//
//            if(numberOfParameters==1){
//                deriv[0] = derivb;
//                return deriv;
//            }
//
//            deriv[1] = derivb;
//
//            //derivative with respect to the a parameter
//            deriv[0] = (1.0 - c)*(theta - b)*D*t;
//            deriv[0] /= onept2;
//
//            //derivative with respect to c parameter
//            if(numberOfParameters==3){
//                deriv[2] = -1.0/(1.0 + t);
//                deriv[2] += 1.0;
//            }
//
//        }
//        return deriv;

        //=======================================================================
        //This gradient is based on the gradient for the 4PL. See Irm4PL.java

        if(numberOfParameters==3){
            a = iparam[0];
            b = iparam[1];
            c = iparam[2];
        }else if(numberOfParameters==2){
            a = iparam[0];
            b = iparam[1];
            c = guessing;
        }else{
            a = discrimination;
            b = iparam[0];
            c = guessing;
        }

        double w = D*(theta-b);
        double z = Math.exp(D*a*(theta-b));
        double z2 = z*z;
        double d = 1+z;
        double d2 = d*d;
        double xmc = u-c;

        if(numberOfParameters==3){
            deriv[0] = xmc*z*w/d - xmc*z2*w/d2;
            deriv[1] = -(xmc*z*D*a/d - xmc*z2*D*a/d2);
            deriv[2] = 1.0 - z/d;
            if(k==0){
                deriv[0] = -deriv[0];
                deriv[1] = -deriv[1];
                deriv[2] = -deriv[2];
            }
        }else if(numberOfParameters==2){
            deriv[0] = xmc*z*w/d - xmc*z2*w/d2;
            deriv[1] = -(xmc*z*D*a/d - xmc*z2*D*a/d2);
            if(k==0){
                deriv[0] = -deriv[0];
                deriv[1] = -deriv[1];
            }
        }else{
            deriv[0] = -(xmc*z*D*a/d - xmc*z2*D*a/d2);
            if(k==0){
                deriv[0] = -deriv[0];
            }
        }
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
        double[] iparam = {discrimination, difficulty, guessing};
        return gradient(theta, iparam, k, D);
    }

    /**
     * Hessian or matrix of second derivatives.
     *
     * @param theta person ability value.
     * @return a two-way array containing the Hessian matrix values.
     */
    public double[][] hessian(double theta){
        double[][] deriv = new double[numberOfParameters][numberOfParameters];
        double e = Math.exp(discrimination*D*(theta-difficulty));
        double onepe3 = 1.0 + e;
        double onepe2 = onepe3*onepe3;
        onepe3 *= onepe2;
        double d2 = D*D;
        double cm1 = guessing-1.0;
        double em1 = e - 1.0;

        //second derivative with respect to the b parameter
        double derivt = discrimination*discrimination;
        derivt *= cm1*d2;
        derivt *= e*em1;
        derivt /= onepe3;

        if(numberOfParameters==1){
            deriv[0][0] = derivt;
            return deriv;
        }
        deriv[1][1] = derivt;

        double bmt = difficulty - theta;

        //second derivative with respect to the a parameter
        derivt = cm1*d2;
        derivt *= e*em1;
        derivt *= bmt*bmt;
        deriv[0][0] = derivt/onepe3;

        //second derivative with respect to a and b
        double t2 = 1 + e;
        t2 += discrimination*D*em1*bmt;
        derivt = t2*cm1;
        derivt *= D*e;
        deriv[1][0] = derivt / onepe3;
        deriv[0][1] = deriv[1][0];

        if(numberOfParameters==3){
            //second derivative with respect to the c parameter
            deriv[2][2] = 0.0;
            double einv = 1.0/e;
            double onepeinv2 = 1.0 + einv;
            onepeinv2 *= onepeinv2;

            //second derivative with respect to a and c
            derivt = D * einv;
            derivt *= bmt;
            deriv[2][0] = derivt / onepeinv2;
            deriv[0][2] = deriv[2][0];

            //second derivative with respect to b and c
            derivt = discrimination * D * einv;
            deriv[2][1] = derivt / onepeinv2;
        }
        return deriv;
    }

    /**
     * From Equating recipes. Computes the derivative with respect to person ability.
     *
     * @param theta person ability value.
     * @param response item response value.
     * @return derivative wrt theta.
     */
    public double derivTheta2(double theta, int response){
        double z = Math.exp(D*discrimination*(theta-difficulty));

        //incorrect response
        if(response == 0) return ( -D*discrimination*(1.0-guessing)*z/((1.0+z)*(1.0+z)) );

        //correct response
        return ( D*discrimination*(1.0-guessing)*z/((1.0+z)*(1.0+z)) );
    }

    /**
     * Derivative from Mathematica. First derivative with respect to person ability.
     *
     * @param theta person ability value.
     * @return first derivative wrt theta.
     */
    public double derivTheta(double theta){
        double p1 = D*discrimination*(1-guessing)*Math.exp(2.0*D*discrimination*(theta-difficulty));
        double p2 = Math.pow(1+Math.exp(D*discrimination*(theta-difficulty)),2);
        double p3 = D*discrimination*(1.0-guessing)*Math.exp(D*discrimination*(theta-difficulty));
        double p4 = 1+Math.exp(D*discrimination*(theta-difficulty));
        double deriv = -p1/p2 + p3/p4;
        return deriv;
    }

    /**
     * Computes the item information function at theta.
     *
     * @param theta person ability value.
     * @return item information.
     */
    public double itemInformationAt(double theta){
        double p = probRight(theta);
        double part1 = Math.pow(p - guessing, 2);
        double part2 = Math.pow(slipping-guessing, 2);
        double a2 = discrimination*discrimination;
        double info = D*D*a2*(part1/part2)*((slipping-p)/p);
        return info;
    }

    public double[] nonZeroPrior(double[] param){
        double[] p = Arrays.copyOf(param, param.length);

        if(numberOfParameters==1){
            if(difficultyPrior!=null) p[0] = difficultyPrior.nearestNonZero(param[0]);
        }else if(numberOfParameters==2){
            if(discriminationPrior!=null) p[0] = discriminationPrior.nearestNonZero(param[0]);
            if(difficultyPrior!=null) p[1] = difficultyPrior.nearestNonZero(param[1]);
        }else{
            if(discriminationPrior!=null) p[0] = discriminationPrior.nearestNonZero(param[0]);
            if(difficultyPrior!=null) p[1] = difficultyPrior.nearestNonZero(param[1]);
            if(guessingPrior!=null) p[2] = guessingPrior.nearestNonZero(param[2]);
        }
        return p;
    }

    public void setDiscriminationPrior(ItemParamPrior discriminationPrior){
        this.discriminationPrior = discriminationPrior;
    }

    public void setDifficultyPrior(ItemParamPrior difficultyPrior){
        this.difficultyPrior = difficultyPrior;
    }

    public void setGuessingPrior(ItemParamPrior guessingPrior){
        this.guessingPrior = guessingPrior;
    }

    public void setSlippingPrior(ItemParamPrior slippingPrior){

    }

    public void setStepPriorAt(ItemParamPrior prior, int index){

    }

    public ItemParamPrior getDiscriminationPrior(){
        return discriminationPrior;
    }

    public ItemParamPrior getDifficultyPrior(){
        return difficultyPrior;
    }

    public ItemParamPrior getGuessingPrior(){
        return guessingPrior;
    }

    public double addPriorsToLogLikelihood(double loglike, double[] iparam){
//        double priorProb = 0.0;
        double ll = loglike;

        if(numberOfParameters==3){
            if(discriminationPrior!=null){
                ll += discriminationPrior.logDensity(iparam[0]);
            }

            if(difficultyPrior!=null){
                ll += difficultyPrior.logDensity(iparam[1]);
            }

            if(guessingPrior!=null){
                ll += guessingPrior.logDensity(iparam[2]);
            }

        }else if(numberOfParameters==2){
            if(discriminationPrior!=null){
                ll += discriminationPrior.logDensity(iparam[0]);
            }

            if(difficultyPrior!=null){
                ll += difficultyPrior.logDensity(iparam[1]);
            }
        }else{
            if(difficultyPrior!=null){
                ll += difficultyPrior.logDensity(iparam[0]);
            }
        }

        return ll;
    }

    public double[] addPriorsToLogLikelihoodGradient(double[] loglikegrad, double[] iparam){
        double[] llg = loglikegrad;

        if(numberOfParameters==3){
            if(discriminationPrior!=null) {
                llg[0] -= discriminationPrior.logDensityDeriv1(iparam[0]);
            }

            if(difficultyPrior!=null) {
                llg[1] -= difficultyPrior.logDensityDeriv1(iparam[1]);
            }

            if(guessingPrior!=null) {
                llg[2] -= guessingPrior.logDensityDeriv1(iparam[2]);
            }
        }else if(numberOfParameters==2){
            if(discriminationPrior!=null) {
                llg[0] -= discriminationPrior.logDensityDeriv1(iparam[0]);
            }

            if(difficultyPrior!=null) {
                llg[1] -= difficultyPrior.logDensityDeriv1(iparam[1]);
            }
        }else{
            if(difficultyPrior!=null) {
                llg[0] -= difficultyPrior.logDensityDeriv1(iparam[0]);
            }
        }
        return llg;
    }


//=======================================================================================================================
// Methods related to scale linking
//=======================================================================================================================

    /**
     * Mean/sigma linking coefficients are computed from the mean and standard deviation of item difficulty.
     * The summary statistics are computed in a storeless manner. This method allows for the incremental
     * update to item difficulty summary statistics by combining them with other summary statistics.
     *
     * @param mean item difficulty mean.
     * @param sd item difficulty standard deviation.
     */
    public void incrementMeanSigma(Mean mean, StandardDeviation sd){
        mean.increment(difficulty);
        sd.increment(difficulty);
    }

    /**
     * Mean/mean linking coefficients are computed from the mean item difficulty and mean item discrimination.
     * The summary statistics are computed in a storeless manner. This method allows for the incremental
     * update to item difficulty summary statistics by combining them with other summary statistics.
     *
     * @param meanDiscrimination item discrimination mean.
     * @param meanDifficulty item difficulty mean.
     */
    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty){
        meanDiscrimination.increment(discrimination);
        meanDifficulty.increment(difficulty);
    }

    /**
     * Computes probability of a response under a linear transformation. This method is mainly used for the
     * characteristic curve linking methods (see {@link com.itemanalysis.psychometrics.irt.equating.StockingLordMethod}).
     * It applies the linear transformation such that the New form is transformed to the Old Form.
     *
     * @param theta examinee proficiency value
     * @param response target category
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return
     */
    public double tStarProbability(double theta, int response, double intercept, double slope){
        double[] iparam = new double[numberOfParameters];
        if(numberOfParameters==3){
            iparam[0] = discrimination/slope;
            iparam[1] = difficulty*slope+intercept;
            iparam[2] = guessing;
        }else if(numberOfParameters==2){
            iparam[0] = discrimination/slope;
            iparam[1] = difficulty*slope+intercept;
        }else{
            iparam[0] = difficulty+intercept;//Rasch model
        }

        if(response==1){
            return probRight(theta, iparam, D);

        }else{
            return 1.0-probRight(theta, iparam, D);
        }

    }

    /**
     * Computes probability of a response under a linear transformation. This method is mainly used for the
     * characteristic curve linking methods (see {@link com.itemanalysis.psychometrics.irt.equating.StockingLordMethod}).
     * It applies the linear transformation such that the Old form is transformed to the New Form.
     *
     * @param theta examinee proficiency value
     * @param response target category
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return
     */
    public double tSharpProbability(double theta, int response, double intercept, double slope){
        double[] iparam = new double[numberOfParameters];
        if(numberOfParameters==3){
            iparam[0] = discrimination*slope;
            iparam[1] = (difficulty - intercept)/slope;
            iparam[2] = guessing;
        }else if(numberOfParameters==2){
            iparam[0] = discrimination*slope;
            iparam[1] = (difficulty - intercept)/slope;
        }else{
            iparam[0] = (difficulty - intercept);//Rasch model
        }

        if(response==1){
            return probRight(theta, iparam, D);
        }else{
            return 1.0-probRight(theta, iparam, D);
        }

    }

    /**
     * Computes item expected value under a linear transformation. This method is mainly used for the characteristic
     * curve linking methods (see {@link com.itemanalysis.psychometrics.irt.equating.StockingLordMethod}).
     * It applies the linear transformation such that the New form is transformed to the Old Form.
     *
     * @param theta person ability value
     * @param intercept intercept linking coefficient.
     * @param slope slope linking coefficient.
     * @return expected value under a linear transformation.
     */
    public double tStarExpectedValue(double theta, double intercept, double slope){
        return tStarProbability(theta, 1, intercept, slope);
    }

    /**
     * Computes probability of a response under a linear transformation. This method is mainly used for the
     * characteristic curve linking methods (see {@link com.itemanalysis.psychometrics.irt.equating.StockingLordMethod}).
     * It applies the linear transformation such that the Old form is transformed to the New Form.
     *
     * @param theta examinee proficiency value
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return expected value under a linear transformation.
     */
    public double tSharpExpectedValue(double theta, double intercept, double slope){
        return tSharpProbability(theta, 1, intercept, slope);
    }

    /**
     * Gets the number of item parameters.
     *
     * @return number of item parameters.
     */
    public int getNumberOfParameters(){
        return numberOfParameters;
    }

    public int getNumberOfEstimatedParameters(){
        if(isFixed) return 0;
        return numberOfParameters;
    }

    /**
     * Linear transformation of item parameters.
     *
     * @param intercept intercept transformation coefficient.
     * @param slope slope transformation coefficient.
     */
    public void scale(double intercept, double slope){
        if(isFixed) return;//DO NOT transform the item parameters when they are fixed
        difficulty = intercept + slope*difficulty;
        difficultyStdError *= slope;

        //Do NOT transform discrimination parameter if it is a Rasch model because discrimination is fixed to 1.
        if(!raschModel){
            discrimination = discrimination/slope;
            discriminationStdError *= slope;
        }

    }

//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//

    public double[] getItemParameterArray(){
        double[] ip = new double[numberOfParameters];
        if(numberOfParameters==1){
            ip[0] = difficulty;
        }else if(numberOfParameters==2){
            ip[0] = discrimination;
            ip[1] = difficulty;
        }else{
            ip[0] = discrimination;
            ip[1] = difficulty;
            ip[2] = guessing;
        }
        return ip;
    }

    public void setStandardErrors(double[] x){
        if(numberOfParameters==1){
            difficultyStdError = x[0];
        }else if(numberOfParameters==2){
            discriminationStdError = x[0];
            difficultyStdError = x[1];
        }else{
            discriminationStdError = x[0];
            difficultyStdError = x[1];
            guessingStdError = x[2];
        }
    }

    /**
     * Gets the type of item response model.
     *
     * @return type of item response model.
     */
    public IrmType getType(){
        return IrmType.L3;//TODO should this be more specific and indicate a 2pl or Rasch model when those models are used?
    }

    /**
     * Gets the item difficulty parameter.
     *
     * @return item difficulty.
     */
    public double getDifficulty(){
        return difficulty;
    }

    /**
     * Set difficulty parameter to an existing value. If you are using this method to fix an item parameter during
     * estimation, you must also set the proposal value in {@link #setProposalDifficulty(double)}.
     *
     */
    public void setDifficulty(double difficulty){
        this.difficulty = difficulty;
    }

    /**
     * A proposal difficulty value is obtained during each iteration of the estimation routine. This method gets
     * the proposal item difficulty values. This method is needed for estimating item difficulty.
     *
     * @return proposed item difficulty value.
     */
    public double getProposalDifficulty(){
        return proposalDifficulty;
    }

    /**
     * A proposal difficulty value is obtained during each iteration of the estimation routine. This method
     * sets the proposal value.
     *
     * @param proposalDifficulty proposed item difficulty value.
     */
    public void setProposalDifficulty(double proposalDifficulty){
        if(!isFixed) this.proposalDifficulty = proposalDifficulty;
    }

    /**
     * Gets the item difficulty standard error.
     *
     * @return item difficulty standard error.
     */
    public double getDifficultyStdError(){
        return difficultyStdError;
    }

    /**
     * Item difficulty standard error may be computed external to the class. This method sets the difficulty
     * standard error to a computed value.
     *
     * @param stdError item difficulty standard error.
     */
    public void setDifficultyStdError(double stdError){
        difficultyStdError = stdError;
    }

    /**
     * Gets item discrimination.
     *
     * @return item discrimination.
     */
    public double getDiscrimination(){
        return discrimination;
    }

    /**
     * Set discrimination parameter to an existing value. If you are using this method to fix an item parameter
     * during estimation, you must also set the proposal value with {@link #setProposalDiscrimination(double)}.
     *
     */
    public void setDiscrimination(double discrimination){
        this.discrimination = discrimination;
    }

    public double getProposalDiscrimination(){
        return proposalDiscrimination;
    }

    /**
     * Set the proposed discrimination estimate.
     *
     * @param discrimination proposed item discrimination value.
     */
    public void setProposalDiscrimination(double discrimination){
        if(!isFixed) this.proposalDiscrimination = discrimination;
    }

    /**
     * Gets the standard error for the item discrimination estimate.
     *
     * @return item discrimination standard error.
     */
    public double getDiscriminationStdError(){
        return discriminationStdError;
    }

    /**
     * The standard error may be computed external to the class. It can be set to a specific value with this method.
     *
     * @param stdError item discrimination standard error.
     */
    public void setDiscriminationStdError(double stdError){
        discriminationStdError = stdError;
    }

    /**
     * Gets the pseudo-guessing (i.e. lower asymptote) parameter.
     *
     * @return guessing parameter.
     */
    public double getGuessing(){
        return guessing;
    }

    /**
     * Set lower asymptote parameter to an existing value. If you are using this method to fix an item parameter
     * during estimation, you must also set the proposal value in {@link #setProposalGuessing(double)}.
     *
     */
    public void setGuessing(double guessing){
        this.guessing = guessing;
    }

    /**
     * A proposal guessing parameter value is obtained during each iteration of the estimation routine. This method
     * sets the proposal value.
     *
     * @param guessing proposed guessing parameter estimate.
     */
    public void setProposalGuessing(double guessing){
        if(!isFixed) this.proposalGuessing = guessing;
    }

    /**
     * Gets the guessing parameter estimate standard error.
     *
     * @return guessing parameter estimate standard error.
     */
    public double getGuessingStdError(){
        return guessingStdError;
    }

    /**
     * The guessing parameter standard error may be computed external to the class. Use this method to set the
     * standard error to a particular value.
     *
     * @param stdError standard error for the guessing parameter estimate.
     */
    public void setGuessingStdError(double stdError){
        guessingStdError = stdError;
    }

    public void setSlipping(double slipping){
        this.slipping = slipping;
    }

    public void setProposalSlipping(double slipping){
        this.proposalSlipping = slipping;
    }

    public void setSlippingStdError(double slipping){

    }

    public double getSlipping(){
        return slipping;
    }

    public double getSlippingStdError(){
        return Double.NaN;
    }

    public double getScalingConstant(){
        return D;
    }

    /**
     * Proposal values for every item parameter are obtained at each iteration of the estimation routine. The
     * proposal values for each parameters are obtained for each in turn using the estimated values from the
     * previous iteration. For example, a proposal difficulty estimate for itemA is obtained in iteration k+1
     * using estimates from iteration k. Then, a proposal difficulty estimate for itemB is obtained in iteration k+1
     * using estimates from iteration k (even though a new estimate exists for itemA). After obtaining proposal
     * values for every item on the test, the proposal values can be accepted as the new parameter estimates. This
     * method must be called to accept the proposal values as the new estimates.
     *
     * Returns the maximum relative absolute difference between existing parameters and new parameters.
     *
     */
    public double acceptAllProposalValues(){
        if(isFixed) return 0;

        double max = 0;

        double delta = Math.abs(this.difficulty - proposalDifficulty);
        if(proposalDifficulty>=1) delta /= proposalDifficulty;
        max = Math.max(max, delta);
        this.difficulty = proposalDifficulty;

        if(numberOfParameters>=2){
            delta = Math.abs(this.discrimination - proposalDiscrimination);
            if(proposalDiscrimination>=1) delta /= proposalDiscrimination;
            max = Math.max(max, delta);
            this.discrimination = proposalDiscrimination;
        }

        if(numberOfParameters==3){
            delta = Math.abs(this.guessing-proposalGuessing);
            if(proposalGuessing>=1) delta /= proposalGuessing;
            max = Math.max(max, delta);
            this.guessing = proposalGuessing;
        }

        return max;
    }


    public double[] getStepParameters(){
        double[] sp = new double[ncat];
        for(int k=0;k<ncat;k++){
            sp[k] = Double.NaN;
        }
        return sp;
    }

    public void setStepStdError(double[] stdError){

    }

    public double[] getStepStdError(){
        double[] sp = new double[ncat];
        for(int k=0;k<ncat;k++){
            sp[k] = Double.NaN;
        }
        return sp;
    }

    public double[] getThresholdParameters(){
        double[] t = {0};
        return t;
    }

    public double[] getThresholdStdError(){
        double[] sp = new double[ncat];
        for(int k=0;k<ncat;k++){
            sp[k] = Double.NaN;
        }
        return sp;
    }

    public void setThresholdStdError(double[] stdError){

    }

    public void setStepParameters(double[] step){

    }

    public void setProposalStepParameters(double[] step){

    }

    public void setThresholdParameters(double[] thresholdParameters){

    }

    public void setProposalThresholds(double[] thresholds){

    }
//=====================================================================================================================//
// END GETTER AND SETTER METHODS                                                                                       //
//=====================================================================================================================//


    /**
     * A string representation of the item parameters. Mainly used for printing and debugging.
     *
     * @return a string of item parameters.
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        String name = getName().toString();
        if(getName()!=null){
            name = getName().toString().substring(0, Math.min(18, name.length()));
        }else{
            name = "";
        }
        f.format("%-18s", name);f.format("%2s", "");

        String m = "";
        if(numberOfParameters==3){
            m = "L3";
        }else if(numberOfParameters==2){
            m = "L2";
        }else{
            m = "L1";
        }
        f.format("%-3s", m);f.format("%4s", "");

        f.format("% 4.2f", getDiscrimination()); f.format("%1s", "");
        f.format("(%4.2f)", getDiscriminationStdError()); f.format("%4s", "");

        f.format("% 4.2f", getDifficulty()); f.format("%1s", "");
        f.format("(%4.2f)", getDifficultyStdError()); f.format("%4s", "");

        if((numberOfParameters<3 && getGuessing()>0) || numberOfParameters==3){
            f.format("% 4.2f", getGuessing()); f.format("%1s", "");
            f.format("(%4.2f)", getGuessingStdError()); f.format("%4s", "");
        }else{
            f.format("%13s", "");
        }


        if(getSlipping()<1) {
            f.format("% 4.2f", getSlipping());  f.format("%1s", "");
            f.format("(%4.2f)", getSlippingStdError());  f.format("%4s", "");
        }
        return f.toString();


//        //OLD==================================================================
//        String name = "";
//        if(getName()!=null){
//            name = getName().toString();
//        }
//
//        f.format("%10s", name);f.format("%2s", ": ");
//        f.format("%1s", "[");
//        f.format("% .6f", getDiscrimination()); f.format("%2s", ", ");
//        f.format("% .6f", getDifficulty()); f.format("%2s", ", ");
//        f.format("% .6f", getGuessing());
//
//        if(getSlipping()<1) {
//            f.format("%2s", ", ");
//            f.format("% .6f", getSlipping());
//        }
//        f.format("%1s", "]");
//        f.format("%n");
//        f.format("%10s", "");f.format("%2s", "");
//        f.format("%1s", "(");
//        f.format("% .6f", getDiscriminationStdError()); f.format("%2s", ", ");
//        f.format("% .6f", getDifficultyStdError()); f.format("%2s", ", ");
//        f.format("% .6f", getGuessingStdError());
//        if(getSlipping()<1){
//            f.format("%2s", ", ");
//            f.format("% .6f", getSlippingStdError());
//        }
//        f.format("%1s", ")");
//
//        return f.toString();
    }


}

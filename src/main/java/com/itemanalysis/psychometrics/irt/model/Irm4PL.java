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
package com.itemanalysis.psychometrics.irt.model;

import com.itemanalysis.psychometrics.irt.estimation.ItemParamPrior;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.Formatter;

/**
 * Four parameter logistic model.
 */
public class Irm4PL extends AbstractItemResponseModel {

    private int numberOfParameters = 4;
    private double discrimination = 1;
    private double difficulty = 0;
    private double guessing = 0;
    private double slipping = 0;
    private double D = 1.7;
    private double proposalDiscrimination = 1;
    private double proposalDifficulty = 0;
    private double proposalGuessing = 0;
    private double proposalSlipping = 0;
    private double discriminationStdError = 1;
    private double difficultyStdError = 0;
    private double guessingStdError = 0;
    private double slippingStdError = 0;
    private ItemParamPrior discriminationPrior = null;
    private ItemParamPrior difficultyPrior = null;
    private ItemParamPrior guessingPrior = null;
    private ItemParamPrior slippingPrior = null;

    /**
     * Constructor for four parameter logistic model
     *
     * @param discrimination item discrimination parameter
     * @param difficulty item difficulty parameter
     * @param guessing lower-asymptote parameter
     * @param slipping upper-asymptote parameter
     * @param D scaling factor that is either 1, 1.7 or 1.702
     */
    public Irm4PL(double discrimination, double difficulty, double guessing, double slipping, double D){
        this.discrimination = discrimination;
        this.difficulty = difficulty;
        this.guessing = guessing;
        this.slipping = slipping;
        this.proposalDiscrimination = discrimination;
        this.proposalDifficulty = difficulty;
        this.proposalGuessing = guessing;
        this.proposalSlipping = slipping;
        this.D = D;
        this.numberOfParameters = 4;
        this.ncat = 2;
        defaultScoreWeights();
    }

    /**
     * Computes probability of a correct response using value provided to the method, not the parameters 
     * stored in the object. Probability of a correct response. The order of the parameters in the iparam array is:
     * iparam[0] = discrimination, iparam[1] = difficulty, iparam[2] = guessing, iparam[3] = slipping.
     * 
     * @param theta person ability parameter.
     * @param iparam array of item parameters. The order is important and will be unique to each implementation of the interface.
     * @param response an item response category.
     * @param D a scaling constant that is either 1 or 1.7 or 1.702.
     * @return
     */
    public double probability(double theta, double[] iparam, int response, double D){
        if(response==1){
            return probRight(theta, iparam, D);
        }else{
            return probWrong(theta, iparam, D);
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

    private double probRight(double theta, double[] iparam, double D){
        if(iparam[0]<0) return 0;
        double z = Math.exp(D*iparam[0]*(theta-iparam[1]));
        return  iparam[2]+(iparam[3]-iparam[2])*z/(1+z);
    }

    private double probWrong(double theta, double[] iparam, double D){
        if(iparam[0]<0) return 0;
        return 1.0 - probRight(theta, iparam, D);
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
     * Compute probability of scoring at or above a response category
     *
     * @param theta a person ability value
     * @param category response category
     * @return
     */
    public double cumulativeProbability(double theta, int category){
        return this.probability(theta, category);
    }

    /**
     * Computes the gradientAt (vector of first partial derivatives) with respect to the item parameters.
     * This method uses item parameters passed to the method. It does NOT use item parameters stored in the
     * object.
     *
     * @param theta person ability estimate.
     * @param iparam array of item parameters.
     * @param k response category
     * @param D scaling constant that is either 1 or 1.7
     * @return an array of first partial derivatives (i.e. the gradientAt).
     */
    public double[] gradient(double theta, double[] iparam, int k, double D){
        double[] deriv = new double[numberOfParameters];

        double a = iparam[0];
        double b = iparam[1];
        double c = iparam[2];
        double x = iparam[3];

        double w = D*(theta-b);
        double z = Math.exp(D*a*(theta-b));
        double z2 = z*z;
        double d = 1+z;
        double d2 = d*d;
        double xmc = x-c;

        deriv[0] = xmc*z*w/d - xmc*z2*w/d2;
        deriv[1] = -(xmc*z*D*a/d - xmc*z2*D*a/d2);
        deriv[2] = 1.0 - z/d;
        deriv[3] = z/d;

        if(k==0){
            deriv[0] = -deriv[0];
            deriv[1] = -deriv[1];
            deriv[2] = -deriv[2];
            deriv[3] = -deriv[3];
        }

        return deriv;
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

    public double[] nonZeroPrior(double[] param){
        double[] p = Arrays.copyOf(param, param.length);
        if(discriminationPrior!=null) p[0] = discriminationPrior.nearestNonZero(param[0]);
        if(difficultyPrior!=null) p[1] = difficultyPrior.nearestNonZero(param[1]);
        if(guessingPrior!=null) p[2] = guessingPrior.nearestNonZero(param[2]);
        if(slippingPrior!=null) p[3] = slippingPrior.nearestNonZero(param[3]);
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
        this.slippingPrior = slippingPrior;
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

    public ItemParamPrior getSlippingPrior(){
        return slippingPrior;
    }

    /**
     * This method is used for marginal maximum likelihood estimation. It adds the logdensity 
     * of a parameter to the loglikelihood. 
     * 
     * @param loglike value of the loglikelihood function
     * @param iparam an item parameter array in a specific order.
     * @return
     */
    public double addPriorsToLogLikelihood(double loglike, double[] iparam){
        double priorProb = 0.0;
        double ll = loglike;

        //discrimination prior
        if(discriminationPrior!=null){
            priorProb = discriminationPrior.logDensity(iparam[0]);
            ll += priorProb;
        }

        //difficulty prior
        if(difficultyPrior!=null){
            priorProb = difficultyPrior.logDensity(iparam[1]);
            ll += priorProb;
        }

        //guessing prior
        if(guessingPrior!=null){
            priorProb = guessingPrior.logDensity(iparam[2]);
            ll += priorProb;
        }

        //slipping prior
        if(slippingPrior!=null){
            priorProb = slippingPrior.logDensity(iparam[3]);
            ll += priorProb;
        }

        return ll;

    }

    /**
     * This method is used for marginal maximum likelihood estimation. It adds the logdensity 
     * of a parameter to the loglikelihood gradient. 
     *
     * @param loglikegrad values of the loglikelihood gradient function
     * @param iparam an item parameter array in a specific order.
     * @return
     */
    public double[] addPriorsToLogLikelihoodGradient(double[] loglikegrad, double[] iparam){
        double[] llg = loglikegrad;

        if(discriminationPrior!=null) {
            llg[0] -= discriminationPrior.logDensityDeriv1(iparam[0]);
        }

        if(difficultyPrior!=null) {
            llg[1] -= difficultyPrior.logDensityDeriv1(iparam[1]);
        }

        if(guessingPrior!=null) {
            llg[2] -= guessingPrior.logDensityDeriv1(iparam[2]);
        }

        if(slippingPrior!=null) {
            llg[3] -= slippingPrior.logDensityDeriv1(iparam[3]);
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
    public void incrementMeanSigma(Mean mean, StandardDeviation sd){//TODO check for correctness
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
    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty){//TODO check for correctness
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
    public double tStarProbability(double theta, int response, double intercept, double slope){//TODO check for correctness
        if(response==1) return tStarProbRight(theta, intercept, slope);
        return tStarProbWrong(theta, intercept, slope);
    }

    private double tStarProbRight(double theta, double intercept, double slope){//TODO check for correctness
        double a = discrimination/slope;
        double b = difficulty*slope+intercept;
        double top = Math.exp(D*a*(theta-b));
        double prob = slipping + (guessing-slipping)*top/(1+top);
        return prob;
    }

    private double tStarProbWrong(double theta, double intercept, double slope){//TODO check for correctness
        return 1.0-tStarProbRight(theta, intercept, slope);
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
    public double tSharpProbability(double theta, int response, double intercept, double slope){//TODO check for correctness
        if(response==1) return tSharpProbRight(theta, intercept, slope);
        return tSharpProbWrong(theta, intercept, slope);
    }

    private double tSharpProbRight(double theta, double intercept, double slope){//TODO check for correctness
        double a = discrimination*slope;
        double b = (difficulty - intercept)/slope;
        double top = Math.exp(D*a*(theta-b));
        double prob = slipping + (guessing-slipping)*top/(1+top);
        return prob;
    }

    private double tSharpProbWrong(double theta, double intercept, double slope){//TODO check for correctness
        return 1.0-tSharpProbRight(theta, intercept, slope);
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
    public double tStarExpectedValue(double theta, double intercept, double slope){//TODO check for correctness
        return tStarProbRight(theta, intercept, slope);
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
    public double tSharpExpectedValue(double theta, double intercept, double slope){//TODO check for correctness
        return tSharpProbRight(theta, intercept, slope);
    }

    /**
     * Linear transformation of item parameters.
     *
     * @param intercept intercept transformation coefficient.
     * @param slope slope transformation coefficient.
     */
    public void scale(double intercept, double slope){//TODO check for correctness
        if(isFixed) return;//DO NOT transform the item parameters when they are fixed
        difficulty = intercept + slope*difficulty;
        discrimination = discrimination/slope;
        difficultyStdError *= slope;
        discriminationStdError *= slope;
    }


//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//

    public double[] getItemParameterArray(){
        double[] ip = new double[numberOfParameters];
        ip[0] = discrimination;
        ip[1] = difficulty;
        ip[2] = guessing;
        ip[3] = slipping;
        return ip;
    }

    public void setStandardErrors(double[] x){
        discriminationStdError = x[0];
        difficultyStdError = x[1];
        guessingStdError = x[2];
        slippingStdError = x[3];
    }

    /**
     * Gets the type of item response model.
     *
     * @return type of item response model.
     */
    public IrmType getType(){
        return IrmType.L4;
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
        this.proposalDifficulty = difficulty;
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
        this.proposalDiscrimination = discrimination;
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
        this.proposalGuessing = guessing;
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

    /**
     * Gets the slipping (i.e. upper asymptote) parameter.
     *
     * @return guessing parameter.
     */
    public double getSlipping(){
        return slipping;
    }

    /**
     * Set upper asymptote parameter to an existing value. If you are using this method to fix an item parameter
     * during estimation, you must also set the proposal value in {@link #setProposalGuessing(double)}.
     *
     */
    public void setSlipping(double slipping){
        this.slipping = slipping;
        this.proposalSlipping = slipping;
    }

    /**
     * A proposal slipping parameter value is obtained during each iteration of the estimation routine. This method
     * sets the proposal value.
     *
     * @param slipping proposed slipping parameter estimate.
     */
    public void setProposalSlipping(double slipping){
        if(!isFixed) this.proposalSlipping = slipping;
    }

    /**
     * Gets the guessing parameter estimate standard error.
     *
     * @return guessing parameter estimate standard error.
     */
    public double getSlippingStdError(){
        return slippingStdError;
    }

    /**
     * The guessing parameter standard error may be computed external to the class. Use this method to set the
     * standard error to a particular value.
     *
     * @param stdError standard error for the slipping parameter estimate.
     */
    public void setSlippingStdError(double stdError){
        slippingStdError = stdError;
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
     */
    public double acceptAllProposalValues(){
        if(isFixed) return 0;
        double max = 0;
        double delta = 0;

        delta = Math.abs(this.discrimination - proposalDiscrimination);
        if(proposalDiscrimination>=1) delta /= proposalDiscrimination;
        max = Math.max(max, delta);
        this.discrimination = proposalDiscrimination;

        delta = Math.abs(this.difficulty - proposalDifficulty);
        if(proposalDifficulty>=1) delta /= proposalDifficulty;
        max = Math.max(max, delta);
        this.difficulty = proposalDifficulty;

        delta = Math.abs(this.guessing-proposalGuessing);
        if(proposalGuessing>=1) delta /= proposalGuessing;
        max = Math.max(max, delta);
        this.guessing = proposalGuessing;

        delta = Math.abs(this.slipping-proposalSlipping);
        if(proposalSlipping>=1) delta /= proposalSlipping;
        max = Math.max(max, delta);
        this.slipping = proposalSlipping;

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
     * A string representaiton of the item parameters. Mainly used for printing and debugging.
     *
     * @return a string of item parameters.
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        String name = "";
        if(getName()!=null){
            name = getName().toString();
        }
        f.format("%-18s", name);f.format("%2s", "");

        f.format("%-3s", "L4");f.format("%4s", "");

        f.format("% 4.2f", getDiscrimination()); f.format("%1s", "");
        f.format("(%4.2f)", getDiscriminationStdError()); f.format("%4s", "");

        f.format("% 4.2f", getDifficulty()); f.format("%1s", "");
        f.format("(%4.2f)", getDifficultyStdError()); f.format("%4s", "");

        f.format("% 4.2f", getGuessing()); f.format("%1s", "");
        f.format("(%4.2f)", getGuessingStdError()); f.format("%4s", "");

        f.format("% 4.2f", getSlipping());  f.format("%1s", "");
        f.format("(%4.2f)", getSlippingStdError());  f.format("%4s", "");

        return f.toString();

//
//
//        StringBuilder sb = new StringBuilder();
//        Formatter f = new Formatter(sb);
//
//        f.format("%10s", getName().toString());f.format("%2s", ": ");
//        f.format("%1s", "[");
//        f.format("% .6f", getDiscrimination()); f.format("%2s", ", ");
//        f.format("% .6f", getDifficulty()); f.format("%2s", ", ");
//        f.format("% .6f", getGuessing()); f.format("%2s", ", ");
//        f.format("% .6f", getSlipping());f.format("%1s", "]");
//        f.format("%n");
//        f.format("%10s", "");f.format("%2s", "");
//        f.format("%1s", "(");
//        f.format("% .6f", getDiscriminationStdError()); f.format("%2s", ", ");
//        f.format("% .6f", getDifficultyStdError()); f.format("%2s", ", ");
//        f.format("% .6f", getGuessingStdError()); f.format("%2s", ", ");
//        f.format("% .6f", getSlippingStdError());f.format("%1s", ")");
//        return f.toString();
    }


}

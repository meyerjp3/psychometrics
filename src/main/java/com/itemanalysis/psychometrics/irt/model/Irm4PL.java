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

import java.util.Formatter;

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
     * Constructor for three parameter logistic model
     *
     * @param discrimination item discrimination parameter
     * @param difficulty item difficulty parameter
     * @param guessing lower-asymptote parameter
     * @param slipping upper-asymptote parameter
     * @param D scaling factor
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

    public double probability(double theta, double[] iparam, int response, double D){
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
        return Math.min(1.0, Math.max(0.0, prob)); //always return value between 0 and 1
    }

    private double probRight(double theta, double[] iparam, double D){
        double z = Math.exp(D*iparam[0]*(theta-iparam[1]));
        return  iparam[2]+(iparam[3]-iparam[2])*z/(1+z);
    }

    private double probWrong(double theta, double[] iparam, double D){
        return 1.0 - probRight(theta, iparam, D);
    }

    private double probRight(double theta){
        double top = Math.exp(D*discrimination*(theta-difficulty));
        double prob = guessing + (slipping-guessing)*top/(1+top);
        return prob;
    }

    private double probWrong(double theta){
        return 1.0-probRight(theta);
    }

    public double expectedValue(double theta){
        return scoreWeight[1]*probRight(theta);
    }

    /**
     * Computes the gradientAt (vector of first partial derivatives) with respect to the item parameters.
     * This method uses item parameters passed to teh method. It does NOT use item parameters stored in the
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
     * Computes gradientAt using item parameters stored in teh object.
     *
     * @param theta person ability value
     * @param k response category
     * @return gradientAt
     */
    public double[] gradient(double theta, int k){
        double[] iparam = {discrimination, difficulty, guessing, slipping};
        return gradient(theta, iparam, k, D);
    }

    public double derivTheta(double theta){
        throw new UnsupportedOperationException("Deriv theta not yet implemented");
    }

    public double itemInformationAt(double theta){
        throw new UnsupportedOperationException("Item information not yet implemented");
    }

    public void setDiscriminationPrior(ItemParamPrior discriminationPrior){
        this.discriminationPrior = discriminationPrior;
    }

    public void setDifficultyPrior(ItemParamPrior difficultyPrior){
        this.difficultyPrior = difficultyPrior;
    }

    public void setguessingPrior(ItemParamPrior guessingPrior){
        this.guessingPrior = guessingPrior;
    }

    public void setSlippingPrior(ItemParamPrior slippingPrior){
        this.slippingPrior = slippingPrior;
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
        double max = 0;

        max = Math.max(max, this.difficulty - proposalDifficulty);
        this.difficulty = proposalDifficulty;

        max = Math.max(max, this.discrimination - proposalDiscrimination);
        this.discrimination = proposalDiscrimination;

        max = Math.max(max, Math.abs(this.guessing-proposalGuessing));
        this.guessing = proposalGuessing;

        max = Math.max(max, Math.abs(this.slipping-proposalSlipping));
        this.slipping = proposalSlipping;

        return max;
    }


    public double[] getStepParameters(){
        throw new UnsupportedOperationException();
    }

    public void setStepStdError(double[] stdError){
        throw new UnsupportedOperationException();
    }

    public double[] getStepStdError(){
        throw new UnsupportedOperationException();
    }

    public double[] getThresholdParameters(){
        double[] t = {0};
        return t;
    }

    public double[] getThresholdStdError(){
        throw new UnsupportedOperationException();
    }

    public void setThresholdStdError(double[] stdError){
        throw new UnsupportedOperationException();
    }

    public void setStepParameters(double[] step){
        throw new UnsupportedOperationException();
    }

    public void setProposalStepParameters(double[] step){
        throw new UnsupportedOperationException();
    }

    public void setThresholdParameters(double[] thresholdParameters){
        throw new UnsupportedOperationException();
    }

    public void setProposalThresholds(double[] thresholds){
        throw new UnsupportedOperationException();
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

        f.format("%10s", getName().toString());f.format("%2s", ": ");
        f.format("%1s", "[");
        f.format("% .6f", getDiscrimination()); f.format("%2s", ", ");
        f.format("% .6f", getDifficulty()); f.format("%2s", ", ");
        f.format("% .6f", getGuessing()); f.format("%2s", ", ");
        f.format("% .6f", getSlipping());f.format("%1s", "]");
        f.format("%n");
        f.format("%10s", "");f.format("%2s", "");
        f.format("%1s", "(");
        f.format("% .6f", getDiscriminationStdError()); f.format("%2s", ", ");
        f.format("% .6f", getDifficultyStdError()); f.format("%2s", ", ");
        f.format("% .6f", getGuessingStdError()); f.format("%2s", ", ");
        f.format("% .6f", getSlippingStdError());f.format("%1s", ")");
        return f.toString();
    }


}

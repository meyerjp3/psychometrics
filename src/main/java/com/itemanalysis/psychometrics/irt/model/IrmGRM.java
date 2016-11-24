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
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;

/**
 * Samejima's graded response model.
 *
 * TODO currently uses an array of m-1 step parameters for an item with m categories. Change to array of m steps when use this for estimation.
 */
public class IrmGRM extends AbstractItemResponseModel {

    private double discrimination = 1.0;
    private double proposalDiscrimination = 1.0;
    private double discriminationStdError = 0.0;
    private double D = 1.7;
    private double[] step;
    private double[] proposalStep;
    private double[] stepStdError;

    private ItemParamPrior discriminationPrior = null;
    private ItemParamPrior[] stepPrior = null;

    /**
     * Default constructor
     *
     * @param discrimination item discrimination parameter
     * @param step an array of m-1 step parameters for an m category item
     * @param D a scaling constant that is either 1.0, 1.7, or 1.712.
     */
    public IrmGRM(double discrimination, double[] step, double D){
        this.discrimination = discrimination;
        this.step = step;
        this.stepStdError = new double[step.length];
        this.D = D;
        maxCategory = step.length;
        ncat = maxCategory+1;
        defaultScoreWeights();
    }

    public double cumulativeProbability(double theta, double[] iparam, int category, double D){
        if(category>maxCategory || category<minCategory) return 0;
        if(category==minCategory) return 1.0;

        double a = iparam[0];
        double[] s = Arrays.copyOfRange(iparam, 1, iparam.length);

        double Zk = D*a*(theta-s[category-1]);
        double expZk = Math.exp(Zk);
        double prob = expZk/(1+expZk);

        return prob;
    }

    /**
     * Compute cumulative probability of scoring at or above category.
     *
     * @param theta examinee proficiency
     * @param category response category
     * @return
     */
    public double cumulativeProbability(double theta, int category){
        if(category>maxCategory || category<minCategory) return 0;
        if(category==minCategory) return 1.0;

        double Zk = D*discrimination*(theta-step[category-1]);
        double expZk = Math.exp(Zk);
        double prob = expZk/(1+expZk);

        return prob;
    }

    public double probability(double theta, double[] iparam, int response, double D){
        if(response==minCategory)  return 1.0-cumulativeProbability(theta, iparam, response+1, D);
        if(response==maxCategory) return cumulativeProbability(theta, iparam, response, D);
        double prob1 = cumulativeProbability(theta, response+1);
        double prob = cumulativeProbability(theta, response);
        return prob - prob1;
    }

    /**
     * Computes probability of a response using parameters stored in the object.
     * This method returns the category probability, not the cumulative probability.
     *
     * @param theta
     * @param response
     * @return
     */
    public double probability(double theta, int response){
        if(response==minCategory)  return 1.0-cumulativeProbability(theta, response+1);
        if(response==maxCategory) return cumulativeProbability(theta, response);
        double prob1 = cumulativeProbability(theta, response+1);
        double prob = cumulativeProbability(theta, response);
        return prob - prob1;
    }

    public double expectedValue(double theta){
        double ev = 0;
        for(int i=0;i< ncat;i++){
            ev += scoreWeight[i]*probability(theta, i);
        }
        return ev;
    }

    public double[] nonZeroPrior(double[] param){
        double[] p = Arrays.copyOf(param, param.length);
        if(discriminationPrior!=null) p[0] = discriminationPrior.nearestNonZero(param[0]);
        for(int k=1;k<param.length;k++){
            if(stepPrior[k-1]!=null) p[k] = stepPrior[k-1].nearestNonZero(param[k]);
        }
        return p;
    }

    public void setDiscriminationPrior(ItemParamPrior prior){
        discriminationPrior = prior;
    }

    public void setStepPriorAt(ItemParamPrior prior, int k){
        stepPrior[k] = prior;
    }

    public void setDifficultyPrior(ItemParamPrior difficultyPrior){

    }

    public void setGuessingPrior(ItemParamPrior guessingPrior){

    }

    public void setSlippingPrior(ItemParamPrior slippingPrior){

    }

    public double[] gradient(double theta, double[] iparam, int k, double D){
        //empty method
        return null;
    }

    public double[] gradient(double theta, int k){
        //empty method
        return null;
    }

    public double addPriorsToLogLikelihood(double ll, double[] iparam){
        return ll;
    }

    public double[] addPriorsToLogLikelihoodGradient(double[] loglikegrad, double[] iparam){
        //empty method
        return loglikegrad;
    }

    public void setScoreWeights(double[] scoreWeight)throws DimensionMismatchException {
        if(scoreWeight.length!=step.length) throw new DimensionMismatchException(scoreWeight.length, step.length);
        this.scoreWeight = scoreWeight;
    }

    /**
     * Partial derivative wrt Theta. This calculation is required for IRT true score equating.
     *
     * @param theta person proficiency value at which derivative is calculated
     * @return first partial derivative
     */
    public double derivTheta(double theta){
        double deriv = 0.0;
        for(int k=0;k<ncat;k++){
            deriv += scoreWeight[k]*derivCalc(theta, k);
        }
        return deriv;
    }

    /**
     * Support function for derivTheta(). Translated from Equating Recipes C++ library
     * by Kolen and Brennan. C++ function PdLGRoverTheta() written by Seonghoon Kim on
     * 09/05/08. For original C++ library, see
     * http://www.education.uiowa.edu/centers/casma/computer-programs.aspx#equatingrecipes
     *
     * @param theta person proficiency at which derivative is calculated
     * @param resp response category.
     * @return
     */
    private double derivCalc(double theta, int resp){
        double cp_jk = 0;
        double cp_jk1 = 0;

        if (resp == 0) {
            cp_jk  = 1.0;
            cp_jk1 = 1.0/(1.0 + Math.exp(-D*discrimination*(theta-step[resp])));
        }else {
            if (resp < (ncat-1)) {
                cp_jk  = 1.0/(1.0 + Math.exp(-D*discrimination*(theta-step[resp-1])));
                cp_jk1 = 1.0/(1.0 + Math.exp(-D*discrimination*(theta-step[resp])));
            }else { /* CatId == CatNum */
                cp_jk  = 1.0/(1.0 + Math.exp(-D*discrimination*(theta-step[resp-1])));
                cp_jk1 = 0.0;
            }
        }
        return ( D*discrimination*(cp_jk*(1.0-cp_jk) - cp_jk1*(1.0-cp_jk1)) );
    }

    public void incrementMeanSigma(Mean mean, StandardDeviation sd){
        for(int i=0;i<maxCategory;i++){
            mean.increment(step[i]);
            sd.increment(step[i]);
        }

    }

    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty){
        meanDiscrimination.increment(discrimination);
        for(int i=0;i<maxCategory;i++){
            meanDifficulty.increment(step[i]);
        }

    }

    public void scale(double intercept, double slope){
        if(isFixed) return;//DO NOT transform the item parameters when they are fixed
        discrimination /= slope;
        discriminationStdError *= slope;
        for(int i=0;i<maxCategory;i++){
            step[i] = step[i]*slope + intercept;
            stepStdError[i] = stepStdError[i]*slope;
        }
    }

    public int getNumberOfParameters(){
        return step.length+1;
    }

    public int getNumberOfEstimatedParameters(){
        if(isFixed) return 0;
        return step.length+1;
    }

    /**
     * From Ostini and Nering but needs checking. May need to incorporate score function.
     *
     * @param theta
     * @return
     */
    public double itemInformationAt(double theta){
        double dSum = 0.0;
        double top = 0.0;
        double bot = 0.0;
        for(int k=0;k<ncat;k++){
            top = Math.pow(derivCalc(theta, k), 2);
            bot = probability(theta, k);
            dSum += top/bot;
        }
        return dSum;
    }

    private double tStarCumulativeProbability(double theta, int category, double intercept, double slope){
        if(category>maxCategory || category<minCategory) return 0;
        if(category==minCategory) return 1.0;

        double a = discrimination/slope;
        double b = step[category-1]*slope+intercept;

        double Zk = D*a*(theta-b);
        double expZk = Math.exp(Zk);
        double prob = expZk/(1+expZk);

        return prob;
    }

    /**
     * Returns the probability of a response with a linear transformatin of the parameters.
     * This transformation is such that Form X (New Form) is transformed to the scale of Form Y
     * (Old Form). It implements the backwards (New to Old) transformation as described in Kim
     * and Kolen.
     *
     * @param theta examinee proficiency parameter
     * @param response item response
     * @param intercept intercept coefficient of linear transformation
     * @param slope slope (i.e. scale) parameter of the linear transformation
     * @return probability of a response at values of linearly transformed item parameters
     */
    public double tStarProbability(double theta, int response, double intercept, double slope){
        if(response==minCategory)  return 1.0-tStarCumulativeProbability(theta, response+1, intercept, slope);
        if(response==maxCategory) return tStarCumulativeProbability(theta, response, intercept, slope);
        double prob1 = tStarCumulativeProbability(theta, response+1, intercept, slope);
        double prob = tStarCumulativeProbability(theta, response, intercept, slope);
        return prob - prob1;
    }

    /**
     * computes the expected value using parameters stored in the object
     *
     * @param theta
     * @return
     */
    public double tStarExpectedValue(double theta, double intercept, double slope){
        double ev = 0;
        for(int i=1;i<ncat;i++){
            ev += scoreWeight[i]*tStarProbability(theta, i, intercept, slope);
        }
        return ev;
    }

    private double tSharpCumulativeProbability(double theta, int category, double intercept, double slope){
        if(category>maxCategory || category<minCategory) return 0;
        if(category==minCategory) return 1.0;

        double a = discrimination*slope;
        double b = (step[category-1]-intercept)/slope;

        double Zk = D*a*(theta-b);
        double expZk = Math.exp(Zk);
        double prob = expZk/(1+expZk);

        return prob;
    }

    public double tSharpProbability(double theta, int response, double intercept, double slope){
        if(response==minCategory)  return 1.0-tSharpCumulativeProbability(theta, response+1, intercept, slope);
        if(response==maxCategory) return tSharpCumulativeProbability(theta, response, intercept, slope);
        double prob1 = tSharpCumulativeProbability(theta, response+1, intercept, slope);
        double prob = tSharpCumulativeProbability(theta, response, intercept, slope);
        return prob - prob1;
    }

    public double tSharpExpectedValue(double theta, double intercept, double slope){
        double ev = 0;
        for(int i=1;i<ncat;i++){
            ev += scoreWeight[i]*tSharpProbability(theta, i, intercept, slope);
        }
        return ev;
    }

    public String toString(){
        String s = "[" + getDiscrimination();
        double[] sp = getStepParameters();
        for(int i=0;i<sp.length;i++){
            s+= ", " + sp[i];
        }
        s+= "]";
        return s;
    }

    public IrmType getType(){
        return IrmType.GRM;
    }

    public double getScalingConstant(){
        return D;
    }


//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//

    public double[] getItemParameterArray(){
        double[] ip = new double[getNumberOfParameters()];
        ip[0] = discrimination;
        for(int k=0;k<ncatM1;k++){
            ip[k+1] = step[k];
        }
        return ip;
    }

    public void setStandardErrors(double[] x){
        discriminationStdError = x[0];
        for(int k=0;k<ncat-1;k++){
            stepStdError[k] = x[k+1];
        }
    }


    public double getDifficulty(){
        return 0.0;
    }

    public void setDifficulty(double difficulty){

    }

    public double getProposalDifficulty(){
        return 0.0;
    }

    public void setProposalDifficulty(double difficulty){

    }

    public double getDifficultyStdError(){
        return Double.NaN;
    }

    public void setDifficultyStdError(double stdError){

    }

    public double getDiscrimination(){
        return discrimination;
    }

    public void setDiscrimination(double discrimination){
        this.discrimination = discrimination;
    }

    public void setProposalDiscrimination(double discrimination){
        this.proposalDiscrimination = discrimination;
    }

    public double getDiscriminationStdError(){
        return discriminationStdError;
    }

    public void setDiscriminationStdError(double stdError){
        discriminationStdError = stdError;
    }

    public double getGuessing(){
        return 0;
    }

    public void setGuessing(double guessing){

    }

    public void setProposalGuessing(double guessing){

    }

    public double getGuessingStdError(){
        return Double.NaN;
    }

    public void setGuessingStdError(double stdError){

    }

    public void setSlipping(double slipping){

    }

    public void setProposalSlipping(double slipping){

    }

    public void setSlippingStdError(double slipping){

    }

    public double getSlipping(){
        return Double.NaN;
    }

    public double getSlippingStdError(){
        return Double.NaN;
    }

    public double[] getStepParameters(){
        return step;
    }

    public void setStepParameters(double[] step){
        this.step = step;
    }

    public void setProposalStepParameters(double[] step){
        this.proposalStep = step;
    }

    public double[] getStepStdError(){
        return stepStdError;
    }

    public void setStepStdError(double[] stdError){
        stepStdError = stdError;
    }

    public double[] getThresholdParameters(){
        return step;
    }

    public void setThresholdParameters(double[] thresholds){

    }

    public void setProposalThresholds(double[] thresholds){

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

    public double acceptAllProposalValues(){
        if(isFixed) return 0;
        double max = 0;
        if(!isFixed){
            double delta = Math.abs(this.discrimination-proposalDiscrimination);
            if(proposalDiscrimination>=1) delta /= proposalDiscrimination;
            max = Math.max(max, delta);
            this.discrimination = this.proposalDiscrimination;

            for(int m=0;m<ncat;m++){
                delta = Math.abs(this.step[m]-proposalStep[m]);
                if(proposalStep[m]>=1) delta /= proposalStep[m];
                max = Math.max(max, delta);
            }
            this.step = this.proposalStep;
        }
        return max;
    }

//=====================================================================================================================//
// END GETTER AND SETTER METHODS                                                                                       //
//=====================================================================================================================//

}

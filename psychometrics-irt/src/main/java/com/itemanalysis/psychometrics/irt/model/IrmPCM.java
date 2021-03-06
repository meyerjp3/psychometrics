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

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.estimation.ItemParamPrior;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;

/**
 * This version of the Partial Credit Model (GPCM) uses a difficulty parameter (b),
 * and one or more threshold parameters. For an item with M categories, there are
 * M-1 threshold parameters (t). For k = 2,..., M.
 * Let Zk = sum_{v=2}^k {(theta-b-t_v)} if k>1 and Zk = 0 if k==1. The probability
 * of a response of k is given by, exp(Zk)/(1+sum_{k=2}^M {exp(Zk)}).
 *
 * This form of the PCM is used in WINSTEPS and jMetrik Rasch estimation procedures.
 *
 * Note that it differs from IrmGPCM2 in that IrmPCM does not include a discrimination
 * parameter (i.e. a=1). It also differs in computation of the step parameters. This
 * class uses b_v = (b+t_v), whereas IrmGPCM2 uses b_v = (b-t_v). The partial credit
 * model as used by PARSCALE can be obtained with IrmGPCM2
 *
 */
public class IrmPCM extends AbstractItemResponseModel{

    private double difficulty = 0.0;
    private double proposalDifficulty = 0.0;
    private double difficultyStdError = 0.0;
    private double D = 1.7;
    private double[] threshold;//an item with M score categories has M-1 thresholds.
    private double[] proposalThreshold;
    private double[] thresholdStdError;

    /**
     * Default constructor
     *
     * @param difficulty item difficulty parameter
     * @param threshold an array of m-1 threshold parameters for an m category item
     * @param D a scaling constant that is either 1.0, 1.7, or 1.712
     */
    public IrmPCM(double difficulty, double[] threshold, double D){
        this.difficulty = difficulty;
        this.proposalDifficulty = difficulty;
        this.threshold = threshold;
        this.proposalThreshold = threshold;
        this.thresholdStdError = new double[threshold.length];
        this.D = D;
        ncatM1 = threshold.length;
        ncat = ncatM1+1;
        defaultScoreWeights();
    }

    public void setName(VariableName name){
        this.name = name;
    }

    public double probability(double theta, double[] iparam, int category){
        double t = numer(theta, iparam, category);
        double b = denom(theta, iparam, D);
        return t/b;
    }

    /**
     * Computes probability of a response using parameters stored in the object.
     *
     * @param theta person proficiency value
     * @param category category for which the probability of a response is sought.
     * @return probability of responding in category
     */
    public double probability(double theta, int category){
        double t = numer(theta, category);
        double b = denom(theta);
        return t/b;
    }

    private double numer(double theta, double[] iparam, int category){
        double Zk = 0;
        double b = iparam[0];
        double[] s = Arrays.copyOfRange(iparam, 1, iparam.length);

        //first category
        Zk = D*(theta-b);

        for(int k=0; k<category; k++){
            Zk += D*(theta-b-s[k]);
        }
        return Math.exp(Zk);
    }

    /**
     * Computes the expression for responding in a category.
     * It is the numerator for the probability of observing a response.
     *
     * @param  theta person proficiency value
     * @param category category for which probability is sought
     * @return expression for responding in category
     */
    private double numer(double theta, int category){
        double Zk = 0;
        double expZk = 0;
        double s = 0;

        //first category
        Zk = D*(theta-difficulty);

        for(int k=0; k<category; k++){
            Zk += D*(theta-difficulty-threshold[k]);
        }
        return Math.exp(Zk);
    }

    private double denom(double theta, double[] iparam, double D){
        double denom = 0.0;
        double expZk = 0.0;

        for(int k=0;k<ncat;k++){
            expZk = numer(theta, iparam, k);
            denom += expZk;
        }
        return denom;
    }

    /**
     * Denominator is the sum of the numerators. This method is used for
     * computing the probability of a response.
     *
     * @param theta
     * @return
     */
    private double denom(double theta){
        double denom = 0.0;
        double expZk = 0.0;

        for(int k=0;k<ncat;k++){
            expZk = numer(theta, k);
            denom += expZk;
        }
        return denom;
    }

    /**
     * computes the expected value using parameters stored in the object
     *
     * @param theta
     * @return
     */
    public double expectedValue(double theta){
        double ev = 0;
        for(int i=1;i< ncat;i++){
            ev += scoreWeight[i]*probability(theta, i);
        }
        return ev;
    }

    /**
     * Compute probability of scoring at or above a response category
     *
     * @param theta a person ability value
     * @param category response category
     * @return
     */
    public double cumulativeProbability(double theta, int category){
        if(category==0){
            return this.probability(theta, 0);
        }else{
            double cp = 0;
            for(int k=category;k<ncat;k++){
                cp += this.probability(theta, k);
            }
            return cp;
        }
    }

    public double[] gradient(double theta, double[] iparam, int k, double D){
        //empty method
        return null;
    }

    /**
     * Computes the gradientAt of teh item response model with respect to the item parameters
     * @param theta person ability value
     * @return gradientAt
     */
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

        /**
     * Partial derivative with respect to theta.
     *
     * @param theta person proficiency value
     * @return partial derivative at theta
     */
    public double derivTheta(double theta){
        double d1 = denom(theta);
        double d2 = d1*d1;
        double x1 = subCalcForDerivTheta(theta);
        double n1 = 0.0;
        double deriv = 0.0;
        double p1 = 0.0;
        double p2 = 0.0;

        for(int k=0;k<ncat;k++){
            n1 = numer(theta, k);
            p1 = (D*n1*(1.0+k))/d1;
            p2 = (n1*x1)/d2;
            deriv += scoreWeight[k]*(p1-p2);
        }
    	return deriv;

    }

    /**
     * Calculation needed for derivTheta().
     *
     * @param theta person proficiency value
     * @return
     */
    private double subCalcForDerivTheta(double theta){
        double sum = 0.0;
        for(int k=0;k<ncat;k++){
            sum += D*numer(theta, k)*(1.0+k);
        }
        return sum;
    }

    public double itemInformationAt(double theta){
        double T = 0;
        double prob = 0.0;
        double sum1 = 0.0;
        double sum2 = 0.0;

        for(int i=0;i< ncat;i++){
            prob = probability(theta, i);
            T = scoreWeight[i];
            sum1 += T*T*prob;
            sum2 += T*prob;
        }

        double info = D*D*(sum1 - Math.pow(sum2, 2));
        return info;

    }

    public void incrementMeanSigma(Mean mean, StandardDeviation sd){
        for(int i=0;i<ncatM1;i++){
            mean.increment(difficulty-threshold[i]);
            sd.increment(difficulty-threshold[i]);
        }

    }

    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty){
        meanDiscrimination.increment(1.0);
        for(int i=0;i<ncatM1;i++){
            meanDifficulty.increment(difficulty-threshold[i]);
        }

    }

    public void scale(double intercept, double slope){
        if(isFixed) return;//DO NOT transform the item parameters when they are fixed
        difficulty = difficulty*slope + intercept;
        difficultyStdError *= slope;
        for(int i=0;i<ncatM1;i++){
            threshold[i] = threshold[i]*slope;
            thresholdStdError[i] = thresholdStdError[i]*slope;
        }
    }

    public int getNumberOfParameters(){
        return threshold.length+1;
    }

    public int getNumberOfEstimatedParameters(){
        if(isFixed) return 0;
        return threshold.length+1;
    }

    public double getScalingConstant(){
        return D;
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
        if(response> maxWeight || response<minWeight) return 0;

        double[] iparam = new double[getNumberOfParameters()];
        iparam[0] = difficulty+intercept;
        for(int i=0;i<threshold.length;i++){
            iparam[i+1] = threshold[i];//TODO will need to change when first step is added to step array. First step should alway be zero.
        }
        return probability(theta, iparam, response);

//        double Zk = 0;
//        double expZk = 0;
//        double numer = 0;
//        double denom = 0;
//        double b = 0;
//        double t = 0;
//
//        for(int k=0;k<ncat;k++){
//            Zk = 0;
//            for(int v=1;v<(k+1);v++){
//                b = difficulty+intercept;
//                t = threshold[v-1];
//                Zk += D*(theta-(b+t));
//            }
//            expZk = Math.exp(Zk);
//            if(scoreWeight[k]==response) numer = expZk;
//            denom += expZk;
//        }
//        return numer/denom;
    }

    /**
     * computes the expected value using parameters stored in the object
     *
     * @param theta
     * @return
     */
    public double tStarExpectedValue(double theta, double intercept, double slope){
        double ev = 0;
        for(int i=0;i<ncat;i++){
            ev += scoreWeight[i]*tStarProbability(theta, i, intercept, slope);
        }
        return ev;
    }

    public double tSharpProbability(double theta, int response, double intercept, double slope){
        if(response> maxWeight || response<minWeight) return 0;

        double[] iparam = new double[getNumberOfParameters()];
        iparam[0] = (difficulty-intercept);
        for(int i=0;i<threshold.length;i++){
            iparam[i+1] = threshold[i];//TODO will need to change when first step is added to step array. First step should alway be zero.
        }
        return probability(theta, iparam, response);

//        double Zk = 0;
//        double expZk = 0;
//        double numer = 0;
//        double denom = 0;
//        double b = 0;
//        double t = 0;
//
//        for(int k=0;k<ncat;k++){
//            Zk = 0;
//            for(int v=1;v<(k+1);v++){
//                b = (difficulty-intercept);
//                t = threshold[v-1];
//                Zk += D*(theta-(b+t));
//            }
//            expZk = Math.exp(Zk);
//            if(scoreWeight[k]==response) numer = expZk;
//            denom += expZk;
//        }
//        return numer/denom;
    }

    public double tSharpExpectedValue(double theta, double intercept, double slope){
        double ev = 0;
        for(int i=0;i<ncat;i++){
            ev += scoreWeight[i]*tSharpProbability(theta, i, intercept, slope);
        }
        return ev;
    }

    public String toString(){
        String s = "[" + getDiscrimination() + ", " + getDifficulty();
        double[] threshold = getThresholdParameters();
        for(int i=0;i<threshold.length;i++){
            s+= ", " + threshold[i];
        }
        s+= "]";
        return s;
    }

    public IrmType getType(){
        return IrmType.PCM;
    }

//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//
    public double[] getItemParameterArray(){
        double[] ip = new double[getNumberOfParameters()];
        ip[0] = difficulty;
        for(int k=0;k<ncatM1;k++){
            ip[k+1] = threshold[k];
        }
        return ip;
    }

    public void setStandardErrors(double[] x){
        difficultyStdError = x[0];
        for(int k=0;k<ncat;k++){
            thresholdStdError[k] = x[k+1];
        }
    }

    public double[] nonZeroPrior(double[] param){
//        double[] p = Arrays.copyOf(param, param.length);
//        if(discriminationPrior!=null) p[0] = discriminationPrior.nearestNonZero(param[0]);
//        if(difficultyPrior!=null) p[1] = difficultyPrior.nearestNonZero(param[1]);
//        for(int k=2;k<param.length;k++){
//            if(stepPrior[k-2]!=null) p[k] = stepPrior[k-2].nearestNonZero(param[k]);
//        }
        return param;
    }

    public void setDiscriminationPrior(ItemParamPrior prior){

    }

    public void setStepPriorAt(ItemParamPrior prior, int k){

    }

    public void setDifficultyPrior(ItemParamPrior difficultyPrior){

    }

    public void setGuessingPrior(ItemParamPrior guessingPrior){

    }

    public void setSlippingPrior(ItemParamPrior slippingPrior){

    }

    public void setScalingConstant(double D){
        this.D = D;
    }

    public double getDifficulty(){
        return difficulty;
    }

    public void setDifficulty(double difficulty){
        this.difficulty = difficulty;
    }

    public double getProposalDifficulty(){
        return proposalDifficulty;
    }

    public void setProposalDifficulty(double difficulty){
        if(!isFixed) this.proposalDifficulty = difficulty;
    }

    public double getDifficultyStdError(){
        return difficultyStdError;
    }

    public void setDifficultyStdError(double stdError){
        difficultyStdError = stdError;
    }

    public double getDiscrimination(){
        return 1.0;
    }

    public void setDiscrimination(double discrimination){

    }

    public double getProposalDiscrimination(){
        return 1.0;
    }

    public void setProposalDiscrimination(double discrimination){

    }

    public double getDiscriminationStdError(){
        return Double.NaN;
    }

    public void setDiscriminationStdError(double stdError){

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

    public void setGuessingStdError(double StdError){

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

    public double[] getThresholdParameters(){
        return threshold;
    }

    public void setThresholdParameters(double[] thresholds){
        this.threshold = thresholds;
        this.proposalThreshold = thresholds;
    }

    public double[] getProposalThresholds(){
        return this.proposalThreshold;
    }

    public void setProposalThresholds(double[] thresholds){
        this.proposalThreshold = thresholds;
    }

    public double[] getThresholdStdError(){
        return thresholdStdError;
    }

    public void setThresholdStdError(double[] stdError){
        thresholdStdError = stdError;
    }

    public void setStepParameters(double[] step){

    }

    public void setProposalStepParameters(double[] step){

    }

    public double[] getStepParameters(){
        double[] t = new double[ncatM1];
        for(int k=0;k<ncatM1;k++){
            t[k] = difficulty+threshold[k];
        }
        return t;
    }

    public void setStepParameters(){

    }

    public void setProposalStepParameters(){

    }

    public double[] getStepStdError(){
        return thresholdStdError;
    }

    public void setStepStdError(double[] stdError){
        thresholdStdError = stdError;
    }

    public double acceptAllProposalValues(){
        if(isFixed) return 0;
        double max = Math.max(0, Math.abs(this.difficulty-this.proposalDifficulty));
        for(int m=0;m<getNcat()-1;m++){
            max = Math.max(max, Math.abs(this.threshold[m]-this.proposalThreshold[m]));
        }
        this.difficulty = this.proposalDifficulty;
        this.threshold = this.proposalThreshold;
        return max;
    }
//=====================================================================================================================//
// END GETTER AND SETTER METHODS                                                                                       //
//=====================================================================================================================//

}

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

/**
 * This version of the Generalized Partial Credit Model (GPCM) uses a discrimination
 * parameter, a difficulty parameter (b), and one or more threshold parameters. For an
 * item with M categories, there are M-1 threshold parameters (t). For k = 2,..., M,
 * Let Zk = sum_{v=2}^k {D*a*(theta-b+t_v)} if k>1 and Zk = 0 if k==1. The probability
 * of a response of k is given by, exp(Zk)/(1+sum_{k=2}^M {exp(Zk)}).
 *
 * This form of the GPCM is used in PARSCALE.
 *
 * The relationship with IrmGPCM is that the thresholds and difficulty in IrmGPCM2
 * can be combined to get teh step parameters in IrmGPCM such that, b_v = (b - t_v).
 * This decomposition of the step parameters is given in Muraki's 1992 article. It
 * differs from the decomposition of the step parameters in Linacre's parameterization
 * of the PCM as used in jMetrik and WINSTEPS. Linacre's parameterization is found
 * in IrmPCM.
 *
 * TODO currently stores m-1 thresholds for an item with m categories. Will need to change to an array of m thresholds when use this model for estimation.
 *
 */
public class IrmGPCM2 extends AbstractItemResponseModel{

    private double discrimination = 1.0;
    private double proposalDiscrimination = 1.0;
    private double discriminationStdError = 0.0;
    private double difficulty = 0.0;
    private double proposalDifficulty = 0.0;
    private double difficultyStdError = 0.0;
    private double D = 1.7;
    private double[] threshold;
    private double[] proposalThreshold;
    private double[] thresholdStdError;

    private ItemParamPrior discriminationPrior = null;
    private ItemParamPrior difficultyPrior = null;
    private ItemParamPrior[] stepPrior = null;

    /**
     * Default constructor
     *
     * @param discrimination item discrimination parameter
     * @param difficulty item difficulty parameter
     * @param threshold an array of m-1 threshold parameters for an m category item.
     * @param D a scaling constant that is either 1.0, 1.7, or 1.712.
     */
    public IrmGPCM2(double discrimination, double difficulty, double[] threshold, double D){
        this.discrimination = discrimination;
        this.difficulty = difficulty;
        this.threshold = threshold;
        this.thresholdStdError = new double[threshold.length];
        this.D = D;
        ncatM1 = threshold.length;
        ncat = ncatM1+1;
        maxCategory = ncat;
        defaultScoreWeights();
    }

    public double probability(double theta, double[] iparam, int category, double D){
        double t = numer(theta, iparam, category, D);
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

    /**
     * Compute probability of scoring at or above a category.
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

    private double numer(double theta, double[] iparam, int category, double D){
        double Zk = 0;
        double a = iparam[0];
        double b = iparam[1];
        double[] t = Arrays.copyOfRange(iparam, 2, iparam.length);

        //first category
        Zk = D*a*(theta-b);

        for(int k=0; k<category; k++){
            Zk += D*a*(theta-b+t[k]);
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

        //first category
        Zk = D*discrimination*(theta-difficulty);

        for(int k=0; k<category; k++){
            Zk += D*discrimination*(theta-difficulty+threshold[k]);
        }
        return Math.exp(Zk);
    }

    private double denom(double theta, double[] iparam, double D){
        double denom = 0.0;
        double expZk = 0.0;

        for(int k=0;k<ncat;k++){
            expZk = numer(theta, iparam, k, D);
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
            p1 = (D*n1*(1.0+k)*discrimination)/d1;
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
            sum += D*numer(theta, k)*(1.0+k)*discrimination;
        }
        return sum;
    }

    /**
     * computes the expected value using parameters stored in the object
     *
     * @param theta
     * @return
     */
    public double expectedValue(double theta){
        double ev = 0;
        for(int i=0;i<ncat;i++){
            ev += scoreWeight[i]*probability(theta, i);
        }
        return ev;
    }

    public double[] nonZeroPrior(double[] param){
        double[] p = Arrays.copyOf(param, param.length);
        if(discriminationPrior!=null) p[0] = discriminationPrior.nearestNonZero(param[0]);
        if(difficultyPrior!=null) p[1] = difficultyPrior.nearestNonZero(param[1]);
        for(int k=2;k<param.length;k++){
            if(stepPrior[k-2]!=null) p[k] = stepPrior[k-2].nearestNonZero(param[k]);
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

    }

    public void setSlippingPrior(ItemParamPrior slippingPrior){

    }

    public void setStepPriorAt(ItemParamPrior prior, int index){
        this.stepPrior[index] = prior;
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

    public double itemInformationAt(double theta){

        double T = 0;
        double prob = 0.0;
        double sum1 = 0.0;
        double sum2 = 0.0;
        double a2 = discrimination*discrimination;

        for(int i=0;i< ncat;i++){
            prob = probability(theta, i);
            T = scoreWeight[i];
            sum1 += T*T*prob;
            sum2 += T*prob;
        }

        double info = D*D*a2*(sum1 - Math.pow(sum2, 2));
        return info;

    }

    public void incrementMeanSigma(Mean mean, StandardDeviation sd){
        for(int i=0;i<ncatM1;i++){
            mean.increment(difficulty-threshold[i]);
            sd.increment(difficulty-threshold[i]);
        }

    }

    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty){
        meanDiscrimination.increment(discrimination);
        for(int i=0;i<ncatM1;i++){
            meanDifficulty.increment(difficulty-threshold[i]);
        }

    }

    public void scale(double intercept, double slope){
        if(isFixed) return;//DO NOT transform the item parameters when they are fixed
        discrimination /= slope;
        discriminationStdError *= slope;
        difficulty = difficulty*slope + intercept;
        difficultyStdError *= slope;
        for(int i=0;i<ncatM1;i++){
            threshold[i] = threshold[i]*slope;
            thresholdStdError[i] = thresholdStdError[i]*slope;
        }
    }

    public int getNumberOfParameters(){
        return threshold.length+2;
    }

    public int getNumberOfEstimatedParameters(){

        //TODO change to threshold.length+1 when threshold array changed to include first threshold that is fixed to zero.
        if(isFixed) return 0;
        return threshold.length+2;
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
     * @param category item response
     * @param intercept intercept coefficient of linear transformation
     * @param slope slope (i.e. scale) parameter of the linear transformation
     * @return probability of a response at values of linearly transformed item parameters
     */
    public double tStarProbability(double theta, int category, double intercept, double slope){
        if(category> maxCategory || category<minCategory) return 0;

        double[] iparam = new double[getNumberOfParameters()];
        iparam[0] = discrimination/slope;
        iparam[1] = difficulty*slope+intercept;
        for(int i=0;i<threshold.length;i++){
            iparam[i+2] = threshold[i]*slope;
        }
        return probability(theta, iparam, category, D);

//        double Zk = 0;
//        double expZk = 0;
//        double numer = 0;
//        double denom = 0;
//        double a = discrimination/slope;
//        double b = 0;
//        double t = 0;

//        for(int k=0;k<ncat;k++){
//            Zk = 0;
//            for(int v=1;v<(k+1);v++){
//                b = difficulty*slope+intercept;
//                t = threshold[v-1]*slope;
//                Zk += D*a*(theta-(b-t));
//            }
//            expZk = Math.exp(Zk);
//            if(k==category) numer = expZk;
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

    public double tSharpProbability(double theta, int category, double intercept, double slope){
        if(category>maxCategory || category<minCategory) return 0;

        double[] iparam = new double[getNumberOfParameters()];
        iparam[0] = discrimination*slope;
        iparam[1] = (difficulty-intercept)/slope;
        for(int i=0;i<threshold.length;i++){
            iparam[i+2] = threshold[i]/slope;
        }
        return probability(theta, iparam, category, D);

//        double Zk = 0;
//        double expZk = 0;
//        double numer = 0;
//        double denom = 0;
//        double a = discrimination*slope;
//        double b = 0;
//        double t = 0;
//
//        for(int k=0;k<ncat;k++){
//            Zk = 0;
//            for(int v=1;v<(k+1);v++){
//                b = (difficulty-intercept)/slope;
//                t = threshold[v-1]/slope;
//                Zk += D*a*(theta-(b-t));
//            }
//            expZk = Math.exp(Zk);
//            if(k==category) numer = expZk;
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
        double[] sp = getStepParameters();
        for(int i=0;i<sp.length;i++){
            s+= ", " + sp[i];
        }
        s+= "]";
        return s;
    }

    public IrmType getType(){
        return IrmType.GPCM2;
    }

//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//

    public double[] getItemParameterArray(){
        double[] ip = new double[getNumberOfParameters()];
        ip[0] = discrimination;
        ip[1] = difficulty;
        for(int k=0;k<ncatM1;k++){
            ip[k+2] = threshold[k];
        }
        return ip;
    }

    public void setStandardErrors(double[] x){
        discriminationStdError = x[0];
        difficultyStdError = x[1];
        for(int k=0;k<ncatM1;k++){
            thresholdStdError[k] = x[k+2];
        }
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
        this.proposalDifficulty = difficulty;
    }

    public double getDifficultyStdError(){
        return difficultyStdError;
    }

    public void setDifficultyStdError(double stdError){
        difficultyStdError = stdError;
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
        return 0.0;
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

    public double[] getThresholdParameters(){
        return threshold;
    }

    public void setThresholdParameters(double[] thresholds){
        this.threshold = thresholds;
    }

    public void setProposalThresholds(double[] thresholds){
        this.proposalThreshold = threshold;
    }

    public double[] getThresholdStdError(){
        return thresholdStdError;
    }

    public void setThresholdStdError(double[] stdError){
        thresholdStdError = stdError;
    }

    public double[] getStepParameters(){
        double[] t = new double[ncatM1];
        for(int k=0;k<ncatM1;k++){
            t[k] = difficulty-threshold[k];
        }
        return t;
    }

    public void setStepParameters(double[] step){

    }

    public void setProposalStepParameters(double[] step){

    }

    public double[] getStepStdError(){
        double[] sp = new double[ncat];
        for(int k=0;k<ncat;k++){
            sp[k] = Double.NaN;
        }
        return sp;
    }

    public void setStepStdError(double[] stdError){

    }

    public double acceptAllProposalValues(){
        if(isFixed) return 0;
        double max = 0;
        if(!isFixed){
            double delta = Math.abs(this.difficulty-this.proposalDifficulty);
            if(proposalDifficulty>=1) delta /= proposalDifficulty;
            max = Math.max(max, delta);

            delta = Math.abs(this.discrimination-this.proposalDiscrimination);
            if(proposalDiscrimination>=1) delta /= proposalDiscrimination;
            max = Math.max(max, delta);

            for(int m=0;m<getNcat();m++){
                delta = Math.abs(this.threshold[m]-this.proposalThreshold[m]);
                if(proposalThreshold[m]>=1) delta /= proposalThreshold[m];
                max = Math.max(max, delta);
            }
            this.difficulty = this.proposalDifficulty;
            this.discrimination = this.proposalDiscrimination;
            this.threshold = this.proposalThreshold;
        }
        return max;
    }
//=====================================================================================================================//
// END GETTER AND SETTER METHODS                                                                                       //
//=====================================================================================================================//

}

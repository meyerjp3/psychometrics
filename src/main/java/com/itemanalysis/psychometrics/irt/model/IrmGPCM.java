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
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;


/**
 * This version of the Generalized Partial Credit Model (GPCM) uses a discrimination
 * parameter and one or more step parameters. For an item with M categories, there
 * are M-1 step parameters (b). For k = 2,..., M, Let Zk = sum_{v=2}^k {D*a*(theta-b_v)}
 * if k>1 and Zk = 0 if k==1. The probability of a response of k is given by,
 * exp(Zk)/(1+sum_{k=2}^M {exp(Zk)}).
 *
 * This form of the GPCM is used in Brad Hanson's ICL program.
 *
 */
public class IrmGPCM extends AbstractItemResponseModel {

    private double discrimination = 1.0;
    private double proposalDiscrimination = 1.0;
    private double D = 1.7;
    private double[] step;
    private double[] proposalStep;
    private double[] stepStdError;

    public IrmGPCM(double discrimination, double[] step, double D){
        this.discrimination = discrimination;
        this.step = step;
        this.stepStdError = new double[step.length];
        this.D = D;
        ncatM1 = step.length;
        ncat = ncatM1+1;
        maxCategory = ncat;
        defaultScoreWeights();
    }

//    public double probability(double theta, int category){
//        if(category>maxCategory || category<minCategory) return 0;
//        double Zk = 0;
//        double expZk = 0;
//        double numer = 0;
//        double denom = 0;
//        for(int k=0;k<ncat;k++){
//            Zk = 0;
//            for(int v=1;v<(k+1);v++){
//                Zk += D*discrimination*(theta-step[v-1]);
//            }
//            expZk = Math.exp(Zk);
//            if(k==category) numer = expZk;
//            denom += expZk;
//        }
//        return numer/denom;
//    }

    /**
     * Computes probability of a response using parameters stored in the object.
     *
     * @param theta
     * @param category
     * @return
     */
    public double probability(double theta, int category){
        double t = numer(theta, category);
        double b = denom(theta);
        return t/b;
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

    public double derivTheta(double theta){
        double denom = denom(theta);
        double denom2 = denom*denom;
        double denomDeriv = denomDeriv(theta);
        double numer = 0.0;
        double p1 = 0.0;
        double p2 = 0.0;
        double deriv = 0.0;

        for(int k=0;k<ncat;k++){
            numer = numer(theta, k);
            p1 = (D*numer*(1.0+k)*discrimination)/denom;
            p2 = (numer*denomDeriv)/denom2;
            deriv += scoreWeight[k]*(p1-p2);
        }
        return deriv;
    }

    private double numer(double theta, int category){
        double Zk = 0;
        double expZk = 0;
        double s = 0;

        //first category
        Zk = D*discrimination*(theta-s);

        for(int k=0; k<category; k++){
            s = step[k];
            Zk += D*discrimination*(theta-s);
        }
        return Math.exp(Zk);
    }

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
     * Computation needed for derivTheta()
     *
     * @param theta
     * @return
     */
    private double denomDeriv(double theta){
        double denom = 0.0;
        double expZk = 0.0;

        for(int k=0;k<ncat;k++){
            expZk = numer(theta, k);
            denom += expZk*(1.0+k)*discrimination;
        }
        return denom;
    }

    public void incrementMeanSigma(Mean mean, StandardDeviation sd){
        for(int i=0;i< ncat;i++){
            mean.increment(step[i]);
            sd.increment(step[i]);
        }

    }

    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty){
        meanDiscrimination.increment(discrimination);
        for(int i=0;i< ncat;i++){
            meanDifficulty.increment(step[i]);
        }
    }

    public void scale(double intercept, double slope){
        discrimination /= slope;
        for(int i=0;i<ncatM1;i++){
            step[i] = step[i]*slope + intercept;
            stepStdError[i] = stepStdError[i]*slope;
        }
    }

    public int getNumberOfParameters(){
        return step.length+1;
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

        double Zk = 0;
        double expZk = 0;
        double numer = 0;
        double denom = 0;
        double a = discrimination/slope;
        double b = 0;

        for(int k=0;k<ncat;k++){
            Zk = 0;
            for(int v=1;v<(k+1);v++){
                b = step[v-1]*slope+intercept;
                Zk += D*a*(theta-b);
            }
            expZk = Math.exp(Zk);
            if(k==category) numer = expZk;
            denom += expZk;
        }
        return numer/denom;
    }

    /**
     * computes the expected value using parameters stored in the object
     *
     * @param theta
     * @return
     */
    public double tStarExpectedValue(double theta, double intercept, double slope){
        double ev = 0;
        for(int i=1;i< ncat;i++){
            ev += scoreWeight[i]*tStarProbability(theta, i, intercept, slope);
        }
        return ev;
    }

    public double tSharpProbability(double theta, int category, double intercept, double slope){
        if(category> maxCategory || category<minCategory) return 0;

        double Zk = 0;
        double expZk = 0;
        double numer = 0;
        double denom = 0;
        double a = discrimination*slope;
        double b = 0;

        for(int k=0;k<ncat;k++){
            Zk = 0;
            for(int v=1;v<(k+1);v++){
                b = (step[v-1]-intercept)/slope;
                Zk += D*a*(theta-b);
            }
            expZk = Math.exp(Zk);
            if(k==category) numer = expZk;
            denom += expZk;
        }
        return numer/denom;
    }

    public double tSharpExpectedValue(double theta, double intercept, double slope){
        double ev = 0;
        for(int i=1;i< ncat;i++){
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
        return IrmType.GPCM;
    }

//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//
    public double getDifficulty(){
        return 0.0;
    }

    public void setDifficulty(double difficulty){
        throw new UnsupportedOperationException();
    }

    public double getProposalDifficulty(){
        return 0.0;
    }

    public void setProposalDifficulty(double difficulty){
        throw new UnsupportedOperationException();
    }

    public double getDifficultyStdError(){
        throw new UnsupportedOperationException();
    }

    public void setDifficultyStdError(double stdError){
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    public void setDiscriminationStdError(double stdError){
        throw new UnsupportedOperationException();
    }

    public double getGuessing(){
        throw new UnsupportedOperationException();
    }

    public void setGuessing(double guessing){
        throw new UnsupportedOperationException();
    }

    public void setProposalGuessing(double guessing){
        throw new UnsupportedOperationException();
    }

    public double getGuessingStdError(){
        throw new UnsupportedOperationException();
    }

    public void setGuessingStdError(double stdError){
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    public void setProposalThresholds(double[] thresholds){
        throw new UnsupportedOperationException();
    }

    public double[] getThresholdStdError(){
        throw new UnsupportedOperationException();
    }

    public void setThresholdStdError(double[] stdError){
        throw new UnsupportedOperationException();
    }

    public void acceptAllProposalValues(){
        if(!isFixed){
            this.discrimination = this.proposalDiscrimination;
            this.step = this.proposalStep;
        }
    }
//=====================================================================================================================//
// END GETTER AND SETTER METHODS                                                                                       //
//=====================================================================================================================//

}

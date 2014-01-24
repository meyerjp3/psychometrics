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

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class Irm3PL extends AbstractItemResponseModelWithGradient {

    private double discrimination = 1.0;
    private double difficulty = 0.0;
    private double guessing = 0.0;
    private double D = 1.7;
    private double discriminationStdError = 0.0;
    private double difficultyStdError = 0.0;
    private double guessingStdError = 0.0;
    private int numberOfParameters = 1;
    private double proposalDiscrimination = 1.0;
    private double proposalDifficulty = 0.0;
    private double proposalGuessing = 0.0;


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
        this.difficulty = difficulty;
        this.proposalDiscrimination = 1.0;
        this.proposalDifficulty = difficulty;
        this.proposalGuessing = 0.0;
        this.D = D;
        numberOfParameters = 1;
        this.ncat = 2;
        defaultScoreWeights();
    }

    public double probability(double theta, int response){
        if(response==1) return probRight(theta);
        return probWrong(theta);
    }

    private double probRight(double theta){
        double top = Math.exp(D*discrimination*(theta-difficulty));
        double prob = guessing + (1.0-guessing)*top/(1+top);
        return prob;
    }

    private double probWrong(double theta){
        return 1.0-probRight(theta);
    }

    public double expectedValue(double theta){
        return scoreWeight[1]*probRight(theta);
    }

    public double[] firstDerivative(double theta){
        double[] deriv = new double[numberOfParameters];
        double t = Math.exp(D*discrimination*(theta-difficulty));
        double onept2 = 1.0 + t;
        onept2 *= onept2;

        //derivative with respect to b parameter
        double derivb = -discrimination*(1.0-guessing)*D*t;
        derivb /= onept2;

        if(numberOfParameters==1){
            deriv[0] = derivb;
            return deriv;
        }else{
            deriv[1] = derivb;
        }

        //derivative with respect to the a parameter
        deriv[0] = (1.0 - guessing)*(theta - difficulty)*D*t;
        deriv[0] /= onept2;

        //derivative with respect to c parameter
        if(numberOfParameters==3){
            deriv[2] = -1.0/(1.0 + t);
            deriv[2] += 1.0;
        }

        return deriv;
    }

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
     * From Equating recipes
     * @param theta
     * @param response
     * @return
     */
    public double derivTheta2(double theta, int response){
        double z = Math.exp(D*discrimination*(theta-difficulty));

        //incorrect response
        if(response == 0) return ( -D*discrimination*(1.0-guessing)*z/((1.0+z)*(1.0+z)) );

        //correct response
        return ( D*discrimination*(1.0-guessing)*z/((1.0+z)*(1.0+z)) );
    }

    /**
     * Derivative from Mathematica.
     *
     * @param theta
     * @return
     */
    public double derivTheta(double theta){
        double p1 = D*discrimination*(1-guessing)*Math.exp(2.0*D*discrimination*(theta-difficulty));
        double p2 = Math.pow(1+Math.exp(D*discrimination*(theta-difficulty)),2);
        double p3 = D*discrimination*(1.0-guessing)*Math.exp(D*discrimination*(theta-difficulty));
        double p4 = 1+Math.exp(D*discrimination*(theta-difficulty));
        double deriv = -p1/p2 + p3/p4;
        return deriv;
    }

    public double itemInformationAt(double theta){
        double p = probRight(theta);
        double part1 = Math.pow(p - guessing, 2);
        double part2 = Math.pow(1.0-guessing, 2);
        double a2 = discrimination*discrimination;
        double info = D*D*a2*(part1/part2)*((1.0-p)/p);
        return info;
    }

    public void incrementMeanSigma(Mean mean, StandardDeviation sd){
        mean.increment(difficulty);
        sd.increment(difficulty);
    }

    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty){
        meanDiscrimination.increment(discrimination);
        meanDifficulty.increment(difficulty);
    }

        /**
     * Computes probability of a response under a linear transformation. This method
     * is mainly used for the Characteristic Curve Linking method. It applies
     * the linear transformation such that the New form is transformed to the Old Form.
     *
     * @param theta examinee proficiency value
     * @param response target category
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return
     */
    public double tStarProbability(double theta, int response, double intercept, double slope){
        if(response==1) return tStarProbRight(theta, intercept, slope);
        return tStarProbWrong(theta, intercept, slope);
    }

    private double tStarProbRight(double theta, double intercept, double slope){
        double a = discrimination/slope;
        double b = difficulty*slope+intercept;
        double top = Math.exp(D*a*(theta-b));
        double prob = guessing + (1.0-guessing)*top/(1+top);
        return prob;
    }

    private double tStarProbWrong(double theta, double intercept, double slope){
        return 1.0-tStarProbRight(theta, intercept, slope);
    }

    /**
     * Computes probability of a response under a linear transformation. This method
     * is mainly used for the Characteristic Curve Linking method. It applies
     * the linear transformation such that the Old form is transformed to the New Form.
     *
     * @param theta examinee proficiency value
     * @param response target category
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return
     */
    public double tSharpProbability(double theta, int response, double intercept, double slope){
        if(response==1) return tSharpProbRight(theta, intercept, slope);
        return tSharpProbWrong(theta, intercept, slope);
    }

    private double tSharpProbRight(double theta, double intercept, double slope){
        double a = discrimination*slope;
        double b = (difficulty - intercept)/slope;
        double top = Math.exp(D*a*(theta-b));
        double prob = guessing + (1.0-guessing)*top/(1+top);
        return prob;
    }

    private double tSharpProbWrong(double theta, double intercept, double slope){
        return 1.0-tSharpProbRight(theta, intercept, slope);
    }

    public double tStarExpectedValue(double theta, double intercept, double slope){
        return tStarProbRight(theta, intercept, slope);
    }

    public double tSharpExpectedValue(double theta, double intercept, double slope){
        return tSharpProbRight(theta, intercept, slope);
    }

    public int getNumberOfParameters(){
        return numberOfParameters;
    }

    public void scale(double intercept, double slope){
        difficulty = intercept + slope*difficulty;
        discrimination = discrimination/slope;
        difficultyStdError *= slope;
        discriminationStdError *= slope;
    }

    public String toString(){
        return "[" + getDiscrimination() + ", " + getDifficulty() + ", " + getGuessing() + "]";
    }

    public IrmType getType(){
        return IrmType.L3;
    }

//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//
    public double getDifficulty(){
        return difficulty;
    }

    /**
     * Set difficulty to an existing value. If you are using this method to fix an
     * item parameter during estimation, you must also set the proposal value.
     * @param difficulty
     */
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
        return discrimination;
    }

    /**
     * Set item discrimination to an existing value. If you are using this method to fix an
     * item parameter during estimation, you must also set the proposal value.
     * @param discrimination
     */
    public void setDiscrimination(double discrimination){
        this.discrimination = discrimination;
    }

    public double getProposalDiscrimination(){
        return proposalDiscrimination;
    }

    public void setProposalDiscrimination(double discrimination){
        if(!isFixed) this.proposalDiscrimination = discrimination;
    }

    public double getDiscriminationStdError(){
        return discriminationStdError;
    }

    public void setDiscriminationStdError(double stdError){
        discriminationStdError = stdError;
    }

    public double getGuessing(){
        return guessing;
    }

    /**
     * Set lower asymptote parameter to an existing value. If you are using this method to fix an
     * item parameter during estimation, you must also set the proposal value.
     * @return
     */
    public void setGuessing(double guessing){
        this.guessing = guessing;
    }

    public double getProposalGuessing(){
        return proposalGuessing;
    }

    public void setProposalGuessing(double guessing){
        if(!isFixed) this.proposalGuessing = guessing;
    }

    public double getGuessingStdError(){
        return guessingStdError;
    }

    public void setGuessingStdError(double stdError){
        guessingStdError = stdError;
    }

    public void acceptAllProposalValues(){
        this.difficulty = proposalDifficulty;
        this.discrimination = proposalDiscrimination;
        this.guessing = proposalGuessing;
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

    public void setThresholdParameters(double[] thresholdParameters){
        throw new UnsupportedOperationException();
    }

    public void setProposalThresholds(double[] thresholds){
        throw new UnsupportedOperationException();
    }
//=====================================================================================================================//
// END GETTER AND SETTER METHODS                                                                                       //
//=====================================================================================================================//

}

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

import java.util.Formatter;

/**
 * An implementation of {@link AbstractItemResponseModel} that allows for the three-parameter logistic (3PL) model,
 * two-parameter logistic (2PL) model, one-parameter logistic (1PL) model, and the Rasch model. The particular
 * type of item response model is determined by the constructor used to create the object. See the constructors
 * for more details on which one to use for each type of model.
 */
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
        double prob = 0.0;
        if(response==1){
            prob = probRight(theta);
        }else{
            prob = probWrong(theta);
        }
        return Math.min(1.0, Math.max(0.0, prob)); //always return value between 0 and 1
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

    /**
     * Computes the gradient (vector of first partial derivatives) with respect to the item parameters.
     *
     * @param theta person ability estimate.
     * @return an array of first partial derivatives (i.e. the gradient).
     */
    public double[] gradient(double theta){
        double[] deriv = new double[numberOfParameters];
        double t = Math.exp(-discrimination*D*(theta-difficulty));
        double onept2 = 1.0 + t;
        onept2 *= onept2;

        //derivative with respect to b parameter
        double derivb = -discrimination*(1.0-guessing)*D*t;
        derivb /= onept2;

        if(numberOfParameters==1){
            deriv[0] = derivb;
            return deriv;
        }

        deriv[1] = derivb;

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
        double part2 = Math.pow(1.0-guessing, 2);
        double a2 = discrimination*discrimination;
        double info = D*D*a2*(part1/part2)*((1.0-p)/p);
        return info;
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

    public ItemParamPrior getDiscriminationPrior(){
        return discriminationPrior;
    }

    public ItemParamPrior getDifficultyPrior(){
        return difficultyPrior;
    }

    public ItemParamPrior getGuessingPrior(){
        return guessingPrior;
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
    public double tSharpExpectedValue(double theta, double intercept, double slope){
        return tSharpProbRight(theta, intercept, slope);
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
     * Linear transformation of item parameters.
     *
     * @param intercept intercept transformation coefficient.
     * @param slope slope transformation coefficient.
     */
    public void scale(double intercept, double slope){
        difficulty = intercept + slope*difficulty;
        discrimination = discrimination/slope;
        difficultyStdError *= slope;
        discriminationStdError *= slope;
    }

    /**
     * A string representaiton of the item parameters. Mainly used for printing and debugging.
     *
     * @return a string of item parameters.
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%1s", "[");
        f.format("% .6f", getDiscrimination()); f.format("%2s", ", ");
        f.format("% .6f", getDifficulty()); f.format("%2s", ", ");
        f.format("% .6f", getGuessing()); f.format("%1s", "]");
        return f.toString();

//        return "[" + getDiscrimination() + ", " + getDifficulty() + ", " + getGuessing() + "]";
    }

    /**
     * Gets the type of item response model.
     *
     * @return type of item response model.
     */
    public IrmType getType(){
        return IrmType.L3;
    }

//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//

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

        if(numberOfParameters>=2){
            max = Math.max(max, this.discrimination - proposalDiscrimination);
            this.discrimination = proposalDiscrimination;
        }

        if(numberOfParameters==3){
            max = Math.max(max, Math.abs(this.guessing-proposalGuessing));
            this.guessing = proposalGuessing;
        }

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

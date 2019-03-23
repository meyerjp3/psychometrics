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

import com.itemanalysis.psychometrics.data.ItemScoring;
import com.itemanalysis.psychometrics.data.VariableLabel;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.estimation.ItemFitStatistic;
import com.itemanalysis.psychometrics.irt.estimation.ItemParamPrior;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * An item response model describes the relationship between the probability of a score category and person
 * ability (i.e. the latent trait). Unidimensional item response models involve a single latent trait
 * and one or more item parameters. This interface accounts for the methods that all item response models
 * should have.
 *
 * Note that the terms person ability, examinee proficiency, and latent trait are used interchangeably. They
 * are synonyms.
 */
public interface ItemResponseModel {

    /**
     * Computes the probability of response. It ranges between 0 and 1.
     *
     * @param theta a person ability value.
     * @param response an item response (i.e. a person's score on an item).
     * @return the probability of the response.
     */
    public double probability(double theta, int response);

    /**
     * Computes the probability of a response using item parameter values passed in iparam. It does
     * NOT use the item parameters stored in the object.
     *
     * @param theta person ability parameter.
     * @param iparam array of item parameters. The order is important and will be unique to each implementation of the interface.
     * @param response an item response category.
     * @param D a sclaing constant that is either 1 or 1.7.
     * @return probability of a response.
     */
    public double probability(double theta, double[] iparam, int response, double D);

    /**
     * For a binary item, {@link #probability(double, int)} and the excpected value returned by this method
     * are the same thing. For a polytomous item, the expected value ranges from the minimum possible
     * item score and the maximum possible item score.
     *
     * @param theta a person ability value.
     * @return a person's expected score on the item.
     */
    public double expectedValue(double theta);

    /**
     * Mainly here for the graded response model
     *
     * @param theta a person ability value
     * @param category response category
     * @return the probability of responding at or above the given category
     */
    public double cumulativeProbability(double theta, int category);

    public void setDiscriminationPrior(ItemParamPrior prior);

    public void setDifficultyPrior(ItemParamPrior prior);

    public void setGuessingPrior(ItemParamPrior prior);

    public void setSlippingPrior(ItemParamPrior prior);

    public void setStepPriorAt(ItemParamPrior prior, int index);

    public void setItemScoring(ItemScoring itemScoring);

    /**
     * If the prior density for a parameter is zero, adjust parameter to the nearest non zero value.
     */
    public double[] nonZeroPrior(double[] param);

    public ItemScoring getItemScoring();

    /**
     * Performs a linear transformation of item parameters and standard errors.
     *
     * @param intercept intercept transformation coefficient.
     * @param slope slope transformation coefficient.
     */
    public void scale(double intercept, double slope);

    /**
     * Adds prior probabilities to the loglikelihood. This method
     * is used in {@link com.itemanalysis.psychometrics.irt.estimation.MarginalMaximumLikelihoodEstimation
     * @param ll loglikelihood value
     */
    public double addPriorsToLogLikelihood(double loglike, double[] iparam);

    /**
     * Computes the gradientAt at theta. This method uses the item parameters stored in the object.
     *
     * @param theta person ability value
     * @return
     */
    public double[] gradient(double theta, int category);

    /**
     * Computes the gradientAt (vector of first partial derivatives) with respect to the item parameters.
     * This method uses item parameters passed to teh method. It does NOT use item parameters stored in the
     * object.
     *
     * Note: The second argument (int k) is not actually used by this class. It is here to satisfy the interface.
     *
     * @param theta person ability estimate.
     * @param iparam array of item parameters.
     * @param category response category
     * @param D scaling constant that is either 1 or 1.7
     * @return an array of first partial derivatives (i.e. the gradientAt).
     */
    public double[] gradient(double theta, double[] iparam, int category, double D);

    /**
     * Adds log-prior probabilities to the item loglikelihood. This method
     * is used in {@link com.itemanalysis.psychometrics.irt.estimation.MarginalMaximumLikelihoodEstimation}.
     *
     * @param loglikegrad
     */
    public double[] addPriorsToLogLikelihoodGradient(double[] loglikegrad, double[] iparam);

    /**
     * Gets the number of item parameters in the response model.
     *
     * @return number of item parameters.
     */
    public int getNumberOfParameters();

    /**
     * Number of estimated parameters does not count any values fixed during estimation.
     * @return
     */
    public int getNumberOfEstimatedParameters();

    public double[] getItemParameterArray();

    public void setStandardErrors(double[] x);

    /**
     * Gets the number of response categories. This value is 2 for binary items and greater than 2 for polytomous
     * items.
     *
     * @return number of response categories.
     */
    public int getNcat();

    public double getScalingConstant();

    /**
     * A fixed item will use its initial values as the item parameters and no further estimation or update will be
     * applied to the item parameters. This method checks whether the item has been set as a fixed item.
     *
     * @return
     */
    public boolean isFixed();

    /**
     * A fixed item will use its initial values as the item parameters and no further estimation or update will be
     * applied to the item parameters. This method is how the item is set to fixed status or not.
     *
     * @param isFixed
     */
    public void setFixed(boolean isFixed);

    /**
     * Gets the item difficulty parameter.
     *
     * @return item difficulty.
     */
    public double getDifficulty();

    /**
     * Set difficulty parameter to an existing value. If you are using this method to fix an item parameter during
     * estimation, you must also set the proposal value in {@link #setProposalDifficulty(double)}.
     *
     */
    public void setDifficulty(double difficulty);

    /**
     * A proposal difficulty value is obtained during each iteration of the estimation routine. This method gets
     * the proposal item difficulty values. This method is needed for estimating item difficulty.
     *
     * @return proposed item difficulty value.
     */
    public double getProposalDifficulty();

    /**
     * A proposal difficulty value is obtained during each iteration of the estimation routine. This method
     * sets the proposal value.
     *
     * @param difficulty proposed item difficulty value.
     */
    public void setProposalDifficulty(double difficulty);

    /**
     * Gets the item difficulty standard error.
     *
     * @return item difficulty standard error.
     */
    public double getDifficultyStdError();

    /**
     * Item difficulty standard error may be computed external to the class. This method sets the difficulty
     * standard error to a computed value.
     *
     * @param StdError item difficulty standard error.
     */
    public void setDifficultyStdError(double StdError);

    /**
     * Proposal values for every item parameter are obtained at each iteration of the estimation routine. The
     * proposal values for each parameters are obtained for each in turn using the estimated values from the
     * previous iteration. For example, a proposal difficulty estimate for itemA is obtained in iteration k+1
     * using estimates from iteration k. Then, a proposal difficulty estimate for itemB is obtained in iteration k+1
     * using estimates from iteration k (even though a new estimate exists for itemA). After obtaining proposal
     * values for every item on the test, the proposal values can be accepted as the new parameter estimates. This
     * method must be called to accept the proposal values as the new estimates.
     *
     * Return the largest difference between the old and new parameters.
     *
     */
    public double acceptAllProposalValues();

    /**
     * Gets item discrimination.
     *
     * @return item discrimination.
     */
    public double getDiscrimination();

    /**
     * Set discrimination parameter to an existing value. If you are using this method to fix an item parameter
     * during estimation, you must also set the proposal value with {@link #setProposalDiscrimination(double)}.
     *
     */
    public void setDiscrimination(double discrimination);

    /**
     * Set the proposed discrimination estimate.
     *
     * @param discrimination proposed item discrimination value.
     */
    public void setProposalDiscrimination(double discrimination);

    /**
     * Gets the standard error for the item discrimination estimate.
     *
     * @return item discrimination standard error.
     */
    public double getDiscriminationStdError();

    /**
     * The standard error may be computed external to the class. It can be set to a specific value with this method.
     *
     * @param StdError item discrimination standard error.
     */
    public void setDiscriminationStdError(double StdError);

    /**
     * Gets the pseudo-guessing (i.e. lower asymptote) parameter.
     *
     * @return guessing parameter.
     */
    public double getGuessing();

    /**
     * Set lower asymptote parameter to an existing value. If you are using this method to fix an item parameter
     * during estimation, you must also set the proposal value in {@link #setProposalGuessing(double)}.
     *
     */
    public void setGuessing(double guessing);

    /**
     * A proposal guessing parameter value is obtained during each iteration of the estimation routine. This method
     * sets the proposal value.
     *
     * @param guessing proposed guessing parameter estimate.
     */
    public void setProposalGuessing(double guessing);

    /**
     * Gets the guessing parameter estimate standard error.
     *
     * @return guessing parameter estimate standard error.
     */
    public double getGuessingStdError();

    /**
     * The guessing parameter standard error may be computed external to the class. Use this method to set the
     * standard error to a particular value.
     *
     * @param StdError standard error for the guessing parameter estimate.
     */
    public void setGuessingStdError(double StdError);

    /**
     * Gets the slipping (i.e. upper asymptote) parameter.
     *
     * @return slipping parameter.
     */
    public double getSlipping();

    /**
     * Set upper asymptote parameter to an existing value. If you are using this method to fix an item parameter
     * during estimation, you must also set the proposal value in {@link #setProposalSlipping(double)}.
     *
     */
    public void setSlipping(double slipping);

    /**
     * A proposal slipping parameter value is obtained during each iteration of the estimation routine. This method
     * sets the proposal value.
     *
     * @param slipping proposed slipping parameter estimate.
     */
    public void setProposalSlipping(double slipping);

    /**
     * Gets the slipping parameter estimate standard error.
     *
     * @return slipping parameter estimate standard error.
     */
    public double getSlippingStdError();

    /**
     * The slipping parameter standard error may be computed external to the class. Use this method to set the
     * standard error to a particular value.
     *
     * @param StdError standard error for the slipping parameter estimate.
     */
    public void setSlippingStdError(double StdError);

    public void setStepParameters(double[] step);

    public void setProposalStepParameters(double[] step);

    /**
     * Polytomous item response models may have step parameters. Gets the array of step parameters. Model that
     * do not have step parameters must implement an empty method
     *
     * @return array of step parameters.
     */
    public double[] getStepParameters();

    /**
     * Polytomous item response models may use an overall item difficulty parameter and two or more threshold
     * parameters. The difficulty and threshold parameters can be combined to produce the step parameters.
     *
     * @return array of threshold parameters.
     */
    public double[] getThresholdParameters();

    /**
     * Gets that standard errors for each step parameter estimate.
     *
     * @return an array of step standard errors.
     */
    public double[] getStepStdError();

    /**
     * Sets the standard error for the step parameter estimates.
     *
     * @param stdError an array of standard errors for the step parameters.
     */
    public void setStepStdError(double[] stdError);

    /**
     * Sets the threshold parameters to particular values. These values will be updated during estimateion unless
     * the item is fixed in {@link #setFixed(boolean)}.
     *
     * @param thresholds array of threshold parameters.
     */
    public void setThresholdParameters(double[] thresholds);

    /**
     * Sets the proposed threshold parameters estimates to particular values. These values will be updated during
     * estimation unless the item is fixed in {@link #setFixed(boolean)}.
     *
     * @param thresholds array of proposed threshold parameter estimates.
     *
     */
    public void setProposalThresholds(double[] thresholds);

    /**
     * Gets the array of standard errors fort eh threshold parameter estimates.
     *
     * @return array of standard errors.
     */
    public double[] getThresholdStdError();

    /**
     * Set the threshold standard errors.
     *
     * @param stdError an array of standard errors for the threshold paramter estimates.
     */
    public void setThresholdStdError(double[] stdError);

    /**
     * A polytomous item is scored with two or more ordinal categories such as 0, 1, 2, 3 or 1, 2, 3, 4, 5. The
     * score weights are not the same as the array index of the threshold or step parameter estimates. They are
     * weights applied to each category. The score weights are set with this method. If no score weights are
     * set, then the default score wieghts are typically 0, 1, 2, and so on.
     *
     * @param scoreWeight an array of category score weights.
     * @throws DimensionMismatchException
     */
    public void setScoreWeights(double[] scoreWeight)throws DimensionMismatchException;

    /**
     * Sets the name of the item.
     * @param name an item name
     */
    public void setName(VariableName name);

    /**
     * Gets the name of the item.
     * @return item name.
     */
    public VariableName getName();

    public void setLabel(VariableLabel label);

    public VariableLabel getLabel();

    /**
     * An item may be assigned to a group of items. The group membership code is the group ID. This method sets
     * the groupId for this item.
     *
     * @param groupId the group ID.
     */
    public void setGroupId(String groupId);

    /**
     * Gets the item group ID code.
     *
     * @return item group ID.
     */
    public String getGroupId();

    /**
     * Gets the lowest possible score weight.
     *
     * @return minimum score weight.
     */
    public double getMinScoreWeight();

    /**
     * Gets the largest possible score weight.
     *
     * @return maximum score weight.
     */
    public double getMaxScoreWeight();

    /**
     * Gets an array fo score weights.
     *
     * @return
     */
    public byte[] getScoreWeights();

    public void setItemFitStatistic(ItemFitStatistic fitStatistic);

    public ItemFitStatistic getItemFitStatistic();

    /**
     * Computes the first derivative with respect to person ability.
     *
     * @param theta a person ability value.
     * @return first derivative wrt theta.
     */
    public double derivTheta(double theta);

    /**
     * Computes the item information function at theta.
     *
     * @param theta person ability value.
     * @return item information at theta.
     */
    public double itemInformationAt(double theta);


    /**
     * Gets the type of item response model.
     *
     * @return type of item response model.
     */
    public IrmType getType();

    //====================================================================================================
    // Methods below are mainly used for implementation of IRT linking methods
    //====================================================================================================

    /**
     * Computes probability of a response under a linear transformation. This method is mainly used for the
     * characteristic curve linking methods (see {@link com.itemanalysis.psychometrics.irt.equating.StockingLordMethod}).
     * It applies the linear transformation such that the New form is transformed to the Old Form.
     *
     * @param theta examinee proficiency value
     * @param response target category
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return probability of a response using transformed values.
     */
    public double tStarProbability(double theta, int response, double intercept, double slope);

    /**
     * Computes probability of a response under a linear transformation. This method is mainly used for the
     * characteristic curve linking methods (see {@link com.itemanalysis.psychometrics.irt.equating.StockingLordMethod}).
     * It applies the linear transformation such that the Old form is transformed to the New Form.
     *
     * @param theta examinee proficiency value
     * @param response target category
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return probability of a response using transformed values.
     */
    public double tSharpProbability(double theta, int response, double intercept, double slope);

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
    public double tStarExpectedValue(double theta, double intercept, double slope);

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
    public double tSharpExpectedValue(double theta, double intercept, double slope);

    /**
     * Mean/sigma linking coefficients are computed from teh mean and standard deviation of item difficulty.
     * The summary statistics are computed in a storeless manner. This method allows for teh incremental
     * update to item difficulty summary statistics by combining them with other summary statistics.
     *
     * @param mean item difficulty mean.
     * @param sd item difficulty standard deviation.
     */
    public void incrementMeanSigma(Mean mean, StandardDeviation sd);

    /**
     * Mean/mean linking coefficients are computed from teh mean item difficulty and mean item discrimination.
     * The summary statistics are computed in a storeless manner. This method allows for teh incremental
     * update to item difficulty summary statistics by combining them with other summary statistics.
     *
     * @param meanDiscrimination item discrimination mean.
     * @param meanDifficulty item difficulty mean.
     */
    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty);

}

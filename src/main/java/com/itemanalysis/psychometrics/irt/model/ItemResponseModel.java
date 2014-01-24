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

public interface ItemResponseModel {

    public double probability(double theta, int response);

    public double expectedValue(double theta);

    /**
     * Linear transformation of item parameters.
     *
     * @param intercept
     * @param slope
     */
    public void scale(double intercept, double slope);

    public int getNumberOfParameters();

    public int getNcat();

    public boolean isFixed();

    public void setFixed(boolean isFixed);

    public double getDifficulty();

    /**
     * Set difficulty parameter to an existing value. If you are using this method to fix an
     * item parameter during estimation, you must also set the proposal value.
     * @return
     */
    public void setDifficulty(double difficulty);

    public double getProposalDifficulty();

    public void setProposalDifficulty(double difficulty);

    public double getDifficultyStdError();

    public void setDifficultyStdError(double StdError);

    public void acceptAllProposalValues();

    public double getDiscrimination();

    /**
     * Set discrimination parameter to an existing value. If you are using this method to fix an
     * item parameter during estimation, you must also set the proposal value.
     * @return
     */
    public void setDiscrimination(double discrimination);

    public void setProposalDiscrimination(double discrimination);

    public double getDiscriminationStdError();

    public void setDiscriminationStdError(double StdError);

    public double getGuessing();

    /**
     * Set lower asymptote parameter to an existing value. If you are using this method to fix an
     * item parameter during estimation, you must also set the proposal value.
     * @return
     */
    public void setGuessing(double guessing);

    public void setProposalGuessing(double guessing);

    public double getGuessingStdError();

    public void setGuessingStdError(double StdError);

    public double[] getStepParameters();

    public double[] getThresholdParameters();

    public double[] getStepStdError();

    public void setStepStdError(double[] stdError);

    public void setThresholdParameters(double[] thresholds);

    public void setProposalThresholds(double[] thresholds);

    public double[] getThresholdStdError();

    public void setThresholdStdError(double[] stdError);

    public void setScoreWeights(double[] scoreWeight)throws DimensionMismatchException;

    public void setName(VariableName name);

    public VariableName getName();

    public void setGroupId(String groupId);

    public String getGroupId();

    public double getMinScoreWeight();

    public double getMaxScoreWeight();

    public byte[] getScoreWeights();

    public double derivTheta(double theta);

    public double itemInformationAt(double theta);

    //====================================================================================================
    // Methods below are mainly used for implementation of IRT linking methods
    //====================================================================================================

    public double tStarProbability(double theta, int response, double intercept, double slope);

    public double tSharpProbability(double theta, int response, double intercept, double slope);

    public double tStarExpectedValue(double theta, double intercept, double slope);

    public double tSharpExpectedValue(double theta, double intercept, double slope);

    public void incrementMeanSigma(Mean mean, StandardDeviation sd);

    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty);

    public IrmType getType();

}

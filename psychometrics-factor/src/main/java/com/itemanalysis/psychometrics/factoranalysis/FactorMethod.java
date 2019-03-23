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
package com.itemanalysis.psychometrics.factoranalysis;

import com.itemanalysis.psychometrics.data.VariableName;

/**
 * This interface allows for a variety of methods for estimating parameters in exploratory
 * factor analysis.
 */
public interface FactorMethod {


    public double[] getStartValues();

    public double[] getSquaredMultipleCorrelation();

    public String printStartValues();

    /**
     * A method for estimating parameters. The returned value is the value of the criterion function at the 
     * estimated values. It should be the smallest possible value. This value is returned for display in the output.
     *
     * @return value of the criterion function at the estimated values
     */
    public double estimateParameters();

    /**
     * Return a factor loading for a particular item.
     *
     * @param i variable index of the factor loading
     * @param j factor index of the loading
     * @return
     */
    public double getFactorLoadingAt(int i, int j);

    /**
     * Returns all factor loadings for a particular factor
     *
     * @param factorID position number of factor of interest.
     * @return array of factor loadings.
     */
    public double[] getLoadingsForFactorAt(int factorID);

    /**
     * Returns the variabl's uniqueness (i.e. residual value)
     *
     * @param i variable index of the uniqueness
     * @return
     */
    public double getUniquenessAt(int i);

    /**
     * Returns the communality (sum of squared factor laodings) for a variable. The sum is computed over factors.
     *
     * @param i index of the variable for which the communality is sought.
     * @return communality for a variable.
     */
    public double getCommunalityAt(int i);

    /**
     * Sum of squared factor loading for a particular factor. The sum is computed over items.
     *
     * @param j index of the factor for which the computation is needed.
     * @return sum of squared factor loadings.
     */
    public double getSumsOfSquaresAt(int j);

    /**
     * Proportion of explained variance
     *
     * @param j index of the factor for which the explained variance is computed
     * @return
     */
    public double getProportionOfExplainedVarianceAt(int j);

    /**
     * Returns the proportion of total variance for a factor.
     *
     * @param j ndex of the factor for which the proportion of variance is computed
     * @return
     */
    public double getProportionOfVarianceAt(int j);

    /**
     * Returns the name of the variable at index.
     *
     * @param index position (zero based) of variable for which name is sought.
     * @return variable name.
     */
    public VariableName getVariableNameAt(int index);

    public void setVariableNameAt(int index, VariableName name);

}

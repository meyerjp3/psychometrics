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
package com.itemanalysis.psychometrics.cfa;

import com.itemanalysis.psychometrics.data.VariableInfo;
import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public interface ConfirmatoryFactorAnalysisEstimator extends DifferentiableMultivariateFunction {

    public final static int UNWEIGHTED_LEAST_SQUARES = 1;

    public final static int GENERALIZED_LEAST_SQUARES = 2;

    public final static int MAXIMUM_LIKELIHOOD = 3;

    public double fMin();

    public double[][] residuals();

    public double[][] squaredResiduals();

    public double meanSquaredResidual();

    public double sumMatrix(RealMatrix matrix);

    public double sumSquaredElements(RealMatrix matrix);

    public double chisquare();

    public double pvalue();

    public double mcdonaldOmega();

    public double gfi();

    public double agfi();

    public double rmsea();

    public double aic();

    public double bic();

    public double degreesOfFreedom();

    public String printEstimates(ArrayList<VariableInfo> items);

    @Override
    public String toString();

}

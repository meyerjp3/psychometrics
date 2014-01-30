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


import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public interface ConfirmatoryFactorAnalysisModel {

    public final static int CONGENERIC = 1;

    public final static int TAU_EQUIVALENT = 2;

    public final static int PARALLEL = 3;

    public int getNumberOfItems();

    public double[] getFactorLoading();

    public double[] getErrorVariance();
    
    public int getFactorLoadingSize();

    public int getErrorVarianceSize();

    public void setGradient(RealMatrix factorLoadingFirstDerivative, RealMatrix errorVarianceFirstDerivative, double[] gradient);

    public void setParameters(double[] argument);

    public void setInitialValues();

    public void setInitialFactorLoading(double[] inits);

    public double[] getInitialValuesVector();

    public int getNumberOfParameters();

    public RealMatrix getImpliedCovariance(double[] argument);

    public RealMatrix getBeta(double[] argument);

    public String getName();

    public double[] getGradient(RealMatrix factorLoadingFirstDerivative, RealMatrix errorVarianceFirstDerivative);

}

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
package com.itemanalysis.psychometrics.kernel;

import com.itemanalysis.psychometrics.data.DefaultItemScoring;
import com.itemanalysis.psychometrics.data.ItemScoring;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.exceptions.ItemScoringException;
import com.itemanalysis.psychometrics.quadrature.UniformQuadratureRule;

public class KernelRegressionItem {

    private boolean continuousItem = false;

    private VariableAttributes variableAttributes = null;

    private ItemScoring itemScoring = new DefaultItemScoring();

    private KernelFunction kernel = null;

    private Bandwidth bandwidth = null;

    private UniformQuadratureRule uniform = null;

    private KernelRegression expectedScore = null;

    public KernelRegressionItem(VariableAttributes variableAttributes, KernelFunction kernel, Bandwidth bandwidth, UniformQuadratureRule uniform){
        this.variableAttributes = variableAttributes;
        this.kernel = kernel;
        this.bandwidth = bandwidth;
        this.uniform = uniform;
        this.itemScoring = variableAttributes.getItemScoring();
        initializeCategories();
    }

    public void initializeCategories(){
        expectedScore = new KernelRegression(kernel, bandwidth, uniform);
    }

    public void increment(double x, Object y)throws ItemScoringException {
        KernelRegression kreg;
        double score;

        //increment item
        score = itemScoring.computeItemScore(y);
        expectedScore.increment(x, score);
    }

    public double[] getExpectedValues(){
        return expectedScore.value();
    }

    public double[] getValues(Object obj){
        return expectedScore.value();
    }

    public double[] getPoints(){
        return expectedScore.getPoints();
    }

    public double[][] getSeriesForChart(){
        double[][] series = new double[2][];
        series[0] = getPoints();
        series[1] = getExpectedValues();
        return series;
    }

    public double getMinimumPossibleScore(){
        return itemScoring.minimumPossibleScore();
    }

    public double getMaximumPossibleScore(){
        return itemScoring.maximumPossibleScore();
    }



}

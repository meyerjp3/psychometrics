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

import java.util.Iterator;
import java.util.TreeMap;

public class KernelRegressionCategories {

    private boolean continuousItem = false;

    private VariableAttributes variableAttributes = null;

    private ItemScoring itemScoring = new DefaultItemScoring();

    private KernelFunction kernel = null;

    private Bandwidth bandwidth = null;

    private UniformQuadratureRule uniform = null;

    private TreeMap<Object, KernelRegression> kernelRegressionMap = null;

    private KernelRegression expectedScore = null;

    public KernelRegressionCategories(VariableAttributes variableAttributes, KernelFunction kernel, Bandwidth bandwidth, UniformQuadratureRule uniform){
        this.variableAttributes = variableAttributes;
        this.kernel = kernel;
        this.bandwidth = bandwidth;
        this.uniform = uniform;
        this.itemScoring = variableAttributes.getItemScoring();
        kernelRegressionMap = new TreeMap<Object, KernelRegression>();
        initializeCategories();
    }

    public void initializeCategories(){
        expectedScore = new KernelRegression(kernel, bandwidth, uniform);

        //add all categories to map
        Iterator<Object> iter = itemScoring.categoryIterator();
        Object obj = null;
        while(iter.hasNext()){
            obj = iter.next();
            kernelRegressionMap.put(obj, new KernelRegression(kernel, bandwidth, uniform));
        }
    }

    public void increment(double x, Object y)throws ItemScoringException {
        KernelRegression kreg;
        double score;

        //increment all categories
        for(Object o : kernelRegressionMap.keySet()){
            kreg = kernelRegressionMap.get(o);
            score = itemScoring.computeCategoryScore(o, y);
            kreg.increment(x, score);
        }

        //increment item
        score = itemScoring.computeItemScore(y);
        expectedScore.increment(x, score);
    }

    public double[] getExpectedValues(){
        return expectedScore.value();
    }

    public double[] getValues(Object obj){
        KernelRegression kreg = kernelRegressionMap.get(obj);
        return kreg.value();
    }

    public double[] getPoints(Object obj){
        KernelRegression kreg = kernelRegressionMap.get(obj);
        return kreg.getPoints();
    }

    public TreeMap<Object, KernelRegression> getRegressionMap(){
        return kernelRegressionMap;
    }

    public double getScoreValue(Object category)throws ItemScoringException{
        return itemScoring.computeItemScore(category);
    }

    public  double getMinimumPossibleScore(){
        return itemScoring.minimumPossibleScore();
    }

    public double getMaximumPossibleScore(){
        return itemScoring.maximumPossibleScore();
    }

}

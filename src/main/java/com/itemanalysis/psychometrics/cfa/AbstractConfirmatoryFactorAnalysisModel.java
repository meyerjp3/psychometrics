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

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public abstract class AbstractConfirmatoryFactorAnalysisModel implements ConfirmatoryFactorAnalysisModel{
    
    public double[] factorLoading = null;

    public double[] errorVariance = null;
    
    protected int nItems = 0;

    public AbstractConfirmatoryFactorAnalysisModel(int nItems){
        this.nItems = nItems;
        this.factorLoading = new double[nItems];
        this.errorVariance = new double[nItems];
        this.setInitialValues();
    }

    public void setInitialValues(){
        for(int i=0;i<nItems;i++){
            factorLoading[i]=1.0+Math.random();//make this random?
            errorVariance[i]=1.0;//make this random?
        }
    }

    public int getNumberOfItems(){
        return nItems;
    }

    public double[] getFactorLoading(){
        return factorLoading;
    }

    public double[] getErrorVariance(){
        return errorVariance;
    }

    public int getFactorLoadingSize(){
        return factorLoading.length;
    }

    public int getErrorVarianceSize(){
        return errorVariance.length;
    }

}

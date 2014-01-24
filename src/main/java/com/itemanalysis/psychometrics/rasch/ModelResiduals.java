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
package com.itemanalysis.psychometrics.rasch;

import com.itemanalysis.psychometrics.data.VariableName;

import java.util.LinkedHashMap;

@Deprecated
public class ModelResiduals {

     private RatingScaleModel rsm = null;

    /**
     * Map of all items
     */
    private LinkedHashMap<VariableName, RatingScaleItem> items = null;


    public ModelResiduals(LinkedHashMap<VariableName, RatingScaleItem> items){
        this.items = items;
        rsm = new RatingScaleModel();
    }

    /**
     * Computes residual for a person and item. A null evaluate is returned for a person
     * missing an item response. Call this method for each person.
     *
     * @param theta
     * @param score
     * @return
     */
    public Double value(double theta, byte score, double difficulty, double[] thresholds){
        Double residual = null;
        double expectedValue = 0.0;
        if(score>-1){
            expectedValue = rsm.expectedValue(theta, difficulty, thresholds);
            residual = score-expectedValue;
        }else{
            residual = null;
        }
        return residual;
    }

}

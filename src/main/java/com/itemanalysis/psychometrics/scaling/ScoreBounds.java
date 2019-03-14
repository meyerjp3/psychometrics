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
package com.itemanalysis.psychometrics.scaling;

import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class ScoreBounds {

    public Double minPossibleScore = Double.NEGATIVE_INFINITY;

    public Double maxPossibleScore = Double.POSITIVE_INFINITY;
    
    public int precision = 4;

    /**
     * Use this constructor for the default bounds of negative and positive infinity
     */
    public ScoreBounds(){
        this.precision = 2;
    }

    /**
     * Use this constructor for the default bounds of negative and positive infinity
     * It also allows precision to be set.
     */
    public ScoreBounds(int precision){
        this.precision = precision;
    }

    /**
     * Use this constructor for providing starting values for an incremental update.
     * Uses default precision.
     *
     * @param minPossibleScore starting rho for smallest possible score
     * @param maxPossibleScore starting rho for largest possible score
     */
    public ScoreBounds(Double minPossibleScore, Double maxPossibleScore){
        if(minPossibleScore>maxPossibleScore){
            this.minPossibleScore = maxPossibleScore;
            this.maxPossibleScore = minPossibleScore;
        }else{
            this.minPossibleScore = minPossibleScore;
            this.maxPossibleScore = maxPossibleScore;
        }
    }

    /**
     * Use this constructor for providing starting values for an incremental update.
     *
     * @param minPossibleScore starting rho for smallest possible score
     * @param maxPossibleScore starting rho for largest possible score
     * @param precision amount of precision in score display
     */
    public ScoreBounds(Double minPossibleScore, Double maxPossibleScore, int precision){
        if(minPossibleScore>maxPossibleScore){
            this.minPossibleScore = maxPossibleScore;
            this.maxPossibleScore = minPossibleScore;
        }else{
            this.minPossibleScore = minPossibleScore;
            this.maxPossibleScore = maxPossibleScore;
        }
        this.precision = precision;
    }

    public ScoreBounds(ArrayList<VariableAttributes> variables, int precision){
        minPossibleScore = 0.0;
        maxPossibleScore = 0.0;
        this.precision = precision;
        this.incrementByItemScores(variables);
    }

    /**
     * increment according to item scores
     *
     * @param minValue smallest possible item score
     * @param maxValue largest possible item score
     */
    public void increment(Double minValue, Double maxValue){
        if(minValue>maxValue){
            minPossibleScore += maxValue;
            maxPossibleScore += minValue;
        }else{
            minPossibleScore += minValue;
            maxPossibleScore += maxValue;
        }
    }

    public final void incrementByItemScores(ArrayList<VariableAttributes> variables){
        for(VariableAttributes v : variables){
            if(v.getType().getItemType()== ItemType.BINARY_ITEM || v.getType().getItemType()==ItemType.POLYTOMOUS_ITEM){
                minPossibleScore+=v.getMinimumPossibleItemScore();
                maxPossibleScore+=v.getMaximumPossibleItemScore();
            }
        }
    }

    /**
     * Constraint the min and max possible values.
     *
     * @param minPossibleScore
     * @param maxPossibleScore
     */
    public void setConstraints(Double minPossibleScore, Double maxPossibleScore){
        if(minPossibleScore>maxPossibleScore){
            this.minPossibleScore = Math.max(this.minPossibleScore, maxPossibleScore);
            this.maxPossibleScore = Math.min(this.maxPossibleScore, minPossibleScore);
        }else{
            this.minPossibleScore = Math.max(this.minPossibleScore, minPossibleScore);
            this.maxPossibleScore = Math.min(this.maxPossibleScore, maxPossibleScore);
        }
    }

    public Double getMaxPossibleScore(){
        return maxPossibleScore;
    }

    public Double getMinPossibleScore(){
        return minPossibleScore;
    }

    public Double checkConstraints(Double value){
        if(value<minPossibleScore){
            return Precision.round(minPossibleScore, precision);
        }else if(value>maxPossibleScore){
            return Precision.round(maxPossibleScore, precision);
        }else{
            return Precision.round(value, precision);
        }
    }

    public Double checkPrecisionOnly(Double value){
        return Precision.round(value, precision);
    }

}

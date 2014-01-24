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
package com.itemanalysis.psychometrics.measurement;

import com.itemanalysis.psychometrics.polycor.PearsonCorrelation;
import com.itemanalysis.psychometrics.polycor.PolyserialPlugin;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer
 */
public class ItemStats {

    private Object id = null;

    /**
     * Proportion endorsing item/category
     */
    private Mean mean = null;

    /**
     * Standard deviation of endorsed category
     */
    private StandardDeviation sd = null;

    /**
     * Item/Category - total correlation
     */
    private PearsonCorrelation pointBiserial = null;

    /**
     * Item/Category - total correlation
     */
    private PolyserialPlugin polyserial = null;

    private boolean biasCorrection = true;

    private boolean pearson = true;

    public ItemStats(Object id, boolean biasCorrection, boolean pearson, boolean continuousItem){
        this.biasCorrection = biasCorrection;
        this.pearson = pearson;
        mean = new Mean();
        sd = new StandardDeviation();

        if(continuousItem) this.pearson = true;

        if(this.pearson){
            pointBiserial = new PearsonCorrelation();
        }else{
            polyserial = new PolyserialPlugin();
        }
    }

    public void increment(double testScore, double itemScore){
        mean.increment(itemScore);
        sd.increment(itemScore);
        if(pearson){
            pointBiserial.increment(testScore, itemScore);
        }else{
            polyserial.increment(testScore, (int)itemScore);
        }
    }

    public Object getId(){
        return id;
    }

    public double getDifficulty(){
        return mean.getResult();
    }

    public double getStdDev(){
        return sd.getResult();
    }

    public double getDiscrimination(){
        if(pearson){
            if(biasCorrection) return pointBiserial.correctedValue();
            return pointBiserial.value();
        }else{
            if(biasCorrection) return polyserial.spuriousCorrectedValue();
            return polyserial.value();
        }

    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
//        f.format("%27s", id.toString() + "(" + category.scoreValue() + ")");f.format("%2s", " ");  //list original values
        f.format("% 10.4f", getDifficulty()); f.format("%2s", " ");//category proportion endorsing
        f.format("% 10.4f", getStdDev()); f.format("%2s", " ");//category standard deviation
        f.format("% 10.4f", getDiscrimination());f.format("%2s", " ");  //item discrimination

        return f.toString();
    }

}

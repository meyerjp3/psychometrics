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
package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.statistics.PearsonCorrelation;
import com.itemanalysis.psychometrics.statistics.PolyserialCorrelationPlugin;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Formatter;

/**
 * This class computes statistics for individual item response options. It is used by the
 * ClassicalItem class in a classical item analysis.
 *
 * @author J. Patrick Meyer
 */
public class ItemStats {

    /**
     * The string or double value respresenting the value of the response option
     */
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
    private PolyserialCorrelationPlugin polyserial = null;

    /**
     * Use a biased corrected (n-1) standard deviation if true. Use baised (n) strandard deviation if not.
     */
    private boolean biasCorrection = true;

    private boolean dIndex = false;

    private Mean upper = null;

    private Mean lower = null;

    /**
     * Use a Pearson type of correlation if true. Otherwise use a polyserial type of correlation.
     */
    private boolean pearson = true;

    public ItemStats(Object id, boolean biasCorrection, boolean pearson, boolean continuousItem){
        this(id, biasCorrection, pearson, continuousItem, false);
    }

    public ItemStats(Object id, boolean biasCorrection, boolean pearson, boolean continuousItem, boolean dIndex){
        this.biasCorrection = biasCorrection;
        this.pearson = pearson;
        this.dIndex = dIndex;
        mean = new Mean();
        sd = new StandardDeviation();

        if(dIndex){
            upper = new Mean();
            lower = new Mean();
        }


        if(continuousItem) this.pearson = true;

        if(this.pearson){
            pointBiserial = new PearsonCorrelation();
        }else{
            polyserial = new PolyserialCorrelationPlugin();
        }
    }

    /**
     * Incrementally update the item statistics
     *
     * @param testScore sum score on teh total test
     * @param itemScore score on teh individual item (or response option)
     */
    public void increment(double testScore, double itemScore){
        mean.increment(itemScore);
        sd.increment(itemScore);
        if(pearson){
            pointBiserial.increment(testScore, itemScore);
        }else{
            polyserial.increment(testScore, (int)itemScore);
        }
    }

    /**
     * The D index must be incremented in a second loop after test scores have been computed and
     * percentiles computed. Use this method to increment the D index.
     *
     * @param itemScore item response score
     * @param testScore test score
     * @param lowCut test score cutoff value for lower group
     * @param upperCut test score cutoff value for the upper group
     */
    public void incrementDindex(double itemScore, double testScore, double lowCut, double upperCut){

        if(dIndex){
            if(testScore <= lowCut){
                lower.increment(itemScore);
            }else if (testScore >= upperCut){
                upper.increment(itemScore);
            }
        }
    }

    /**
     *
     * @return id of the response option
     */
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

    public double getDindexLower(){
        if(dIndex){
            return lower.getResult();
        }else{
            return Double.NaN;
        }
    }

    public double getDindexUpper(){
        if(dIndex){
            return upper.getResult();
        }else{
            return Double.NaN;
        }
    }

    public double getDIndex(){
        if(dIndex){
            return upper.getResult()-lower.getResult();
        }else{
            return Double.NaN;
        }
    }

    /**
     * A string that contains all of the item statistics.
     *
     * @return string of estimated statistics
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("% 10.4f", getDifficulty()); f.format("%2s", " ");//category proportion endorsing
        f.format("% 10.4f", getStdDev()); f.format("%2s", " ");//category standard deviation
        f.format("% 10.4f", getDiscrimination());f.format("%2s", " ");  //item discrimination

        return f.toString();
    }

}

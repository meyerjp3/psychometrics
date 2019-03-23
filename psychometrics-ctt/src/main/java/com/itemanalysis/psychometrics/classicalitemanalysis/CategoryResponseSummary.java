/**
 * Copyright 2015 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.statistics.PearsonCorrelation;
import com.itemanalysis.psychometrics.statistics.PolyserialCorrelationPlugin;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class CategoryResponseSummary {

    private VariableName itemName = null;

    private Object categoryID = null;

//    private ArrayList<Double> responseScore = null;

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
    private PearsonCorrelation pearsonCorrelation = null;

    /**
     * Item/Category - total correlation
     */
    private PolyserialCorrelationPlugin polyserialCorrelation = null;

    /**
     * Adjust the item discrimination for spuriousness if true. Otherwise make no adjustment
     */
    private boolean correctForSpuriousness = true;

    private Mean upper = null;

    private Mean lower = null;

    private DiscriminationType discriminationType = DiscriminationType.PEARSON;

    private double observedMinItemScore = 0;

    private double observedMaxItemScore = 1;

    public CategoryResponseSummary(VariableName itemName, Object categoryID, boolean correctForSpuriousness, DiscriminationType discriminationType){
        this.itemName = itemName;
        this.categoryID = categoryID;

        this.correctForSpuriousness = correctForSpuriousness;
        this.discriminationType = discriminationType;
        mean = new Mean();
        sd = new StandardDeviation();

        if(discriminationType==DiscriminationType.DINDEX27 || discriminationType==DiscriminationType.DINDEX33){
            upper = new Mean();
            lower = new Mean();
        }else if(discriminationType==DiscriminationType.POLYSERIAL){
            polyserialCorrelation = new PolyserialCorrelationPlugin();
        }else{
            pearsonCorrelation = new PearsonCorrelation();
        }

    }

    /**
     * Use this incremental update for each response option.
     *
     * @param response the examinee response to an item
     * @param testScore the examinee's test score
     */
    public void increment(Object response, double testScore){
        if(categoryID.equals(response)){
            mean.increment(1.0);
            sd.increment(1.0);
            observedMaxItemScore = Math.max(observedMaxItemScore, 1.0);
            observedMinItemScore = Math.min(observedMinItemScore, 1.0);
            if(discriminationType==DiscriminationType.PEARSON){
                pearsonCorrelation.increment(testScore, 1.0);
            }else if(discriminationType==DiscriminationType.POLYSERIAL){
                polyserialCorrelation.increment(testScore, 1);
            }

        }else{
            mean.increment(0.0);
            sd.increment(0.0);
            observedMaxItemScore = Math.max(observedMaxItemScore, 0.0);
            observedMinItemScore = Math.min(observedMinItemScore, 0.0);
            if(discriminationType==DiscriminationType.PEARSON){
                pearsonCorrelation.increment(testScore, 0.0);
            }else if(discriminationType==DiscriminationType.POLYSERIAL){
                polyserialCorrelation.increment(testScore, 0);
            }

        }


    }

    /**
     * Use this incremental update if the item is continuous or the response is already scored.
     *
     * @param itemScore an examinee's item score
     * @param testScore an examinee's test score
     */
    public void increment(double itemScore, double testScore){
        mean.increment(itemScore);
        sd.increment(itemScore);
        observedMaxItemScore = Math.max(observedMaxItemScore, itemScore);
        observedMinItemScore = Math.min(observedMinItemScore, itemScore);
        if(discriminationType==DiscriminationType.PEARSON){
            pearsonCorrelation.increment(testScore, itemScore);
        }else if(discriminationType==DiscriminationType.POLYSERIAL){
            polyserialCorrelation.increment(testScore, (int) itemScore);
        }
    }


    public void incrementDindex(Object response, double testScore, double lowerBound, double upperBound){
        if(categoryID.equals(response)){
            if(testScore <= lowerBound){
                lower.increment(1.0);
            }else if (testScore >= upperBound){
                upper.increment(1.0);
            }
        }else{
            if(testScore <= lowerBound){
                lower.increment(0.0);
            }else if (testScore >= upperBound){
                upper.increment(0.0);
            }
        }
    }

    public double getMinimumObservedItemScore(){
        return observedMinItemScore;
    }

    public double getMaximumObservedItemScore(){
        return observedMaxItemScore;
    }

    /**
     * This method is called when computing teh D-index for the overall item.
     *
     * @param itemScore an examinee's item score
     * @param testScore an examinee's summed score
     * @param lowerBound the value at lower pth percentile
     * @param upperBound the value at the 100-pth percentile
     */
    public void incrementDindex(double itemScore, double testScore, double lowerBound, double upperBound){
        if(testScore <= lowerBound){
            lower.increment(itemScore);
        }else if (testScore >= upperBound){
            upper.increment(itemScore);
        }
    }

    public double getDifficulty(){
        return mean.getResult();
    }

    public double getStandardDeviation(){
        return sd.getResult();
    }

    public long getSampleSize(){
        return mean.getN();
    }

    /**
     * Returns the discrimination for the response category, which may also be the whole item.
     * @return discrimination value
     */
    public double getDiscrimination(){
        if(discriminationType==DiscriminationType.DINDEX27 || discriminationType==DiscriminationType.DINDEX33){
            return (upper.getResult() - lower.getResult())/(observedMaxItemScore-observedMinItemScore);
        }else if (discriminationType==DiscriminationType.POLYSERIAL){
            if(correctForSpuriousness) return polyserialCorrelation.spuriousCorrectedValue();
            return polyserialCorrelation.value();
        }else{
            if(correctForSpuriousness) return pearsonCorrelation.correctedValue();
            return pearsonCorrelation.value();
        }
    }

    public double getDindexLowerMean(){
        return lower.getResult();
    }

    public double getDindexUpperMean(){
        return upper.getResult();
    }

}

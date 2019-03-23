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

import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;

import java.util.Random;
import java.util.TreeMap;

/**
 * A RawScore object is created for every examinee. It includes the overall
 * raw score and any subscale scores computed for a examinee.
 *
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class RawScore  implements Comparable<Object>{


    /**
     * Maps responseVector to VariableInfo ArrayList in item analysis
     */
    private int[] resposneVectorIndex = null;

    /**
     * Vector of item responses for an examinee. Used to avoid repeated calls to database.
     */
    private Object[] responseVector = null;

    /**
     * Vector of scored item responses for an examinee.  Used to avoid repeated calls to database.
     */
    private Double[] scoreVector = null;

    private Double rawScore = 0.0;

    private TreeMap<String, Double> subscale = null;

    private int counter = 0;

    private int numberOfValidItems = 0;

    private DefaultLinearTransformation linear = null;

    public RawScore(int numberOfItems){
        resposneVectorIndex = new int[numberOfItems];
        responseVector = new Object[numberOfItems];
        scoreVector = new Double[numberOfItems];
        subscale = new TreeMap<String, Double>();
    }

    public RawScore(double rawScore){
        subscale = new TreeMap<String, Double>();
        this.rawScore=rawScore;
    }

    public void increment(Double score){
        if(!Double.isNaN(score)){
            rawScore+=score;
            numberOfValidItems++;
        }
    }

    /**
     * For holding an examinee's vector of item responses.
     * This method is used to avoid repeated calls to the database.
     * @param position
     * @param response
     * @param score
     */
    public void incrementResponseVector(Integer position, Object response, Double score){
        resposneVectorIndex[counter] = position;
        if(Double.isNaN(score)){
            responseVector[counter] = null;
            scoreVector[counter] = null;
        }else{
            responseVector[counter] = response;
            scoreVector[counter] = score;
        }
        counter++;
    }

    public void incrementSubScaleScore(String subscaleKey, double score){
        Double temp = subscale.get(subscaleKey);
        if(temp==null){
            if(!Double.isNaN(score)){
                temp = new Double(score);
                subscale.put(subscaleKey, temp);
            }
        }else{
            Double two = new Double(temp+score);
            subscale.put(subscaleKey, two);
        }
        
    }

    public void addLinearTransformation(DefaultLinearTransformation linear){
        this.linear = linear;
    }

    public double getSubscaleScoreAt(String subscaleKey){
        return subscale.get(subscaleKey);
    }

    public TreeMap<String, Double> getSubscaleScore(){
        return subscale;
    }

    public Double value(){
        return rawScore;
    }

    public Double meanValue(){
        return rawScore/(double)numberOfValidItems;
    }

    public Integer intValue(){
        Integer i = Double.valueOf(Math.floor(rawScore+0.5)).intValue();
        return i;
    }

    public Double transformedValue(){
        return linear.transform(rawScore);
    }

    public Double valueWithRandomUniformVariate(){
        Random rand = new Random();
        return rawScore+rand.nextDouble();
    }

    public Object[] getResponseVector(){
        return responseVector;
    }

    public Double[] getScoreVector(){
        return scoreVector;
    }

    public int[] getResponseVectorIndex(){
        return resposneVectorIndex;
    }

    public int compareTo(Object o){
        if(!(o instanceof RawScore))
            throw new ClassCastException("RawScore object expected");
        RawScore that = (RawScore)o;
		return this.value().compareTo(that.value());
	}

    @Override
    public boolean equals(Object o){
        if(!(o instanceof RawScore)) return false;
		if(o==this) return true;
		RawScore that = (RawScore)o;
        return this.value()==that.value();
    }

    @Override
    public int hashCode(){
		return rawScore.hashCode();
	}

}

/**
 * Copyright 2016 J. Patrick Meyer
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

import java.util.Formatter;
import java.util.Iterator;

/**
 * Stores the item responses and frequency of item resposes for an item. Provides methods for computing
 * overall summary statistics or statistics for each response option.
 *
 *
 */
@Deprecated
public class TextItemResponseSummary extends AbstractItemResponseSummary{


    public TextItemResponseSummary(VariableName variableName){
        super(variableName);
    }

    public void increment(int response){
        increment((double)response, 1.0);
    }

    public void increment(int response, double freqWeight){
        increment((double)response, freqWeight);
    }

    public void increment(double response){
        increment(response, 1.0);
    }

    public void increment(double response, double freqWeight){
        String resp = Double.valueOf(response).toString();
        Double freq = stringResponseMap.get(resp);
        if(null==freq) freq = new Double(0);
        freq = new Double(freq + freqWeight);
        stringResponseMap.put(resp, freq);
        totalFrequency += freqWeight;

        stringScoreMap.put(resp, response);
    }

    /**
     * A unit increment of the item reponse count.
     *
     * @param response
     */
    public void increment(String response){
        increment(response, 1.0);
    }

    /**
     * An increment of the item response count that uses a frequency weight.
     *
     * @param response an item response
     * @param freqWeight a frequency weight for the response
     */
    public void increment(String response, double freqWeight){
        Double freq = stringResponseMap.get(response);
        if(null==freq) freq = new Double(0);
        freq = new Double(freq + freqWeight);
        stringResponseMap.put(response, freq);
        totalFrequency += freqWeight;

        stringScoreMap.put(response, 0.0);
    }

    public void setScoreAt(String response, double score){
        stringScoreMap.put(response, score);
    }

    public Iterator<String> stringIterator(){
        return stringResponseMap.keySet().iterator();
    }

    public Iterator<Double> doubleIterator() {
        return null;
    }

    /**
     * Provides the weighted frequency of a response.
     *
     * @param response an item response
     * @return a frequency weight
     */
    public double getFrequencyAt(String response){
        return stringResponseMap.get(response);
    }

    public double getFrequencyAt(double response){
        return getFrequencyAt(Double.valueOf(response).toString());
    }

    public double getFrequencyAt(int response){
        return getFrequencyAt(Integer.valueOf(response).toString());
    }

    /**
     * Provides the weighted proportion of a response.
     *
     * @param response
     * @return
     */
    public double getProportionAt(String response){
        return getFrequencyAt(response)/totalFrequency;
    }

    public double getProportionAt(double response){
        return getFrequencyAt(response)/totalFrequency;
    }

    public double getProportionAt(int response){
        return getFrequencyAt(response)/totalFrequency;
    }

    public double getSampleStandardDeviationAt(String response){
        double p =getProportionAt(response);
        double v = (totalFrequency*p*(1-p))/(totalFrequency-1);
        return Math.sqrt(v);
    }

    /**
     * Item scores should only be accessed through this method.
     * Gets the score for a particular response. If a score has not been provided for a response and
     * the response is a Double, it will covert the response to a double and return it. If a score
     * has not been provided for the response and the response is not a double or integer, it will
     * return 0.0.
     *
     * @param response an item reponse
     * @return score for the item response
     */
    public double getScoreAt(String response){
        if(stringScoreMap.containsKey(response)){
            return stringScoreMap.get(response);
        }else{
            return 0.0;
        }
    }

    /**
     * Computes a weighted sample mean. If responses are letters or non-number
     * objects and no scoring is provided, then the sample mean will equal 0.
     *
     * @return sample mean
     */
    public double mean(){
        double sum = 0;
        double denom = 0;
        double freq = 0;

        for(String s : stringScoreMap.keySet()){
            freq = getFrequencyAt(s);
            sum += getScoreAt(s)*freq;
            denom += freq;
        }

        if(denom==0) return Double.NaN;
        return sum/denom;
    }

    /**
     * Computes a weighted sample variance (i.e. n-1 in denominator). If responses are letters or non-number
     * objects and no scoring is provided, then the sample variance will equal 0.
     *
     * @return sample variance
     */
    public double sampleVariance(){
        double mean = mean();
        if(Double.isNaN(mean)) return Double.NaN;

        double sumOfSquares = 0;
        double denom = 0;
        double score = 0;
        double freq = 0;
        double dev = 0;

        for(String s : stringScoreMap.keySet()){
            freq = getFrequencyAt(s);
            score = getScoreAt(s);
            dev = (score-mean);
            sumOfSquares += dev*dev*freq;
            denom += freq;
        }

        return sumOfSquares/(denom-1);
    }

    /**
     * Computes a weighted population variance (i.e. n in denominator). If responses are letters or non-number
     * objects and no scoring is provided, then the population variance will equal 0.
     *
     * @return sample variance
     */
    public double populationVariance(){
        double mean = mean();
        if(Double.isNaN(mean)) return Double.NaN;

        double sumOfSquares = 0;
        double denom = 0;
        double score = 0;
        double freq = 0;
        double dev = 0;

        for(String s : stringScoreMap.keySet()){
            freq = getFrequencyAt(s);
            score = getScoreAt(s);
            dev = (score-mean);
            sumOfSquares += dev*dev;
            denom += freq;
        }

        return sumOfSquares/denom;
    }

    /**
     * Sample standard deviation (i.e. n-1 in denominator. If responses are letters or non-number
     * objects and no scoring is provided, then the sample standard deviation will equal 0.
     *
     * @return
     */
    public double sampleStandardDeviation(){
        double var = sampleVariance();
        if(Double.isNaN(var)) return Double.NaN;
        return Math.sqrt(var);
    }

    /**
     * Population standard deviation (i.e. n in denominator). If responses are letters or non-number
     * objects and no scoring is provided, then the population standard deviation.
     * @return
     */
    public double populationStandardDeviation(){
        double var = populationVariance();
        if(Double.isNaN(var)) return Double.NaN;
        return Math.sqrt(var);
    }

    public VariableName getName(){
        return variableName;
    }

    public double getTotalFrequency(){
        return totalFrequency;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        Iterator<String> iter = stringResponseMap.keySet().iterator();
        String s = "";

        sb.append(getOutputString());
        f.format("%n");

        while(iter.hasNext()){
            s = iter.next();
            sb.append(getOutputStringAt(s));
            f.format("%n");
        }

        sb.append(getTotalFrequencyOutputString());
        f.format("%n");

        return f.toString();
    }

    /**
     * Formatted output for the item mean and standard deviation. Defaults to two decimal places.
     * @return formatted output
     */
    public String getOutputString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%20s", variableName.toString());f.format("%2s", "");
        f.format("%10.2f", mean());f.format("%2s", "");
        f.format("%10.2f", sampleStandardDeviation());
        return f.toString();
    }

    /**
     * Formatted output for a particlar response. It provides the proportion and sample standard deviation.
     * Defaults to two decimal places.
     *
     * @param response an item response
     * @return formatted output
     */
    public String getOutputStringAt(String response){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%20s", response.toString() + "(" + stringScoreMap.get(response) + ")"); f.format("%2s", "");
        f.format("%10.2f", getProportionAt(response)); f.format("%2s", "");
        f.format("%10.2f", getSampleStandardDeviationAt(response));
        return f.toString();
    }

    public String getTotalFrequencyOutputString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%20s", "Total Frequency");f.format("%2s", "");
        f.format("%10.2f", getTotalFrequency());f.format("%2s", "");
        f.format("%10s", "");
        return f.toString();
    }

}

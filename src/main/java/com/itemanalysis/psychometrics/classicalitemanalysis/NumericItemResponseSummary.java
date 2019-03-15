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
 * Summarizes item responses that are numeric. Provides frequencies, proportions, mean, variance, and standard
 * deviation for the entire item and for each individual response option.
 */
@Deprecated
public class NumericItemResponseSummary extends AbstractItemResponseSummary{

    public NumericItemResponseSummary(VariableName name){
        super(name);
    }

    /**
     * Increment frequency count of a response by one.
     *
     * @param response an item response that can be parsed as a double.
     */
    public void increment(String response){
        increment(response, 1.0);
    }

    /**
     * Increment frequency count of a response by an amount equal to freqWeight. It take a String
     * as input but it will be parsed as a double value.
     *
     * @param response an item response that can be parsed as a double
     * @param freqWeight frequency weight of the response
     * @throws NumberFormatException throws if string cannot be parsed into a double
     */
    public void increment(String response, double freqWeight)throws NumberFormatException{
        double resp = Double.parseDouble(response);
        Double freq = doubleResponseMap.get(resp);
        if(null==freq) freq = new Double(0);
        freq = new Double(freq + freqWeight);
        doubleResponseMap.put(resp, freq);
        totalFrequency += freqWeight;

        doubleScoreMap.put(resp, resp);
    }

    /**
     * Increment frequency count of a response by one.
     *
     * @param response an item resposne
     */
    public void increment(int response){
        increment(response, 1.0);
    }

    /**
     * Increment frequency count of a response by an amount equal to freqWeight.
     *
     * @param response an item response that will be converted to a double
     * @param freqWeight frequency weight of the response
     */
    public void increment(int response, double freqWeight){
        double resp = Integer.valueOf(response).doubleValue();
        increment(resp, freqWeight);

        doubleScoreMap.put(resp, resp);
    }

    /**
     * Increment frequency count of a response by one.
     *
     * @param response an item response
     */
    public void increment(double response){
        increment(response, 1.0);
    }

    /**
     * Increment frequency count of a response by an amount equal to freqWeight.
     *
     * @param response an item response
     * @param freqWeight the frequency weight for the response
     */
    public void increment(double response, double freqWeight){
        Double freq = doubleResponseMap.get(response);
        if(null==freq) freq = new Double(0);
        freq = new Double(freq + freqWeight);
        doubleResponseMap.put(response, freq);
        totalFrequency += freqWeight;

        doubleScoreMap.put(response, response);
    }

    /**
     * Sets the value of a response to that given by score. This score value will be used in computation
     * of summary statistics such as the mean and variance. If this method is not used, then the score
     * will be the same as the item response. This method takes a String as the response.
     *
     * @param response an item response
     * @param score a numeric value for the score
     * @throws NumberFormatException thrown if the item response cannot be parsed as a double.
     */
    public void setScoreAt(String response, double score)throws NumberFormatException{
        double resp = Double.parseDouble(response);
        doubleScoreMap.put(resp, score);
    }

    /**
     * Sets the value of a response to that given by score. This score value will be used in computation
     * of summary statistics such as the mean and variance. If this method is not used, then the score
     * will be the same as the item response. This version uses an int as the response.
     *
     * @param response an item response
     * @param score a numeric value for the response
     */
    public void setScoreAt(int response, double score){
        double resp = Integer.valueOf(response).doubleValue();
        doubleScoreMap.put(resp, score);
    }

    /**
     * Sets the value of a response to that given by score. This score value will be used in computation
     * of summary statistics such as the mean and variance. If this method is not used, then the score
     * will be the same as the item response. This method uses a double as the response.
     *
     * @param response an item response
     * @param score a numeric value for the response
     */
    public void setScoreAt(double response, double score){
        doubleScoreMap.put(response, score);
    }

    /**
     * Returns the score that is assigned to an item response. This method takes a String as the item response.
     *
     * @param response an item response
     * @return the score for the item response
     * @throws NumberFormatException thrown if the item response cannot be parsed as a double
     */
    public double getScoreAt(String response)throws NumberFormatException{
        double resp = Double.parseDouble(response);
        return doubleScoreMap.get(resp);
    }

    /**
     * Returns the score that is assigned to an item response. This method takes an int as the item response.
     *
     * @param response an item response
     * @return the score for an item response
     */
    public double getScoreAt(int response){
        double resp = Integer.valueOf(response).doubleValue();
        return doubleScoreMap.get(resp);
    }

    /**
     * Returns the score that is assigned to an item response. This method takes a double as the item response.
     *
     * @param response an item response
     * @return the score for the item response
     */
    public double getScoreAt(double response){
        return doubleScoreMap.get(response);
    }

    /**
     * The item sample mean.
     *
     * @return sample mean
     */
    public double getMean(){
        double sum = 0;
        double denom = 0;
        double freq = 0;

        for(Double d : doubleScoreMap.keySet()){
            freq = doubleResponseMap.get(d);
            sum += doubleScoreMap.get(d)*freq;
            denom += freq;
        }

        if(denom==0) return Double.NaN;
        return sum/denom;
    }

    /**
     * The item sample variance (i.e. n-1 in the denominator)
     *
     * @return sample variance
     */
    public double getVariance(){
        double mean = getMean();
        if(Double.isNaN(mean)) return Double.NaN;

        double sumOfSquares = 0;
        double denom = 0;
        double score = 0;
        double freq = 0;
        double dev = 0;

        for(Double d : doubleScoreMap.keySet()){
            freq = doubleResponseMap.get(d);
            score = doubleScoreMap.get(d);
            dev = (score-mean);
            sumOfSquares += dev*dev*freq;
            denom += freq;
        }

        return sumOfSquares/(denom-1);
    }

    /**
     * The item sample standard deviation (i.e. n-1 in the denominator)
     *
     * @return sample standard deviation
     */
    public double getStandardDeviation(){
        return Math.sqrt(getVariance());
    }

    /**
     * The item population variance (i.e. n in the denominator).
     *
     * @return population variance
     */
    public double getPopulationVariance(){
        double mean = getMean();
        if(Double.isNaN(mean)) return Double.NaN;

        double sumOfSquares = 0;
        double denom = 0;
        double score = 0;
        double freq = 0;
        double dev = 0;

        for(Double d : doubleScoreMap.keySet()){
            freq = doubleResponseMap.get(d);
            score = doubleScoreMap.get(d);
            dev = (score-mean);
            sumOfSquares += dev*dev*freq;
            denom += freq;
        }

        return sumOfSquares/denom;
    }

    /**
     * The item population standard deviation (i.e. n in the denominator).
     *
     * @return population standard deviation
     */
    public double getPopulationStandardDeviation(){
        return Math.sqrt(getPopulationVariance());
    }

    /**
     * Frequency of a particular item response, where the response is given as a String.
     *
     * @param response an item response
     * @return frequency of the response
     * @throws NumberFormatException thrown if response cannot be parsed as a double.
     */
    public double getFrequencyAt(String response)throws NumberFormatException{
        double resp = Double.parseDouble(response);
        return doubleResponseMap.get(resp);
    }

    /**
     * Frequency of a particular item response, where the response is given as an int.
     *
     * @param response and item response
     * @return frequency of the item response
     */
    public double getFrequencyAt(int response){
        double resp = Integer.valueOf(response).doubleValue();
        return doubleResponseMap.get(resp);
    }

    /**
     * Frequency of a particular item response, where the response is given as a double.
     *
     * @param response an item response
     * @return frequency of the response
     */
    public double getFrequencyAt(double response){
        return doubleResponseMap.get(response);
    }

    /**
     * Relative frequency of a particular item response, where the response is given as a String.
     *
     * @param response an item response
     * @return relative frequency of an item response
     * @throws NumberFormatException thrown if response cannot be parsed as a double
     */
    public double getProportionAt(String response)throws NumberFormatException{
        if(totalFrequency==0) return Double.NaN;
        double resp = Double.parseDouble(response);
        double freq = doubleResponseMap.get(resp);
        return freq/totalFrequency;
    }

    /**
     * Relative frequency of a particular item response, where the response is given as an int.
     *
     * @param response and item response
     * @return relative frequency of the item response
     */
    public double getProportionAt(int response){
        if(totalFrequency==0) return Double.NaN;
        double resp = Integer.valueOf(response).doubleValue();
        double freq = doubleResponseMap.get(resp);
        return freq/totalFrequency;
    }

    /**
     * Relative frequency of a particular item response, where the response is given as a double.
     *
     * @param response an item response
     * @return relative frequency of the item response
     */
    public double getProportionAt(double response){
        if(totalFrequency==0) return Double.NaN;
        double freq = doubleResponseMap.get(response);
        return freq/totalFrequency;
    }

    /**
     * Sample standard deviation of a particular item response, where the response is given as a String.
     *
     * @param response an item response
     * @return sample standard deviaiton of the response
     * @throws NumberFormatException thrown if the response cannot be parsed as a double
     */
    public double getStandardDeviationAt(String response)throws NumberFormatException{
        double p = getProportionAt(response);
        double v = (totalFrequency*p*(1-p))/(totalFrequency-1);
        return Math.sqrt(v);
    }

    /**
     * Sample standard deviation of a particular item response, where the response is given as an int.
     *
     * @param response an item response
     * @return sample standard deviation of the response
     */
    public double getStandardDeviationAt(int response){
        double p = getProportionAt(response);
        double v = (totalFrequency*p*(1-p))/(totalFrequency-1);
        return Math.sqrt(v);
    }

    /**
     * Sample standard deviation of a particular item response, where the response is given as a double.
     *
     * @param response an item response
     * @return sample standard deviation of the response
     */
    public double getStandardDeviationAt(double response){
        double p = getProportionAt(response);
        double v = (totalFrequency*p*(1-p))/(totalFrequency-1);
        return Math.sqrt(v);
    }

    /**
     * Population standard deviation of a particular item response, where the response is given as a String.
     *
     * @param response an item response
     * @return sample standard deviaiton of the response
     * @throws NumberFormatException thrown if the response cannot be parsed as a double
     */
    public double getPopulationStandardDeviationAt(String response){
        double p = getProportionAt(response);
        double v = p*(1-p);
        return Math.sqrt(v);
    }

    /**
     * Population standard deviation of a particular item response, where the response is given as an int.
     *
     * @param response an item response
     * @return sample standard deviation of the response
     */
    public double getPopulationStandardDeviationAt(int response){
        double p = getProportionAt(response);
        double v = p*(1-p)/totalFrequency;
        return Math.sqrt(v);
    }

    /**
     * Population standard deviation of a particular item response, where the response is given as a double.
     *
     * @param response an item response
     * @return sample standard deviation of the response
     */
    public double getPopulationStandardDeviationAt(double response){
        double p = getProportionAt(response);
        double v = p*(1-p);
        return Math.sqrt(v);
    }

    public Iterator<Double> doubleIterator(){
        return doubleResponseMap.keySet().iterator();
    }

    public Iterator<String> stringIterator(){
        return null;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        Iterator<Double> iter = doubleResponseMap.keySet().iterator();
        Double d = null;

        sb.append(getOutputString());
        f.format("%n");

        while(iter.hasNext()){
            d = iter.next();
            sb.append(getOutputStringAt(d));
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
        f.format("%10.2f", getMean());f.format("%2s", "");
        f.format("%10.2f", getStandardDeviation());
        return f.toString();
    }

    public String getOutputStringAt(String response){
        double resp = Double.parseDouble(response);
        return getOutputStringAt(resp);
    }

    public String getOutputStringAt(int response){
        double resp = Integer.valueOf(response).doubleValue();
        return getOutputStringAt(resp);
    }

    /**
     * Formatted output for a particlar response. It provides the proportion and sample standard deviation.
     * Defaults to two decimal places.
     *
     * @param response an item response
     * @return formatted output
     */
    public String getOutputStringAt(double response){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%20s", response + "(" + doubleScoreMap.get(response) + ")"); f.format("%2s", "");
        f.format("%10.2f", getProportionAt(response)); f.format("%2s", "");
        f.format("%10.2f", getStandardDeviationAt(response));
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

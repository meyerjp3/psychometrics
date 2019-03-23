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

import java.util.Iterator;

@Deprecated
public interface ItemResponseSummary {


    public void increment(String response);

    public void increment(String response, double freqWeight);

    public void increment(int response);

    public void increment(int response, double freqWeight);

    public void increment(double response);

    public void increment(double response, double freqWeight);

    public void setScoreAt(String response, double score);

    public void setScoreAt(int response, double score);

    public void setScoreAt(double response, double score);

    public double getScoreAt(String response);

    public double getScoreAt(int response);

    public double getScoreAt(double response);


    public double getName();

    public double getMean();

    public double getVariance();

    public double getStandardDeviation();

    public double getPopulationVariance();

    public double getPopulationStandardDeviation();

    public double getTotalFrequency();


    public double getFrequencyAt(String response);

    public double getFrequencyAt(int response);

    public double getFrequencyAt(double response);

    public double getProportionAt(String response);

    public double getProportionAt(int response);

    public double getProportionAt(double response);

    public double getStandardDeviationAt(String response);

    public double getStandardDeviationAt(int response);

    public double getStandardDeviationAt(double response);

    public double getPopulationStandardDeviationAt(String response);

    public double getPopulationStandardDeviationAt(int response);

    public double getPopulationStandardDeviationAt(double response);


    public String getOutputString();

    public String getOutputStringAt(String response);

    public String getTotalFrequencyOutputString();


    public Iterator<String> stringIterator();

    public Iterator<Double> doubleIterator();



}

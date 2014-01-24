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
package com.itemanalysis.psychometrics.histogram;

/**
 *
 * @author J. Patrick Meyer
 */
public interface BinCalculation {

    public enum BinCalculationType{
        STURGES, SCOTT, FREEDMAN_DIACONIS;
    }

    public void increment(double x);

    public int numberOfBins();

    public double binWidth();

    public double min();

    public double max();

    public double sampleSize();

}

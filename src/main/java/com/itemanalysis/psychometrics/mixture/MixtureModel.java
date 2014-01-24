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
package com.itemanalysis.psychometrics.mixture;


public interface MixtureModel {

    public double posteriorProbability(int group, int dataRow);

    public double loglikelihood();

    public double mStep();//should return latest evaluate of teh loglikelihood

    public void multipleRandomStarts();

    public double runEM(); //returns the last evaluate of the loglikelihood

    public void setEmOptions(int emMaxIter, double emConvergenceCriterion, int emStarts);

    public int freeParameters();

    public void fitStatistics();

    public int sampleSize();

    public int numberOfGroups();

    public String printHistory();

}

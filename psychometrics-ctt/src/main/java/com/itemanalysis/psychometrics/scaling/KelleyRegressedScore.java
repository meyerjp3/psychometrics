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

import com.itemanalysis.psychometrics.reliability.ScoreReliability;
import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.statistics.LinearTransformation;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class KelleyRegressedScore {

    private double mean = Double.NaN;

    private ScoreReliability reliability = null;

    private DefaultLinearTransformation linear = null;

    public KelleyRegressedScore(double mean, ScoreReliability reliability){
        this.mean = mean;
        this.reliability = reliability;
    }

    public double value(double score){
        LinearTransformation linearTransformation = new DefaultLinearTransformation();
        return value(score, linearTransformation);
    }

    public double value(double score, LinearTransformation linearTransformation){
        double r = reliability.value();//unbiased = false
        double ks = r*score+(1.0-r)*mean;
        ks = linearTransformation.transform(ks);
        return ks;
    }

}

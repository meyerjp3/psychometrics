/*
 * Copyright 2014 J. Patrick Meyer
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

package com.itemanalysis.psychometrics.irt.estimation;

import java.util.Formatter;

/**
 * A class for storing the maximum change in logits and the log-likelihood at each iteration.
 * This class is just for record keeping and displaying the history.
 */
public class IterationRecord {

    private double delta = 0.0;

    private double loglikelihood = 0.0;

    /**
     * Each instance stores information about the iteration.
     *
     * @param delta maximum change in logits.
     * @param loglikelihood log-likelihood value.
     */
    public IterationRecord(double delta, double loglikelihood){
        this.delta = delta;
        this.loglikelihood = loglikelihood;
    }

    /**
     * Gets the maximum change in logits.
     *
     * @return maximum change in lgits.
     */
    public double getDelta(){
        return delta;
    }

    /**
     * Gets the log-likelihood.
     *
     * @return log-likelihood value.
     */
    public double getLoglikelihood(){
        return loglikelihood;
    }

    /**
     * A string for displaying results.
     *
     * @return value for this iteration.
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%17.15f", delta);f.format("%5s", ""); f.format("%20.12f", loglikelihood);
        return f.toString();
    }

}

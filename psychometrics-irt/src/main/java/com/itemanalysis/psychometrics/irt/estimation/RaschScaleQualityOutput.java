/*
 * Copyright 2013 J. Patrick Meyer
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
 * A class for formatting scale quality output for display in plain text form.
 */
public class RaschScaleQualityOutput {

    private RaschScaleQualityStatistics items = null;

    private RaschScaleQualityStatistics persons = null;

    public RaschScaleQualityOutput(RaschScaleQualityStatistics items, RaschScaleQualityStatistics persons){
        this.items = items;
        this.persons = persons;
    }

    /**
     * Gets a string with the formatted scale quality output.
     *
     * @return scale quality output.
     */
    public String printScaleQuality(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-50s", "SCALE QUALITY STATISTICS");f.format("%n");
        f.format("%50s", "==================================================");f.format("%n");
        f.format("%-20s", "Statistic");f.format("%2s", "");
        f.format("%10s", "Items");f.format("%2s", "");
        f.format("%10s", "Persons");f.format("%2s", "");f.format("%n");
        f.format("%50s", "--------------------------------------------------");f.format("%n");
        f.format("%-20s", "Observed Variance");f.format("%2s", "");
        f.format("%10.4f", items.observedVariance());f.format("%2s", "");
        f.format("%10.4f", persons.observedVariance());f.format("%n");

        f.format("%-20s", "Observed Std. Dev.");f.format("%2s", "");
        f.format("%10.4f", items.observedStandardDeviation());f.format("%2s", "");
        f.format("%10.4f", persons.observedStandardDeviation());f.format("%n");

        f.format("%-20s", "Mean Square Error");f.format("%2s", "");
        f.format("%10.4f", items.meanSquareError());f.format("%2s", "");
        f.format("%10.4f", persons.meanSquareError());f.format("%n");

        f.format("%-20s", "Root MSE");f.format("%2s", "");
        f.format("%10.4f", items.rootMeanSquareError());f.format("%2s", "");
        f.format("%10.4f", persons.rootMeanSquareError());f.format("%n");

        f.format("%-20s", "Adjusted Variance");f.format("%2s", "");
        f.format("%10.4f", items.adjustedVariance());f.format("%2s", "");
        f.format("%10.4f", persons.adjustedVariance());f.format("%n");

        f.format("%-20s", "Adjusted Std. Dev.");f.format("%2s", "");
        f.format("%10.4f", items.adjustedStandardDeviation());f.format("%2s", "");
        f.format("%10.4f", persons.adjustedStandardDeviation());f.format("%n");

        f.format("%-20s", "Separation Index");f.format("%2s", "");
        f.format("%10.4f", items.separationIndex());f.format("%2s", "");
        f.format("%10.4f", persons.separationIndex());f.format("%n");

        f.format("%-20s", "Number of Strata");f.format("%2s", "");
        f.format("%10.4f", items.numberOfStrata());f.format("%2s", "");
        f.format("%10.4f", persons.numberOfStrata());f.format("%n");

        f.format("%-20s", "Reliability");f.format("%2s", "");
        f.format("%10.4f", items.reliability());f.format("%2s", "");
        f.format("%10.4f", persons.reliability());f.format("%n");

        f.format("%50s", "==================================================");f.format("%n");

        return f.toString();
    }


}

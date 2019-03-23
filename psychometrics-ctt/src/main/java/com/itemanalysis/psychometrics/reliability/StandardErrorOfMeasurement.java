/**
 * Copyright 2014 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.reliability;

public class StandardErrorOfMeasurement {

    public StandardErrorOfMeasurement(){

    }

    public double value(double testVariance, double reliability){
        return Math.sqrt(testVariance*(1-reliability));
    }

    public double value(double testVariance, ScoreReliability reliability){
        return Math.sqrt(testVariance*(1-reliability.value()));
    }

    public double value(ScoreReliability reliability){
        double testVariance = reliability.totalVariance();
        return Math.sqrt(testVariance*(1-reliability.value()));
    }


}

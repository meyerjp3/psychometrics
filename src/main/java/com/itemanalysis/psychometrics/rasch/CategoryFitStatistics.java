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
package com.itemanalysis.psychometrics.rasch;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class CategoryFitStatistics {

    double k = 0.0;

    double sumKmEni = 0.0;

    double sumKmEniWni = 0.0;

    double sumPnikKmEni = 0.0;

    double sumPnikKmEniWni = 0.0;

    public CategoryFitStatistics(Integer k){
        this.k = Integer.valueOf(k).doubleValue();
    }

    public void increment(double Xni, double Eni, double Wni, double Pnik){
        double kMe2 = (k-Eni)*(k-Eni);
        if(Xni==k){
            sumKmEni += kMe2;//Ox
            sumKmEniWni += kMe2/Wni;
        }
        sumPnikKmEni += Pnik*kMe2;//Mx
        sumPnikKmEniWni += Pnik*kMe2/Wni;
    }

    public double getUnweightedMeanSquare(){
        return sumKmEniWni/sumPnikKmEniWni;
    }

    public double getWeightedMeanSquare(){
        return sumKmEni/sumPnikKmEni;//Ox/Mx
    }

}

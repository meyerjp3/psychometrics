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

package com.itemanalysis.psychometrics.statistics;

/**
 * This class is used for holding gradientAt values for IRT models.
 *
 * @author J. Patrick Meyer
 * @since November 22, 2008
 *
 */
@Deprecated
public class IRTGradient {

    double derivWRT_A = 0.0, derivWRT_B = 0.0, derivWRT_C = 0.0;

    public IRTGradient(){

    }

    public void setDerivWRT_A(double derivWRT_A){
        this.derivWRT_A = derivWRT_A;
    }

    public void setDerviWRT_B(double derivWRT_B){
        this.derivWRT_B = derivWRT_B;
    }

    public void setDerivWRT_C(double derivWRT_C){
        this.derivWRT_C = derivWRT_C;
    }

    public double getDerivWRT_A(){
        return derivWRT_A;
    }

    public double getDerivWRT_B(){
        return derivWRT_B;
    }

    public double getDerivWRT_C(){
        return derivWRT_C;
    }

}

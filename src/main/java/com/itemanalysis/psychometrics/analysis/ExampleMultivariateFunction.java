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
package com.itemanalysis.psychometrics.analysis;

/**
 * An instantiation of AbstractMultivariateFunction for testing purposes only.
 * It exists to check the accuracy of the numerical gradientAt methods.
 *
 */
public class ExampleMultivariateFunction extends AbstractMultivariateFunction {

    private int equation = 0;

    public ExampleMultivariateFunction(int equation){
        this.equation = equation;
    }

    public double value(double[] x){
        switch(equation){
            case 1: return value1(x);
            case 2: return value2(x);
        }
        return value1(x);
    }

    public double value1(double[] x){
        double v = x[0]*x[0]+x[0]*x[1];
        return v;
    }

    public double value2(double[] x){
        return 100.0*Math.pow(x[1] - x[0]*x[0],2) + Math.pow(1 - x[0],2);
    }


}

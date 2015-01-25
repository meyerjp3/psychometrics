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
package com.itemanalysis.psychometrics.polycor;

import com.itemanalysis.psychometrics.analysis.AbstractMultivariateFunction;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class PolychoricTwoStepVariance extends AbstractMultivariateFunction {

    PolychoricLogLikelihoodTwoStep loglik = null;

    public PolychoricTwoStepVariance(PolychoricLogLikelihoodTwoStep loglik){
        this.loglik = loglik;
    }

    public int domainDimension(){
        return 1;
    }

    public double value(double[] x){
        return loglik.value(x[0]);
    }

    public double[][] variance(double[] x){
        RealMatrix m = this.hessianAt(x);
        LUDecomposition SLUD = new LUDecomposition(m);
        RealMatrix inv = SLUD.getSolver().getInverse();
        return inv.getData();
    }

}

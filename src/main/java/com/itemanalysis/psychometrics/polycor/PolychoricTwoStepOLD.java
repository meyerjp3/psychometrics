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



import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.univariate.BrentOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariatePointValuePair;

import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class PolychoricTwoStepOLD extends AbstractPolychoricCorrelationOLD {
    
    PolychoricLogLikelihoodTwoStep loglik = null;

    public PolychoricTwoStepOLD(){
        super();
    }

    /**
     * Compute the two-step approximation to the polychoric correlation.
     *
     * @param data two way array of frequency counts
     */
    public void compute(double[][] data){
        loglik = new PolychoricLogLikelihoodTwoStep(data);
        BrentOptimizer brent = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair result = brent.optimize(200, loglik, GoalType.MINIMIZE, -1.0, 1.0);
        rhoComputed = true;
        rho = result.getPoint();
    }

    /**
     *
     * @return polychoric correlation
     */
    public double value(){
        return rho;
    }

    public double getCorrelationStandardError(){
        double[] x = {value()};
        return loglik.getStandardError(x);
    }

    /**
     *
     * @return row thresholds
     */
    public double[] getRowThresholds(){
        return loglik.getRowThresholds();
    }

    public double[] getValidRowThresholds(){
        return loglik.getValidRowThresholds();
    }

    /**
     *
     * @return column thresholds
     */
    public double[] getColumnThresholds(){
        return loglik.getColumnThresholds();
    }

    public double[] getValidColumnThresholds(){
        return loglik.getValidColumnThresholds();
    }

    public int getNumberOfValidRowThresholds(){
        return loglik.getNumberOfValidRowThresholds();
    }

    public int getNumberOfValidColumnThresholds(){
        return loglik.getNumberOfValidColumnThresholds();
    }

    /**
     * Returns an array of all parameters. This method is primarily
     * used to provide starting values for the maximum likelihood
     * method of computing the polychoric correlation.
     * 
     * @return
     */
    public double[] getParameterArray(){
        return loglik.getParameterArray(rho);
    }

    public String printVerbose(){
        double[] x = {rho};
        String s =loglik.print(x);
        return s;
    }

    public String printThresholds(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double r = this.value();
        double[] t = this.getRowThresholds();

        for(int i=0;i<t.length-1;i++){
            f.format("% 6.4f", t[i]); f.format("%2s", "");
        }
        return f.toString();

    }

    public String print(){
        return "";
    }

}

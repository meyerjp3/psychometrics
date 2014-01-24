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
package com.itemanalysis.psychometrics.cfa;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.log4j.Logger;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class UnweightedLeastSquares extends AbstractConfirmatoryFactorAnalysisEstimator {

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public UnweightedLeastSquares(ConfirmatoryFactorAnalysisModel model, RealMatrix varcov, double numberOfExaminees){
        super(model, varcov, numberOfExaminees);
    }

//    public double evaluate(double[] argument, double[] gradient)throws MathRuntimeException{
//        F = evaluate(argument);
//        computeGradient(argument, gradient);
//        return F;
//    }

        /**
     * Empty method - artifact of interface
     *
     * @param argument
     * @return
     */
    public double value(double[] argument){
        SIGMA = model.getImpliedCovariance(argument);
        RealMatrix D=varcov.subtract(SIGMA);
        RealMatrix D2=D.multiply(D);
        F=0.5*D2.getTrace();
        return  F;
    }

    public double gfi(){
        double fit = 0.0;
        double q = Double.valueOf(model.getNumberOfItems()).doubleValue();
        RealMatrix D = varcov.subtract(SIGMA);
        RealMatrix D2 = D.multiply(D);
        RealMatrix V2 = varcov.multiply(varcov);
        fit = 1.0-D2.getTrace()/V2.getTrace();
        return fit;
    }

    public double agfi(){
        double gfi = gfi();
        double q = Double.valueOf(model.getNumberOfItems()).doubleValue();
        double p1 = (q*(q+1))/(2.0*degreesOfFreedom());
        double p2 = 1.0-gfi;
        double fit = 1.0-p1*p2;
        return fit;
    }

    public int domainDimension(){
		return model.getNumberOfParameters();
	}

//    public int getNumArguments(){
//		return model.getNumberOfParameters();
//	}

}

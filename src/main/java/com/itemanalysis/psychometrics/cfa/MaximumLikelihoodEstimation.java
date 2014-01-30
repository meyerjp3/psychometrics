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

import com.itemanalysis.psychometrics.statistics.IdentityMatrix;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.log4j.Logger;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public class MaximumLikelihoodEstimation extends AbstractConfirmatoryFactorAnalysisEstimator{

    //compute determinant of varcov
    LUDecomposition CVLUD = null;
    double detVc=0.0;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    int eval = 0;

    public MaximumLikelihoodEstimation(ConfirmatoryFactorAnalysisModel model, RealMatrix varcov, double numberOfExaminees){
        super(model, varcov, numberOfExaminees);
        CVLUD = new LUDecomposition(varcov);
        detVc=CVLUD.getDeterminant();
    }

//    public double evaluate(double[] argument, double[] gradient)throws MathRuntimeException{
//        System.out.println("1EVALS: " + eval++);
//        F = evaluate(argument);
//        computeGradient(argument, gradient);
//        return F;
//    }
    
    public double value(double[] argument){
        model.setParameters(argument);

//            Linesearch method in QNMinimizer is causing NaN values after repeated calls to here
//            Next libe is for monitoring values when called from line search
//            No problem occurs with CGMinimizer
//            System.out.println("valueAt: " + argument[0] + " " + argument[1]);
            SIGMA = model.getImpliedCovariance(argument);

            //compute determinant of SIGMA
            LUDecomposition SLUD = new LUDecomposition(SIGMA);
            double detSig=SLUD.getDeterminant();

            //compute inverse of SIGMA
            RealMatrix SIGMAinv = SLUD.getSolver().getInverse();
            RealMatrix VC_SIGMA_INV = varcov.multiply(SIGMAinv);
            double trace=VC_SIGMA_INV.getTrace();

            //convert number of items to double
            double p = Double.valueOf(model.getNumberOfItems()).doubleValue();

            //compute objective function
            F = Math.log(detSig) + trace - Math.log(detVc) - p;
        return  F;
    }

    public double gfi(){
        double fit = 0.0;
        double q = Double.valueOf(model.getNumberOfItems()).doubleValue();
        RealMatrix I = new IdentityMatrix(nItems);
        LUDecomposition SLUD = new LUDecomposition(SIGMA);
        RealMatrix Sinv = SLUD.getSolver().getInverse();
        RealMatrix P1 = Sinv.multiply(varcov);
        RealMatrix D = P1.subtract(I);
        double numerator = D.multiply(D).getTrace();

        RealMatrix P2 = Sinv.multiply(varcov);
        double denom = P2.multiply(P2).getTrace();

        fit = 1.0-numerator/denom;
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

    public double[] gradient(double[] x){
        model.setParameters(x);
        double[] g = null;
        SIGMA = model.getImpliedCovariance(x);
        LUDecomposition SLUD = new LUDecomposition(SIGMA);
        RealMatrix Sinv = SLUD.getSolver().getInverse();
        RealMatrix D = SIGMA.subtract(varcov);
        RealMatrix derLambda = Sinv.multiply(D).multiply(Sinv).multiply(model.getBeta(x));
        RealMatrix derError = Sinv.multiply(D).multiply(Sinv);
        g = model.getGradient(derLambda, derError);
        return g;
    }

    @Override
    public MultivariateVectorFunction gradient() {
        return new MultivariateVectorFunction() {
            public double[] value(double[] point) {
                return gradient(point);
            }
        };
    }

    @Override
    public MultivariateFunction partialDerivative(final int k) {
        return new MultivariateFunction() {
            public double value(double[] point){
                return gradient(point)[k];
            }
        };
    }

    public int domainDimension(){
		return model.getNumberOfParameters();
	}

//    public int getNumArguments(){
//		return model.getNumberOfParameters();
//	}

}

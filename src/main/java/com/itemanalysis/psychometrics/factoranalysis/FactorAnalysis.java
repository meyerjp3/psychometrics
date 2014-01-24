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
package com.itemanalysis.psychometrics.factoranalysis;

import com.itemanalysis.psychometrics.optimization.BOBYQAOptimizer;
import com.itemanalysis.psychometrics.optimization.CGMinimizer;
import com.itemanalysis.psychometrics.optimization.QNMinimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.*;

import java.util.Arrays;
import java.util.Formatter;

public class FactorAnalysis {

    private PointValuePair uniqueness = null;
    private RealMatrix correlationMatrix = null;
    private int nVariables = 0;
    private int nFactors = 0;
    private int nParameters = 0;
    private MINRESmethod minres = null;

    public FactorAnalysis(RealMatrix correlationMatrix, int nFactors){
        this.correlationMatrix = correlationMatrix;
        this.nFactors = Math.max(nFactors, 1);
        this.nVariables = correlationMatrix.getColumnDimension();
        this.nParameters = nVariables;
    }

    private double[] getInitialValues(){
        double[] init = new double[nParameters];
        for(int i=0;i<nParameters;i++){
            init[i] = 0.5;
        }
        return init;
    }

    private double[] getLowerBounds(){
        double[] lb = new double[nParameters];
        for(int i=0;i<nParameters;i++){
            lb[i] = -1.0;
        }
        return lb;
    }

    private double[] getUpperBounds(){
        double[] ub = new double[nParameters];
        for(int i=0;i<nParameters;i++){
            ub[i] = 1.0;
        }
        return ub;
    }

    public void estimateParameters(){
        minres = new MINRESmethod(correlationMatrix, nFactors);

//        QNMinimizer minimizer = new QNMinimizer(15, true);
//        double[] x = minimizer.minimize(minres, 1e-8, getInitialValues());

//        CGMinimizer minimizer = new CGMinimizer();
//        double[] x = minimizer.minimize(minres, 1e-4, getInitialValues(), 150);

//        for(int i=0;i<x.length;i++){
//            System.out.println("U2: " + x[i]);
//        }

//        System.out.println(minimizer.printRecord());




//FOR R way
        int numIterpolationPoints = nParameters + 2;
        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
        RandomGenerator g = new JDKRandomGenerator();
        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(nParameters, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        uniqueness = optimizer.optimize(new MaxEval(5000),
                new ObjectiveFunction(minres),
                GoalType.MINIMIZE,
//                new SimpleBounds(getLowerBounds(), getUpperBounds()),
                SimpleBounds.unbounded(nParameters),
                new InitialGuess(getInitialValues()));
        minres.computeFactorLoadings(uniqueness.getPoint(), correlationMatrix);





    }

    public String printFactorLoadings(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        int offset = 0;
        int offset2 = nVariables*nFactors;
        for(int i=0;i<nVariables;i++){
            f.format("%10s", "v"+(i+1));
            for(int j=0;j<nFactors;j++){
                f.format("%10.4f", minres.getFactorLoadingAt(i,j)); f.format("%5s", "");
            }
            f.format("%10.4f", uniqueness.getPoint()[i]);
            f.format("%n");
        }

        return f.toString();
    }

//    public String printFactorLoadings(){
//        StringBuilder sb = new StringBuilder();
//        Formatter f = new Formatter(sb);
//
//        int offset = 0;
//        int offset2 = nVariables*nFactors;
//        for(int i=0;i<nVariables;i++){
//            f.format("%10s", "v"+(i+1));
//            for(int j=0;j<nFactors;j++){
//                f.format("%10.4f", uniqueness.getPoint()[offset++]); f.format("%5s", "");
//            }
//            f.format("%10.4f", uniqueness.getPoint()[offset2++]);
//            f.format("%n");
//        }
//
//        return f.toString();
//    }

}

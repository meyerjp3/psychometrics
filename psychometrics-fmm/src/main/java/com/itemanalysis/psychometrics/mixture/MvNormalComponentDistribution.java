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
package com.itemanalysis.psychometrics.mixture;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.random.CorrelatedRandomVectorGenerator;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.NormalizedRandomGenerator;

import java.util.Formatter;

public class MvNormalComponentDistribution implements ComponentDistribution {

    /**
     * k x 1 Matrix of means
     */
    private RealMatrix mu = null;

    /**
     * k x k matrix of covariances
     */
    private RealMatrix sigma = null;

    private double pi = 0.0;

    public MvNormalComponentDistribution(int dimensions){

    }

    /**
     *
     * @param x a matrix of dimension 1 x k, where k is the number of variables
     * @return
     */
    public double density(RealMatrix x)throws SingularMatrixException {
        double prob = 0.0;
        RealMatrix xTran = x.transpose();
        int d = xTran.getRowDimension();
        double det = new LUDecomposition(sigma).getDeterminant();
        double nconst = 1.0/Math.sqrt(det*Math.pow(2.0*Math.PI, d));
        RealMatrix Sinv = new LUDecomposition(sigma).getSolver().getInverse();
        RealMatrix delta = xTran.subtract(mu);
        RealMatrix dist = (delta.transpose().multiply(Sinv).multiply(delta));
        prob = nconst*Math.exp(-0.5*dist.getEntry(0, 0));
        return prob;
    }


    public void generateStartValues(RealMatrix x, RealMatrix mean, RealMatrix cov){
        JDKRandomGenerator jg = new JDKRandomGenerator();
        NormalizedRandomGenerator rg = new GaussianRandomGenerator(jg);
        CorrelatedRandomVectorGenerator sg = null;
        sg = new CorrelatedRandomVectorGenerator(mean.getColumn(0), cov, 0.00001, rg);
        mu = new Array2DRowRealMatrix(sg.nextVector());
    }

    public void setMixingProportion(double pi){
        this.pi = pi;
    }

    public void setMean(RealMatrix mu){
        this.mu = mu;
    }

    public void setCovariance(RealMatrix sigma){
        this.sigma = sigma;
    }

    public double getMixingProportion(){
        return pi;
    }

    public RealMatrix getMean(){
        return mu;
    }

    public RealMatrix getCov(){
        return sigma;
    }

    public String printMixingProportion(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%6.4f", pi);f.format("%n");
        return f.toString();
    }

    public String printMean(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        for(int i=0;i<mu.getRowDimension();i++){
            f.format("% 8.2f", mu.getEntry(i, 0));f.format("%5s", "");
        }
        f.format("%n");
        return f.toString();
    }

    public String printCovariance(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        for(int i=0;i<sigma.getRowDimension();i++){
            for(int j=0;j<sigma.getColumnDimension();j++){
                f.format("% 8.2f", sigma.getEntry(i, j));f.format("%5s", "");
            }
            f.format("%n");
        }
        return f.toString();
    }

}

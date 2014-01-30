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

import com.itemanalysis.psychometrics.analysis.AbstractMultivariateFunction;
import com.itemanalysis.psychometrics.data.VariableInfo;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Formatter;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
@Deprecated
public abstract class AbstractConfirmatoryFactorAnalysisEstimator extends AbstractMultivariateFunction
        implements ConfirmatoryFactorAnalysisEstimator{
    
    protected ConfirmatoryFactorAnalysisModel model = null;

    protected double F=0.0;

    protected double numberOfExaminees = 0.0;

    protected RealMatrix SIGMA = null;

    protected RealMatrix varcov=null;

    protected RealMatrix VCinv = null;

    protected int nItems = 0;

    protected ArrayList<VariableInfo> items = null;

    public AbstractConfirmatoryFactorAnalysisEstimator(ConfirmatoryFactorAnalysisModel model, RealMatrix varcov, double numberOfExaminees){
        this.model = model;
        this.varcov = varcov;
        this.numberOfExaminees = numberOfExaminees;
        this.nItems = model.getNumberOfItems();
        LUDecomposition CVLUD = new LUDecomposition(varcov);
        VCinv=CVLUD.getSolver().getInverse();
    }

    public double fMin(){
        return F;
    }
    
    public double[][] residuals(){
        double[][] resid = new double[nItems][nItems];
        for(int i=0;i<SIGMA.getRowDimension();i++){
            for(int j=0;j<SIGMA.getColumnDimension();j++){
                resid[i][j]=varcov.getEntry(i, j)-SIGMA.getEntry(i, j);
            }
        }
        return resid;
    }

    public double[][] squaredResiduals(){
        double[][] resid = new double[nItems][nItems];
        double temp=0.0;
        for(int i=0;i<SIGMA.getRowDimension();i++){
            for(int j=0;j<SIGMA.getColumnDimension();j++){
                temp=varcov.getEntry(i, j)-SIGMA.getEntry(i, j);
                resid[i][j] = temp*temp;
            }
        }
        return resid;
    }

    public double meanSquaredResidual(){
        double ni = Double.valueOf(nItems).doubleValue();
        double temp=0.0, sum=0.0;
        for(int i=0;i<SIGMA.getRowDimension();i++){
            for(int j=0;j<SIGMA.getColumnDimension();j++){
                temp=varcov.getEntry(i, j)-SIGMA.getEntry(i, j);
                sum+=temp*temp;
            }
        }
        return sum/(ni*ni);
    }

    public double sumMatrix(RealMatrix matrix){
        double sum=0.0;
        for(int i=0;i<matrix.getRowDimension();i++){
            for(int j=0;j<matrix.getColumnDimension();j++){
                sum+=matrix.getEntry(i, j);
            }
        }
        return sum;
    }

    public double sumSquaredElements(RealMatrix matrix){
        double sum=0.0;
        double v=0.0;
        for(int i=0;i<matrix.getRowDimension();i++){
            for(int j=0;j<matrix.getColumnDimension();j++){
                v=matrix.getEntry(i, j);
                sum+=(v*v);
            }
        }
        return sum;
    }

    public double chisquare(){
        return (numberOfExaminees-1)*F;
    }

    public double pvalue(){
        ChiSquaredDistribution chi = new ChiSquaredDistribution(degreesOfFreedom());
        double p = 0.0;
        p = 1.0-chi.cumulativeProbability(chisquare());
        return p;
    }

    public double mcdonaldOmega(){
        double omega = 0.0;
        double sumLambda = 0.0, sumLambda2 = 0.0;
        double sumErrorVar = 0.0;
        double[] fl = model.getFactorLoading();
        double[] er = model.getErrorVariance();
        for(int i=0;i<nItems;i++){
            sumLambda+=fl[i];
            sumErrorVar+=er[i];
        }
        sumLambda2 = Math.pow(sumLambda, 2);
        omega = sumLambda2/(sumLambda2 + sumErrorVar);
        return omega;
    }

    public double aic(){
        double k = (nItems*(nItems+1)/2.0)-degreesOfFreedom();
        double a = chisquare()/numberOfExaminees+2.0*k/(numberOfExaminees-1);
        return a;
    }

    public double bic(){
        double b = chisquare()+model.getNumberOfParameters()*Math.log(numberOfExaminees)*nItems;
        return b;
    }

    public double rmsea(){
        double df = degreesOfFreedom();
        double v = Math.max((chisquare()-df)/(numberOfExaminees-1.0), 0.0);
        double rmsea = Math.sqrt(v/df);
        return rmsea;
    }

    public double degreesOfFreedom(){
        double ni = Double.valueOf(nItems).doubleValue();
        double m = (ni*(ni+1.0))/2.0;
        double df = m-model.getNumberOfParameters();
        return df;
    }

    public String printEstimates(ArrayList<VariableInfo> items){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] fl = model.getFactorLoading();
        double[] er = model.getErrorVariance();
//        f.format("%50s", "       CONFIRMATORY FACTOR ANALYSIS RESULTS       ");
        f.format("%n");
        f.format("%-50s", model.getName());f.format("%n");
        f.format("%50s", "==================================================");f.format("%n");
        f.format("%10s", "     "); f.format("%5s", ""); f.format("%10s", "Factor ");f.format("%5s", ""); f.format("%10s", " Error"); f.format("%n");
        f.format("%10s", " Item"); f.format("%5s", ""); f.format("%10s", "Loading");f.format("%5s", ""); f.format("%10s", "Variance");f.format("%n");
        f.format("%50s", "--------------------------------------------------");f.format("%n");


        for(int i=0;i<items.size();i++){
            f.format("%10s", items.get(i));f.format("%5s", ""); f.format("% 10.4f", fl[i]);f.format("%5s", ""); f.format("% 10.4f", er[i]);f.format("%n");
        }
        f.format("%50s", "--------------------------------------------------");
        f.format("%n");
        return f.toString();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] fl = model.getFactorLoading();
        double[] er = model.getErrorVariance();

        for(int i=0;i<nItems;i++){
            f.format("% .4f", fl[i]); f.format("%5s", ""); f.format("% .4f", er[i]); f.format("%n");
        }

        f.format("%10s", "McDonald's Omega = "); f.format("%8.4f", mcdonaldOmega());f.format("%n");
        f.format("%10s", "GFI = "); f.format("%8.4f", gfi());f.format("%n");
        f.format("%10s", "AGFI = "); f.format("%8.4f", agfi());f.format("%n");
        f.format("%10s", "RMSEA = "); f.format("%8.4f", rmsea());f.format("%n");
        f.format("%10s", "RMSR = "); f.format("%8.4f", Math.sqrt(meanSquaredResidual()));f.format("%n");
        f.format("%10s", "BIC = "); f.format("%8.4f", Math.sqrt(bic()));f.format("%n");
        f.format("%10s", "X^2 = "); f.format("%8.4f", chisquare());f.format("%n");
        f.format("%10s", "df = "); f.format("%8.4f", degreesOfFreedom());f.format("%n");
        f.format("%10s", "p = "); f.format("%8.4f", pvalue());f.format("%n");
        return f.toString();
    }

}

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
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;

import java.util.Formatter;
import java.util.concurrent.Callable;

public class MvNormalMixtureModel implements MixtureModel, Callable<MvNormalMixtureModel> {

    private boolean sameVarianceWithin = true;

    private boolean sameCovarianceWithin = true;

    private boolean localIndependence = true;

    private boolean sameCovarianceBetween = true;

    private boolean converged = false;

    private int sampleSize = 0;

    private int emIter = 0;

    private int emStarts = 100;

    private int emMaxIter = 500;

    private double emConvergenceCriterion = 0.0001;

    private Formatter emHistory = null;

    /**
     * Number of variables
     */
    private int dimensions = 1;

    /*
     * Number of component distributions (i.e. number of latent classes)
     */
    private int groups = 1;

    private double[] piKp1 = null;

    private RealMatrix data = null;

    private ComponentDistribution[] compDistribution = null;

    private InformationFitCriteria fit = null;

    private StringBuilder sb = null;

    private RealMatrix initialMean = null;

    private RealMatrix initialCovariance = null;

    private String statusMessage = "OK";



    public MvNormalMixtureModel(RealMatrix data, int groups){
        this.data = data;
        this.sampleSize = data.getRowDimension();
        this.dimensions = data.getColumnDimension();
        this.groups = groups;
        piKp1 = new double[groups];

        sb = new StringBuilder();
        emHistory = new Formatter(sb);
        emHistory.format("%10s", "ITERATION");emHistory.format("%5s", "");
        emHistory.format("%12s", "LOGLIKE");emHistory.format("%5s", "");
        emHistory.format("%12s", "DELTA");emHistory.format("%5s", "");emHistory.format("%n");
        emHistory.format("%50s", "==================================================");emHistory.format("%n");

        initializeComponentDistributions();
    }

    public void estimateMean(RealMatrix x){
        VectorialMean m = new VectorialMean(dimensions);
        for(int i=0;i<x.getRowDimension();i++){
            m.increment(x.getRow(i));
        }
        initialMean = new Array2DRowRealMatrix(m.getResult());

    }

    public void estimateCov(RealMatrix x){
        Covariance cv = new Covariance(x);
        initialCovariance = cv.getCovarianceMatrix();
    }

    private void initializeComponentDistributions(){
        compDistribution = new ComponentDistribution[groups];
        double sum = 0.0;

        for(int g=0;g<groups;g++){
            compDistribution[g] = new MvNormalComponentDistribution(dimensions);
            this.estimateMean(data);
            this.estimateCov(data);

            compDistribution[g].setMean(initialMean);
            compDistribution[g].setCovariance(initialCovariance);

            if(g<(groups-1)){
                compDistribution[g].setMixingProportion(1.0/groups);
                sum += 1.0/groups;
            }
        }
        compDistribution[(groups-1)].setMixingProportion(1.0-sum);//ensure mixing proportion sums to one
    }

    public void setModelConstraints(boolean sameVarianceWithin, boolean sameCovarianceWithin, boolean localIndependence, boolean sameCovarianceBetween){
        this.sameVarianceWithin = sameVarianceWithin;
        this.sameCovarianceWithin = sameCovarianceWithin;
        this.localIndependence = localIndependence;
        this.sameCovarianceBetween = sameCovarianceBetween;
    }

    public void setEmOptions(int emMaxIter, double emConvergenceCriterion, int emStarts){
        this.emMaxIter = emMaxIter;
        this.emConvergenceCriterion = emConvergenceCriterion;
        this.emStarts = emStarts;
    }

    public double posteriorProbability(int group, int dataRow){
        int index = 0;
        double value = 0.0;
        double sum = 0.0;
        double pi = 0.0;
        RealMatrix row = data.getRowMatrix(dataRow);
        try{
            for(ComponentDistribution compDist : compDistribution){
                pi = compDist.getMixingProportion();
                sum += pi*compDist.density(row);
                if(index==group) value = pi*compDist.density(row);
                index++;
            }
            if(sum==0.0) return Double.NaN;
        }catch(SingularMatrixException ex){
            statusMessage = "Singular Matrix";
        }

        return value/sum;
    }

    public double loglikelihood(){
        double ll = 0.0;
        double sum = 0.0;
        try{
            for(int i=0;i<sampleSize;i++){
                sum = 0.0;
                for(int g=0;g<groups;g++){
                    sum += compDistribution[g].getMixingProportion()*compDistribution[g].density(data.getRowMatrix(i));
                }
                ll += Math.log(sum);
            }
        }catch(SingularMatrixException ex){
            statusMessage = "Singular Matrix";
        }

        return ll;
    }

    public void printMatrix(RealMatrix matrix){
        for(int i=0;i<matrix.getRowDimension();i++){
            for(int j=0;j<matrix.getColumnDimension();j++){
                System.out.println("  " + matrix.getEntry(i, j));
            }
            System.out.println();
        }
        System.out.println();
    }

    public double mStep(){
        RealMatrix[] M = new Array2DRowRealMatrix[groups];
        RealMatrix[] S = new Array2DRowRealMatrix[groups];
        double[] T1 = new double[groups];
        RealMatrix[] T2 = new Array2DRowRealMatrix[groups];
        RealMatrix[] T3 = new Array2DRowRealMatrix[groups];
        double pp = 0.0;
        RealMatrix rowData = null;
        RealMatrix temp = null;

        try{
            //estimate new means, covariances, and mixing proportions
            for(int g=0;g<groups;g++){
                T2[g] = new Array2DRowRealMatrix(dimensions, 1);
                T3[g] = new Array2DRowRealMatrix(dimensions, dimensions);
                for(int i=0;i<sampleSize;i++){
                    pp = posteriorProbability(g, i);
                    rowData = data.getRowMatrix(i).transpose();
                    T1[g] += pp;
                    T2[g] = T2[g].add(rowData.scalarMultiply(pp));
                    temp = rowData.scalarMultiply(pp).multiply(rowData.transpose());
                    T3[g] = T3[g].add(temp);
                }

            }

            for(int g=0;g<groups;g++){
                M[g] = T2[g].scalarMultiply(1.0/T1[g]);
                temp = T2[g].scalarMultiply(1.0/T1[g]).multiply(T2[g].transpose());
                S[g] = T3[g].subtract(temp).scalarMultiply(1.0/T1[g]);
            }
        }catch(SingularMatrixException ex){
            statusMessage = "Singular Matrix";
        }



        //apply constraints
        if(sameVarianceWithin) setCommonVarianceWithinClass(S);
        if(sameCovarianceWithin && !localIndependence) setCommonCovarianceWithinClass(S);
        if(localIndependence) setLocalIndependence(S);
        if(sameCovarianceBetween) setCommonCovariance(S);

        piKp1 = computeMixingProportions();

        //set values of new estimates for eStep (i.e. posterior probability) and computation of loglikelihood
        MvNormalComponentDistribution dist = null;
        for(int g=0;g<groups;g++){
            dist = (MvNormalComponentDistribution)compDistribution[g];
            dist.setMixingProportion(piKp1[g]);
            dist.setMean(M[g]);
            dist.setCovariance(S[g]);
        }

        double ll = loglikelihood();
        return ll;
    }

    private double[] computeMixingProportions(){
        double mixSum = 0.0;
        double sum = 0.0;
        double  pp = 0.0;
        double[] pi = new double[groups];
        for(int g=0;g<(groups-1);g++){
            sum = 0.0;
            for(int i=0;i<sampleSize;i++){
                pp = posteriorProbability(g, i);
                sum += pp;
            }
            pi[g] = sum/sampleSize;
            mixSum += pi[g];
        }
        pi[(groups-1)]=1.0-mixSum;
        return pi;
    }

    private void setCommonCovariance(RealMatrix[] cov){
        RealMatrix tempS = new Array2DRowRealMatrix(dimensions, dimensions);
        for(int g=0;g<groups;g++){
            tempS = tempS.add(cov[g].scalarMultiply(compDistribution[g].getMixingProportion()));
        }
        for(int g=0;g<groups;g++){
            cov[g] = tempS;
        }
    }

    private void setLocalIndependence(RealMatrix[] cov){
        for(int g=0;g<groups;g++){
            for(int i=0;i<dimensions;i++){
                for(int j=0;j<dimensions;j++){
                    if(j!=i) cov[g].setEntry(i, j, 0.0);
                }
            }
        }
    }

    private void setCommonVarianceWithinClass(RealMatrix[] cov){
        double var = 0.0;
        for(int g=0;g<groups;g++){
            var = cov[g].getEntry(0, 0);
            for(int i=0;i<dimensions;i++){
                cov[g].setEntry(i, i, var);
            }
        }
    }

    private void setCommonCovarianceWithinClass(RealMatrix[] cov){
        if(dimensions==1) return;
        double value = 0.0;
        for(int g=0;g<groups;g++){
            value = cov[g].getEntry(0, 1);
            for(int i=0;i<dimensions;i++){
                for(int j=0;j<dimensions;j++){
                    if(j!=i) cov[g].setEntry(i, j, value);
                }
            }
        }
    }

    public int freeParameters(){
        int free = 0; //count number of means
        double d = (double)dimensions;
        int k = (int)(d*(d+1.0)/2.0);
        if(sameVarianceWithin){
            free+=1;
        }else{
            free+=dimensions;
        }
        if(sameCovarianceWithin && !localIndependence){
            free+=1;
        }else if(!sameCovarianceWithin && !localIndependence){
            free+=(k-dimensions);
        }else{//local independence
            free+=0;
        }
        if(sameCovarianceBetween){
            free*=1.0;
        }else{
            free*=groups;
        }

        free += dimensions*groups;//count means
        free += (groups-1);//count mixing proportions
        return free;
    }

    public int sampleSize(){
        return sampleSize;
    }

    public int numberOfGroups(){
        return groups;
    }

    public int itertaions(){
        return emIter;
    }

    public void fitStatistics(){
        fit = new InformationFitCriteria(this);
    }

    public void multipleRandomStarts(){
        RealMatrix[] Mbest = new RealMatrix[groups];
        RealMatrix[] Sbest = new RealMatrix[groups];
        double[] piBest = new double[groups];
        RealMatrix[] M = new RealMatrix[groups];
        RealMatrix[] S = new RealMatrix[groups];
        double[] pi = new double[groups];
        MvNormalComponentDistribution mvnDist = null;
        double llOld = Double.MIN_VALUE;

        try{
            for(int i=0;i<emStarts;i++){
                for(int g=0;g<groups;g++){
                    mvnDist = (MvNormalComponentDistribution)compDistribution[g];
                    mvnDist.generateStartValues(data, initialMean, initialCovariance);
                    M[g] = mvnDist.getMean();
                    S[g] = (RealMatrix)mvnDist.getCov();
                }
                pi = computeMixingProportions();
                for(int g=0;g<groups;g++){
                    compDistribution[g].setMixingProportion(pi[g]);
                }

                double ll = loglikelihood();
                if(ll>llOld){
                    Mbest = M;
                    Sbest = S;
                    piBest = pi;
                }
                llOld = ll;
            }
        }catch(SingularMatrixException ex){
            statusMessage = "Singular Matrix Exception occurred in random start";
        }


        for(int g=0;g<groups;g++){
            mvnDist = (MvNormalComponentDistribution)compDistribution[g];
            mvnDist.setMean(Mbest[g]);
            mvnDist.setCovariance(Sbest[g]);
            mvnDist.setMixingProportion(piBest[g]);
        }
    }

    private void addToHistory(int iter, double logLikelihood, double delta){
        emHistory.format("%10d", iter); emHistory.format("%5s", "");
        emHistory.format("%12.6f", logLikelihood); emHistory.format("%5s", "");
        emHistory.format("%12.8f", delta); emHistory.format("%5s", "");
        emHistory.format("%n");
    }

    public String printHistory(){
        return emHistory.toString();
    }

    public double[] getMean(int group){
        MvNormalComponentDistribution mvnDist = (MvNormalComponentDistribution)compDistribution[group];
        return mvnDist.getMean().getColumn(0);
    }

    public double[][] getCov(int group){
        MvNormalComponentDistribution mvnDist = (MvNormalComponentDistribution)compDistribution[group];
        return mvnDist.getCov().getData();
    }

    public double getMixingProportion(int  group){
        MvNormalComponentDistribution mvnDist = (MvNormalComponentDistribution)compDistribution[group];
        return mvnDist.getMixingProportion();
    }

    public boolean converged(){
        return converged;
    }

    public MvNormalMixtureModel call(){
        multipleRandomStarts();
        runEM();
        return this;
    }

    public double runEM(){
        double ll = 0.0;
        double llOld = loglikelihood();
        double delta = 1.0;

        while(delta>emConvergenceCriterion && emIter < emMaxIter){
            ll = mStep();
            delta = Math.abs(llOld-ll);
            llOld = ll;
            addToHistory(emIter+1, ll, delta);
            emIter++;
        }
        fitStatistics();
        converged = (delta<=emConvergenceCriterion);
        return ll;
    }

    public double getFitStat(String fitStat){
        return fit.getFitStat(fitStat);
    }

    public String printFit(){
        return fit.printFitStatistics();
    }

    public String printDelimitedFit(){
        String conv = "N";
        if(converged) conv = "Y";
        return conv + "," + statusMessage + "," + fit.printDelimitedFitStatistics();
    }

    public String printResults(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%20s", "Number of groups = "); f.format("%-4d", groups); f.format("%n");
        f.format("%20s", "Free parameters = "); f.format("%-4d", freeParameters()); f.format("%n");
        f.format("%20s", "Sample size = "); f.format("%-10d", sampleSize); f.format("%n");
        f.format("%20s", "Log-likelihood = "); f.format("%-12.4f", this.loglikelihood());f.format("%n");
        f.format("%20s", "Converged = "); f.format("%-5s", converged);f.format("%n");
        f.format("%20s", "Status = "); f.format("%-35s", statusMessage);f.format("%n");
        f.format("%n");
        f.format(fit.printFitStatistics());f.format("%n");

        MvNormalComponentDistribution mvnDist = null;
        for(int g=0;g<groups;g++){
            f.format("%n");
            mvnDist = (MvNormalComponentDistribution)compDistribution[g];
            f.format("%-12s", "Group " + (g+1) +" "); f.format("%n");
            f.format("%12s", "Mix Prop: "); f.format(mvnDist.printMixingProportion());
            f.format("%12s", "Mean: "); f.format(mvnDist.printMean());
            f.format("%12s", "Covar: "); f.format("%n");
            f.format(mvnDist.printCovariance());
        }
        return f.toString();
    }

}

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
package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.optimization.DiffFunction;
import com.itemanalysis.psychometrics.uncmin.Uncmin_methods;

/**
 * This class contains contains methods for the loglikelihood function for an item, which is the
 * value of the objective function that is being minimized in the Mstep to obtain the maximum
 * likelihood or Bayes modal estimates. See {@link com.itemanalysis.psychometrics.irt.estimation.MstepParallel}.
 * This class is also used in computation of the starting values. See {@link com.itemanalysis.psychometrics.irt.estimation.StartingValues}.
 */
public class ItemDichotomous implements ItemLogLikelihoodFunction {

    private Irm3PL model = null;
    private double[] rjk = null;
    private double[] nk = null;
    private DistributionApproximation latentDistribution = null;
    private int nPar = 1;
    private int nPoints = 0;
    private static double LOG_ZERO = Math.log(1e-8);

    public ItemDichotomous(){

    }

    /**
     * Default constructor takes an item response model, latent distribution, and estimates from the Estep.
     *
     * @param model item response model for which new parameter estimates are computed
     * @param latentDistribution quadrature points and weight for the latent distribution
     * @param rjk expected number of correct responses at each quadrature point
     * @param nk expected number of responses at each quadrature node
     */
    public void setModel(ItemResponseModel model, DistributionApproximation latentDistribution, double[] rjk, double[] nk){
        this.model = (Irm3PL)model;
        this.rjk = rjk;
        this.nk = nk;
        this.latentDistribution = latentDistribution;
        this.nPar = model.getNumberOfParameters();
        this.nPoints = latentDistribution.getNumberOfPoints();
    }

    /**
     * Log-likelihood for an item.
     *
     * @return log-likelihood
     */
    public double logLikelihood(){
        double ll = 0.0;
        double p = 0.0;
        double q = 0.0;

        for(int k=0;k<nPoints;k++){
            p = model.probability(latentDistribution.getPointAt(k), 1);
            p = Math.min(0.99999999, Math.max(0.00000001, p)); //always use value strictly between 0 and 1
            q = 1-p;
            ll += rjk[k]*Math.log(p)+(nk[k]-rjk[k])*Math.log(q);

            //the next loop will be preferred when generalizing to poytomous items
//            for(int m=0;m<model.getNcat();m++){
//                p = model.probability(latentDistribution.getPointAt(k), m);//assumes item uses the default score weights that start at zero.
//                ll += rjk[k]*Math.log(p);
//            }

        }

        //add item priors
        double priorProb = 0.0;
        ItemParamPrior prior = null;

        //difficulty prior
        prior = model.getDifficultyPrior();
        if(prior!=null){
            priorProb = prior.logDensity(model.getDifficulty());
            ll += priorProb;
        }

        //discrimination prior
        if(nPar>=2){
            prior = model.getDiscriminationPrior();
            if(prior!=null){
                priorProb = prior.logDensity(model.getDiscrimination());
                ll += priorProb;
            }
        }

        //guessing prior
        if(nPar==3){
            prior = model.getGuessingPrior();
            if(prior!=null){
                priorProb = prior.logDensity(model.getGuessing());
                ll += priorProb;
            }
        }

        return -ll;//negative because minimizing
    }

    /**
     * Method required for DiffFunction interface to QNMinimizer
     *
     * @return
     */
    public int domainDimension(){
        return nPar;
    }


    /**
     * Method required for DiffFunction interface to QNMinimizer
     *
     * @param point parameter values
     * @return value of objective function
     */
    public double valueAt(double[] point){
        double LL = 0;
        double oldAparam = model.getDiscrimination();
        double oldBparam = model.getDifficulty();
        double oldCparam = model.getGuessing();

        //the order of the parameters must match the order in the gradient function in the ItemResponseModel class.
        if(nPar==3){
            model.setDiscrimination(point[0]);
            model.setDifficulty(point[1]);
            model.setGuessing(point[2]);
        }else if(nPar==2){
            model.setDiscrimination(point[0]);
            model.setDifficulty(point[1]);
        }else{
            model.setDifficulty(point[0]);
        }

        LL = logLikelihood();

        //set parameters back to old values
        model.setDiscrimination(oldAparam);
        model.setDifficulty(oldBparam);
        model.setGuessing(oldCparam);

        return LL;
    }

    /**
     * Method required for DiffFunction interface to QNMinimizer
     *
     * See ItemDichotomous.h in ETIRM
     *
     *
     * @param point parameter values. The order of the parameters must match the order in the gradient function in the
     *              ItemResponseModel class.
     * @return gradient values
     */
    public double[] derivativeAt(double[] point){
        //the order of the parameters must match the order in the gradient function in the ItemResponseModel class.
        double oldAparam = model.getDiscrimination();
        double oldBparam = model.getDifficulty();
        double oldCparam = model.getGuessing();

        if(nPar==3){
            model.setDiscrimination(point[0]);
            model.setDifficulty(point[1]);
            model.setGuessing(point[2]);
        }else if(nPar==2){
            model.setDiscrimination(point[0]);
            model.setDifficulty(point[1]);
        }else{
            model.setDifficulty(point[0]);
        }

        double[] loglikegrad = new double[nPar];
        double[] igrad = null;
        double quad = 0.0;
        double p = 0.0;
        double t = 0.0;
        for(int k=0;k<nPoints;k++){
            quad = latentDistribution.getPointAt(k);
            p = model.probability(quad, 1);
            p = Math.min(0.99999999, Math.max(0.00000001, p)); //always use value strictly between 0 and 1
            igrad = model.gradient(quad);
            t = rjk[k]-nk[k]*p;
            t /= (1.0-p)*p;

            loglikegrad[0] += -t*igrad[0];// use -t since function is to be minimized, not maximized

            if(nPar>=2){
                loglikegrad[1] += -t*igrad[1];// use -t since function is to be minimized, not maximized
            }
            if(nPar==3){
                loglikegrad[2] += -t*igrad[2];// use -t since function is to be minimized, not maximized
            }
        }

        //add priors
        ItemParamPrior prior = null;

        if(nPar==3){
            prior = model.getDiscriminationPrior();
            if(prior!=null) {
                loglikegrad[0] -= prior.logDensityDeriv1(model.getDiscrimination());
            }

            prior = model.getDifficultyPrior();
            if(prior!=null) {
                loglikegrad[1] -= prior.logDensityDeriv1(model.getDifficulty());
            }

            prior = model.getGuessingPrior();
            if(prior!=null) {
                loglikegrad[2] -= prior.logDensityDeriv1(model.getGuessing());
            }
        }else if(nPar==2){
            prior = model.getDiscriminationPrior();
            if(prior!=null) {
                loglikegrad[0] -= prior.logDensityDeriv1(model.getDiscrimination());
            }

            prior = model.getDifficultyPrior();
            if(prior!=null) {
                loglikegrad[1] -= prior.logDensityDeriv1(model.getDifficulty());
            }
        }else{
            prior = model.getDifficultyPrior();
            if(prior!=null) {
                loglikegrad[0] -= prior.logDensityDeriv1(model.getDifficulty());
            }
        }

        //set parameters back to old values
        model.setDiscrimination(oldAparam);
        model.setDifficulty(oldBparam);
        model.setGuessing(oldCparam);

        return loglikegrad;
    }

    /**
     * Method required by Uncmin_methods interface
     *
     * @param x parameter values
     * @return
     */
    public double f_to_minimize(double x[]){
        double[] xx = new double[nPar];
        for(int i=0;i<nPar;i++){
            xx[i] = x[i+1];
        }
        return valueAt(xx);
    }

    /**
     * Method required by Uncmin_methods interface
     *
     * @param x parameter values
     * @param g gradient values
     */
    public void gradient(double[] x, double[] g){
        double[] xx = new double[nPar];
        for(int i=0;i<nPar;i++){
            xx[i] = x[i+1];
        }

        double[] grad = derivativeAt(xx);//compute gradient
        for(int i=0;i<nPar;i++){
            g[i+1] = grad[i];
        }

    }

    /**
     * Method required by Uncmin_methods interface
     *
     * @param x parameter values
     * @param a hessian matrix
     */
    public void hessian(double[] x, double[][] a){
        //empty - will be computed numerically by Uncmin routine
    }

}

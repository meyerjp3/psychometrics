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

import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.optimization.DiffFunction;
import com.itemanalysis.psychometrics.uncmin.Uncmin_methods;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.util.Precision;

import java.util.Arrays;

/**
 * This class contains contains methods for the loglikelihood function for an item, which is the
 * value of the objective function that is being minimized in the Mstep to obtain the maximum
 * likelihood or Bayes modal estimates. See {@link MstepParallel}.
 * This class is also used in computation of the starting values. See {@link StartingValues}.
 * It applies to binary and polytomous item response models.
 */
public class ItemLogLikelihood implements DiffFunction, Uncmin_methods{

    private ItemResponseModel model = null;
    private EstepItemEstimates r = null;
    private double[] nk = null;
    private QuadratureRule latentDistribution = null;
    private int nPar = 1;
    private int nPoints = 0;
//    private static double LOG_ZERO = Math.log(1e-8);
    private final double EPSILON = Precision.EPSILON;

    public ItemLogLikelihood(){

    }

    public void setModel(ItemResponseModel model, QuadratureRule latentDistribution, EstepItemEstimates r, double[] nk){
        this.model = model;
        this.r = r;
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
    public double logLikelihood(double[] iparam){
        double ll = 0.0;
        double p = 0.0;

        for(int t=0;t<nPoints;t++){
            for(int k=0;k<model.getNcat();k++){
                p = model.probability(latentDistribution.getPointAt(t), iparam, k, model.getScalingConstant());//assumes item uses the default score weights that start at zero.
                p = Math.min(1.0-EPSILON, Math.max(EPSILON, p)); //always use value strictly between 0 and 1
                ll += r.getRjktAt(k,t)*Math.log(p);
            }
        }

        //add item priors
        ll = model.addPriorsToLogLikelihood(ll, iparam);
        //Return negative because minimizing
        return -ll;
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
        //The order of the parameters must match the order in the gradientAt function in the ItemResponseModel class.
        double LL = logLikelihood(point);
        return LL;
    }

    /**
     * Method required for DiffFunction interface to QNMinimizer
     *
     * See ItemDichotomous.h in ETIRM
     *
     *
     * @param point parameter values. The order of the parameters must match the order in the gradientAt function in the
     *              ItemResponseModel class.
     * @return gradientAt values
     */
    public double[] derivativeAt(double[] point){
        //The order of the parameters must match the order in the gradientAt function in the ItemResponseModel class.

        //Compute gradientAt of item log-likelihood
        double[] loglikegrad = new double[nPar];//There should be one element for each parameter (including first step that is fixed to 0?)
        double[] igrad = null;
        double quad = 0.0;
        double p = 0.0;
        double x = 0.0;
        for(int t=0;t<nPoints;t++){
            quad = latentDistribution.getPointAt(t);

            for(int i=0;i<nPar;i++){
                for(int k=0;k<model.getNcat();k++){
                    igrad = model.gradient(quad, point, k, model.getScalingConstant());
                    p = model.probability(quad, point, k, model.getScalingConstant());
                    p = Math.min(1.0-EPSILON, Math.max(EPSILON, p)); //Always use value strictly between 0 and 1.
                    x = r.getRjktAt(k,t)/p * igrad[i];
                    loglikegrad[i] += -x;
                }
            }
        }

        //add priors
        loglikegrad = model.addPriorsToLogLikelihoodGradient(loglikegrad, point);

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
     * @param g gradientAt values
     */
    public void gradient(double[] x, double[] g){
        double[] xx = new double[nPar];
        for(int i=0;i<nPar;i++){
            xx[i] = x[i+1];
        }

        double[] grad = derivativeAt(xx);//compute gradientAt
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

    /**
     * This numeric calculation of the Hessian is used to compute standard errors for the
     * item parameter estimates. It uses the analytic derivatives in the calculation.
     *
     * @param x item parameter array
     * @return
     */
    private double[][] numericHessian(double[] x){
//        double EPSILON = 1e-8;
        int n = x.length;
        double[][] hessian = new double[n][n];
        double[] gradientAtXpls = null;
        double[] gradientAtX = derivativeAt(x);//analytic derivative
        double xtemp = 0.0;
        double stepSize = 0.0001;

        for(int j=0;j<n;j++){
            stepSize = Math.sqrt(EPSILON)*(Math.abs(x[j])+1.0);//from SAS manual on nlp procedure
            xtemp = x[j];
            x[j] = xtemp + stepSize;
            double [] x_copy = Arrays.copyOfRange(x, 0, x.length);
            gradientAtXpls = derivativeAt(x_copy);//analytic derivative
            x[j] = xtemp;
            for(int i=0;i<n;i++){
                hessian[i][j] = (gradientAtXpls[i]-gradientAtX[i])/stepSize;
            }
        }
        return hessian;
    }

    /**
     * Computes the standard errors of the item parameter estimates.
     *
     * @param x array of item parameter estimates
     * @return array of item parameter standard errors
     */
    public double[] stdError(double[] x, double[][] hessian){
        double[] se = new double[x.length];
        try{
//            RealMatrix m = new Array2DRowRealMatrix(numericHessian(x));
            RealMatrix m = new Array2DRowRealMatrix(hessian);
            RealMatrix info = new LUDecomposition(m).getSolver().getInverse();


            for(int i=0;i<info.getRowDimension();i++){
                se[i] = Math.sqrt(info.getEntry(i,i));
            }
        }catch(SingularMatrixException ex){
            for(int i=0;i<se.length;i++){
                se[i] = Double.NaN;
            }
            ex.printStackTrace();
        }

        return  se;
    }

    public void stdError(ItemResponseModel irm){
        double[] x = irm.getItemParameterArray();
        irm.setStandardErrors(stdError(x, numericHessian(x)));
    }

}

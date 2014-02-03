/*
 * Copyright 2013 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.distribution.NormalDistributionApproximation;
import com.itemanalysis.psychometrics.distribution.UserSuppliedDistributionApproximation;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.*;

import java.util.LinkedHashMap;

/**
 * This class holds an item response vector for an examinee and stores a count of the
 * number of examinees with the same response vector. It computes the loglikelihood
 * and the first derivative of the loglikelihood. These methods can be used for maximum
 * likelihood (ML), Bayes modal (MAP), expected a posteriori (EAP), and proportinal curve
 * fitting (PCF) estimation of person ability in univariate item response models.
 * Note: The PCF method should only be used for members of the Rasch family of item
 * response models.
 *
 * The order of item in the array irm MUST be the same as the order or item
 * responses in the array responses.
 *
 * Missing responses are coded as -1.
 *
 */
public class IrtExaminee extends ItemResponseVector implements UnivariateDifferentiableFunction {

    private ItemResponseModel[] irm = null;
    private NormalDistribution mapPrior = null;
    private DistributionApproximation distributionApproximation = null;
    private EstimationMethod method = EstimationMethod.ML;
    private double estimatedTheta = 0.0;//TODO initialize to NaN??
    private double MinPRS = 0.0;//minimum possible raw (i.e. sum) score
    private double MaxPRS = 0.0;//maximum possible raw (i.e. sum) score

    public IrtExaminee(String groupID, ItemResponseModel[] irm)throws DimensionMismatchException{
        super(groupID, irm.length);
        this.irm = irm;
        if(irm.length!=response.length) throw new DimensionMismatchException(irm.length, response.length);
        initializeScores();
    }

    public IrtExaminee(ItemResponseModel[] irm)throws DimensionMismatchException{
        super("", irm.length);
        this.irm = irm;
        if(irm.length!=response.length) throw new DimensionMismatchException(irm.length, response.length);
        initializeScores();
    }

    public void setItemResponseModels(){
        this.irm = irm;
        initializeScores();
    }

    public void setStartValue(double theta){
        this.estimatedTheta = theta;
    }

    private void initializeScores(){
        for(int i=0;i<response.length;i++){
            if(response[i]!=-1){
                MinPRS += irm[i].getMinScoreWeight();
                MaxPRS += irm[i].getMaxScoreWeight();
            }
        }
    }

    public boolean missingResponseAt(int index){
        return response[index]==-1;
    }

    //adjust sum score to be nonextreme (i.e. all items correct or all wrong)
        //convergence criterion set to 0.01 for extreme persons. This makes it
        //consisten with the updateExtremem method in JMLE.java
    public double getAdjustedSumScore(double adjustment){
        double adjustedSumScore = sumScore;
        if(sumScore==MinPRS){
            adjustedSumScore = MinPRS + adjustment;
        }else if(sumScore==MaxPRS){
            adjustedSumScore = MaxPRS - adjustment;
        }else{
            adjustedSumScore = sumScore;
        }
        return adjustedSumScore;
    }

    public boolean isExtreme(){
        if(sumScore==MinPRS || sumScore==MaxPRS) return true;
        return false;
    }

    /**
     * computes the loglikelihood of a response vector at a given value of theta.
     *
     * @param theta examinee ability
     * @return
     */
    public double logLikelihood(double theta){
        if(validResponses <= 0) return Double.NaN;
        double ll = 0.0;
        double prob = 0.0;
        byte resp = 0;
        VariableName varName = null;
        for(int i=0;i<response.length;i++){
            resp = response[i];
            if(resp!=-1){
                prob = irm[i].probability(theta, resp);
                prob = Math.min(Math.max(0.00001, prob), 0.99999);
                ll += Math.log(prob);
            }
        }

        if(method == EstimationMethod.MAP){
            ll += Math.log(mapPrior.density(theta));
        }

        return ll;
    }

    /**
     * First derivative of loglikelihood with respect to theta.
     *
     * @param theta examinee ability
     * @return first derivative
     */
    public double derivLogLikelihood(double theta){
        double deriv = 0.0;

        for(ItemResponseModel i : irm){
            deriv += i.derivTheta(theta);
        }
        return deriv;

    }

    /**
     * Maximum likelihood estimate (MLE) of examinee ability.
     *
     * @param thetaMin smallest possible ability estimate (lower bound on BrentOptimizer)
     * @param thetaMax largest possible ability estimate (upper bound on BrentOptimizer)
     * @return MLE of examinee ability
     */
    public double maximumLikelihoodEstimate(double thetaMin, double thetaMax){
        method = EstimationMethod.ML;
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair pair = optimizer.optimize(new MaxEval(100),
                new UnivariateObjectiveFunction(this),
                GoalType.MAXIMIZE,
                new SearchInterval(thetaMin, thetaMax));
        estimatedTheta = pair.getPoint();
        return estimatedTheta;
    }

    /**
     * Maximum a Posteriori (MAP) estimate of examinee ability using a normal prior
     * distribution.
     *
     * @param mean mean of normal prior distribution
     * @param sd standard deviation of prior distribution
     * @param thetaMin smallest possible ability estimate (lower bound on BrentOptimizer)
     * @param thetaMax largest possible ability estimate (upper bound on BrentOptimizer)
     * @return MAP estimate of examinee ability
     */
    public double mapEstimate(double mean, double sd, double thetaMin, double thetaMax){
        mapPrior = new NormalDistribution(mean, sd);
        method = EstimationMethod.MAP;
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        UnivariatePointValuePair pair = optimizer.optimize(new MaxEval(100),
                new UnivariateObjectiveFunction(this),
                GoalType.MAXIMIZE,
                new SearchInterval(thetaMin, thetaMax));
        estimatedTheta = pair.getPoint();
        return estimatedTheta;
    }

    /**
     * Expected a Posteriori (EAP) estimate of examinee ability using a normal distribution.
     *
     * @param mean mean of normal distribution
     * @param sd standard deviation of normal distribution
     * @param thetaMin smallest possible ability score
     * @param thetaMax largest possible ability score
     * @param numPoints number of quadrature points
     * @return eap ability estimate
     */
    public double eapEstimate(double mean, double sd, double thetaMin, double thetaMax, int numPoints){
        method = EstimationMethod.EAP;
        distributionApproximation = new NormalDistributionApproximation(mean, sd, thetaMin, thetaMax, numPoints);

        double point = 0.0;
        double w = 0.0;
        double numer = 0.0;
        double denom = 0.0;

        for(int i=0;i<numPoints;i++){
            point = distributionApproximation.getPointAt(i);
            w = Math.exp(logLikelihood(point))*distributionApproximation.getDensityAt(i);
            numer += point*w;
            denom += w;
        }
        estimatedTheta = numer/denom;
        return estimatedTheta;
    }

    /**
     * EAP estimate using a distribution provided by the user such as quadrature points
     * and weights from item calibration.
     *
     * @param dist User specified quadrature points and weights.
     * @return
     */
    public double eapEstimate(UserSuppliedDistributionApproximation dist){
        method = EstimationMethod.EAP;
        distributionApproximation = dist;
        int numPoints = dist.getNumberOfPoints();
        double point = 0.0;
        double w = 0.0;
        double numer = 0.0;
        double denom = 0.0;

        for(int i=0;i<numPoints;i++){
            point = dist.getPointAt(i);
            w = Math.exp(logLikelihood(point))*dist.getDensityAt(i);
            numer += point*w;
            denom += w;
        }

        estimatedTheta = numer/denom;
        return estimatedTheta;
    }

    /**
     * Computes ability estimate using proportional curve fitting. This method
     * is only appropriate with the Rasch, Partial Credit, and Rating Scale models.
     * It may not converge to the maximum value if used with other models.
     * This method is the same method used for person estimation in the Rasch package.
     *
     * @param maxIter maximum number of iterations
     * @param converge convergence criterion (e.g. 0.01)
     * @param adjustment extreme score (i.e. all items correct) adjustment factor
     * @return examinee ability estimate
     */
    public double pcfEstimate(int maxIter, double converge, double adjustment){
        method = EstimationMethod.PCF;
        double theta = 0.0;
        int iter = 0;
        double previousTheta = 0.0;
        double delta = 1.0+converge;
        double TCC1 = 0.0; //this is the TCC at current theta rho
        double TCC2 = 0.0; //this is the TCC at current theta rho + d
//        double MinPRS = 0.0;
//        double MaxPRS = 0.0;

//        double adjustedSumScore = 0.0;
        int index = 0;
//        boolean extreme = false;

        int maxIteration = maxIter;
        double convergenceCriterion = converge;

//        for(int i=0;i<response.length;i++){
//            if(response[i]!=-1){
//                MinPRS += irm[i].getMinScoreWeight();
//                MaxPRS += irm[i].getMaxScoreWeight();
//            }
//        }

        //adjust sum score to be nonextreme (i.e. all items correct or all wrong)
        //convergence criterion set to 0.01 for extreme persons. This makes it
        //consisten with the updateExtremem method in JMLE.java
//        if(sumScore==MinPRS){
//            adjustedSumScore = MinPRS + adjustment;
////            extreme = true;
//        }else if(sumScore==MaxPRS){
//            adjustedSumScore = MaxPRS - adjustment;
////            extreme = true;
//        }else{
//            adjustedSumScore = sumScore;
//        }

//        if(extreme){
//            maxIteration = 1;
//            convergenceCriterion = 0.01;
//            delta = 1.0 + convergenceCriterion;
//        }

        while(delta > convergenceCriterion && iter < maxIteration){
            previousTheta = theta;
            TCC1 = 0.0;
            TCC2 = 0.0;
            index = 0;

            for(ItemResponseModel i : irm){
                if(response[index]!=-1){
                    TCC1 += i.expectedValue(theta);
                    TCC2 += i.expectedValue(theta+delta);
                }
                index++;
            }

            double slope = delta/(logisticOgive(TCC2, MinPRS, MaxPRS)-logisticOgive(TCC1, MinPRS, MaxPRS));
            double intercept = theta - slope*logisticOgive(TCC1, MinPRS, MaxPRS);
            double tempTheta = slope*logisticOgive(getAdjustedSumScore(adjustment), MinPRS, MaxPRS)+intercept;
            //do not change theta by more than one logit per iteration - from WINSTEPS documents
            theta = Math.max(Math.min(theta+1,tempTheta),theta-1);
            delta = Math.abs(previousTheta-theta);
            iter++;
        }

        estimatedTheta = theta;
        return estimatedTheta;
    }

    /**
     * Local logistic ogive from WINSTEPS documentation. Used in pcfEstimate method.
     *
     * @param x observed score
     * @param xMin minimum possible score
     * @param xMax maximum possible score
     * @return
     */
    private double logisticOgive(double x, double xMin, double xMax){
        return Math.log((x-xMin)/(xMax-x));
    }

    public double testInformationAt(double theta){
        double info = 0.0;
        for(ItemResponseModel i : irm){
            info += i.itemInformationAt(theta);
        }

        if(method==EstimationMethod.MAP){
            double sd = mapPrior.getStandardDeviation();
            info += 1.0/(sd*sd);
        }

        return info;
    }

    /**
     * Computes standard error for EAP method.
     *
     * @param theta person ability value
     * @return standard error
     */
    public double eapStandardErrorAt(double theta){
        method = EstimationMethod.EAP;
        int numPoints = distributionApproximation.getNumberOfPoints();
        double point = 0.0;
        double w = 0.0;
        double numer = 0.0;
        double denom = 0.0;
        double dif = 0.0;
        double var = 0.0;

        for(int i=0;i<numPoints;i++){
            point = distributionApproximation.getPointAt(i);
            w = Math.exp(logLikelihood(point))*distributionApproximation.getDensityAt(i);
            dif = point-theta;
            numer += dif*dif*w;
            denom += w;
        }
        var = numer/denom;
        return Math.sqrt(var);
    }

    public double mleStandardErrorAt(double theta){
        method = EstimationMethod.ML;
        double info = testInformationAt(theta);
        info = Math.max(0.0, info);//to prevent sqrt of negative number - but should never occur anyway.
        return 1/Math.sqrt(info);
    }

    public double mapStandardErrorAt(double theta){
        method = EstimationMethod.MAP;
        double info = testInformationAt(theta);
        info = Math.max(0.0, info);//to prevent sqrt of negative number - but should never occur anyway.
        return 1/Math.sqrt(info);
    }

    public double pcfStandardErrorAt(double theta){
        return mleStandardErrorAt(theta);
    }

    public double getTheta(){
        return estimatedTheta;
    }


//============================================================================================================
// Methods needed for BrentOptimizer
//============================================================================================================

    /**
     * Returns value of loglikelihood using DerivativeStructure per interface requirements
     * @param t
     * @return
     */
    public DerivativeStructure value(DerivativeStructure t){
        return new DerivativeStructure(1, 0, 0, logLikelihood(t.getValue()));
    }

    /**
     * Returns first derivative of loglikelihood using DerivativeStructure per interface requirements
     * @param t
     * @return
     */
    public DerivativeStructure derivLogLikelihood(DerivativeStructure t){
        return new DerivativeStructure(1, 1, 0, derivLogLikelihood(t.getValue()));
    }

    public double value(double param){
        return logLikelihood(param);
    }




}

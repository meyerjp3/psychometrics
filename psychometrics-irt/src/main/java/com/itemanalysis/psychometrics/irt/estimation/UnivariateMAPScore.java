package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.*;

import java.util.LinkedHashMap;

public class UnivariateMAPScore extends AbstractUnivariatePersonScoring implements UnivariateFunction {

    private double thetaMin = -7;
    private double thetaMax = 7;
    private int maxIter = 100;
    private double tolerance = 1e-10;
    private byte[] responseVector = null;
    private ItemResponseModel[] irm = null;
    private double estimatedValue = Double.NaN;
    private NormalDistribution normalDistribution = null;

    public UnivariateMAPScore(ItemResponseModel[] irm, double mean, double sd, double thetaMin, double thetaMax, int maxIter, double tolerance){
        this.irm = irm;
        this.thetaMin = thetaMin;
        this.thetaMax = thetaMax;
        this.maxIter = maxIter;
        this.tolerance = tolerance;
        normalDistribution = new NormalDistribution(mean, sd);
    }

    public UnivariateMAPScore(LinkedHashMap<VariableName, ItemResponseModel> irm, double mean, double sd, double thetaMin, double thetaMax, int maxIter, double tolerance){
        this.irm = new ItemResponseModel[irm.size()];

        int index=0;
        for(VariableName v : irm.keySet()){
            this.irm[index] = irm.get(v);
            index++;
        }

        this.thetaMin = thetaMin;
        this.thetaMax = thetaMax;
        this.maxIter = maxIter;
        this.tolerance = tolerance;
        normalDistribution = new NormalDistribution(mean, sd);
    }

    public UnivariateMAPScore(ItemResponseModel[] irm, double mean, double sd){
        this(irm, mean, sd, -7, 7, 100, 0.001);
    }

    public UnivariateMAPScore(ItemResponseModel[] irm){
        this(irm, 0, 1, -7, 7, 100, 0.001);
    }

    /**
     * Maximum likelihood estimate (MLE) of examinee ability.
     *
     * @return MLE of examinee ability
     */
    public double estimate(byte[] responseVector){
        this.responseVector = responseVector;
        UnivariateOptimizer optimizer = new BrentOptimizer(tolerance, 1e-14);
        UnivariatePointValuePair pair = optimizer.optimize(new MaxEval(maxIter),
                new UnivariateObjectiveFunction(this),
                GoalType.MAXIMIZE,
                new SearchInterval(thetaMin, thetaMax));
        estimatedValue = pair.getPoint();
        return estimatedValue;
    }

    public double standardErrorAt(byte[] responseVector, double theta){
        double info = testInformationAt(irm, theta);
        info = Math.max(0.0, info);//to prevent sqrt of negative number - but should never occur anyway.
        return 1/Math.sqrt(info);
    }

    /**
     * For UnivariateFunctionInterface
     *
     * @param theta current estimate
     * @return
     */
    public double value(double theta){
        return logLikelihood(responseVector, irm, theta);
    }

    @Override
    public double logLikelihood(byte[] responseVector, ItemResponseModel[] irm, double theta){
        double ll = super.logLikelihood(responseVector, irm, theta);
        ll += Math.log(normalDistribution.density(theta));
        return ll;
    }

    @Override
    public double testInformationAt(ItemResponseModel[] irm, double theta){
        double info = super.testInformationAt(irm, theta);
        double sd = normalDistribution.getStandardDeviation();
        info += 1.0/(sd*sd);
        return info;
    }

}

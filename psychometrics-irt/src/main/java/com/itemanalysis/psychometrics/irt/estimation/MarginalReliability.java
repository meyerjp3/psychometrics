package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.TidyOutput;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

/**
 * Computes marginal reliability for a group of examinees.
 */
public class MarginalReliability {

    private Mean meanSEM2 = null;

    private Variance thetaVar = null;

    public MarginalReliability(){
        meanSEM2 = new Mean();
        thetaVar = new Variance();
    }

    /**
     * Update statistic with new data points.
     *
     * @param theta an estimate of a person's latent trait score
     * @param sem the standard error of measurement for the estimated trait score.
     */
    public void increment(double theta, double sem){
        meanSEM2.increment(sem*sem);
        thetaVar.increment(theta);
    }

    /**
     * Marginal reliability value.
     *
     * @return marginal reliability
     */
    public double getValue(){
        return (thetaVar.getResult()-meanSEM2.getResult())/thetaVar.getResult();
    }

    public TidyOutput getTidyOutput(){
        TidyOutput tidyOutput = new TidyOutput();

        tidyOutput.addValue("statistic", "marginal_reliability");
        tidyOutput.addValue("value", Double.valueOf(getValue()).toString());

        return tidyOutput;
    }

}

package com.itemanalysis.psychometrics.statistics;

public class WeightedOnlineCorrelation {

    private WeightedOnlineCovariance cov = null;

    private WeightedOnlineStandardDeviation sdX = null;

    private WeightedOnlineStandardDeviation sdY = null;

    public WeightedOnlineCorrelation(boolean unbiased){
        cov = new WeightedOnlineCovariance(unbiased);
        sdX = new WeightedOnlineStandardDeviation(unbiased);
        sdY = new WeightedOnlineStandardDeviation(unbiased);
    }

    public void increment(double x, double y, double weight){
        cov.increment(x, y, weight);
        sdX.increment(x, weight);
        sdY.increment(y, weight);
    }

    public void increment(double x, double y){
        increment(x, y, 1);
    }

    public double getResult(){
        return cov.getResult()/(sdX.getResult()*sdY.getResult());
    }

    public double getN(){
        return cov.getN();
    }

    /**
     * Correct correlation for spuriousness. This method assumes that
     * the test item is Y and the test score is X. Used for the
     * point-biserial and biserial correlation in an item analysis.
     * Method is primarily used for classical item analysis computation
     * of the classical item discimination value.
     *
     * @return correlation corrected for spuriousness
     */
    public Double correctedValue(){
        double testSd = sdX.getResult();
        double itemSd = sdY.getResult();
        double rOld = this.getResult();
        double denom = Math.sqrt(itemSd*itemSd+testSd*testSd-2*rOld*itemSd*testSd);
        if(denom==0.0) return Double.NaN;
        return (rOld*testSd-itemSd)/denom;
    }


}

package com.itemanalysis.psychometrics.statistics;

public class WeightedOnlineStandardDeviation {

    private WeightedOnlineVariance variance = null;

    public WeightedOnlineStandardDeviation(boolean unbiased){
        variance = new WeightedOnlineVariance(unbiased);
    }

    public WeightedOnlineStandardDeviation(){
        this(true);
    }

    public void increment(double x, double weight){
        variance.increment(x, weight);
    }

    public void increment(double x){
        increment(x, 1);
    }

    public double getResult(){
        double v = variance.getResult();
        return Math.sqrt(v);
    }

    public double getN(){
        return variance.getN();
    }

}


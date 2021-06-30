package com.itemanalysis.psychometrics.statistics;

public class WeightedOnlineVariance {

    private double wSum = 0;

    private double wSum2 = 0;

    private double mean = 0;

    private double meanOld = 0;

    private double S = 0;

    private boolean unbiased = true;

    public WeightedOnlineVariance(boolean unbiased){
        this.unbiased = unbiased;
    }

    public WeightedOnlineVariance(){
        this(true);
    }

    /**
     * incrementally update variance
     *
     * @param x a numeric value
     * @param weight a sampling weight
     */
    public void increment(double x, double weight){
        wSum += weight;
        wSum2 += weight*weight;
        meanOld = mean;
        mean = meanOld + (weight/wSum)*(x-meanOld);
        S += weight*(x-meanOld)*(x-mean);
    }

    public void increment(double x){
        increment(x, 1);
    }

    public double getResult(){
        if(unbiased) return S/(wSum-1);
        return S/wSum;
    }

    public double getN(){
        return wSum;
    }


}

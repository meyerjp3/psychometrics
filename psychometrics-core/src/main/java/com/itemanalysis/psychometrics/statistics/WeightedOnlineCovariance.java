package com.itemanalysis.psychometrics.statistics;

public class WeightedOnlineCovariance {

    private double meanx = 0;

    private double meany = 0;

    private double wsum = 0;

    private double wsum2 = 0;

    private double C = 0;

    private boolean unbiased = true;

    public WeightedOnlineCovariance(boolean unbiased){
        this.unbiased = unbiased;
    }

    public void increment(double x, double y, double weight){
        wsum += weight;
        double dx = x - meanx;
        meanx += (weight/wsum)*dx;
        meany += (weight/wsum)*(y-meany);
        C += weight*dx*(y-meany);
    }

    public void increment(double x, double y){
        increment(x, y, 1);
    }

    public double getResult(){
        if(unbiased) return C/(wsum-1);
        return C/wsum;
    }

    public double getN(){
        return wsum;
    }

}


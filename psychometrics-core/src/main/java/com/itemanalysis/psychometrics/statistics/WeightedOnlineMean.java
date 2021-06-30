package com.itemanalysis.psychometrics.statistics;

public class WeightedOnlineMean {

    private double numerator = 0;

    private double denominator = 0;

    public WeightedOnlineMean(){

    }

    /**
     * Increment the statistic using a frequency weight.
     *
     * @param x value of the variable
     * @param weight a frequency weight
     */
    public void increment(double x, double weight){
        numerator += x*weight;
        denominator += weight;
    }

    /**
     * Increment the statistic by one observation
     *
     * @param x value of the variable
     */
    public void increment(double x){
        increment(x, 1);
    }

    public double getResult(){
        return numerator/denominator;
    }

    public double getN(){
        return denominator;
    }


}


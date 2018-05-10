package com.itemanalysis.psychometrics.polycor;

import com.itemanalysis.psychometrics.statistics.TwoWayTable;
import com.itemanalysis.psychometrics.uncmin.Fmin;
import com.itemanalysis.psychometrics.uncmin.Fmin_methods;

import java.util.Formatter;

public class PolychoricTwoStep extends AbstractPolychoricCorrelation{

    /**
     * Constructor for use when data have been summarized in an row x column frequency
     * table.
     *
     * @param data a row by column table of frequencies.
     */
    public PolychoricTwoStep(double[][] data){
        this.data = data;
        nrow = data.length;
        ncol = data[0].length;
        initialize();
    }

    /**
     * Constructor for use when data have been summarized in a TwoWayTable.
     *
     * @param table a TwoWayTable.
     */
    public PolychoricTwoStep(TwoWayTable table){
        this(table.getTable());
    }

    /**
     * Constructor for incrementally updating the data. Must call increment(x, y)
     * to add data to object before calling value().
     */
    public PolychoricTwoStep(){
        incremental = true;
        table = new TwoWayTable();
    }

    /**
     * Two-step estimate of the polychoric correlation.
     *
     * @return a correlation
     */
    public double value(){
        if(incremental){
            this.data = table.getTable();
            this.nrow = data.length;
            this.ncol = data[0].length;
            initialize();
        }

        rowThresholds = new double[nrow-1];
        columnThresholds = new double[ncol-1];
        double[] rSum = cumulativeRowSums();
        double[] cSum = cumulativeColumnSums();

        double prob = 0;
        for(int i=0;i<rowThresholds.length;i++){
            prob = rSum[i]/N;
            rowThresholds[i] = norm.inverseCumulativeProbability(prob);
        }

        for(int j=0;j<columnThresholds.length;j++){
            prob = cSum[j]/N;
            columnThresholds[j] = norm.inverseCumulativeProbability(prob);
        }

        TwoStepLikelihoodFunction likelihoodFunction = new TwoStepLikelihoodFunction(rowThresholds, columnThresholds);
        double[] initial = {0};
        Fmin optimizer = new Fmin();
        rho = optimizer.fmin(-1.0, 1.0, likelihoodFunction, 1e-6);
        rho = Math.max(-1, Math.min(rho, 1));//ensure that correlation is between -1 and 1.

        return rho;
    }

    public String print(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        int am1 = rowThresholds.length;
        int bm1 = columnThresholds.length;

        f.format("%34s", "Polychoric correlation, Two-Step est. = "); f.format("%6.4f", rho); f.format("%n");
        f.format("%n");
        f.format("%-18s", "Row Thresholds"); f.format("%n");

        for(int i=0;i<rowThresholds.length;i++){
            f.format("%6.4f", rowThresholds[i]); f.format("%n");
        }

        f.format("%n");
        f.format("%n");
        f.format("%-19s", "Column Thresholds"); f.format("%n");

        for(int i=0;i<columnThresholds.length;i++){
            f.format("% 6.4f", columnThresholds[i]); f.format("%n");
        }

        f.format("%n");
        return f.toString();

    }

    /**
     * Likelihood function for the two-step approximation. Thresholds are obtained from the inverse
     * cumulative normal quadrature, and teh correlation is found by Brent's method.
     */
    public class TwoStepLikelihoodFunction implements Fmin_methods {
        private double maxCorrelation = 0.9999;
        private double[] rc = null;
        private double[] cc = null;

        public TwoStepLikelihoodFunction(double[] rc, double[] cc){
            this.rc = rc;
            this.cc = cc;
        }

        public double f_to_minimize(double x){
            double rho = x;
            if(Math.abs(rho) > maxCorrelation) rho = Math.signum(rho)*maxCorrelation;
            return logLikelihood(rc, cc, rho);
        }
    }

}

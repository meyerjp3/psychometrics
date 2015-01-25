package com.itemanalysis.psychometrics.polycor;

import com.itemanalysis.psychometrics.distribution.BivariateNormalDistributionImpl;
import com.itemanalysis.psychometrics.statistics.TwoWayTable;
import org.apache.commons.math3.distribution.NormalDistribution;

public abstract class AbstractPolychoricCorrelation implements PolychoricCorrelation{

    protected double[][] data = null;
    protected int nrow = 0;
    protected int ncol = 0;
    protected double N = 0;
    protected double[] rowThresholds = null;
    protected double[] columnThresholds = null;
    protected double rho = Double.NaN;
    protected double fmin = 0;
    protected boolean incremental = false;
    protected TwoWayTable table = null;
    protected NormalDistribution norm = new NormalDistribution();
    private BivariateNormalDistributionImpl bvnorm = new BivariateNormalDistributionImpl();

    protected void initialize(){
        double[] rowSum = new double[nrow];
        double[] colSum = new double[ncol];

        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                rowSum[i]+=data[i][j];
                colSum[j]+=data[i][j];
                N+=data[i][j];
            }
        }

        //Find rows with nonzero sums
        int[] goodRow = new int[nrow];
        int validRowcount = 0;
        for(int i=0;i<nrow;i++){
            if(rowSum[i]!=0){
                validRowcount++;
                goodRow[i]=i;
            }
        }

        //Find columns with nonzero sums
        int[] goodCol = new int[ncol];
        int validColCount = 0;
        for(int i=0;i<ncol;i++){
            if(colSum[i]!=0){
                validColCount++;
                goodCol[i]=i;
            }
        }

        if(validRowcount==0) throw new IllegalArgumentException("All row sums are 0.");
        if(validColCount==0) throw new IllegalArgumentException("All column sums are zero");

        //Eliminate rows and columns with nonzero sums
        if(validRowcount!=nrow || validColCount!=ncol){
            double[][] x2 = new double[validRowcount][validColCount];
            int r=0;
            int c=0;
            N=0;
            for(int i=0;i<nrow;i++){
                c=0;
                for(int j=0;j<ncol;j++){
                    x2[r][c] = data[goodRow[i]][goodCol[j]];
                    N+=x2[r][c];
                    c++;
                }
                r++;
            }
            data=x2;
            nrow = data.length;
            ncol = data[0].length;
        }
    }

    protected double[] cumulativeRowSums(){
        double[] cumSum = new double[nrow];
        double total = 0;

        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                total += data[i][j];
                cumSum[i] = total;
            }
        }
        return cumSum;
    }

    protected double[] cumulativeColumnSums(){
        double[] cumSum = new double[ncol];
        double total = 0;

        for(int j=0;j<ncol;j++){
            for(int i=0;i<nrow;i++){
                total += data[i][j];
                cumSum[j] = total;
            }
        }
        return cumSum;
    }

    protected double[] getParameterArray(double rho, double[] rc, double[] cc){
        int offset = 1;
        double[] par = new double[offset+rc.length+cc.length];
        par[0] = rho;

        for(int i=0;i<rc.length;i++){
            par[offset] = rc[i];
            offset++;
        }
        for(int j=0;j<cc.length;j++){
            par[offset] = cc[j];
            offset++;
        }
        return par;
    }

    protected double extractRho(double[] par, boolean uncmin){
        if(uncmin) return par[1];
        return par[0];
    }

    protected double[] extractRowThresholds(double[] par, boolean uncmin){
        int offset=1;
        double[] rc = new double[nrow-1];
        for(int i=0;i<nrow-1;i++){
            if(uncmin){
                rc[i] = par[offset+1];
            }else{
                rc[i] = par[offset];
            }
            offset++;
        }
        return rc;
    }

    protected double[] extractColumnThresholds(double[] par, boolean uncmin){
        int offset = 1+(nrow-1);
        double[] cc = new double[ncol-1];
        for(int j=0;j<ncol-1;j++){
            if(uncmin){
                cc[j] = par[offset+1];
            }else{
                cc[j] = par[offset];
            }
            offset++;
        }
        return cc;
    }

    protected double logLikelihood(double[] rowThresholds, double[] colThresholds, double rho){
        double logLike = 0;

        double[] rowCuts = new double[nrow+1];
        double[] colCuts = new double[ncol+1];
        rowCuts[0] = -10;
        colCuts[0] = -10;
        rowCuts[nrow] = 10;
        colCuts[ncol] = 10;

        for(int i=0;i<rowThresholds.length;i++){
            rowCuts[i+1] = rowThresholds[i];
        }
        for(int j=0;j<colThresholds.length;j++){
            colCuts[j+1] = colThresholds[j];
        }

//        System.out.println("LL row cuts: " + Arrays.toString(rowCuts));
//        System.out.println("LL col cuts: " + Arrays.toString(colCuts));

        double prob = 0;
        double[] lower = new double[2];
        double[] upper = new double[2];
        for(int i=0;i<rowCuts.length-1;i++){
            for(int j=0;j<colCuts.length-1;j++){
                lower[0] = rowCuts[i];
                lower[1] = colCuts[j];
                upper[0] = rowCuts[i+1];
                upper[1] = colCuts[j+1];
                prob = bvnorm.cumulativeProbability(lower, upper, rho);
                logLike += data[i][j]*Math.log(prob);
            }
        }
        return -logLike;
    }

    public double[] getRowThresholds(){
        return rowThresholds;
    }

    public double[] getColumnThresholds(){
        return columnThresholds;
    }

    public void increment(int x, int y){
        table.addValue(x, y);
    }

    public void increment(char x, char y){
        table.addValue(x, y);
    }

    public void increment(long x, long y){
        table.addValue(x, y);
    }

}

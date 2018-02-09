package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Formatter;

public class GuttmanLambda6 extends AbstractScoreReliability {

    public GuttmanLambda6(double[][] matrix){
        this.matrix = matrix;
        nItems = matrix.length;
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.GUTTMAN_LAMBDA6;
    }

    public double value(){
        double[] smc = squaredMultipleCorrelation(covToCor(this.matrix));

        double sum = 0;
        for(int i=0;i<nItems;i++){
            sum += 1.0-smc[i];
        }

        double observedScoreVariance = this.totalVariance();
        double lambda6 = 1.0 - sum/observedScoreVariance;
        return lambda6;
    }

    /**
     * Creates a submatrix that is the covariance matrix without row itemIndex
     * and column itemIndex;
     *
     * @param itemIndex index of row and column to be omitted.
     * @return submatrix
     */
    private double[][] matrixWithoutItemAt(int itemIndex){
        double[][] m = new double[nItems-1][nItems-1];

        int i2 = 0;
        int j2 = 0;
        for(int i=0;i<nItems;i++){
            if(i!=itemIndex){
                for(int j=0;j<nItems;j++){
                    if(j!= itemIndex){
                        m[i2][j2] = this.matrix[i][j];
                        j2++;
                    }
                }
                i2++;
            }
        }
        return m;
    }

    /**
     * Computes the squared multiple correlation (smc)
     *
     * @return smc
     *
     */
    private double[] squaredMultipleCorrelation(double[][] corMatrix){
        RealMatrix S = new Array2DRowRealMatrix(corMatrix);
        LUDecomposition lu = new LUDecomposition(S);
        RealMatrix X = lu.getSolver().getInverse();

        double[] smc = new double[nItems];
        for(int i=0;i<nItems;i++){
            smc[i] = 1.0-(1.0/X.getEntry(i,i));
        }

        return smc;
    }

    /**
     * Converts the covariance matrix to a correlaiton matrix.
     *
     * @param cov covariance matrix
     * @return correlation matrix
     */
    private double[][] covToCor(double[][] cov){
        double[][] cor = new double[cov.length][cov.length];

        for(int i=0;i<cov.length;i++){
            for(int j=0;j<cov.length;j++){
                cor[i][j] = cov[i][j]/(Math.sqrt(cov[i][i])*Math.sqrt(cov[j][j]));
            }
        }

        return cor;
    }

    /**
     * Computes reliability with each item omitted in turn. The first element in the array is the
     * reliability estimate without the first item. The second item in the array is the reliability
     * estimate without the second item and so on.
     *
     * @return array of item deleted estimates.
     */
    public double[] itemDeletedReliability(){
        double[] rel = new double[nItems];

        GuttmanLambda6 l6 = null;
        for(int i=0;i<nItems;i++){
            l6 = new GuttmanLambda6(matrixWithoutItemAt(i));
            rel[i] = l6.value();
        }

        return rel;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        Formatter f = new Formatter(builder);
        String f2="%.2f";
        f.format("%21s", "Guttman's Lambda-6 = "); f.format(f2,this.value());
        return f.toString();
    }

    public String printItemDeletedSummary(ArrayList<VariableAttributes> var){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-56s", " Guttman's Lambda-6 (SEM in Parentheses) if Item Deleted"); f.format("%n");
        f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", var.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }

}

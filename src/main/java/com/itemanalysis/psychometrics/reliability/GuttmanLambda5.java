package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.data.VariableAttributes;

import java.util.ArrayList;
import java.util.Formatter;

public class GuttmanLambda5 extends AbstractScoreReliability {

    public GuttmanLambda5(double[][] matrix){
        this.matrix = matrix;
        nItems = matrix.length;
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.GUTTMAN_LAMBDA5;
    }

    public double value(){
        double largestSum = sumOfSquareCovariances(0);
        for(int i=1;i<nItems;i++){
            largestSum = Math.max(largestSum, sumOfSquareCovariances(i));
        }

        double observedScoreVariance = this.totalVariance();
        double lambda1 = 1-this.diagonalSum()/observedScoreVariance;
        double lambda5 = lambda1 + 2.0*(Math.sqrt(largestSum)/observedScoreVariance);
        return lambda5;

    }

    /**
     * Sum the squared covariance between teh item at itemIndex and the remaining items.
     *
     * @param itemIndex item index
     * @return sum of the squared covariance
     */
    private double sumOfSquareCovariances(int itemIndex){
        double sum = 0;

        for(int i=0;i<nItems;i++){
            if(i!=itemIndex) sum += matrix[itemIndex][i]*matrix[itemIndex][i];
        }

        return sum;
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
        double totalVariance = this.totalVariance();
        double diagonalSum = this.diagonalSum();
        double totalVarianceAdjusted = 0;
        double diagonalSumAdjusted = 0;
        double lambda1Adjusted = 0;
        double lambda5Adjusted = 0;

        for(int i=0;i<nItems;i++){
            //Compute item variance
            double itemVariance = matrix[i][i];

            //Compute sum of covariance between this item and all others
            double itemCovariance = 0;
            double largestSum = -1;
            for(int j=0;j<nItems;j++){
                if(i!=j){
                    itemCovariance += matrix[i][j];
                    largestSum = Math.max(largestSum, sumOfSquareCovariances(i));
                }
            }
            itemCovariance *= 2;
            totalVarianceAdjusted = totalVariance - itemCovariance - itemVariance;
            diagonalSumAdjusted = diagonalSum - itemVariance;
            lambda1Adjusted = 1-diagonalSumAdjusted/totalVarianceAdjusted;
            lambda5Adjusted = lambda1Adjusted + 2.0*(Math.sqrt(largestSum)/totalVarianceAdjusted);
            rel[i] = lambda5Adjusted;
        }

        return rel;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        Formatter f = new Formatter(builder);
        String f2="%.2f";
        f.format("%21s", "Guttman's Lambda-5 = "); f.format(f2,this.value());
        return f.toString();
    }

    public String printItemDeletedSummary(ArrayList<VariableAttributes> var){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-56s", " Guttman's Lambda-5 (SEM in Parentheses) if Item Deleted"); f.format("%n");
        f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", var.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }

}

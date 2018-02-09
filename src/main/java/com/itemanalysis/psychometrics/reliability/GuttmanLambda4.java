package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.data.VariableAttributes;

import java.util.ArrayList;
import java.util.Formatter;

public class GuttmanLambda4 extends AbstractScoreReliability {

    public GuttmanLambda4(double[][] matrix){
        this.matrix = matrix;
        nItems = matrix.length;
    }

    public ScoreReliabilityType getType() {
        return ScoreReliabilityType.GUTTMAN_LAMBDA4;
    }

    public double value(){
        int[][] index = splitTest();
        double totalVariance = this.totalVariance();
        double part1TotalVariance = partTotalVariance(index[0]);
        double part2TotalVariance = partTotalVariance(index[1]);
        double lambda4 = 2.0*(1.0-(part1TotalVariance+part2TotalVariance)/totalVariance);
        return lambda4;
    }

    private double valueWithoutItemAt(int omittedItem){
        int[][] index = splitTestWithoutItemsAt(omittedItem);
        double totalVariance = this.totalVariance();
        double itemVariance = matrix[omittedItem][omittedItem];

        //Compute sum of covariance between this item and all others
        double itemCovariance = 0;
        for(int j=0;j<nItems;j++){
            if(omittedItem!=j) itemCovariance += matrix[omittedItem][j];
        }
        itemCovariance *= 2;

        double totalVarianceAdjusted = totalVariance - itemCovariance - itemVariance;
        double part1TotalVariance = partTotalVariance(index[0]);
        double part2TotalVariance = partTotalVariance(index[1]);
        double lambda4 = 2.0*(1.0-(part1TotalVariance+part2TotalVariance)/totalVarianceAdjusted);
        return lambda4;
    }

    /**
     * Splits test into first half and second half. If the test length is odd, then the
     * last item in the first half is also the first item in teh second half.
     * @return item indices for two parts.
     */
    private int[][] splitTest(){
        int halfTestLength = nItems/2;
        int splitIndex[][] = new int[2][halfTestLength];

        for(int i=0;i<halfTestLength;i++){
            splitIndex[0][i] = i;
            splitIndex[1][i] = halfTestLength + i;
        }

        if((nItems % 2) == 0){
            //even number of items

        }else{
            //odd number of items
            //set first item index in part 2 to be the same as last item index in part 1.
            splitIndex[1][0] = splitIndex[0][halfTestLength-1];
        }
        return splitIndex;
    }

    private int[][] splitTestWithoutItemsAt(int omittedItem){
        int halfTestLength = (nItems-1)/2;
        int splitIndex[][] = new int[2][halfTestLength];

        //create array of item indices without the omitted item index
        int index = 0;
        int[] validIndex = new int[nItems-1];
        for(int i=0;i<nItems;i++){
            if(i!=omittedItem){
                validIndex[index] = i;
                index++;
            }
        }

        for(int i=0;i<halfTestLength;i++){
            splitIndex[0][i] = validIndex[i];
            splitIndex[1][i] = validIndex[halfTestLength + i];
        }


        if(((nItems-1) % 2) == 0){
            //even number of items

        }else{
            //odd number of items
            //set first item index in part 2 to be the same as last item index in part 1.
            splitIndex[1][0] = splitIndex[0][halfTestLength-1];

        }
        return splitIndex;
    }

    private double partTotalVariance(int[] itemIndex){
        double totalVariance = 0.0;

        for(int i=0;i<itemIndex.length;i++){
            for(int j=0;j<itemIndex.length;j++){
                totalVariance += this.matrix[itemIndex[i]][itemIndex[j]];
            }
        }

        return totalVariance;
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
        double itemVariance = 0;
        double itemCovariance = 0;
        double totalVarianceAdjusted = 0;
        double part1TotalVariance = 0;
        double part2TotalVariance = 0;
        double lambda4Adjusted = 0;

        for(int i=0;i<nItems;i++){
            int[][] index = splitTestWithoutItemsAt(i);
            itemVariance = matrix[i][i];

            //Compute sum of covariance between this item and all others
            itemCovariance = 0;
            for(int j=0;j<nItems;j++){
                if(i!=j) itemCovariance += matrix[i][j];
            }
            itemCovariance *= 2;

            totalVarianceAdjusted = totalVariance - itemCovariance - itemVariance;
            part1TotalVariance = partTotalVariance(index[0]);
            part2TotalVariance = partTotalVariance(index[1]);
            lambda4Adjusted = 2.0*(1.0-(part1TotalVariance+part2TotalVariance)/totalVarianceAdjusted);
            rel[i] = lambda4Adjusted;
        }

        return rel;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        Formatter f = new Formatter(builder);
        String f2="%.2f";
        f.format("%21s", "Guttman's Lambda-4 = "); f.format(f2,this.value());
        return f.toString();
    }

    public String printItemDeletedSummary(ArrayList<VariableAttributes> var){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-56s", " Guttman's Lambda-4 (SEM in Parentheses) if Item Deleted"); f.format("%n");
        f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", var.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }

}

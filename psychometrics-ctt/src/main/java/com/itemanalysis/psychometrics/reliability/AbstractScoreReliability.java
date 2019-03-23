/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.commons.math3.distribution.FDistribution;

import java.util.ArrayList;
import java.util.Formatter;

/**
 * Methods implemented by all instances of ScoreReliability.
 *
 */
public abstract class AbstractScoreReliability implements ScoreReliability,  Comparable<ScoreReliability>{

    protected double[][] matrix = null;

    protected int nItems=0;

    public double totalVariance(){
        double total = 0.0;
        for(int i=0;i<nItems;i++){
            for(int j=0;j<nItems;j++){
                total += matrix[i][j];
            }
        }
        return total;
    }

    protected double diagonalSum(){
        double total = 0.0;
        for(int i=0;i<nItems;i++){
            total += matrix[i][i];
        }
        return total;
    }

    protected double rowSum(int row){
        double total = 0.0;
        for(int i=0;i<nItems;i++){
            total += matrix[row][i];
        }
        return total;
    }

    /**
     * This confidence interval applies to Coefficient alpha because it has a known sampling quadrature.
     * For other reliabilty methods it is only an approximation of the confidence interval because the
     * sampling quadrature of other reliability estimates is not known.
     *
     * @return confidence interval as an array with the lower bound in position 0 and the upper bound in position 1.
     */
    public double[] confidenceInterval(double numberOfExaminees){
//        double numberOfExaminees = matrix.getMaxSampleSize();
        double[] confidenceInterval = new double[2];
        double numberOfItems = (double)nItems;
		double df1=numberOfExaminees-1.0;
		double df2=(numberOfExaminees-1.0)*(numberOfItems-1.0);

        try{
            FDistribution fDist = new FDistribution(df1, df2);
            confidenceInterval[0] = 1.0-((1.0-this.value())*fDist.inverseCumulativeProbability(0.975));
            confidenceInterval[1] = 1.0-((1.0-this.value())*fDist.inverseCumulativeProbability(0.025));
        }catch(Exception ex){
            confidenceInterval[0] = Double.NaN;
            confidenceInterval[1] = Double.NaN;
        }
		return confidenceInterval;
	}

    public String confidenceIntervalToString(double[] confidenceInterval){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		f.format("(%6.4f, ",confidenceInterval[0]);
        f.format("%6.4f)",confidenceInterval[1]);
		return f.toString();
	}

    /**
     * Creates a submatrix that is the covariance matrix without row itemIndex
     * and column itemIndex;
     *
     * @param itemIndex index of row and column to be omitted.
     * @return submatrix
     */
    protected double[][] matrixWithoutItemAt(int itemIndex){
        double[][] m = new double[nItems-1][nItems-1];

        int i2 = 0;
        int j2 = 0;
        for(int i=0;i<nItems;i++){
            j2 = 0;
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

    protected double varianceWithoutItemAt(int itemIndex){
        double var = 0.0;

        for(int i=0;i<nItems;i++){
            if(i!=itemIndex){
                for(int j=0;j<nItems;j++){
                    if(j!= itemIndex){
                        var += this.matrix[i][j];
                    }
                }
            }
        }
        return var;
    }

    public String printItemDeletedSummary(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-56s", " " + getType().toString() + " (SEM in Parentheses) if Item Deleted"); f.format("%n");
        f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", "Item "+(i+1)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }

    public String printItemDeletedSummary(ArrayList<VariableAttributes> variableAttributes){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-56s", " " + getType().toString() + " (SEM in Parentheses) if Item Deleted"); f.format("%n");
        f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", variableAttributes.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }

    public int compareTo(ScoreReliability that){
        double v1 = this.value();
        double v2 = that.value();
		if(v1>v2) return 1;
		if(v1<v2) return -1;
		return 0;
	}

	public boolean equals(Object obj){
		if(this==obj)return true;
		if(obj == null) return false;
        if(obj instanceof ScoreReliability){
            return compareTo((ScoreReliability)obj)==0;
        }else{
            return false;
        }
	}

	public int hashCode(){
		Double v = new Double(value());
		return v.hashCode();
	}

}

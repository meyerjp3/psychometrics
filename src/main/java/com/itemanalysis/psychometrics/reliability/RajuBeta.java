/*
 * Copyright 2012 J. Patrick Meyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;

import java.util.ArrayList;
import java.util.Formatter;



public class RajuBeta extends AbstractScoreReliability{

	private double[] lambda;
	
	public RajuBeta(CovarianceMatrix matrix, boolean unbiased){
		this.matrix = matrix;
        this.unbiased = unbiased;
        nItems = matrix.getNumberOfVariables();
		double ni=(double)nItems;
		
		lambda = new double[nItems];
		for(int i=0;i<nItems;i++){
			lambda[i]=1/ni;
		}
	}

    public RajuBeta(CovarianceMatrix matrix){
        this(matrix, false);
    }
	
	public RajuBeta(CovarianceMatrix matrix, double[] numberOfItems, boolean unbiased){
		this.matrix=matrix;
        this.unbiased = unbiased;
		nItems = matrix.getNumberOfVariables();
		for(int i=0;i<nItems;i++){
			lambda[i]=1/numberOfItems[i];
		}
	}

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.RAJU_BETA;
    }
	
	private double sumLambdaSquared(){
		double sumLambda=0.0;
		
		for(int i=0;i<lambda.length;i++){
			sumLambda+=Math.pow(lambda[i],2);
		}
		return sumLambda;
	}
	
	public double value(){
		double sumLambda2 = sumLambdaSquared();
		double observedScoreVariance = matrix.totalVariance(unbiased);
		double componentVariance = matrix.diagonalSum(unbiased);
		double raju=(1/(1-sumLambda2))*((observedScoreVariance-componentVariance)/observedScoreVariance);
		return raju;
	}

    public double[] itemDeletedReliability(){
        double[] rel = new double[nItems];
        CovarianceMatrix cm = null;
        RajuBeta rb = null;
        for(int i=0;i<nItems;i++){
            cm = matrix.matrixSansVariable(i, unbiased);
            rb = new RajuBeta(cm, unbiased);
            rel[i] = rb.value();
        }
        return rel;
    }

    @Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		String f2="%.2f";
		f.format("%14s", "Raju's Beta = "); f.format(f2,this.value());
		return f.toString();
	}

    public String ifDeletedToString(ArrayList<VariableAttributes> var){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-55s", " Raju's Beta (SEM in Parentheses) if Item Deleted"); f.format("%n");
		f.format("%-55s", "======================================================="); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", var.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " "); f.format("%n");
        }
        return f.toString();
    }

}

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

import java.util.Formatter;



public class RajuBeta extends AbstractScoreReliability{

	private double[] lambda;
	
	public RajuBeta(double[][] matrix){
		this.matrix = matrix;
        nItems = matrix.length;
		double ni=(double)nItems;
		
		lambda = new double[nItems];
		for(int i=0;i<nItems;i++){
			lambda[i]=1/ni;
		}
	}
	
	public RajuBeta(double[][] matrix, double[] numberOfItems){
		this.matrix=matrix;
		nItems = matrix.length;
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

	private double sumLambdaSquaredWithItemAt(int index){
		double sumLambda2=0.0;

		double LAMBDA = 1/((double)nItems-1); //TODO allow user to provide Lambda values.

		for(int i=0;i<lambda.length;i++){
			if(i != index) sumLambda2+=Math.pow(LAMBDA,2); //TODO allow user to provide Lambda values.
		}
		return sumLambda2;
	}

	public double value(){
		double sumLambda2 = sumLambdaSquared();
		double observedScoreVariance = this.totalVariance();
		double componentVariance = this.diagonalSum();
		double raju=(1/(1-sumLambda2))*((observedScoreVariance-componentVariance)/observedScoreVariance);
		return raju;
	}

    public double[] itemDeletedReliability(){
		double[] rel = new double[nItems];
		double totalVariance = this.totalVariance();
		double diagonalSum = this.diagonalSum();
		double totalVarianceAdjusted = 0;
		double diagonalSumAdjusted = 0;
		double sumLambda2Adjusted = 0;
		double reliabilityWithoutItem = 0;


		for(int i=0;i<nItems;i++){
			//Compute item variance
			double itemVariance = matrix[i][i];

			//Compute sum of covariance between this item and all others
			double itemCovariance = 0;
			for(int j=0;j<nItems;j++){
				if(i!=j) itemCovariance += matrix[i][j];
			}
			itemCovariance *= 2;

			sumLambda2Adjusted = sumLambdaSquaredWithItemAt(i);
			totalVarianceAdjusted = totalVariance - itemCovariance - itemVariance;
			diagonalSumAdjusted = diagonalSum - itemVariance;
			reliabilityWithoutItem = (1/(1-sumLambda2Adjusted))*(1.0 - diagonalSumAdjusted/totalVarianceAdjusted);
			rel[i] = reliabilityWithoutItem;
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

}

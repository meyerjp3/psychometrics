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



/**
 * Computes Guttman's Lambda-2 reliability coefficient.
 * @author J. Patrick Meyer
 * @since January 29, 2008
 *
 */
public class GuttmanLambda extends AbstractScoreReliability{

	public GuttmanLambda(CovarianceMatrix matrix, boolean unbiased){
		this.matrix = matrix;
        this.unbiased = unbiased;
        nItems = matrix.getNumberOfVariables();
	}

    public GuttmanLambda(CovarianceMatrix matrix){
        this(matrix, false);
    }

    public void isUnbiased(boolean unbiased){
        this.unbiased = unbiased;
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.GUTTMAN_LAMBDA;
    }
	
	public double value(){
		double observedScoreVariance = matrix.totalVariance(unbiased);
		double lambda1 = 1-matrix.diagonalSum(unbiased)/observedScoreVariance;
		double k = Double.valueOf(matrix.getNumberOfVariables()).doubleValue();
		double ssV=0.0;
		
		for(int i=0;i<nItems;i++){
			for(int j=0;j<nItems;j++){
				if(i!=j){
					ssV+=Math.pow(matrix.getCovarianceAt(i, j, unbiased),2);
				}
			}
		}
		double gl = lambda1 + Math.sqrt((k/(k-1))*ssV)/observedScoreVariance;
		return gl;
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
        CovarianceMatrix cm = null;
        GuttmanLambda gl = null;
        for(int i=0;i<nItems;i++){
            cm = matrix.matrixSansVariable(i, unbiased);
            gl = new GuttmanLambda(cm, unbiased);
            rel[i] = gl.value();
        }
        return rel;
    }

    @Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		String f2="%.2f";
		f.format("%21s", "Guttman's Lambda-2 = "); f.format(f2,this.value());
		return f.toString();
	}

    public String ifDeletedToString(ArrayList<VariableAttributes> var){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-56s", " Guttman's Lambda-2 (SEM in Parentheses) if Item Deleted"); f.format("%n");
		f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", var.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }

}

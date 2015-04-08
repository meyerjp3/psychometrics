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
 * Computes teh Feldt-Brennan estimate of reliability. This method assumes that
 * scores are classically congeneric.
 */
public class FeldtBrennan extends AbstractScoreReliability{
	
	public FeldtBrennan(CovarianceMatrix matrix, boolean unbiased){
		this.matrix = matrix;
        this.unbiased = unbiased;
        nItems = matrix.getNumberOfVariables();
	}

    public FeldtBrennan(CovarianceMatrix matrix){
        this(matrix, false);
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.FELDT_CLASSICAL_CONGENERIC;
    }

	private double squaredRowSums(){
		double value=0.0;
		for(int i=0;i<nItems;i++){
			value+=Math.pow(matrix.rowSum(i, true), 2);
		}
		return value;
	}

    /**
     * Computes the Feldt-Brennan estimate of reliability
     *
     * @return estimate of reliability
     */
	public double value(){
		double observedScoreVariance = matrix.totalVariance(unbiased);
		double componentVariance = matrix.diagonalSum(unbiased);
		double fcc=(observedScoreVariance*(observedScoreVariance-componentVariance))/(Math.pow(observedScoreVariance, 2)-this.squaredRowSums());
		return fcc;
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
        FeldtBrennan fb = null;
        for(int i=0;i<nItems;i++){
            cm = matrix.matrixSansVariable(i, unbiased);
            fb = new FeldtBrennan(cm, unbiased);
            rel[i] = fb.value();
        }
        return rel;
    }

    @Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		String f2="%.2f";
		f.format("%16s", "Feldt-Brennan = "); f.format(f2,this.value());
		return f.toString();
	}

    public String ifDeletedToString(ArrayList<VariableAttributes> var){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-56s", " Feldt-Brennan if Item Deleted"); f.format("%n");
		f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", var.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }

}

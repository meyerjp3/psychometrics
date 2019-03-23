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


/**
 * Computes teh Feldt-Brennan estimate of reliability. This method assumes that
 * scores are classically congeneric.
 */
public class FeldtBrennan extends AbstractScoreReliability{
	
	public FeldtBrennan(double[][] matrix){
		this.matrix = matrix;
        nItems = matrix.length;
	}

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.FELDT_CLASSICAL_CONGENERIC;
    }

	private double squaredRowSums(){
		double value=0.0;
		for(int i=0;i<nItems;i++){
			value+=Math.pow(this.rowSum(i), 2);
		}
		return value;
	}

    /**
     * Computes the Feldt-Brennan estimate of reliability
     *
     * @return estimate of reliability
     */
	public double value(){
		double observedScoreVariance = this.totalVariance();
		double componentVariance = this.diagonalSum();
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

        FeldtBrennan fb = null;
        for(int i=0;i<nItems;i++){
            fb = new FeldtBrennan(matrixWithoutItemAt(i));
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

}

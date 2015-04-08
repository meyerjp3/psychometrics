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
 * Computes teh Feldt-Gilmer estimate of reliability. This method assumes score are
 * classically congeneric.
 */
public class FeldtGilmer extends AbstractScoreReliability{

	public FeldtGilmer(CovarianceMatrix matrix, boolean unbiased){
		this.matrix=matrix;
        this.unbiased = unbiased;
        nItems = matrix.getNumberOfVariables();
	}

    public FeldtGilmer(CovarianceMatrix matrix){
        this(matrix, false);
    }

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.FELDT_GILMER;
    }
	
	private int getEll(){
        double[] offDiag= new double[nItems];
		for(int i=0;i<nItems;i++){
			offDiag[i]=matrix.rowSum(i, unbiased)-matrix.getVarianceAt(i, unbiased);
		}

		int maxIndex=0;
		double maxValue=offDiag[0];
		for(int i=1;i<nItems;i++){
			if(offDiag[i]>maxValue){
				maxIndex=i;
				maxValue=offDiag[i];
			}
		}
		return maxIndex;
	}
	
	private double[] D(int ell){
		double[] d = new double[nItems];
		double num=0.0;
		double denom=0.0;
		
		for(int i=0;i<nItems;i++){
			if(i==ell){
				d[i]=1.0;
			}else{
				num=matrix.rowSum(i, unbiased)-matrix.getCovarianceAt(i, ell, unbiased)-matrix.getVarianceAt(i, unbiased);
				denom=matrix.rowSum(ell, unbiased)-matrix.getCovarianceAt(i, ell, unbiased)-matrix.getVarianceAt(ell, unbiased);
				d[i]=num/denom;
			}
		}
		return d;
	}
	
	public double value(){
		if(nItems<3) return Double.NaN;
		int ell = getEll();
		double[] d=D(ell);
		double sumD=0.0;
		double sumD2=0.0;
		double observedScoreVariance = matrix.totalVariance(unbiased);
		double componentVariance = matrix.diagonalSum(unbiased);
		
		for(int i=0;i<nItems;i++){
			sumD+=d[i];
			sumD2+=Math.pow(d[i], 2);
		}
		
		double fg=(Math.pow(sumD, 2)/(Math.pow(sumD, 2)-sumD2))*((observedScoreVariance-componentVariance)/observedScoreVariance);
		return fg;
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
        FeldtGilmer fg = null;
        for(int i=0;i<nItems;i++){
            cm = matrix.matrixSansVariable(i, unbiased);
            fg = new FeldtGilmer(cm, unbiased);
            rel[i] = fg.value();
        }
        return rel;
    }

    @Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		Formatter f = new Formatter(builder);
		String f2="%.2f";
		f.format("%15s", "Feldt-Gilmer = "); f.format(f2,this.value());
		return f.toString();
	}

    public String ifDeletedToString(ArrayList<VariableAttributes> var){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        double[] del = itemDeletedReliability();
        f.format("%-56s", " Feldt-Gilmer  (SEM in Parentheses) if Item Deleted"); f.format("%n");
		f.format("%-56s", "========================================================"); f.format("%n");
        for(int i=0;i<del.length;i++){
            f.format("%-10s", var.get(i)); f.format("%5s", " ");
            f.format("%10.4f", del[i]); f.format("%5s", " ");f.format("%n");
        }
        return f.toString();
    }

}

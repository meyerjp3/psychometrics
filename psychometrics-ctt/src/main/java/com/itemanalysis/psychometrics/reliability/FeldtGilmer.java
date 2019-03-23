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
 * Computes teh Feldt-Gilmer estimate of reliability. This method assumes score are
 * classically congeneric.
 */
public class FeldtGilmer extends AbstractScoreReliability{

	public FeldtGilmer(double[][] matrix){
		this.matrix=matrix;
        nItems = matrix.length;
	}

    public ScoreReliabilityType getType(){
        return ScoreReliabilityType.FELDT_GILMER;
    }
	
	private int getEll(){
        double[] offDiag= new double[nItems];
		for(int i=0;i<nItems;i++){
			offDiag[i]=this.rowSum(i)-matrix[i][i];
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
				num=this.rowSum(i)-matrix[i][ell]-matrix[i][i];
				denom=this.rowSum(ell)-matrix[i][ell]-matrix[ell][ell];
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
		double observedScoreVariance = this.totalVariance();
		double componentVariance = this.diagonalSum();
		
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

        FeldtGilmer fg = null;
        for(int i=0;i<nItems;i++){
            fg = new FeldtGilmer(matrixWithoutItemAt(i));
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

}

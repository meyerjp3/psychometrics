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
package com.itemanalysis.psychometrics.statistics;


import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat.OutputAlignment;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Computes NxN covariance and correlation matrix. The Covariance
 * class will automatically do a pairwise deletion. For listwise 
 * deletion, the data should be screened for missing values before 
 * calling increment() and increment should only be called when a
 * particular case has data for all variableNames. For example, do not
 * call increment() if a case is missing data on any of the 
 * variableNames in a matrix and listwise deletion is required.
 * 
 * @author J. Patrick Meyer
 * @since January 27, 2008
 *
 */
public class StreamingCovarianceMatrix {

	private StreamingCovariance[][] covMat;
    private int numberOfVariables=0;
    private ArrayList<VariableName> variableNames = null;
    private boolean unbiased = true;//use n-1 instead of n

    public StreamingCovarianceMatrix(ArrayList<VariableName> variableNames, boolean unbiased){
        this.variableNames = variableNames;
        this.unbiased = unbiased;
		this.numberOfVariables= variableNames.size();
		covMat = new StreamingCovariance[numberOfVariables][numberOfVariables];
        initializeMatrix();

	}

    public StreamingCovarianceMatrix(ArrayList<VariableName> variableNames){
        this(variableNames, true);
    }

    public StreamingCovarianceMatrix(LinkedHashMap<VariableName, VariableAttributes> variableAttributeMap, boolean unbiased){
        this.unbiased = unbiased;
        this.variableNames = new ArrayList<VariableName>();
        for(VariableName v : variableAttributeMap.keySet()){
            this.variableNames.add(v);
        }

		this.numberOfVariables= variableNames.size();
		covMat = new StreamingCovariance[numberOfVariables][numberOfVariables];
        initializeMatrix();
	}

    public StreamingCovarianceMatrix(LinkedHashMap<VariableName, VariableAttributes> variableAttributeMap){
        this(variableAttributeMap, true);
    }

    /**
     * This constructor is primarily used in the TestSummary.java class.
     *
     * @param numberOfVariables
     */
    public StreamingCovarianceMatrix(int numberOfVariables, boolean unbiased){
        this.numberOfVariables=numberOfVariables;
        this.unbiased = unbiased;

        this.variableNames = new ArrayList<VariableName>();
        VariableName v = null;
        for(int i=0;i<numberOfVariables;i++){
            v = new VariableName("V"+(i+1));
            this.variableNames.add(v);
        }

        covMat = new StreamingCovariance[numberOfVariables][numberOfVariables];
        initializeMatrix();
    }

    public StreamingCovarianceMatrix(int numberOfVariables){
        this(numberOfVariables, true);
    }

    public StreamingCovarianceMatrix(double[][] x, boolean unbiased){
        this.numberOfVariables=numberOfVariables;
        this.unbiased = unbiased;
        this.variableNames = new ArrayList<VariableName>();
        VariableName v = null;
        for(int i=0;i<numberOfVariables;i++){
            v = new VariableName("V"+(i+1));
            this.variableNames.add(v);
        }

        covMat = new StreamingCovariance[numberOfVariables][numberOfVariables];
        initializeMatrix();

        //loop over data and increment covariance matrix
        for(int i=0;i<x.length;i++){
            for(int j=0;j<numberOfVariables;j++){
                for(int k=0;k<numberOfVariables;k++)
                    this.increment(j, k, x[i][j], x[i][k]);
            }
        }

    }

    public StreamingCovarianceMatrix(double[][] x){
        this(x, true);
    }

    //Initialize covariance matrix
    private void initializeMatrix(){
        for(int i=0;i<numberOfVariables;i++){
            for(int j=0;j<numberOfVariables;j++){
                covMat[i][j] = new StreamingCovariance(unbiased);
            }
        }
    }

    public void setNameAt(int index, VariableName name){
        variableNames.add(index, name);
    }

    public void setNameAt(int index, String name){
        variableNames.add(index, new VariableName(name));
    }

    public double getMinSampleSize(){
        double minSampleSize = covMat[0][0].sampleSize();
        for(int i=0;i<covMat.length;i++){
            for(int j=i;j<covMat[0].length;j++){
                minSampleSize = Math.min(minSampleSize, covMat[i][j].sampleSize());
            }
        }
        return minSampleSize;
    }

    public double getMaxSampleSize(){
        double maxSampleSize = 0;
        for(int i=0;i<covMat.length;i++){
            for(int j=i;j<covMat[0].length;j++){
                maxSampleSize = Math.max(maxSampleSize, covMat[i][j].sampleSize());
            }
        }
        return maxSampleSize;
    }

    /**
     * Increment values in the matrix.
     * Only increments for the upper diagonal of the matrix.
     *
     * @param xIndex index of variable x
     * @param yIndex index of variable y
     * @param x value of variable x
     * @param y value of variable y
     */
	public void increment(int xIndex, int yIndex, double x, double y){
        if(yIndex>=xIndex){
            covMat[xIndex][yIndex].increment(x, y);
        }
	}

    /**
     * Gives the value of the covriance matrix as a two-way array.
     *
     * @return covariance matrix
     */
	public double[][] value(){
		double[][] cov = new double[numberOfVariables][numberOfVariables];
		for(int i=0;i<numberOfVariables;i++){
			for(int j=i;j<numberOfVariables;j++){
                cov[i][j]=covMat[i][j].value();
                cov[j][i]=covMat[i][j].value();
			}
		}
		return cov;
	}

    /**
     * Gives the covariance matrix with the off-diagonal elements set to zero.
     *
     * @return diagonal matrix of covariances
     */
	public double[][] diagonalMatrix(){
        double[][] cov = new double[numberOfVariables][numberOfVariables];
        for(int i=0;i<numberOfVariables;i++){
            for(int j=i;j<numberOfVariables;j++){
                cov[i][j] = 0.0;
            }
            cov[i][i]=covMat[i][i].value();
        }
        return cov;
    }

    public RealMatrix diagonalMatrixAsMatrix(){
	    RealMatrix matrix = new Array2DRowRealMatrix(diagonalMatrix());
	    return matrix;
    }

	public RealMatrix valueAsMatrix(){
	    RealMatrix matrix = new Array2DRowRealMatrix(value());
	    return matrix;
    }
	
	public double[][] correlation(){
		double[][] cor = new double[numberOfVariables][numberOfVariables];
		for(int i=0;i<numberOfVariables;i++){
			for(int j=i;j<numberOfVariables;j++){
				cor[i][j]=covMat[i][j].correlation();
                cor[j][i]=covMat[i][j].correlation();
			}
		}
		return cor;
	}

    public double getStandardErrorAt(int i, int j){
        if(j<i) return covMat[j][i].correlationStandardError();
        return covMat[i][j].correlationStandardError();
    }

    public double getPvalueAt(int i, int j){
        if(j<i) return covMat[j][i].correlationPvalue();
        return covMat[i][j].correlationPvalue();
    }

    public double getCovarianceAt(int i, int j){
        if(j>=i) return covMat[i][j].value();
        return covMat[j][i].value();
        
    }

    public double getCorrelationAt(int i, int j){
        if(j<i)return covMat[j][i].correlation();
        return covMat[i][j].correlation();
    }
	
	public double totalVariance(){
		double sum=0;
		for(int i=0;i<numberOfVariables;i++){
			for(int j=0;j<numberOfVariables;j++){
                if(j<i){
                    sum+=covMat[j][i].value();
                }else{
                    sum+=covMat[i][j].value();
                }
			}
		}
		return sum;
	}
	
	public double diagonalSum(){
		double sum=0;
		for(int i=0;i<numberOfVariables;i++){
            sum+=covMat[i][i].value();
		}
		return sum;
	}
	
	public double covarianceSum(){
		double sum=0;
		for(int i=0;i<numberOfVariables;i++){
			for(int j=i;j<numberOfVariables;j++){
				if(i!=j){
                    sum+=covMat[i][j].value()*2.0;
				}
			}
		}
		return sum;
	}
	
	public double getVarianceAt(int index){
        return covMat[index][index].value();
	}
	
	public double rowSum(int rowIndex){
		double sum=0.0;
		for(int j=0;j<numberOfVariables;j++){
            if(j<rowIndex){
                sum+=covMat[j][rowIndex].value();
            }else{
                sum+=covMat[rowIndex][j].value();
            }
		}
		return sum;
	}
	
	public double columnSum(int columnIndex){
		double sum=0.0;
		for(int i=0;i<numberOfVariables;i++){
            if(columnIndex<i){
                sum+=covMat[columnIndex][i].value();
            }else{
                sum+=covMat[i][columnIndex].value();
            }
		}
		return sum;
	}
	
	public int getNumberOfVariables(){
		return covMat.length;
	}
	
//	/**
//	 *
//	 * @return double indicating sample size. This method is only valid for
//	 * listwise deletion. Sample sizes vary for each pair with pairwise
//	 * deletion.
//	 */
//	public double listwiseSampleSize(){
//	    double N = covMat[0][0].sampleSize();
//	    for(int i=0;i<covMat.length;i++){
//	        for(int j=0;j<covMat[0].length;j++){
//	            N = Math.min(N, covMat[i][j].sampleSize());
//            }
//        }
//		return covMat[0][0].sampleSize();
//	}

    @Override
	public String toString(){
		return printCovarianceMatrix();
	}

    public String printCorrelationMatrix(boolean showStdError){
        return printMatrix("CORRELATION MATRIX", true, showStdError);
    }

    public String printCovarianceMatrix(){
        return printMatrix("COVARIANCE MATRIX", false, false);
    }

    private String printMatrix(String title, boolean correlation, boolean showStdError){
        TextTable table = new TextTable();

        TextTableColumnFormat[] cformats = new TextTableColumnFormat[numberOfVariables+1];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setStringFormat(10, OutputAlignment.LEFT);
        for(int i=0;i<numberOfVariables;i++){
            cformats[i+1] = new TextTableColumnFormat();
            cformats[i+1].setDoubleFormat(10, 4, OutputAlignment.RIGHT);
        }
        double exrtaRowFactor = 1.0;
        if(showStdError)exrtaRowFactor = 3.0;
        int nrows = (numberOfVariables);
        nrows =  (int)((double)nrows*exrtaRowFactor+5);
        table.addAllColumnFormats(cformats, nrows);
        table.setAllCellPadding(2);

        table.getRowAt(0).addHeader(0, numberOfVariables+1, title, TextTablePosition.LEFT);
        table.getRowAt(1).addHorizontalRule(0, numberOfVariables+1, "=");

        for(int i=0;i<numberOfVariables;i++){
            table.getRowAt(2).addHeader(i+1, 1, variableNames.get(i).toString(), TextTablePosition.RIGHT);
        }
        table.getRowAt(3).addHorizontalRule(0, numberOfVariables+1, "-");

        int row = 0;
        for(int i=0;i<numberOfVariables;i++){
            row = 4+i*(int)exrtaRowFactor;
            table.getRowAt(row).addStringAt(0, variableNames.get(i).toString());
            for(int j=0;j<numberOfVariables;j++){
                if(correlation){
                    table.getRowAt(row).addDoubleAt(j+1, this.getCorrelationAt(i, j));
                    if(showStdError){
                        table.getRowAt(row+1).addDoubleAt(j+1, this.getStandardErrorAt(i, j));
                        table.getRowAt(row+2).addDoubleAt(j+1, this.getPvalueAt(i, j));
                    }
                }else{
                    table.getRowAt(row).addDoubleAt(j+1, this.getCovarianceAt(i, j));
                }
                
            }
        }
        table.getRowAt(nrows-1).addHorizontalRule(0, numberOfVariables+1, "=");

        return table.toString();
    }

}

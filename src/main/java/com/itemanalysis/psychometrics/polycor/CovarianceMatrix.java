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
package com.itemanalysis.psychometrics.polycor;


import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat.OutputAlignment;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Computes NxN covariance and correlation matrix. The Covariance
 * class will automatically do a pairwise deletion. For listwise 
 * deletion, the data should be screened for missing values before 
 * calling increment() and increment should only be called when a
 * particular case has data for all variables. For example, do not 
 * call increment() if a case is missing data on any of the 
 * variables in a matrix and listwise deletion is required.
 * 
 * @author J. Patrick Meyer
 * @since January 27, 2008
 *
 */
public class CovarianceMatrix {

	private Covariance[][] covMat;
    private int numberOfVariables=0;
    private ArrayList<VariableAttributes> variables = null;

    public CovarianceMatrix(double[][] covarianceMatrix){
        this.numberOfVariables = covarianceMatrix.length;
        covMat = new Covariance[numberOfVariables][numberOfVariables];
        this.variables = new ArrayList<VariableAttributes>();
        VariableAttributes variableAttributes = null;
        Covariance cov = null;
        for(int i=0;i<numberOfVariables;i++){
            variableAttributes = new VariableAttributes("V"+(i+1), "", ItemType.NOT_ITEM, DataType.DOUBLE, i, "");
            this.variables.add(variableAttributes);
            for(int j=0;j<numberOfVariables;j++){
                cov = new Covariance(covarianceMatrix[i][j]);
                covMat[i][j] = cov;
            }
        }
    }

    public CovarianceMatrix(ArrayList<VariableAttributes> variables){
        this.variables = variables;
		this.numberOfVariables=variables.size();
		covMat = new Covariance[numberOfVariables][numberOfVariables];
	}

    public CovarianceMatrix(LinkedHashMap<VariableName, VariableAttributes> variableAttributeMap){
        this.variables = new ArrayList<VariableAttributes>();
        for(VariableName v : variableAttributeMap.keySet()){
            this.variables.add(variableAttributeMap.get(v));
        }

		this.numberOfVariables=variables.size();
		covMat = new Covariance[numberOfVariables][numberOfVariables];
	}

    /**
     * This constructor is primarily used in the TestSummary.java class.
     *
     * @param numberOfVariables
     */
    public CovarianceMatrix(int numberOfVariables){
        this.numberOfVariables=numberOfVariables;
		covMat = new Covariance[numberOfVariables][numberOfVariables];
        this.variables = new ArrayList<VariableAttributes>();
        VariableAttributes variableAttributes = null;
        for(int i=0;i<numberOfVariables;i++){
            variableAttributes = new VariableAttributes("V"+(i+1), "", ItemType.NOT_ITEM, DataType.DOUBLE, i, "");
            this.variables.add(variableAttributes);
        }
    }

    public CovarianceMatrix(Covariance[][] covMat, ArrayList<VariableAttributes> variables){
        this.covMat = covMat;
        this.variables = variables;
        this.numberOfVariables = variables.size();
    }

    public void setNameAt(int index, VariableName name){
        variables.get(index).setName(name);
    }

    public void setNameAt(int index, String name){
        variables.get(index).setName(new VariableName(name));
    }

    public double getMaxSampleSize(){
        double maxSampleSize = 0;
        Covariance covariance = null;
        for(int i=0;i<covMat.length;i++){
            for(int j=i;j<covMat[0].length;j++){
                covariance = covMat[i][j];
                if(covariance!=null) maxSampleSize = Math.max(maxSampleSize, covariance.sampleSize());
            }
        }
        return maxSampleSize;
    }

    /**
     * Increment values in the matrix.
     * Only increments for the upper diagonal of the matrix.
     *
     * @param xIndex
     * @param yIndex
     * @param x
     * @param y
     */
	public void increment(int xIndex, int yIndex, Double x, Double y){
        if(yIndex>=xIndex){
            if(covMat[xIndex][yIndex]==null) covMat[xIndex][yIndex] = new Covariance();
            covMat[xIndex][yIndex].increment(x, y);
        }
	}
	
	public double[][] value(boolean unbiased){
		double[][] cov = new double[numberOfVariables][numberOfVariables];
		for(int i=0;i<numberOfVariables;i++){
			for(int j=i;j<numberOfVariables;j++){
                cov[i][j]=covMat[i][j].value(unbiased);
                if(i!=j){
                    cov[j][i]=covMat[i][j].value(unbiased);
                }
			}
		}
		return cov;
	}
	
	public double[][] correlation(boolean unbiased){
		double[][] cor = new double[numberOfVariables][numberOfVariables];
		for(int i=0;i<numberOfVariables;i++){
			for(int j=i;j<numberOfVariables;j++){
				cor[i][j]=covMat[i][j].correlation(unbiased);
                if(i!=j){
                    cor[j][i]=covMat[i][j].correlation(unbiased);
                }
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

    public double getCovarianceAt(int i, int j, boolean unbiased){
        if(j>=i) return covMat[i][j].value(unbiased);
        return covMat[j][i].value(unbiased);
        
    }

    public double getCorrelationAt(int i, int j, boolean unbiased){
        if(j<i)return covMat[j][i].correlation(unbiased);
        return covMat[i][j].correlation(unbiased);
    }
	
	public double totalVariance(boolean unbiased){
		double sum=0;
		for(int i=0;i<numberOfVariables;i++){
			for(int j=0;j<numberOfVariables;j++){
                if(j<i){
                    sum+=covMat[j][i].value(unbiased);
                }else{
                    sum+=covMat[i][j].value(unbiased);
                }
			}
		}
		return sum;
	}
	
	public double diagonalSum(boolean unbiased){
		double sum=0;
		for(int i=0;i<numberOfVariables;i++){
            sum+=covMat[i][i].value(unbiased);
		}
		return sum;
	}
	
	public double covarianceSum(boolean unbiased){
		double sum=0;
		for(int i=0;i<numberOfVariables;i++){
			for(int j=i;j<numberOfVariables;j++){
				if(i!=j){
                    sum+=covMat[i][j].value(unbiased)*2.0;
				}
			}
		}
		return sum;
	}
	
	public double getVarianceAt(int index, boolean unbiased){
        return covMat[index][index].value(unbiased);
	}
	
	public double rowSum(int rowIndex, boolean unbiased){
		double sum=0.0;
		for(int j=0;j<numberOfVariables;j++){
            if(j<rowIndex){
                sum+=covMat[j][rowIndex].value(unbiased);
            }else{
                sum+=covMat[rowIndex][j].value(unbiased);
            }
		}
		return sum;
	}
	
	public double columnSum(int columnIndex, boolean unbiased){
		double sum=0.0;
		for(int i=0;i<numberOfVariables;i++){
            if(columnIndex<i){
                sum+=covMat[columnIndex][i].value(unbiased);
            }else{
                sum+=covMat[i][columnIndex].value(unbiased);
            }
		}
		return sum;
	}

    /**
     * returns an upper diagonal matrix that excludes variable at
     * variableIndex from current matrix
     *
     * @param variableIndex
     * @param unbiased
     * @return
     */
	public CovarianceMatrix matrixSansVariable(int variableIndex, boolean unbiased){
		int n=this.numberOfVariables;
		Covariance[][] newCm  = new Covariance[n-1][n-1];
		int rowIndex=0, colIndex=0;
        ArrayList<VariableAttributes> v = new ArrayList<VariableAttributes>();

		for(int i=0;i<n;i++){
			if(i!=variableIndex){
                v.add(variables.get(i));
				colIndex=rowIndex;
				for(int j=i;j<n;j++){
					if(j!=variableIndex){
                        newCm[rowIndex][colIndex]=new Covariance(covMat[i][j]);
                        colIndex++;
					}
				}
				rowIndex++;
			}
		}
        CovarianceMatrix cvm = new CovarianceMatrix(newCm, v);
		return cvm;
	}
	
	
	public int getNumberOfVariables(){
		return covMat.length;
	}
	
	/**
	 * 
	 * @return double indicating sample size. This method is only valid for
	 * listwise deletion. Sample sizes vary for each pair with pairwise 
	 * deletion.
	 */
	public double listwiseSampleSize(){
		return covMat[0][0].sampleSize();
	}

    @Override
	public String toString(){
		return printCovarianceMatrix(true);
	}

    public String printCorrelationMatrix(boolean unbiased, boolean showStdError){
        return printMatrix("CORRELATION MATRIX", unbiased, true, showStdError);
    }

    public String printCovarianceMatrix(boolean unbiased){
        return printMatrix("COVARIANCE MATRIX", unbiased, false, false);
    }

    private String printMatrix(String title, boolean unbiased, boolean correlation, boolean showStdError){
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
            table.getRowAt(2).addHeader(i+1, 1, variables.get(i).getName().toString(), TextTablePosition.RIGHT);
        }
        table.getRowAt(3).addHorizontalRule(0, numberOfVariables+1, "-");

        int row = 0;
        for(int i=0;i<numberOfVariables;i++){
            row = 4+i*(int)exrtaRowFactor;
            table.getRowAt(row).addStringAt(0, variables.get(i).getName().toString());
            for(int j=0;j<numberOfVariables;j++){
                if(correlation){
                    table.getRowAt(row).addDoubleAt(j+1, this.getCorrelationAt(i, j, unbiased));
                    if(showStdError){
                        table.getRowAt(row+1).addDoubleAt(j+1, this.getStandardErrorAt(i, j));
                        table.getRowAt(row+2).addDoubleAt(j+1, this.getPvalueAt(i, j));
                    }
                }else{
                    table.getRowAt(row).addDoubleAt(j+1, this.getCovarianceAt(i, j, unbiased));
                }
                
            }
        }
        table.getRowAt(nrows-1).addHorizontalRule(0, numberOfVariables+1, "=");

        return table.toString();
    }

}

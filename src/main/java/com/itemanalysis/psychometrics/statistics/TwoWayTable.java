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
import org.apache.commons.math3.stat.Frequency;

import java.util.Iterator;
import java.util.TreeMap;


public class TwoWayTable {
	
	private TreeMap<Comparable<?>, Frequency> tableRows; //holds the FrequencyTables for each row
	private Frequency colMargin, rowMargin;
    private VariableAttributes rowVariable = null;
    private VariableAttributes colVariable = null;
	private double totalCount=0;
	
	public TwoWayTable(){
		tableRows=new TreeMap<Comparable<?>, Frequency>();
		colMargin=new Frequency();
		rowMargin=new Frequency();
	}

    public void setRowVariable(VariableAttributes rowVariable){
        this.rowVariable = rowVariable;
    }

    public void setColVariable(VariableAttributes colVariable){
        this.colVariable = colVariable;
    }
	
	public String getRowVariableNameForDatabase(){
		return rowVariable.getName().nameForDatabase();
	}
	
	public String getColVariableNameForDatabase(){
		return colVariable.getName().nameForDatabase();
	}
	
	public void addValue(Comparable<?> rowValue, Comparable<?> colValue){
		Frequency ft=tableRows.get(rowValue);
		if(ft==null){
			ft=new Frequency();
			ft.addValue(colValue);
			tableRows.put(rowValue,ft);
		}else{
			ft.addValue(colValue);
		}
		rowMargin.addValue(rowValue); //addValue row margins
		colMargin.addValue(colValue); //addValue column margins
		totalCount++;
	}

    public void addValue(int rowValue, int colValue){
        addValue(Long.valueOf(rowValue), Long.valueOf(colValue));
    }

    public void addValue(Integer rowValue, Integer colValue){
        addValue(Long.valueOf(rowValue.longValue()), Long.valueOf(colValue.longValue()));
    }

    public void addValue(long rowValue, long colValue){
        addValue(Long.valueOf(rowValue), Long.valueOf(colValue));
    }

    public void addValue(char rowValue, char colValue){
        addValue(Character.valueOf(rowValue), Character.valueOf(colValue));
    }

    public void clear(){
        rowMargin.clear();
        colMargin.clear();
        for(Comparable<?> c : tableRows.keySet()){
            tableRows.get(c).clear();
        }
        totalCount=0;
    }

    public long getCount(Comparable<?> rowValue, Comparable<?> colValue){
        if(rowValue instanceof Integer && colValue instanceof Integer){
            return getCount(((Integer) rowValue).longValue(), ((Integer) rowValue).longValue());
        }
        long result = 0;
        try{
            Frequency freq = tableRows.get(rowValue);
            if(freq!=null){
                result = freq.getCount(colValue);
            }
        }catch(ClassCastException ex){
            //ignore and return 0 -- ClassCastException will be thrown if rho is not comparable
        }
        return result;
    }

    public long getCount(int rowValue, int colValue){
        return getCount(Long.valueOf(rowValue), Long.valueOf(colValue));
    }

    public long getCount(long rowValue, long colValue){
        return getCount(Long.valueOf(rowValue), Long.valueOf(colValue));
    }

    public long getCount(char rowValue, char colValue){
        return getCount(Character.valueOf(rowValue), Character.valueOf(colValue));
    }

    public double getPct(Comparable<?> rowValue, Comparable<?> colValue){
        if(totalCount == 0){
            return Double.NaN;
        }
        return (double)getCount(rowValue, colValue)/(double)totalCount;
    }

    public double getPct(int rowValue, int colValue){
        return getPct(Long.valueOf(rowValue), Long.valueOf(colValue));
    }

    public double getPct(long rowValue, long colValue){
        return getPct(Long.valueOf(rowValue), Long.valueOf(colValue));
    }

    public double getPct(char rowValue, char colValue){
        return getPct(Character.valueOf(rowValue), Character.valueOf(colValue));
    }

    public long getRowCount(Comparable<?> rowValue){
        if(rowValue instanceof Integer){
            return rowMargin.getCount(((Integer) rowValue).longValue());
        }
        long result = 0;
        try{
            result = rowMargin.getCount(rowValue);
        }catch(ClassCastException ex){
            //ignore and return 0 -- ClassCastException will be thrown if rho is not comparable
        }
        return result;
    }

    public long getRowCount(int rowValue){
        return getRowCount(Long.valueOf(rowValue));
    }

    public long getRowCount(long rowValue){
        return getRowCount(Long.valueOf(rowValue));
    }

    public long getRowCount(char rowValue){
        return getRowCount(Character.valueOf(rowValue));
    }

    public double getRowPct(Comparable<?> rowValue){
        long rowTotal = rowMargin.getSumFreq();
        if(rowTotal == 0){
            return Double.NaN;
        }
        return (double)getRowCount(rowValue)/(double)rowTotal;
    }

    public double getRowPct(int rowValue){
        return getRowPct(Long.valueOf(rowValue));
    }

    public double getRowPct(long rowValue){
        return getRowPct(Long.valueOf(rowValue));
    }

    public double getRowPct(char rowValue){
        return getRowPct(Character.valueOf(rowValue));
    }

    public long getColCount(Comparable<?> colValue){
        if(colValue instanceof Integer){
            return colMargin.getCount(((Integer) colValue).longValue());
        }
        long result = 0;
        try{
            result = colMargin.getCount(colValue);
        }catch(ClassCastException ex){
            //ignore and return 0 -- ClassCastException will be thrown if rho is not comparable
        }
        return result;
    }

    public long getColCount(int colValue){
        return getColCount(Long.valueOf(colValue));
    }

    public long getColCount(long colValue){
        return getColCount(Long.valueOf(colValue));
    }

    public long getColCount(char colValue){
        return getColCount(Character.valueOf(colValue));
    }

    public double getColPct(Comparable<?> colValue){
        long colTotal = colMargin.getSumFreq();
        if(colTotal == 0){
            return Double.NaN;
        }
        return (double)getColCount(colValue)/(double)colTotal;
    }

    public double getColPct(int colValue){
        return getColPct(Long.valueOf(colValue));
    }

    public double getColPct(long colValue){
        return getColPct(Long.valueOf(colValue));
    }

    public double getColPct(char colValue){
        return getColPct(Character.valueOf(colValue));
    }

    public long getRowCumFreq(Comparable<?> rowValue){
        if(rowMargin.getSumFreq()==0){
            return 0;
        }
        if(rowValue instanceof Integer){
            return rowMargin.getCumFreq((((Integer)rowValue).longValue()));
        }
        return rowMargin.getCumFreq(rowValue);
    }

    public long getRowCumFreq(int rowValue){
        return getRowCumFreq(Long.valueOf(rowValue));
    }

    public long getRowCumFreq(long rowValue){
        return getRowCumFreq(Long.valueOf(rowValue));
    }

    public long getRowCumFreq(char rowValue){
        return getRowCumFreq(Character.valueOf(rowValue));
    }

    public double getRowCumPct(Comparable<?> rowValue){
        final long sumFreq = rowMargin.getSumFreq();
        if(sumFreq==0){
            return Double.NaN;
        }
        return (double)rowMargin.getCumFreq(rowValue)/(double)sumFreq;
    }

    public double getRowCumPct(int rowValue){
        return getRowCumPct(Long.valueOf(rowValue));
    }

    public double getRowCumPct(long rowValue){
        return getRowCumPct(Long.valueOf(rowValue));
    }

    public double getRowCumPct(char rowValue){
        return getRowCumPct(Character.valueOf(rowValue));
    }

    public long getColCumFreq(Comparable<?> colValue){
        if(colMargin.getSumFreq()==0){
            return 0;
        }
        if(colValue instanceof Integer){
            return colMargin.getCumFreq((((Integer)colValue).longValue()));
        }
        return colMargin.getCumFreq(colValue);
    }

    public long getColCumFreq(int colValue){
        return getColCumFreq(Long.valueOf(colValue));
    }

    public long getColCumFreq(long colValue){
        return getColCumFreq(Long.valueOf(colValue));
    }

    public long getColCumFreq(char colValue){
        return getColCumFreq(Character.valueOf(colValue));
    }

    public double getColCumPct(Comparable<?> colValue){
        final long sumFreq = colMargin.getSumFreq();
        if(sumFreq==0){
            return Double.NaN;
        }
        return (double)colMargin.getCumFreq(colValue)/(double)sumFreq;
    }

    public double getColCumPct(int colValue){
        return getColCumPct(Long.valueOf(colValue));
    }

    public double getColCumPct(long colValue){
        return getColCumPct(Long.valueOf(colValue));
    }

    public double getColCumPct(char colValue){
        return getColCumPct(Character.valueOf(colValue));
    }

	public int getRowUniqueCount(){
		return rowMargin.getUniqueCount();
	}
	
	public int getColUniqueCount(){
		return colMargin.getUniqueCount();
	}

    public int getColUniqueCountAtRow(Comparable<?> rowValue){
		Frequency f = tableRows.get(rowValue);
        return f.getUniqueCount();
	}

    public double[][] getTable(){
        int nrow = rowMargin.getUniqueCount();
        int ncol = colMargin.getUniqueCount();
        double[][] table = new double[nrow][ncol];
        Iterator<Comparable<?>> rows = rowMargin.valuesIterator();
        Iterator<Comparable<?>> cols = null;
        Comparable<?> rTemp = null;
        int i=0;
        int j=0;

        while(rows.hasNext()){
            cols = colMargin.valuesIterator();
            rTemp = rows.next();
            j=0;
            while(cols.hasNext()){
                table[i][j] = (double)getCount(rTemp, cols.next());
                j++;
            }
            i++;
        }
        return table;
    }

	
//	public Frequency getRowTable(Object rowValue){
//		Frequency table = tableRows.get(rowValue);
//		if(table==null) return new Frequency();
//		return table;
//	}

    public Iterator<Comparable<?>> rowValuesIterator(){
        return rowMargin.valuesIterator();
    }

    public Iterator<Comparable<?>> colValuesIterator(){
        return colMargin.valuesIterator();
    }

//	public Object[] getRowValuesFor(Object rowValue){
//		Frequency row = tableRows.get(rowValue);
//		if(row==null) return null;
//		return row.getValues();
//	}
	
	public double getFreqSum(){
		return totalCount;
	}
	
//	public Frequency getRowMarginTable(){
//		return rowMargin;
//	}
	
//	public long getRowMarginCount(Comparable<?> rowValue){
//		return rowMargin.getCount(rowValue);
//	}
	
//	public Frequency getColumnMarginTable(){
//		return colMargin;
//	}
	
//	public long getColumnMarginCount(Comparable<?> colValue){
//		return colMargin.getCount(colValue);
//	}
	
	/**
	 * Return object array of marginal row values.
	 * 
	 * @return object array of marginal row values
	 */
//	public Object[] getRowMarginalValues(){
//		Object[] rowKeys = rowMargin.getValues();
//		return rowKeys;
//	}
	
	/**
	 * 
	 * @param rowValue row index
	 * @return addValue for row margin
	 */
//	public long getRowMarginalValueFor(Comparable<?> rowValue){
//		return rowMargin.getCount(rowValue);
//	}
	
	/**
	 * 
	 * @return object array of column marginal values
	 */
//	public Object[] getColumnMarginalValues(){
//		Object[] ckeys=colMargin.getValues();
//		return ckeys;
//	}
	
	/**
	 *
	 * @return addValue for column margin
	 */
//	public long getColumnMarginalValueFor(Comparable<?> colValue){
//		return colMargin.getCount(colValue);
//	}
	
	public int getSize(){
		return tableRows.size();
	}

}

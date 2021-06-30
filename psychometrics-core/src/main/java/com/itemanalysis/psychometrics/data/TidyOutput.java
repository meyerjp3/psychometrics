package com.itemanalysis.psychometrics.data;

import java.sql.Timestamp;
import java.util.*;

/**
 * Holds data as String format for outputting in Tidy format for CSV files.
 * Add data to a row of tidy output with addValue(). Multiple values may be added per row.
 * To move to the next row of output, call nextRow().
 */
public class TidyOutput {

    private LinkedHashSet<String> columnNames = null;

    private LinkedHashMap<Integer, HashMap<String, String>> output = null;

    private Date date = null;

    private int currentRow = 0;

    private int maxRow = 0;

    public TidyOutput(){
        columnNames = new LinkedHashSet<String>();
        output = new LinkedHashMap<Integer, HashMap<String, String>>();
        date = new Date();
    }


    /**
     * Constructor with initial list of column names. Additional names may be added later with calls
     * to addValue()
     *
     * @param columnNames an array of unique column names.
     */
    public TidyOutput(String[] columnNames){
        this.columnNames = new LinkedHashSet<String>();
        this.output = new LinkedHashMap<Integer, HashMap<String, String>>();
        date = new Date();

        for(String s : columnNames){
            this.columnNames.add(s);
        }
    }

//    /**
//     * Adds an element to a specific row of data.
//     *
//     * @param rowNumber row number. Should start at zero and be ordered integers.
//     * @param columnName name of output column.
//     * @param value value for the row.
//     */
//    public void addValue(int rowNumber, String columnName, String value){
//        HashMap<String, String> rowValues = output.get(rowNumber);
//        if(rowValues==null){
//            rowValues = new HashMap<String, String>();
//            output.put(rowNumber, rowValues);
//        }
//        rowValues.put(columnName, value);
//        columnNames.add(columnName);
//        maxRow = Math.max(maxRow, rowNumber);
//    }

    /**
     * Adds an element to the current row of data.
     *
     * @param columnName name of output column.
     * @param value value for the row.
     */
    public void addValue(String columnName, String value){
        HashMap<String, String> rowValues = output.get(currentRow);
        if(rowValues==null){
            rowValues = new HashMap<String, String>();
            output.put(currentRow, rowValues);
        }
        rowValues.put(columnName, value);
        columnNames.add(columnName);
        maxRow = Math.max(maxRow, currentRow);
    }

    public boolean addTidyOutput(TidyOutput tidyOutput){

        //check that output has at least one column in common
        HashSet<String> thatColumnNames = tidyOutput.getColumnNames();
        boolean noMatchFound = true;
        for(String s : this.columnNames){
            if(thatColumnNames.contains(s)){
                noMatchFound = false;
                continue;
            }
        }

        if(noMatchFound) return false;

        //append that tidy output to this one.
        Iterator<Integer> iter = tidyOutput.getRowIterator();
        HashMap<String, String> rowMap = null;

        while(iter.hasNext()){
            rowMap = tidyOutput.getOutputRowMap(iter.next());
            for(String s : rowMap.keySet()){
                this.addValue(s, rowMap.get(s));
            }
        }

        return true;
    }

    /**
     * Call this method to move to the next row.
     */
    public void nextRow(){
        currentRow++;
    }

//    public void addRow(String name, String part, String partId, String method, String groupVariable, String groupId, String statistic, String value){
//        String[] row = new String[numberOfColumns];
//
//        row[0] = name;
//        row[1] = part;
//        row[2] = partId;
//        row[3] = method;
//        row[4] = groupVariable;
//        row[4] = groupId;
//        row[5] = statistic;
//        row[6] = value;
//
//        output.add(row);
//
//    }

    public HashSet<String> getColumnNames(){
        return columnNames;
    }

    public HashMap<String, String> getOutputRowMap(int rowNumber){
        return output.get(rowNumber);
    }

    public int getNumberOfColumns(){
        return columnNames.size();
    }

    public int getCurrentRow(){
        return currentRow;
    }

    public int getNumberOfRows(){
        return output.size();
    }

    public Iterator<Integer> getRowIterator(){
        return output.keySet().iterator();
    }

    public String[] getTidyRow(int rowNumber){
        String[] row = new String[columnNames.size()];
        int index = 0;
        String temp = "";
        HashMap<String, String> tempRow;

        for(String s : columnNames){
            tempRow = output.get(rowNumber);
            if(tempRow==null){
                row[index] = "";
            }else{
                temp = tempRow.get(s);

                if(temp==null){
                    row[index] = "";
                }else{
                    row[index] = temp;
                }

            }
            index++;
        }

        return row;
    }

    /**
     * An array of values that represent the header of a csv file.
     *
     * @return a string array with header names
     */
    public String[] getHeader(){
        String[] header = new String[columnNames.size()];
        int index = 0;

        for(String s : columnNames){
            header[index] = s;
            index++;
        }

        return header;
    }

    public String getTimeStamp(){
        Timestamp timeStamp = new Timestamp(date.getTime());
        return timeStamp.toString();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        HashMap<String, String> row;
        String value = "";
        ArrayList<String> rowValues;

        for(Integer i : output.keySet()){
            row = output.get(i);
            rowValues = new ArrayList();

            for(String s : columnNames){
                value = row.get(s);
                if(value==null) value = "";
                rowValues.add(value);
            }

            sb.append(String.join(",", rowValues));
            sb.append("\n");
        }

        return sb.toString();
    }

}

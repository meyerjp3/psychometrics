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
package com.itemanalysis.psychometrics.texttable;

import java.util.ArrayList;

public class TextTable {
    
    private ArrayList<TextTableRow> rows = null;

    public TextTable(){
        rows = new ArrayList<TextTableRow>();
    }

    public void addAllColumnFormats(TextTableColumnFormat[] columnFormats, int numberOfRows){
        for(int i=0;i<numberOfRows;i++){
            TextTableRow r = new TextTableRow(columnFormats.length);
            r.addAllColumnFormats(columnFormats);
            rows.add(r);
        }
    }
    
    public void setAllCellPadding(int padding){
        for(TextTableRow r : rows){
            r.setCellPadding(padding);
        }
    }

    public void addRow(TextTableRow row){
        rows.add(row);
    }

    public void addRow(TextTableRow row, TextTableColumnFormat[] columnFormats){
        row.addAllColumnFormats(columnFormats);
        rows.add(row);
    }

    public void addRowAt(int index, TextTableRow row){
        rows.add(index, row);
    }

    public TextTableRow getRowAt(int index){
        return rows.get(index);
    }

    public void addStringAt(int r, int c, String text){
        if(r<rows.size() && c < rows.get(r).getnumberOfColumns()){
            rows.get(r).addStringAt(c, text);
        }
    }

    public void addIntAt(int r, int c, int number){
        if(r<rows.size() && c < rows.get(r).getnumberOfColumns()){
            rows.get(r).addIntAt(c, number);
        }
    }

    public void addLongAt(int r, int c, long number){
        if(r<rows.size() && c < rows.get(r).getnumberOfColumns()){
            rows.get(r).addLongAt(c, number);
        }
    }

    public void addDoubleAt(int r, int c, double number){
        if(r<rows.size() && c < rows.get(r).getnumberOfColumns()){
            rows.get(r).addDoubleAt(c, number);
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(TextTableRow r : rows){
            sb.append(r.toString());
            sb.append("\n");
        }
//        for(int i=0;i<rows.size();i++){
//            if(rows[i]==null){
//                sb.append("");
//            }else{
//                sb.append(rows[i].toString());
//            }
//            sb.append("\n");
//        }
        return sb.toString();
    }



}

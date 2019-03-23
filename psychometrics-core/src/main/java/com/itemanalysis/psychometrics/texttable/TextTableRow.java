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

import org.apache.commons.math3.util.Precision;

public class TextTableRow {

    private TextTableColumnFormat[] colFormats = null;

    private int[] colSizes = null;

    private String[] cellText = null;

    private int numberOfColumns = 0;

    private int currentPosition = 0;

    private int columnPadding = 5;

    public TextTableRow(int numberOfColumns){
        this.numberOfColumns = numberOfColumns;
        colFormats = new TextTableColumnFormat[numberOfColumns];
        colSizes = new int[numberOfColumns];
        cellText = new String[numberOfColumns];
    }

    public void addNextColumnFormat(TextTableColumnFormat columnFormat){
        colFormats[currentPosition] = columnFormat;
        colSizes[currentPosition] = columnFormat.getSize();
        currentPosition++;
    }

    public void addColumnFormatAt(int columnPosition, TextTableColumnFormat columnFormat){
        if(columnPosition<colFormats.length){
            colFormats[columnPosition] = columnFormat;
            colSizes[columnPosition] = columnFormat.getSize();
        }else{
            addNextColumnFormat(columnFormat);
        }
    }

    public void addAllColumnFormats(TextTableColumnFormat[] colFormats){
        for(int i=0;i<colFormats.length;i++){
            addNextColumnFormat(colFormats[i]);
        }
    }

    public int getRowSize(){
        int size = 0;
        for(int i=0;i<colFormats.length;i++){
            size+=colSizes[i];
        }
        return size;
    }

    public int getnumberOfColumns(){
        return numberOfColumns;
    }

    public void setCellPadding(int cellPadding){
        this.columnPadding = cellPadding;
    }

    public void addStringAt(int index, String text){
        if(index<numberOfColumns){
            String s = String.format(colFormats[index].getFormat(), text);
            if(columnPadding>0) s += String.format("%" + columnPadding + "s", "");
            cellText[index] = s;
        }
    }

    public void addIntAt(int index, int number){
        if(index<numberOfColumns){
            String s = String.format(colFormats[index].getFormat(), number);
            if(columnPadding>0) s += String.format("%" + columnPadding + "s", "");
            cellText[index] = s;
        }
    }

    public void addLongAt(int index, long number){
        if(index<numberOfColumns){
            String s = String.format(colFormats[index].getFormat(), number);
            if(columnPadding>0) s += String.format("%" + columnPadding + "s", "");
            cellText[index] = s;
        }
    }

    public void addDoubleAt(int index, double number){
        if(index<numberOfColumns){
            String s = String.format(colFormats[index].getFormat(), Precision.round(number, colFormats[index].getPrecision()));
            if(columnPadding>0) s += String.format("%" + columnPadding + "s", "");
            cellText[index] = s;
        }
    }

    public void addHorizontalRule(int startCol, int colSpan, String text){
        colSpan = Math.max(1, colSpan);
        String t = text.substring(0,1);
        String f = "";
        int end = startCol+colSpan;
        if(end>numberOfColumns) end = numberOfColumns;
        int csize = 0;

        for(int i=0;i<numberOfColumns;i++){
            if((i<startCol || i>=end) && cellText[i]==null){
                f = "";
                csize = colFormats[i].getSize();
                for(int j=0;j<csize;j++) f +=  " ";
                if(columnPadding>0 && i<(numberOfColumns-1)){
                    for(int j=0;j<columnPadding;j++) f +=  " ";
                }
                cellText[i] = f;
            }else if(i>=startCol && i<end){
                f = "";
                csize = colFormats[i].getSize();
                for(int j=0;j<csize;j++) f +=  t;
                if(columnPadding>0 && i<(numberOfColumns-1)){
                    for(int j=0;j<columnPadding;j++) f +=  t;
                }
                cellText[i] = f;
            }
        }
    }

    public void addHeader(int startCol, int colSpan, String text, TextTablePosition position){
        colSpan = Math.max(1, colSpan);
        String newText = "";
        int start = 0;
        int end = 0;
        int offset = 0;
        int width = 0;
        int lastIndex = startCol+colSpan;
        int lastIndexM1 = lastIndex-1;

        //FIXME does not adjust for columns used for padding
        if(lastIndex >  numberOfColumns) lastIndex = numberOfColumns;
        for(int i=startCol;i<lastIndex;i++){
            width += colFormats[i].getSize();
            if(colSpan>1 && i<lastIndexM1){
                width += columnPadding;
            }
        }

        //fit text to available space
        //center text if available space is greater than tLength
        if(position== TextTablePosition.CENTER){
            newText = this.centerText(text, width);
        }else if(position== TextTablePosition.LEFT){
            newText = this.leftJustifyText(text, width);
        }else{
            newText = this.rightJustifyText(text, width);
        }

        //divide text over columns
        //add padding if column span==1
        for(int i=startCol;i<lastIndex;i++){
            if(colSpan==1){
                offset = colFormats[i].getSize();
                end = start+offset;
                cellText[i] = newText.substring(start, end);
                if(startCol>0){
                    cellText[i] = String.format("%"+columnPadding+"s", "")+cellText[i];
                }
            }else if(colSpan>1 && i<lastIndexM1){
                offset = colFormats[i].getSize()+columnPadding;
                end = start+offset;
                cellText[i] = newText.substring(start, end);
            }
            start += offset;
        }
    }

    public String centerText(String text, int width){
        text = text != null ? text.trim() : "";
        int difference = width - text.length();
        StringBuilder sb = new StringBuilder();

        //too big, truncate
        if(difference==0){
            return text;
        }else if(difference<0){
            return text.substring(0, width);
        }

        int prependSize = difference/2;
        int appendSize = prependSize;
        if((prependSize+appendSize)!=difference){
            prependSize++;
        }

        if(prependSize>0) sb.append(String.format("%"+prependSize+"s", ""));
        sb.append(text);
        if(appendSize>0) sb.append(String.format("%"+appendSize+"s", ""));
        return sb.toString();
    }

    public String leftJustifyText(String text, int width){
        text = text != null ? text.trim() : "";
        int difference = width - text.length();
        StringBuilder sb = new StringBuilder();

        //too big, truncate
        if(difference==0){
            return text;
        }else if(difference<0){
            return text.substring(0, width);
        }

        sb.append(String.format("%-"+width+"s", text));
        return sb.toString();
    }

    public String rightJustifyText(String text, int width){
        text = text != null ? text.trim() : "";
        int difference = width - text.length();
        StringBuilder sb = new StringBuilder();

        //too big, truncate
        if(difference==0){
            return text;
        }else if(difference<0){
            return text.substring(0, width);
        }
        
        sb.append(String.format("%"+width+"s", text));
        return sb.toString();
    }

    @Override
    public String toString(){
        String row = "";
        for(int i=0;i<numberOfColumns;i++){
            if(cellText[i]==null || cellText[i].equalsIgnoreCase("")){
                row += String.format("%"+colFormats[i].getSize()+"s", " ");
                if(columnPadding>0) row += String.format("%"+columnPadding+"s", "");
            }else{
                row += cellText[i];
            }
        }
        return row;
    }

}

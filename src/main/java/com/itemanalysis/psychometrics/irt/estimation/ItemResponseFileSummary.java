/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.irt.estimation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.Frequency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class ItemResponseFileSummary {

    public ItemResponseFileSummary(){

    }

    public ItemResponseVector[] getResponseVectors(String fileName, boolean headerIncluded){
        File f = new File(fileName);
        return getResponseVectors(f, headerIncluded);
    }

    /**
     * Reads a comma delimited file.
     *
     * @param f file to be summarized
     * @param headerIncluded true if header included. False otherwise. The header will be omitted (ignored).
     * @return
     */
    public ItemResponseVector[] getResponseVectors(File f, boolean headerIncluded){
        Frequency freq = new Frequency();
        String responseString = "";

        try{
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            if(headerIncluded) br.readLine();//skip header
            while((line=br.readLine())!=null){
                line = line.replaceAll(",", "");
                freq.addValue(line);
            }
            br.close();

        }catch(IOException ex){
            ex.printStackTrace();
        }

        ItemResponseVector[] responseData = new ItemResponseVector[freq.getUniqueCount()];
        ItemResponseVector irv = null;
        Iterator<Comparable<?>> iter = freq.valuesIterator();
        int index = 0;
        byte[] rv = null;

        //create array of ItemResponseVector objects
        while(iter.hasNext()){
            Comparable<?> value = iter.next();
            responseString = value.toString();

            int n=responseString.length();
            rv = new byte[n];

            String response = "";
            for (int i = 0;i < n; i++){
                response = String.valueOf(responseString.charAt(i)).toString();
                rv[i] = Byte.parseByte(response);
            }

            //create response vector objects
            irv = new ItemResponseVector(rv, Long.valueOf(freq.getCount(value)).doubleValue());
            responseData[index] = irv;
            index++;
        }

        return responseData;

    }

    /**
     * Summarize comma delimited file. It will extract the data beginning in the column indicated by start
     * and it will continue for nItems columns.
     *
     * @param f file to summarize
     * @param start the column index of the first item. It is zero based. If teh data start in the first column, then start=0.
     * @param nItems number of items to read from the file. It will begin at the column indicated by start.
     * @param headerIncluded true if header is included. False otherwise. The header will be omitted.
     * @return an array of item resposne vectors
     */
    public ItemResponseVector[] getResponseVectors(File f, int start, int nItems, boolean headerIncluded){
        Frequency freq = new Frequency();
        String responseString = "";

        try{
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            if(headerIncluded) br.readLine();//skip header
            while((line=br.readLine())!=null){
                s = line.split(",");
                line = "";

                for(int j=start;j<start+nItems;j++){
                    line += s[j];
                }
                freq.addValue(line);
            }
            br.close();

        }catch(IOException ex){
            ex.printStackTrace();
        }

        ItemResponseVector[] responseData = new ItemResponseVector[freq.getUniqueCount()];
        ItemResponseVector irv = null;
        Iterator<Comparable<?>> iter = freq.valuesIterator();
        int index = 0;
        byte[] rv = null;

        //create array of ItemResponseVector objects
        while(iter.hasNext()){
            Comparable<?> value = iter.next();
            responseString = value.toString();

            int n=responseString.length();
            rv = new byte[n];

            String response = "";
            for (int i = 0;i < n; i++){
                response = String.valueOf(responseString.charAt(i)).toString();
                rv[i] = Byte.parseByte(response);
            }

            //create response vector objects
            irv = new ItemResponseVector(rv, Long.valueOf(freq.getCount(value)).doubleValue());
            responseData[index] = irv;
            index++;
        }
        return responseData;

    }


    private ItemResponseVector[] readTapData(){
        byte[][] tap = new byte[35][18];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/tap-data.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    tap[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();

        }catch(IOException ex){
            ex.printStackTrace();
        }

        Frequency freq = new Frequency();
        for(int i=0;i<tap.length;i++){
            freq.addValue(Arrays.toString(tap[i]));
        }

        ItemResponseVector[] responseData = new ItemResponseVector[freq.getUniqueCount()];
        ItemResponseVector irv = null;
        Iterator<Comparable<?>> iter = freq.valuesIterator();
        int index = 0;

        //create array of ItemResponseVector objects
        while(iter.hasNext()){
            //get response string from frequency summary and convert to byte array
            Comparable<?> value = iter.next();
            String s = value.toString();
            s = s.substring(1,s.lastIndexOf("]"));
            String[] sa = s.split(",");
            byte[] rv = new byte[sa.length];
            for(int i=0;i<sa.length;i++){
                rv[i] = Byte.parseByte(sa[i].trim());
            }

            //create response vector objects
            irv = new ItemResponseVector(rv, Long.valueOf(freq.getCount(value)).doubleValue());
            responseData[index] = irv;
            index++;
        }
//        //display results of summary
//        for(int i=0;i<responseData.length;i++){
//            System.out.println(responseData[i].toString() + ": " + responseData[i].getFrequency());
//        }

        return responseData;
    }

}

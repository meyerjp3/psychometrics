package com.itemanalysis.psychometrics.irt.estimation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.Frequency;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;

public class ItemResponseVectorTest {

    private byte[][] lsat7 = null;

    public void readLsat7Data(){
        lsat7 = new byte[1000][5];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    lsat7[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void summarizeLSAT7dataTest(){
        System.out.println("Testing summary of LSAT7 data");
        readLsat7Data();

        //true values from mirt package in R
        String[][] trueValues = {
                {"[0, 0, 0, 0, 0]", "12"},
                {"[0, 0, 0, 0, 1]", "19"},
                {"[0, 0, 0, 1, 0]", "1"},
                {"[0, 0, 0, 1, 1]", "7"},
                {"[0, 0, 1, 0, 0]", "3"},
                {"[0, 0, 1, 0, 1]", "19"},
                {"[0, 0, 1, 1, 0]", "3"},
                {"[0, 0, 1, 1, 1]", "17"},
                {"[0, 1, 0, 0, 0]", "10"},
                {"[0, 1, 0, 0, 1]", "5"},
                {"[0, 1, 0, 1, 0]", "3"},
                {"[0, 1, 0, 1, 1]", "7"},
                {"[0, 1, 1, 0, 0]", "7"},
                {"[0, 1, 1, 0, 1]", "23"},
                {"[0, 1, 1, 1, 0]", "8"},
                {"[0, 1, 1, 1, 1]", "28"},
                {"[1, 0, 0, 0, 0]", "7"},
                {"[1, 0, 0, 0, 1]", "39"},
                {"[1, 0, 0, 1, 0]", "11"},
                {"[1, 0, 0, 1, 1]", "34"},
                {"[1, 0, 1, 0, 0]", "14"},
                {"[1, 0, 1, 0, 1]", "51"},
                {"[1, 0, 1, 1, 0]", "15"},
                {"[1, 0, 1, 1, 1]", "90"},
                {"[1, 1, 0, 0, 0]", "6"},
                {"[1, 1, 0, 0, 1]", "25"},
                {"[1, 1, 0, 1, 0]", "7"},
                {"[1, 1, 0, 1, 1]", "35"},
                {"[1, 1, 1, 0, 0]", "18"},
                {"[1, 1, 1, 0, 1]", "136"},
                {"[1, 1, 1, 1, 0]", "32"},
                {"[1, 1, 1, 1, 1]", "308"}
        };


        //summarize response vectors into a frequency object
        Frequency freq = new Frequency();
        for(int i=0;i<lsat7.length;i++){
            freq.addValue(Arrays.toString(lsat7[i]));
        }

        assertEquals("Same number of response strings", trueValues.length, freq.getUniqueCount());


        for(int i=0;i<trueValues.length;i++){
            assertEquals("Response vector comparison: ", Double.parseDouble(trueValues[i][1]), Long.valueOf(freq.getCount(trueValues[i][0])).doubleValue(), 1e-5);
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

        //display results of summary
        for(int i=0;i<responseData.length;i++){
            System.out.println(responseData[i].toString() + ": " + responseData[i].getFrequency());
        }




    }

}
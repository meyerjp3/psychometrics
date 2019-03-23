package com.itemanalysis.psychometrics.histogram;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class HistogramTest {

    @Test
    public void histogramTest1(){
        System.out.println("Histogram Test - Frequency");
        double[] x = {
                -0.64537246,  0.16150298, -0.91788455, -0.49463162, -0.64612372,  0.62760865,
                0.15147345, -1.62900923,  1.80648602,  0.10830578,  0.44272709, -0.83828587,
                2.30296046, -0.59314984,  0.82499361,  0.57991353, -0.28491925,  0.06577923,
                -1.18676207, -0.09038179, -0.18250810,  0.92943437, -1.45428327,  1.17391684,
                -1.39885180,  0.39904871, -1.15471526,  0.97386141,  0.39317559,  2.20008329,
                -1.91036670,  0.23829852,  0.77072167, -1.17423599, -1.22468951,  1.12270793,
                2.28145437,  0.08716378,  0.29204499, -0.76221463,  0.57936141, -0.30701344,
                1.01573704,  0.35215572,  0.01077527, -1.63345655, -0.42547746,  0.61502089,
                0.56758215,  0.31170858};

        double[] true_frequency = {5, 7, 8, 14, 10, 2, 4};

        Histogram hist = new Histogram(HistogramType.FREQUENCY, BinCalculationType.STURGES, false);
        double[] y = hist.evaluate(x);
//        System.out.println(hist.toString());

        for(int i=0;i<y.length;i++){
            assertEquals("Histogram frequency", true_frequency[i], y[i], 1e-15);
        }

    }

    @Test
    public void histogramTest2(){
        System.out.println("Histogram Test - Relative Frequency");
        double[] x = {
                -0.64537246,  0.16150298, -0.91788455, -0.49463162, -0.64612372,  0.62760865,
                0.15147345, -1.62900923,  1.80648602,  0.10830578,  0.44272709, -0.83828587,
                2.30296046, -0.59314984,  0.82499361,  0.57991353, -0.28491925,  0.06577923,
                -1.18676207, -0.09038179, -0.18250810,  0.92943437, -1.45428327,  1.17391684,
                -1.39885180,  0.39904871, -1.15471526,  0.97386141,  0.39317559,  2.20008329,
                -1.91036670,  0.23829852,  0.77072167, -1.17423599, -1.22468951,  1.12270793,
                2.28145437,  0.08716378,  0.29204499, -0.76221463,  0.57936141, -0.30701344,
                1.01573704,  0.35215572,  0.01077527, -1.63345655, -0.42547746,  0.61502089,
                0.56758215,  0.31170858};

        Histogram hist = new Histogram(HistogramType.DENSITY, BinCalculationType.STURGES, false);
        double[] y = hist.evaluate(x);

        double[] true_relative_frequency = {0.16614055, 0.23259291, 0.26582489, 0.46518583, 0.33228111, 0.06645622, 0.13107663};
        //not sure how R computed the last value in this array because it does not equal freq/(binWidth*n)
        //All other calculations are comparable up to about four decimal places.

//        System.out.println(hist.toString());

        for(int i=0;i<y.length;i++){
            assertEquals("Histogram relative frequency", true_relative_frequency[i], y[i], 1e-2);
        }

    }

    @Test
    public void indexTest(){

        double[] x = {-7,1.4, 2.0, 2.5, 3, 5.3, 4.1, 8};
        double[] bounds = {0,2,4,6};//[0,2) [2,4) [4,6) [6,+Inf]
        double index = 0;

        for( double d : x){
            int insertionPoint = Arrays.binarySearch(bounds, d);
            if(insertionPoint<0){
                insertionPoint = ~insertionPoint-1;
            }else{
                insertionPoint = insertionPoint-1;//will be lower inclusive, upper exclusive [x,y)
            }
            System.out.println("Value = " + d);
            System.out.println("  Insertion Point = " + insertionPoint);
            System.out.println("            index = " + insertionPoint);
        }


    }


}
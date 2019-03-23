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
package com.itemanalysis.psychometrics.histogram;

/**
 * The Freedman-Diaconis method fro computing the number of bins. Unlike the other bin calculations, this class
 * is not storeless because it must store the array of data points to compute percentiles.
 *
 * @author J. Patrick Meyer
 */
public class FreedmanDiaconisBinCalculation implements BinCalculation{

    private double n = 0;
    private double min = 0;
    private double max = 0;
    private double q1 = 0;
    private double q3 = 0;

    /**
     * Create the Freedman-Diaconis class fro computing the number of bins.
     */
//    public FreedmanDiaconisBinCalculation(){
//        x = new ResizableDoubleArray();
//    }

    public FreedmanDiaconisBinCalculation(double n, double min, double max, double q1, double q3){
        this.n = n;
        this.min = min;
        this.max = max;
        this.q1 = q1;
        this.q3 = q3;
    }

//    /**
//     * Incrementally update summary statistics. Even though the data are stored, the incremental statistics should
//     * cut down on the memory requirements and processing time.
//     *
//     * @param value a value added to the summary statistics.
//     */
//    @Override
//    public void increment(double value){
//        x.addElement(value);
//        min.increment(value);
//        max.increment(value);
//        sd.increment(value);
//        n++;
//    }

    /**
     * Gets the number of bins calculated by the Freedman-Diaconis method.
     *
     * @return number of histogram bins.
     */
    public int numberOfBins(){
        if(n==0.0) return 1;
//        Percentile p = new Percentile();
//        double[] y = x.getElements();
//        p.evaluate(y);
//        double q3 = p.evaluate(y, 75);
//        double q1 = p.evaluate(y, 25);
		double iqr=q3-q1;
        double binWidth=2.0*(iqr/Math.pow(n,1.0/3.0));
        int numberOfBins=(int)Math.ceil((max-min)/binWidth);
        return numberOfBins;
    }

    /**
     * Gets the width of the bins according to the number of bins being used.
     *
     * @return bin width.
     */
    public double binWidth(){
        if(n==0.0) return 1;
//        Percentile p = new Percentile();
//        double[] y = x.getElements();
//        p.evaluate(y);
//        double q3 = p.evaluate(y, 75);
//        double q1 = p.evaluate(y, 25);
		double iqr=q3-q1;
        double binWidth=2.0*(iqr/Math.pow(n,1.0/3.0));
        return binWidth;
    }

    public BinCalculationType getType(){
        return BinCalculationType.FREEDMAN_DIACONIS;
    }


}

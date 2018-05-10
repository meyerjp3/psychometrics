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


import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.util.ResizableDoubleArray;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;

/**
 * A histogram involves an array of value for the x-axis (i.e. the points) and an array of values for the
 * y-axis (i.e. the values). The type of y-axis values returned the evaluate methods are determined
 * by the argument {@link #histogramType}.
 *
 * This class provides two different ways to create a histogram. One way involves passing an entire double array
 * to {@link #evaluate(double[])} a second way requires that you first incrementally update the data with
 * calls to {@link #increment(double)} or {@link #setData(double[])} and then call {@link #evaluate()}. If you
 * call the method {@link #evaluate()} without first calling {@link #setData(double[])} or incrementally
 * updating the data, you will get a null pointer exception.
 *
 * @author J. Patrick Meyer
 *
 */
public class Histogram implements QuadratureRule {

    private int numberOfBins = 1;

	private double binWidth = 1.0;

    private double n = 0;

	private ArrayList<Bin> bins = new ArrayList<Bin>();

    private BinCalculationType binCalculationType = BinCalculationType.STURGES;

    private double[] points = null;

    private double[] value = null;

    private ResizableDoubleArray data = null;

    private boolean lowerInclusive = true;

    private BinCalculation binCalc = null;
    
    private HistogramType histogramType = HistogramType.FREQUENCY;

    protected boolean interpolatorUpdateNeeded = false;

    //Cubic spline interpolator
    private UnivariateFunction interpolationFunction = null;

    public Histogram(HistogramType histogramType){
        this(histogramType, BinCalculationType.STURGES, true);
    }

    /**
     * This constructor is the most general. It can only use three of the four bin calculation classes. That is,
     * the {@link #binCalculationType} must be a FreedmanDiaconisBinCalculation, ScottBinCalculation, or
     * SturgesBinCalculation. A SimpleBinCalculation is not permitted.
     *
     * @param histogramType indicates the type of y-axis values that will be returned when {@link #evaluate()}
     *                      is called. It has no other effect.
     * @param lowerInclusive true if the bins should be lower inclusive. If false, the bins will be upper inclusive.
     */
    public Histogram(HistogramType histogramType, BinCalculationType binCalculationType, boolean lowerInclusive){
        if(binCalculationType==BinCalculationType.SIMPLE) throw new IllegalArgumentException("Cannot use a SimpleBinCalculation with this constructor");
        this.histogramType = histogramType;
        this.binCalculationType = binCalculationType;
        this.lowerInclusive = lowerInclusive;
        data = new ResizableDoubleArray();
        bins = new ArrayList<Bin>();
    }

    /**
     * This constructor is for use with a {@link com.itemanalysis.psychometrics.histogram.SimpleBinCalculation}
     * where the user provides the min, max, and number of points.
     *
     * @param histogramType type of histogram
     * @param numberOfBins number of bins to include in histogram
     * @param min minimum value
     * @param max maximum value
     * @param lowerInclusive true if lower bound is included in the interval but teh upper bound is not.
     *                       If false lower bound not included but upper bound is included.
     */
    public Histogram(HistogramType histogramType, int numberOfBins, double min, double max, boolean lowerInclusive){
        this.histogramType = histogramType;
        this.binCalculationType = BinCalculationType.SIMPLE;
        binCalc = new SimpleBinCalculation(numberOfBins, min, max);
        this.lowerInclusive = lowerInclusive;
        data = new ResizableDoubleArray();
        bins = new ArrayList<Bin>();
    }

    private void createHistogram(double[] x){
        n = x.length;
        Min min = new Min();
        Max max = new Max();
        Mean mean = new Mean();
        StandardDeviation sd = new StandardDeviation();

        for(int i=0;i<x.length;i++){
            min.increment(x[i]);
            max.increment(x[i]);
            mean.increment(x[i]);
            sd.increment(x[i]);
        }

        double range = max.getResult() - min.getResult();
        double lowestBoundary = min.getResult()-range/1000;
        double largestBoundary = max.getResult()+range/1000;

        if(binCalculationType== BinCalculationType.SCOTT){
            binCalc = new ScottBinCalculation(n, min.getResult(), max.getResult(), sd.getResult());
        }else if(binCalculationType== BinCalculationType.FREEDMAN_DIACONIS){
            Percentile percentile = new Percentile();
            double q1 = percentile.evaluate(x, 25);
            double q3 = percentile.evaluate(x, 75);
            binCalc = new FreedmanDiaconisBinCalculation(n, min.getResult(), max.getResult(), q1, q3);
        }else if(binCalculationType==BinCalculationType.STURGES){
            binCalc = new SturgesBinCalculation(n, min.getResult(), max.getResult());
        }

        numberOfBins = binCalc.numberOfBins();
        binWidth = binCalc.binWidth();

        //create bins
        createBins(lowestBoundary, largestBoundary);

        //count observations in each bin
        for(int i=0;i<n;i++){
            for(Bin b : bins){
                b.increment(x[i]);
            }
        }
    }

    private double[] getFrequency(double[] x){
        createHistogram(x);

        int index = 0;
        double sum = 0;
        for(Bin b : bins){
            points[index] = b.getMidPoint();
            value[index] = b.getFrequency();
            index++;
        }
        return value;
    }

    private double[] getRelativeFrequency(double[] x){
        createHistogram(x);

        int index = 0;
        for(Bin b : bins){
            points[index] = b.getMidPoint();
            value[index] = b.getFrequency()/n;
            index++;
        }
        return value;
    }

    private double[] getNormalizedRelativeFrequency(double[] x){
        createHistogram(x);

        int index = 0;
        double sum = 0;
        for(Bin b : bins){
            points[index] = b.getMidPoint();
            value[index] = b.getFrequency()/n;
            sum += value[index];
            index++;
        }

        for(int i=0;i<numberOfBins;i++){
            value[i]/=sum;
        }
        return value;
    }


    private double[] getDensity(double[] x){
        createHistogram(x);

        int index = 0;
        for(Bin b : bins){
            points[index] = b.getMidPoint();
            value[index] = b.getFrequency()/(n*binWidth);
            index++;
        }
        return value;
    }

    private double[] getFrequency(){
        return getFrequency(data.getElements());
    }

    private double[] getRelativeFrequency(){
        return getRelativeFrequency(data.getElements());
    }

    private double[] getNormalizedRelativeFrequency(){
        return getNormalizedRelativeFrequency(data.getElements());
    }

    private double[] getDensity(){
        return getDensity(data.getElements());
    }

    public double getSumOfValues(){
        double sum = 0;
        for(int i=0;i<numberOfBins;i++){
            sum+=value[i];
        }
        return sum;
    }

    public double getValueAt(int index){
        return value[index];
    }


    /**
     * Create the bins.
     */
    private void createBins(double lowestBoundary, double largestBoundary){
        if(bins!=null) bins.clear();
		Bin bin=null;
        points = new double[numberOfBins];
        value = new double[numberOfBins];

        for(int i=1;i<numberOfBins;i++){
            bin = new Bin(lowestBoundary+(i-1)*binWidth, lowestBoundary+i*binWidth, lowerInclusive);
            bins.add(bin);
        }
        bin = new Bin(lowestBoundary+(numberOfBins-1)*binWidth, largestBoundary, lowerInclusive);
        bins.add(bin);

        for(int i=0;i<bins.size();i++){
            points[i] = bins.get(i).getMidPoint();
        }

	}

    /**
     * Incrementally counts the number of observations in each bin. The value is counted only in the bin to which
     * it belongs.
     *
     * @param value a value to be counted in a bin.
     */
	public void increment(double value){
		data.addElement(value);
	}

    public void increment(double value, double frequency){
        for(int w=0;w<frequency;w++){
            data.addElement(value);
        }
    }

    /**
     * Gets the number of histogram bins.
     *
     * @return number of bins.
     */
    public int getNumberOfBins(){
        return numberOfBins;
    }

    public double getSampleSize(){
        return n;
    }

    /**
     * Histogram bins are stored in an array list. Gets the bin at the specified index.
     *
     * @param index position in the array list of the bin.
     * @return a histogram bin.
     */
    public Bin getBinAt(int index){
        return bins.get(index);
    }

    /**
     * Gets an array of evaluation points. This method is required by the {@link QuadratureRule} interface.
     * The evaluation points are the bin midpoints.
     *
     * @return an array of evaluation points
     */
    public double[] getPoints(){
		return points;
    }

    public void setData(double[] x){
        if(data!=null)data.clear();

        for(int i=0;i<x.length;i++){
            data.addElement(x[i]);
        }
    }

    /**
     * Gets an array of value values. This method is required by the {@link QuadratureRule} interface.
     *
     * @return an array of value values.
     */
    public double[] evaluate(){
        if(value!=null) return value;

        if(histogramType==HistogramType.FREQUENCY){
            return getFrequency();
        }else if(histogramType==HistogramType.RELATIVE_FREQUENCY){
            return getRelativeFrequency();
        }else if(histogramType==HistogramType.NORMALIZED_RELATIVE_FREQUENCY){
            return getNormalizedRelativeFrequency();
        }else{
            return getDensity();
        }
    }

    public double[] evaluate(double[] x){
        if(value!=null) return value;

        if(histogramType==HistogramType.FREQUENCY){
            return getFrequency(x);
        }else if(histogramType==HistogramType.RELATIVE_FREQUENCY){
            return getRelativeFrequency(x);
        }else if(histogramType==HistogramType.NORMALIZED_RELATIVE_FREQUENCY){
            return getNormalizedRelativeFrequency(x);
        }else{
            return getDensity(x);
        }
    }

    /**
     * Gets an evaluation points at the specified index. This method is required by the
     * {@link QuadratureRule} interface. The evaluation points are the bin midpoints.
     *
     * @param index array index of evaluation point.
     * @return an evaluation point.
     */
    public double getPointAt(int index){
        if(points==null) evaluate();
        return points[index];
    }

    /**
     * Gets a value value at the specified index.  This method is required by the
     * {@link QuadratureRule} interface.
     *
     * @param index array index of value value.
     * @return value value.
     */
    public double getDensityAt(int index){
        if(value==null) evaluate();
        return value[index];
    }

    public void setDensityAt(int index, double value){
        this.value[index] = value;
    }

    public void setPointAt(int index, double value){
        points[index] = value;
    }

    public double getMinimum(){
        return points[0];
    }

    public double getMaximum(){
        return points[numberOfBins-1];
    }

    public double getMean(){
        double m = 0.0;
        for(int i=0;i<numberOfBins;i++){
            m += points[i]* value[i];
        }
        return m;
    }

    public double getStandardDeviation(){
        double m = getMean();
        double m2 = 0;
        for(int i=0;i<numberOfBins;i++){
            m2 += (points[i]-m)*(points[i]-m)* value[i];
        }
        return Math.sqrt(m2);
    }

    public double getBinWidth(){
        return binWidth;
    }

    /**
     * Gets the number of evaluation points (and corresponding number of value values).
     *  This method is required by the {@link QuadratureRule} interface.
     *
     * @return number of evaluation points.
     */
    public int getNumberOfPoints(){
        return bins.size();
    }

    /**
     * Gets the iterator for the array list of bin objects.
     *
     * @return an iterator.
     */
    public Iterator<Bin> iterator(){
        return bins.iterator();
    }

    /**
     * Uses current quadrature points and weights to compute the mean and standard deviation of the
     * density, and tehn standardizes the quadrature to have a mean of zero and a standard deviation of one.
     * It achieves standardization in one of two possible ways. If keepPoints is true, the original points are
     * retained and linear interpolation of the empiricial cumulative quadrature is used to obtain the new
     * weights. That is, the points are never changed, but the quadrature is standardized. If keepPoints is
     * false, the original weights are retained, but the points are transformed to standardize the quadrature.
     *
     * @param keepPoints if true original points are retained and weights are computed at these points using
     *                   linear interpolation of the empirical cumulative quadrature. If false, original weights
     *                   are retained and standardization is achieved by linearly transforming the original points.
     * @return transformation coefficients as a double array with two values used for the linear transformation of
     * the points. The first value is the slope and the second value is the intercept.
     */
    public double[] standardize(boolean keepPoints){

        //Compute current mean and standard deviation
        double newMean = this.getMean();
        double newSD = this.getStandardDeviation();

        //Compute transformation coefficients
        double slope = 1.0/newSD;
        double intercept = -slope*newMean;
        double[] coef = {slope, intercept};

        //Keep points and change weights to standardize the quadrature.
        if(keepPoints){
            //Transform points and compute cumulative sum of weights (i.e. cumulative probabilities)
            double[] x = new double[numberOfBins+2];
            double[] w = new double[numberOfBins+2];
            double cumSum = 0;
            for(int i=0;i<numberOfBins;i++){
                x[i+1] = points[i]*slope+intercept;
                cumSum += value[i];
                w[i+1] = cumSum;
            }

            //To interpolate at the boundary, set lower bound just below the minimum and
            //the upper bound just above the maximum.
            double lower = Math.min(getMinimum(), x[1]);
            double upper = Math.max(getMaximum(), x[numberOfBins]);
            x[0] = lower-0.05;
            x[numberOfBins+1] = upper+0.05;

            //Set weights to zero for the lower boundary and 1.0 for the upper boundary
            w[0] = 0.0;
            w[numberOfBins+1] = 1.0;

            //Interpolator object
            UnivariateFunction interpolationFunction = null;
            UnivariateInterpolator interpolator = new LinearInterpolator();
            interpolationFunction = interpolator.interpolate(x, w);
            interpolatorUpdateNeeded = false;

            value[0] = interpolationFunction.value(points[0]);
            for(int i=1;i<numberOfBins;i++){
                value[i] = interpolationFunction.value(points[i])-interpolationFunction.value(points[i-1]);
            }
        }
        //Keep weights and linearly transform points to standardize the quadrature
        else{
            for(int i=0;i<numberOfBins;i++){
                points[i] = points[i]*slope+intercept;
            }
        }

        return coef;
    }

    /**
     * A string representation of the histogram. It lists the bin intervals, midpoints, and value values.
     *
     * @return histogram values for display as plain text.
     */
    @Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);

		String li = "", ui="";

		f.format("%n");
		f.format("%64s", "                        HISTOGRAM VALUES                        ");f.format("%n");
		f.format("%64s", "================================================================");f.format("%n");
		f.format("%16s","Lower Bound");f.format("%18s","Upper Bound"); f.format("%15s","MidPoint");

        if(histogramType==HistogramType.FREQUENCY){
            f.format("%15s","Frequency");
        }else if(histogramType==HistogramType.RELATIVE_FREQUENCY){
            f.format("%15s","Rel. Freq.");
        }else if(histogramType==HistogramType.NORMALIZED_RELATIVE_FREQUENCY){
            f.format("%15s","Rel. Freq.");
        }else {
            f.format("%15s","Density");
        }

        f.format("%n");
		f.format("%64s", "----------------------------------------------------------------"); f.format("%n");

        Bin b = null;
        for(int i=bins.size()-1;i>-1;i--){
            b = bins.get(i);
//		for(Bin b : bins){
			if(b.lowerInclusive()){
                li = "[";
                ui = ")";
            }else{
                li = "(";
                ui = "]";
            }

			f.format("%1s", li);f.format("% 15.5f, ",b.getLowerBound());f.format("% 15.5f",b.getUpperBound()); f.format("%1s", ui); f.format("% 15.5f",points[i]); f.format("% 15.5f",value[i]);
			f.format("%n");
		}
		f.format("%64s", "================================================================");f.format("%n");
        f.format("%11s", "Binwidth = "); f.format("% .4f", binWidth);f.format("%n");
		return f.toString();
	}

}

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
package com.itemanalysis.psychometrics.rasch;

import com.itemanalysis.psychometrics.histogram.Bin;
import com.itemanalysis.psychometrics.histogram.Histogram;
import com.itemanalysis.psychometrics.statistics.NormalDensity;
import com.itemanalysis.psychometrics.statistics.UniformDensity;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;


/**
 * This class represents a distribution of Theta objects.
 *
 * @author J. Patrick Meyer
 * @since July 12, 2008
 *
 */
@Deprecated
public class ThetaDistribution {

	private ArrayList<Theta> thetaDistribution = new ArrayList<Theta>();
//	private Mean mean = new Mean();
//	private StandardDeviation sd = new StandardDeviation();

    //may need this for equating
//	public ThetaDistribution(double[] theta, double[] weight){
//		for(int i=0;i<theta.length;i++){
//			thetaDistribution.add(new Theta(theta[i], weight[i], 10000000+i));
//			mean.increment(theta[i], weight[i]);
//			sd.increment(theta[i], weight[i]);
//		}
//	}

	public ThetaDistribution(double[] theta){
		for(int i=0;i<theta.length;i++){
			thetaDistribution.add(new Theta(theta[i], 10000000+i));
//			mean.increment(theta[i]);
//			sd.increment(theta[i]);
		}
	}

    //may need this for equating
//	public ThetaDistribution(double[][] rho){
//		for(int i=0;i<rho.length;i++){
//			thetaDistribution.add(new Theta(rho[i][0], rho[i][1], 10000000+i));
//			mean.increment(rho[i][0], rho[i][1]);
//			sd.increment(rho[i][0], rho[i][1]);
//		}
//	}

	public ThetaDistribution(){

	}

	public void add(double theta){
		int id = thetaDistribution.size();
		Theta t = new Theta(theta, 100000000+id);
		thetaDistribution.add(t);
//		mean.increment(theta);
//		sd.increment(theta);
	}

    public void add(Histogram histogram){
        Iterator<Bin> iter = histogram.iterator();
        Theta t = null;
        Bin b = null;
        int index = 0;
        while(iter.hasNext()){
            b = iter.next();
            t = new Theta(b.getMidPoint(), b.getFrequency(), index);
            thetaDistribution.add(t);
            index++;
        }
    }

	public void add(double theta, double weight, int id){
		Theta t = new Theta(theta, weight, id);
		thetaDistribution.add(t);
	}

	public double dnorm(double z){
		double prob;
		prob = 1/Math.sqrt(2.0*Math.PI)*Math.exp(-(z*z)/2);
		return prob;
	}

	public void uniformPoints(int numPoints, double min, double max){
        UniformDensity uniform = new UniformDensity();
        double[][] density = uniform.value(min, max, numPoints);
		for(int i=0;i<numPoints;i++){
			thetaDistribution.add(new Theta(density[i][0], density[i][1], 10000000+i));
		}
	}

    public void normalPoints(int numPoints, double mean, double sd){
        NormalDensity normal = new NormalDensity(mean, sd);
        double[][] density = normal.value( numPoints);
        for(int i=0;i<numPoints;i++){
			thetaDistribution.add(new Theta(density[i][0], density[i][1], 10000000+i));
		}
    }

//	public void randomNormalPoints(int numPoints, double mean, double variance){
//		Random rand = new Random();
//		for(int i=0;i<numPoints;i++){
//			thetaDistribution.add(new Theta(rand.nextGaussian()*Math.sqrt(variance)+mean, 10000000+i));
//		}
//	}

	public void print(){
		Theta t=null;
		Iterator<Theta> iter = thetaDistribution.iterator();
		while(iter.hasNext()){
			t=iter.next();
			System.out.format("%f      %f%n", t.value(), t.getWeight());
		}
	}

	//FIXME will not give correct sample size when weights are used

	public int size(){
		return thetaDistribution.size();
	}

//	public double getTotalSampleSize(){
//		return mean.getValidN();
//	}

	public Theta getThetaAt(int index){
		return thetaDistribution.get(index);
	}

	public double[] getTheta(){
		int numPoints = thetaDistribution.size();
		double[] t = new double[numPoints];
		for(int i=0;i<numPoints;i++){
			t[i]=thetaDistribution.get(i).value();
		}
		return t;
	}

//	public double getMean(){
//		return mean.getResult();
//	}

//	public double getVariance(){
//		return Math.pow(sd.getResult(),2);
//	}

    @Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		int index=0;

		Theta t=null;
		Iterator<Theta> iter = thetaDistribution.iterator();
		while(iter.hasNext()){
			t=iter.next();
			f.format("%.6f", t.value());
			if(index<(thetaDistribution.size()-1)){
				f.format("%1s", ",");
			}
			index++;
		}
		return f.toString();
	}

	public String getTable(){
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		Theta t=null;
		Iterator<Theta> iter = thetaDistribution.iterator();

		f.format("%10s", "Value"); f.format("%5s", " "); f.format("%10s", "Density");f.format("%n");
		f.format("%25s", "=========================");f.format("%n");
		while(iter.hasNext()){
			t=iter.next();
			f.format("% 10.4f", t.value()); f.format("%5s", " "); f.format("% 10.4f", t.getWeight());f.format("%n");
		}
		f.format("%25s", "=========================");f.format("%n");
		return f.toString();
	}

	public String getTranformedValues(){
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		int index=0;

		Theta t=null;
		Iterator<Theta> iter = thetaDistribution.iterator();
		while(iter.hasNext()){
			t=iter.next();
			f.format("%.6f", t.value());
			if(index<(thetaDistribution.size()-1)){
				f.format("%1s", ",");
			}
			index++;
		}
		return f.toString();
	}

	public Iterator<Theta> iterator(){
		return thetaDistribution.iterator();
	}

}


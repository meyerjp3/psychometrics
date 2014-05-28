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

public class EstepEstimates {

    private double[][] rjk = null;
    private double[] nk = null;
    private int nItems = 0;
    private int nPoints = 0;
    private double loglikelihood = 0.0;

    public EstepEstimates(int nItems, int nPoints){
        this.nItems = nItems;
        this.nPoints = nPoints;
        rjk = new double[nItems][nPoints];
        nk = new double[nPoints];
    }

    public void incrementRjk(int j, int k, double value){
        rjk[j][k]+=value;
    }

    public void incrementNk(int k, double value){
        nk[k]+=value;
    }

    public void incrementLoglikelihood(double value){
        loglikelihood+=value;
    }

    public double[][] getRjk(){
        return rjk;
    }

    public double[] getNk(){
        return nk;
    }

    public double getSumNk(){
        double sum = 0.0;
        for(int k=0;k<nPoints;k++){
            sum += nk[k];
        }
        return sum;
    }

    public double getRjkAt(int j, int k){
        return rjk[j][k];
    }

    public double[] getRjkAt(int j){
        return rjk[j];
    }

    public double getNkAt(int k){
        return nk[k];
    }

    public double getLoglikelihood(){
        return loglikelihood;
    }

    public void increment(EstepEstimates estepEstimates){
        for(int k=0;k<nPoints;k++){
            for(int j=0;j<nItems;j++){
                rjk[j][k] += estepEstimates.getRjkAt(j, k);
            }
            nk[k] += estepEstimates.getNkAt(k);
        }
        loglikelihood += estepEstimates.getLoglikelihood();
    }

    public EstepEstimates add(EstepEstimates estepEstimates){
        EstepEstimates est = new EstepEstimates(nItems, nPoints);
        est.increment(this);
        est.increment(estepEstimates);
        return est;
    }

    @Override
    public String toString(){
        String s = "";
        for(int j=0;j<nItems;j++){
            s+= "Item" + (j+1) + ": ";
            for(int k=0; k<nPoints;k++){
                s += rjk[j][k] + "  ";
            }
            s+= "\n";
        }

        s+= "  nk: ";
        for(int k=0; k<nPoints; k++){
            s+= + nk[k] + "  ";
        }
        s+= "\n NkSum = " + getSumNk();
        return s;
    }

}

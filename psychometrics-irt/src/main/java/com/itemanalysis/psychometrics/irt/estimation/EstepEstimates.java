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

    private EstepItemEstimates[] r = null;

    private double[] nt = null;
    private int nItems = 0;
    private int nPoints = 0;
    private double loglikelihood = 0.0;
    private String groupCode = "";
    private double groupMean = 0;
    private double groupSD = 0;

    private int[] ncat = null;

    public EstepEstimates(int nItems, int[] ncat, int nPoints){
        this.nItems = nItems;
        this.nPoints = nPoints;
        this.ncat = ncat;
        r = new EstepItemEstimates[nItems];
        nt = new double[nPoints];


        //Initialize second dimension of array.
        for(int j=0;j<nItems;j++){
            r[j] = new EstepItemEstimates(ncat[j], nPoints);
        }


    }

    public void incrementRjkt(int j, int k, int t, double value){
        r[j].incrementRjkt(k, t, value);
    }

    public void incrementNt(int t, double value){
        nt[t]+=value;
    }

    public void incrementLoglikelihood(double value){
        loglikelihood+=value;
    }

    public double[] getNt(){
        return nt;
    }

    public double getSumNt(){
        double sum = 0.0;
        for(int t=0;t<nPoints;t++){
            sum += nt[t];
        }
        return sum;
    }

    public double getSumRkt(int j){
        double sum = 0;
        for(int k=0;k<ncat[j];k++){
            for(int t=0;t<nPoints;t++){
                sum += r[j].getRjktAt(k,t);
            }
        }
        return sum;
    }

    public double getRjkAt(int j, int k, int t){
        return r[j].getRjktAt(k, t);
    }

    public EstepItemEstimates getRjkAt(int j){
        return r[j];
    }

    public double getNtAt(int t){
        return nt[t];
    }

    public double getLoglikelihood(){
        return loglikelihood;
    }

    public void increment(EstepEstimates estepEstimates){
        for(int t=0;t<nPoints;t++){
            for(int j=0;j<nItems;j++){
                for(int k=0;k<ncat[j];k++){
                    r[j].incrementRjkt(k, t, estepEstimates.getRjkAt(j, k, t));
                }
            }

            nt[t] += estepEstimates.getNtAt(t);
        }
        loglikelihood += estepEstimates.getLoglikelihood();
    }

    public EstepEstimates add(EstepEstimates estepEstimates){
        EstepEstimates est = new EstepEstimates(nItems, ncat, nPoints);
        est.increment(this);
        est.increment(estepEstimates);
        return est;
    }

    @Override
    public String toString(){
        String s = "";
        for(int j=0;j<nItems;j++){
            s+= "Item" + (j+1) + ": ";
            for(int t=0; t<nPoints;t++){
                s += "[";
                for(int k=0;k<ncat[j];k++){
//                    s += rjkt[j][getIndex(j,k,t)] + "  ";
                    s += r[j].getRjktAt(k, t) + "  ";
                }
                s += "]";
            }
            s+= "\n";
        }

        s+= "  nt: ";
        for(int k=0; k<nPoints; k++){
            s+= + nt[k] + "  ";
        }

        s+= "\n RjSum = ";
        for(int j=0;j<nItems;j++){
            s+= getSumRkt(j) + " ";
        }

        s+= "\n NtSum = " + getSumNt();

        return s;
    }

}

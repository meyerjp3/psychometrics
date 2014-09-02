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

    /**
     * A ragged array that stores the expected number of responses to each category for each item.
     * This is a two-dimensional array but it is storing values for three dimensions. The three
     * dimensions are the number of items by the number of response categories by the number of
     * quadrature points. Indexes that map the three dimensions onto two dimensions are explained
     * below in the comments for the method getIndex(int j, int k, int t).
     */
//    private double[][] rjkt = null;

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
//        rjkt = new double[nItems][];
        r = new EstepItemEstimates[nItems];
        nt = new double[nPoints];


        //Initialize second dimension of array.
        for(int j=0;j<nItems;j++){
//            rjkt[j] = new double[ncat[j]*nPoints];
            r[j] = new EstepItemEstimates(ncat[j], nPoints);
        }


    }

    /**
     * Instead of using a three-way array (nitems x ncat x nPoints) to store expected counts,
     * the last two dimensions are stored as a single array. That is the ncat x nPoints
     * dimensions are stored as a single dimension that is ncat x nPoints in size for each item.
     * For example, a item with 2 categories using 4 quadrature points would look like this in two dimensions:
     *
     * [0,0] = A
     * [0,1] = B
     * [0,2] = C
     * [0,3] = D
     *
     * [1,0] = E
     * [1,1] = F
     * [1,2] = G
     * [1,3] = H
     *
     * And, in one dimension is would appear as shown below with the index computed as nQuad*row+col.
     *
     * [0] = A  {4*0+0 = 0}
     * [1] = B  {4*0+1 = 1}
     * [2] = C  {4*0+2 = 2}
     * [3] = D  {4*0+3 = 3}
     * [4] = E  {4*1+0 = 4}
     * [5] = F  {4*1+1 = 5}
     * [6] = G  {4*1+2 = 6}
     * [7] = H  {4*1+3 = 7}
     *
     * This method does the calculation, nPoints*row+col, to get the ine-dimensional array index
     * using two-dimensional array indices, k, and t.
     *
     *
     * @param k category index
     * @param t quadrature point index
     * @return
     */
//    private int getIndex(int j, int k, int t){
//        if(k>ncat[j]-1) throw new ArrayIndexOutOfBoundsException("Row index " + k + " > array size of " + ncat[j] + ".");
//        if(t>nPoints-1) throw new ArrayIndexOutOfBoundsException("Column index " + t + " > array size of " + nPoints + ".");
//        return nPoints*k+t;
//    }

    public void incrementRjkt(int j, int k, int t, double value){
//        rjkt[j][getIndex(j, k, t)]+=value;
        r[j].incrementRjkt(k, t, value);
    }

    public void incrementNt(int t, double value){
        nt[t]+=value;
    }

    public void incrementLoglikelihood(double value){
        loglikelihood+=value;
    }

//    public double[][] getRjkt(){
//        return rjkt;
//    }

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
//        return rjkt[j][getIndex(j, k,t)];
        return r[j].getRjktAt(k, t);
    }

//    public double[] getRjkAt(int j){
//        return rjkt[j];
//    }

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
//                    rjkt[j][getIndex(j,k,t)] += estepEstimates.getRjkAt(j, k, t);
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

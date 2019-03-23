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

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;

import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedHashMap;

/**
 * This class computes the quadrature of summed scores for a given set of item response models.
 * It uses the recursive algorithm developed by Thissen, et al. It assumes that test scores range
 * from 0 to the sum of the maximum points awarded by each item. The value of the test score is
 * also the index of the score in the array.
 *
 * See:
 * Thissen, D., Pommerich, M., Billeaud, K., and Williams, V. S. L. (1995). Item response theory for scores
 * on tests including polytomous items with ordered responses. Applied Psychological Measurement, 19 (1),
 * 39-49.
 *
 * Thissen, D., and Wainer, H. (2001). Test scoring. Mahwah, NJ: Lawrence Erlbuam.
 *
 */
public class IrtObservedScoreDistribution {

    private ItemResponseModel[] irm = null;
    private int nItems = 0;
    private int nPoints = 50;
    private int maxObservedScore = 0;
    private int maxPL1 = 0;
    private QuadratureRule latentDistribution = null;
    private double[][] L = null;
    private double[] summedScoreDensity = null;

    public IrtObservedScoreDistribution(ItemResponseModel[] irm, QuadratureRule latentDistribution){
        this.irm = irm;
        this.nItems = irm.length;
        this.latentDistribution = latentDistribution;
        this.nPoints = latentDistribution.getNumberOfPoints();
        initialize();
    }

    public IrtObservedScoreDistribution(LinkedHashMap<VariableName, ItemResponseModel> irm, QuadratureRule latentDistribution){
        this.nItems = irm.size();
        this.irm = new ItemResponseModel[this.nItems];

        //Convert map to array
        int i=0;
        for(VariableName v : irm.keySet()){
            this.irm[i] = irm.get(v);
            i++;
        }

        this.latentDistribution = latentDistribution;
        this.nPoints = latentDistribution.getNumberOfPoints();
        initialize();
    }

    private void initialize(){
        for(int j=0;j<nItems;j++){
            maxObservedScore += irm[j].getMaxScoreWeight();
        }
        maxPL1 = maxObservedScore+1;
        L = new double[nPoints][maxPL1];
    }


    /**
     * Computes the IRT observed score quadrature for a test that contains only binary items.
     * This recursive algorithm is described in the book Test Scoring.
     *
     * Thissen, D., and Wainer, H. (2001). Test scoring. Mahwah, NJ: Lawrence Erlbuam.
     *
     */
    public void computeAllBinaryItems(){
        int iStar = 0;
        double theta = 0;
        double[] prevL = null;
        summedScoreDensity = new double[maxPL1];

        for(int k=0;k<nPoints;k++){

            theta = latentDistribution.getPointAt(k);
            L[k][0] = irm[0].probability(theta, 0);
            L[k][1] = irm[0].probability(theta, 1);

            iStar=1;
            while(iStar<maxObservedScore){
                prevL = Arrays.copyOf(L[k], L[k].length);
                L[k][0] = prevL[0]*irm[iStar].probability(theta, 0);
                for(int x=1;x<=iStar;x++){
                    L[k][x] = prevL[x]*irm[iStar].probability(theta, 0) +
                              prevL[x-1]*irm[iStar].probability(theta, 1);
                    L[k][x+1] = prevL[x]*irm[iStar].probability(theta, 1);
                }
                iStar++;
            }

            for(int j=0;j<maxPL1;j++){
                summedScoreDensity[j] += latentDistribution.getDensityAt(k)*L[k][j];
            }

        }

    }

    /**
     * Computes the IRT observed score quadrature for a test that contains binary items, polytomous items, or
     * binary and polytomous items. This method is preferred over computeAllBinaryItems() because it is more
     * general and can handle binary item, polytomous item, and mixed format tests. It is an implementation
     * of the recursive algorithm described in:
     *
     * Thissen, D., Pommerich, M., Billeaud, K., and Williams, V. S. L. (1995). Item response theory for scores
     * on tests including polytomous items with ordered responses. Applied Psychological Measurement, 19 (1),
     * 39-49.
     *
     * See also:
     * Thissen, D., and Wainer, H. (2001). Test scoring. Mahwah, NJ: Lawrence Erlbuam.
     *
     */
    public void compute(){
        double theta = 0;
        double[] prevL = null;
        int iterMaxSumScore = 0;

        summedScoreDensity = new double[maxPL1];

        for(int i=0;i<nPoints;i++){
            theta = latentDistribution.getPointAt(i);

            int ncat = irm[0].getNcat();
            for(int k=0;k<ncat;k++){
                L[i][k] = irm[0].probability(theta, k);
            }

            iterMaxSumScore = (int)irm[0].getMaxScoreWeight();

            for(int iStar=1;iStar<nItems;iStar++){
                prevL = Arrays.copyOf(L[i], L[i].length);
                ncat = irm[iStar].getNcat();

                L[i] = new double[maxPL1];

                for(int x=0;x<=iterMaxSumScore;x++){
                    for(int k=0;k<ncat;k++){
                        L[i][x+k] += prevL[x]*irm[iStar].probability(theta, k);
//                        System.out.println("Theta"+(i+1) + ":  L("+x+", "+ k +") = " + "L^"+iStar+"_"+(x+k)+" = L^"+(iStar-1)+"_"+x+" * T"+k+","+iStar);
                    }

                }
                iterMaxSumScore+=irm[iStar].getMaxScoreWeight();
//                System.out.println("ITEM: " + iStar + "  MAX+ISC" + irm[iStar].getMaxScoreWeight() + "  MAXSC: " + maxSumScore);
            }

            for(int j=0;j<maxPL1;j++){
                summedScoreDensity[j] += latentDistribution.getDensityAt(i)*L[i][j];
            }

        }
    }

    /**
     * Computes the EAP estimate of examinee ability (theta) for a given summed score.
     *
     * @return EAP estimate of theta.
     */
    public double getEAP(int summedScore){
        if(summedScore<0 || summedScore>L[0].length) return Double.NaN;
        double eap = 0;
        double theta = 0;
        double sum = 0;
        for(int k=0;k<L.length;k++){
            theta = latentDistribution.getPointAt(k);
            eap += theta*L[k][summedScore]*latentDistribution.getDensityAt(k);
        }
        return eap/summedScoreDensity[summedScore];
    }

    /**
     * Returns estimated IRT summed score density
     *
     * @return array of density values
     */
    public double[] getDensity(){
        return summedScoreDensity;
    }

    /**
     * Gets the density of a summed score.
     *
     * @param summedScore target summed score
     * @return density for the summed score.
     */
    public double getDensityAt(int summedScore){
        if(summedScore>maxObservedScore) return 0.0;
        if(summedScore<0) return 0.0;
        return summedScoreDensity[summedScore];
    }

    public int getNumberOfScores(){
        return maxPL1;
    }

    public QuadratureRule getLatentDistribution(){
        return latentDistribution;
    }

    /**
     * Provides the probability of a summed score given a value of theta.
     *
     * @param thetaIndex position of the quadrature point (latent quadrature value) in the array.
     * @param summedScore a summed score.
     * @return probability of a summed score.
     */
    public double getProbabilityAt(int thetaIndex, int summedScore){
        return L[thetaIndex][summedScore];
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-19s", "        SUM SCORE:");
        for(int x=0;x<maxPL1;x++){
            f.format("%9d", x); f.format("%2s", "");
        }
        f.format("%n");

        for(int k=0;k<nPoints;k++){
            f.format("%7s", "THETA ("); f.format("% 8.6f", latentDistribution.getPointAt(k)); f.format("%3s", "): ");
            for(int x=0;x<maxPL1;x++){
                f.format("% 8.6f", L[k][x]); f.format("%2s", "");
            }
            f.format("%n");
        }

        f.format("%-19s", "  Prob(Sum Score):");
        for(int x=0;x<maxPL1;x++){
            f.format("% 8.6f", summedScoreDensity[x]); f.format("%2s", "");
        }
        f.format("%n");

        f.format("%-19s", "        EAP SCORE:");
        for(int x=0;x<maxPL1;x++){
            f.format("% 8.6f", getEAP(x)); f.format("%2s", "");
        }
        f.format("%n");

        return f.toString();
    }


}

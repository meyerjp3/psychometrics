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

import com.itemanalysis.psychometrics.distribution.UserSuppliedDistributionApproximation;
import com.itemanalysis.psychometrics.histogram.*;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmType;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.uncmin.DefaultUncminOptimizer;
import com.itemanalysis.psychometrics.uncmin.UncminException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.Pair;

/**
 * Computes starting values for binary item response models. It begins with PROX estimates of
 * item difficulty and person ability (theta). It then maximizes the marginal likelihood for
 * each item using observed counts obtained from the PROX estimates. This class is based on the
 * Start3PL.h class in Brad Hanson's ETIRM library.
 */
public class StartingValues {

    private ItemResponseVector[] responseVector = null;
    private ItemResponseModel[] irm = null;
    private int nResponseVectors = 0;
    private int nItems = 0;
    private double[] theta = null;
    private Histogram hist = null;
    private EstepEstimates estepEstimates = null;

    /**
     * Default constructor takes an array of item response vectors and an array of item response models.
     *
     * @param responseVector
     * @param irm
     */
    public StartingValues(ItemResponseVector[] responseVector, ItemResponseModel[] irm){
        this.responseVector = responseVector;
        this.irm = irm;
        nResponseVectors = responseVector.length;
        nItems = irm.length;
        theta = new double[responseVector.length];
    }

    /**
     * The public method for computing start values. Starting values for item discrimination are constrained to
     * the interval 0.3 to 3.0. Lower asymptote (guessing) values are constrained to the interval 0.05 to 1.0.
     * No constraints are applied to item difficulty values.
     *
     * This method first computes PROX estimates for item difficulty and person ability. It then finds the
     * item parameters that maximize teh marginal likelihood using observed counts.
     *
     * @return
     */
    public ItemResponseModel[] computeStartingValues(){

        DefaultUncminOptimizer optimizer = new DefaultUncminOptimizer();
        ItemDichotomous itemDichotomous = new ItemDichotomous();
        double[] initialValue = null;
        double[] param = null;

        //compute prox estimates of item difficulty and person ability
        prox(0.01, 10);

        //summarize the prox values
        computeEstepEstimates();

        //The histogram will return frequencies, but relative frequencies are needed for the latent distribution.
        //Create a new latent distribution with relative frequencies.
        UserSuppliedDistributionApproximation latentDistribution = new UserSuppliedDistributionApproximation();
        double sum = hist.getSumOfValues();
        for(int k=0;k<hist.getNumberOfPoints();k++){
            latentDistribution.increment(hist.getPointAt(k), hist.getDensityAt(k)/sum);
        }


        for(int j=0;j<nItems;j++){
            int nPar = irm[j].getNumberOfParameters();

            //Compute start values for binary items only
            if(irm[j].getType()== IrmType.L3){
                initialValue = new double[nPar];
                if(nPar==3){
                    initialValue[0] = irm[j].getDiscrimination();
                    initialValue[1] = irm[j].getDifficulty();
                    initialValue[2] = irm[j].getGuessing();
                }else if(nPar==2){
                    initialValue[0] = irm[j].getDiscrimination();
                    initialValue[1] = irm[j].getDifficulty();
                }else{
                    initialValue[0] = irm[j].getDifficulty();
                }

                try{
                    //Using observed counts, find the item parameters that maximize the marginal likelihood
                    itemDichotomous.setModel((Irm3PL) irm[j], latentDistribution, estepEstimates.getRjkAt(j), estepEstimates.getNk());
                    optimizer.minimize(itemDichotomous, initialValue, true, false, 100);
                    param = optimizer.getParameters();
                }catch(UncminException ex){
                    //TODO the exception will be lost in a non-console program because no stack trace will be shown.
                    ex.printStackTrace();
                }


                if(nPar==3){
                    //only accept discrimination parameter estimates between 0.3 and 3.0 inclusive.
                    irm[j].setDiscrimination(Math.min(3.0, Math.max(param[0], 0.3)));
                    irm[j].setDifficulty(param[1]);

                    //only accept guessing parameter estimates between 0.05 and 1 inclusive.
                    irm[j].setGuessing(Math.min(1.000, Math.max(param[2], 0.05)));
                }else if(nPar==2){
                    irm[j].setDiscrimination(param[0]);
                    irm[j].setDifficulty(param[1]);
                }else{
                    irm[j].setDifficulty(param[0]);
                }


            }

        }

        //For debugging
//        for(int j=0;j<nItems;j++){
//            System.out.println("START VALUES: " + irm[j].toString());
//        }

        return irm;

    }

    /**
     * Creates a histogram of the latent values (theta) computed from the PROX procedure. The number of histogram
     * bins is computed by the Sturges method by default. This method counts the number of examinees in each bin to
     * get the nk values and it also computes the number of correct responses for examinees in each bin to get
     * pjk. The actual counts and actual number of correct responses at each theta level are the "E step" estimates
     * used by the optimizer to compute starting values.
     *
     * This method assumes that each examinee completes at least one binary item.
     *
     */
    private void computeEstepEstimates(){

        //create histogram
        hist = new Histogram(HistogramType.FREQUENCY);
        for(int l=0;l<theta.length;l++){
            //weighted analysis
            hist.increment(theta[l], responseVector[l].getFrequency());
        }

        double[] freq = hist.evaluate();
        int nBins = hist.getNumberOfBins();

        estepEstimates = new EstepEstimates(nItems, nBins);

        //compute number of examinees at each theta level
        for(int k=0;k<nBins;k++){
            estepEstimates.incrementNk(k, freq[k]);
        }

        //count number correct at each theta level for each binary item
        for(int l=0;l<nResponseVectors;l++){
            for(int w=0;w<responseVector[l].getFrequency();w++){
                for(int k=0;k<nBins;k++){
                    if(hist.getBinAt(k).inBin(theta[l])){
                        for(int j=0;j<nItems;j++){
                            if(irm[j].getType()==IrmType.L3){
                                estepEstimates.incrementRjk(j, k, responseVector[l].getResponseAt(j));
                            }
                        }
                    }

                }
            }
        }

    }

    /**
     * Compute the sum score and maximum possible test score from the binary items only.
     *
     * @param responseVector an examinee's response vector.
     * @return a pair reresenting teh sum score and maximum possible test score.
     */
    private Pair<Double, Double> computePersonScores(ItemResponseVector responseVector){
        double maxScore = 0.0;
        double sumScore = 0.0;
        for(int j=0;j<nItems;j++){
            if(responseVector.getResponseAt(j)!=-1 && irm[j].getType()==IrmType.L3){
                maxScore += irm[j].getMaxScoreWeight();
                sumScore += responseVector.getResponseAt(j);
            }
        }
        Pair<Double, Double> valuePair = new Pair<Double, Double>(new Double(sumScore), new Double(maxScore));
        return valuePair;
    }

    /**
     * Computes normal approximation estimates (PROX) of item difficulty and person ability
     * in a way that allows for missing data (Linacre, 1994). It is an iterative procedure.
     *
     * Linacre, J. M., (1994). PROX with missing data, or known item or person measures.
     * Rasch Measurement Transactions, 8:3, 378, http://www.rasch.org/rmt/rmt83g.htm.
     *
     * @param converge convergence criterion as the maximum change in person logits.
     * @param maxIter maximum number of iterations. About 10 iterations works well.
     */
    private void prox(double converge, int maxIter){
        double delta = 1.0+converge;
        int iter = 0;
        double pProx = 0;
        double pScore = 0;
        double maxTestScore = 0;
        double maxChange = 0;
        double logit = 0;

        Mean personGrandMean = new Mean();
        StandardDeviation personGrandSd = new StandardDeviation();
        double iProx = 0.0;
        double iMean = 0;
        theta = new double[nResponseVectors];

        Mean[] mPerson = new Mean[nItems];//Item difficulty mean for those examinees completing item j
        StandardDeviation[] sdPerson = new StandardDeviation[nItems];//Item difficulty standard deviation for those examinees completing item j
        double[] Si = null;
        double[] Ni = null;

        Mean[] mItem = new Mean[nResponseVectors];
        StandardDeviation[] sdItem = new StandardDeviation[nResponseVectors];

        while(delta>converge && iter < maxIter){
            Si = new double[nItems];
            Ni = new double[nItems];

            //Compute descriptive statistics for persons and items
            double resp = 0;
            double freq = 0;
            for(int l=0;l<nResponseVectors;l++){
                freq = responseVector[l].getFrequency();

                for(int j=0;j<nItems;j++){
                    if(irm[j].getType()==IrmType.L3){
                        resp = responseVector[l].getResponseAt(j);

                        //initialize arrays
                        if(l==0){
                            mPerson[j] = new Mean();
                            sdPerson[j] = new StandardDeviation();
                        }

                        if(j==0){
                            mItem[l] = new Mean();
                            sdItem[l] = new StandardDeviation();
                        }

                        //increment item and person summary statistics
                        if(resp!=-1){
                            //incorporate weights - crude workaround
                            for(int w=0;w<freq;w++){
                                mItem[l].increment(irm[j].getDifficulty());
                                sdItem[l].increment(irm[j].getDifficulty());

                                mPerson[j].increment(theta[l]);
                                sdPerson[j].increment(theta[l]);
                                Si[j] += resp;//TODO compute Si from the expected values of the irm to allow for missing data.
                                Ni[j]++;
                            }

                        }
                    }
                }//end item loop

            }//end summary loop

            //Compute item PROX for binary items only
            iMean = 0;
            double pSd = 1e-8;
            double ni = 0;
            for(int j=0;j<nItems;j++){
                if(irm[j].getType()==IrmType.L3){
                    pSd = sdPerson[j].getResult();

                    //adjust extreme person scores
                    if(Si[j]==0) Si[j]+=0.3;
                    if(Si[j]==Ni[j]) Si[j]-=0.3;

                    logit = Math.log(Si[j]/(Ni[j]-Si[j]));
                    iProx = mPerson[j].getResult()-Math.sqrt(1.0+pSd/2.9)*logit;
                    irm[j].setDifficulty(iProx);
                    iMean += iProx;
                    ni++;
                }
            }
            iMean /= ni;

            //center difficulties about the mean item difficulty
            for(int j=0;j<nItems;j++){
                if(irm[j].getType()==IrmType.L3){
                    iProx = irm[j].getDifficulty();
                    irm[j].setDifficulty(iProx-iMean);
                }
            }

            //Compute person PROX
            maxChange = 0;
            personGrandMean.clear();
            personGrandSd.clear();
            Pair<Double, Double> personScores = null;
            for(int l=0;l<nResponseVectors;l++){
                personScores = computePersonScores(responseVector[l]);
                pScore = personScores.getFirst();
                maxTestScore = personScores.getSecond();

                //adjust extreme person scores
                if(pScore==0) pScore+=0.3;
                if(pScore==maxTestScore) pScore-=0.3;

                logit = Math.log(pScore/(maxTestScore-pScore));
                pProx = mItem[l].getResult() + Math.sqrt(1.0+sdItem[l].getResult()/2.9)*logit;
                maxChange = Math.max(maxChange, Math.abs(theta[l]-pProx));
                theta[l] = pProx;
                personGrandMean.increment(pProx);
                personGrandSd.increment(pProx);
            }

            delta = maxChange;
            iter++;

        }//end while

        //Linearly transform theta estimate to have a mean of 0 and a standard deviation of 1.
        //Apply the same transformation to item difficulty values.
        double A = 1.0/personGrandSd.getResult();
        double B = -A*personGrandMean.getResult();

        for(int l=0;l<nResponseVectors;l++){
            theta[l] = theta[l]*A+B;
        }

        double a = 1;
        double b = 0;
        for(int j=0;j<nItems;j++){
            if(irm[j].getType()==IrmType.L3){
                b = irm[j].getDifficulty();
                irm[j].setDifficulty(b*A+B);

                //Adjust discrimination parameter for scaling constant.
                //PROX assumes a logit scale. This conversion is to convert to the normal metric.
                a = irm[j].getDiscrimination();
                irm[j].setDiscrimination(a/irm[j].getScalingConstant());
            }            
        }

        //For debugging
//        System.out.println("ITER: " + iter);
//        for(int j=0;j<nItems;j++){
//            System.out.println("PROX: " + irm[j].toString());
//        }

    }


}

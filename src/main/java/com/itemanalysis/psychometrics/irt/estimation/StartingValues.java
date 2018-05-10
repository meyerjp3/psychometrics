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

import com.itemanalysis.psychometrics.quadrature.ContinuousQuadratureRule;
import com.itemanalysis.psychometrics.histogram.*;
import com.itemanalysis.psychometrics.irt.model.IrmType;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.uncmin.DefaultUncminOptimizer;
import com.itemanalysis.psychometrics.uncmin.UncminException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;

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
    private int[] ncat = null;
    private double[] theta = null;
    private Histogram hist = null;
    private EstepEstimates estepEstimates = null;
    private int binaryItemCount = 0;
    private ArrayList<EMStatusListener> emStatusListeners = new ArrayList<EMStatusListener>();

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

        ncat = new int[nItems];
        for(int j=0;j<nItems;j++){
            ncat[j] = irm[j].getNcat();
            if(ncat[j]==2) binaryItemCount++;
        }
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
        if(binaryItemCount==0) return irm;//DO not compute prox estimates if data only have polytomous items

        DefaultUncminOptimizer optimizer = new DefaultUncminOptimizer();
        ItemLogLikelihood itemLogLikelihood = new ItemLogLikelihood();
        double[] initialValue = null;
        double[] param = null;

        //compute prox estimates of item difficulty and person ability
        prox(0.01, 10);

//        System.out.println("PROX completed");

        //summarize the prox values
        computeEstepEstimates();

//        System.out.println("ESTEP completed");

        //The histogram will return frequencies, but relative frequencies are needed for the latent quadrature.
        //Create a new latent quadrature with relative frequencies.
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule(hist.getNumberOfPoints(), hist.getMinimum(), hist.getMaximum());
        double sum = hist.getSumOfValues();
        for(int k=0;k<hist.getNumberOfPoints();k++){
            latentDistribution.setPointAt(k, hist.getPointAt(k));
            latentDistribution.setDensityAt(k, hist.getDensityAt(k)/sum);
        }

        for(int j=0;j<nItems;j++){

            //Compute start values for binary items only
            if(irm[j].getType()== IrmType.L3 || irm[j].getType()==IrmType.L4){
                int nPar = irm[j].getNumberOfParameters();

                initialValue = irm[j].nonZeroPrior(irm[j].getItemParameterArray());

                try{
                    //Using observed counts, find the item parameters that maximize the marginal likelihood
                    itemLogLikelihood.setModel(irm[j], latentDistribution, estepEstimates.getRjkAt(j), estepEstimates.getNt());
                    optimizer.minimize(itemLogLikelihood, initialValue, true, false, 50, 1);
                    param = optimizer.getParameters();
                }catch(UncminException ex){
                    fireEMStatusEvent("UNCMIN exception: Starting values nonlinear regression failed. Using defaults instead." );
                    ex.printStackTrace();
                }catch(Exception ex){
                    fireEMStatusEvent("UNCMIN exception." );
                    ex.printStackTrace();
                }

                //Make sure start values have nonzero density
                double[] finalStarts = irm[j].nonZeroPrior(param);
                setParameters(j, finalStarts, initialValue);


            }

//            System.out.println("Stating values for: " + irm[j].getName());

        }

        //For debugging
//        for(int j=0;j<nItems;j++){
//            System.out.println("START VALUES: " + irm[j].toString());
//        }

        return irm;

    }

    private void setParameters(int j, double[] param, double[] initialValue){

        int nPar = irm[j].getNumberOfParameters();
        if(nPar==4){
            if(param!=null){
                irm[j].setDiscrimination(param[0]);
                irm[j].setDifficulty(param[1]);
                irm[j].setGuessing(param[2]);
                irm[j].setSlipping(param[3]);
            }else{
                irm[j].setDiscrimination(initialValue[0]);
                irm[j].setDifficulty(initialValue[1]);
                irm[j].setGuessing(0.1);
                irm[j].setSlipping(0.9);
            }

        }else if(nPar==3){
            if(param!=null){
                irm[j].setDiscrimination(param[0]);
                irm[j].setDifficulty(param[1]);
                irm[j].setGuessing(param[2]);
            }else{
                irm[j].setDiscrimination(initialValue[0]);
                irm[j].setDifficulty(initialValue[1]);
                irm[j].setGuessing(0.1);
            }

        }else if(nPar==2){
            if(param!=null){
                irm[j].setDiscrimination(param[0]);
                irm[j].setDifficulty(param[1]);
            }else{
                irm[j].setDiscrimination(initialValue[0]);
                irm[j].setDifficulty(initialValue[1]);
            }

        }else{
            if(param!=null){
                irm[j].setDifficulty(param[0]);
            }else{
                irm[j].setDifficulty(initialValue[0]);
            }
        }

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

        estepEstimates = new EstepEstimates(nItems, ncat, nBins);

        //compute number of examinees at each theta level
        for(int t=0;t<nBins;t++){
            estepEstimates.incrementNt(t, freq[t]);
        }

        int x = 0;
        int value = 0;


        //count number correct at each theta level for each binary item
        for(int l=0;l<nResponseVectors;l++){
            for(int w=0;w<responseVector[l].getFrequency();w++){
                for(int t=0;t<nBins;t++){
                    if(hist.getBinAt(t).inBin(theta[l])){
                        for(int j=0;j<nItems;j++){

                            x = Byte.valueOf(responseVector[l].getResponseAt(j)).intValue();
                            if(x!=-1){
                                if(irm[j].getType()==IrmType.L3 || irm[j].getType()==IrmType.L4){
                                    for(int k=0;k<ncat[j];k++){
                                        value = 0;
                                        if(x==k) value = 1;
                                        estepEstimates.incrementRjkt(j, k, t, value);
                                    }
                                }
                            }


                        }
                    }

                }
            }
        }
        //For debugging
        //System.out.println(estepEstimates.toString());

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
            if(responseVector.getResponseAt(j)!=-1 && (irm[j].getType()==IrmType.L3 || irm[j].getType()==IrmType.L4)){
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

                    //initialize arrays
                    if(l==0){
                        mPerson[j] = new Mean();
                        sdPerson[j] = new StandardDeviation();
                    }

                    if(j==0){
                        mItem[l] = new Mean();
                        sdItem[l] = new StandardDeviation();
                    }

                    if(irm[j].getType()==IrmType.L3 || irm[j].getType()==IrmType.L4){

                        resp = responseVector[l].getResponseAt(j);

                        //increment item and person summary statistics
                        if(resp!=-1){
                            //incorporate weights - crude workaround
                            for(int w=0;w<freq;w++){
                                mItem[l].increment(irm[j].getDifficulty());
                                sdItem[l].increment(irm[j].getDifficulty());

                                mPerson[j].increment(theta[l]);
                                sdPerson[j].increment(theta[l]);
                                Si[j] += resp;
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
                if(irm[j].getType()==IrmType.L3 || irm[j].getType()==IrmType.L4){
                    pSd = sdPerson[j].getResult();

                    //adjust extreme item scores
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
                if(irm[j].getType()==IrmType.L3 || irm[j].getType()==IrmType.L4){
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

                //WARNING: With missing data could have some people with a maxTestScore and pScore of 0.
                //If such a case occurs, the logit wil be Double.NaN.
                logit = Math.log(pScore/(maxTestScore-pScore));
                pProx = mItem[l].getResult() + Math.sqrt(1.0+sdItem[l].getResult()/2.9)*logit;
                maxChange = Math.max(maxChange, Math.abs(theta[l]-pProx));
                theta[l] = pProx;

                if(!Double.isNaN(pProx)){
                    personGrandMean.increment(pProx);
                    personGrandSd.increment(pProx);
                }
            }



            delta = maxChange;
            iter++;

            fireEMStatusEvent(iter, delta, Double.NaN);

        }//end while

        //Linearly transform theta estimate to have a mean of 0 and a standard deviation of 1.
        //Apply the same transformation to item difficulty values.
        double A = 1.0/personGrandSd.getResult();
        if(Double.isNaN(A)) A = 1.0;//Should not happen but this will prevent causing all scores to be Double.NaN
        A = Math.min(0.00001, A); //To prevent division by 0;

        double B = -A*personGrandMean.getResult();



        for(int l=0;l<nResponseVectors;l++){
            theta[l] = theta[l]*A+B;
        }

        double a = 1;
        double b = 0;
        for(int j=0;j<nItems;j++){
            if(irm[j].getType()==IrmType.L3 || irm[j].getType()==IrmType.L3){
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

    public String printItemParameters(){
        String s = "";
        for(int j=0;j<nItems;j++){
           s += irm[j].toString() + "\n";
        }
        return s;
    }

    //=====================================================================================================================
// METHOD FOR HANDLING THE EMSTATUS LISTENER
// These methods have nothnig to do with estimation. They only pertain to the publication of intermediate results.
//=====================================================================================================================

    public void addEMStatusListener(EMStatusListener listener){
        emStatusListeners.add(listener);
    }

    public void removeEMStatusListener(EMStatusListener listener){
        emStatusListeners.remove(listener);
    }

    public void fireEMStatusEvent(String message){
        for(EMStatusListener l : emStatusListeners){
            l.handleEMStatusEvent(new EMStatusEventObject(this, message));
        }
    }

    public void fireEMStatusEvent(int iteration, double delta, double loglikelihood){
        for(EMStatusListener l : emStatusListeners){
            l.handleEMStatusEvent(new EMStatusEventObject(this, "PROX CYCLE: ", iteration, delta, loglikelihood));
        }
    }


}

package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.estimation.IrtObservedScoreDistribution;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import org.apache.commons.math3.util.Precision;

import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedHashMap;

/**
 * Performs IRT Observed Score Equating.
 * For details see:
 *
 * Zeng, L., & Kolen, M. J. (1995). An alternative approach for IRT observed-score equating of
 * number-correct scores. Applied Psychological Measurement, 19 (3), 231-240.
 *
 */
public class IrtObservedScoreEquating implements IrtEquating{

    private LinkedHashMap<VariableName, ItemResponseModel> itemFormX = null;

    private LinkedHashMap<VariableName, ItemResponseModel> itemFormY = null;

    private QuadratureRule latentDistributionFormX = null;

    private QuadratureRule latentDistributionFormY = null;

    private double weightPopulationX = 1.0;

    private double weightPopulationY = 0.0;

    private double[] rawScore = null;

    private double[] yEquivObservedScore = null;

    private double minScoreX = 0.0;

    private double minScoreY = 0.0;

    private double[] formXMoments;

    private double[] formYMoments;

    private IrtObservedScoreDistribution f1 = null;

    private IrtObservedScoreDistribution f2 = null;

    private IrtObservedScoreDistribution g1 = null;

    private IrtObservedScoreDistribution g2 = null;

    private double[] synthetic1 = null;

    private double[] synthetic2 = null;

    public IrtObservedScoreEquating(LinkedHashMap<VariableName, ItemResponseModel> itemFormX, QuadratureRule latentDistributionFormX,
                                    LinkedHashMap<VariableName, ItemResponseModel> itemFormY, QuadratureRule latentDistributionFormY,
                                    double weightPopulationX){

        this.itemFormX = itemFormX;
        this.latentDistributionFormX = latentDistributionFormX;
        this.itemFormY = itemFormY;
        this.latentDistributionFormY = latentDistributionFormY;
        this.weightPopulationX = weightPopulationX;
        this.weightPopulationY = 1.0-weightPopulationX;
    }

    public IrtObservedScoreEquating(ItemResponseModel[] itemFormX, QuadratureRule latentDistributionFormX,
                                    ItemResponseModel[] itemFormY, QuadratureRule latentDistributionFormY,
                                    double weightPopulationX){

        this.itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        for(int i=0;i<itemFormX.length;i++){
            this.itemFormX.put(new VariableName("item"+(i+1)), itemFormX[i]);
        }
        this.latentDistributionFormX = latentDistributionFormX;

        this.itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();
        for(int i=0;i<itemFormY.length;i++){
            this.itemFormY.put(new VariableName("item"+(i+1)), itemFormY[i]);
        }

        this.latentDistributionFormY = latentDistributionFormY;
        this.weightPopulationX = weightPopulationX;
        this.weightPopulationY = 1.0-weightPopulationX;

    }


    public void equateScores(){

        //Form X (new form), Group 1 (new group)
        f1 = new IrtObservedScoreDistribution(itemFormX, latentDistributionFormX);
        f1.compute();
        int nScoresX = f1.getNumberOfScores();

        //Form X (new form), Group 2 (old group)
        f2 = new IrtObservedScoreDistribution(itemFormX, latentDistributionFormY);
        f2.compute();

        synthetic1 = new double[nScoresX];
        for(int i=0;i<nScoresX;i++){
            synthetic1[i] = weightPopulationX*f1.getDensityAt(i) + weightPopulationY*f2.getDensityAt(i);
        }

        //Form Y (old form), Group 1 (new group)
        g1 = new IrtObservedScoreDistribution(itemFormY, latentDistributionFormX);
        g1.compute();
        int nScoresY = g1.getNumberOfScores();

        //Form Y (old form), Group 2 (old group)
        g2 = new IrtObservedScoreDistribution(itemFormY, latentDistributionFormY);
        g2.compute();

        synthetic2 = new double[nScoresY];
        for(int i=0;i<nScoresY;i++){
            synthetic2[i] = weightPopulationX*g1.getDensityAt(i) + weightPopulationY*g2.getDensityAt(i);
        }

        //Form X CDF
        double[] cdfX = new double[nScoresX];
        double[] prx = new double[nScoresX];
        rawScore = new double[nScoresX];

        //initialize first element
        cdfX[0] = synthetic1[0];

        //compute CDF
        for(int i=1;i<nScoresX;i++){
            cdfX[i] = cdfX[i-1] + synthetic1[i];
        }

        //Find form X percentile ranks
        for(int i=0;i<prx.length;i++){
            rawScore[i] = i;
            prx[i] = percentileRank(minScoreX, nScoresX, 1, cdfX, i);
        }


        //Form Y CDF
        double[] cdfY = new double[nScoresY];
        cdfY[0] = synthetic2[0];

        for(int i=1;i<nScoresY;i++){
            cdfY[i] = cdfY[i-1] + synthetic2[i];
        }

        //Compute Form X moments
        formXMoments = computeMoments(synthetic1);

        //Compute Form Y moments
        formYMoments = computeMoments(synthetic2);

        //Conduct equipercentile equating of IRT observed scores
        yEquivObservedScore = equipercentileEquating(nScoresY, minScoreY, 1.0, cdfY, nScoresX, prx);

    }

    /**
     * Computes first four moments of the diven distribution. Assumes that scores corresponding to
     * densities in dist start at 0. For example, if dist[] has 4 elements, they are the density values
     * for scores 0, 1, 2, and 3.  Returns an array with values:
     * moment[0] = mean.
     * moment[1] = standard deviation.
     * moment[2] = skewness.
     * moment[4] = kurtosis.
     *
     * @param dist density values.
     * @return moments
     */
    private double[] computeMoments(double[] dist){
        int n = dist.length;
        double var = 0;
        double dev = 0;
        double dev2 = 0;
        double[] moments = new double[4];

        for(int i=0;i<n;i++){
            moments[0] += i*dist[i];
        }

        for(int i=0;i<n;i++){
            dev = i - moments[0];
            dev2 = dev*dev;
            var += dev2*dist[i];
            dev *= dev2*dist[i];
            moments[2] += dev;
            dev2 = dev2*dev2*dist[i];
            moments[3] += dev2;
        }

        moments[1] = Math.sqrt(var);
        var *= moments[1];
        moments[2] = moments[2] / var;
        var *= moments[1];
        moments[3] = moments[3] / var;

        return moments;
    }

    public double[] getYEquivalentScores(){
        return yEquivObservedScore;
    }

    public double getYEquivalentObservedScoreAt(int score){
        if(score<rawScore[0]) return yEquivObservedScore[0];
        if(score>rawScore[rawScore.length-1]) return yEquivObservedScore[rawScore.length-1];
        return yEquivObservedScore[score];
    }

    public double getFormXPopulatinWeight(){
        return weightPopulationX;
    }

    public double getFormYPopulationWeight(){
        return weightPopulationY;
    }

    public double getFormXMean(){
        return formXMoments[0];
    }

    public double getFormXStandardDeviation(){
        return formXMoments[1];
    }

    public double getFormXSkewness(){
        return formXMoments[2];
    }

    public double getFormXKurtosis(){
        return formXMoments[3];
    }

    public double getFormYMean(){
        return formYMoments[0];
    }

    public double getFormYStandardDeviation(){
        return formYMoments[1];
    }

    public double getFormYSkewness(){
        return formYMoments[2];
    }

    public double getFormYKurtosis(){
        return formYMoments[3];
    }

    public double[] getFormXScores(){
        return rawScore;
    }


    public double[] getFormXThetaValues(){
        //Not implemented
        return null;
    }

    /**
     * Not really necessary here. Only implemented because required by interface.
     *
     * @return array
     */
    public char[] getStatus(){
        char[] status = new char[rawScore.length];
        Arrays.fill(status, 'Y');
        return status;
    }

    /**
     * Computes percentile ranks from a cumulative relative frequency distribution.
     * Assumes that crfd is for a discrete distribution.
     *
     * Adapted from Equating Recipes.
     * Center for Advanced Studies in Measurement and Assessment (CASMA)
     * University of Iowa which is distributed uner GNU LGPL
     *
     * Formula used is the analogue of Equation 2.14 in Kolen nd Brennan (2004)
     * for the more general case of x scores being any real numbers
     * differing by a constant amount (inc) and ranging from min to max
     *
     * In effect, the inverse of this function is the percentilePoint() function.
     *
     * @param min minimum possible score
     * @param max maximum possible score
     * @param inc increment
     * @param x score for which percentile rank is sought
     * @return percentile rank
     */
    private double percentileRank(double min, double max, double inc, double[] crfd, double x){
        int i = 0;
        double pr = 0;
        double xstar = 0;
        double loc = ((x-min)/inc + 0.5);

        if (x < min-inc/2){
            pr = 0.0;
        }else if (x < min + inc/2){
            pr = 100*((x - (min - inc/2))/inc)*crfd[0];
        }else if (x >= max + inc/2){
            pr = 100.0;
        }else{
            for(i=1;i<=loc;i++){
                xstar =  min + i*inc;
                if (x < xstar + inc/2) break;
            }
            pr = 100*(crfd[i-1] + ((x - (xstar - inc/2))/inc)*(crfd[i] - crfd[i-1]));
        }

        return pr;
    }


    /**
     * Computes percentile point at a given percentile rank (i.e. inverse of percentile rank).
     * Assumes that crfd is for a discrete distribution.
     *
     * Adapted from Equating Recipes.
     * Center for Advanced Studies in Measurement and Assessment (CASMA)
     * University of Iowa which is distributed uner GNU LGPL
     *
     * @param ns Number of raw score categories
     * @param min Minimum raw score
     * @param inc Increment
     * @param crfd Cumulative relative frequency distribution
     * @param pr Percentile rank (0 <= pr <= 100)
     * @return percentile point for a given pr.
     */
    private double percentilePoint(int ns, double min, double inc, double[] crfd, double pr){

        //Express as proportion
        double prp = pr/100;

        //"upper" perc point on scale [-.5, (ns-1)+.5]
        double ppU = 0;

        //"lower" perc point on scale [-.5, (ns-1)+.5]
        double ppL = 0;

        //i =  x*_U = smallest integer such that crfd[i] > prp
        int i = 0;

        //j = x*_L = largest integer such that crfd[j] < prp
        int j = 0;

        /**
         * Special case: PR=0 and 0 freqs at bottom of rfd[].
         * First line of code means that prp <= 1.0e-8
         * is an operational definition of prp==0.  Remaining code is
         * to handle possibility of 0 freq's at bottom of rfd[].
         * Note that for loop starts at 0
         */
        if(prp <= 1.0e-8){
            ppL = -.5;
            for(i=0;i<=ns-1;i++) if(crfd[i] > 1.0e-8) break;
            ppU = i - .5;
            return  min + inc*((ppU + ppL)/2);
        }

        /**
         * Special case: PR=1 and 0 freqs at top of rfd[].
         * First line of code means that 1 - 1.0e-8 is
         * an operational definition of prp==1.  Remaining code is
         * to handle possibility of 0 freq's at top of rfd[].
         * Not that for loop starts at ns-1
         */
        if(prp >= 1. - 1.0e-8){
            ppU = ns -.5;
            for(j=ns-1;j>=0;j--) if(crfd[j] < 1. - 1.0e-8) break;
            ppL = 1 + j + .5;
            return  min + inc*((ppU + ppL)/2);
        }


        /**
         * Special case:  crfd[0] > prp can happen in equipercentile
         * equating. If this occurs, then we have the following anomalous
         * circumstances:
         * (a) x*_U = 0 by defn on p. 45, in which case
         * crfd[x*_U - 1] = crfd[-1] which is undefined; and
         * (b) x*_L is non-existent by defn on p. 45.
         * Next two lines of code are consistent with graphical
         * procedures in Kolen and Brennan (sect. 2.5.1).
         */
        if(crfd[0] > prp)
            return min + inc*(prp/crfd[0] -.5);


        //majority of work occurs next

        /**
         * upper pp -- get x*_U
         */
        for(i=1;i<=ns-1;i++) if(crfd[i] > prp) break;
        if(crfd[i] != crfd[i-1])
            ppU =  (prp - crfd[i-1])/(crfd[i] - crfd[i-1]) + (i - .5);
        else
            ppU = i - .5;

        /**
         * lower pp -- get x*_L
         */
        for(j=ns-2;j>=0;j--) if(crfd[j] < prp) break;
        if(crfd[j+1] != crfd[j])
            ppL = (prp - crfd[j])/(crfd[j+1] - crfd[j]) + (j + .5);
        else
            ppL = j + .5;

        //return area
        return min + inc*((ppU + ppL)/2);
    }

    /**
     *
     * Computes equipercentile equivalents on scale of y for percentile
     * ranks on scale of x. See comments in perc_point() for details.
     *
     * Adapted from Equating Recipes.
     * Center for Advanced Studies in Measurement and Assessment (CASMA)
     * University of Iowa which is distributed uner GNU LGPL
     *
     * @param nsy Number of raw score categories for old form Y
     * @param miny Minimum raw score for old form Y
     * @param incy Increment between consecutive raw scores for old form Y
     * @param crfdy Cumulative rel freq dist for old form Y
     * @param nsx Number of raw score categories for X
     * @param prdx Percentile rank distribution for new form X
     * @return Form Y equipercentile equivalents of new form X scores
     */
    public double[] equipercentileEquating(int nsy, double miny, double incy, double[] crfdy, int nsx, double[] prdx){

        double[] eraw = new double[nsx];

        for(int i=0;i<=nsx-1;i++){
            eraw[i] = percentilePoint(nsy,miny,incy,crfdy,prdx[i]);
        }

        return eraw;
    }

    public String printFormXDistribution(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double[] f1density = f1.getDensity();
        double[] f1Moments = computeMoments(f1density);

        double[] f2density = f2.getDensity();
        double[] f2Moments = computeMoments(f2density);

        double[] synMoments = computeMoments(synthetic1);


        f.format("%-48s", "Form X (New Form) IRT Summed Score Density"); f.format("%n");
        f.format("%48s", "================================================"); f.format("%n");
        f.format("%10s", "Raw Score"); f.format("%2s", "  ");
        f.format("%10s", "Group 1"); f.format("%2s", "  ");
        f.format("%10s", "Group 2"); f.format("%2s", "  ");
        f.format("%10s", "Group S"); f.format("%n", "  ");
        f.format("%48s", "------------------------------------------------"); f.format("%n");
        for(int i=0;i<rawScore.length;i++){
            f.format("%10.5f", rawScore[i]); f.format("%2s", "  ");
            f.format("%10.5f", f1.getDensityAt(i)); f.format("%2s", "  ");
            f.format("%10.5f", f2.getDensityAt(i)); f.format("%2s", "  ");
            f.format("%10.5f", synthetic1[i]); f.format("%2s", "  ");
            f.format("%n");
        }
        f.format("%n");
        f.format("%10s", "Mean"); f.format("%2s", "  ");
        f.format("%10.4f", f1Moments[0]); f.format("%2s", "  ");
        f.format("%10.4f", f2Moments[0]); f.format("%2s", "  ");
        f.format("%10.4f", synMoments[0]);
        f.format("%n");

        f.format("%10s", "S.D."); f.format("%2s", "  ");
        f.format("%10.4f", f1Moments[1]); f.format("%2s", "  ");
        f.format("%10.4f", f2Moments[1]); f.format("%2s", "  ");
        f.format("%10.4f", synMoments[1]);
        f.format("%n");

        f.format("%10s", "Skewness"); f.format("%2s", "  ");
        f.format("%10.4f", f1Moments[2]); f.format("%2s", "  ");
        f.format("%10.4f", f2Moments[2]); f.format("%2s", "  ");
        f.format("%10.4f", synMoments[2]);
        f.format("%n");

        f.format("%10s", "Kurtosis"); f.format("%2s", "  ");
        f.format("%10.4f", f1Moments[3]); f.format("%2s", "  ");
        f.format("%10.4f", f2Moments[3]); f.format("%2s", "  ");
        f.format("%10.4f", synMoments[3]);
        f.format("%n");
        f.format("%48s", "================================================"); f.format("%n");

        return f.toString();
    }

    public String printFormYDistribution(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        double[] g1density = g1.getDensity();
        double[] g1Moments = computeMoments(g1density);

        double[] g2density = g2.getDensity();
        double[] g2Moments = computeMoments(g2density);

        double[] synMoments = computeMoments(synthetic2);

        f.format("%-48s", "Form Y (Old Form) IRT Summed Score Density"); f.format("%n");
        f.format("%48s", "================================================"); f.format("%n");
        f.format("%10s", "Raw Score"); f.format("%2s", "  ");
        f.format("%10s", "Group 1"); f.format("%2s", "  ");
        f.format("%10s", "Group 2"); f.format("%2s", "  ");
        f.format("%10s", "Group S"); f.format("%n", "  ");
        f.format("%48s", "------------------------------------------------"); f.format("%n");
        for(int i=0;i<rawScore.length;i++){
            f.format("%10.5f", rawScore[i]); f.format("%2s", "  ");
            f.format("%10.5f", g1.getDensityAt(i)); f.format("%2s", "  ");
            f.format("%10.5f", g2.getDensityAt(i)); f.format("%2s", "  ");
            f.format("%10.5f", synthetic2[i]); f.format("%2s", "  ");
            f.format("%n");
        }
        f.format("%n");
        f.format("%10s", "Mean"); f.format("%2s", "  ");
        f.format("%10.4f", g1Moments[0]); f.format("%2s", "  ");
        f.format("%10.4f", g2Moments[0]); f.format("%2s", "  ");
        f.format("%10.4f", synMoments[0]);
        f.format("%n");

        f.format("%10s", "S.D."); f.format("%2s", "  ");
        f.format("%10.4f", g1Moments[1]); f.format("%2s", "  ");
        f.format("%10.4f", g2Moments[1]); f.format("%2s", "  ");
        f.format("%10.4f", synMoments[1]);
        f.format("%n");

        f.format("%10s", "Skewness"); f.format("%2s", "  ");
        f.format("%10.4f", g1Moments[2]); f.format("%2s", "  ");
        f.format("%10.4f", g2Moments[2]); f.format("%2s", "  ");
        f.format("%10.4f", synMoments[2]);
        f.format("%n");

        f.format("%10s", "Kurtosis"); f.format("%2s", "  ");
        f.format("%10.4f", g1Moments[3]); f.format("%2s", "  ");
        f.format("%10.4f", g2Moments[3]); f.format("%2s", "  ");
        f.format("%10.4f", synMoments[3]);
        f.format("%n");
        f.format("%48s", "================================================"); f.format("%n");

        return f.toString();
    }

    public String printDistributionSummary(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%30s", "Synthetic Distribution Moments");f.format("%n");
        f.format("%23s", "(Form X Group Weight = "); f.format("%10.4f)", weightPopulationX);f.format("%n");
        f.format("%34s", "==================================");f.format("%n");
        f.format("%10s", "Statistic"); f.format("%2s", "  "); f.format("%10s", "Form X");f.format("%2s", "  "); f.format("%10s", "Form Y");f.format("%n");
        f.format("%34s", "----------------------------------");f.format("%n");
        f.format("%10s", "Mean"); f.format("%2s", "  "); f.format("%10.4f", getFormXMean()); f.format("%2s", "  "); f.format("%10.4f", getFormYMean()); f.format("%n");
        f.format("%10s", "S.D."); f.format("%2s", "  "); f.format("%10.4f", getFormXStandardDeviation()); f.format("%2s", "  "); f.format("%10.4f", getFormYStandardDeviation()); f.format("%n");
        f.format("%10s", "Skewness"); f.format("%2s", "  "); f.format("%10.4f", getFormXSkewness()); f.format("%2s", "  "); f.format("%10.4f", getFormYSkewness()); f.format("%n");
        f.format("%10s", "Kurtosis"); f.format("%2s", "  "); f.format("%10.4f", getFormXKurtosis()); f.format("%2s", "  "); f.format("%10.4f", getFormYKurtosis()); f.format("%n");
        f.format("%34s", "==================================");f.format("%n");

        return f.toString();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-28s", "IRT Observed Score Equating");f.format("%n");
        f.format("%-28s", "(Form X Equated to Form Y)");f.format("%n");
        f.format("%38s", "======================================");f.format("%n");
        f.format("%38s", "Rounded");f.format("%n");
        f.format("%10s", "Raw Score"); f.format("%2s", "  ");
        f.format("%12s", "Y-Equivalent"); f.format("%2s", "  ");
        f.format("%12s", "Y-Equivalent"); f.format("%n");
        f.format("%38s", "--------------------------------------");f.format("%n");

        for(int i=0;i<rawScore.length;i++){
            f.format("%10.4f", rawScore[i]); f.format("%4s", "  ");
            f.format("%10.4f", yEquivObservedScore[i]);f.format("%4s", "  ");
            f.format("%10.4f", Precision.round(yEquivObservedScore[i], 0));f.format("%n");
        }

        f.format("%38s", "======================================");f.format("%n");

        return f.toString();

    }

}

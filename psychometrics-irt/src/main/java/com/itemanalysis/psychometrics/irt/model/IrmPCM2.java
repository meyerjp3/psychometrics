package com.itemanalysis.psychometrics.irt.model;

import com.itemanalysis.psychometrics.irt.estimation.ItemParamPrior;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.Formatter;

/**
 * Class that implements the partial credit model. This version is different from {@link com.itemanalysis.psychometrics.irt.model.IrmPCM}
 * in that it uses step parameters only. It does not use the difficulty plus threshold parameters.
 *
 */
public class IrmPCM2 extends AbstractItemResponseModel  {

    private double D = 1.7;
    private double[] step;
    private double[] proposalStep;
    private double[] stepStdError;
    private ItemParamPrior[] stepPrior = null;

    /**
     * Default constructor for an m category item.
     *
     * @param step an array of m step parameters. The first step parameter should be fixed to 0.
     * @param D scaling constant tha is either 1 or 1.7 (or 1.712)
     */
    public IrmPCM2(double[] step, double D){
        ncat = step.length;
        ncatM1 = ncat-1;
        this.step = step;
//        this.proposalStep = step;
        this.stepStdError = new double[ncat];
        this.D = D;

        maxCategory = ncat;
        defaultScoreWeights();
        stepPrior = new ItemParamPrior[ncat];
    }

    /**
     * Computes the probability of responding in category k using item parameters passed to the method using the
     * iparam argument. It does NOT use the item parameters stored in the object.
     *
     * @param theta person ability parameter
     * @param iparam an array of all item parameters. The order is [0] discrimination parameter,
     *               [1:length] array of step parameters.
     * @param category response category for which probability is sought.
     * @param D scaling constant tha is either 1 or 1.7
     * @return probability of a response
     */
    public double probability(double theta, double[] iparam, int category, double D){
        double t = numer(theta, iparam, category, D);
        double b = denom(theta, iparam, D);
        return t/b;
    }

    /**
     * Computes probability of a response using parameters stored in the object.
     *
     * @param theta person ability parameter
     * @param category response category for which probability is sought.
     * @return probability of a response
     */
    public double probability(double theta, int category){
        double t = numer(theta, category);
        double b = denom(theta);
        return t/b;
    }

    /**
     * Computes the expected value using parameters stored in the object.
     *
     * @param theta person ability value
     * @return expected item response
     */
    public double expectedValue(double theta){
        double ev = 0;
        for(int i=0;i<ncat;i++){
            ev += scoreWeight[i]*probability(theta, i);
        }
        return ev;
    }

    /**
     * Compute probability of scoring at or above a response category.
     *
     * @param theta a person ability value
     * @param category response category
     * @return
     */
    public double cumulativeProbability(double theta, int category){
        if(category==0){
            return this.probability(theta, 0);
        }else{
            double cp = 0;
            for(int k=category;k<ncat;k++){
                cp += this.probability(theta, k);
            }
            return cp;
        }
    }

    /**
     * Computes the numerator of the item response model. This method is used internally for the computation
     * of the probability of an item response. It uses item parameter values passed in the iparam argument.
     * It does NOT use item parameter values stored in the object.
     *
     * @param theta person ability value
     * @param iparam item parameter array. The order is iparam[0] = discrimination, iparam[1] = step1 (fixed to zero),
     *               iparam[2] = step 2, iparam[3] = step 3, ..., iparam[m+1] = step m.
     * @param category response category.
     * @param D scaling constant that is either 1 or 1.7
     * @return numerator value of the item response model.
     */
    private double numer(double theta, double[] iparam, int category, double D){
        double Zk = 0;
        for(int k=0; k<=category; k++){
            Zk += D*(theta-iparam[k]);
        }
        return Math.exp(Zk);
    }

    /**
     * Computes the numerator of the item response model. This method is used internally for the computation
     * of the probability of an item response. It uses item parameter values stored in the object.
     *
     * @param theta person ability value.
     * @param category response category.
     * @return
     */
    private double numer(double theta, int category){
        double Zk = 0;
        for(int k=0; k<=category; k++){
            Zk += D*(theta-step[k]);
        }
        return Math.exp(Zk);
    }

    /**
     * Computes the denominator of the item response model. This method is used internally for the computation
     * of the probability of an item response. It uses item parameter values passed in the iparam argument.
     * It does NOT use item parameter values stored in the object.
     *
     * @param theta person ability values.
     * @param iparam item parameter array. The order is iparam[0] = discrimination, iparam[1] = step1 (fixed to zero),
     *               iparam[2] = step 2, iparam[3] = step 3, ..., iparam[m+1] = step m.
     * @param D scaling constant that is either 1 or 1.7
     * @return denominator value of the item response model.
     */
    private double denom(double theta, double[] iparam, double D){
        double denom = 0.0;
        double expZk = 0.0;

        for(int k=0;k<ncat;k++){
            expZk = numer(theta, iparam, k, D);
            denom += expZk;
        }
        return denom;
    }

    /**
     * Computes the denominator of the item response model. This method is used internally for the computation
     * of the probability of an item response. It uses item parameter values stored in the object.
     *
     * @param theta person ability value.
     * @return denominator value of the item response model.
     */
    private double denom(double theta){
        double denom = 0.0;
        double expZk = 0.0;

        for(int k=0;k<ncat;k++){
            expZk = numer(theta, k);
            denom += expZk;
        }
        return denom;
    }

    /**
     * Gradient of item response model with respect to (wrt) item parameters. The response categories are
     * indexed k = 0, 1, 2, ..., m. This method computes the gradientAt using values using item parameter
     * values stored in the object.
     *
     * @param theta person ability value
     * @param category category for which the gradientAt is sought.
     * @return gradientAt of response model wrt item parameters
     */
    public double[] gradient(double theta, int category){
        double[] iparam = new double[getNumberOfParameters()];

        for(int k=0;k<ncat;k++){
            iparam[k] = step[k];
        }
        return gradient(theta, iparam, category, D);
    }

    /**
     * Gradient of item response model with respect to (wrt) item parameters.
     * The response categories are indexed k = 0, 1, 2, ..., m. This method computes the gradientAt using values
     * passed in the iparam argument. It does NOT use the stored item parameter values.
     *
     * @param theta person ability value
     * @param iparam array of item parameters. The order is iparam[0] = discrimination,
     *        iparam[1] = step1 (fixed to zero), iparam[2] = step 2, iparam[3] = step 3, ..., iparam[m+1] = step m.
     * @param k zero based index of the response category i.e. k = 0, 1, 2, ..., m.
     * @return gradientAt of response model wrt item parameters
     */
    public double[] gradient(double theta, double[] iparam, int k, double D){
        int nPar = iparam.length;
        int ncat = nPar;

        int ncatM1 = ncat-1;
        double[] grad = new double[nPar];
        double[] fk = new double[ncat];
        double g = 0;

        //Compute numerator values of irm and denominator of irm
        for(int i=0;i<ncat;i++){
            fk[i] = numer(theta, iparam, i, D);
            g += fk[i];
        }
        double g2 = g*g;

        double bsum = 0;
        double dif = 0;
        double p1 = 0;
        double expP1 = 0;
        double[] db = new double[ncat];//Holds first derivatives of response model numerator wrt steps.

        //Compute first derivative of numerator of response model wrt steps (db).
        for(int kk=0;kk<ncat;kk++){
            bsum = 0;
            for(int j=0;j<=kk;j++){
                bsum += iparam[j];
            }
            dif = (kk+1)*theta-bsum;
            p1 = D*(dif);
            expP1 = Math.exp(p1);
            db[kk] = -D*expP1;
        }

        //First partial derivatives wrt step parameters
        double gPrimeBkSum = 0;
        double pd = 0;
        for(int i=ncatM1; i>-1;i--){//Go backwards to avoid repetitive sums.
            gPrimeBkSum += db[i];
            pd = 0;
            if(i<=k) pd = db[k];
            grad[i] = (g*pd - gPrimeBkSum*fk[k])/g2;
        }

        return grad;
    }

    /**
     * Computation needed for derivTheta()
     *
     * @param theta person ability value
     * @return
     */
    private double denomDerivTheta(double theta){
        double denom = 0.0;
        double expZk = 0.0;

        for(int k=0;k<ncat;k++){
            expZk = numer(theta, k);
            denom += expZk*(1.0+k);
        }
        return denom;
    }

    /**
     * First derivative of item response model with respect to theta.
     *
     * @param theta a person ability value.
     * @return first derivative
     */
    public double derivTheta(double theta){
        double denom = denom(theta);
        double denom2 = denom*denom;
        double denomDeriv = denomDerivTheta(theta);
        double numer = 0.0;
        double p1 = 0.0;
        double p2 = 0.0;
        double deriv = 0.0;

        for(int k=0;k<ncat;k++){
            numer = numer(theta, k);
            p1 = (D*numer*(1.0+k))/denom;
            p2 = (numer*denomDeriv)/denom2;
            deriv += scoreWeight[k]*(p1-p2);
        }
        return deriv;
    }

    public double itemInformationAt(double theta){
        double T = 0;
        double prob = 0.0;
        double sum1 = 0.0;
        double sum2 = 0.0;

        for(int i=0;i< ncat;i++){
            prob = probability(theta, i);
            T = scoreWeight[i];
            sum1 += T*T*prob;
            sum2 += T*prob;
        }

        double info = D*D*(sum1 - Math.pow(sum2, 2));
        return info;

    }

//=====================================================================================================================//
// METHODS USED TO INCORPORATE PRIORS INTO MMLE                                                                        //
//=====================================================================================================================//

    public double[] nonZeroPrior(double[] param){
        double[] p = Arrays.copyOf(param, param.length);
        for(int k=1;k<param.length;k++){
            if(stepPrior[k-1]!=null) p[k] = stepPrior[k-1].nearestNonZero(param[k]);
        }
        return p;
    }

    public void setDiscriminationPrior(ItemParamPrior discriminationPrior){

    }

    public void setDifficultyPrior(ItemParamPrior difficultyPrior){

    }

    public void setGuessingPrior(ItemParamPrior guessingPrior){

    }

    public void setSlippingPrior(ItemParamPrior slippingPrior){

    }

    public void setStepPriorAt(ItemParamPrior prior, int k){
        stepPrior[k] = prior;
    }

    public double addPriorsToLogLikelihood(double ll, double[] iparam){
        return ll;
    }

    public double[] addPriorsToLogLikelihoodGradient(double[] loglikegrad, double[] iparam){
        int ncat = iparam.length;

        double[] llg = loglikegrad;
        for(int k=0;k<ncat;k++){
            if(stepPrior[k]!=null){
                llg[k] -= stepPrior[k].logDensityDeriv1(iparam[k]);
            }
        }
        return llg;
    }

//=====================================================================================================================//
// METHODS USED IN IRT LINKING AND EQUATING                                                                            //
//=====================================================================================================================//

    public void incrementMeanSigma(Mean mean, StandardDeviation sd){
        for(int i=1;i<ncat;i++){//Start at 1 because first step is fixed to zero. Do not count it here.
            mean.increment(step[i]);
            sd.increment(step[i]);
        }
    }

    public void incrementMeanMean(Mean meanDiscrimination, Mean meanDifficulty){
        meanDiscrimination.increment(1.0);
        for(int i=1;i<ncat;i++){//Start at 1 because first step is fixed to zero. Do not count it here.
            meanDifficulty.increment(step[i]);
        }
    }

    /**
     * Computes a linear transformation of item parameters.
     *
     * @param intercept intercept transformation coefficient.
     * @param slope slope transformation coefficient.
     */
    public void scale(double intercept, double slope){
        if(isFixed) return;//DO NOT transform the item parameters when they are fixed
        for(int k=1;k<ncat;k++){//start at 1 because first step is fixed to zero. Do not rescale it.
            step[k] = step[k]*slope + intercept;
            stepStdError[k] = stepStdError[k]*slope;
        }
    }

    /**
     * Returns the probability of a response with a linear transformation of the parameters.
     * This transformation is such that Form X (New Form) is transformed to the scale of Form Y
     * (Old Form). It implements the backwards (New to Old) transformation as described in Kim
     * and Kolen.
     *
     * @param theta examinee proficiency parameter
     * @param category item response
     * @param intercept intercept coefficient of linear transformation
     * @param slope slope (i.e. scale) parameter of the linear transformation
     * @return probability of a response at values of linearly transformed item parameters
     */
    public double tStarProbability(double theta, int category, double intercept, double slope){
        if(category> maxCategory || category<minCategory) return 0;

        double[] iparam = new double[getNumberOfParameters()];
        for(int i=0;i<step.length;i++){
            if(i==0){
                iparam[i] = step[i];//first step fixed to zero and not transformed
            }else{
                iparam[i] = step[i]*slope+intercept;
            }
        }
        return probability(theta, iparam, category, D);
    }

    /**
     * Computes the expected value using parameters stored in the object. This expected value uses
     * a linear transformation of the Form X (New Form) values to the Form Y (Old Form) scale.
     *
     * @param theta person ability value.
     * @return expected value of a response
     */
    public double tStarExpectedValue(double theta, double intercept, double slope){
        double ev = 0;
        for(int i=0;i< ncat;i++){
            ev += scoreWeight[i]*tStarProbability(theta, i, intercept, slope);
        }
        return ev;
    }

    /**
     * Returns the probability of a response with a linear transformation of the parameters.
     * This transformation is such that Form Y (Old Form) is transformed to the scale of Form X
     * (New Form). It implements the forward (Old to New) transformation as described in Kim
     * and Kolen.
     *
     * @param theta examinee proficiency value
     * @param category item response
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return probability of a response at values of linearly transformed item parameters
     */
    public double tSharpProbability(double theta, int category, double intercept, double slope){
        if(category> maxCategory || category<minCategory) return 0;

        double[] iparam = new double[getNumberOfParameters()];
        for(int i=0;i<step.length;i++){
            if(i==0){
                iparam[i] = step[i];//first step fixed to zero and not transformed
            }else{
                iparam[i] = (step[i]-intercept)/slope;
            }

        }
        return probability(theta, iparam, category, D);
    }

    /**
     * Computes the expected value using parameters stored in the object. This expected value uses
     * a linear transformation of the Form Y (Old Form) values to the Form X (New Form) scale.
     *
     * @param theta examinee proficiency value
     * @param intercept linking coefficient for intercept
     * @param slope linking coefficient for slope
     * @return
     */
    public double tSharpExpectedValue(double theta, double intercept, double slope){
        double ev = 0;
        for(int i=0;i< ncat;i++){
            ev += scoreWeight[i]*tSharpProbability(theta, i, intercept, slope);
        }
        return ev;
    }

//=====================================================================================================================//
// GETTER AND SETTER METHODS MAINLY FOR USE WHEN ESTIMATING PARAMETERS                                                 //
//=====================================================================================================================//

    public double[] getItemParameterArray(){
        double[] ip = new double[getNumberOfParameters()];
        for(int k=0;k<ncat;k++){
            ip[k] = step[k];
        }
        return ip;
    }

    public void setStandardErrors(double[] x){
        for(int k=0;k<ncat;k++){
            stepStdError[k] = x[k];
        }
    }

    public IrmType getType(){
        return IrmType.PCM2;
    }

    public int getNumberOfParameters(){
        return ncat;
    }

    public int getNumberOfEstimatedParameters(){
        if(isFixed) return 0;
        return ncat-1;//Do not count first steps which is fixed to zero
    }

    public double getScalingConstant(){
        return D;
    }

    public double getDifficulty(){
        return 0.0;
    }

    public void setDifficulty(double difficulty){

    }

    public double getProposalDifficulty(){
        return 0.0;
    }

    public void setProposalDifficulty(double difficulty){

    }

    public double getDifficultyStdError(){
        return Double.NaN;
    }

    public void setDifficultyStdError(double stdError){

    }

    public double getDiscrimination(){
        return 1.0;
    }

    public void setDiscrimination(double discrimination){

    }

    public void setProposalDiscrimination(double discrimination){

    }

    public double getDiscriminationStdError(){
        return Double.NaN;
    }

    public void setDiscriminationStdError(double stdError){

    }

    public double getGuessing(){
        return Double.NaN;
    }

    public void setGuessing(double guessing){

    }

    public void setProposalGuessing(double guessing){

    }

    public double getGuessingStdError(){
        return Double.NaN;
    }

    public void setGuessingStdError(double stdError){

    }

    public void setSlipping(double slipping){

    }

    public void setProposalSlipping(double slipping){

    }

    public void setSlippingStdError(double slipping){

    }

    public double getSlipping(){
        return Double.NaN;
    }

    public double getSlippingStdError(){
        return Double.NaN;
    }

    public double[] getStepParameters(){
        return step;
    }

    public void setStepParameters(double[] step){
        if(step.length>ncat) throw new IllegalArgumentException("Step parameter array is too large.");
        this.step = step;
    }

    public void setProposalStepParameters(double[] step){
        if(step.length>ncat) throw new IllegalArgumentException("Step parameter array is too large.");
        this.proposalStep = step;
    }

    public double[] getStepStdError(){
        return stepStdError;
    }

    public void setStepStdError(double[] stdError){
        stepStdError = stdError;
    }

    public double[] getThresholdParameters(){
        return step;
    }

    public void setThresholdParameters(double[] thresholds){

    }

    public void setProposalThresholds(double[] thresholds){

    }

    public double[] getThresholdStdError(){
        double[] sp = new double[ncat];
        for(int k=0;k<ncat;k++){
            sp[k] = Double.NaN;
        }
        return sp;
    }

    public void setThresholdStdError(double[] stdError){

    }

    public double acceptAllProposalValues(){
        if(isFixed) return 0;
        double delta = 0;
        double max = 0;
        if(!isFixed){
            for(int m=0;m<ncat;m++){
                delta = Math.abs(this.step[m]-this.proposalStep[m]);
                if(proposalStep[m]>=1) delta /= proposalStep[m];
                max = Math.max(max, delta);
            }
            this.step = this.proposalStep;
        }
        return max;
    }
//=====================================================================================================================//
// END GETTER AND SETTER METHODS                                                                                       //
//=====================================================================================================================//


    /**
     * Displays the item parameter values and standard errors.
     *
     * @return String representation of item parameter values and standard errors.
     */
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        String name = getName().toString();
        if(getName()!=null){
            name = getName().toString().substring(0, Math.min(18, name.length()));
        }else{
            name = "";
        }
        f.format("%-18s", name);f.format("%2s", "");

        f.format("%-3s", "PC4");f.format("%4s", "");

        f.format("%16s", "");

//        f.format("% 4.2f", getDiscrimination()); f.format("%1s", "");
//        f.format("(%4.2f)", getDiscriminationStdError()); f.format("%4s", "");

        double[] step = getStepParameters();
        double[] stepSe = getStepStdError();
        for(int k=1;k<ncat;k++){
            if(k>1) f.format("%43s", "");
            f.format("% 2.2f", step[k]); f.format("%1s", "");
            f.format("(%2.2f)", stepSe[k]);
            if(k<ncatM1) f.format("%n");
        }

        return f.toString();


//        StringBuilder sb = new StringBuilder();
//        Formatter f = new Formatter(sb);
//
//        f.format("%10s", getName().toString());f.format("%2s", ": ");
//        f.format("%1s", "[");
//        for(int k=1;k<ncat;k++){
//            f.format("% .6f", step[k]);//Do not print first step parameter because fixed to zero.
//            if(k<ncatM1) f.format("%2s", ", ");
//        }
//        f.format("%1s", "]");
//        f.format("%n");
//        f.format("%10s", "");f.format("%2s", "");
//        f.format("%1s", "(");
//        for(int k=1;k<ncat;k++){
//            f.format("% .6f", stepStdError[k]);//Do not print first step parameter because fixed to zero.
//            if(k<ncatM1) f.format("%2s", ", ");
//        }
//        f.format("%1s", ")");
//
//        return f.toString();

    }

}

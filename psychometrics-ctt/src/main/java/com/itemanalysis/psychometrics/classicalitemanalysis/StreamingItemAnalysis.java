package com.itemanalysis.psychometrics.classicalitemanalysis;

import com.itemanalysis.psychometrics.statistics.PearsonCorrelation;
import com.itemanalysis.psychometrics.statistics.PolyserialCorrelationPlugin;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A class for calculating classical item statistics. It does not store data in memory. It uses
 * storeless statistics that get updated with each record with a call to either increment() or
 * incrementMissing().
 *
 * You must call either
 * increment() or incrementMissing() for every observation for the calculations
 * to be correct.
 *
 */
public class StreamingItemAnalysis {

    private String itemName = "";
    private boolean allCategories = true;
    private boolean unbiased = false;
    private boolean correctSpuriousness = true;
    private Frequency freq = null;
    private Mean itemMean = null;
    private Mean testScoreMean = null;
    private double minItemScore = Double.MAX_VALUE;
    private double maxItemScore = -Double.MAX_VALUE;
    private StandardDeviation itemStandardDeviation = null;
    private StandardDeviation testScoreStandardDeviation = null;
    private DiscriminationType discriminationType = DiscriminationType.PEARSON;
    private PearsonCorrelation pearsonCorrelation = null;
    private PolyserialCorrelationPlugin polyserialCorrelation = null;
    private HashMap<Object, Mean> selectedMean = null;
    private long missingCount = 0;
    private int precision = 4;

    //flag criteria
    private double lowPvalue = 0.1;
    private double highPvalue = 0.9;
    private double lowDiscrimination = 0.2;
    private double highDiscrimination = 0.8;

    /**
     * Main constructor for the class.
     *
     * @param itemName the name of the item
     * @param allCategories true if will compute statistics for al lresponse options. False if only computing overall item statistics.
     * @param unbiased true is standard deviation and correlation calculations will use n-1. False if these calculations should use n.
     * @param correctSpuriousness true if the item-total and option point-biserial correlations should be corrected for spuriousness.
     */
    public StreamingItemAnalysis(String itemName, boolean allCategories, boolean unbiased, boolean correctSpuriousness, DiscriminationType discriminationType){
        this.itemName = itemName;
        this.allCategories = allCategories;
        this.unbiased = unbiased;
        this.correctSpuriousness = correctSpuriousness;
        freq = new Frequency();
        itemMean = new Mean();
        itemStandardDeviation = new StandardDeviation(unbiased);
        testScoreMean = new Mean();
        testScoreStandardDeviation = new StandardDeviation(unbiased);
        selectedMean = new HashMap<Object, Mean>();

        if(discriminationType==DiscriminationType.POLYSERIAL){
            polyserialCorrelation = new PolyserialCorrelationPlugin();
        }else{
            pearsonCorrelation = new PearsonCorrelation();
        }
    }

    /**
     * Count an item response by incrementing the statistics. Only use this method when data are present.
     * Use when either data are present or when the missing response is scored as zero (prior to calling this method).
     * Use incrementMissing() when the item response is missing and the item score is not provided.
     *
     * @param response an item reponse (may be null if allCategories==false)
     * @param itemScore the numeric value assign to the response
     * @param rawScore the examinee's raw test score
     */
    public void increment(Object response, double itemScore, double rawScore){
        itemMean.increment(itemScore);
        itemStandardDeviation.increment(itemScore);
        testScoreMean.increment(rawScore);
        testScoreStandardDeviation.increment(rawScore);
        minItemScore = Math.min(minItemScore, itemScore);
        maxItemScore = Math.max(maxItemScore, itemScore);

        if(discriminationType==DiscriminationType.PEARSON){
            pearsonCorrelation.increment(rawScore, itemScore);
        }else if(discriminationType==DiscriminationType.POLYSERIAL){
            polyserialCorrelation.increment(rawScore, (int)itemScore);
        }

        if(!allCategories) return;

        //count item responses
        freq.addValue(response.toString());

        //Increment test score mean for group selecting the response
        Mean m1 = selectedMean.get(response);
        if (null == m1) {
            m1 = new Mean();
            selectedMean.put(response, m1);
        }
        m1.increment(rawScore);

    }

    /**
     * Count a missing observation. Use this method when an examinee does not have a response for the item.
     */
    public void incrementMissing(){
        missingCount++;
    }

    /**
     * Set the number of decimal places displyed in the output. Does not affect precision during calculation.
     * Uses the absolute value of the method argument. The value -1 or 1 result in the display of one decimal place.
     *
     * @param precision number of decimal places to display in the output.
     */
    public void setPrecision(int precision){
        this.precision = Math.abs(precision);
    }

    /**
     * Set the lower bound for the item difficulty flag. Item difficulty values as
     * a percent of the observed maximum item score (p-values) below this number will be flagged.
     * Forced to be between 0 and 1.
     *
     * @param lowPvalue item mean expressed as a proportion. Must be between 0 and 1.
     */
    public void setLowPvalue(double lowPvalue){
        this.lowPvalue = Math.min(Math.max(0, lowPvalue), 1.0);
    }

    /**
     * Set the upper bound for the item difficulty flag. Item difficulty values as
     * a percent of the observed maximum item score (p-values) above this number will be flagged.
     * Forced to be between 0 and 1.
     *
     * @param highPvalue item mean expressed as a proportion. Must be between 0 and 1.
     */
    public void setHighPvalue(double highPvalue){
        this.highPvalue = Math.max(Math.min(1, highPvalue), 0.0);
    }

    /**
     * Sets the lower bound for an item discrimination flag. Item-total correlations below this value
     * will be flagged. Forced to be between -1 and 1.
     *
     * @param lowDiscrimination a correlation value between 0 and 1.
     */
    public void setLowDiscrimination(double lowDiscrimination){
        this.lowDiscrimination = Math.min(Math.max(lowDiscrimination, -1.0), 1.0);
    }

    /**
     * Sets the upper bound for an item discrimination flag. Item-total correlations above this value
     * will be flagged. Forced to be between -1 and 1.
     *
     * @param highDiscrimination a correlation value between 0 and 1.
     */
    public void setHighDiscrimination(double highDiscrimination){
        this.highDiscrimination = Math.min(Math.max(highDiscrimination, -1.0), 1.0);
    }

    /**
     * The overall item mean. For a binary item, this value is the p-value. For a polytomous item,
     * it is the item mean.
     *
     * @return item mean
     */
    public double getItemDifficulty(){
        return itemMean.getResult();
    }

    /**
     * The overall item standard deviation.
     *
     * @return standard deviation
     */
    public double getItemStandardDeviation(){
        return itemStandardDeviation.getResult();
    }

    /**
     * The overall item-total correlation, also known as item discrimination.
     *
     * @return the item-total correlation
     */
    public double getItemTotalCorrelation(){
        if(correctSpuriousness){
            if(discriminationType==DiscriminationType.PEARSON){
                return pearsonCorrelation.correctedValue();
            }else{
                return polyserialCorrelation.spuriousCorrectedValue();
            }
        }

        if(discriminationType==DiscriminationType.POLYSERIAL){
            return polyserialCorrelation.value();
        }

        return pearsonCorrelation.value();
    }

    /**
     * Count of examinees selecting the response.
     *
     * @param response the item response
     * @return frequency count of response
     */
    public long getFrequencyAt(Comparable<?> response){
        return freq.getCount(response);
    }

    /**
     * Proportion of examinees responding to the item who selecting the response.
     *
     * @param response the item response
     * @return proportion endorsing the response.
     */
    public double getProportionAt(Comparable<?> response){
        double validCount = getValidCount();
        if(validCount==0) return 0;
        return freq.getCount(response)/validCount;
    }

    /**
     * Point-biserial correlation that describes the relationship between selecting the response
     * and the raw test score. It is the option point-biserial correlation.
     *
     * @param response the item response
     * @return the point-biserial correlation
     */
    public double getPointBiserialAt(Comparable<?> response){

        Mean m1= selectedMean.get(response);
        double n = testScoreMean.getN();
        double n1 = (double)m1.getN();
        double n0 = n-n1;


        double sd = testScoreStandardDeviation.getResult();
        if(sd==0) return Double.NaN;

        double meanRight = m1.getResult();
        double meanWrong = (n*testScoreMean.getResult()-n1*meanRight)/n0;

        double part1 = (meanRight - meanWrong)/sd;
        double part2 = 1;

        if(unbiased){
            part2 = Math.sqrt(n1*n0/(n*(n-1)));
        }else{
            part2 = Math.sqrt(n1*n0/(n*n));
        }

        double ptbis = part1*part2;

        if(correctSpuriousness){
            double p = getProportionAt(response);
            if(p==0) return Double.NaN;
            double itemVar = p*(1.0-p);
            double itemSd = Math.sqrt(itemVar);

            if(unbiased){
                itemVar = itemVar*(n/(n-1));
                itemSd = Math.sqrt(itemVar);
            }

            double testSd = testScoreStandardDeviation.getResult();
            double denom = Math.sqrt(itemVar+testSd*testSd-2.0*ptbis*itemSd*testSd);
            if(denom==0.0) return Double.NaN;
            return (ptbis*testSd-itemSd)/denom;
        }

        return ptbis;
    }

    /**
     * Number of examinees responding to the item.
     *
     * @return valid count
     */
    public long getValidCount(){
        return itemMean.getN();
    }

    /**
     * Number of examinees missing a response to this item.
     *
     * @return missing count
     */
    public long getMissingCount(){
        return missingCount;
    }

    /**
     * The total number of examinees is the valid count plus the missing count.
     *
     * @return total number of examinees in the data file.
     */
    public long getTotalCount(){
        return getValidCount()+getMissingCount();
    }

    /**
     * Number of observed response options.
     *
     * @return number of resposne options.
     */
    public int getNumberOfCategories(){
        if(allCategories) return freq.getUniqueCount();
        return 0;
    }

    /**
     * A flag to indicate whether an item difficulty value is OK (no flag),
     * or is below the lower bound (LP), or above the upper bound (HP).
     * Bounds may be set by the user.
     *
     * @return item difficulty flag.
     */
    public String getDifficultyFlag(){
        double scoreRange = maxItemScore-minItemScore;
        double pval = itemMean.getResult()/scoreRange;//convert to p-value

        if(pval < lowPvalue){
            return "LP ";
        }else if(pval > highPvalue){
            return "HP ";
        }

        return "";
    }

    /**
     * A flag to indicate whether an item discrimination value is OK (no flag),
     * or is below the lower bound (LD), or above the upper bound (HD).
     * Bounds may be set by the user.
     *
     * @return item discrimination flag.
     */
    public String getDiscriminationFlag(){
        double discrim = getItemTotalCorrelation();

        if(discrim < lowDiscrimination){
            return "LD";
        }else if(discrim > highDiscrimination){
            return "HD";
        }
        return "";
    }

    /**
     * Creates a header with labels for displaying output.
     *
     * @return a header
     */
    public String getHeader(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        if(allCategories){
            f.format("%100s", "===================================================================================================="); f.format("%n");
            f.format("%20s", "Item");
            f.format("%12s", "Difficulty");
            f.format("%10s", "Discrim.");
            f.format("%10s", "S.D.");
            f.format("%10s", "Option");
            f.format("%10s", "Count");
            f.format("%10s", "Prop.");
            f.format("%10s", "Pt.Bis.");
            f.format("%8s", "Flags"); f.format("%n");
            f.format("%100s", "----------------------------------------------------------------------------------------------------");
        }else{
            f.format("%80s", "================================================================================"); f.format("%n");
            f.format("%20s", "Item");
            f.format("%12s", "Difficulty");
            f.format("%10s", "Discrim.");
            f.format("%10s", "S.D.");
            f.format("%10s", "Option");
            f.format("%10s", "Count");
            f.format("%8s", "Flags"); f.format("%n");
            f.format("%80s", "--------------------------------------------------------------------------------");
        }

        return f.toString();
    }

    /**
     * Display formatted results. Call printHeader() before this method to add a labeled header to the output.
     *
     * @return output
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        boolean first = true;
        f.format("%20s", itemName);
        f.format("%12."+precision+"f", getItemDifficulty());
        f.format("%10."+precision+"f", getItemTotalCorrelation());
        f.format("%10."+precision+"f", getItemStandardDeviation());

        double validCount = getValidCount();

        if(allCategories){
            Iterator<Comparable<?>> iter = freq.valuesIterator();
            while(iter.hasNext()){
                Comparable<?> value = iter.next();
                String s = value.toString();
                f.format("%10s", s.substring(Math.max(0, s.length()-9)));
                f.format("%10d", freq.getCount(value));
                f.format("%10."+precision+"f", getProportionAt(value));
                f.format("%10."+precision+"f", getPointBiserialAt(value));

                if(first){
                    f.format("%4s", getDifficultyFlag());
                    f.format("%4s", getDiscriminationFlag());
                    first = false;
                }

                f.format("%n");

                if(iter.hasNext()){
                    f.format("%52s", "");
                }else{
                    f.format("%62s", "Missing"); f.format("%10d", missingCount);f.format("%n");
                    f.format("%62s", "Valid"); f.format("%10d", (long)validCount);f.format("%n");
                    f.format("%62s", "Total"); f.format("%10d", getTotalCount());f.format("%n");
                }
            }
        }else{
            f.format("%10s", "Missing"); f.format("%10d", missingCount);
            f.format("%4s", getDifficultyFlag());
            f.format("%4s", getDiscriminationFlag());
            f.format("%n");
            f.format("%62s", "Valid"); f.format("%10d", (long)validCount);f.format("%n");
            f.format("%62s", "Total"); f.format("%10d", getTotalCount());f.format("%n");
        }

        f.format("%n");
        return f.toString();
    }

    /**
     * A footer for output.  Call after toString() to complete the display of formatted output.
     *
     * @return an output footer.
     */
    public String getFooter(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        if(allCategories){
            f.format("%100s", "===================================================================================================="); f.format("%n");
        }else{
            f.format("%80s", "================================================================================"); f.format("%n");
        }

        return f.toString();
    }


    /**
     * Compute number of elements in the output array created in getOutputArray
     *
     * @return number of elements in the output array
     */
    public int getNumberOfOutputColumns(){
        if(allCategories){
            return 9+3*freq.getUniqueCount();
        }else{
            return 8;
        }
    }

    /**
     * Create an Object array that contains the statistics for every options for this item. This method is mainly
     * used for writing output to file using the Outputter interface.
     *
     * @return array of item statistics.
     */
    public Object[] getOutputArray(){
        int count = getNumberOfOutputColumns();
        Object[] obj = new Object[count];
        int col = 0;
        double validCount = getValidCount();

        obj[col++] = itemName;
        obj[col++] = getItemDifficulty();
        obj[col++] = getItemTotalCorrelation();
        obj[col++] = getItemStandardDeviation();
        obj[col++] = validCount;
        obj[col++] = getMissingCount();
        obj[col++] = getTotalCount();
        obj[col++] = getDifficultyFlag() + " " + getDiscriminationFlag();

        if(allCategories){
            obj[col++] = freq.getUniqueCount();

            Iterator<Comparable<?>> iter = freq.valuesIterator();
            while(iter.hasNext()){
                Comparable<?> value = iter.next();

                if(validCount>0){
                    obj[col++] = freq.getCount(value);
                    obj[col++] = getProportionAt(value);
                    obj[col++] = getPointBiserialAt(value);
                }else{
                    obj[col++] = 0;
                    obj[col++] = 0;
                    obj[col++] = 0;
                }

            }
        }

        return obj;
    }


}

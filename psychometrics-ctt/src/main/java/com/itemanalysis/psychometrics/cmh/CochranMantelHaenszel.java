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
package com.itemanalysis.psychometrics.cmh;

import com.itemanalysis.psychometrics.data.TidyOutput;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.statistics.WeightedOnlineStandardDeviation;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.*;

/**
 *
 * See the article below for details:
 * 
 * Zwick, R., & Thayer, D. T. (1996). Evaluating the magnitude of differential item functioning in polytomous items.
 * Journal of Educational and Behavioral Statistics, 21, 187-201.
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class CochranMantelHaenszel {

    /**
     * Computes the Cochran-Mantel-Haenszel statistics and related quantities.
     *
     * TreeMap includes the k, R X C tables.
     * Each table is a strata indexed by an ordinal number that is the TreeMap key.
     * The TwoWayTable allows for R X C tables, but this use is based on 2 X C tables.
     * 
     */
    private TreeMap<Double, CmhTable> strata = null;
    private TreeSet<Double> itemScores = null;
    private String title = "";
    private String focalCode = "";
    private String referenceCode = "";
    private String itemName = null;
    private WeightedOnlineStandardDeviation combinedGroupsSd = null;
    private WeightedOnlineStandardDeviation focalSd = null;
    private WeightedOnlineStandardDeviation referenceSd = null;
    private boolean etsDelta = false;
    private double totalSampleSize = 0;
    private double totalFocalSize = 0;
    private double totalReferenceSize = 0;
    private double validSampleSize = 0.0;
    private double validFocalSampleSize = 0;
    private double validReferenceSampleSize = 0;
    private int validStrata = 0;
    private Double cmhChiSquare = null;
    private Double comOddsRatio = null;
    private ChiSquaredDistribution chiSquare = new ChiSquaredDistribution(1.0);
    double[] ci = null;
    double minItemScore = Double.MAX_VALUE;
    double maxItemScore = -Double.MAX_VALUE;


    public CochranMantelHaenszel(String focalCode, String referenceCode, String itemName, boolean etsDelta){
        this(focalCode+"_"+referenceCode, focalCode, referenceCode, itemName, etsDelta);
    }

    public CochranMantelHaenszel(String title, String focalCode, String referenceCode, String itemName, boolean etsDelta){
        strata = new TreeMap<Double, CmhTable>();
        itemScores = new TreeSet<Double>();
        this.title = title + "("+focalCode+"_"+referenceCode+")";
        this.focalCode = focalCode;
        this.referenceCode = referenceCode;
        this.itemName = itemName;
        this.etsDelta = etsDelta;
        combinedGroupsSd = new WeightedOnlineStandardDeviation();
        focalSd = new WeightedOnlineStandardDeviation();
        referenceSd = new WeightedOnlineStandardDeviation();
    }

    /**
     * Increment the statistic for one observation.
     *
     * @param strataScore mathing score i.e. raw score, decile, or quintile
     * @param groupValue examinees group membership
     * @param itemScore examinees response to item
     */
    public void increment(double strataScore, String groupValue, double itemScore){
        if(groupValue==null || "".equals(groupValue.trim()) || Double.isNaN(itemScore)) return;

        CmhTable temp = strata.get(strataScore);
        if(temp==null){
            temp = new CmhTable(focalCode, referenceCode);
            strata.put(strataScore, temp);
        }
        temp.increment(groupValue, itemScore);
        combinedGroupsSd.increment(itemScore);

        if(groupValue.equals(focalCode)){
            focalSd.increment(itemScore);
            itemScores.add(itemScore);
        }else if(groupValue.equals(referenceCode)){
            referenceSd.increment(itemScore);
            itemScores.add(itemScore);
        }

        minItemScore = Math.min(minItemScore, itemScore);
        maxItemScore = Math.max(maxItemScore, itemScore);
        
    }

    /**
     * Increment the statistic with a weighted observation.
     *
     * @param strataScore mathing score i.e. raw score, decile, or quintile
     * @param groupValue examinees group membership
     * @param itemScore examinees response to item
     * @param frequency a frequency weight
     */
    public void increment(double strataScore, String groupValue, double itemScore, long frequency){
        if(groupValue==null || "".equals(groupValue.trim()) || Double.isNaN(itemScore)) return;

        CmhTable temp = strata.get(strataScore);
        if(temp==null){
            temp = new CmhTable(focalCode, referenceCode);
            strata.put(strataScore, temp);
        }
        temp.increment(groupValue, itemScore, frequency);
        combinedGroupsSd.increment(itemScore, frequency);

        if(groupValue.equals(focalCode)){
            focalSd.increment(itemScore, frequency);
            itemScores.add(itemScore);
        }else if(groupValue.equals(referenceCode)){
            referenceSd.increment(itemScore, frequency);
            itemScores.add(itemScore);
        }

        minItemScore = Math.min(minItemScore, itemScore);
        maxItemScore = Math.max(maxItemScore, itemScore);

    }

    public double focalTotalSize(){
        Set<Double> keys = strata.keySet();
        Iterator<Double> iter = keys.iterator();
        CmhTable table = null;
        double fSum = 0.0;

        while(iter.hasNext()){
            table = strata.get(iter.next());
            fSum += table.focalSize();
        }
        return fSum;
    }

    public double getTotalFocalSize(){
        return totalFocalSize;
    }

    public double getTotalReferenceSize(){
        return totalReferenceSize;
    }

    public boolean isPolytomous(){
        for(Double d : strata.keySet()){
            if(strata.get(d).numberOfScoreLevels()>2) return true;
        }
        return false;
    }

    /**
     * 
     *
     * @return Cochran-Mantel-Haenszel chi-square statistic
     */
    public double cochranMantelHaenszel(){
        if(cmhChiSquare!=null) return cmhChiSquare;
        Iterator<Double> iter = strata.keySet().iterator();
        CmhTable table = null;
        double focalSumOfScoresSum = 0.0;
        double expectedValueSum = 0.0;
        double varSum = 0.0;
        double tableVar = 0.0;
        double tableTotal = 0.0;

        while(iter.hasNext()){
            table = strata.get(iter.next());
            tableVar = table.variance();
            tableTotal = table.total();
            totalSampleSize += tableTotal;
            totalFocalSize += table.focalSize();
            totalReferenceSize += table.referenceSize();

            if(tableVar>0){
                validSampleSize+=tableTotal;
                validFocalSampleSize+=table.focalSize();
                validReferenceSampleSize+=table.referenceSize();
                validStrata++;
                focalSumOfScoresSum += table.focalSumOfScores();
                expectedValueSum += table.expectedValue();
                varSum += tableVar;
            }
        }

        cmhChiSquare = Math.pow(focalSumOfScoresSum-expectedValueSum, 2)/varSum;
        return cmhChiSquare;
    }

    /**
     * Computed only for the first two rows and first two columns in each strata
     *
     * @return
     */
    public double commonOddsRatio(){
        if(comOddsRatio!=null) return comOddsRatio;
        Set<Double> keys = strata.keySet();
        Iterator<Double> iter = keys.iterator();
        CmhTable table = null;
        double numerator = 0.0;
        double denominator = 0.0;

        while(iter.hasNext()){
            table = strata.get(iter.next());
            numerator += table.commonOddsRatioNumerator();
            denominator += table.commonOddsRatioDenominator();
        }
        comOddsRatio = numerator/denominator;
        return comOddsRatio;
    }

    public double commonOddsRatioVariance(){
        Set<Double> keys = strata.keySet();
        Iterator<Double> iter = keys.iterator();
        CmhTable table = null;
        double var = 0.0;
        double num1 = 0.0, num2 = 0.0, num3 = 0.0;
        double denomADsum = 0.0, denomBCsum = 0.0;
        double A = 0.0, B = 0.0, C = 0.0, D = 0.0, T = 0.0;

        while(iter.hasNext()){
            table = strata.get(iter.next());
            A = table.referenceRight();
            B = table.referenceWrong();
            C = table.focalRight();
            D = table.focalWrong();
            T = A+B+C+D;
            if(T>0){
                num1 += (A+D)*(A*D)/Math.pow(T,2);
                denomADsum += (A*D)/T;
                num2 += ((A+D)*(B*C)+(B+C)*(A*D))/Math.pow(T,2);
                denomBCsum += (B*C)/T;
                num3 += (B+C)*(B*C)/Math.pow(T,2);
            }
        }
        var =   num1/(2.0*Math.pow(denomADsum,2)) +
                num2/(2.0*denomADsum*denomBCsum) +
                num3/(2.0*Math.pow(denomBCsum,2));
        return var;
    }

    /**
     * Computes 95% confidence interval for the common odds ratio.
     * Used in computation of ETS DIF classification for binary items.
     *
     * @param commonOddsRatio
     * @return
     */
    public double[] commonOddsRatioConfidenceInterval(double commonOddsRatio){
        if(ci!=null) return ci;
        ci = new double[2];
        double var = commonOddsRatioVariance();
        double sigma = Math.sqrt(var);
        ci[0] = commonOddsRatio*Math.exp(-1.96*sigma);
        ci[1] = commonOddsRatio*Math.exp(1.96*sigma);
        return ci;
    }

    /**
     * Computes 95% confidence interval for the standardized mean difference.
     * Assumes SMD is distributed unit normal.
     *
     * @param standardizedMeanDifference
     * @return
     */
    public double[] smdConfidenceInterval(double standardizedMeanDifference){
        if(ci!=null) return ci;
        ci = new double[2];
        double moe = 1.96*Math.sqrt(varianceSMD());
        ci[0] = standardizedMeanDifference - moe;
        ci[1] = standardizedMeanDifference + moe;
        return ci;
    }

    /**
     * Compute variance of the standardized mean difference (SMD). This method
     * is described in Zwick & Thayer (1996) Equation 7.
     *
     * @return
     */
    public double varianceSMD(){
        double var = 0.0;
        double w = 0.0;
        double nfk = 0.0;
        double nrk = 0.0;
        double inner = 0.0;

        CmhTable temp = null;
        for(Double d : strata.keySet()){
            temp = strata.get(d);
            nfk = temp.focalSize();
            nrk = temp.referenceSize();
            if(nfk>0.0 && nrk > 0.0){
                w = nfk/focalTotalSize();
                inner = 1.0/nfk + 1.0/nrk;
                var += (w*w)*(inner*inner)*temp.variance();
            }
        }
        return var;
    }

    public double etsDelta(double commonOddsRatio){
        double d = -2.35*Math.log(commonOddsRatio);
        return d;
    }

    /**
     * Computes the standardized mean difference (SMD). For binary items, this method
     * is the same as Dorans and Kulick (1986) STD P-DIF. See Zwick and Thayer (1996)
     * for details.
     *
     * @return
     */
    public double pF(){
        double focalTotalSize = focalTotalSize();
        Set<Double> keys = strata.keySet();
        Iterator<Double> iter = keys.iterator();
        Double strataScore = null;
        CmhTable table = null;
        double pF = 0.0;
        double pR = 0.0;
        double focalSize = 0.0;
        double referenceSize = 0.0;
        double prop = 0.0;

        while(iter.hasNext()){
            strataScore = iter.next();
            table = strata.get(strataScore);
            focalSize = table.focalSize();
            referenceSize = table.referenceSize();
            if(focalSize > 0.0 && referenceSize > 0.0){
                prop = (focalSize/focalTotalSize);
                pF += prop*(table.focalSumOfScores()/focalSize);
                pR += prop*(table.referenceSumOfScores()/referenceSize);
            }
        }
        double smd = pF - pR;
        return smd;
    }

    /**
     *
     * Returns a Z test of the SMD statistic, Z_H(SMD) on page
     * 194 of Zwick and Thayer (1996). This rho is distributed as a standard
     * normal variate under the null hypothesis.
     *
     *
     */
    public double zSMD(){
        return pF()/Math.sqrt(varianceSMD());
    }

    public String etsBinayClassification(double cochranMantelHaenszel, double pvalue, double commonOddsRatio){
        if(validSampleSize==0) return "";

        double[] ci = commonOddsRatioConfidenceInterval(commonOddsRatio);
        String difClass = "B ";
        if(pvalue > 0.05 || (0.65 < commonOddsRatio && commonOddsRatio < 1.53)){
            difClass = "A ";
        }else if(commonOddsRatio < 0.53 && ci[1] < 0.65){
            difClass = "C+";
        }else if(commonOddsRatio > 1.89 && ci[0] > 1.53){
            difClass = "C-";
        }else{
            if(commonOddsRatio < 1.0){
                difClass = "B+";
            }else{
                difClass = "B-";
            }
        }
        return difClass;
    }

    public String smdDifClass(){
        double ES = pF()/(maxItemScore-minItemScore); //divide by item score range to limit ES to the interval from -1 to +1.
        double aES = Math.abs(ES);
        String difClass = "";
        if(aES < 0.05) return "AA ";
        if(aES >= 0.05 && aES < 0.10){
            difClass =  "BB";
        }else if(aES > 0.10){
            difClass = "CC";
        }

        if(ES>0) difClass +="+";
        else difClass +="-";

        return difClass;
    }

    public String getVariableName(){
        return itemName;
    }

    public double getPValue(){
        double value = cochranMantelHaenszel();
        double pvalue = 1.0-chiSquare.cumulativeProbability(value);
        return pvalue;
    }

    public int getSampleSize(){
        return (int)validSampleSize;
    }

    public long getValidFocalSampleSize(){
        return (long)validFocalSampleSize;
    }

    public long getValidReferenceSampleSize(){
        return (long)validReferenceSampleSize;
    }

    public double getEffectSize(){
        double effectSize = 0;
        if(!isPolytomous()){
            effectSize = commonOddsRatio();
            if(etsDelta) effectSize = etsDelta(effectSize);
        }else{
            effectSize = pF();
        }
        return effectSize;
    }

    public double[] getEffectSizeConfidenceInterval(){
        double[] confInt = new double[2];
        if(!isPolytomous()){
            double effectSize = commonOddsRatio();
            confInt = commonOddsRatioConfidenceInterval(effectSize);
            if(etsDelta){
                confInt[0] = etsDelta(confInt[0]);
                confInt[1] = etsDelta(confInt[1]);
            }
        }else{
            double smd = pF();
            confInt = smdConfidenceInterval(smd);
        }
        return confInt;
    }

    public String getETSDifClassification(){
        String etsClass = "A";
        if(!isPolytomous()){
            etsClass = etsBinayClassification(cochranMantelHaenszel(), getPValue(), commonOddsRatio());
        }else{
            etsClass = smdDifClass();
        }
        return etsClass;
    }

    public String getFocalCode(){
        return focalCode;
    }

    public String getReferenceCode(){
        return referenceCode;
    }

    /**
     * From 1999 South Carolina PACT Technical Documentation
     *
     *
     * @return
     */
//    public String etsPolytomousClassification(double cochranMantelHaenszel, double pvalue, double polytomousEffectSize){
//        String difClass = "BB";
//        if(pvalue > 0.05){
//            difClass = "AA ";
//        }else if(Math.abs(polytomousEffectSize)<=0.17){
//            difClass = "AA ";
//        }else if(0.17<Math.abs(polytomousEffectSize) && Math.abs(polytomousEffectSize)<=0.25){
//            difClass = "BB";
//            if(polytomousEffectSize>=0) difClass += "+"; else difClass += "-";
//        }else if(Math.abs(polytomousEffectSize)>0.25){
//            difClass = "CC";
//            if(polytomousEffectSize>=0) difClass += "+"; else difClass += "-";
//        }
//        return difClass;
//    }

    public String printTables(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        Set<Double> keys = strata.keySet();
        Iterator<Double> iter = keys.iterator();
        CmhTable table = null;
        Double score = null;

        while(iter.hasNext()){
            score = iter.next();
            table = strata.get(score);
            f.format("%n");
            f.format("%n");
            f.format("%n");
            f.format("%45s", "Score Level: "); f.format("%-10.4f",  + score); f.format("%n");
            f.format("%100s", table.toString()); f.format("%n");
        }
        return f.toString();
    }

    /**
     * Creates an array list of string arrays. Each element in the List is a row of data.
     * Each row has five columns (array elements): item name, group ID, stratum, item score, and frequency count.
     * Focal group data are shown first, then reference group data.
     *
     * @return array list of data
     */
    public ArrayList<String[]> getFrequencyTables(){
        ArrayList<String[]> tableList = new ArrayList<>();
        CmhTable table = null;
        String[] rowValues = null;

        //Focal group data
        for(Double score : strata.keySet()){
            table = strata.get(score);

            for(Double itemScore : itemScores){
                rowValues = new String[5];
                rowValues[0] = itemName;
                rowValues[1] = focalCode;
                rowValues[2] = score.toString();
                rowValues[3] = itemScore.toString();
                rowValues[4] = Long.valueOf((long)table.getFocalRow().freqAt(itemScore)).toString();
                tableList.add(rowValues);
            }
        }

        //Reference group data
        for(Double score : strata.keySet()){
            table = strata.get(score);

            for(Double itemScore : itemScores){
                rowValues = new String[5];
                rowValues[0] = itemName;
                rowValues[1] = referenceCode;
                rowValues[2] = score.toString();
                rowValues[3] = itemScore.toString();
                rowValues[4] = Long.valueOf((long)table.getReferenceRow().freqAt(itemScore)).toString();
                tableList.add(rowValues);
            }
        }

        return tableList;
    }

    public TidyOutput getTidyFrequencyTables(){
        TidyOutput tidyOutput = new TidyOutput();
        CmhTable table = null;

        //Focal group data
        for(Double score : strata.keySet()){
            table = strata.get(score);

            for(Double itemScore : itemScores){
                tidyOutput.addValue("itemid", itemName);
                tidyOutput.addValue("group", focalCode);
                tidyOutput.addValue("stratum", score.toString());
                tidyOutput.addValue("itemscore", itemScore.toString());
                tidyOutput.addValue("frequency", Long.valueOf((long)table.getFocalRow().freqAt(itemScore)).toString());
                tidyOutput.nextRow();

            }
        }

        //Reference group data
        for(Double score : strata.keySet()){
            table = strata.get(score);

            for(Double itemScore : itemScores){
                tidyOutput.addValue("itemid", itemName);
                tidyOutput.addValue("group", referenceCode);
                tidyOutput.addValue("stratum", score.toString());
                tidyOutput.addValue("itemscore", itemScore.toString());
                tidyOutput.addValue("frequency", Long.valueOf((long)table.getReferenceRow().freqAt(itemScore)).toString());
                tidyOutput.nextRow();
            }
        }

        return tidyOutput;
    }

    public String printHeader(){
		StringBuilder buffer = new StringBuilder();
		Formatter f = new Formatter(buffer);
        f.format("%-10s", " Item");f.format("%2s", " ");
		f.format("%10s", "Chi-square");f.format("%2s", " ");
		f.format("%7s", "p-value");f.format("%2s", " ");
        f.format("%7s", "Valid N");f.format("%2s", " ");
		f.format("%28s", "    E.S. (95% C.I.)     ");f.format("%2s", " ");
        f.format("%5s",  "Class");f.format("%2s", " ");
		f.format("%n");
        f.format("%-10s","----------");f.format("%2s", " ");
		f.format("%10s", "----------");f.format("%2s", " ");
        f.format("%7s", "-------");f.format("%2s", " ");
        f.format("%7s", "-------");f.format("%2s", " ");
		f.format("%28s", "----------------------------");f.format("%2s", " ");
        f.format("%5s", "-----");f.format("%2s", " ");
		f.format("%n");
		return f.toString();
	}

    /**
     * This method provides the results in a form that can be inserted into
     * a database table.
     *
     * @return
     */
    public String getDatabaseString(){
        String output = "";

        double cmh = cochranMantelHaenszel();
        double pvalue = getPValue();
        double commonOddsRatio = 0.0;
        double[] tempConfInt = {0.0, 0.0};
        double[] confInt = {0.0, 0.0};
        double smd = 0.0;
        double effectSize = 0.0;
        String etsClass = "";

        if(!isPolytomous()){
            commonOddsRatio = commonOddsRatio();
            tempConfInt = commonOddsRatioConfidenceInterval(commonOddsRatio);
            if (etsDelta) {
                effectSize = etsDelta(commonOddsRatio);
                confInt[0] = etsDelta(tempConfInt[0]);
                confInt[1] = etsDelta(tempConfInt[1]);
            } else {
                effectSize = commonOddsRatio;
                confInt[0] = tempConfInt[0];
                confInt[1] = tempConfInt[1];
            }
            etsClass = etsBinayClassification(cmh, pvalue, commonOddsRatio);
        }else if(isPolytomous()){
            smd = pF();
            tempConfInt = smdConfidenceInterval(smd);
            confInt[0] = tempConfInt[0];
            confInt[1] = tempConfInt[1];
            effectSize = pF();
            etsClass = smdDifClass();
        }


        output += itemName + ",";

        if(Double.isNaN(cmh) || Double.isInfinite(cmh)){
            output += ",";
        }else{
            output += cmh + ",";
        }
        if(Double.isNaN(pvalue) || Double.isInfinite(pvalue)){
            output += ",";
        }else{
            output += pvalue + ",";
        }

        output += validSampleSize +",";

        if(Double.isNaN(effectSize) || Double.isInfinite(cmh)){
            output += ",";
        }else{
            output += effectSize + ",";
        }

        if(Double.isNaN(confInt[0]) || Double.isInfinite(cmh)){
            output += ",";
        }else{
            output += confInt[0] + ",";
        }

        if(Double.isNaN(confInt[1]) || Double.isInfinite(cmh)){
            output += ",";
        }else{
            output += confInt[1] + ",";
        }

        output += etsClass;

        return output;
    }

    @Override
    public String toString(){
        StringBuilder buffer = new StringBuilder();
		Formatter f = new Formatter(buffer);
        ChiSquaredDistribution chiSquare = new ChiSquaredDistribution(1.0);

        double cmh = cochranMantelHaenszel();
        Double pvalue = 1.0;
        pvalue = 1.0-chiSquare.cumulativeProbability(cmh);

        double commonOddsRatio = 0.0;
        double[] tempConfInt = {0.0, 0.0};
        double[] confInt = {0.0, 0.0};
        double smd = 0.0;
        double effectSize = 0.0;
        String etsClass = "";

//        if(itemVariable.getType().getItemType()== ItemType.BINARY_ITEM){
        if(!isPolytomous()) {
            commonOddsRatio = commonOddsRatio();
            tempConfInt = commonOddsRatioConfidenceInterval(commonOddsRatio);
            if (etsDelta) {
                effectSize = etsDelta(commonOddsRatio);
                confInt[0] = etsDelta(tempConfInt[0]);
                confInt[1] = etsDelta(tempConfInt[1]);
            } else {
                effectSize = commonOddsRatio;
                confInt[0] = tempConfInt[0];
                confInt[1] = tempConfInt[1];
            }
            etsClass = etsBinayClassification(cmh, pvalue, commonOddsRatio);
//        }else if(itemVariable.getType().getItemType()==ItemType.POLYTOMOUS_ITEM){
        }else{
            smd = pF();
            tempConfInt = smdConfidenceInterval(smd);
            confInt[0] = tempConfInt[0];
            confInt[1] = tempConfInt[1];
            effectSize = pF();
//            etsClass = etsPolytomousClassification(cmh, pvalue, effectSize);
            etsClass = smdDifClass();
        }

        f.format("%-10s", itemName);f.format("%2s", " ");
//		f.format("%10s", focalCode + "/" + referenceCode);f.format("%2s", " ");
		f.format("%10.2f", cmh);f.format("%2s", " ");
		f.format("%7.2f", pvalue);f.format("%2s", " ");
        f.format("%7.0f", validSampleSize); f.format("%2s", " ");
		f.format("%8.2f", effectSize); f.format(" (%8.2f", confInt[0]);f.format(",%8.2f)", confInt[1]);f.format("%2s", " ");
        f.format("%5s", etsClass);f.format("%2s", " ");
        return f.toString();
    }

    /**
     * Formats output as a tidy dataset for a csv file
     *
     * @return a string array in tidy format
     */
    public TidyOutput getTidyOutput() {
        double cmh = cochranMantelHaenszel();
        double pvalue = getPValue();
        double commonOddsRatio = 0.0;
        double[] tempConfInt = {0.0, 0.0};
        double[] confInt = {0.0, 0.0};
        double smd = 0.0;
        double effectSize = 0.0;
        String etsClass = "";

        if(!isPolytomous()){
            commonOddsRatio = commonOddsRatio();
            tempConfInt = commonOddsRatioConfidenceInterval(commonOddsRatio);
            if (etsDelta) {
                effectSize = etsDelta(commonOddsRatio);
                confInt[0] = etsDelta(tempConfInt[0]);
                confInt[1] = etsDelta(tempConfInt[1]);
            } else {
                effectSize = commonOddsRatio;
                confInt[0] = tempConfInt[0];
                confInt[1] = tempConfInt[1];
            }
            etsClass = etsBinayClassification(cmh, pvalue, commonOddsRatio);
        }else if(isPolytomous()){
            smd = pF();
            tempConfInt = smdConfidenceInterval(smd);
            confInt[0] = tempConfInt[0];
            confInt[1] = tempConfInt[1];
            effectSize = pF();
            etsClass = smdDifClass();
        }

        TidyOutput tidyOutput = new TidyOutput();
        String timeStamp = tidyOutput.getTimeStamp();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "chi_square");
        tidyOutput.addValue("value", Double.valueOf(cmh).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "p_value");
        tidyOutput.addValue("value", Double.valueOf(pvalue).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "valid_strata");
        tidyOutput.addValue("value", Integer.valueOf(validStrata).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "valid_focal_count");
        tidyOutput.addValue("value", Long.valueOf(getValidFocalSampleSize()).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "valid_reference_count");
        tidyOutput.addValue("value", Long.valueOf(getValidReferenceSampleSize()).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "valid_total_count");
        tidyOutput.addValue("value", Double.valueOf(validSampleSize).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "focal_count");
        tidyOutput.addValue("value", Double.valueOf(totalFocalSize).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "reference_count");
        tidyOutput.addValue("value", Double.valueOf(totalReferenceSize).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();


        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "total_count");
        tidyOutput.addValue("value", Double.valueOf(totalSampleSize).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "effect_size");
        tidyOutput.addValue("value", Double.valueOf(effectSize).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "conf_int_lower");
        tidyOutput.addValue("value", Double.valueOf(confInt[0]).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        tidyOutput.addValue("name", itemName);
        tidyOutput.addValue("date", timeStamp);
        tidyOutput.addValue("method", "dif_cmh");
        tidyOutput.addValue("title", title);
        tidyOutput.addValue("focal", focalCode);
        tidyOutput.addValue("reference", referenceCode);
        tidyOutput.addValue("statistic", "conf_int_upper");
        tidyOutput.addValue("value", Double.valueOf(confInt[1]).toString());
        tidyOutput.addValue("etsclass", etsClass);
        tidyOutput.nextRow();

        return tidyOutput;
    }


}

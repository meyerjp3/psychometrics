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
package com.itemanalysis.psychometrics.rasch;

import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.measurement.DefaultItemScoring;
import com.itemanalysis.psychometrics.scaling.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat.OutputAlignment;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * JMLE estimation of Rasch, Partial Credit, and Rating Scale model parameters.
 * 
 * For new data, call summarizeData(), flagExtremes(), prox(), and then run().
 *
 *
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class JMLE {

    private TestFrequencyTable table = null;

    /**
     * Map of all items
     */
    private LinkedHashMap<VariableName, RatingScaleItem> items = null;

    /**
     * Map of all thresholds
     */
    private LinkedHashMap<String, RatingScaleThresholds> thresholds = null;

    /**
     * VariableInfo list for all items.
     */
    private ArrayList<VariableAttributes> variables = null;

    /**
     * Counts number of items in each rating scale group
     */
    private Frequency groupFrequencies = null;

    private int uniqueGroups = 0;

    private int nPeople = 0;

    private int nItems = 0;

    private double adjust = 0.3;

    /**
     * scored item response data
     */
    private byte[][] data = null;

    /**
     * Array of person parameter estimates
     */
    private double[] theta = null;

    /**
     * Flags to indicate extreme persons
     */
    private boolean[] extremePersons = null;

    private RatingScaleModel rsm = null;

    private Max maxDelta = null;

    private boolean ignoreMissing = true;

    private ArrayList<Double> iterationDelta = null;

    private ArrayList<Double> extremeIterationDelta = null;

    public JMLE(ArrayList<VariableAttributes> variables, double adjust, int nPeople, boolean ignoreMissing){
        this.variables = variables;
        this.adjust = adjust;
        this.ignoreMissing = ignoreMissing;
        items = new LinkedHashMap<VariableName, RatingScaleItem>();
        thresholds = new LinkedHashMap<String, RatingScaleThresholds>();
        groupFrequencies = new Frequency();
        this.nPeople = nPeople;
        nItems = variables.size();
        theta = new double[nPeople];
        data = new byte[nPeople][nItems];
        extremePersons = new boolean[nPeople];

        table = new TestFrequencyTable();
        rsm = new RatingScaleModel();
        maxDelta = new Max();
        iterationDelta = new ArrayList<Double>();
        extremeIterationDelta = new ArrayList<Double>();
        createItemsAndCategories(variables);
        initializeExtremes();
    }

    /**
     * This method creates the TestFrequencyTable and initializes data structures.
     *
     * @param variables
     */
    private void createItemsAndCategories(ArrayList<VariableAttributes> variables){
        RatingScaleItem item = null;
        String subscale = "";//may need to make this default to item name
        int k = 2;
        double mpis = 1.0;
        int position = 0;
        for(VariableAttributes v : variables){
            subscale = v.getSubscale(true);
            mpis = v.getMaximumPossibleItemScore().doubleValue();
            k = (int)mpis+1;//number of categories
            item = new RatingScaleItem(
                    v.getName(),
                    subscale,
                    (int)v.getItemScoring().maximumPossibleScore(),
                    position);
            table.addItem(v.getName(), v.getItemGroup(), position, (DefaultItemScoring)v.getItemScoring());
            items.put(v.getName(), item);
            groupFrequencies.addValue(subscale);

            if(k>2){
                RatingScaleThresholds thresh = thresholds.get(subscale);
                if(thresh==null){
                    thresh = new RatingScaleThresholds(subscale, k);
                    thresholds.put(subscale, thresh);
                }
                thresh.addItemToGroup(item);
            }
            position++;
        }
        uniqueGroups = groupFrequencies.getUniqueCount();
    }

    /**
     * Sets extreme persons and items to false. Should be called from constructor.
     */
    private void initializeExtremes(){
        for(int i=0;i<nPeople;i++){
            extremePersons[i] = false;
        }
    }

//==============================================================================================================================
// METHODS FOR SUMMARIZING DATA, COMPUTING VARIOUS RAW SCORES, AND CHECKING FOR EXTREME PERSONS AND ITEMS
//==============================================================================================================================

    /**
     * Summarizes data into a TestFreqeuncyTable and stores scored responses in two way byte array
     * in a two way byte array.
     *
     * @throws SQLException
     */
    public void summarizeData(ResultSet rs)throws SQLException{
        Object response = null;
        byte responseScore = 0;
        try{
            int r = 0;
            int c = 0;
            while(rs.next()){
                c = 0;
                for(VariableAttributes v : variables){//columns in data will be in same order as variables
                    response = rs.getObject(v.getName().nameForDatabase());
                    if((response==null || response.equals("") || response.equals("NA")) && ignoreMissing){
                        data[r][c] = -1;//code for omitted responses
                    }else{
                        responseScore = (byte)v.getItemScoring().computeItemScore(response);
                        table.increment(v.getName(), v.getSubscale(true), responseScore);
                        data[r][c] = responseScore;
                    }
                    c++;
                }
                r++;
            }
        }catch(SQLException ex){
            throw new SQLException(ex);
        }
    }

    /**
     * This method identifies extreme items and persons. Given that removing some
     * extreme persons may cause extreme items and removal of extreme items may cause
     * extreme persons, this method loops until no extreme items or people are found
     * or the number of iterations equals the number of persons. In the case of Guttman
     * items, all items and persons will be flagged as extreme.
     *
     * @return number of iteration.
     */
    public int flagExtremes(){
        int priorFlags = 0;
        int currentFlags = 1;
        int iter = 0;
        boolean hasThresholds = !thresholds.isEmpty();
        while(currentFlags>priorFlags && iter<nPeople){
            priorFlags = currentFlags;
            currentFlags = flagExtremePersons();
            currentFlags += flagExtremeItems();
            if(hasThresholds) currentFlags += flagExtremeCategories();
            iter++;
        }
        return iter;
    }

    /**
     * This method identifies persons with exteme scores. An extreme
     * score is defined as one in which the valid raw score (vRS) equals
     * the valid maximum possible raw score (vMPRS).
     *
     * @return
     */
    private int flagExtremePersons(){
        int n = 0;
        double vrs = 0.0;
        for(int i=0;i<nPeople;i++){
            vrs = validRawScore(data[i]);
            if(vrs==vMPRS(data[i]) || vrs==0.0){
                extremePersons[i] = true;
                n++;
            }
        }
        return n;
    }

    /**
     * This method identifies items with extreme scores. An extreme
     * score is defined as one in which the valid raw item score (vRIS)
     * equals the valid maximum possible raw item score (vMPRIS)
     *
     * @return
     */
    private int flagExtremeItems(){
        int n = 0;
        int col = 0;
        double vrs = 0.0;
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            col = rsi.getColumn();
            vrs = validRIS(col);

            if(vrs==vMPRIS(col) || vrs==0.0){
                rsi.setExtremeItem(true);
                n++;
            }
        }
        return n;
    }

    private int flagExtremeCategories(){
        RatingScaleThresholds rst = null;
        double vCC = 0.0;
        int k = 2;
        int n = 0;
        for(String s : thresholds.keySet()){
            rst = thresholds.get(s);
            k = rst.getNumberOfCategories();
            for(int i=0;i<k;i++){
                vCC = validTpj((byte)i, s);
                if(vCC==vMPCC(s) || vCC==0.0){
                    rst.setExtreme(true);
                    n++;
                }
            }
//            if(rst.checkInestimableThresholds()){
//                rst.setExtreme(true);
//                n++;
//            }
        }
        return n;
    }

    /**
     * The raw score (RS) is the sum of a person's observed item scores taken
     * over all items completed by the examinee.
     *
     * @param scores
     * @return
     */
    public double rawScore(byte[] scores){
        double raw = 0.0;
        for(int i=0;i<scores.length;i++){
            if(scores[i]>-1) raw += scores[i];
        }
        return raw;
    }

    public double rawScore(int index){
        return rawScore(data[index]);
    }

    /**
     * The valid raw score (VRS) is the sum of a person's observed item scores
     * taken over all nonextreme items completed by the examinee.
     *
     * @param scores
     * @return
     */
    public double validRawScore(byte[] scores){
        double raw = 0.0;
        int col = 0;
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            col = rsi.getColumn();
            if(scores[col]>-1 && !rsi.extremeItem() && !rsi.droppedItem()) raw += scores[col];
        }
        return raw;
    }

    public double validRawScore(int index){
        return validRawScore(data[index]);
    }

    /**
     * The valid raw item score is the sum of items scores over persons who
     * completed the items and do not have an extreme person score. The
     * raw item score (RIS) is also called Sip.
     *
     * @param col
     * @return
     */
    private double validRIS(int col){
        double vris = 0.0;
        for(int i=0;i<nPeople;i++){
            if(data[i][col]>-1 && !extremePersons[i]) vris += data[i][col];
        }
        return vris;
    }

    /**
     * Return the number of observations in category j, over all persons and items
     * in the group. This method should not be called for inestimable thresholds.
     *
     * @param value
     * @param groupId
     * @return
     */
    private double Tpj(byte value, String groupId){
        RatingScaleThresholds rst = thresholds.get(groupId);
        double tpj = 0.0;
        Iterator<RatingScaleItem> iter = rst.getItemIterator();
        while(iter.hasNext()){
            tpj += table.Tij(iter.next().getName(), value, groupId);
        }
        return tpj;
    }

    /**
     * This method computes the number of observations in category j, over all nonextreme persons
     * responding to the item group. This method should not be called for inestimable thresholds.
     *
     * @param value
     * @param groupId
     * @return
     */
    private double validTpj(byte value, String groupId){
        RatingScaleThresholds rst = thresholds.get(groupId);
        double Tpj = 0.0;
        int[] col = rst.getItemColumns();
        for(int i=0;i<nPeople;i++){
            if(!extremePersons[i]){
                for(int j=0;j<col.length;j++){
                    if(data[i][col[j]]==value) Tpj++;
                }
            }
        }
        return Tpj;
    }

    private double[] Spj(String groupId){
        RatingScaleThresholds rst = thresholds.get(groupId);
        int k = rst.getNumberOfCategories();
        double[] spj = new double[k];
        for(int i=0;i<k;i++){
            spj[i] = table.Spj((byte)i, groupId);
        }
        return spj;
    }

    /**
     * MPCC is the Maximum Possible Category Count. It is the number of persons responding
     * to items in the rating scale group identified by groupId. MPCC is the largest possible
     * rho of Tpj. MPCC == MPRIS when there is only one item in the group and only two response categories,
     * that are scored 0 and 1. If there is no missing data, MPCC == nPeople.
     *
     * @param groupId
     * @return
     */
    private double MPCC(String groupId){
        RatingScaleThresholds rst = thresholds.get(groupId);
        int[] col = rst.getItemColumns();
        double mtpj = 0.0;
        for(int i=0;i<nPeople;i++){
            for(int j=0;j<col.length;j++){
                if(data[i][col[j]]>-1) mtpj++;
            }
        }
        return mtpj;
    }

    /**
     * vMPCC is the valid Maximum Possible Category Count. It is the number of nonextreme persons responding
     * to nonextreme items in the rating scale group identified by groupId. If there are no extreme persons
     * or items, then vMPCC == MPCC.
     *
     * @param groupId
     * @return
     */
    private double vMPCC(String groupId){
        RatingScaleThresholds rst = thresholds.get(groupId);
        int[] col = rst.getValidItemColumns();
        double vmtpj = 0.0;
        for(int i=0;i<nPeople;i++){
            if(!extremePersons[i]){
                for(int j=0;j<col.length;j++){
                    if(data[i][col[j]]>-1) vmtpj++;
                }
            }
        }
        return vmtpj;
    }



    /**
     * MPRS is the Maximum Possible Raw Score. It is the sum of the maximum item score
     * over all items completed by the person. It is computed once.
     *
     * @param scores
     * @return
     */
    private double MPRS(byte[] scores){
        double mprs = 0.0;
        int c = 0;
        for(VariableAttributes v : variables){
            if(scores[c]>-1) mprs += v.getItemScoring().maximumPossibleScore();
            c++;
        }
        return mprs;
    }

    /**
     * vMPRS is the Valid Maximum Possible Raw Score. It is the same as MPRS except that
     * the sum is only over nonextreme items completed by the person. It may be computed iteratively
     * during the frequency summary phase to identify extreme persons.
     *
     * @param scores array of scores for a person
     * @return
     */
    private double vMPRS(byte[] scores){
        double vmprs = 0.0;
        int col = 0;
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            col = rsi.getColumn();
            if(scores[col]>-1 && !rsi.extremeItem() && !rsi.droppedItem()) vmprs += rsi.getMaximumPossibleScore();
        }
        return vmprs;
    }

    /**
     * MPRIS is the Maximum Possible Raw Item Score. It is the product of the number of persons
     * completing the item and the maximum item score. This method provides the maximum
     * possible rho of Sip.
     *
     * @param col is the column position of the item
     * @return
     */
    private double MPRIS(int col){
        double mpris = 0.0;
        double mis = variables.get(col).getItemScoring().maximumPossibleScore();
        double n = 0.0;
        for(int i=0;i<nPeople;i++){
            if(data[i][col]>-1) n++;
        }
        mpris = n*mis;
        return mpris;
    }

    private double MPRIS(VariableName name){
        int col = items.get(name).getColumn();
        return MPRIS(col);
    }

    /**
     * MPRCS is the Maximum Possible Raw Category Score. It is the number of persons
     * responding to the item times the category score. This method provides the
     * maximum possible rho of Spj.
     *
     * @param evaluate category score
     * @param rating scale category group identifier.
     * @return
     */
//    private double MPRCS(Byte rho, String groupId){
//        double mprcs = 0.0;
//        double cs = (double)rho;
//        double n = 0.0;
//        int[] c = thresholds.get(groupId).getItemColumns();
//        for(int i=0;i<nPeople;i++){
//            for(int j=0;j<c.length;j++){
//                if(data[i][c[j]]>-1) n++;
//            }
//        }
//        mprcs = n*cs;
//        return mprcs;
//    }

    /**
     * MPRCC is the Maximum Possible Raw Category Count. It is the largest possible rho of Tpj.
     *
     * @param groupId
     * @return
     */
//    private double MPRCC(String groupId){
//        double nGroup = groupFrequencies.getCount(groupId);
//        double mpris = nGroup*nPeople;
//        return mpris;
//    }

    /**
     * vMPRCC is the Valid Maximum Possible Raw Category Count.
     *
     * @param groupId
     * @return
     */
//    private double vMPRCC(String groupId){
//        double n = 0.0;
//        Iterator<RatingScaleItem> iter = null;
//        RatingScaleItem rsi = null;
//        for(int i=0;i<nPeople;i++){
//            if(extremePersons[i]){
//                iter = thresholds.get(groupId).getItemIterator();
//                while(iter.hasNext()){
//                    if(data[i][iter.next().getColumn()]>-1) n++;
//                }
//            }
//        }
//        double mprcc = MPRCC(groupId);
//        return mprcc-n;
//    }


    /**
     * vMPRCS is the Valid Maximum Possible Raw Category Score.
     *
     * @param evaluate
     * @param groupId
     * @return
     */
//    private double vMPRCS(Byte rho, String groupId){
//        double mpris = 0.0;
//        double n = 0.0;
//        RatingScaleThresholds rst = thresholds.get(groupId);
//        Iterator<RatingScaleItem> iter = null;
//        int col = 0;
//        double mprcs = MPRCS(rho, groupId);
//
//        for(int i=0;i<nPeople;i++){
//            if(extremePersons[i]){
//                iter = rst.getItemIterator();
//                while(iter.hasNext()){
//                    col = iter.next().getColumn();
//                    if(data[i][col]==rho){
//                        n++;
//                    }
//                }
//            }
//        }
//
//
//        for(int i=0;i<nPeople;i++){
//            if(!extremePersons[i]){
//                iter = rst.getItemIterator();
//                while(iter.hasNext()){
//                    col = iter.next().getColumn();
//                    if(data[i][col]==rho){
//                        n++;
//                    }
//                }
//            }
//        }
//        mpris = n*rho;
//        return mpris;
//    }

    /**
     * vMPRIS is the Valid Maximum Possible Raw Item Score. It is the product of the number of non extreme persons
     * completing the item and the maximum item score.
     *
     * @param col column position of item
     * @return
     */
    private double vMPRIS(int col){
        double mpris = 0.0;
        double mis = variables.get(col).getItemScoring().maximumPossibleScore();
        double n = 0.0;
        for(int i=0;i<nPeople;i++){
            if(data[i][col]>-1 && !extremePersons[i]) n++;
        }
        mpris = n*mis;
        return mpris;
    }

    public boolean extremePerson(int index){
        return extremePersons[index];
    }

    public byte[] getItemResponseVector(int index){
        return data[index];
    }

//==============================================================================================================================
// EXTREME SCORE ADJUSTMENTS
//==============================================================================================================================

    /**
     * Adjusts the item score for extreme item scores
     *
     * @param name name of item
     * @return
     */
    private double adjustedRIS(VariableName name){
        double Sip = table.Sip(name);
        int col = items.get(name).getColumn();
        double max = MPRIS(col);
        if(Sip==max) return Sip-adjust;
        if(Sip==0.0) return adjust;
        return Sip;
    }

    /**
     * Adjusts the category score for extreme categories.
     *
     * FIXME sum over adjusted Spj != sum over Spj
     *
     * @param evaluate category score
     * @param groupId identifier of the rating scale group
     * @return
     */
//    private double adjustedSpj(Byte rho, String groupId){
//        double Spj = table.Spj(rho, groupId);
//        double max = MPRCS(rho, groupId);
//        if(Spj==max) return Spj-adjust;
//        if(Spj==0.0) return adjust;
//        return Spj;
//    }
//
//    private double[] adjustedSpj(String groupId){
//        int k = thresholds.get(groupId).getNcat();
//        double[] spj = new double[k];
//        for(int i=0;i<k;i++){
//            spj[i] = adjustedSpj((byte)i, groupId);
//        }
//        return spj;
//    }

    /**
     * Adjusts extreme raw scores according to adjust. This method is consistent with
     * WINSTEPS documentation.
     *
     * raw = Math.max(Math.min(max, raw-adjust), 0.0+adjust);
     *
     * @param scores
     * @return
     */
    private double adjustedRawScore(byte[] scores){
        double raw = 0.0;
        for(int i=0;i<scores.length;i++){
            if(scores[i]>-1) raw += scores[i];
        }
        if(raw==MPRS(scores)) return raw-adjust;
        if(raw==0.0) return adjust;
        return raw;
    }

//==============================================================================================================================
// JMLE ESTIMATION ROUTINE
//==============================================================================================================================


    /**
     * Run the primary JML estimation routines.
     *
     * To estimate thetas using known item parameters, the known parameters must be established and updateItems
     * should be set to false.
     *
     * Extreme persons and items are updated at the same time as nonextreme items and persons. However, the extreme
     * items and persons are not counted toward the convergence criterion and they are not used to estimate
     * parameters for the nonextreme items and persons. 
     *
     *
     * @param globalMaxIter
     * @param globalConvergence maximum change in logits (LCONV in WINSTEPS documentation)
     * @param updateItems set to true if items are to be updated. Note individual items can be fixed too.
     * @throws SQLException
     */
    public void update(int globalMaxIter, double globalConvergence, boolean updateItems, boolean updatePersons){
        double DELTA = globalConvergence+1.0; //LCONV
        maxDelta = new Max();
        Mean itemMean = new Mean();
        int iter = 0;

        RatingScaleItem rsi = null;
        RatingScaleThresholds rst = null;
        double newDifficulty = 0.0;
        while(DELTA >= globalConvergence && iter<globalMaxIter){
            if(updateItems){
                itemMean.clear();

                //update items that are not fixed
                for(VariableName v : items.keySet()){
                    rsi = items.get(v);
                    if(rsi.fixedParameter()){
                        itemMean.increment(rsi.getDifficulty());
                    }else{
                        if(!rsi.extremeItem() && !rsi.droppedItem()){
                            newDifficulty = updateDifficulty(items.get(v), validRIS(rsi.getColumn()), vMPRIS(rsi.getColumn()), 0.0, DELTA);
                            itemMean.increment(newDifficulty);
                        }
                    }
                }

                //update thresholds
                for(String s : thresholds.keySet()){
                    rst = thresholds.get(s);
                    if(!rst.extremeThreshold() && !rst.fixedParameter()) updateThresholds(rst);
                }

                //accept new thresholds and increment delta. Only increments delta for non extreme categories
                for(String s : thresholds.keySet()){
                    maxDelta.increment(thresholds.get(s).acceptProposalThresholds());
                }

                //Recenter proposal difficulties, accept proposal difficulties, and increment delta
                //Extreme items are not recentered, and their change in rho not counted in delta.
                for(VariableName v : items.keySet()){
                    rsi = items.get(v);
                    if(!rsi.extremeItem() && !rsi.droppedItem()){
                        rsi.recenterProposalDifficulty(itemMean.getResult());
                        maxDelta.increment(rsi.acceptProposalDifficulty());
                    }

                    /**
                     * Set new threshold rho in RatingScaleItem object
                     */
                    if(rsi.getNumberOfCategories()>2){
                        rsi.setThresholds(thresholds.get(rsi.getGroupId()).getThresholds());
                    }
                }
            }

            //update persons
            //Change in person parameter is not counted toward delta.
            double tDelta = 0.0;
            if(updatePersons){
                for(int i=0;i<nPeople;i++){
                    if(!extremePersons[i]){
                        tDelta = updatePersons(i, validRawScore(data[i]),  vMPRS(data[i]), DELTA);
                        maxDelta.increment(tDelta);
                    }
                }
            }

            DELTA = maxDelta.getResult();
            maxDelta.clear();
            iterationDelta.add(DELTA);

            //compute residuals for all nonextreme items completed by nonextreme examinees
            //the residual to compute are the expected score (TCC, iTCC) and observed score (RS, RIS)

            iter++;
        }//end JMLE loop
    }

    public void updateExtreme(int globalMaxIter, double globalConvergence, boolean updateItems, boolean updatePersons){
        double DELTA = globalConvergence+1.0; //LCONV
        maxDelta = new Max();
        int iter = 0;

        RatingScaleItem rsi = null;
        while(DELTA >= globalConvergence && iter<globalMaxIter){
            if(updateItems){

                //update items that are not fixed
                for(VariableName v : items.keySet()){
                    rsi = items.get(v);
                    if(rsi.extremeItem()){
                        extremeItemUpdate(items.get(v), adjustedRIS(v), MPRIS(rsi.getColumn()), adjust, DELTA);
                    }
                }

                //Recenter proposal difficulties, accept proposal difficulties, and increment delta
                //Extreme items are not recentered, and their change in rho not counted in delta.
                for(VariableName v : items.keySet()){
                    rsi = items.get(v);
                    if(rsi.extremeItem()) maxDelta.increment(rsi.acceptProposalDifficulty());
                }
            }

            //update persons
            //Change in person parameter is not counted toward delta.
            double tDelta = 0.0;
            if(updatePersons){
                for(int i=0;i<nPeople;i++){
                    if(extremePersons[i]){
                        tDelta = extremePersonUpdate(i, adjustedRawScore(data[i]),  MPRS(data[i]), DELTA);
                        maxDelta.increment(tDelta);
                    }
                }
            }

            DELTA = maxDelta.getResult();
            maxDelta.clear();
            extremeIterationDelta.add(DELTA);
            iter++;
        }//end JMLE loop
    }
    
    /**
     * Adjust items for UCON bias
     */
    public void uconBiasAdjustment(){
        for(VariableName v : items.keySet()){
            items.get(v).biasAdjustment(nItems);
        }
        for(String s : thresholds.keySet()){
            thresholds.get(s).biasAdjustment(nItems);
        }
    }
    
    /**
     * Compute item and threshold standard errors. If correcting item parameter estimates
     * for UCON bias, call uconBiasAdjustment() before calling this method.
     * 
     */
    public void computeItemStandardErrors(){
        //compute item and threshold standard errors
        for(VariableName v : items.keySet()){
            items.get(v).computeStandardError(theta, extremePersons, data);
        }
        for(String s : thresholds.keySet()){
            thresholds.get(s).computeStandardErrors(theta, extremePersons, data);
        }
    }

    public void computeItemFit(){
        RatingScaleItem rsi = null;
        int col = 0;
        for(int i=0;i<data.length;i++){
            if(!extremePersons[i]){
                for(VariableName v : items.keySet()){
                    rsi = items.get(v);
                    col = rsi.getColumn();
                    if(data[i][col]>-1) rsi.incrementFitStatistics(theta[i], data[i][col]);
                }
            }
            
        }
    }

    public void computeCategoryFit(){
        if(thresholds.isEmpty()) return;
        RatingScaleItem rsi = null;
        RatingScaleThresholds rst = null;
        double b = 0.0;
        int col = 0;
        for(int i=0;i<data.length;i++){
            if(!extremePersons[i]){
                for(VariableName v : items.keySet()){
                    rsi = items.get(v);
                    if(rsi.getNumberOfCategories()>2){
                        b = rsi.getDifficulty();
                        col = rsi.getColumn();
                        if(data[i][col]>-1){
                            rst = thresholds.get(rsi.getGroupId());
                            rst.incrementFit(data[i][col], theta[i], b);
                        }
                    }
                }
            }
        }
    }

    public FitStatistics computePersonFit(double theta, byte[] scores){
        FitStatistics fit = new FitStatistics();
        RatingScaleItem rsi = null;
        double difficulty = 0.0;
        double[] itemThresholds = null;
        int col = 0;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            col = rsi.getColumn();
            difficulty = rsi.getDifficulty();
            itemThresholds = rsi.getThresholds();
            if(!rsi.extremeItem() && scores[col]>-1){
                fit.increment(
                    scores[col],
                    rsm.expectedValue(theta, difficulty, itemThresholds),
                    rsm.varianceOfResponse(theta, difficulty, itemThresholds),
                    rsm.kurtosisOfResponse(theta, difficulty, itemThresholds)
                    );
            }
        }
        return fit;
    }

    public FitStatistics computePersonFit(int index){
        return computePersonFit(theta[index], data[index]);
    }

//==============================================================================================================================
// METHODS FOR ITEM AND THRESHOLD PARAMETERS
//==============================================================================================================================

    /**
     * Computes PROX starting values fro items, thresholds, and persons.
     */
    public void prox(){
        Mean m = new Mean();
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            rsi.prox(MPRIS(v), adjustedRIS(v));
            m.increment(rsi.getDifficulty());
        }

        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            rsi.recenter(m.getResult());
            rsi.recenterProposalDifficulty(m.getResult());
        }

        RatingScaleThresholds rst = null;
        for(String s : thresholds.keySet()){
            rst = thresholds.get(s);
            if(!rst.extremeThreshold()){
                rst.categoryProx(Spj(rst.getGroupId()));
                rst.recenterProposalThresholds();
                rst.recenterThresholds();
            }
        }

        for(int i=0;i<nPeople;i++){
            theta[i]=prox(data[i]);
        }

    }

    /**
     * Proportional curve fitting update to the difficulty parameter estimate.
     * Returns two values. The first rho is the new difficulty and the second
     * is the score residual. Both are used in checking for convergence. The
     * score residual is part of the globalResidual test.
     *
     * @param rsi
     * @param Sip item score
     * @param maxSip
     * @param d
     * @return array with new difficulty rho at first index and score residual at second
     */
    private double updateDifficulty(RatingScaleItem rsi, double Sip, double maxSip, double minSip, double d){
        if(rsi.fixedParameter()) return 0.0;
        double iTCC1 = 0.0;
        double iTCC2 = 0.0;
        double proposalDifficulty = 0.0;
        double difficulty = rsi.getDifficulty();
        double[] itemThresholds = rsi.getThresholds();

        for(int i=0;i<theta.length;i++){
            if(data[i][rsi.getColumn()]>-1 && !extremePersons[i]){
                iTCC1+=rsm.expectedValue(theta[i], difficulty, itemThresholds);
                iTCC2+=rsm.expectedValue(theta[i], difficulty+d, itemThresholds);
            }
        }
        double slope = d/(logisticOgive(iTCC2, minSip, maxSip)-logisticOgive(iTCC1, minSip, maxSip));
        double intercept = difficulty - slope*logisticOgive(iTCC1, minSip, maxSip);
        proposalDifficulty = slope*Math.log((Sip-minSip)/(maxSip-Sip))+intercept;
        //do not change theta by more than one logit per iteration - from WINSTEPS documents
        proposalDifficulty = Math.max(Math.min(difficulty+1.0,proposalDifficulty),difficulty-1.0);
        rsi.setProposalDifficulty(proposalDifficulty);
        return proposalDifficulty;
    }

    /**
     * Item difficulty update for extreme items only. It uses all examinees, extreme and nonextreme.
     *
     * @param rsi
     * @param Sip
     * @param maxSip
     * @param d
     * @return
     */
    public double extremeItemUpdate(RatingScaleItem rsi, double Sip, double maxSip, double minSip, double d){
        if(rsi.fixedParameter()) return 0.0;
        double iTCC1 = 0.0;
        double iTCC2 = 0.0;
        double proposalDifficulty = 0.0;
        double difficulty = rsi.getDifficulty();
        double[] itemThresholds = rsi.getThresholds();

        for(int i=0;i<theta.length;i++){
            if(data[i][rsi.getColumn()]>-1){
                iTCC1+=rsm.expectedValue(theta[i], difficulty, itemThresholds);
                iTCC2+=rsm.expectedValue(theta[i], difficulty+d, itemThresholds);
            }
        }
        double xMin = 0.0;
        double slope = d/(logisticOgive(iTCC2, xMin, maxSip)-logisticOgive(iTCC1, xMin, maxSip));
        double intercept = difficulty - slope*logisticOgive(iTCC1, xMin, maxSip);
        proposalDifficulty = slope*Math.log((Sip-xMin)/(maxSip-Sip))+intercept;
        //do not change theta by more than one logit per iteration - from WINSTEPS documents
        proposalDifficulty = Math.max(Math.min(difficulty+1.0,proposalDifficulty),difficulty-1.0);
        rsi.setProposalDifficulty(proposalDifficulty);
        return proposalDifficulty;
    }

    /**
     * Update all thresholds. Only updates thresholds if with nonextreme examinees who completed the item.
     *
     * @param rsThresholds
     */
    private void updateThresholds(RatingScaleThresholds rsThresholds){
        if(rsThresholds.fixedParameter()) return ;
        double Tpj = 0.0;
        double TpjM1 = 0.0;
        byte cat = 0;
        byte catM1 = 0;
        int k = rsThresholds.getNumberOfCategories();
        String groupId = rsThresholds.getGroupId();

        for(byte i=0;i<k;i++){
            if(i==0){
                cat=i;
            }else{
                catM1=cat;
                cat = i;
                TpjM1 = validTpj(catM1, groupId);
                Tpj = validTpj(cat, groupId);
                updateSingleThreshold(rsThresholds, Tpj, TpjM1, i);
            }
        }
        rsThresholds.recenterProposalThresholds();
    }

    /**
     * Updates a single threshold.
     *
     * @param rsThresholds
     * @param Tpj
     * @param TpjM1
     * @param category
     */
    private void updateSingleThreshold(RatingScaleThresholds rsThresholds, double Tpj, double TpjM1, byte category){
        double catKSum = 0.0;
        double catKm1Sum = 0.0;
        double thresh = 0.0;
        double pthresh = 0.0;
        double[] itemDifficulties = rsThresholds.getGroupItemDifficulties();
        double[] itemThresholds = rsThresholds.getThresholds();

        for(int i=0;i<nPeople;i++){
            if(rsThresholds.hasGroupResponse(i, data) && !extremePersons[i]){
                catKSum+=rsm.valueForGroup(theta[i], itemDifficulties, itemThresholds, category);
                catKm1Sum+=rsm.valueForGroup(theta[i], itemDifficulties, itemThresholds, category-1);
            }
        }
        thresh = itemThresholds[category];
        pthresh = thresh - Math.log(Tpj/TpjM1) + Math.log(catKSum/catKm1Sum);
        //do not change threshold by more than one logit - from WINSTEPS documentation
        pthresh = Math.max(Math.min(thresh+1.0,pthresh),thresh-1.0);
        rsThresholds.setProposalThresholdAt(category, pthresh);
    }

//==============================================================================================================================
// METHODS FOR PERSON PARAMETERS
//==============================================================================================================================

    /**
     * PROX calculation of theta. Used for start values.
     *
     */
    private double prox(byte[] scores){
        double MPRS = MPRS(scores);
        double aRS = adjustedRawScore(scores);
        double thetaProx = Math.log(aRS/(MPRS-aRS));
        return thetaProx;
    }


    /**
     * Proportional curve fitting update to the person parameter.
     * Only updates person parameters with nonextreme items completed by the examinee.
     *
     * @param d
     * @return array with change in person parameter estimate at first index and score residual at second index
     */
    private double updatePersons(int personIndex, double rawScore, double MPRS, double d){
        double previousTheta = theta[personIndex];
        double TCC1 = 0.0; //this is the TCC at current theta rho
        double TCC2 = 0.0; //this is the TCC at current theta rho + d

        int col = 0;
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            col = rsi.getColumn();
            if(data[personIndex][col]>-1 && !rsi.extremeItem() && !rsi.droppedItem()){
                TCC1 += rsm.expectedValue(theta[personIndex], rsi.getDifficulty(), rsi.getThresholds());
                TCC2+= rsm.expectedValue(theta[personIndex]+d, rsi.getDifficulty(), rsi.getThresholds());
            }
        }
        double xMin = 0.0;
        double slope = d/(logisticOgive(TCC2, xMin, MPRS)-logisticOgive(TCC1, xMin, MPRS));
        double intercept = theta[personIndex] - slope*logisticOgive(TCC1, xMin, MPRS);
        double tempTheta = slope*logisticOgive(rawScore, xMin, MPRS)+intercept;
        //do not change theta by more than one logit per iteration - from WINSTEPS documents
        theta[personIndex] = Math.max(Math.min(theta[personIndex]+1,tempTheta),theta[personIndex]-1);
        double delta = Math.abs(previousTheta-theta[personIndex]);
        return delta;
    }

    /**
     * Update method for extreme persons only. This method uses all items, extreme and not extreme,
     * completed by the examinee.
     *
     * @param personIndex
     * @param d
     * @return
     */
    private double extremePersonUpdate(int personIndex, double rawScore, double MPRS, double d){
        if(!extremePersons[personIndex]) return 0.0;
        double previousTheta = theta[personIndex];
        double TCC1 = 0.0; //this is the TCC at current theta rho
        double TCC2 = 0.0; //this is the TCC at current theta rho + d

        int col = 0;
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            col = rsi.getColumn();
            if(data[personIndex][col]>-1){
                TCC1 += rsm.expectedValue(theta[personIndex], rsi.getDifficulty(), rsi.getThresholds());
                TCC2+= rsm.expectedValue(theta[personIndex]+d, rsi.getDifficulty(), rsi.getThresholds());
            }
        }
        double aRS = adjustedRawScore(data[personIndex]);

//        double MPRS = MPRS(data[personIndex]);
        double xMin = 0.0;
        double slope = d/(logisticOgive(TCC2, xMin, MPRS)-logisticOgive(TCC1, xMin, MPRS));
        double intercept = theta[personIndex] - slope*logisticOgive(TCC1, xMin, MPRS);
        double tempTheta = slope*logisticOgive(aRS, xMin, MPRS)+intercept;
        //do not change theta by more than one logit per iteration - from WINSTEPS documents
        theta[personIndex] = Math.max(Math.min(theta[personIndex]+1,tempTheta),theta[personIndex]-1);
        double delta = Math.abs(previousTheta-theta[personIndex]);
        return delta;
    }

    /**
     * Local logistic ogive from WINSTEPS documentation.
     *
     * @param x observed score
     * @param xMin minimum possible score
     * @param xMax maximum possible score
     * @return
     */
    private double logisticOgive(double x, double xMin, double xMax){
        return Math.log((x-xMin)/(xMax-x));
    }

    /**
     * Standard error of person parameter estimate.
     *
     * @param row of examinee in data array
     * @return
     */
    public double computePersonStandardError(int row){
        double sum=0.0;
        int col = 0;
        RatingScaleItem rsi = null;

        for(VariableName v : items.keySet()){
            col = items.get(v).getColumn();
            rsi = items.get(v);
            if(data[row][col]>-1){
                sum+=rsm.denomInf(theta[row], rsi.getDifficulty(), rsi.getThresholds());
            }
        }
        if(sum==0.0) return Double.NaN;
        double stdError = 1/Math.sqrt(sum);
        return stdError;
    }

    public LinkedHashMap<VariableName, RatingScaleItem> getItems(){
        return items;
    }

    public LinkedHashMap<String, RatingScaleThresholds> getThresholds(){
        return thresholds;
    }

    public double[] getThetas(){
        return theta;
    }

    public TestFrequencyTable getTable(){
        return table;
    }

    public void linearTransformation(DefaultLinearTransformation lt, int precision){
        Mean pMean = new Mean();
        StandardDeviation pSd = new StandardDeviation();

        //set transformation and rescale persons
        double newScale = lt.getScale();
        double newMean = lt.getIntercept();
        double oldPersonMean = pMean.evaluate(theta);
        double oldPersonSd = pSd.evaluate(theta);

        lt.setScaleAndIntercept(oldPersonMean, newMean, oldPersonSd, newScale);

        for(int i=0;i<theta.length;i++){
            theta[i] = lt.transform(theta[i]);
        }

        //set transformation and rescale items
        Mean iMean = new Mean();
        StandardDeviation iSd = new StandardDeviation();
        double tempDifficulty = 0.0;

        for(VariableName v : items.keySet()){
            tempDifficulty = items.get(v).getDifficulty();
            iMean.increment(tempDifficulty);
            iSd.increment(tempDifficulty);
        }

        lt.setScaleAndIntercept(iMean.getResult(), newMean, iSd.getResult(), newScale);

        for(VariableName v : items.keySet()){
            items.get(v).linearTransformation(lt, precision);
        }

        //set transformation and rescale thresholds
        RatingScaleThresholds tempThresholds = null;

        for(String s : thresholds.keySet()){
            tempThresholds = thresholds.get(s);
            lt.setScaleAndIntercept(tempThresholds.getThresholdMean(), newMean, tempThresholds.getThresholdStandardDeviation(), newScale);
            thresholds.get(s).linearTransformation(lt, precision);
        }
    }

    public ScaleQualityStatistics getItemSideScaleQualitys(){
        ScaleQualityStatistics iStats = new ScaleQualityStatistics();
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            if(!rsi.extremeItem()) iStats.increment(rsi.getDifficulty(), rsi.getDifficultyStandardError());
        }
        return iStats;
    }

    public ScaleQualityStatistics getPersonSideScaleQualitys(){
        ScaleQualityStatistics pStats = new ScaleQualityStatistics();
        for(int i=0;i<nPeople;i++){
            if(!extremePersons[i]) pStats.increment(theta[i], computePersonStandardError(i));
        }
        return pStats;
    }

    public double getThetaAt(int index){
        return theta[index];
    }

    public String printItemStats(String title){
        TextTableColumnFormat[] cformats = new TextTableColumnFormat[7];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setStringFormat(10, OutputAlignment.LEFT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setDoubleFormat(10, 2, OutputAlignment.RIGHT);
        cformats[2] = new TextTableColumnFormat();
        cformats[2].setDoubleFormat(10, 2, OutputAlignment.RIGHT);
        cformats[3] = new TextTableColumnFormat();
        cformats[3].setDoubleFormat(8, 2, OutputAlignment.RIGHT);
        cformats[4] = new TextTableColumnFormat();
        cformats[4].setDoubleFormat(8, 2, OutputAlignment.RIGHT);
        cformats[5] = new TextTableColumnFormat();
        cformats[5].setDoubleFormat(8, 2, OutputAlignment.RIGHT);
        cformats[6] = new TextTableColumnFormat();
        cformats[6].setDoubleFormat(8, 2, OutputAlignment.RIGHT);
        
        int n = items.size();

        TextTable textTable = new TextTable();
        textTable.addAllColumnFormats(cformats, n+5);
        textTable.getRowAt(0).addHeader(0, 8, title, TextTablePosition.CENTER);
        textTable.getRowAt(1).addHorizontalRule(0, 8, "=");
        textTable.getRowAt(2).addHeader(0, 1, "Item", TextTablePosition.LEFT);
        textTable.getRowAt(2).addHeader(1, 1, "Difficulty", TextTablePosition.CENTER);
        textTable.getRowAt(2).addHeader(2, 1, "Std. Error", TextTablePosition.CENTER);
        textTable.getRowAt(2).addHeader(3, 1, "WMS", TextTablePosition.CENTER);
        textTable.getRowAt(2).addHeader(4, 1, "Std. WMS", TextTablePosition.CENTER);
        textTable.getRowAt(2).addHeader(5, 1, "UMS", TextTablePosition.CENTER);
        textTable.getRowAt(2).addHeader(6, 1, "Std. UMS", TextTablePosition.CENTER);
        textTable.getRowAt(3).addHorizontalRule(0, 7, "-");

        int index = 4;
        RatingScaleItem rsi = null;
        for(VariableName v : items.keySet()){
            rsi = items.get(v);
            textTable.getRowAt(index).addStringAt(0, v.toString());
            if(rsi.droppedItem()){
                textTable.getRowAt(index).addHeader(1, 6, "DROPPED", TextTablePosition.CENTER);
            }else{
                textTable.getRowAt(index).addDoubleAt(1, rsi.getDifficulty());
                textTable.getRowAt(index).addDoubleAt(2, rsi.getDifficultyStandardError());
                textTable.getRowAt(index).addDoubleAt(3, rsi.getFitStatistics().getWeightedMeanSquare());
                textTable.getRowAt(index).addDoubleAt(4, rsi.getFitStatistics().getStandardizedWeightedMeanSquare());
                textTable.getRowAt(index).addDoubleAt(5, rsi.getFitStatistics().getUnweightedMeanSquare());
                textTable.getRowAt(index).addDoubleAt(6, rsi.getFitStatistics().getStandardizedUnweightedMeanSquare());
            }
            index++;
        }
        textTable.getRowAt(n+4).addHorizontalRule(0, 7, "=");
        return textTable.toString();
    }

    public String printCategoryStats(){
        if(thresholds.isEmpty()) return "";
        TextTableColumnFormat[] cformats = new TextTableColumnFormat[6];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setStringFormat(10, OutputAlignment.LEFT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setIntFormat(10, OutputAlignment.RIGHT);
        cformats[2] = new TextTableColumnFormat();
        cformats[2].setDoubleFormat(10, 2, OutputAlignment.RIGHT);
        cformats[3] = new TextTableColumnFormat();
        cformats[3].setDoubleFormat(8, 2, OutputAlignment.RIGHT);
        cformats[4] = new TextTableColumnFormat();
        cformats[4].setDoubleFormat(8, 2, OutputAlignment.RIGHT);
        cformats[5] = new TextTableColumnFormat();
        cformats[5].setDoubleFormat(8, 2, OutputAlignment.RIGHT);

        int numRows = 0;
        for(String s : thresholds.keySet()){
            numRows += thresholds.get(s).getNumberOfCategories() + 1;
        }

        TextTable textTable = new TextTable();
        textTable.addAllColumnFormats(cformats, numRows+5);
        textTable.getRowAt(0).addHeader(0, 8, "FINAL JMLE CATEGORY STATISTICS", TextTablePosition.CENTER);
        textTable.getRowAt(1).addHorizontalRule(0, 8, "=");
        textTable.getRowAt(2).addHeader(0, 1, "Group", TextTablePosition.LEFT);
        textTable.getRowAt(2).addHeader(1, 1, "Category", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(2, 1, "Threshold", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(3, 1, "Std. Error", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(4, 1, "WMS", TextTablePosition.RIGHT);
        textTable.getRowAt(2).addHeader(5, 1, "UMS", TextTablePosition.RIGHT);
        textTable.getRowAt(3).addHorizontalRule(0, 6, "-");

        int index = 4;
        RatingScaleThresholds rst = null;
        for(String s : thresholds.keySet()){
            rst = thresholds.get(s);
            for(int i=0;i<rst.getNumberOfCategories();i++){
                if(i==0){
                    textTable.getRowAt(index).addStringAt(0, s);
                    textTable.getRowAt(index).addIntAt(1, i);
                    if(rst.extremeThreshold()){
                        textTable.getRowAt(index).addHeader(2, 4, "DROPPED", TextTablePosition.LEFT);
                    }else{
                        textTable.getRowAt(index).addHeader(2, 4, " ", TextTablePosition.LEFT);
                    }
                }else{
                    textTable.getRowAt(index).addStringAt(0, " ");
                    textTable.getRowAt(index).addIntAt(1, i);
                    if(rst.extremeThreshold()){
                        textTable.getRowAt(index).addHeader(2, 1, " ", TextTablePosition.LEFT);
                        textTable.getRowAt(index).addHeader(3, 1, " ", TextTablePosition.LEFT);
                        textTable.getRowAt(index).addHeader(4, 1, " ", TextTablePosition.LEFT);
                        textTable.getRowAt(index).addHeader(5, 1, " ", TextTablePosition.LEFT);
                    }else{
                        textTable.getRowAt(index).addDoubleAt(2, rst.getThresholdAt(i));
                        textTable.getRowAt(index).addDoubleAt(3, rst.getStandardErrorAt(i));
                        textTable.getRowAt(index).addDoubleAt(4, rst.getCategoryFitAt(i).getWeightedMeanSquare());
                        textTable.getRowAt(index).addDoubleAt(5, rst.getCategoryFitAt(i).getUnweightedMeanSquare());
                    }
                }
                index++;
            }
            
            textTable.getRowAt(index).addHeader(0, 6, " ", TextTablePosition.LEFT);//add empty row
            index++;
        }
        textTable.getRowAt(numRows+3).addHorizontalRule(0, 6, "=");
        return textTable.toString();
    }


    /**
     * Publishes global iteration information in the jmetrik log.
     */
    public String printIterationSummary(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        int iter = 1;

        f.format("%-35s", "RASCH ANALYSIS ITERATION SUMMARY");f.format("%n");
        f.format("%10s", "Iteration"); f.format("%5s", "");f.format("%10s", "Max Change");f.format("%n");
        f.format("%-35s","===================================");f.format("%n");

        for(Double d : iterationDelta){
            f.format("%10d", iter); f.format("%5s", "");
            f.format("%10.6f", d);
            f.format("%n");
            iter++;
        }

        if(extremeIterationDelta.size()>0){
            iter = 0;
            
            f.format("%n");
            f.format("%n");
            f.format("%-35s", "EXTREME ITEM/PERSON ITERATION SUMMARY");f.format("%n");
            f.format("%10s", "Iteration"); f.format("%5s", "");f.format("%10s", "Max Change");f.format("%n");
            f.format("%-35s","===================================");f.format("%n");

            for(Double d : extremeIterationDelta){
                f.format("%10d", iter); f.format("%5s", "");
                f.format("%10.6f", d);
                f.format("%n");
                iter++;
            }
        }

        return f.toString();
    }

}

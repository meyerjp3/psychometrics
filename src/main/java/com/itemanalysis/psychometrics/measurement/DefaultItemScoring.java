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
package com.itemanalysis.psychometrics.measurement;

import com.itemanalysis.psychometrics.data.Category;
import com.itemanalysis.psychometrics.data.VariableType;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The default implementation of the ItemScoring interface. It maintains information
 * about the item and scores examinees responses. It also includes special codes
 * for omitted and not reached responses. These types of response codes are either
 * treated as missing data or scored as zero points. Missing data retun a value of
 * Double.NaN.
 *
 */
public class DefaultItemScoring implements ItemScoring{
    
    public TreeMap<Object, Category> categories = null;

    /**
     * Largest obtainable score on the item
     */
    private Max maximumPossibleScore = null;

    /**
     * Smallest possible score on the item
     */
    private Min minimumPossibleScore = null;

    /**
     * True if item is scored in one of only two possible ways.
     */
    private int binaryScoring = 0;

    /**
     * Code that represents an omitted item response
     */
    private Object omitCode = null;

    /**
     * Code that represents an item that was not reached by an examinees.
     * an item is not reached if the item before it has a response but
     * no item after it has a response.
     *
     */
    private Object notReachedCode = null;

    public DefaultItemScoring(){
        categories = new TreeMap<Object, Category>();
        maximumPossibleScore = new Max();
        minimumPossibleScore = new Min();
    }

    public void addCategory(Category cat){
        categories.put(cat.responseValue(), cat);
        maximumPossibleScore.increment(cat.scoreValue());
        minimumPossibleScore.increment(cat.scoreValue());
    }

    public void removeCategory(Category cat){
        categories.remove(cat);
    }

    public void clearCategory(){
        this.categories = null;
        binaryScoring = 0;
        maximumPossibleScore = new Max();
        minimumPossibleScore = new Min();
        categories = new TreeMap<Object, Category>();
    }

    /**
     * Sets the omit code. The code will be set to null if
     * (a) the omit code is an empty string, or
     * (b) the code is a string when the data type is double.
     *
     * @param omitCode
     * @param type
     */
    public void setOmitCode(Object omitCode, VariableType type){
        String s = omitCode.toString();
        if("".equals(s)){
            this.omitCode = null;
            return;
        }

        try{
            Double d = Double.valueOf(s);

            //code is double
            if(type.getDataType()==VariableType.DOUBLE){
                this.omitCode = d;
            }else{
                this.omitCode = s;
            }
        }catch(NumberFormatException ex){
            //code is string
            if(type.getDataType()==VariableType.DOUBLE){
                this.omitCode = null;
            }else{
                this.omitCode = s;
            }
        }

    }

    /**
     * Sets the not reached code. The code will be set to null if
     * (a) the code is an empty string, or
     * (b) the code is a string when the data type is double.
     *
     * @param notReachedCode
     * @param type
     */
    public void setNotReachedCode(Object notReachedCode, VariableType type){
        String s = notReachedCode.toString();
        if("".equals(s)){
            this.notReachedCode = null;
            return;
        }

        try{
            Double d = Double.valueOf(s);

            //code is double
            if(type.getDataType()==VariableType.DOUBLE){
                this.notReachedCode = d;
            }else{
                this.notReachedCode = s;
            }
        }catch(NumberFormatException ex){
            //code is string
            if(type.getDataType()==VariableType.DOUBLE){
                this.notReachedCode = null;
            }else{
                this.notReachedCode = s;
            }
        }
    }

    public Object getOmitCode(){
        return omitCode;
    }

    public Object getNotReachedCode(){
        return notReachedCode;
    }

    public void clearOmittedAndNotReachedCodes(){
        this.omitCode = null;
        this.notReachedCode = null;
    }

    public int numberOfCategories(){
        return categories.size();
    }

    public double maximumPossibleScore(){
        return maximumPossibleScore.getResult();
    }

    public double minimumPossibleScore(){
        return minimumPossibleScore.getResult();
    }

    /**
     * Return category iterator for binary and polytomous items.
     * No categories exist for continuous items.
     *
     * @return
     */
    public Iterator<Object> categoryIterator(){
        return categories.keySet().iterator();
    }

    public String getCategoryScoreString(Object response){
        Category c = categories.get(response);
        if(c==null) return "";
        String s = c.responseValue().toString() + "(" + c.scoreValue() + ")";
        return s;
    }

    /**
     * Missing responses, omitted responses, and not reached responses are scored as 0 points.
     *
     * @param response item response
     * @return item score
     */
    public double computeItemScore(Object response){
        return computeItemScore(response, true, true, true);
    }

    /**
     * Missing responses, omitted responses, and not reached responses are treated the same way.
     *
     *
     * @param response item response
     * @param scoreAsZero Score all special codes as 0 points if true. Score as Double.NaN otherwise.
     * @return item score
     */
    public double computeItemScore(Object response, boolean scoreAsZero){
        return computeItemScore(response, scoreAsZero, scoreAsZero, scoreAsZero);
    }

    /**
     * Main item score method. It handles missing data, omitted, and not reached responses
     * differently depending on user options. The value Double.NaN is used to indicate missing data.
     * This value must be handled by the calling method.
     *
     * @param response item response
     * @param missingZero score missing responses as zero points if true. Otherwise, score as Double.NaN.
     * @param omitZero  score omitted responses as zero points if true. Otherwise, score as Double.NaN.
     * @param notreachedZero  score not reached responses as zero points if true. Otherwise, score as Double.NaN.
     * @return item score.
     */
    public double computeItemScore(Object response, boolean missingZero, boolean omitZero, boolean notreachedZero){
        if(response==null){
            if(missingZero) return 0.0;
            else return Double.NaN;
        }

        if(omitCode!=null && omitCode.equals(response)){
            if(omitZero) return 0.0;
            else return Double.NaN;
        }

        if(notReachedCode!=null && notReachedCode.equals(response)){
            if(notreachedZero) return 0.0;
            else return Double.NaN;
        }

        double score = Double.NaN;
        Category temp = categories.get(response);
        if(temp==null){
            //undefined categories scored same as missing data
            if(missingZero) return 0.0;
            else return Double.NaN;
        }else{
            score = temp.scoreValue();
        }
        return score;
    }

    /**
     * Computes the item score such that missing data and special codes are scored as zero points.
     *
     * @param response examinee response
     * @param type variable type is either string or double.
     * @return item score
     */
    public double computeItemScore(Object response, VariableType type){
        return computeItemScore(response, type, true, true, true);
    }

    /**
     * Computes the item score. If scoreAsZero is false and the response is null or
     * is one of the special codes (i.e. omitted or not reached) then this method
     * will return Double.NaN. If scoreAsZero is true, then this method will return
     * a value of 0 for a null response or a response that equals on of the special
     * codes.
     *
     * @param response examinee response.
     * @param type variable type is either double or string.
     * @param scoreAsZero true if missing data and special codes should be scored as zero points.
     * @return item score.
     */
    public double computeItemScore(Object response, VariableType type, boolean scoreAsZero){
        return computeItemScore(response, type, scoreAsZero, scoreAsZero, scoreAsZero);
    }

    /**
     * Primary method for scoring an item. It allows each special code (missing, omitted, and not reached)
     * to be handled differently. The method will handle continuous items and make the appropriate data
     * conversion first. If the item is not continuous, it will call the item scoring method.
     *
     * @param response item response
     * @param type type of variable
     * @param missingZero Score missing response as zero points if true. Score as Double.NaN otherwise.
     * @param omitZero  Score omitted response as zero points if true. Score as Double.NaN otherwise.
     * @param notreachedZero  Score not reached response as zero points if true. Score as Double.NaN otherwise.
     * @return item score.
     */
    public double computeItemScore(Object response, VariableType type, boolean missingZero, boolean omitZero, boolean notreachedZero){
        if(type.getItemType()== VariableType.CONTINUOUS_ITEM && type.getDataType()==VariableType.DOUBLE){
            return Double.parseDouble(response.toString());
        }
        if(type.getItemType()==VariableType.BINARY_ITEM || type.getItemType()==VariableType.POLYTOMOUS_ITEM){
            return computeItemScore(response, missingZero, omitZero, notreachedZero);
        }
        return Double.NaN;
    }

    /**
     * Returns 1 if response == categoryId and 0 otherwise.
     *
     * @param categoryId
     * @param response
     * @return
     */
    public double computeCategoryScore(Object categoryId, Object response){
        Category temp = categories.get(categoryId);
        return temp.categoryScore(response);
    }

    /**
     * This method return the answer key for an item. the answer key is
     * the category code with the highest score values. For a multiple-choice
     * question it will return the response options with a score of, say, 1
     * where all other scores are 0. For a polytomous item it will return a
     * plus sign to indicate increasing order and a minus sign to indicate
     * reverse order. jMetrik uses this method to populate a table with
     * the answer key.
     *
     * @return answer key
     */
    public String getAnswerKey(){
        Category cat;
        double scoreValue = 0;
        double maxScore = Double.NEGATIVE_INFINITY;
        String answerKey = "";
        if(binaryScoring()){
            for(Object o : categories.keySet()){
                cat = categories.get(o);
                scoreValue = cat.scoreValue();
                if(scoreValue>maxScore){
                    maxScore = scoreValue;
                    answerKey = o.toString();
                }
            }
        }else{
            //determine if polytomous item is in ascending order or reverse order
            Set<Object> keySet = categories.keySet();
            Object[] obj = keySet.toArray();
            Arrays.sort(obj);

            Category cat1 = categories.get(obj[0]);
            Category cat2 = categories.get(obj[obj.length-1]);

            if(cat1.scoreValue()<=cat2.scoreValue()){
                answerKey = "+";//ascending order
                double min = minimumPossibleScore();
                if(min!=1) answerKey += (int)min;
            }else{
                answerKey = "-";//reverse order
                double max = maximumPossibleScore();
                if(max!=numberOfCategories()) answerKey += (int)max;
            }

        }

        return answerKey;
    }

    /**
     * Parses string from syntax file or database.
     * The original responses should be a comma delimited list
     * enclosed in paretheses, REGEX=\\((.+?(?=,|\\)))\\). The score values should also be
     * a comma delimited list enclosed in paretheses. The score rho
     * list can only contain numbers, REGEX=\\(([[-+]?[0-9]*\\.?[0-9]+(?=,|\\))]+?)\\).
     * Leading and trailing white spaces within each list are NOT permitted.
     * The original responses should be listed first, followed by
     * the score values. Both lists must be enclosed in parentheses. 
     * This format allows an item to have one or more correct responses, partial
     * credit assigned to any or all responses, and polytomous item
     * categories to be collapsed.
     *
     * For example, (A,B,C,D) (0,1,0,0)
     * indicates the original responses A, B, C, and D are scored
     * 1, 0, 0, 0 (i.e. A is correct all others are incorrect).
     * As another example (A,B,C,D) (0,1,0,1) indicates
     * that the original values A and D are scored 1 but C and D are
     * scored 0. For polytomous items, (1,2,3,4) (1,2,3,4) indicates
     * that the responses 1, 2, 3, and 4 are scored 1, 2, 3 and 4.
     * A polytomous item category may also be  collapsed. For example,
     * (1,2,3,4) (1,2,3,3) indicates that responses or 3 and 4 are scored 3,
     * thereby collapsing the third and fourth categories.
     *
     * INCORRECT: (A, B, C, D)(0, 1, 0, 0) because leading and trailing
     * spaces within the list are not permitted.
     *
     * Original and scored lists will be forced to have equal length by
     * adding spaces or zeros to the end of a list until both lists
     * are of equal length.
     *
     *
     */
    public int addAllCategories(String optionScoreKey, VariableType type){
        if(type.getItemType()==VariableType.CONTINUOUS_ITEM && optionScoreKey.trim().equals("")) {
            clearCategory();
            return VariableType.CONTINUOUS_ITEM;
        }
        if(optionScoreKey.trim().equals("")) return VariableType.NOT_ITEM;
        /**
         * regular expression for (A,B,C,D) (0,1,0,0)
         * Careful it erroneously also matches trailing commas (A,B,C,D,) (0,1,0,0,)
         *
         * Group 1 = original rho list, \\((.+?(?=,|\\)))\\)
         * Group 2 = score rho list. Can only include numbers, \\(([[-+]?[0-9]*\\.?[0-9]+(?=,|\\))]+?)\\)
         * Both lists must be enclosed in parentheses.
         *
         */
        String REGEX = "\\((.+?(?=,|\\)))\\)\\s*\\(([[-+]?[0-9]*\\.?[0-9]+(?=,|\\))]+?)\\)";
        Pattern pattern = Pattern.compile(REGEX);
        String clean = optionScoreKey.trim();
        Matcher matcher = pattern.matcher(clean);
        int matchCount = 0;
        String original = "";
        String scoring = "";
        while(matcher.find()){
            original = matcher.group(1);
            scoring = matcher.group(2);
            matchCount++;
        }
        if(matchCount!=1) return VariableType.NOT_ITEM;//none or multiple matches found - should only be one match - format problem

        if(original.trim().equals("") || scoring.trim().equals("")){
            clearCategory();
            return VariableType.NOT_ITEM;
        }

        String[] orig = original.split(",");
        String[] scor = scoring.split(",");

        int origLength = orig.length;
        int scorLengh = scor.length;
        int maxLength = Math.max(origLength, scorLengh);
        String[] newOrig = newOrig = new String[maxLength];
        String[] newScor = newScor = new String[maxLength];

        int counter = 0;
        if(maxLength==origLength && maxLength==scorLengh) {
            for(int i=0;i<maxLength;i++){
                newOrig[i]=orig[i].trim();
                newScor[i]=scor[i].trim();
            }
        }else if(maxLength==scorLengh){
            //scor is longer
            counter = 0;
            for(int i=0;i<maxLength;i++){
                if(counter<origLength){
                    newOrig[i]=orig[counter].trim();
                    newScor[i]=scor[i].trim();
                } else{
                    newOrig[i] = " ";
                    newScor[i] = "0";
                }

                counter++;
            }
        }else{
            //orig is longer
            counter = 0;
            for(int i=0;i<maxLength;i++){
                if(counter<scorLengh){
                    newOrig[i]=orig[i].trim();
                    newScor[i]=scor[counter].trim();
                }else{
                    newOrig[i]=orig[i].trim();
                    newScor[i] = "0";
                }
                counter++;
            }
        }

        for(int i=0;i<maxLength;i++){
            Category cat = null;
            Double cScore = null;
            if(type.getDataType()==VariableType.DOUBLE && !newOrig[i].equals("")){
                cScore = Double.parseDouble(newScor[i]);
                cat = new Category(Double.parseDouble(newOrig[i]), cScore);
            }else{
                cScore = Double.parseDouble(newScor[i]);
                cat = new Category(newOrig[i], cScore);
            }
            this.addCategory(cat);
            if(cScore==0 || cScore==1){
                binaryScoring+=0;
            }else{
                binaryScoring++;
            }

        }
        if(binaryScoring==0)return VariableType.BINARY_ITEM;
        return VariableType.POLYTOMOUS_ITEM;
    }

    private int charaterCount(String text, char target){
        char[] characters = text.toCharArray();
        int count = 0;
        for(int i=0; i<characters.length;i++){
            if(characters[i]==target) count++;
        }
        return count;
    }

    /**
     * Determine whether the item scoring is binary or polytomous.
     *
     * @return true if binary scoring, false otherwise.
     */
    public boolean binaryScoring(){
        return binaryScoring==0;
    }

    /**
     * this method creates a String that represents the item scoring. It
     * is referred to as the score string.
     *
     * @return
     */
    public String printOptionScoreKey(){
        if(categories.size()==0) return "";
        String tempCat = "";
        String catOrig = "(";
        String catScor = "(";
        String finString = "";
        Iterator<Object> iter = categories.keySet().iterator();
        Object obj = null;
        Category temp = null;
        int nullCategory = 0;
        while(iter.hasNext()){
            obj = iter.next();
            temp = categories.get(obj);
            tempCat = temp.responseValue().toString();
            if(tempCat == null || tempCat.equals("")) nullCategory++;
            catOrig += tempCat;
            catScor += Double.valueOf(temp.scoreValue()).toString();
            if(iter.hasNext()){
                catOrig += ",";
                catScor += ",";
            }else{
                catOrig += ")";
                catScor += ")";
            }
        }
        finString = catOrig + " " + catScor;

        return finString;
    }

    /**
     * this method creates a double array of all existing options scores. The
     * options themselves are not returned. Only the scores are returned.
     *
     * @return array of options scores.
     */
    public Double[] scoreArray(){
        Double[] s = new Double[categories.size()];
        Iterator<Object> iter = categories.keySet().iterator();
        Object obj = null;
        Category temp = null;
        int index=0;
        while(iter.hasNext()){
            obj = iter.next();
            temp = categories.get(obj);
            s[index] = temp.scoreValue();
            index++;
        }
        Arrays.sort(s);
        return s;
    }

}

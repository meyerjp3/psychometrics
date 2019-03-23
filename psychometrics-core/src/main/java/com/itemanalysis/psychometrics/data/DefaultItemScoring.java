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
package com.itemanalysis.psychometrics.data;

import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.util.Pair;

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

    public TreeMap<Object, Category> categoryMap = null;

    /**
     * Largest obtainable score on the item
     */
    private Max maximumPossibleScore = null;

    /**
     * Smallest possible score on the item
     */
    private Min minimumPossibleScore = null;

    private TreeSet<Double> scoreLevels = null;

    private DefaultSpecialDataCodes specialDataCodes = null;

    private boolean isContinuous = false;

    private VariableName variableName = null;

    public DefaultItemScoring(){
        this(false);
    }

    public DefaultItemScoring(boolean isContinuous){
        this.isContinuous = isContinuous;
        categoryMap = new TreeMap<Object, Category>(new ItemResponseComparator());
        maximumPossibleScore = new Max();
        minimumPossibleScore = new Min();
        specialDataCodes = new DefaultSpecialDataCodes();
        scoreLevels = new TreeSet<Double>();
        variableName = new VariableName("");//To be consistent with past usage of this class.
    }

    public void addCategory(Category cat){
        categoryMap.put(cat.responseValue(), cat);
        maximumPossibleScore.increment(cat.scoreValue());
        minimumPossibleScore.increment(cat.scoreValue());
        scoreLevels.add(cat.scoreValue());
    }

    public void removeCategory(Category cat){
        categoryMap.remove(cat);
        scoreLevels.remove(cat.scoreValue());
    }

    public void addCategory(Object categoryID, double scoreValue){
        categoryMap.put(categoryID, new Category(categoryID, scoreValue));
        maximumPossibleScore.increment(scoreValue);
        minimumPossibleScore.increment(scoreValue);
        scoreLevels.add(scoreValue);
    }

    public void clearCategory(){
        this.categoryMap.clear();
        this.scoreLevels.clear();
        this.categoryMap.clear();
        maximumPossibleScore = new Max();
        minimumPossibleScore = new Min();
    }

    /**
     * Gets the number of response options.
     *
     * @return number of response options.
     */
    public int numberOfCategories(){
        return categoryMap.size();
    }

    public void isContinuous(boolean isContinuous){
        this.isContinuous = isContinuous;
    }

    public boolean isContinuous(){
        return isContinuous;
    }

    /**
     * Gets the number of score levels. For a polytomous item, this number will be the same as the value returned by
     * {@link #numberOfCategories()} unless the score categories are collapsed. With collapsed score categories,
     * the number of score levels will be less than the number of response options.
     *
     * @return number of score levels.
     */
    public int numberOfScoreLevels(){
        return scoreLevels.size();
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
        return categoryMap.keySet().iterator();
    }

    public String getCategoryScoreString(Object response){
        Category c = categoryMap.get(response);
        if(c==null) return "";
        String s = c.responseValue().toString() + "(" + c.scoreValue() + ")";
        return s;
    }

    public void setSpecialDataCodes(DefaultSpecialDataCodes specialDataCodes){
        this.specialDataCodes = specialDataCodes;
    }

    /**
     * Missing responses, omitted responses, and not reached responses are scored according to the value in
     * the SepcialDataCodes object.
     *
     * @param response an item response that is either a Double or String
     * @return item score
     */
    public double computeItemScore(Object response){
        if(response==null){
            return specialDataCodes.computeMissingScore(DefaultSpecialDataCodes.PERMANENT_MISSING_DATA_CODE);
        }

        if(specialDataCodes.isMissing(response)){
            return specialDataCodes.computeMissingScore(response);
        }

        if(isContinuous){
            return Double.parseDouble(response.toString());
        }

        double score = Double.NaN;
        Category temp = categoryMap.get(response);

        if(temp==null){
            //undefined categories scored same as missing data
            return specialDataCodes.computeMissingScore(DefaultSpecialDataCodes.PERMANENT_MISSING_DATA_CODE);//return missing score
        }else{
            score = temp.scoreValue();
        }
        return score;
    }

    /**
     * Creates an array of category keys and their corresponding score for the response.
     * They are stored in an array of Pairs so that the category ID and score are kept together.
     *
     * @param response an item response
     * @return pair of category value score pairs
     */
    public Pair[] computeScoreVector(Object response){
        Pair[] pair = new Pair[this.numberOfCategories()];
        Pair tempPair = null;
        double score = 0;
        int index = 0;
        for(Object obj : categoryMap.keySet()){
            score = computeCategoryScore(obj, response);
            tempPair = new Pair(obj, score);
            pair[index] = tempPair;
            index++;
        }
        return pair;
    }

    /**
     * Returns 1 if response == categoryId and 0 otherwise.
     *
     * @param categoryId category ID
     * @param response response value
     * @return category score
     */
    public double computeCategoryScore(Object categoryId, Object response){
        if(categoryId.equals(response)) return 1.0;
        return 0.0;
//        Category temp = categories.get(categoryId);
//        return temp.categoryScore(response);
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
//        Category cat;
        double scoreValue = 0;
        double maxScore = Double.NEGATIVE_INFINITY;
        String answerKey = "";
        if(binaryScoring()){
//            for(Object o : categories.keySet()){
//                cat = categories.get(o);
//                scoreValue = cat.scoreValue();
            for(Object o : categoryMap.keySet()){
                scoreValue = categoryMap.get(o).scoreValue();
                if(scoreValue>maxScore){
                    maxScore = scoreValue;
                    answerKey = o.toString();
                }
            }
        }else{
            //determine if polytomous item is in ascending order or reverse order
            Set<Object> keySet = categoryMap.keySet();
            Object[] obj = keySet.toArray();
            Arrays.sort(obj);

            Category cat1 = categoryMap.get(obj[0]);
            Category cat2 = categoryMap.get(obj[obj.length-1]);

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
    public ItemType addAllCategories(String optionScoreKey, DataType type){
        if(type==DataType.DOUBLE && optionScoreKey.trim().equals("")) {
            clearCategory();
            isContinuous = true;
            return ItemType.CONTINUOUS_ITEM;
        }
        if(optionScoreKey.trim().equals("")) return ItemType.NOT_ITEM;
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
        if(matchCount!=1) return ItemType.NOT_ITEM;//none or multiple matches found - should only be one match - format problem

        if(original.trim().equals("") || scoring.trim().equals("")){
            clearCategory();
            return ItemType.NOT_ITEM;
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
            if(type==DataType.DOUBLE && !newOrig[i].equals("")){
                cScore = Double.parseDouble(newScor[i]);
                cat = new Category(Double.parseDouble(newOrig[i]), cScore);
            }else{
                cScore = Double.parseDouble(newScor[i]);
                cat = new Category(newOrig[i], cScore);
            }
            this.addCategory(cat);

//            if(cScore==0 || cScore==1){
//                binaryScoring+=0;
//            }else{
//                binaryScoring++;
//            }

        }
//        if(binaryScoring==0)return ItemType.BINARY_ITEM;
//        return ItemType.POLYTOMOUS_ITEM;
        return getItemType();
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
//        return binaryScoring==0;
        return getItemType()==ItemType.BINARY_ITEM;
    }

    /**
     * An item type is defined by its scoring.
     * A NOT_ITEM has no scoring assigned to the category.
     * A BINARY_ITEM has only two score levels with values 0 and 1.
     * A POLYTOMOUS_ITEM has more than two levels and each level is an integer.
     * A CONTINUOUS_ITEM has more than two levels and each level is a real number. Or, it is an item
     * set as continuous in the constructor or parsing of a score string.
     *
     * @return type of item
     */
    public ItemType getItemType(){
//        if(categories==null || categories.size()==0){
        if(categoryMap==null || categoryMap.size()==0){
            return ItemType.NOT_ITEM;
        }else if(minimumPossibleScore()==0 && maximumPossibleScore()==1 && scoreLevels.size()==2) {
            return ItemType.BINARY_ITEM;
        }else{
            if(isContinuous) return ItemType.CONTINUOUS_ITEM;
            for(Double d : scoreLevels){
                if(d!= Math.floor(d)) return ItemType.CONTINUOUS_ITEM;
            }
            return ItemType.POLYTOMOUS_ITEM;
        }
    }

    /**
     * this method creates a String that represents the item scoring. It
     * is referred to as the score string.
     *
     * @return string representation of option score key
     */
    public String printOptionScoreKey(){
//        if(categories.size()==0) return "";
        if(categoryMap.size()==0) return "";
//        String tempCat = "";
        String catOrig = "(";
        String catScor = "(";
        String finString = "";
        Iterator<Object> iter = categoryMap.keySet().iterator();
        Object obj = null;
        Category temp = null;
        String tempCat = "";
        int nullCategory = 0;
        while(iter.hasNext()){
            obj = iter.next();
            temp = categoryMap.get(obj);
            tempCat = temp.responseValue().toString();

            if(tempCat == null || tempCat.equals("")) nullCategory++;
//            if(temp == null || Double.isNaN(temp)) nullCategory++;
//            catOrig += obj.toString();
//            catScor += temp.toString();
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
    public double[] scoreArray(){

        double[] s = new double[categoryMap.size()];
        Iterator<Object> iter = categoryMap.keySet().iterator();
        Category temp = null;
        int index=0;
        while(iter.hasNext()){
            temp = categoryMap.get(iter.next());
            s[index] = temp.scoreValue();
            index++;
        }
        Arrays.sort(s);
        return s;
    }

    public VariableName getName(){
        return variableName;
    }

    /**
     * Sort Strings before Doubles.
     */
    public class ItemResponseComparator implements Comparator<Object>{
        public int compare(Object obj1, Object obj2){
            if(obj1 instanceof Double && obj2 instanceof Double){
                return ((Double)obj1).compareTo((Double)obj2);
            }else if(obj1 instanceof String && obj2 instanceof Double){
                return 1;
            }else if(obj1 instanceof Double && obj2 instanceof String){
                return -1;
            }else{
                return obj1.toString().compareTo(obj2.toString());
            }
        }

        public boolean equals(Object obj){
            if(obj instanceof ItemResponseComparator){
                return true;
            }
            return false;

        }
    }


}

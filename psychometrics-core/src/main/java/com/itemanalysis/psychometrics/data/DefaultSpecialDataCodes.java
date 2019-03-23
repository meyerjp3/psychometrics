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
package com.itemanalysis.psychometrics.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultSpecialDataCodes implements SpecialDataCodes{

    public final static String PERMANENT_MISSING_DATA_CODE = "";

    private Object missingDataCode = "NA";

    private Object notReachedCode = "NR";

    private Object omittedCode = "OM";

    private double missingDataScore = 0;

    private double notReachedScore = 0;

    private double omittedScore = 0;

    public DefaultSpecialDataCodes(){

    }

    /**
     * Creates an object using the variable name and code string. The code string
     * if formatted like (NA,OM,NR)(-1,-1,-1)
     * @param codeString
     */
    public DefaultSpecialDataCodes(String codeString){
        parseSpecialCodeString(codeString);
    }

    /**
     * Parses the code string that has the format (NA,OM,NR)(-1,-1,-1).
     * The comma separated values in the first set of parentheses are the special
     * data codes for missing, omitted, and not reached responses, respectively.
     * The comma separated list in the second set of parentheses are the points
     * assigned to each code. A score of -1 indicates that the response should be ignored.
     *
     * Typically, the score will either be -1 (ignore) or 0 (score as zero points).
     *
     * @param codeString
     */
    public void parseSpecialCodeString(String codeString){
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
        String clean = codeString.trim();
        Matcher matcher = pattern.matcher(clean);
        int matchCount = 0;
        String original = "";
        String scoring = "";
        while(matcher.find()){
            original = matcher.group(1);
            scoring = matcher.group(2);
            matchCount++;
        }
        if(matchCount!=1) return;//none or multiple matches found. Use default.

        if(original.trim().equals("") || scoring.trim().equals("")) return; //Nothing found. Use default.

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
        }else {
            //orig is longer
            counter = 0;
            for (int i = 0; i < maxLength; i++) {
                if (counter < scorLengh) {
                    newOrig[i] = orig[i].trim();
                    newScor[i] = scor[counter].trim();
                } else {
                    newOrig[i] = orig[i].trim();
                    newScor[i] = "0";
                }
                counter++;
            }
        }

        missingDataCode = newOrig[0];
        omittedCode = newOrig[1];
        notReachedCode = newOrig[2];

        missingDataScore = Double.parseDouble(newScor[0]);
        omittedScore = Double.parseDouble(newScor[1]);
        notReachedScore = Double.parseDouble(newScor[2]);

    }

    /**
     * Not implemented here. This method is only used in version 5 or later.
     * @param text missing code test
     */
    public void addMissingDataCode(String text){
        throw new UnsupportedOperationException();
    }

    public void setMissingDataCode(Object missingDataCode){
        this.missingDataCode = missingDataCode;
    }

    public void setOmittedCode(Object omittedCode){
        this.omittedCode = omittedCode;
    }

    public void setNotReachedCode(Object notReachedCode){
        this.notReachedCode = notReachedCode;
    }

    public void setMissingDataScore(double missingDataScore){
        this.missingDataScore = missingDataScore;
    }

    public void setOmittedScore(double omittedScore){
        this.omittedScore = omittedScore;
    }

    public void setNotReachedScore(double notReachedScore){
        this.notReachedScore = notReachedScore;
    }

    public Object getMissingDataCode(){
        return missingDataCode;
    }

    public Object getOmittedCode(){
        return omittedCode;
    }

    public Object getNotReachedCode(){
        return notReachedCode;
    }

    public boolean isMissing(String response){
        String s = response.trim();
        if(PERMANENT_MISSING_DATA_CODE.equals(s) || missingDataCode.equals(s) ||
                omittedCode.equals(s) || notReachedCode.equals(s)){
            return true;
        }
        return false;
    }

    public boolean isMissing(Object object){
        String s = object.toString();
        return isMissing(s);
    }

    public boolean isMissing(double response){
        String s = Double.valueOf(response).toString();
        return isMissing(s);
    }

    public boolean isMissing(int response){
        String s = Integer.valueOf(response).toString();
        return isMissing(s);
    }

    public boolean isPresent(String response){
        return !isMissing(response);
    }

    public boolean isPresent(Object object){
        return !isMissing(object);
    }

    public boolean isPresent(double response){
        return !isMissing(response);
    }

    public boolean isPresent(int response){
        return !isMissing(response);
    }

    public boolean isOmitted(Object response){
        return omittedCode.equals(response);
    }

    public boolean isNotReached(Object response){
        return notReachedCode.equals(response);
    }

    /**
     * Computes the score associated with a missing data code. If no matching code is found then
     * it returns -1. You should check for missing data with isMissing() before calling this method.
     * Otherwise, calculation errors may occur because -1 returned when a different (nonmissing)
     * value should be returned.
     *
     * @param response
     * @return
     */
    public double computeMissingScore(String response){
        String s = response.trim();
        if(PERMANENT_MISSING_DATA_CODE.equals(s)){
            return missingDataScore;
        }else if(missingDataCode.equals(s)){
            return missingDataScore;
        }else if(omittedCode.equals(s)){
            return omittedScore;
        }else if(notReachedCode.equals(s)){
            return notReachedScore;
        }
        //Response is a legitimate, nonmissing value.
        return -1;
    }

    public double computeMissingScore(Object response){
        String s = response.toString();
        return computeMissingScore(s);
    }

    public double computeMissingScore(double response){
        String s = Double.valueOf(response).toString();
        return computeMissingScore(s);
    }

    public double computeMissingScore(int response){
        String s = Integer.valueOf(response).toString();
        return computeMissingScore(s);
    }



    /**
     * Prints the code string.
     *
     * @return
     */
    @Override
    public String toString(){
        String s = "(" + missingDataCode + "," + notReachedCode + "," + omittedCode + ")";
        s += "(" + missingDataScore + "," + notReachedScore + "," + omittedScore + ")";
        return s;
    }


}

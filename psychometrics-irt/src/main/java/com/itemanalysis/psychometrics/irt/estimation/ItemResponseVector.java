/*
 * Copyright 2013 J. Patrick Meyer
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

package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A class for storing the item response vector and frequency counts. This class is designed
 * for storing summary information for marginal maximum likelihood estimation. It is also
 * designed for use with IRT Person scoring.
 *
 * The item response vector should be ordered integers starting at 0. The code -1 indicates
 * a missing response.
 *
 */
public class ItemResponseVector implements Comparable<ItemResponseVector>{

    /**
     * The item response vector.
     */
    protected byte[] response = null;

    /**
     * Number of times the response vector is observed in the sample. It could also be a smapling weight.
     */
    protected double freq = 1;

    /**
     * Number of non-missing item responses.
     */
    protected double validResponses = 0.0;

    /**
     * Sum of the response vector, excluding missing responses.
     */
    protected double sumScore = 0.0;

    /**
     * Value of the IRT person latent trait score
     */
    protected double traitScore = Double.NaN;

    /**
     * Value of the IRT person latent trait score standard error
     */
    protected double traitScoreStdError = Double.NaN;

    /**
     * Number of items in the response vector
     */
    protected int nItems = 0;

    /**
     * A group ID for this response vector. Used for multigroup estimation.
     */
    private String groupID = "";

    /**
     * A unique identifier for one examinee. If using the object for multiple people, then make this the same as the groupID.
     */
    private String personID = "";

    private String responseString = "";

    /**
     * Stores the posterior probability of the response vector
     */
    private double posteriorProbability = 0;

    /**
     * A map that allows the response to be called by item name
     */
    private HashMap<VariableName, Integer> nameIndexMap = null;

    public ItemResponseVector(String personID, String groupID, byte[] response, VariableName[] itemName, double freq){
        this.personID = personID;
        this.groupID = groupID;
        this.response = response;
        this.freq = freq;
        this.nItems = response.length;
        this.nameIndexMap = new HashMap<VariableName, Integer>();
        for(int i=0;i<response.length;i++){
            nameIndexMap.put(itemName[i], Integer.valueOf(i));
            if(response[i]!=-1){
                sumScore += response[i];
                validResponses++;
            }
            responseString += response[i];
        }
    }

    public ItemResponseVector(String personID, String groupID, byte[] response, ArrayList<VariableName>  itemName, double freq){
        this.personID = personID;
        this.groupID = groupID;
        this.response = response;
        this.freq = freq;
        this.nItems = response.length;
        this.nameIndexMap = new HashMap<VariableName, Integer>();
        for(int i=0;i<response.length;i++){
            nameIndexMap.put(itemName.get(i), Integer.valueOf(i));
            if(response[i]!=-1){
                sumScore += response[i];
                validResponses++;
            }
            responseString += response[i];
        }
    }

    public ItemResponseVector(String personID, String groupID, ArrayList<Byte> response, ArrayList<VariableName> itemName, double freq){
        this.personID = personID;
        this.groupID = groupID;
        this.freq = freq;
        this.nItems = response.size();
        this.response = new byte[nItems];
        this.nameIndexMap = new HashMap<VariableName, Integer>();

        int index = 0;
        for(VariableName vName : itemName){
            this.response[index] = response.get(index);
            this.nameIndexMap.put(vName, Integer.valueOf(index));
            index++;
        }
    }

    /**
     * A constructor that is designed for storing all response vectors during MML estimation.
     *
     * @param groupID a group indicator code.
     * @param response a response vector.
     */
    public ItemResponseVector(String personID, String groupID, byte[] response, double freq){
        this.personID = personID;
        this.groupID = groupID;
        this.response = response;
        this.freq = freq;
        this.nItems = response.length;
        this.nameIndexMap = new HashMap<VariableName, Integer>();
        for(int i=0;i<response.length;i++){
            nameIndexMap.put(new VariableName("V"+(i+1)), Integer.valueOf(i));
            if(response[i]!=-1){
                sumScore += response[i];
                validResponses++;
            }
            responseString += response[i];
        }
    }

    /**
     * A constructor that is designed for storing all response vectors during MML estimation.
     *
     * @param response a response vector.
     */
    public ItemResponseVector(String personID, byte[] response, double freq){
        this(personID, "", response, freq);
    }

    /**
     * A constructor that is designed for storing all response vectors during MML estimation.
     *
     * @param response a response vector.
     */
    public ItemResponseVector(byte[] response, double freq){
        this("", "", response, freq);
    }

    /**
     * A constructor that takes an argument for the group ID and number of items.
     * @param groupID the group ID code.
     * @param nItems the number of items in the response vector.
     */
    public ItemResponseVector(String groupID, int nItems){
        this("", groupID, nItems);
    }

    /**
     * A constructor that takes an argument for the group ID and number of items.
     * @param personID a person id
     * @param groupID the group ID code.
     * @param nItems the number of items in the response vector.
     */
    public ItemResponseVector(String personID, String groupID, int nItems){
        this.personID = personID;
        this.groupID = groupID;
        this.nItems = nItems;
        response = new byte[nItems];
        this.nameIndexMap = new HashMap<VariableName, Integer>();
        for(int i=0;i<nItems;i++){
            nameIndexMap.put(new VariableName("V"+(i+1)), Integer.valueOf(i));
        }
    }

    /**
     * A constructor that only requires the number of items.
     * @param nItems the number of items in the response vector.
     */
    public ItemResponseVector(int nItems){
        this("", "", nItems);
    }

    /**
     * Resets the response vector and frequency counts to zero.
     */
    public void clearResponseVector(){
        response = new byte[nItems];
        sumScore = 0;
        validResponses = 0;
        responseString = "";
    }

    public void setTraitScore(double traitScore){
        this.traitScore = traitScore;
    }

    public void setTraitScoreStdError(double traitScoreStdError){
        this.traitScoreStdError = traitScoreStdError;
    }

    public void setResponseAt(int index, byte response){
        this.response[index] = response;
    }

    /**
     * Use this method to add an entire item response vector for a single examinee.
     * Also use it to change the response vector. This method is primarily designed
     * for use with IRT person scoring in {@link IrtExaminee}.
     *
     * @param response item response
     */
    public void setResponseVector(byte[] response){
        if(response.length!=nItems) return;//TODO should probably throw an exception
        sumScore = 0;
        validResponses = 0;
        for(int i=0;i<nItems;i++){
            this.response[i] = response[i];
            if(response[i]!=-1){
                sumScore += response[i];
                validResponses++;
            }
            responseString += response[i];
        }
    }

//    /**
//     * Checks whether byte array passed as the argument matches the byte array of the object.
//     * If it matches the count of examinees with this response vector is incremented. You
//     * should create a new IrtExaminee object when this method returns false. In that way,
//     * all unique response vectors are represented by an IrtExaminee object.
//     *
//     * @param response vector of item responses
//     * @return true if response vectors match. Returns false otherwise.
//     */
//    public boolean matchingResponseVector(byte[] response){
//        if(nItems!=response.length) return false;
//        for(int i=0;i<nItems;i++){
//            if(this.response[i]!=response[i]) return false;
//        }
//        freq++;
//        return true;
//    }

    /**
     * Group indicator for use with multigroup estimation.
     *
     * @return group indicator
     */
    public String getGroupID(){
        return groupID;
    }

    public String getPersonID(){
        return personID;
    }

    /**
     * Get item response at given item position.
     *
     * @param itemPosition position of item in array
     * @return item response
     */
    public byte getResponseAt(int itemPosition){
        return response[itemPosition];
    }

    /**
     * Get item response by the variable name
     *
     * @param variableName item for which the response is needed
     * @return
     */
    public byte getResponseAt(VariableName variableName){
        return response[nameIndexMap.get(variableName)];
    }

    public byte getResponseAt(String name){
        return response[nameIndexMap.get(new VariableName(name))];
    }

    /**
     * Gets the frequency count for this response vector.
     *
     * @return frequency count.
     */
    public double getFrequency(){
        return freq;
    }

    public void setFrequency(double freq){
        this.freq = freq;
    }

    public double getSumScore(){
        return sumScore;
    }

    public double getTraitScore(){
        return traitScore;
    }

    public double getTraitScoreStdError(){
        return traitScoreStdError;
    }

    public int getNumberOfItems(){
        return nItems;
    }

    public int getValidResponseCount(){
        return (int)validResponses;
    }

    public void setPosteriorProbability(double posteriorProbability){
        this.posteriorProbability = posteriorProbability;
    }

    public double getPosteriorProbability(){
        return posteriorProbability;
    }

    /**
     * A string representation of the response string.
     *
     * @return response string.
     */
    @Override
    public String toString(){
        String s = "";
        for(byte b : response){
            s += b;
        }
        return s;
    }

    public String printResponseVector(){
        String s = this.toString() + "  FREQ: " + getFrequency();
        return s;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof byte[]){
            byte[] thatResponse = (byte[])o;
            return Arrays.equals(this.response, thatResponse);
        }else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(this.response);
    }

    @Override
    public int compareTo(ItemResponseVector other){
        return responseString.compareTo(other.responseString);
    }


}

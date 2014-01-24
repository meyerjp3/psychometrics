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

import java.util.LinkedHashMap;

public class ItemResponseVector {

    protected byte[] response = null;

    protected double freq = 1;

    protected double validResponses = 0.0;

    protected double sumScore = 0.0;

    protected int nItems = 0;

    private int index = 0;

    private String groupID = "";

    public ItemResponseVector(String groupID, int nItems){
        this.groupID = groupID;
        this.nItems = nItems;
        response = new byte[nItems];
    }

    public ItemResponseVector(int nItems){
        this("", nItems);
    }

    /**
     * Use this method to incrementally add values to the response vector.
     * It is useful when reading data from a database.
     *
     * @param response item response
     */
    public void increment(byte response){
        this.response[index] = response;
        if(response!=-1){
            sumScore += response;
            validResponses++;
        }

        index++;
    }

    public void clearResponseVector(){
        response = new byte[nItems];
        index = 0;
        sumScore = 0;
        validResponses = 0;
    }

    /**
     * Use this method to add an entire item response vector for a single examinee.
     * Also use it to change the response vector.
     *
     * @param response item response
     */
    public void setResponseVector(byte[] response){
        index = 0;
        sumScore = 0;
        validResponses = 0;
        for(byte b : response){
            increment(b);
        }
    }

    /**
     * Checks whether byte array passed as the argument matches the byte array of the object.
     * If it matches the count of examinees with this response vector is incremented. You
     * should create a new IrtExaminee object when this method returns false. In that way,
     * all unique response vectors are represented by an IrtExaminee object.
     *
     * @param response vector of item responses
     * @return true if response vectors match. Returns false otherwise.
     */
    public boolean matchingResponseVector(byte[] response){
        if(nItems!=response.length) return false;
        for(int i=0;i<nItems;i++){
            if(this.response[i]!=response[i]) return false;
        }
        freq++;
        return true;
    }

    /**
     * Group indicator for use with multigroup estimation.
     *
     * @return group indicator
     */
    public String getGroupID(){
        return groupID;
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

    public double getFrequecy(){
        return freq;
    }

    @Override
    public String toString(){
        String s = "";
        for(byte b : response){
            s += b;
        }
        return s;
    }

}

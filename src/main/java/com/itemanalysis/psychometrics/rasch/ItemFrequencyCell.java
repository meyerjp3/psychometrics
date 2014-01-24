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

/**
 *
 * This class hols frequency of responses in category and in category and above. it
 * is the primary class used for summarizing data. An iem category frequency object
 * gets added to a RatingScaleItem.java and a RatingScaleStep.java object as the latter
 * two objects need thes frequencies for estimation.
 *
 * @author J. Patrick Meyer
 */
@Deprecated
public class ItemFrequencyCell {

    /**
     * Response score rho for this category as an Integer
     */
    private Byte categoryValue = null;

    /**
     * Identifier for the group to which this cell belongs
     */
    private String groupId = "";

    /**
     * Frequency of responses in category j of item i.
     */
    private double Tij = 0.0;

    /**
     * Frequency of responses in category j or above for item i.
     */
    private double Sij = 0.0;

    public ItemFrequencyCell(byte categoryValue, String groupId){
        this.categoryValue = categoryValue;
        this.groupId = groupId;
    }

    /**
     * Incrementally updates frequency counts.
     *
     * @param score
     */
    public void increment(byte score, String groupId){
        if(score>=categoryValue && this.groupId.equals(groupId)) Sij++;
        if(score==categoryValue && this.groupId.equals(groupId)) Tij++;
    }

    /**
     * The number of examinees scoring in this category
     *
     * @return
     */
    public double Tij(){
        return Tij;
    }

    public void setTij(double Tij, String groupId){
        if(this.groupId.equals(groupId)) this.Tij = Tij;
    }

    /**
     * The number of examinees scoring in this category or higher.
     *
     * @return
     */
    public double Sij(){
        return Sij;
    }

    public void setSij(double Sij){
        if(this.groupId.equals(groupId)) this.Sij = Sij;
    }

    /**
     * Resets frequency counts to zero.
     */
    public void resetFrequencies(){
        Tij = 0.0;
        Sij = 0.0;
    }



}

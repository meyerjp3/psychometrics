/**
 * Copyright 2016 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.irt.model;

import com.itemanalysis.psychometrics.data.VariableName;

import java.util.*;

/**
 * This class represents the item response models for an entire test or collection of item response models.
 * It provides features for iterating over the item response model objects.
 * 
 * Items may be organized into groups. ItemResponseModels are stored in an ArrayList. Group membership is
 * stored in a Map that gives the array index of each item in a group.
 * 
 * There is an iterator for looping over all items in the list. There is also an iterator for looping
 * over only items within a specific group.
 *
 */
public class IrmCollection implements Iterable<ItemResponseModel> {

    //An optional id for this collection.
    private String id = "";

    //Name of each item group
    private LinkedHashSet<String> groupNames = new LinkedHashSet<String>();

    //Number of categories in each group
    private HashMap<String, Integer> groupNCat = new HashMap<String, Integer>();

    //Names of variables in each group
    private HashMap<String, ArrayList<VariableName>> groupVariables = new HashMap<String, ArrayList<VariableName>>();

    //The array of all item response models.
    public ArrayList<ItemResponseModel> allIrm = new ArrayList<ItemResponseModel>();

    //An array of item response model array index values for each group of items.
    public LinkedHashMap<String, TreeSet<Integer>> groupIndex = new LinkedHashMap<String, TreeSet<Integer>>();

    //The minimum possible test score
    private int minPossibleTestScore = 0;

    //The maximum possible test score
    private int maxPossibleTestScore = 0;

    public IrmCollection(){

    }

    /**
     * Add item response models to this collection. It will also store information about the model
     * such as the groupID and number of categories. They are stored such that the number of categories
     * for a group is set according to the last item added to it.
     *
     * @param irm
     */
    public void addItemResponseModel(ItemResponseModel irm){
        //Add the item response model to the main array and increment the count
        allIrm.add(irm);

        minPossibleTestScore += irm.getMinScoreWeight();
        maxPossibleTestScore += irm.getMaxScoreWeight();

        String groupID = irm.getGroupId();
        groupNames.add(groupID);
        groupNCat.put(groupID, irm.getNcat());

        //Store array index of each item within a group
        TreeSet<Integer> grInd = groupIndex.get(groupID);
        if(null==grInd){
            grInd = new TreeSet<Integer>();
            groupIndex.put(groupID, grInd);
        }
        grInd.add(allIrm.size()-1);

        //Save name of items in each group
        ArrayList<VariableName> gName = groupVariables.get(groupID);
        if(null==gName){
            gName = new ArrayList<VariableName>();
            groupVariables.put(groupID, gName);
        }
        gName.add(irm.getName());
    }

    /**
     * Remove an item response model. It requires that everything be reset to keep accurate indices for each group.
     *
     * @param itemToRemove name of the item to be removed.
     */
    public void removeItemResponseModel(VariableName itemToRemove){
        for(ItemResponseModel item : allIrm){
            if(item.getName().toString().equals(itemToRemove.toString())){
                allIrm.remove(item);
                resetItemArrayIndex();
            }
        }
    }

    /**
     * Used after removing an item from the collection. It clears and repopulates some of the summary information.
     */
    private void resetItemArrayIndex(){
        //Clear all indicies
        for(String s : groupIndex.keySet()){
            groupIndex.get(s).clear();
        }

        //Class all collections. They will be repopulated next.
        groupNames.clear();
        groupNCat.clear();
        groupIndex.clear();
        groupVariables.clear();
        minPossibleTestScore = 0;
        maxPossibleTestScore = 0;

        int index = 0;
        for(ItemResponseModel model : allIrm){
            minPossibleTestScore += model.getMinScoreWeight();
            maxPossibleTestScore += model.getMaxScoreWeight();

            String groupID = model.getGroupId();
            groupNames.add(groupID);
            groupNCat.put(groupID, model.getNcat());

            TreeSet<Integer> arrayIndex = groupIndex.get(groupID);
            if(null==arrayIndex){
                arrayIndex = new TreeSet<Integer>();
                groupIndex.put(groupID, arrayIndex);
            }

            arrayIndex.add(new Integer(index));
            index++;
        }
    }

    /**
     * A test or collection ID
     *
     * @return the ID for this test or collection of items
     */
    public String getID(){
        return id;
    }

    /**
     * Number of items in the collection (i.e. number of items on the test).
     * @return
     */
    public int getNumberIfItems(){
        return allIrm.size();
    }

    public int getMinimumPossibleTestScore(){
        return minPossibleTestScore;
    }

    public int getMaximumPossibleTestScore(){
        return maxPossibleTestScore;
    }

    /**
     * An iterator for all item response models in this collection (i.e. the test).
     *
     * @return an iterator
     */
    public Iterator<ItemResponseModel> iterator(){
        return allIrm.iterator();
    }

    /**
     * An iterator for the item response models for a specific group of items.
     *
     * @param groupID id for the group of items
     * @return an iterator
     */
    public Iterator<ItemResponseModel> groupModelIterator(String groupID){
        return new GroupModelIterator(groupID);
    }

    /**
     * An iterator of the group names.
     *
     * @return an iterator
     */
    public Iterator<String> getGroupNameIterator(){
        return groupNames.iterator();
    }


    /**
     * A class that iterates over the item response model objects for a specific group of items.
     *
     */
    public class GroupModelIterator implements Iterator<ItemResponseModel>{

        private TreeSet<Integer> listIndex = null;
        private Iterator<Integer> iterator = null;

        public GroupModelIterator(String groupID){
            listIndex = groupIndex.get(groupID);
            iterator = listIndex.iterator();
        }

        public boolean hasNext(){
            return iterator.hasNext();
        }

        public ItemResponseModel next(){
            return allIrm.get(iterator.next());
        }

        public void remove(){
            //empty method
        }

    }




}

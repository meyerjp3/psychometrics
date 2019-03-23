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

import com.itemanalysis.psychometrics.exceptions.ItemScoringException;

import java.util.Iterator;

/**
 *
 * @author J. Patrick Meyer
 */
public interface ItemScoring {

    void addCategory(Object categoryID, double categoryScore);

    int numberOfCategories();

    int numberOfScoreLevels();

    double computeItemScore(Object response) throws ItemScoringException;

    void isContinuous(boolean isContinuous);

    double computeCategoryScore(Object categoryId, Object response);

    double maximumPossibleScore();

    double minimumPossibleScore();

    String getAnswerKey();

    ItemType getItemType();

    ItemType addAllCategories(String optionScoreKey, DataType type);

    void clearCategory();

    double[] scoreArray();

    String printOptionScoreKey();

    Iterator<Object> categoryIterator();

    String getCategoryScoreString(Object response);

    VariableName getName();

}

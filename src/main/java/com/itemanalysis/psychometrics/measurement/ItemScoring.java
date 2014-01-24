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

/**
 *
 * @author J. Patrick Meyer
 */
public interface ItemScoring {

    public void addCategory(Category cat);

    public void removeCategory(Category cat);

    public int numberOfCategories();

    public double computeItemScore(Object response);

    public double computeItemScore(Object response, boolean scoreAsZero);

    public double computeItemScore(Object response, boolean missingZero, boolean omitZero, boolean notreachedZero);

    public double computeItemScore(Object response, VariableType type);

    public double computeItemScore(Object response, VariableType type, boolean scoreAsZero);

    public double computeItemScore(Object response, VariableType type, boolean missingZero, boolean omitZero, boolean notreachedZero);

    public double computeCategoryScore(Object categoryId, Object response);

    public double maximumPossibleScore();

    public double minimumPossibleScore();

    public String getAnswerKey();

    public void setOmitCode(Object omitCode, VariableType type);

    public void setNotReachedCode(Object omitCode, VariableType type);

    public Object getOmitCode();

    public Object getNotReachedCode();

}

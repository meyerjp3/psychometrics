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
package com.itemanalysis.psychometrics.irt.estimation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class MultivariateDiscreteDistribution {

    @Deprecated
    public MultivariateDiscreteDistribution(){

    }

    @Deprecated
    public static Set<Set<Integer>> getCombinationsFor(List<Integer> group, int subsetSize) {
        Set<Set<Integer>> resultingCombinations = new HashSet<Set<Integer>> ();
        int totalSize=group.size();
        if (subsetSize == 0) {
            emptySet(resultingCombinations);
        } else if (subsetSize <= totalSize) {
            List<Integer> remainingElements = new ArrayList<Integer> (group);
            Integer X = popLast(remainingElements);

            Set<Set<Integer>> combinationsExclusiveX = getCombinationsFor(remainingElements, subsetSize);
            Set<Set<Integer>> combinationsInclusiveX = getCombinationsFor(remainingElements, subsetSize-1);
            for (Set<Integer> combination : combinationsInclusiveX) {
                combination.add(X);
            }
            resultingCombinations.addAll(combinationsExclusiveX);
            resultingCombinations.addAll(combinationsInclusiveX);
        }
        return resultingCombinations;
    }

    @Deprecated
    private static void emptySet(Set<Set<Integer>> resultingCombinations) {
        resultingCombinations.add(new HashSet<Integer>());
    }

    @Deprecated
    private static Integer popLast(List<Integer> elementsExclusiveX) {
        return elementsExclusiveX.remove(elementsExclusiveX.size()-1);
    }

}

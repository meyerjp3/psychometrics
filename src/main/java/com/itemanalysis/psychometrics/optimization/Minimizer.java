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
package com.itemanalysis.psychometrics.optimization;

/**
 * The interface for unconstrained function minimizers.
 * <p/>
 * Implementations may also vary in their requirements for the
 * arguments.  For example, implementations may or may not care if the
 * <code>initial</code> feasible vector turns out to be non-feasible
 * (or null!).  Similarly, some methods may insist that objectives
 * and/or constraint <code>Function</code> objects actually be
 * <code>DiffFunction</code> objects.
 *
 * @author <a href="mailto:klein@cs.stanford.edu">Dan Klein</a>
 * @version 1.0
 * @since 1.0
 */
public interface Minimizer<T extends Function> {

    /**
     * Attempts to find an unconstrained minimum of the objective
     * <code>function</code> starting at <code>initial</code>, within
     * <code>functionTolerance</code>.
     *
     * @param function          the objective function
     * @param functionTolerance a <code>double</code> value
     * @param initial           a initial feasible point
     * @return Unconstrained minimum of function
     */
    double[] minimize(T function, double functionTolerance, double[] initial);
    double[] minimize(T function, double functionTolerance, double[] initial, int maxIterations);

}


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
 * An interface for double-valued functions over double arrays.
 *
 * @author <a href="mailto:klein@cs.stanford.edu">Dan Klein</a>
 * @version 1.0
 * @since 1.0
 */
public interface Function {
    /**
     * Returns the value of the function at a single point.
     *
     * @param x a <code>double[]</code> input
     * @return the function value at the input
     */
    double valueAt(double[] x);

    /**
     * Returns the number of dimensions in the function's domain
     *
     * @return the number of domain dimensions
     */
    int domainDimension();
}

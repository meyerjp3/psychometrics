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
package com.itemanalysis.psychometrics.irt.model;

public interface ItemResponseModelWithGradient extends ItemResponseModel {

    /**
     * First derivative with respect to person ability.
     *
     * @param theta person ability value.
     * @return first derivative wrt theta.
     */
    @Deprecated
    public double[] firstDerivative(double theta);

    /**
     * Hessian or matrix of second derivatives.
     *
     * @param theta person ability value.
     * @return a two-way array containing teh Hessian matrix values.
     */
    @Deprecated
    public double[][] hessian(double theta);

}

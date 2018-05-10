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

import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.optimization.DiffFunction;
import com.itemanalysis.psychometrics.uncmin.Uncmin_methods;

public interface ItemLogLikelihoodFunction extends DiffFunction, Uncmin_methods {


//    public void setModel(ItemResponseModel model, QuadratureRule latentDistribution, double[] rjk, double[] nk);

    public void setModel(ItemResponseModel model, QuadratureRule latentDistribution, EstepItemEstimates r, double[] nk);

    /**
     * Item loglikelihood function.
     *
     * @return value of the item loglikelihood function.
     */
    public double logLikelihood();

    /**
     * For DiffFunction interface.
     *
     * @return number of parameters
     */
    public int domainDimension();

    /**
     * Computes the loglikelihood value for an item. For DiffFunction interface that is used by
     * QNMinimizer in the optimization package.
     *
     * @param point item parameter values
     * @return loglikelihood value at item parameter values
     */
    public double valueAt(double[] point);

    /**
     * Computes gradientAt of item likelihood function at item parameter values. For DiffFunction interface that
     * is used by QNMinimizer in the optimization package.
     *
     * @param point item parameter values.
     * @return Gradient of item likelihood function at item parameter values
     */
    public double[] derivativeAt(double[] point);

    //

    /**
     * Computes the loglikelihood value for an item. For Uncmin_methods interface in the uncmin package.
     * Note that the array indexing begins at 1. For example, a problem with two paramters will require
     * an input array x[] of length = 3. The first index 0 is not used. This indexing is due to the
     * translation of FORTRAN code in the Uncmin optimizer.
     *
     * @param x input array of item parameters that begins at 1. You must create a new array that begins at 0
     *          and then call valueAt(double[] point) to get the value.
     * @return Loglikelihood value for an item
     */
    public double f_to_minimize(double x[]);

    /**
     * Computes the gradientAt of the loglikelihood function for an item. For Uncmin_methods interface
     * in the uncmin package. Note that the array indexing begins at 1. For example, a problem with two
     * paramters will require an input array x[] of length = 3. The first index 0 is not used. This
     * indexing is due to the translation of FORTRAN code in the Uncmin optimizer. If this method is
     * empty, the gradientAt will be computed numerically by the Uncmin optimizer.
     *
     * @param x input array of item parameters that begins at 1. You must create a new array that begins
     *          at 0 and then call derivativeAt(double[] point) to get the gradientAt.
     * @param g gradientAt at x.
     */
    public void gradient(double[] x, double[] g);

    /**
     * Computes the Hessian of the loglikelihood function for an item. For Uncmin_methods interface in
     * the uncmin package. Note that the array indexing begins at 1. For example, a problem with two
     * paramters will require an input array x[] of length = 3. The first index 0 is not used. This
     * indexing is due to the translation of FORTRAN code in the Uncmin optimizer. If this method is
     * empty, the Hessian will be computed numerically by the Uncmin optimizer.
     *
     * @param x input array of item parameters that begins at 1. You must create a new array that begins
     *          at 0 and then call derivativeAt(double[] point) to get the gradientAt.
     * @param a Hessian at x.
     */
    public void hessian(double[] x, double[][] a);


}

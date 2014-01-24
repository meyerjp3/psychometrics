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
 * This enumeratin was created to organize the selection of different methods for stochastic
 * calculations.  It was also created for use with Stochastic Meta Descent (SMDMinimizer) due
 * to the need for Hessian Vector Products, and the inefficiency of continuing to calculate these
 * vector products in other minimization methods like Stochastic Gradient Descent (SGDMinimizer)
 *
 * @author Alex Kleeman (akleeman@stanford.edu)
 */
public enum StochasticCalculateMethods {

    NoneSpecified(false),
    /*  Used for procedures like Stochastic Gradient Descent */
    GradientOnly (false),
    /*  This is used with the Objective Function can handle calculations using Algorithmic Differentiation*/
    AlgorithmicDifferentiation (true),
    /*  It is often more efficient to calculate the Finite difference within one single for loop,
        if the objective function can handle this, this method should be used instead of
         ExternalFiniteDifference
     */
    IncorporatedFiniteDifference (true),
    /*  ExternalFiniteDifference uses two calls to the objective function to come up with an approximation of
        the H.v
     */
    ExternalFiniteDifference (false);


    /*
    *This boolean is true if the Objective Function is required to calculate the hessian vector product
    *   In the case of ExternalFiniteDifference this is false since two calls are made to the objective
    *   function.
    */
    private boolean objFuncCalculatesHdotV;

    StochasticCalculateMethods(boolean ObjectiveFunctionCalculatesHdotV){
        this.objFuncCalculatesHdotV = ObjectiveFunctionCalculatesHdotV;
    }

    public boolean calculatesHessianVectorProduct(){
        return objFuncCalculatesHdotV;
    }

    public static StochasticCalculateMethods parseMethod(String method) {
        if (method.equalsIgnoreCase("AlgorithmicDifferentiation")){
            return StochasticCalculateMethods.AlgorithmicDifferentiation;
        } else if(method.equalsIgnoreCase("IncorporatedFiniteDifference")){
            return StochasticCalculateMethods.IncorporatedFiniteDifference ;
        } else if(method.equalsIgnoreCase("ExternalFinitedifference")){
            return StochasticCalculateMethods.ExternalFiniteDifference ;
        } else {
            return null;
        }
    }

}


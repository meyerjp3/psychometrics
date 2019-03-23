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
package com.itemanalysis.psychometrics.uncmin;

import org.apache.commons.math3.util.Precision;

/**
 * Provides a more user friendly interface to Uncmin_f77. Many of the flags and tolerances have been set
 * with IRT estimation in mind. These values may not be suitable for other optimization problems.
 */
public class DefaultUncminOptimizer {

    private int[] itrmcd = null;//termination message
    private double[] xpls = null;//final parameter values
    private double[] fpls = null;//value of objective function
    private double[] gpls = null;//gradientAt
    private double[][] a = null;//hessian
    double[] udiag = null;//hessian diagonal
    private Uncmin_f77 optimizer = null;//function to be minimized
    private int n = 0;

    /*
     * See information below for details about this argument. This argument
     * is for the MSG flag in Uncmin_f77. The default is no message.
     */
    private int messageLevel = 0;

    public DefaultUncminOptimizer(){
        this(8);
    }

    public DefaultUncminOptimizer(int messageLevel){
        this.messageLevel = messageLevel;
        optimizer = new Uncmin_f77();
//        optimizer.showDetails(true);//for debugging
    }

    public DefaultUncminOptimizer(boolean verbose){
        if(verbose){
            this.messageLevel = 16;
        }else{
            this.messageLevel = 0;
        }
        optimizer = new Uncmin_f77();
    }

    public void addUncminStatusListener(UncminStatusListener listener){
        optimizer.addUncminStatusListener(listener);
    }

    /**
     * This wrapper will use the same configuration as Uncmin_f77.optif0_f77.
     *
     * @param minclass a class that implements Uncmin_methods to provide the value of the objective funciton, gradientAt, and hessian.
     * @param initialValue initial value for the prameters. It should be an array of teh same size as the number of parameters.
     */
    public void minimize(Uncmin_methods minclass, double[] initialValue)throws UncminException{
        this.minimize(minclass, initialValue, false, false, 150, 2);
    }

    /**
     * Wrapper for calling in Uncmin_f77.optif9_f77. It includes many of the default values from
     * Uncmin_f77.optif0_f77 but allows access to some additional options.
     *
     *
     * @param minclass a class that implements Uncmin_methods to provide the value of the objective funciton, gradientAt, and hessian.
     * @param initialValue initial value for the prameters. It should be an array of teh same size as the number of parameters.
     * @param analyticGradient true if analytic gradientAt is provided in minClass. False otherwise and will compute gradientAt numerically.
     * @param analyticHessian true if analytic Hessian is provided in minClass. False otherwise and will compute Hessian numerically.
     * @param maxIter maximum number of iterations.
     */
    public void minimize(Uncmin_methods minclass, double[] initialValue, boolean analyticGradient, boolean analyticHessian, int maxIter, double maxStep)throws UncminException{
        n = initialValue.length;//should be the actual size of the array, not arrySize+1. It will be resized later
        int np1 = n+1;

        double dlt[] = new double[2];
        double fscale[] = new double[2];
        double stepmx[] = new double[2];
        double gradtl[] = new double[2];
        double steptl[] = new double[2];
        int ndigit[] = new int[2];
        int method[] = new int[2];
        int iexp[] = new int[2];
        int itnlim[] = new int[2];
        int iagflg[] = new int[2];
        int iahflg[] = new int[2];

        int msg[] = new int[2];
        itrmcd = new int[2];//termination code
        fpls = new double[2];//value of objective function

        //initial value array resized for arrays with a base index of 1.
        double[] x = new double[np1];
        xpls = new double[np1];
        for(int i=0;i<n;i++){
            x[i+1] = initialValue[i];
            xpls[i+1] = initialValue[i];
        }

        gpls = new double[np1];
        a = new double[np1][np1];
        udiag = new double[np1];


        //SET TYPICAL SIZE OF X AND MINIMIZATION FUNCTION
        double[] typsiz = new double[np1];
        for (int i = 1; i <= n; i++) {
            typsiz[i] = 1.0;
        }
        fscale[1] = 1.0;

        //SET FLAGS
        method[1] = 1;
        iexp[1] = 1;
        msg[1] = messageLevel;//See information below for details about this argument.
        ndigit[1] = -1;
        itnlim[1] = maxIter;//maximum number of iterations
        iagflg[1] = 0;//0 if numerical gradientAt, 1 if analytic gradientAt provided
        iahflg[1] = 0;//0 if use numerical Hessian, 1 if analytic Hessian provided

        if(analyticGradient) iagflg[1] = 1;
        if(analyticHessian) iahflg[1] = 1;

        // SET TOLERANCES
        double epsm = Precision.EPSILON;

        dlt[1] = -1.0;
        gradtl[1] = Math.pow(epsm,1.0/3.0);
        steptl[1] = Math.sqrt(epsm);
        stepmx[1] = maxStep;//should be greater than 0. Values between 0.1 and 3 work well for IRT estimation (using 2).

        //For debugging
//        optimizer.showDetails(true);

        optimizer.optif9_f77(n, x, minclass, typsiz, fscale, method, iexp, msg, ndigit, itnlim,
                iagflg, iahflg, dlt, gradtl, stepmx, steptl, xpls, fpls, gpls, itrmcd, a, udiag);

    }

//    public void minimize(Uncmin_methods minclass, double[] initialValue, int maxIter)throws UncminException{
//        n = initialValue.length;//should be the actual size of the array, not arrySize+1. It will be resized later
//        int np1 = n+1;
//
//        itrmcd = new int[2];//termination code
//        fpls = new double[2];//value of objective function
//
//        //initial value array resized for arrays with a base index of 1.
//        double[] x = new double[np1];
//        xpls = new double[np1];
//        for(int i=0;i<n;i++){
//            x[i+1] = initialValue[i];
//            xpls[i+1] = initialValue[i];
//        }
//
//        gpls = new double[np1];
//        a = new double[np1][np1];
//        double[] udiag = new double[np1];
//
//        optimizer.showDetails(true);
//
//        optimizer.optif0_f77(n, x, minclass, xpls, fpls, gpls, itrmcd, a, udiag);
//
//    }

    public double getFunctionValue(){
        return fpls[1];
    }

    public double[] getParameters(){
        double[] param = new double[n];
        for(int i=0;i<n;i++){
            param[i] = xpls[i+1];
        }
        return param;
    }

    public double[] getGradient(){
        double[] grad = new double[gpls.length-1];
        for(int i=0;i<grad.length;i++){
            grad[i] = gpls[i+1];
        }
        return grad;
    }

    public double[][] getHessian(){
        int n = a.length-1;
        double[][] H = new double[n][n];
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                H[i][j] = a[i+1][j+1];
            }
        }
        return H;
    }

//    public double[] getHessianDiagonal(){
//        return udiag;
//    }
//
//    public double[][] getHessian(){
//        return a;
//    }

    /**
     * ITRMCD =  0:  Optimal solution found
     * ITRMCD =  1:  Terminated with gradientAt small,
     *               xpls is probably optimal
     * ITRMCD =  2:  Terminated with stepsize small,
     *               xpls is probably optimal
     * ITRMCD =  3:  Lower point cannot be found,
     *               xpls is probably optimal
     * ITRMCD =  4:  Iteration limit (150) exceeded
     * ITRMCD =  5:  Too many large steps,
     *               function may be unbounded
     *
     *
     * @return termination code
     */
    public int getTerminationCode(){
        return itrmcd[1];
    }

    public String getTerminationMessage(){
        switch(itrmcd[1]){
            case 0: return "Optimal solution found";
            case 1: return "Terminated with gradientAt small, X is probably optimal";
            case 2: return "Terminated with stepsize small, X is probably optimal";
            case 3: return "Lower point cannot be found, X is probably optimal";
            case 4: return "Iteration limit exceeded";
            case 5: return "Too many large steps, function may be unbounded";
        }
        return "Optimal solution found";
    }

/*=============================================================================================*/
/* DETAILS ABOUT THE MSG FLAG

    An integer variable which the user may set on input to inhibit certain
    automatic checks or override certain default characteristics of the package.
    There are currently five "message" features which can be used individually
    or in combination.

    = 0 No message.

    = 1 Do not abort package for N=1.

    = 2 Do not check user analytic gradientAt routine D1FN against
        its finite difference estimate. This may be necessary if the
        user knows his gradientAt function is properly coded, but the
        program aborts because the comparative tolerance is too
        tight. It is also efficient if the gradientAt has previously been
        checked. Do not use MSG=2 if the analytic gradientAt is not
        supplied.

    = 4 Do not check user analytic Hessian routine D2FN
        against its finite difference estimate. This may be necessary
        if the user knows his Hessian function is properly coded, but the
        program aborts because the comparative tolerance is too
        tight. It is also efficient if the Hessian has previously been
        checked. Do not use MSG=4 if the analytic Hessian is not
        supplied.

    = 8 Suppress printing of the input state, the final results,
        and the stopping condition.

    = 16 Print intermediate results.

    The user may specify a combination of features by setting MSG to the sum
    of the individual components. As an example, suppose the user wishes to
    override automatic comparison of his analytic Hessian routine and to
    suppress the automatic outputting of all results. The user would set
    MSG=4+8=12.

    The module DFAULT returns a value of 0. If the user specifies an illegal
    value, its value MOD 32 will be used.

    On output, if the program has terminated because of erroneous input
            (ITRMCD=0), MSG contains an error code indicating the reason.

    = 0 No error. (See ITRMCD for termination code)
    = -1 Illegal dimension, N≤0.
    = -2 Attempt to run program for N=1. (See N, MSG above)
    = -3 Illegal tolerance on gradientAt, GRADTL<0.
    = -4 Iteration limit ITNLIM≤0.
    = -5 No good digitsin optimization function, NDIGIT=0.-9-
    = -6 Program asked to override check of analytic gradientAt
         against finite difference estimate, but routine D1FN not
         supplied. (Incompatible input: MSG=0 mod 2 and IAGFLG=0)
    = -7 Program asked to override check of analytic Hessian
         against finite difference estimate, but routine D2FN not
         supplied. (Incompatible input: MSG=0 mod 4 and IAGFLG=0)
    = -21 Probable coding error in the user’s analytic gradientAt
         routine D1FN. Analytic and finite difference gradients do
         not agree within the assigned tolerance. (See computation
         of tolerance under D1FN)
    = -22 Probable coding error in the user’s analytic Hessian
         routine D2FN. Analytic and finite difference Hessians do
         not agree within the assigned tolerance. (See computation
         of tolerance under D2FN).
*/
/*=============================================================================================*/

}

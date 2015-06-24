package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.factoranalysis.*;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by jpm4qs on 6/24/2015.
 */
public class RaschStandardizedResiduals {

    private int nItems = 0;
    private ItemResponseModel[] irm = null;
    private double[][] standardizedResiduals = null;
//    private FactorMethod factorMethod = null;

    public RaschStandardizedResiduals(int nPeople, int nItems){
        this.nItems = nItems;
        this.standardizedResiduals = new double[nPeople][nItems];
    }

    /**
     * Incrementally compute standardized residuals. Missing data or dropped items should be checked before
     * calling this method. Only call this method for nonmissing data and items that have not been dropped.
     *
     * @param i index of current person
     * @param j index of current item
     * @param irm an item response model
     * @param theta a person ability value
     * @param response an item response
     */
    public void increment(int i, int j, ItemResponseModel irm, double theta, byte response){
        double expectedvalue = irm.expectedValue(theta);
        double stdDeviation = Math.sqrt(irm.itemInformationAt(theta));
        double residual = ((double)response) - expectedvalue;
        standardizedResiduals[i][j] = residual/stdDeviation;
    }

//    /**
//     * Conducts a principal components analysis of the standardized residual matrix. A String is returned
//     * to display the results. Factor loadings and other parameters can be obtained directly using
//     * the FactorMethod object with a call to getFactorMethod() after calling this method.
//     *
//     *
//     *
//     * @return string representation of the results.
//     */
//    public String principalComponentsAnalysis(){
//
//        CovarianceMatrix covarianceMatrix = new CovarianceMatrix(nItems);
//        for(int i=0;i<standardizedResiduals.length;i++){
//            for(int j=0;j<standardizedResiduals[0].length;j++){
//                for(int k=j;k<standardizedResiduals[0].length;k++){
//                    covarianceMatrix.increment(j, k, standardizedResiduals[i][j], standardizedResiduals[i][k]);
//                }
//            }
//        }
//
//        RealMatrix R = new Array2DRowRealMatrix(covarianceMatrix.correlation(true));
//        ExploratoryFactorAnalysis fa = new ExploratoryFactorAnalysis(R, nItems/2);
//        fa.estimateParameters(com.itemanalysis.psychometrics.factoranalysis.EstimationMethod.PRINCOMP);
//
//        //Create the FactorMethod object
//        factorMethod = fa.getFactorMethod();
//
//        return fa.printOutput();
//    }

    public double[] eigenValues(){

        CovarianceMatrix covarianceMatrix = new CovarianceMatrix(nItems);
        for(int i=0;i<standardizedResiduals.length;i++){
            for(int j=0;j<standardizedResiduals[0].length;j++){
                for(int k=j;k<standardizedResiduals[0].length;k++){
                    covarianceMatrix.increment(j, k, standardizedResiduals[i][j], standardizedResiduals[i][k]);
                }
            }
        }

        RealMatrix R = new Array2DRowRealMatrix(covarianceMatrix.correlation(true));
        EigenDecomposition eigen = new EigenDecomposition(R);
        return eigen.getRealEigenvalues();
    }

//    public FactorMethod getFactorMethod(){
//        return factorMethod;
//    }


}

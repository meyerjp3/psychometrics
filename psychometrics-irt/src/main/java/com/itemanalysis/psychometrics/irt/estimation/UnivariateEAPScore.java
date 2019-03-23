package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;

import java.util.LinkedHashMap;

public class UnivariateEAPScore extends AbstractUnivariatePersonScoring {

    private QuadratureRule dist = null;

    private ItemResponseModel[] irm = null;

    private int numPoints = 0;

    public UnivariateEAPScore(QuadratureRule dist, ItemResponseModel[] irm){
        this.dist = dist;
        this.irm = irm;
        this.numPoints = dist.getNumberOfPoints();
    }

    public UnivariateEAPScore(QuadratureRule dist, LinkedHashMap<VariableName, ItemResponseModel> irm){
        this.dist = dist;
        this.numPoints = dist.getNumberOfPoints();
        this.irm = new ItemResponseModel[irm.size()];

        int index=0;
        for(VariableName v : irm.keySet()){
            this.irm[index] = irm.get(v);
            index++;
        }
    }

    /**
     * EAP estimate using a quadrature provided by the user such as quadrature points
     * and weights from item calibration.
     *
     * @return
     */
    public double estimate(byte[] responseVector){
        double point = 0.0;
        double w = 0.0;
        double numer = 0.0;
        double denom = 0.0;

        for(int i=0;i<numPoints;i++){
            point = dist.getPointAt(i);
            w = Math.exp(logLikelihood(responseVector, irm, point))* dist.getDensityAt(i);
            numer += point*w;
            denom += w;
        }

        return numer/denom;
    }

    /**
     * Computes standard error for EAP method.
     *
     * @param theta person ability value
     * @return standard error
     */
    public double standardErrorAt(byte[] responseVector, double theta){
        double point = 0.0;
        double w = 0.0;
        double numer = 0.0;
        double denom = 0.0;
        double dif = 0.0;
        double var = 0.0;

        for(int i=0;i<numPoints;i++){
            point = dist.getPointAt(i);
            w = Math.exp(logLikelihood(responseVector, irm, point))*dist.getDensityAt(i);
            dif = point-theta;
            numer += dif*dif*w;
            denom += w;
        }
        var = numer/denom;
        return Math.sqrt(var);
    }

}

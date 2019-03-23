package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;

public abstract class AbstractUnivariatePersonScoring implements UnivariatePersonScoring {


    /**
     * computes the loglikelihood of a responseVector vector at a given value of theta.
     *
     * @param theta examinee ability
     * @return
     */
    public double logLikelihood(byte[] responseVector, ItemResponseModel[] irm, double theta){
        double ll = 0.0;
        double prob = 0.0;
        byte resp = 0;
        VariableName varName = null;
        for(int i=0;i< responseVector.length;i++){
            resp = responseVector[i];
            if(resp!=-1){
                prob = irm[i].probability(theta, resp);
                prob = Math.min(Math.max(0.00001, prob), 0.99999);
                ll += Math.log(prob);
            }
        }

        return ll;
    }

    /**
     * First derivative of loglikelihood with respect to theta.
     *
     * @param theta examinee ability
     * @return first derivative
     */
    public double derivLogLikelihood(ItemResponseModel[] irm, double theta){
        double deriv = 0.0;

        for(ItemResponseModel i : irm){
            deriv += i.derivTheta(theta);
        }
        return deriv;

    }

    public double testInformationAt(ItemResponseModel[] irm, double theta){
        double info = 0.0;
        for(ItemResponseModel i : irm){
            info += i.itemInformationAt(theta);
        }

        return info;
    }

}

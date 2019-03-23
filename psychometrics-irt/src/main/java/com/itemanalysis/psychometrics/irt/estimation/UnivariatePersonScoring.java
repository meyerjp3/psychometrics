package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.math3.distribution.RealDistribution;

public interface UnivariatePersonScoring {

    public double logLikelihood(byte[] responseVector, ItemResponseModel[] irm, double theta);

    public double derivLogLikelihood(ItemResponseModel[] irm, double theta);

    public double estimate(byte[] responseVector);

    public double testInformationAt(ItemResponseModel[] irm, double theta);

    public double standardErrorAt(byte[] responseVector, double theta);

}

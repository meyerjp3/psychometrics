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
package com.itemanalysis.psychometrics.mixture;

import java.util.Formatter;

public class InformationFitCriteria {

    private MixtureModel mixModel = null;

    private double sampleSize = 0.0;

    private double freeParameters = 0.0;

    private double logLikelihood = 0.0;

    public enum FitCriterion{
        AIC, BIC, CAIC, SABIC, SACAIC, ENTROPY, ICLBIC;
    }

    public InformationFitCriteria(MixtureModel mixModel){
        this.mixModel = mixModel;
        sampleSize = (double)mixModel.sampleSize();
        freeParameters = (double)mixModel.freeParameters();
        logLikelihood = mixModel.loglikelihood();
    }

    public double getFitStat(String fitStat){
        if(fitStat.trim().equals("aic")) return aic();
        if(fitStat.trim().equals("bic")) return aic();
        if(fitStat.trim().equals("caic")) return aic();
        if(fitStat.trim().equals("sabic")) return aic();
        if(fitStat.trim().equals("sacaic")) return aic();
        if(fitStat.trim().equals("entropy")) return aic();
        return iclbic();
    }

    public double aic(){
        return -2.0*logLikelihood + 2.0*freeParameters;
    }

    public double bic(){
        return -2.0*logLikelihood + freeParameters*Math.log(sampleSize);
    }

    public double caic(){
        return -2.0*logLikelihood + freeParameters*(Math.log(sampleSize)+1.0);
    }

    public double sabic(){
        return -2.0*logLikelihood + freeParameters*Math.log((sampleSize+2.0)/24.0);
    }

    public double sacaic(){
        return -2.0*logLikelihood + freeParameters*(Math.log((sampleSize+2.0)/24.0)+1.0);
    }

    public double entropy(){
        int groups = mixModel.numberOfGroups();
        double pp = 0.0;
        double sum = 0.0;
        for(int g=0;g<groups;g++){
            for(int i=0;i<sampleSize;i++){
                pp = mixModel.posteriorProbability(g, i);
                if(pp!=0.0) sum += pp*Math.log(pp);
            }
        }
        return -sum;
    }

    public double iclbic(){
        return -2.0*logLikelihood + freeParameters*Math.log(sampleSize) + 2.0*entropy();
    }

    public String printFitStatistics(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%10s", "LogLike = "); f.format("%-12.4f", logLikelihood);f.format("%n");
        f.format("%10s", "AIC = "); f.format("%-12.4f", aic());f.format("%n");
        f.format("%10s", "BIC = "); f.format("%-12.4f", bic());f.format("%n");
        f.format("%10s", "CAIC = "); f.format("%-12.4f", caic());f.format("%n");
        f.format("%10s", "SABIC = "); f.format("%-12.4f", sabic());f.format("%n");
        f.format("%10s", "SACAIC = "); f.format("%-12.4f", sacaic());f.format("%n");
        f.format("%10s", "Entropy = "); f.format("%-12.4f", entropy());f.format("%n");
        f.format("%10s", "ICLBIC = "); f.format("%-12.4f", iclbic());f.format("%n");
        return f.toString();
    }

    public String printDelimitedFitStatistics(){
        String s = mixModel.numberOfGroups() + "," + logLikelihood + "," + aic() + "," + bic() + "," + caic() + "," + sabic() + "," + sacaic() + "," + entropy() + "," + iclbic();
        return s;
    }

}

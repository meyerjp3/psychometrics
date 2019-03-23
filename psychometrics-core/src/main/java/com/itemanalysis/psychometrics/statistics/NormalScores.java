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
package com.itemanalysis.psychometrics.statistics;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 *
 * @author J. Patrick Meyer
 */
public class NormalScores {

    public enum ScoreType{
        BLOM, TUKEY, VANDERWAERDEN;
    }
    
    NormalDistribution norm = null;

    public NormalScores(){
        norm = new NormalDistribution();
    }

    public double blom(double rank, double n){
        double p = (rank-3.0/8.0)/(n+1.0/4.0);
        return norm.inverseCumulativeProbability(p);
    }

    public double tukey(double rank, double n){
        double p = (rank-1.0/3.0)/(n+1.0/3.0);
        return norm.inverseCumulativeProbability(p);
    }

    public double vanderWaerden(double rank, double n){
        double p = rank/(n+1.0);
        return norm.inverseCumulativeProbability(p);
    }

    public double getNormalScore(double rank, double n, ScoreType normalScoreType){
        switch(normalScoreType){
            case BLOM: return blom(rank, n);
            case TUKEY: return tukey(rank, n);
        }
        return vanderWaerden(rank, n);
    }

}

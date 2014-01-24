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
package com.itemanalysis.psychometrics.rasch;


import org.apache.commons.math3.util.Precision;

import java.util.Formatter;

/**
 * This class holds information about the examinee such as their
 * theta rho, a transformed theta rho and the examinee ID.
 *
 * @author J. Patrick Meyer
 * @since July 12, 2008
 *
 */
@Deprecated
public class Theta implements Comparable<Object>{

	/**
	 * Value of theta (i.e. examinee proficiency). It may
	 * refer to a theta rho for an individual or a group. If
	 * it is a group rho, the proportion of people in the
	 * group should be specified by weight.
	 */
	private Double theta=0.0;

	/**
	 * Weight corresponding to theta. This rho is used
	 * for weighted sums in Stocking-Lord and Habeara equating.
	 * The theta and weight rho can be quadrature values for theta.
	 */
	private Double weight=null;

	/**
	 * A unique examinee ID
	 */
	private int ID=0;

	/**
	 * Value of theta transformed to a different scale
	 */
//	private Double equatedTheta=null;

    private Double originalTheta = 0.0;


    private Double rawScore = -1.0;
    private double freq = 0.0;
    private double maximumPossibleScore = 0.0;
    private Double stdError = 0.0;
    RatingScaleModel rsm = new RatingScaleModel();

    public Theta(Double rawScore, Integer maximumPossibleScore){
        this.rawScore=rawScore;
        this.ID=rawScore.intValue();
        this.maximumPossibleScore=maximumPossibleScore;
    }

    public Theta(Integer maximumPossibleScore){
        this.rawScore=0.0;
        this.maximumPossibleScore=maximumPossibleScore;
    }

	public Theta(Double theta, double weight, int ID){
		this.theta=theta;
		this.weight=weight;
		this.ID=ID;
	}

    public Theta(Theta theta){
        this.theta = theta.value();
        this.rawScore = theta.getRawScore();
        this.stdError = theta.getStandardError();
        this.maximumPossibleScore = theta.maximumPossibleScore;
    }

	public Double value(){
		return theta;
	}

	public Double getWeight(){
		return weight;
	}

    public double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10,
            (double) places);
    }

    public int getID(){
    	return ID;
    }

    public void transformTheta(double intercept, double scale){
        originalTheta = theta;
    	theta=theta*scale+intercept;
        stdError *= scale;
    }

    public void transformTheta(double intercept, double scale, int precision){
        originalTheta = theta;
        theta=theta*scale+intercept;
        stdError *= scale;
        theta = Precision.round(theta, precision);
    }

//    public double getTransformedTheta(){
//    	return equatedTheta;
//    }

    public double getOriginalTheta(){
        return originalTheta;
    }

    public void setRawScore(double rawScore){
        this.rawScore=rawScore;
    }

    public double getRawScore(){
        return rawScore;
    }

    public void incrementRawScore(Double value){
        rawScore+=value;
        ID=rawScore.intValue();
    }

    public double getAdjustedRawScore(double adjust){
        double tempRaw = rawScore;
        if(rawScore==0.0){
            return tempRaw+=adjust;
        }else if(rawScore>=maximumPossibleScore){
            return tempRaw-=adjust;
        }
        return tempRaw;
    }

    public void increment(){
        freq++;
    }

    public double getFreq(){
        return freq;
    }

    public boolean perfect(){
        if(rawScore==0.0 || rawScore>=maximumPossibleScore) return true;
        return false;
    }

    public void prox(double adjust){
        theta = Math.log(getAdjustedRawScore(adjust)/(maximumPossibleScore-getAdjustedRawScore(adjust)));
    }

    public void updateMaximumPossibleScore(double maximumPossibleScore){
        this.maximumPossibleScore=maximumPossibleScore;
    }

    public void setPersonProx(double personProx){
        theta=personProx;
    }

    public void multiplyByExpansionFactor(double X){
        theta*=X;
    }

    public void recenter(double mean){
        theta-=mean;
    }
//
//    /**
//     * Computes a Newton-Rhapson update of examinee proficiency in Andrich's rating scale model. This method is used in
//     * Rasch estimation of examinee ability. It restricts the update to validItems
//     * (i.e. those without extreme item scores).
//     *
//     * It is called repeatedly in the JMLE routine until a global convergence criterion is satisfied.
//     *
//     * The method iterates until either maxNewtonUpdate or personConverge have been satisfied.
//     *
//     * @param rsGroup
//     * @param validItems
//     * @param iteration
//     * @param maxNewtonUpdate
//     * @param personConverge
//     */
//    public void update(LinkedHashMap<String, RatingScaleItemGroup> rsGroup, int maxNewtonUpdate, double personConverge, double adjust){
//        double delta = personConverge + 1.0, tempTheta=0.0, evSum=0.0, denomSum=0.0;
//        int iterationCounter = 0;
//        RatingScaleItemGroup rsg = null;
//        double adjustedRawScore = adjustedRawScore(adjust);
//
//        while(Math.abs(delta) >= personConverge && iterationCounter<maxNewtonUpdate){
//            evSum=0.0; //this is the TCC at and item difficulty rho
//            denomSum=0.0;
//
//            for(String s : rsGroup.keySet()){
//                rsg = rsGroup.get(s);
//                for(VariableName v : rsg.getValidItems()){//FIXME need to change this so that it accepts completed items too
//                    evSum+=rsm.expectedValue(this.rho(), rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//                    denomSum+=rsm.denomInf(this.rho(), rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//                }
//            }
//            delta = (adjustedRawScore - evSum)/-denomSum;
//            tempTheta = theta-delta;
//            theta = Math.max(Math.min(theta+1,tempTheta),theta-1);//do not change theta by more than one logit per iteration - from WINSTEPS documents
//            delta = theta-tempTheta;
//            iterationCounter++;
//        }
//    }
//
//    /**
//     * Newton-Rhapson update
//     *
//     * This method is for use when estimating theta for each examinee in the database. It is the same as above
//     * with the addition of a check for completedItems. In this manner, an examinee's theta is updated for only
//     * those valid items that have been completed by the examinee. This method is not used in JMLE estimation.
//     * It is only used for computing examinee level thetas.
//     *
//     * This method is kept separate from the previous one to optimize performance in the JMLE routine.
//     * The previous update() method does not need repeated if-then checks with if(completedItems.contains(v)).
//     * It should therefore be a little faster.
//     *
//     * @param rsGroup
//     * @param completedItems list of items completed by the examinee
//     * @param maxNewtonUpdate
//     * @param personConverge
//     */
//    public void update(LinkedHashMap<String, RatingScaleItemGroup> rsGroup, LinkedHashSet<VariableName> completedItems, int maxNewtonUpdate, double personConverge,
//            double adjust){
//        double delta = personConverge + 1.0, tempTheta=0.0, evSum=0.0, denomSum=0.0;
//        int iterationCounter = 0;
//        RatingScaleItemGroup rsg = null;
//        double adjustedRawScore = adjustedRawScore(adjust);
//
//        while(Math.abs(delta) >= personConverge && iterationCounter<maxNewtonUpdate){
//            evSum=0.0;
//            denomSum=0.0;
//
//            for(String s : rsGroup.keySet()){
//                rsg = rsGroup.get(s);
//                for(VariableName v : rsg.getValidItems()){
//                    if(completedItems.contains(v)){
//                        evSum+=rsm.expectedValue(this.rho(), rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//                        denomSum+=rsm.denomInf(this.rho(), rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//                    }
//                }
//            }
//            delta = (adjustedRawScore - evSum)/-denomSum;
//            tempTheta = theta-delta;
//            theta = Math.max(Math.min(theta+1,tempTheta),theta-1);//do not change theta by more than one logit per iteration - from WINSTEPS documents
//            delta = theta-tempTheta;
//            iterationCounter++;
//        }
//    }
//
//    /**
//     * Update method for estimation by proportional curve fitting
//     *
//     * @param rsGroup
//     * @param adjust
//     */
//    public double pcfUpdate(LinkedHashMap<String, RatingScaleItemGroup> rsGroup, double adjust, double d){
//        double previousTheta = theta;
//        RatingScaleItemGroup rsg = null;
//        double TCC1 = 0.0; //this is the TCC at current theta rho
//        double TCC2 = 0.0; //this is the TCC at current theta rho + d
//        for(String s : rsGroup.keySet()){
//            rsg = rsGroup.get(s);
//            for(VariableName v : rsg.getValidItems()){//FIXME need to change this so that it accepts completed items too
//                TCC1+=rsm.expectedValue(theta, rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//                TCC2+=rsm.expectedValue(theta+d, rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//            }
//        }
//        double adjustedRawScore = adjustedRawScore(adjust);
//        double xMin = 0.0;
//        double xMax = this.maximumPossibleScore;
//        double slope = d/(logisticOgive(TCC2, xMin, xMax)-logisticOgive(TCC1, xMin, xMax));
//        double intercept = theta - slope*logisticOgive(TCC1, xMin, xMax);
//        double tempTheta = slope*Math.log((adjustedRawScore-xMin)/(xMax-adjustedRawScore))+intercept;
//        theta = Math.max(Math.min(theta+1,tempTheta),theta-1);//do not change theta by more than one logit per iteration - from WINSTEPS documents
//        double delta = Math.abs(previousTheta-theta);
//        return delta;
//    }
//
//    /**
//     * Porportional curve fitting method
//     *
//     * This method is for use when estimating theta for each examinee in the database. It is the same as above
//     * with the addition of a check for completedItems. In this manner, an examinee's theta is updated for only
//     * those valid items that have been completed by the examinee. This method is not used in JMLE estimation.
//     * It is only used for computing examinee level thetas.
//     *
//     * This method is kept separate from the previous one to optimize performance in the JMLE routine.
//     * The previous update() method does not need repeated if-then checks with if(completedItems.contains(v)).
//     * It should therefore be a little faster.
//     *
//     * @param rsGroup
//     * @param completedItems list of items completed by the examinee
//     * @param maxNewtonUpdate
//     * @param personConverge
//     */
//    public void pcfUpdate(LinkedHashMap<String, RatingScaleItemGroup> rsGroup, LinkedHashSet<VariableName> completedItems, double adjust){
//        RatingScaleItemGroup rsg = null;
//        double TCC1 = 0.0; //this is the TCC at current theta rho
//        double TCC2 = 0.0; //this is the TCC at current theta rho + d
//        double d = 1.0;
//        for(String s : rsGroup.keySet()){
//            rsg = rsGroup.get(s);
//            for(VariableName v : rsg.getValidItems()){//FIXME need to change this so that it accepts completed items too
//                if(completedItems.contains(v)){
//                    TCC1+=rsm.expectedValue(theta, rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//                    TCC2+=rsm.expectedValue(theta+d, rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//                }
//            }
//        }
//        double adjustedRawScore = adjustedRawScore(adjust);
//        double xMin = 0.0;
//        double xMax = this.maximumPossibleScore;
//        double slope = d/(logisticOgive(TCC2, xMin, xMax)-logisticOgive(TCC1, xMin, xMax));
//        double intercept = theta - slope*logisticOgive(TCC1, xMin, xMax);
//        double tempTheta = slope*Math.log((adjustedRawScore-xMin)/(xMax-adjustedRawScore))+intercept;
//        theta = Math.max(Math.min(theta+1,tempTheta),theta-1);//do not change theta by more than one logit per iteration - from WINSTEPS documents
//    }
//
//    private double logisticOgive(double x, double xMin, double xMax){
//        return Math.log((x-xMin)/(xMax-x));
//    }

    /**
     * Adjusts extreme raw scores according to adjust. This method is consistent with WINSTEPS documentation. It
     * is used for PROX calculation of ability estimates for extreme raw scores.
     *
     * @param adjust
     * @return
     */
    public double adjustedRawScore(double adjust){
        double adjustedScore =rawScore;
        if(rawScore==0){
            adjustedScore = adjust;
        }else if(rawScore==maximumPossibleScore){
            adjustedScore = maximumPossibleScore-adjust;
        }
        return adjustedScore;
    }

    /**
     * Computes the standard error of the proficiency estimate. This method should be called before a call to
     * getStandardError(). This method is used for producing the raw to scale score conversion table.
     *
     * Called at the end of JMLE. The argument completedItems is the set of valid items when this method
     * is called for computing the standard errors for the raw to scale score conversion table. When this method
     * is called for computing the standard error for each examinee, The argument completedItems is the set of valid
     * items that were completed by the examinee.
     *
     * @param rsGroup
     * @param completedItems set of completed and valid items.
     */
//    public void computeStandardError(LinkedHashMap<String, RatingScaleItemGroup> rsGroup, LinkedHashSet<VariableName> completedItems){
//        double sum=0.0;
//
//        RatingScaleItemGroup rsg = null;
//        for(String s : rsGroup.keySet()){
//            rsg = rsGroup.get(s);
//            for(VariableName v : rsg.getValidItems()){
//                if(completedItems.contains(v)){
//                    sum+=rsm.denomInf(this.rho(), rsg.getItemDifficultyValueAt(v), rsg.getStepDifficulties());
//                }
//            }
//
//        }
//        stdError = 1/Math.sqrt(sum);
//    }

    /**
     * Returns the standard error of the estimated person proficiency
     * @return
     */
    public double getStandardError(){
        return stdError;
    }

    public String toStringWithRawScore(String title, boolean header, boolean footer){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        if(header){
            f.format("%-50s", title); f.format("%n");
            f.format("%50s", "=================================================="); f.format("%n");
            f.format("%10s", "Sum Score");f.format("%2s", " ");
            f.format("%10s", "Theta");f.format("%2s", " ");
            f.format("%10s", "Std. Error");f.format("%2s", " ");
            f.format("%10s", "Frequency");f.format("%2s", " "); f.format("%n");
            f.format("%50s", "--------------------------------------------------"); f.format("%n");
        }
        f.format("%10.4f", rawScore);f.format("%2s", " "); f.format("%10.4f", theta);f.format("%2s", " ");
        if(rawScore==0 || rawScore==maximumPossibleScore){
            f.format("%10.4s", "--"); f.format("%2s", " ");
        }else{
            f.format("%10.4f", stdError); f.format("%2s", " ");
        }
        f.format("%10.0f", freq);
        f.format("%n");
        if(footer){
            f.format("%50s", "=================================================="); f.format("%n");
        }

        return f.toString();
    }

    public int compareTo(Object o){
        if(!(o instanceof Theta))
		throw new ClassCastException("Theta object expected");

		Double otherTheta = ((Theta)o).value();

		if(this.theta>otherTheta) return 1;
		if(this.theta<otherTheta) return -1;
		return 0;
	}

    @Override
	public boolean equals(Object o){
		if(!(o instanceof Theta)) return false;
		if(o==this) return true;
		Theta t = (Theta)o;
		if(compareTo(t)==0) return true;
		return false;
	}

    @Override
	public int hashCode(){
		return theta.hashCode();
	}

    @Override
	public String toString(){
		return theta.toString();
	}

}


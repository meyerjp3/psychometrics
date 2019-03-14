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
package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.irt.model.IrmType;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.optimization.BOBYQAOptimizer;
import com.itemanalysis.psychometrics.uncmin.DefaultUncminOptimizer;
import com.itemanalysis.psychometrics.uncmin.UncminException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.univariate.*;
import org.apache.commons.math3.random.*;

import java.util.Formatter;
import java.util.LinkedHashMap;

public class IrtScaleLinking {

    private LinkedHashMap<VariableName, ItemResponseModel> itemFormX = null;
    private LinkedHashMap<VariableName, ItemResponseModel> itemFormY = null;
    private QuadratureRule distX = null;
    private QuadratureRule distY = null;
    private int precision = 4;
    private boolean useUncmin = true;
    private boolean raschFamily = false;
    private EquatingCriterionType stockingLordEquatingCriterionType = EquatingCriterionType.Q1Q2;
    private EquatingCriterionType haebaraEquatingCriterionType = EquatingCriterionType.Q1Q2;
    private MeanSigmaMethod ms = null;
    private MeanMeanMethod mm = null;
    private StockingLordMethod sl = null;
    private HaebaraMethod hb = null;
    private double fHB = 0.0;
    private double fSL = 0.0;


    public IrtScaleLinking(LinkedHashMap<VariableName, ItemResponseModel> itemFormX, LinkedHashMap<VariableName, ItemResponseModel> itemFormY,
                           QuadratureRule distX, QuadratureRule distY, boolean populationSd){
        this.itemFormX = itemFormX;
        this.itemFormY = itemFormY;
        this.distX = distX;
        this.distY = distY;

        ms = new MeanSigmaMethod(itemFormX, itemFormY, populationSd);
        mm = new MeanMeanMethod(itemFormX, itemFormY);
        hb = new HaebaraMethod(itemFormX, itemFormY, distX, distY, haebaraEquatingCriterionType);
        sl = new StockingLordMethod(itemFormX, itemFormY, distX, distY, stockingLordEquatingCriterionType);

    }

    public IrtScaleLinking(LinkedHashMap<VariableName, ItemResponseModel> itemFormX, LinkedHashMap<VariableName, ItemResponseModel> itemFormY,
                           QuadratureRule distX, QuadratureRule distY){
        this(itemFormX, itemFormY, distX, distY, true);
    }

    public void setPrecision(int precision){
        this.precision = precision;
    }

    public void useUncmin(boolean useUncmin){
        this.useUncmin = useUncmin;
    }

    public void setStockingLordCritionType(EquatingCriterionType stockingLordEquatingCriterionType){
        this.stockingLordEquatingCriterionType = stockingLordEquatingCriterionType;
    }

    public void setHaebaraCritionType(EquatingCriterionType haebaraEquatingCriterionType){
        this.haebaraEquatingCriterionType = haebaraEquatingCriterionType;
    }

    public void standardizedStockingLord(boolean standardized){
        sl.setStandardized(standardized);
    }

    public void standardizedHaebara(boolean standardized){
        hb.setStandardized(standardized);
    }

    /**
     * Check to see if all item response models are in teh Rasch family of models.
     *
     * @return true if all item response models are in the Rasch family. Otherwise, return false.
     */
    private boolean checkRaschModel(){
        ItemResponseModel irm = null;
        int count = 0;
        for(VariableName v : itemFormY.keySet()) {
            irm = itemFormY.get(v);

            if(irm.getType() == IrmType.L3 || irm.getType() == IrmType.L4) {
                if (irm.getGuessing() == 0 && irm.getDiscrimination() == 1.0 && irm.getSlipping() == 1.0) {
                    count++;
                }
            }else if (irm.getType() == IrmType.PCM) {
                count++;
            }else {
                return false;
            }
        }
        return count==itemFormY.size();
    }

    public void computeCoefficients(){
        raschFamily = checkRaschModel();

        ms.setPrecision(precision);
        mm.setPrecision(precision);
        hb.setPrecision(precision);
        sl.setPrecision(precision);

        if(raschFamily){

            double[] sv = {mm.getIntercept()};

            UnivariateOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
            JDKRandomGenerator g = new JDKRandomGenerator();

            //Haebara method
            MultiStartUnivariateOptimizer optimizer = new MultiStartUnivariateOptimizer(underlying, 5, g);//Five random starts to Brent optimizer.
            UnivariatePointValuePair hbPair = optimizer.optimize(new MaxEval(500),
                    new UnivariateObjectiveFunction(hb),
                    GoalType.MINIMIZE,
                    new SearchInterval(-4, 4),
                    new InitialGuess(sv));
            hb.setIntercept(hbPair.getPoint());
            hb.setScale(1.0);
            fHB = hbPair.getValue();

            //Stocking-Lord method
            UnivariatePointValuePair slPair = optimizer.optimize(new MaxEval(500),
                    new UnivariateObjectiveFunction(sl),
                    GoalType.MINIMIZE,
                    new SearchInterval(-4, 4),
                    new InitialGuess(sv));
            sl.setIntercept(slPair.getPoint());
            sl.setScale(1.0);
            fSL = slPair.getValue();

        }else{

            double[] hbStartValues = {mm.getIntercept(), mm.getScale()};
            double[] slStartValues = {mm.getIntercept(), mm.getScale()};

            if(useUncmin){
                DefaultUncminOptimizer optimizer = new DefaultUncminOptimizer();

                try{

                    optimizer.minimize(hb, hbStartValues);
                    double[] param = optimizer.getParameters();
                    fHB = optimizer.getFunctionValue();
                    hb.setIntercept(param[0]);

                    if(param.length>1){
                        hb.setScale(param[1]);
                    }else{
                        hb.setScale(1.0);//Rasch family of models
                    }

                    optimizer.minimize(sl, slStartValues);
                    param = optimizer.getParameters();
                    fSL = optimizer.getFunctionValue();
                    sl.setIntercept(param[0]);

                    if(param.length>1){
                        sl.setScale(param[1]);
                    }else{
                        sl.setScale(1.0);//Rasch family of models
                    }

                }catch(UncminException ex){
                    ex.printStackTrace();
                }
            }else{

                int numIterpolationPoints = 2 * 2;//two dimensions A and B
                BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
                RandomGenerator g = new JDKRandomGenerator();
                RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
                MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
                PointValuePair hbOptimum = optimizer.optimize(new MaxEval(1000),
                        new ObjectiveFunction(hb),
                        GoalType.MINIMIZE,
                        SimpleBounds.unbounded(2),
                        new InitialGuess(hbStartValues));

                double[] hbCoefficients = hbOptimum.getPoint();
                hb.setIntercept(hbCoefficients[0]);
                hb.setScale(hbCoefficients[1]);
                fHB = hbOptimum.getValue();

                PointValuePair slOptimum = optimizer.optimize(new MaxEval(1000),
                        new ObjectiveFunction(sl),
                        GoalType.MINIMIZE,
                        SimpleBounds.unbounded(2),
                        new InitialGuess(slStartValues));

                double[] slCoefficients = slOptimum.getPoint();
                sl.setIntercept(slCoefficients[0]);
                sl.setScale(slCoefficients[1]);
                fSL = slOptimum.getValue();

            }

        }

    }

    public MeanSigmaMethod getMeanSigmaMethod(){
        return ms;
    }

    public MeanMeanMethod getMeanMeanMethod(){
        return mm;
    }

    public HaebaraMethod getHaebaraMethod(){
        return hb;
    }

    public StockingLordMethod getStockingLordMethod(){
        return sl;
    }

    public double getHaebaraObjectiveFunctionValue(){
        return fHB;
    }

    public double getStockingLordObjectiveFunctionValue(){
        return fSL;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String gapFormat1 = "%-" + Math.max(13, precision+4+5) + "s";
        String gapFormat2 = "%-" + Math.max(9, precision+4+5) + "s";
        String intFormat = "%" + Math.max(13, (precision+4))  + "." + precision + "f";
        String sclFormat = "%" + Math.max(9, (precision+4))  + "." + precision + "f";
        f.format("%n");
        f.format("%60s", "                TRANSFORMATION COEFFICIENTS                   "); f.format("%n");
        f.format("%60s", "            Form X (New Form) to Form Y (Old Form)            "); f.format("%n");
        f.format("%63s", "==============================================================="); f.format("%n");
        f.format("%-18s", " Method");f.format(gapFormat2, "Slope (A)"); f.format("%5s"," "); f.format(gapFormat1, "Intercept (B)"); f.format("%5s"," "); f.format(gapFormat1, "fmin"); f.format("%n");
        f.format("%63s", "---------------------------------------------------------------"); f.format("%n");
        f.format("%-17s", " Mean/Mean");     f.format(sclFormat, mm.getScale());    f.format("%5s"," "); f.format(intFormat, mm.getIntercept());
        f.format("%5s"," "); f.format("%n");
        f.format("%-17s", " Mean/Sigma");    f.format(sclFormat, ms.getScale());    f.format("%5s"," "); f.format(intFormat, ms.getIntercept());
        f.format("%5s"," "); f.format("%n");
        f.format("%-17s", " Haebara");       f.format(sclFormat, hb.getScale());   f.format("%5s"," "); f.format(intFormat, hb.getIntercept()); f.format("%5s"," "); f.format("%13.6f", fHB);
        f.format("%5s"," "); f.format("%n");
        f.format("%-17s", " Stocking-Lord"); f.format(sclFormat, sl.getScale());    f.format("%5s"," "); f.format(intFormat, sl.getIntercept()); f.format("%5s"," "); f.format("%13.6f", fSL);
        f.format("%5s"," "); f.format("%n");
        f.format("%63s", "==============================================================="); f.format("%n");
        return f.toString();
    }





}

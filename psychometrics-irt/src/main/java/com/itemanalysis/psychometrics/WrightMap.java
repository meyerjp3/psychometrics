/*
 * Copyright 2018 J. Patrick Meyer
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
package com.itemanalysis.psychometrics;

import com.itemanalysis.psychometrics.data.VariableLabel;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import java.util.*;

/**
 * Creates a Wright map with each item response category located at the
 * point where the probability of a response equals the response
 * probability (RP) value.
 *
 */
public class WrightMap {

    private ItemResponseModel[] irm = null;

    private double[] rpValue = null;

    protected boolean descending = true;

    private int precision = 2;//number of decimal places in output

    private LinkedHashMap<String, Double> sortedMap = null;

    private HashMap<VariableName, String[]> responseLabels = null;

    private int maxLabelLength = 0;

    private int maxResponseLength = 0;


    public WrightMap(LinkedHashMap<VariableName, ItemResponseModel> irmMap){
        this(irmMap, true);
    }

    public WrightMap(LinkedHashMap<VariableName, ItemResponseModel> irmMap, boolean descending){
        this.irm = new ItemResponseModel[irmMap.size()];
        int index = 0;
        for(VariableName v : irmMap.keySet()){
            this.irm[index++] = irmMap.get(v);
        }
        responseLabels = new HashMap<VariableName, String[]>();
    }

    public WrightMap(ItemResponseModel[] irm){
        this(irm, true);
    }

    public WrightMap(ItemResponseModel[] irm, boolean descending){
        this.irm = irm;
        this.descending = descending;
        responseLabels = new HashMap<VariableName, String[]>();
    }

    /**
     * Creates an item map and sorts the results by location. Each item is allowed to have its own RP value.
     *
     * @param rpValue an array of RP values. The length of the array must be the same as the number
     *                of elements in the ItemResponse Model array
     * @param min minimum value for the Brent search
     * @param max maximum value for the Brent search
     * @param maxiter maximum number of iterations in the search
     * @return location of the response category where the probability equals the RP value
     */
    public LinkedHashMap<String, Double> createMap(double[] rpValue, double min, double max, int maxiter){

        int index = 0;
        String id = "";
        int ncat = 0;
        RpFunction func = null;
        UnivariateSolver solver = new BrentSolver(1.0e-12, 1.0e-8);
        HashMap<String, Double> unsortedMap = new HashMap<String, Double>();

        for(ItemResponseModel model : irm){
            ncat = model.getNcat();
            String[] cl = responseLabels.get(model.getName());

            for(int j=0;j<ncat;j++){
                id = "["+model.getName().toString();

                if(null!=cl && cl.length==ncat){
                    id += "_" + j + "] " + cl[j].trim();
                }else{
                    id += "] " + j;
                }

                VariableLabel l = model.getLabel();
                maxLabelLength = Math.max(maxLabelLength, l.toString().length());
                if(null!=l && !"".equals(l.toString().trim())){
                    id += ": " + l.toString();
                }

                func = new RpFunction(model, rpValue[index], j);
                double location = solver.solve(maxiter, func, min, max);
                unsortedMap.put(id, location);
            }
            index++;
        }

        sortedMap = sortByValue(unsortedMap);

        return sortedMap;

    }

    public LinkedHashMap<String, Double> createMap(double[] rpValue, double min, double max){
        return createMap(rpValue, min, max, 500);
    }

    /**
     * Method for creating an item map where all items use the same RP value.
     *
     * @param rpValue an RP value
     * @param min minimum value for the Brent search
     * @param max maximum value for the Brent search
     * @param maxiter maximum number of iterations in the search
     * @return location of the response category where the probability equals the RP value
     */
    public LinkedHashMap<String, Double> createMap(double rpValue, double min, double max, int maxiter){
        double[] rp = new double[irm.length];
        for(int i=0;i<rp.length;i++){
            rp[i] = rpValue;
        }

        return createMap(rp, min, max, maxiter);
    }

    public LinkedHashMap<String, Double> createMap(double rpValue, double min, double max){
        return createMap(rpValue, min, max, 500);
    }

    public int size(){
        return sortedMap.size();
    }

    /**
     * Apply response label to a particular item. Use this method when items use different response labels.
     * @param name item name
     * @param respLabel an array of response labels
     */
    public void setResponseLabelAt(VariableName name, String[] respLabel){
        for(int i=0;i<respLabel.length;i++){
            maxResponseLength = Math.max(maxResponseLength, respLabel[i].length());
        }
        responseLabels.put(name, respLabel);

    }

    /**
     * Apply same response labels to all items. Items must have same number of response categories.
     * @param respLabels an array of response labels
     */
    public void setResponseLabels(String[] respLabels){
        for(int i=0;i<irm.length;i++){
            setResponseLabelAt(irm[i].getName(), respLabels);
        }
    }

    public void setPrecision(int precision){
        this.precision = precision;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        maxLabelLength += 34+maxResponseLength;
        String bigBar = "";
        String litBar = "";
        for(int i=0;i<maxLabelLength;i++){
            bigBar += "=";
            litBar +="-";
        }

        f.format("%"+maxLabelLength+"s", bigBar);f.format("%n");
        f.format("%8s", "Location"); f.format("%-30s", "    Item Response Category"); f.format("%n");
        f.format("%"+maxLabelLength+"s", litBar);f.format("%n");

        for(String s : sortedMap.keySet()){
            f.format("%8."+precision+"f", sortedMap.get(s).doubleValue());
            f.format("%-30s", "    " + s);
            f.format("%n");
        }
        f.format("%"+maxLabelLength+"s", bigBar);f.format("%n");f.format("%n");

        return f.toString();
    }

    private LinkedHashMap<String, Double> sortByValue(HashMap<String, Double> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

        if(descending){
            // 2. Sort list with Collections.sort(), provide a custom Comparator
            //    Try switch the o1 o2 position for a different order
            Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return -(o1.getValue()).compareTo(o2.getValue());
                }
            });
        }else{
            // 2. Sort list with Collections.sort(), provide a custom Comparator
            //    Try switch the o1 o2 position for a different order
            Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return (o1.getValue()).compareTo(o2.getValue());
                }
            });
        }



        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }



    /**
     * Inner class needed for the Brent Solver
     */
    class RpFunction implements UnivariateFunction {

        private ItemResponseModel irm = null;
        private double rpValue = 0.5;
        private int category = 0;

        public RpFunction(ItemResponseModel irm, double rpValue, int category){
            this.irm = irm;
            this.rpValue = rpValue;
            this.category = category;
        }

        public double value(double x){
            return irm.cumulativeProbability(x, category) - rpValue;
        }

    }

}

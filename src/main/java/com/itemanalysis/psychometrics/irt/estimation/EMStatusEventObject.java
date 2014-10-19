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
package com.itemanalysis.psychometrics.irt.estimation;

import java.util.EventObject;

public class EMStatusEventObject extends EventObject {

    private String status = "";
    private String title = "EM CYCLE: ";
    private int iteration = 0;
    private double delta = 0.0;
    private double loglikelihood = 0.0;
    private String termCode = "";

    public EMStatusEventObject(Object source){
        super(source);
    }

    public EMStatusEventObject(Object source, String status){
        super(source);
        this.status = status;
    }

    /**
     * Used for EM and start value cycle information
     *
     * @param source
     * @param title
     * @param iteration
     * @param delta
     * @param loglikelihood
     */
    public EMStatusEventObject(Object source, String title, int iteration, double delta, double loglikelihood, String termCode){
        super(source);
        this.title = title;
        this.iteration = iteration;
        this.delta = delta;
        this.loglikelihood = loglikelihood;
        this.termCode = termCode;
    }

    public EMStatusEventObject(Object source, String title, int iteration, double delta, double loglikelihood){
        super(source);
        this.title = title;
        this.iteration = iteration;
        this.delta = delta;
        this.loglikelihood = loglikelihood;
    }

    /**
     * Use for EM cycle information only
     *
     * @param source
     * @param iteration
     * @param delta
     * @param loglikelihood
     */
    public EMStatusEventObject(Object source, int iteration, double delta, double loglikelihood){
        super(source);
        this.iteration = iteration;
        this.delta = delta;
        this.loglikelihood = loglikelihood;
    }

    public EMStatusEventObject(Object source, int iteration, double delta, double loglikelihood, String termCode){
        super(source);
        this.iteration = iteration;
        this.delta = delta;
        this.loglikelihood = loglikelihood;
        this.termCode = termCode;
    }

    public String getStatus(){
        return status;
    }

    public String getTitle(){
        return title;
    }

    public int getIteration(){
        return iteration;
    }

    public double getDelta(){
        return delta;
    }

    public double getLoglikelihood(){
        return loglikelihood;
    }

    public String getTermCode(){
        return termCode;
    }

}

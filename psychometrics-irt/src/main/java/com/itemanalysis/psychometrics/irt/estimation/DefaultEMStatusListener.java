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

import java.util.Formatter;

/**
 * An example EMStatusListener. A better alternative would be to write a listener
 * that sent the eventObject to a log file.
 */
public class DefaultEMStatusListener implements EMStatusListener {

    public DefaultEMStatusListener(){

    }

    public void handleEMStatusEvent(EMStatusEventObject eventObject){
        Formatter f = new Formatter();
        String s = eventObject.getStatus();
        if(!"".equals(s)){
            System.out.println(eventObject.getStatus());
        }else{
            f.format("%10s", eventObject.getTitle());
            f.format("%5d", eventObject.getIteration()); f.format("%4s", "");
            f.format("%.10f", eventObject.getDelta()); f.format("%4s", "");
            f.format("%.10f", eventObject.getLoglikelihood()); f.format("%4s", "");

            String tc = eventObject.getTermCode();

            if(!"".equals(tc)) f.format("%10s", tc); f.format("%4s", "");
            System.out.println(f.toString());
        }

    }

}

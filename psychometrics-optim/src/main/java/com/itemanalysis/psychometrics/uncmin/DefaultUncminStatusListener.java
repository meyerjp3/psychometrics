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
package com.itemanalysis.psychometrics.uncmin;

import java.util.ArrayList;

public class DefaultUncminStatusListener implements UncminStatusListener {

    private ArrayList<Integer> status = null;

    public DefaultUncminStatusListener(){
        status = new ArrayList<Integer>();
    }

    public void handleUncminEvent(UncminStatusEventObject eventObject){
        status.add(eventObject.getTerminationCode());
    }

    @Override
    public String toString(){
        String combinedMessage = "";
        for(int i=0;i<status.size();i++){
            combinedMessage += terminationCodeString(i);
            if(i<status.size()-1) combinedMessage += "\n";
        }
        return combinedMessage;
    }

    public String terminationCodeString(int terminationCode){
        switch(terminationCode){
            case 1: return "    UNCMIN TERM CODE =  1:  Terminated with gradientAt small, xpls is probably optimal";
            case 2: return "    UNCMIN TERM CODE =  2:  Terminated with stepsize small, xpls is probably optimal";
            case 3: return "    UNCMIN TERM CODE =  3:  Lower point cannot be found, xpls is probably optimal";
            case 4: return "    UNCMIN TERM CODE =  4:  Iteration limit (150) exceeded";
            case 5: return "    UNCMIN TERM CODE =  5:  Too many large steps, function may be unbounded";
        }
        return "    UNCMIN TERM CODE =  0:  Optimal solution found";
    }

}

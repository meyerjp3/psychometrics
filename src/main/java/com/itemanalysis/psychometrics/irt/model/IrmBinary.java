/**
 * Copyright 2016 J. Patrick Meyer
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
package com.itemanalysis.psychometrics.irt.model;

/**
 * This class provides a cleaner interface to the Irm3PL class. It does not require the user
 * to provide the scaling coefficient (i.e. D).
 */
public class IrmBinary extends Irm3PL {

    public IrmBinary(double difficulty){
        super(difficulty, 1.0);
    }

    public IrmBinary(double discrimination, double difficulty){
        super(discrimination, difficulty, 1.0);
    }

    public IrmBinary(double discrimination, double difficulty, double lowerAsymptote){
        super(discrimination, difficulty, lowerAsymptote, 1.0);
    }

}

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
package com.itemanalysis.psychometrics.factoranalysis;

import org.apache.commons.math3.linear.RealMatrix;

import java.util.Formatter;

public class RotationResults {

    private double functionValue = 0.0;
    private RealMatrix L = null;
    private RealMatrix Phi = null;
    private RealMatrix rotationMatrix = null;
    private RotationMethod rotationMethod = null;

    public RotationResults(double funcitonValue, RealMatrix L, RealMatrix Phi, RealMatrix rotationMatrix, RotationMethod rotationMethod){
        this.functionValue = funcitonValue;
        this.L = L;
        this.Phi = Phi;
        this.rotationMatrix = rotationMatrix;
        this.rotationMethod = rotationMethod;
    }

    public double getFunctionValue(){
        return functionValue;
    }

    public RealMatrix getFactorLoadings(){
        return L;
    }

    public RealMatrix getPhi(){
        return Phi;
    }

    public RealMatrix getRotationMatrix(){
        return rotationMatrix;
    }

    public String getRotationMethod(){
        return rotationMethod.toString();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        int nrow = L.getRowDimension();
        int ncol = L.getColumnDimension();
        f.format("%-40s", "Factor Loadings: " + rotationMethod.toString());f.format("%n");
        f.format("%40s", "========================================");f.format("%n");
        for(int i=0;i<nrow;i++){
            for(int j=0;j<ncol;j++){
                f.format("% 6.4f", L.getEntry(i,j));f.format("%4s", "");
            }
            f.format("%n");
        }
        f.format("%n");
        f.format("%n");

        f.format("%-30s", "Factor Correlations");f.format("%n");
        f.format("%30s", "==============================");f.format("%n");
        for(int i=0;i<Phi.getRowDimension();i++){
            for(int j=0;j<Phi.getColumnDimension();j++){
                f.format("% 6.4f", Phi.getEntry(i,j));f.format("%4s", "");
            }
            f.format("%n");
        }
        f.format("%n");

        return f.toString();
    }

}

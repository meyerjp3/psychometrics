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
package com.itemanalysis.psychometrics.quadrature;

/**
 * An interface for quadrature approximations such as those used for quadrature points and weights in
 * numeric integration. Each approximation has an array of evaluation points (x-axis) and an array
 * of density values (y-axis). The minimum and maximum values and the number of evaluation points
 * (and corresponding density values) are usually provided by the user.
 *
 */
public interface QuadratureRule {

    /**
     * Gets an array of evaluation points.
     * 
     * @return evaluation points.
     */
    public double[] getPoints();

    /**
     * Gets and array of density values.
     * 
     * @return density values.
     */
    public double[] evaluate();

    /**
     * Gets an element from the array of evaluation points.
     * 
     * @param index array index of evaluation points.
     * @return evaluation point at index.
     */
    public double getPointAt(int index);

    /**
     * Gets an elements from the array of density values.
     * 
     * @param index array index of density value.
     * @return density value at index.
     */
    public double getDensityAt(int index);

    /**
     * Number of evaluation points (and corresponding number of density values) in this approximation.
     * 
     * @return number of points.
     */
    public int getNumberOfPoints();

    public void setPointAt(int index, double point);

    public void setDensityAt(int index, double density);

    public double getMinimum();

    public double getMaximum();

    public double getMean();

    public double getStandardDeviation();

    public double[] standardize(boolean keepPoints);

}

/**
 * Copyright 2015 J. Patrick Meyer
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

import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;

/**
 * Interface for IRT estimation. It does not provide an interface to the estimation itself.
 * Rather, it provides access to the parts of or output from the estimation routine.
 */
public interface IrtEstimation {

    public int getNumberOfItems();

    public int getNumberOfPeople();

    public ItemResponseModel getItemResponseModelAt(int index);

    public double getResidualAt(int i, int j);

}

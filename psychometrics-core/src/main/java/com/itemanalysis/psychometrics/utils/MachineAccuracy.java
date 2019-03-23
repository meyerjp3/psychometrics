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
package com.itemanalysis.psychometrics.utils;


/**
 * determines machine accuracy
 *
 * @version $Id: MachineAccuracy.java,v 1.4 2001/09/09 22:17:11 alexi Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class MachineAccuracy
{
	//
	// Public stuff
	//

	/** machine accuracy constant */
	public static double EPSILON = 2.220446049250313E-16;
	
	public static double SQRT_EPSILON = 1.4901161193847656E-8;
	public static double SQRT_SQRT_EPSILON = 1.220703125E-4;

	/**
	 * compute EPSILON from scratch
	 * @return epsilon
	 */
	public static double computeEpsilon()
	{
		double eps = 1.0;

		while( eps + 1.0 != 1.0 )
		{
			eps /= 2.0;
		}
		eps *= 2.0;
		
		return eps;
	}

	/**
	 * @return true if the relative difference between the two parameters
	 * is smaller than SQRT_EPSILON.
	 *
	 * @param a value 1
	 * @param b value 2
	 * @return whether or not values are the same within epsilon
	 *
	 */
	public static boolean same(double a, double b) {
		return Math.abs((a/b)-1.0) <= SQRT_EPSILON;
	}
}

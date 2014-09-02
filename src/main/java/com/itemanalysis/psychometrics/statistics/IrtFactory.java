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
package com.itemanalysis.psychometrics.statistics;

import java.util.Random;

/**
 * This class is used for simulating data from a binary IRT model.
 * It also computes IRT model probabilities.
 *
 * @author J. Patrick Meyer
 * @since July 12, 2008
 *
 */
@Deprecated
public class IrtFactory {

	Random noise;

	public IrtFactory(){
		noise = new Random();
	}

	public int[][] r3pl(double[] b, double[] a, double[] c, double[] theta, double D){
		int ni=b.length;
		int np=theta.length;
		int[][] u = new int[np][ni];
		for(int i=0;i<np;i++){
			for(int j=0;j<ni;j++){
				if(p3pl(b[j], a[j], c[j], D, theta[i])>=noise.nextDouble()){
					u[i][j]=1;
				}else{
					u[i][j]=0;
				}
			}
		}
		return u;
	}

	public double prasch(double b, double theta){
		return p3pl(b, 1.0, 0.0, 1.0, theta);
	}

	public double p3pl(double b, double a, double c, double D, double theta){
		double p=Math.exp(D*a*(theta-b));
		return c + (1-c)*(p/(1+p));
	}

    /**
     * Compute First derivative of 3PL. Adapted from ICC3PLFunc::ICC3PLDeriv1 in
     * Brad Hanson's ETIRM.
     *
     * @param numParam number of item parameters 1PL = 1, 2PL =2, 3PL = 3
     * @param a discrimination parameter
     * @param b difficulty parameter
     * @param c pseudo guessing parameter
     * @param D scaling factor D = 1.7 or D = 1
     * @param theta person ability parameter
     * @return gradientAt
     */
    public IRTGradient p3plDeriv1(int numParam, double a, double b, double c, double D, double theta){
        IRTGradient gradient = new IRTGradient();
        double t = Math.exp(-a*D * (theta - b));
        double onept2 = Math.pow(1.0 + t,2);

        // derivative with respect to the b parameter
        double derivb = -a * (1.0 -c) * D * t;
        derivb /= onept2;

        gradient.setDerviWRT_B(derivb);
        
        if(numParam == 1){
            return gradient;
        }

        // derivative with respect to the a parameter
        double da = (1.0 - c) * (theta - b) * D * t;
        da /= onept2;
        gradient.setDerivWRT_A(da);

        // derivative with respect to the c parameter
        if(numParam == 3){
            double dc = - 1.0 / (1.0 + t);
            dc += 1.0;
            gradient.setDerivWRT_C(dc);
        }
        return gradient;
    }

}

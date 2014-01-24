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
package com.itemanalysis.psychometrics.polycor;

import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author J. Patrick Meyer
 */
public class PolyserialPluginTest {

    public PolyserialPluginTest() {
    }

    /**
     * Test of rho method, of class PolyserialPlugin.
     */
//    @Test
//    public void testValue() throws Exception {
//        PolyserialPlugin ps = new PolyserialPlugin();
//        double[] trueThresholds = {-0.9944579, 0.5100735, 1.5464331};//values from R
//
//        try{
//            File f = new File("../testdata/polyserial-data.txt");
//            BufferedReader br = new BufferedReader(new FileReader(f));
//            String line = "";
//            String[] strData = null;
//            while((line=br.readLine())!=null){
//                strData = line.split(",");
//                ps.increment(Double.parseDouble(strData[0]), Integer.parseInt(strData[1]));
//            }
//            br.close();
//
//            System.out.println("Polyserial plugin target rho: " + 0.5344298);//rho from R
//            assertEquals("polyserial plugin test target rho of: " + 0.5344298,
//                    0.5344298,
//                    Precision.round(ps.value(), 7), //rounding to same precision as R output
//                    1e-10);
//
//            double[] thresholds = ps.getThresholds();
//
//            assertEquals("Threshold length test:", trueThresholds.length, thresholds.length);
//
//            for(int i=0;i<thresholds.length;i++){
//                System.out.println("testing threshold " + i + " rho of: " + Precision.round(thresholds[i],7));
//                assertEquals("polyserial plugin threshold test " + i + ": ",
//                        trueThresholds[i],
//                        Precision.round(thresholds[i],7),
//                        1e-10);
//            }
//
//
//        }catch(IOException ex){
//            ex.printStackTrace();
//        }
//
//
//    }

}
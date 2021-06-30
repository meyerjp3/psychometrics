package com.itemanalysis.psychometrics.cmh;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CochranMantelHaenszelTest {

    @Test
    public void test1(){
        CochranMantelHaenszel cmh = new CochranMantelHaenszel("F", "M", "item1", false);
        cmh.increment(1.0, "M", 0.0, 40);
        cmh.increment(1.0, "M", 1.0, 30);
        cmh.increment(1.0, "F", 0.0, 20);
        cmh.increment(1.0, "F", 1.0, 10);

        assertEquals("Common odds ration test", 1.5, cmh.commonOddsRatio(), 1e-15);
    }

    @Test
    public void test2(){
        System.out.println("Testing CMH procedure and comparing to SAS results.");


        String[] group = {"foc", "foc", "foc", "foc", "foc", "foc", "foc", "foc", "foc", "foc",
                "foc", "foc", "foc", "foc", "foc", "foc", "ref", "ref", "ref", "ref",
                "ref", "ref", "ref", "ref", "ref", "ref", "ref", "ref", "ref", "ref",
                "ref", "ref", "ref", "ref"};

        double[] stratum ={1, 1, 2, 2, 3, 3, 4, 4, 5, 5,
                6, 6, 7, 7, 8, 9, 1, 1, 2, 2,
                3, 3, 4, 4, 5, 5, 6, 6, 7, 7,
                8, 8, 9, 10};

        double[] itemScore = {0, 1, 0, 1, 0, 1, 0, 1, 0, 1,
                0, 1, 0, 1, 1, 1, 0, 1, 0, 1,
                0, 1, 0, 1, 0, 1, 0, 1, 0, 1,
                0, 1, 1, 1};

        long[] freq = {57, 10, 1503, 432, 1859, 1736, 563, 1383, 124, 541,
                26, 175, 8, 73, 22, 9, 53, 10, 2054, 866,
                2647, 3856, 884, 3316, 188, 1301, 50, 474, 13, 193,
                3, 71, 17, 3};

        CochranMantelHaenszel cmh = new CochranMantelHaenszel("foc", "ref", "example1", false);

        for(int i=0;i<group.length;i++){
            cmh.increment(stratum[i], group[i], itemScore[i], freq[i]);
        }

        double odds = cmh.commonOddsRatio();
        double[] ci = cmh.commonOddsRatioConfidenceInterval(odds);

        //True results are from SAS
        assertEquals("CMH chi-square", 205.2952852813, cmh.cochranMantelHaenszel(), 1e-8);
        assertEquals("CMH p-value", 1.46000887157466E-46, cmh.getPValue(), 1e-8);
        assertEquals("CMH odds-ratio", 1.53011608995743, odds, 1e-8);
        assertEquals("CMH odss-ration CI lower", 1.44335984061157, ci[0], 1e-5);
        assertEquals("CMH odss-ration CI upper", 1.62208701037061, ci[1], 1e-5);
        assertEquals("CMH odss-ration focal count", 8521, cmh.getTotalFocalSize(), 1e-8);
        assertEquals("CMH odss-ration reference count", 15999, cmh.getTotalReferenceSize(), 1e-8);
        assertEquals("CMH ETS class", "B-", cmh.getETSDifClassification());



        //System.out.println(cmh.getTidyOutput().toString());

        //Check data output
        CochranMantelHaenszel cmh2 = new CochranMantelHaenszel("foc", "ref", "example1", false);
        ArrayList<String[]> dataTable = cmh.getFrequencyTables();
        for(String[] s : dataTable){
            cmh2.increment(Double.parseDouble(s[2]), s[1], Double.parseDouble(s[3]), Long.parseLong(s[4]));
//            System.out.println(String.join(",", s));
        }


        odds = cmh2.commonOddsRatio();
        ci = cmh2.commonOddsRatioConfidenceInterval(odds);

        assertEquals("CMH chi-square", 205.2952852813, cmh2.cochranMantelHaenszel(), 1e-8);
        assertEquals("CMH p-value", 1.46000887157466E-46, cmh2.getPValue(), 1e-8);
        assertEquals("CMH odds-ratio", 1.53011608995743, odds, 1e-8);
        assertEquals("CMH odss-ration CI lower", 1.44335984061157, ci[0], 1e-5);
        assertEquals("CMH odss-ration CI upper", 1.62208701037061, ci[1], 1e-5);
        assertEquals("CMH odss-ration focal count", 8521, cmh2.getTotalFocalSize(), 1e-8);
        assertEquals("CMH odss-ration reference count", 15999, cmh2.getTotalReferenceSize(), 1e-8);
        assertEquals("CMH ETS class", "B-", cmh2.getETSDifClassification());



    }


}
package com.itemanalysis.psychometrics.irt.estimation;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ItemResponseFileSummaryTest {


    @Test
    public void lsat7Test(){
        System.out.println("LSAT7 file summary test");
        double[] true_freq = {12,19,1,7,3,19,3,17,10,5,3,7,7,23,8,28,7,39,11,34,14,51,15,90,6,25,7,35,18,136,32,308};
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        ItemResponseVector[] data = fileSummary.getCondensedResponseVectors(f, 0, 5, true);

        for(int i=0;i<data.length;i++){
            assertEquals("LSAT7 frequency: ", true_freq[i], data[i].getFrequency(), 1e-15);
        }

    }


}
package com.itemanalysis.psychometrics.classicalitemanalysis;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StreamingItemAnalysisTest {

    @Test
    public void testStreamingAnalysis() throws IOException {
        // This map will hold the analysis object for each item.
        LinkedHashMap<String, StreamingItemAnalysis> itemMap = new LinkedHashMap<>();
        int nItems = 20;

        // Initialize analysis objects for 20 items
        for (int i = 0; i < nItems; i++) {
            String itemName = "item" + (i + 1);
            // Setting includeProportions=true, includeBiserial=false, includePointBiserial=true
            itemMap.put(itemName, new StreamingItemAnalysis(itemName, true, false, true, DiscriminationType.PEARSON));
        }

        File f = FileUtils.toFile(this.getClass().getResource("/testdata/jalabert-jan15.csv"));
        assertNotNull("Test data file not found: /testdata/jalabert-jan15.csv", f);

        BufferedReader br = new BufferedReader(new FileReader(f));

        // Skip header
        br.readLine();

        String line;
        String[] itemNames = itemMap.keySet().toArray(new String[0]);

        // Process each examinee's responses
        while ((line = br.readLine()) != null) {
            String[] s = line.split(",");
            double rawScore = 0;
            double[] responseVector = new double[s.length];

            // Calculate raw score for the examinee
            for (int i = 0; i < s.length; i++) {
                responseVector[i] = Double.parseDouble(s[i]);
                rawScore += responseVector[i];
            }

            // Increment item statistics for each item
            for (int i = 0; i < nItems; i++) {
                StreamingItemAnalysis itemAnalysis = itemMap.get(itemNames[i]);
                itemAnalysis.increment(s[i], responseVector[i], rawScore);
            }
        }
        br.close();

        // Verification and Assertions
        // The analysis is complete. Now, get the final statistics and assert their correctness.
        // The expected values below are placeholders. You should replace them with the actual expected values from a known source.

        StreamingItemAnalysis item1 = itemMap.get("item1");
        System.out.println(item1.toString());
        assertEquals("Item difficulty for item1 is not correct.", 1.26, item1.getItemDifficulty(), 0.001);
        assertEquals("Item discrimination for item1 is not correct.", 0.505, item1.getItemTotalCorrelation(), 0.001);


        StreamingItemAnalysis item2 = itemMap.get("item2");
        System.out.println(item2.toString());
        assertEquals("Item difficulty for item2 is not correct.", 1.459, item2.getItemDifficulty(), 0.001);
        assertEquals("Item discrimination for item2 is not correct.", 0.4645, item2.getItemTotalCorrelation(), 0.001);

        // Example for another item
        StreamingItemAnalysis item20 = itemMap.get("item20");
        System.out.println(item20.toString());
        assertEquals("Item difficulty for item20 is not correct.", 1.583, item20.getItemDifficulty(), 0.001);
        assertEquals("Item discrimination for item20 is not correct.", 0.6504, item20.getItemTotalCorrelation(), 0.001);
    }
}
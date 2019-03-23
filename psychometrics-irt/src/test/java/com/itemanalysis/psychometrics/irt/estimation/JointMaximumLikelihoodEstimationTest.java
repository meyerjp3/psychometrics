package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmPCM;
import com.itemanalysis.psychometrics.irt.model.IrmPoissonCounts;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.util.Precision;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class JointMaximumLikelihoodEstimationTest {

    private byte[][] guttman = {
            {1,1,1,1},
            {1,1,1,0},
            {1,1,0,0},
            {1,0,0,0},
            {0,0,0,0}
    };



    //true estimates from WINSTEPS
    double[] pcm_true_theta = {-1.2888,-0.9794,0.4989,-2.178,-0.6944,-1.8889,-0.6944,-2.5791,1.2668,4.8791,
        0.0199,-0.2731,2.8814,1.7561,-1.4625,-1.6583,1.2668,-2.178,-1.4625,-0.6944,
        0.0199,-0.2731,0.8582,2.0524,0.0199,0.3325,0.4989,4.8791,-0.8353,-0.5548,
        -0.5548,1.4982,2.0524,-0.1286,1.2668,0.8582,1.7561,0.4989,1.4982,0.8582,
        0.4989,0.4989,-2.5791,-2.5791,-0.4148,-1.4625,1.0549,-0.4148,0.6735,0.6735};
    double[] pcm_true_difficulty = {.4660, 1.2748, -.7871, -.6225, -.5352, -.5179, .3122, 1.0925, -.0478, -.6349};
    double[][] pcm_true_thresholds = {
        {-.6938, -.2761, .9699},
        {-1.2529, .2577, .9952},
        {.6468, -1.1317, .4849},
        {-.8579, .0950, .7629},
        {-.4673, -.6340, 1.1013},
        {-1.2687, .0250, 1.2437},
        {-1.5642, -.0758, 1.6400},
        {-.9223, -.1147, 1.0370},
        {-.1785, -1.1312, 1.3097},
        {-.6653, -.3791, 1.0445}};

    //true estimates from WINSTEPS
    double[] pcm_true_theta_transformed = {37.1125,40.2061,54.9885,28.2200,43.0562,31.1113,43.0562,24.2093,62.6681,98.7911,
            50.1987,47.2689,78.8137,67.5608,35.3750,33.4170,62.6681,28.2200,35.3750,43.0562,
            50.1987,47.2689,58.5818,70.5238,50.1987,53.3252,54.9885,98.7911,41.6475,44.4524,
            44.4524,64.9821,70.5238,48.7142,62.6681,58.5818,67.5608,54.9885,64.9821,58.5818,
            54.9885,54.9885,24.2093,24.2093,45.8522,35.3750,60.5494,45.8522,56.7353,56.7353};
    double[] pcm_true_difficulty_transformed = {54.6595, 62.7477, 42.128, 43.7751, 44.6478, 44.8210, 53.1216, 60.9251,
            49.5225, 43.6510};
    double[][] pcm_true_thresholds_transformed = {
            {-6.9378,-2.7614, 9.6992},
            {-12.529, 2.5768, 9.9524},
            { 6.4684,-11.317, 4.8486},
            {-8.5787, 0.9497, 7.6290},
            {-4.6734,-6.3399,11.0134},
            {-12.6870,0.2501,12.4366},
            {-15.642,-0.7584,16.4005},
            {-9.2227,-1.1470,10.3698},
            {-1.7854,-11.312,13.0974},
            {-6.6531,-3.7915,10.4445}};

    //true estimates from WINSTEPS
    double[] rsm_true_theta = {-1.2719,-0.9343, 0.5404,-2.2333,-0.6294,-1.9253,-0.6294,-2.6523, 1.2445, 4.8110,
         0.0899,-0.1970, 2.8000, 1.7006,-1.4623,-1.6760, 1.2445,-2.2333,-1.4623,-0.6294,
         0.0899,-0.1970, 0.8702, 1.9835, 0.0899, 0.3859, 0.5404, 4.8110,-0.7790,-0.4835,
        -0.4835, 1.4586, 1.9835,-0.0542, 1.2445, 0.8702, 1.7006, 0.5404, 1.4586, 0.8702,
         0.5404, 0.5404,-2.6523,-2.6523,-0.3398,-1.4623, 1.0501,-0.3398, 0.7011, 0.7011};
    double[] rsm_true_difficulty = {.4970, 1.2741, -.9124, -.5864, -.5471, -.4305, .3132, 1.1092, -.0910, -.6260};
    double[][] rsm_true_thresholds = {
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441},
        {-0.8213, -0.2227, 1.0441}};

    //true estimates from WINSTEPS
    double[] cat_true_theta = {-2.3064,-3.6151,0.1305,1.2456,0.1305,0.6409,-0.857,-1.4489,-2.3064,-1.4489,-0.857,
            0.1305,-0.3527,-1.4489,0.6409,-0.3527,0.6409,-1.4489,2.1237,-3.6151,0.5052,1.163,-0.6378,2.0902,
            -0.0687,1.163,2.0902,-3.5113,3.4526,0.5052,1.163,1.163,-1.2801,0.5052,-0.0687,-0.6378,-0.0687,
            -0.0687,-3.5113,3.4526};
    double[] cat_true_difficulty = {-.8282, .1644, 1.4034, .1644, -3.8751, -.7563, .3229, .0203, -.9233,
            -5.3438, -.4100, .1782, -.1191, -.4100, 1.1934};





    private byte[][] readTapData(){
        byte[][] tap = new byte[35][18];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/tap-data.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    tap[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return tap;
    }

    private byte[][] readPcmData(){
        byte[][] pcm = new byte[50][10];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/pcm-data.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    pcm[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return pcm;
    }

    private byte[][] readMixedFormatData(){
        byte[][] data = new byte[100][15];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/mixed-format-2.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    data[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return data;
    }

    private byte[][] readMixedFormatData3(){
        byte[][] data = new byte[150][15];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/mixed-format-2.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    data[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return data;
    }

    private byte[][] readCatData(){
        byte[][] cat = new byte[40][15];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/cat-data.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    cat[row][j] = Byte.parseByte(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return cat;
    }

    private byte[][] readNullCategoryData(){
        byte[][] data = new byte[75][8];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/null-categories.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<s.length;j++){
                    if(j>0) data[row][j-1] = Byte.parseByte(s[j]);//first column contains and id. Skip it.
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return data;
    }

    private byte[][] readPoissonCountsData(){
        byte[][] data = new byte[500][8];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/poisson-counts-example.csv"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=1;j<s.length;j++){
                    data[row][j-1] = Byte.parseByte(s[j]);//skip first variable. It is an id variable, not an item.
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return data;
    }

    /**
     * Because of the Guttman pattern, parameters are inestimable. The algorithm should
     * recognize the Guttman pattern and throw an exception because no data are left after
     * iteratively removing extreme persons and items.
     */
    @Test(expected = NoDataException.class)
    public void testGuttmanPattern(){
        System.out.println("JMLE Guttman pattern test.");
        ItemResponseModel[] irm = new ItemResponseModel[4];
        for(int i=0;i<4;i++){
            irm[i] = new Irm3PL(0.0, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(guttman, irm);
        jmle.summarizeData(0.3);
    }

    /**
     * This test involves the TAP data from Wright and Stone's Best Test Design text.
     * It is also the data in WINSTEPS example1.
     *
     * Using the values jmle.estimateParameters(150, 0.00001, 1, .01), jMetrik should
     * give four decimal places of accuracy with winsteps when winsteps uses the
     * same convergence criterion (i.e control file uses CONVERGE=L and LCONV = 0.00001.)
     * More decimal places of accuracy are possible if use a more stringent criterion.
     *
     */
    @Test
    public void testTapData(){
        System.out.println("JMLE TAP DATA TEST");
        byte[][] data = readTapData();
        int nItems = data[0].length;
        int nPeople = data.length;

        //true estimates from WINSTEPS
        double[] tap_true_theta = {-3.0853, -.2759, 0.9885, -3.7594, -2.3553, -1.4619, 2.9878, -0.2759,-2.3553, -3.7594,
                2.0459, -.2759, -.2759, -1.4619, -6.7909, -.2759, .9885, -.2759, -.2759, .9885, -4.4750, -.2759, -.2759,
                3.8891, -.2759, -3.0853, -.2759, 2.0459, .9885, -1.4619, -.2759, 3.8891, 2.0459, .9885, 2.0459};
        double[] tap_true_difficulty = {-6.7490, -6.7490, -6.7490, -4.5520, -3.9692, -3.5053, -3.9692, -2.4393, -3.5053,
                -1.6287, 0.8284, 2.3267, 2.0282, 3.4997, 4.9620, 4.9620, 4.9620, 6.2965};
        double[] true_item_stdError = {1.8730, 1.8730, 1.8730, .8230, .7144, .6520, .7144, .5509, .6520, .4925, .4584,
                .5605, .5331, .7123, 1.0894, 1.0894, 1.0894, 1.8759};
        //winsteps only gives two decimal places for fit statistics
        double[] true_wms = {1.00,1.00,1.00,0.92,1.07,1.21,1.38,0.60,0.63,1.10,1.12,1.21,0.71,1.64,0.77,0.77,0.77,1.00};
        double[] true_ums = {1.00,1.00,1.00,0.35,0.53,1.03,2.48,0.44,0.21,0.86,0.81,1.14,0.37,1.67,0.11,0.11,0.11,1.00};

        ItemResponseModel[] irm = new ItemResponseModel[18];
        for(int i=0;i<nItems;i++){
            irm[i] = new Irm3PL(0.0, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);

        jmle.itemProx();
//        System.out.println(jmle.printBasicItemStats());
        jmle.estimateParameters(150, 0.00001);
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();
        jmle.computeItemFitStatistics();

//        ExploratoryFactorAnalysis efa = jmle.getPrincipalComponentsForStandardizedResiduals(5);
//        System.out.println(efa.printOutput("Principal Component Analysis of Std. Residuals"));



//        System.out.println(jmle.printFrequencyTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing extreme items");
        assertEquals("  JMLE tap test: extreme", -1, jmle.getExtremeItemAt(0));
        assertEquals("  JMLE tap test: extreme", -1, jmle.getExtremeItemAt(1));
        assertEquals("  JMLE tap test: extreme", -1, jmle.getExtremeItemAt(2));
        assertEquals("  JMLE tap test: extreme",  0, jmle.getExtremeItemAt(3));
        assertEquals("  JMLE tap test: extreme",  0, jmle.getExtremeItemAt(4));
        assertEquals("  JMLE tap test: extreme",  1, jmle.getExtremeItemAt(17));

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE tap test: difficulty", tap_true_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing difficulty std. error");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE tap test: std. error", true_item_stdError[j], Precision.round(irm[j].getDifficultyStdError(), 5), 1e-4);
        }

        System.out.println("     Testing INFIT WMS");
        for(int j=0;j<nItems;j++){
            //fit statistic not applicable to extreme items
            if(jmle.getExtremeItemAt(j)==0){
                assertEquals("  JMLE tap test: WMS", true_wms[j], Precision.round(jmle.getItemFitStatisticsAt(j).getWeightedMeanSquare(), 5), 1e-2);
            }
        }

        System.out.println("     Testing OUTFIT WMS");
        for(int j=0;j<nItems;j++){
            //fit statistic not applicable to extreme items
            if(jmle.getExtremeItemAt(j)==0){
                assertEquals("  JMLE tap test: UMS", true_ums[j], Precision.round(jmle.getItemFitStatisticsAt(j).getUnweightedMeanSquare(), 5), 1e-2);
            }
        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<nPeople;i++){
            assertEquals("  JMLE tap test: ability", tap_true_theta[i], Precision.round(theta[i], 5), 1e-4);
        }
    }


    /**
     * This test involves the TAP data from Wright and Stone's Best Test Design text.
     * It is also the data in WINSTEPS example1. It involves three fixed item parameters.
     *
     * Using the values jmle.estimateParameters(250, 0.00001, 1, .01), jMetrik should
     * give four decimal places of accuracy with winsteps when winsteps uses the
     * same convergence criterion (i.e control file uses CONVERGE=L and LCONV = 0.00001.)
     * More decimal places of accuracy are possible if use a more stringent criterion.
     *
     */
    @Test
    public void testTapDataFixedParam(){
        System.out.println("JMLE TAP DATA TEST FIXED PARAMETERS");
        byte[][] data = readTapData();
        int nItems = data[0].length;

        //true estimates from WINSTEPS
        double[] tap_true_difficulty = {-6.6171, -6.6171, -6.6171, -4.4262, -3.7000, -3.3882, -3.8476, -2.3363, -3.3882,
                -1.5375, 0.8500, 2.3796, 2.0832, 3.5000, 5.0043, 5.0043, 5.0043, 6.3376};

        ItemResponseModel[] irm = new ItemResponseModel[18];
        for(int i=0;i<nItems;i++){
            irm[i] = new Irm3PL(0.0, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        //set fixed parameters
        irm[4].setFixed(true);
        irm[4].setDifficulty(-3.7);
        irm[10].setFixed(true);
        irm[10].setDifficulty(0.85);
        irm[13].setFixed(true);
        irm[13].setDifficulty(3.5);

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.itemProx();
        jmle.estimateParameters(150, 0.00001);
//        System.out.println(jmle.printIterationHistory());


        System.out.println("     Testing fixed difficulty");
        for(int j=0;j<irm.length;j++){
            assertEquals("  JMLE tap test: fixed difficulty", tap_true_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
            System.out.println(irm[j].toString());
        }

    }

    /**
     * Fix all items and check th score table. It should have raw scores from 0 to 18 because all
     * items are fixed and none of the extreme items are dropped.
     *
     * True theta values in score table (true_scoreTable) from WINSTEPS using the item
     * parameters in tap_true_difficulty array.
     */
    @Test
    public void testTapDataAllItemsFixed(){
        System.out.println("JMLE TAP DATA SCORE TABLE WITH FIXED PARAMETERS");
        byte[][] data = readTapData();
        int nItems = data[0].length;

        //true estimates from WINSTEPS
        double[] tap_true_difficulty = {-6.6171, -6.6171, -6.6171, -4.4262, -3.7000, -3.3882, -3.8476, -2.3363, -3.3882,
                -1.5375, 0.8500, 2.3796, 2.0832, 3.5000, 5.0043, 5.0043, 5.0043, 6.3376};

        double[] true_scoreTable = {-8.93904, -7.51335, -6.43587, -5.57658, -4.82076, -4.15276, -3.53151, -2.90290,
                -2.20359, -1.34004, -.19676, 1.03313, 2.07424, 2.99413, 3.85601, 4.68154, 5.53466, 6.61733, 8.07333};

        ItemResponseModel[] irm = new ItemResponseModel[18];
        for(int i=0;i<nItems;i++){
            irm[i] = new Irm3PL(0.0, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));

            irm[i].setDifficulty(tap_true_difficulty[i]);
            irm[i].setFixed(true);
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.itemProx();
        jmle.estimateParameters(150, 0.005);

        String table = jmle.printScoreTable(150, 0.005, 0.3, new DefaultLinearTransformation(0, 1), 5);
        Object[][] scoreTable = jmle.getScoreconversionTableForOutputter();
//        System.out.println(table);
//        System.out.println(jmle.printIterationHistory());


        for(int i=0;i<true_scoreTable.length; i++){
            assertEquals("  JMLE tap test: fixed difficulty score table", true_scoreTable[i], (Double)scoreTable[i][1], 1e-4);
        }

    }

    /**
     * This test involves polytomous items scored in four categories. A
     * partial credit item is applied to each item. Thus, it estimates
     * item difficulty and several threshold parameters for each item.
            */
    @Test
    public void testPartialCreditModel(){
        System.out.println("JMLE PCM DATA TEST");
        byte[][] data = readPcmData();
        int nItems = data[0].length;

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] threshold = {0.0, 0.0, 0.0};
        for(int i=0;i<nItems;i++){
            irm[i] = new IrmPCM(0.0, threshold, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.itemProx();
        jmle.estimateParameters(50, 0.00001);
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();
        jmle.computeItemFitStatistics();
//        System.out.println(jmle.printBasicItemStats());

//        ExploratoryFactorAnalysis efa = jmle.getPrincipalComponentsForStandardizedResiduals(5);
//        System.out.println(efa.printOutput("Principal Component Analysis of Standardized Residuals"));

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println(jmle.printRatingScaleTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());




        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE pcm test: difficulty", pcm_true_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing thresholds");
        for(int j=0;j<nItems;j++){
            double[] est_thresh = irm[j].getThresholdParameters();
            for(int k=0;k<est_thresh.length;k++){
                assertEquals("  JMLE pcm test: threshold", pcm_true_thresholds[j][k], Precision.round(est_thresh[k], 5), 1e-4);
            }

        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<data.length;i++){
            assertEquals("  JMLE pcm test: ability", pcm_true_theta[i], Precision.round(theta[i], 5), 1e-4);
        }

    }

    /**
     * This test also uses the PCM data, but instead of using a partial credit model
     * for each item, it uses a rating scale model. Thus, the threshold estimates
     * must be the same for all items.
     */
    @Test
    public void testRatingScaleModel(){
        System.out.println("JMLE RSM DATA TEST");
        byte[][] data = readPcmData();
        int nItems = data[0].length;

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] threshold = {0.0, 0.0, 0.0};
        for(int i=0;i<nItems;i++){
            irm[i] = new IrmPCM(0.0, threshold, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));

            //Assiging same groupID to all items puts them in teh same rating scale group
            irm[i].setGroupId("A");
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(50, 0.00001);

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println(jmle.printRatingScaleTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE pcm test: difficulty", rsm_true_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing thresholds");
        for(int j=0;j<nItems;j++){
            double[] est_thresh = irm[j].getThresholdParameters();
            for(int k=0;k<est_thresh.length;k++){
                assertEquals("  JMLE pcm test: threshold", rsm_true_thresholds[j][k], Precision.round(est_thresh[k], 5), 1e-4);
            }

        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<data.length;i++){
            assertEquals("  JMLE pcm test: ability", rsm_true_theta[i], Precision.round(theta[i], 5), 1e-4);
        }

    }

    /**
     * This test uses data that includes missing responses like you would see in a CAT or
     * in the context of a common item equating design. It also includes several extreme
     * items and persons. The missing data and extreme persons and items must be correctly
     * identified to obtain accurate estimates.
     */
    @Test
    public void testCatData(){
        System.out.println("JMLE CAT DATA TEST");
        byte[][] data = readCatData();
        int nItems = data[0].length;
        int nPeople = data.length;

        ItemResponseModel[] irm = new ItemResponseModel[15];
        for(int i=0;i<nItems;i++){
            irm[i] = new Irm3PL(0.0, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(150, 0.00001);
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE cat test: difficulty", cat_true_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<nPeople;i++){
            assertEquals("  JMLE cat test: ability", cat_true_theta[i], Precision.round(theta[i], 5), 1e-4);
        }
    }

    /**
     * This test involves polytomous items scored in four categories. A
     * partial credit item is applied to each item. Thus, it estimates
     * item difficulty and several threshold parameters for each item.
     *
     * Same test as before but applies a linear transformation.
     */
    @Test
    public void testPartialCreditModelWithTransformation(){
        System.out.println("JMLE PCM DATA TEST - TRANSFORMED");
        byte[][] data = readPcmData();
        int nItems = data[0].length;

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] threshold = {0.0, 0.0, 0.0};
        for(int i=0;i<nItems;i++){
            irm[i] = new IrmPCM(0.0, threshold, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(50, 0.00001);
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();
        jmle.linearTransformation(50.0, 10.0);//do linear transformation here

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println(jmle.printRatingScaleTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE pcm test: difficulty", pcm_true_difficulty_transformed[j], Precision.round(irm[j].getDifficulty(), 5), 1e-3);
        }

        System.out.println("     Testing thresholds");
        for(int j=0;j<nItems;j++){
            double[] est_thresh = irm[j].getThresholdParameters();
            for(int k=0;k<est_thresh.length;k++){
                assertEquals("  JMLE pcm test: threshold", pcm_true_thresholds_transformed[j][k], Precision.round(est_thresh[k], 5), 1e-3);
            }

        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<data.length;i++){
            assertEquals("  JMLE pcm test: ability", pcm_true_theta_transformed[i], Precision.round(theta[i], 5), 1e-3);
        }

    }


    /**
     * This test involves the TAP data from Wright and Stone's Best Test Design text.
     * It is also the data in WINSTEPS example1. Instead of centering on items,
     * intdeterminacy is resolved by centering on persons. A linear
     * transformation is then applied to put the estimates on a normal ogive scale.
     *
     * Using the values jmle.estimateParameters(150, 0.00001, 1, .01), jMetrik should
     * give four decimal places of accuracy with winsteps when winsteps uses the
     * same convergence criterion (i.e control file uses CONVERGE=L and LCONV = 0.00001.)
     * More decimal places of accuracy are possible if use a more stringent criterion.
     *
     */
    @Test
    public void testTapDataCenterPersons(){
        System.out.println("JMLE TAP DATA TEST - CENTER PERSONS");

        //true estimates from WINSTEPS
        double[] tap_true_theta_pcenter = {-1.7044,-0.0469,0.6991,-2.1021,-1.2737,-0.7466,1.8787,-0.0469,-1.2737,-2.1021,
                1.3230,-0.0469,-0.0469,-0.7466,-3.8907,-0.0469,0.6991,-0.0469,-0.0469,0.6991,
                -2.5243,-0.0469,-0.0469,2.4105,-0.0469,-1.7044,-0.0469,1.3230,0.6991,-0.7466,
                -0.0469,2.4105,1.3230,0.6991,1.3230};
        double[] tap_true_difficulty_pcenter = {-3.8660, -3.8660, -3.8660, -2.5697, -2.2259, -1.9522, -2.2259, -1.3233, -1.9522, -.8450, .6047,
                1.4886, 1.3125, 2.1807, 3.0435, 3.0435, 3.0435, 3.83095};


        byte[][] data = readTapData();
        int nItems = data[0].length;
        int nPeople = data.length;

        ItemResponseModel[] irm = new ItemResponseModel[18];
        for(int i=0;i<nItems;i++){
            irm[i] = new Irm3PL(0.0, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(150, 0.00001, false); //estimate parameters and center on persons
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();
        jmle.linearTransformation(0.0, 0.59);//Do linear transformation here. Put on normal ogive scale

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE tap test: difficulty", tap_true_difficulty_pcenter[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<nPeople;i++){
            assertEquals("  JMLE tap test: ability", tap_true_theta_pcenter[i], Precision.round(theta[i], 5), 1e-4);
        }
    }

    /**
     * This test involves polytomous items scored in four categories. A
     * partial credit item is applied to each item. Thus, it estimates
     * item difficulty and several threshold parameters for each item.
     *
     * Items 1, 5, 8, and 10 have been fixed to specified values.
     *
     * The procedure for fixing items is different in this example than
     * the one in testPartialCreditModelFixed(). Here parameters are
     * fixed after instantiation by calling methods.
     *
     */
    @Test
    public void testPartialCreditModelSomeFixed(){
        System.out.println("JMLE PCM DATA TEST");
        byte[][] data = readPcmData();
        int nItems = data[0].length;

        double[] pcm_fixed_winsteps_difficulty = {0.4600, 1.2227, -0.8435, -0.6782, -0.6000, -0.5736, 0.2583, 1.0000, -0.102, -0.7000};
        double[][] pacm_fixed_winsteps_thresholds ={
                {-0.7000, -0.3000, 1.0000},
                {-1.2554, 0.2589, 0.9964},
                {0.6412, -1.1306, 0.4894},
                {-0.8639, 0.0965, 0.7674},
                {-0.5000, -0.6000, 1.1000},
                {-1.2751, 0.0268, 1.2483},
                {-1.5692, -0.0739, 1.6431},
                {-0.9000, -0.1500, 1.0500},
                {-0.1833, -1.1296, 1.3130},
                {-0.7500, -0.3500, 1.1000}
        };

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] threshold = {0.0, 0.0, 0.0};
        for(int i=0;i<nItems;i++){
            irm[i] = new IrmPCM(0.0, threshold, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        //set fixed parameters
        irm[0].setFixed(true);
        irm[0].setDifficulty(0.46);
        irm[0].setThresholdParameters(new double[]{-0.7, -0.3, 1.0});

        irm[4].setFixed(true);
        irm[4].setDifficulty(-0.60);
        irm[4].setThresholdParameters(new double[]{-0.50, -0.60, 1.10});

        irm[7].setFixed(true);
        irm[7].setDifficulty(1.00);
        irm[7].setThresholdParameters(new double[]{-0.90, -0.15, 1.05});

        irm[9].setFixed(true);
        irm[9].setDifficulty(-0.70);
        irm[9].setThresholdParameters(new double[]{-0.75, -0.35, 1.10});

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.itemProx();
        jmle.estimateParameters(150, 0.00001);

//        System.out.println(jmle.printIterationHistory());


        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
//            System.out.println(irm[j].toString());
            assertEquals("  JMLE pcm test: difficulty", pcm_fixed_winsteps_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing thresholds");
        for(int j=0;j<nItems;j++){
            double[] est_thresh = irm[j].getThresholdParameters();
            for(int k=0;k<est_thresh.length;k++){
                assertEquals("  JMLE pcm test: threshold", pacm_fixed_winsteps_thresholds[j][k], Precision.round(est_thresh[k], 5), 1e-4);
            }

        }

    }

    /**
     * This test involves polytomous items scored in four categories. A
     * partial credit item is applied to each item. Thus, it estimates
     * item difficulty and several threshold parameters for each item.
     * Unlike testPartialCreditModel(), this test fixes item 1 and item 5
     * to specific values.
     *
     * The procedure for fixing items is different in this example than
     * the one in testPartialCreditModelSomeFixed(). Here parameters are
     * fixed in the constructor.
     *
    */
    @Test
    public void testPartialCreditModelFixed(){
        System.out.println("JMLE PCM DATA TEST - FIXED");

        //true estimates from WINSTEPS PCM
        double[] pcm_fixed_true_theta = {-1.0581,-0.7314,0.7719,-2.0132,-0.4339,-1.7010,-0.4339,-2.4452,1.5297,5.1272,
            0.2951,-0.0009,3.1327,2.0137,-1.2431,-1.4528,1.5297,-2.0132,-1.2431,-0.4339,
            0.2951,-0.0009,1.1267,2.3077,0.2951,0.6071,0.7719,5.1272,-0.5805,-0.2894,
            -0.2894,1.7584,2.3077,0.1456,1.5297,1.1267,2.0137,0.7719,1.7584,1.1267,
            0.7719,0.7719,-2.4452,-2.4452,-0.1455,-1.2431,1.3207,-0.1455,0.9445,0.9445};
        double[] pcm_fixed_true_difficulty = {0.6000, 1.5360, -.5415, -.3754, -0.3000, -.2724, .5665, 1.3550, .2082, -.3899};
        double[][] pcm_fixed_true_thresholds = {
            {-0.8000,-0.2000,1.0000},
            {-1.2582, 0.2658,0.9924},
            { 0.6091,-1.1186,0.5095},
            {-0.8972, 0.1112,0.7860},
            {-1.5000, 0.5000,1.0000},
            {-1.3098, 0.0426,1.2671},
            {-1.5899,-0.0599,1.6497},
            {-0.9287,-0.1061,1.0347},
            {-0.2047,-1.1168,1.3214},
            {-0.7059,-0.3632,1.0691}};

        byte[][] data = readPcmData();
        int nItems = data[0].length;

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] threshold = {0.0, 0.0, 0.0};
        for(int i=0;i<nItems;i++){
            if(i==0){
                //fix the first item to known values
                irm[i] = new IrmPCM(pcm_fixed_true_difficulty[0], pcm_fixed_true_thresholds[0], 1.0);
                irm[i].setFixed(true);
            }else if(i==4){
                //fix the fifth item to known values
                irm[i] = new IrmPCM(pcm_fixed_true_difficulty[4], pcm_fixed_true_thresholds[4], 1.0);
                irm[i].setFixed(true);
            }else{
                irm[i] = new IrmPCM(0.0, threshold, 1.0);
            }
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(50, 0.00001);
//        jmle.computeItemStandardErrors();

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println(jmle.printRatingScaleTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE pcm fixed test: difficulty", pcm_fixed_true_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-3);
        }

        System.out.println("     Testing thresholds");
        for(int j=0;j<nItems;j++){
            double[] est_thresh = irm[j].getThresholdParameters();
            for(int k=0;k<est_thresh.length;k++){
                assertEquals("  JMLE pcm fixed test: threshold", pcm_fixed_true_thresholds[j][k], Precision.round(est_thresh[k], 5), 1e-4);
            }

        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<data.length;i++){
            assertEquals("  JMLE pcm fixed test: ability", pcm_fixed_true_theta[i], Precision.round(theta[i], 5), 1e-3);
        }

    }


       /**
     * This test involves the TAP data from Wright and Stone's Best Test Design text.
     * It is also the data in WINSTEPS example1. It differs from testTapData()
        * because it uses two fixed item parameters.
     *
     * Using the values jmle.estimateParameters(150, 0.00001, 1, .01), jMetrik should
     * give four decimal places of accuracy with winsteps when winsteps uses the
     * same convergence criterion (i.e control file uses CONVERGE=L and LCONV = 0.00001.)
     * More decimal places of accuracy are possible if use a more stringent criterion.
     *
     */
    @Test
    public void testTapDataFixed(){
        System.out.println("JMLE TAP DATA TEST - FIXED");

        //true estimates from WINSTEPS
        double[] tap_true_fixed_theta = {-2.8747,-0.0587,1.2057,-3.5514,-2.1417,-1.2457,3.2048,-0.0587,-2.1417,-3.5514,2.263,
                -0.0587,-0.0587,-1.2457,-6.5862,-0.0587,1.2057,-0.0587,-0.0587,1.2057,-4.2688,-0.0587,-0.0587,4.1061,
                -0.0587,-2.8747,-0.0587,2.263,1.2057,-1.2457,-0.0587,4.1061,2.263,1.2057,2.263};
        double[] tap_true_fixed_difficulty = {-6.5394,-6.5394,-6.5394,-4.3412,-3.7577,-3.2930,-3.8000,-2.2000,-3.2930,-1.4131,
                1.0453,2.5437,2.2452,3.7168,5.1791,5.1791,5.1791,6.5136};


        byte[][] data = readTapData();
        int nItems = data[0].length;
        int nPeople = data.length;

        ItemResponseModel[] irm = new ItemResponseModel[18];
        for(int i=0;i<nItems;i++){
            if(i==6){
                irm[i] = new Irm3PL(-3.8, 1.0);
                irm[i].setFixed(true);
            }else if(i==7){
                irm[i] = new Irm3PL(-2.2, 1.0);
                irm[i].setFixed(true);
            }else{
                irm[i] = new Irm3PL(0.0, 1.0);
            }
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(200, 0.00001);
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println(jmle.printIterationHistory());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE tap test fixed: difficulty", tap_true_fixed_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-3);
        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<nPeople;i++){
            assertEquals("  JMLE tap test fixed: ability", tap_true_fixed_theta[i], Precision.round(theta[i], 5), 1e-3);
        }
    }

    /**
     * This test involves the TAP data from Wright and Stone's Best Test Design text.
     * It is also the data in WINSTEPS example1.
     *
     * Using the values jmle.estimateParameters(150, 0.00001, 1, .01), jMetrik should
     * give four decimal places of accuracy with winsteps when winsteps uses the
     * same convergence criterion (i.e control file uses CONVERGE=L and LCONV = 0.00001.)
     * More decimal places of accuracy are possible if use a more stringent criterion.
     *
     */
    @Test
    public void testTapDataBiasCorrection(){
        System.out.println("JMLE TAP DATA TEST - BIAS CORRECTION");

        //true estimates from WINSTEPS
    double[] tap_true_theta_stbias = {-2.9945,-0.2678,0.9595,-3.6489,-2.286,-1.4189,2.8999,-0.2678,-2.286,-3.6489,
            1.9857,-0.2678,-0.2678,-1.4189,-6.5911,-0.2678,0.9595,-0.2678,-0.2678,0.9595,-4.3434,-0.2678,-0.2678,
            3.7747,-0.2678,-2.9945,-0.2678,1.9857,0.9595,-1.4189,-0.2678,3.7747,1.9857,0.9595,1.9857};
    double[] tap_true_difficulty_stbias = {-6.2669,-6.2669,-6.2669,-4.2268,-3.6857,-3.2549,-3.6857,-2.2651,-3.2549,-1.5124,
            0.7692,2.1605,1.8833,3.2497,4.6076,4.6076,4.6076,5.8468};

        byte[][] data = readTapData();
        int nItems = data[0].length;
        int nPeople = data.length;

        ItemResponseModel[] irm = new ItemResponseModel[18];
        for(int i=0;i<nItems;i++){
            irm[i] = new Irm3PL(0.0, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(150, 0.00001);
        jmle.biasCorrection();
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE tap test bias correction: difficulty", tap_true_difficulty_stbias[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<nPeople;i++){
            assertEquals("  JMLE tap test bias correction: ability", tap_true_theta_stbias[i], Precision.round(theta[i], 5), 1e-4);
        }
    }

    /**
     * This test involves 10 binary and 5 three-category polytomous items.
     * True estimates obtained from Winsteps using the STBIAS=Y option.
    */
    @Test
    public void testMixedFormat(){
        System.out.println("JMLE MIXED FORMAT DATA TEST - BIAS CORRECTED");
        byte[][] data = readMixedFormatData();
        int nItems = data[0].length;

        //true values from winsteps
        double[] true_difficulty = {-3.7169,.4811,-.0805,-1.0032,-2.7765,.9942,2.8631,.8779,-2.3394,.9942,.8129,
                2.0111,1.3631,-.7204,.2395};
        double[][] true_thresholds = {
                {0.7680,-0.7680},
                {0.4958, -0.4958},
                {0.0297,-0.0297},
                {-0.4434, 0.4434},
                {-0.1286, 0.1286}};
        double[] true_theta = {
                -1.1815,1.4831,0.1904,0.1904,0.9667,-1.673,1.4831,-0.7699,-0.4138,-2.2797,
                3.6656,-1.673,0.9667,1.2204,-1.1815,-0.0975,1.7634,-0.4138,0.7152,4.9303,
                -1.1815,-1.673,0.9667,-1.1815,-0.4138,4.9303,-1.1815,1.7634,2.4362,0.4587,
                -2.2797,-0.0975,2.0734,0.7152,1.2204,3.6656,-0.7699,0.4587,0.9667,2.0734,
                2.9068,-0.0975,-0.0975,-1.673,3.6656,0.1904,2.4362,0.4587,-3.052,0.4587,
                4.9303,-0.0975,2.4362,1.4831,1.4831,0.7152,-0.7699,-2.2797,0.7152,-0.0975,
                -0.4138,0.9667,0.4587,3.6656,3.6656,1.7634,-0.0975,-0.0975,-1.673,-0.0975,
                1.2204,0.1904,0.9667,0.7152,0.4587,0.9667,0.9667,1.2204,-1.1815,-1.1815,
                2.4362,-1.1815,2.0734,-0.0975,-3.052,2.4362,2.4362,-0.0975,0.4587,-1.1815,
                -0.0975,-0.7699,2.4362,1.4831,3.6656,-0.7699,-0.7699,1.2204,-1.1815,1.4831};

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] threshold = {0.0, 0.0};
        for(int i=0;i<nItems;i++){
            if(i<10){
                irm[i] = new Irm3PL(0.0, 1.0);
            }else{
                irm[i] = new IrmPCM(0.0, threshold, 1.0);
            }
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(50, 0.00001);
        jmle.biasCorrection();
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println(jmle.printRatingScaleTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
            assertEquals("  JMLE mixed test bias corrected: difficulty", true_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing thresholds");
        int index = 10;
        for(int j=0;j<5;j++){
            index = 10+j;
            double[] est_thresh = irm[index].getThresholdParameters();
            for(int k=0;k<est_thresh.length;k++){
                assertEquals("  JMLE mixed test bias corrected: threshold", true_thresholds[j][k], Precision.round(est_thresh[k], 5), 1e-4);
            }
        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<data.length;i++){
            assertEquals("  JMLE mixed test bias corrected: ability", true_theta[i], Precision.round(theta[i], 5), 1e-4);
        }

    }


    /**
     * This test involves 10 binary and 5 three-category polytomous items.
     * True estimates obtained from Winsteps using the STBIAS=Y option.
    */
    @Test
    public void testMixedFormat3(){
        System.out.println("JMLE MIXED FORMAT DATA TEST 3 - BIAS CORRECTED");
        byte[][] data = readMixedFormatData3();
        int nItems = data[0].length;

        //true values from winsteps
//        double[] true_difficulty = {};
//        double[][] true_thresholds = {
//                };
//        double[] true_theta = {
//                };

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] threshold = {0.0, 0.0};
        double[] threshold2 = {0.0, 0.0};
        for(int i=0;i<nItems;i++){
            if(i<10){
                irm[i] = new Irm3PL(0.0, 1.0);
            }else if(i==10 && i<13){
                irm[i] = new IrmPCM(0.0, threshold, 1.0);
            }else{
                irm[i] = new IrmPCM(0.0, threshold2, 1.0);
            }
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(50, 0.00001);
        jmle.biasCorrection();
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println(jmle.printRatingScaleTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems;j++){
//            assertEquals("  JMLE mixed test bias corrected: difficulty", true_difficulty[j], Precision.round(irm[j].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing thresholds");
        int index = 10;
        for(int j=0;j<5;j++){
            index = 10+j;
            double[] est_thresh = irm[index].getThresholdParameters();
            for(int k=0;k<est_thresh.length;k++){
//                assertEquals("  JMLE mixed test bias corrected: threshold", true_thresholds[j][k], Precision.round(est_thresh[k], 5), 1e-4);
            }
        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<data.length;i++){
//            assertEquals("  JMLE mixed test bias corrected: ability", true_theta[i], Precision.round(theta[i], 5), 1e-4);
        }

    }

    /**
     * These data include 75 responses to 8 items. Each item should be
     * scored 0, 1, 2, and 3 but two items have null categories.
     * Item 1 has no observations of the value 1 and Item 2 has no
     * observations of the value 3. These items should be dropped
     * and the analysis should run as though they were never included.
     * You should get the same results as running the analysis without
     * item 1 and item 2.
     */
    @Test
    public void testNullCategory(){
        System.out.println("JMLE PCM NULL CATEGORY TEST");
        byte[][] data = readNullCategoryData();
        int nItems = data[0].length;

        //True values from WINSTEPS. First two items omitted.
        double[] true_difficulty = {0.6538, 0.8331, -0.8814, 0.6335, 0.0391, -1.2781};
        double[] true_difficulty_stdError = {0.1947,0.1546,0.1545,0.1523,0.1406,0.1778};
        double[] true_infit_ms = {1.04,1.06,1.00,1.01,0.91,0.99};
        double[] true_outfit_ms = {1.08,1.00,1.00,0.90,0.85,0.97};
        double[][] true_threshold = {
                {-2.7177,0.3570,2.3608},
                {-1.0270,-0.1273,1.1544},
                {-1.2895,0.8853,0.4042},
                {-1.1960,0.5614,0.6345},
                {-0.5000,-0.0200,0.5199},
                {-1.8075,0.1434,1.6640}};
        double[] true_theta = {1.1508,1.9057,-0.4664,0.0563,1.9057,-0.1997,-0.1997,0.8492,-0.4664,-0.4664,
                0.8492,-1.9498,1.9057,-2.5772,0.5727,0.0563,0.0563,0.8492,0.0563,-0.1997,
                -1.0823,-1.4698,0.5727,0.3111,0.5727,-0.7553,-1.9498,-0.1997,1.4931,0.0563,
                -3.5135,0.3111,0.8492,1.1508,-0.1997,-0.7553,-0.4664,0.0563,-0.7553,-3.5135,
                1.1508,0.3111,0.0563,-0.1997,-0.4664,1.4931,0.5727,-0.4664,-1.0823,0.8492,
                2.4533,-0.7553,0.3111,0.3111,1.9057,1.1508,-2.5772,1.4931,-0.7553,0.5727,
                0.8492,1.9057,-2.5772,0.5727,-1.9498,-0.1997,0.8492,0.8492,1.1508,-1.0823,
                -3.5135,-0.7553,1.1508,0.3111,0.5727};
        double[] true_theta_std_error = {0.5644,0.6814,0.5249,0.504,0.6814,0.5096,0.5096,0.5358,0.5249,0.5249,
                0.5358,0.7356,0.6814,0.8572,0.5173,0.504,0.504,0.5358,0.504,0.5096,
                0.594,0.6536,0.5173,0.5068,0.5173,0.5522,0.7356,0.5096,0.6089,0.504,
                1.1175,0.5068,0.5358,0.5644,0.5096,0.5522,0.5249,0.504,0.5522,1.1175,
                0.5644,0.5068,0.504,0.5096,0.5249,0.6089,0.5173,0.5249,0.594,0.5358,
                0.8103,0.5522,0.5068,0.5068,0.6814,0.5644,0.8572,0.6089,0.5522,0.5173,
                0.5358,0.6814,0.8572,0.5173,0.7356,0.5096,0.5358,0.5358,0.5644,0.594,
                1.1175,0.5522,0.5644,0.5068,0.5173};

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        double[] threshold = {0.0, 0.0, 0.0};
        for(int i=0;i<nItems;i++){
            irm[i] = new IrmPCM(0.0, threshold, 1.0);
            irm[i].setName(new VariableName("V"+(i+1)));
        }

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.itemProx();
        jmle.estimateParameters(50, 0.00001);
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();
        jmle.computeItemFitStatistics();

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println(jmle.printRatingScaleTables());
//        System.out.println();
//        System.out.println(jmle.printBasicItemStats());
//        System.out.println();
//        System.out.println(jmle.printPersonStats());
//        System.out.println();
//        System.out.println(jmle.printIterationHistory());

        System.out.println("     Testing dropped items");
        assertEquals("   JMLE null category test: dropped status 1", -1, jmle.getDroppedStatusAt(0));
        assertEquals("   JMLE null category test: dropped status 2", -1, jmle.getDroppedStatusAt(1));
        assertEquals("   JMLE null category test: dropped status 3", 0, jmle.getDroppedStatusAt(2));
        assertEquals("   JMLE null category test: dropped status 4", 0, jmle.getDroppedStatusAt(3));
        assertEquals("   JMLE null category test: dropped status 5", 0, jmle.getDroppedStatusAt(4));
        assertEquals("   JMLE null category test: dropped status 6", 0, jmle.getDroppedStatusAt(5));
        assertEquals("   JMLE null category test: dropped status 7", 0, jmle.getDroppedStatusAt(6));
        assertEquals("   JMLE null category test: dropped status 8", 0, jmle.getDroppedStatusAt(7));

        System.out.println("     Testing difficulty");
        for(int j=0;j<nItems-2;j++){
            assertEquals("  JMLE null category test: difficulty", true_difficulty[j], Precision.round(irm[j+2].getDifficulty(), 5), 1e-4);
        }

        System.out.println("     Testing difficulty std. error");
        for(int j=0;j<nItems-2;j++){
            assertEquals("  JMLE null category test: std. error", true_difficulty_stdError[j], Precision.round(irm[j+2].getDifficultyStdError(), 5), 1e-4);
        }

        System.out.println("     Testing infit ms");
        for(int j=0;j<nItems-2;j++){
            assertEquals("  JMLE null category test: infit", true_infit_ms[j], Precision.round(jmle.getItemFitStatisticsAt(j+2).getWeightedMeanSquare(), 2), 1e-2);
        }

        System.out.println("     Testing outfit ms");
        for(int j=0;j<nItems-2;j++){
            assertEquals("  JMLE null category test: outfit", true_outfit_ms[j], Precision.round(jmle.getItemFitStatisticsAt(j+2).getUnweightedMeanSquare(), 2), 1e-2);
        }

        System.out.println("     Testing thresholds");
        for(int j=0;j<nItems-2;j++){
            double[] est_thresh = irm[j+2].getThresholdParameters();
            for(int k=0;k<est_thresh.length;k++){
                assertEquals("  JMLE null category test: threshold", true_threshold[j][k], Precision.round(est_thresh[k], 5), 1e-4);
            }
        }

        System.out.println("     Testing persons");
        double[] theta = jmle.getPersonEstimates();
        for(int i=0;i<data.length;i++){
            assertEquals("  JMLE pcm test: ability", true_theta[i], Precision.round(theta[i], 5), 1e-4);
        }

        System.out.println("     Testing person std. error");
        double[] theta_std_error = jmle.getPersonStdError();
        for(int i=0;i<data.length;i++){
            assertEquals("  JMLE pcm test: ability", true_theta_std_error[i], Precision.round(theta_std_error[i], 5), 1e-4);
        }

    }

    //@Test
    public void testPoissonCounts(){
        System.out.println("JMLE POISSON COUNTS TEST");

        byte[][] data = readPoissonCountsData();
        int ni = data[0].length;
        ItemResponseModel[] irm = new ItemResponseModel[ni];
        ItemResponseModel poissonModel = null;

        int maxCount = 30;

        for(int i=0;i<ni;i++){
            poissonModel = new IrmPoissonCounts(0, maxCount, 1.0);
            irm[i] = poissonModel;
        }

        double[] winstepsResults = {0.16, -0.30, -0.50, -0.42, 0.20, 0.07, 0.35, 0.44};

        irm[0] = new IrmPoissonCounts(winstepsResults[0], maxCount, 1.0);
        irm[1] = new IrmPoissonCounts(winstepsResults[1], maxCount, 1.0);
        irm[2] = new IrmPoissonCounts(winstepsResults[2], maxCount, 1.0);
        irm[3] = new IrmPoissonCounts(winstepsResults[3], maxCount, 1.0);
        irm[4] = new IrmPoissonCounts(winstepsResults[4], maxCount, 1.0);
        irm[5] = new IrmPoissonCounts(winstepsResults[5], maxCount, 1.0);
        irm[6] = new IrmPoissonCounts(winstepsResults[6], maxCount, 1.0);
        irm[7] = new IrmPoissonCounts(winstepsResults[7], maxCount, 1.0);

        JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(data, irm);
        jmle.summarizeData(0.3);
        jmle.estimateParameters(10, 0.005);
        jmle.computeItemStandardErrors();
        jmle.computePersonStandardErrors();

//        System.out.println(jmle.printFrequencyTables());
//        System.out.println(jmle.printRatingScaleTables());
//        System.out.println();
        System.out.println(jmle.printBasicItemStats());
        System.out.println();
        System.out.println(jmle.printPersonStats());
        System.out.println();
        System.out.println(jmle.printIterationHistory());

        for(int i=0;i<irm.length;i++){
            assertEquals("  JMLE Poisson test: ability", winstepsResults[i], Precision.round(irm[i].getDifficulty(), 2), 1e-5);
        }

    }


}

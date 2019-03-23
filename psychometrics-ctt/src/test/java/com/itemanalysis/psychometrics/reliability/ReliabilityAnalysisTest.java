package com.itemanalysis.psychometrics.reliability;

import com.itemanalysis.psychometrics.statistics.StreamingCovarianceMatrix;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ReliabilityAnalysisTest {

    /**
     * True values computed by Brian Habing's R function:
     * alpha<-function(testdata){
     * n<-ncol(testdata)
     * nexmn<-nrow(testdata)
     * x<-apply(testdata,1,sum)
     * s2y<-diag(var(testdata))*(nexmn-1)/nexmn
     * s2x<-var(x)*(nexmn-1)/nexmn
     * alpha<-(n/(n-1))*(1-sum(s2y)/s2x)
     * s2yy<-(((var(testdata)-diag(diag(var(testdata))))*(nexmn-1)/nexmn))^2
     * lambda2<-1-sum(s2y)/s2x+sqrt((n/(n-1))*sum(s2yy))/s2x
     * list(alpha=alpha,lambda2=lambda2)}
     *
     */
    @Test
    public void genericTest1() {
        System.out.println("Reliability test comparing to Habing's R code");
        double[][] x = getData();
        StreamingCovarianceMatrix S = new StreamingCovarianceMatrix(50);
        for(int i=0;i<1000;i++){
            for(int j=0;j<50;j++){
                for(int k=0;k<50;k++){
                    S.increment(j, k, x[i][j], x[i][k]);
                }
            }
        }

        double[][] CV = S.value();

        CoefficientAlpha alpha = new CoefficientAlpha(CV);
        System.out.println("  Cronbach's alpha: " + alpha.value());
        assertEquals("Testing alpha", 0.902653, alpha.value(), 1e-6);//True value from from Brian Habing's R function;

        //Raju beta is same as Coefficient alpha in this condition
        RajuBeta beta = new RajuBeta(CV);
        System.out.println("  Raju's beta: " + beta.value());
        assertEquals("Testing alpha", 0.902653, beta.value(), 1e-6);//True value from from Brian Habing's R function;

        GuttmanLambda2 lambda = new GuttmanLambda2(CV);
        System.out.println("  Guttman's lambda2: " + lambda.value());
        assertEquals("Testing lambda", 0.9033415, lambda.value(), 1e-6);//True value from from Brian Habing's R function;
    }

    @Test
    public void exam1Test(){
        System.out.println("Reliability and item deleted reliability test for exam1 data comparing to SPSS");
        int nItems = 56;
        StreamingCovarianceMatrix covarianceMatrix = new StreamingCovarianceMatrix(nItems);

        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/exam1-items-scored.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double v1 = 0;
            double v2 = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int i=0;i<nItems;i++){
                    v1 = Double.parseDouble(s[i]);
                    for(int j=0;j<nItems;j++){
                        v2 = Double.parseDouble(s[j]);
                        covarianceMatrix.increment(i, j, v1, v2);
                    }
                }
            }
            br.close();

            CoefficientAlpha coefficientAlpha = new CoefficientAlpha(covarianceMatrix.value());
            System.out.println("  Cronbach's alpha for exam1: " + coefficientAlpha.value());
            assertEquals("Testing alpha for exam1", 0.910616920064902, coefficientAlpha.value(), 1e-15);//True value from SPSS

            double[] deletedReliability = coefficientAlpha.itemDeletedReliability();

            //From SPSS
            double[] trueItemTotalCorrelation = {
                    0.3117199187, 0.2619045311, 0.4111686384, 0.2387297820, 0.2947699166, 0.3493441437, 0.3206892133,
                    0.4160691362, 0.3203589081, 0.4622234782, 0.4498696760, 0.3321656951, 0.4755506109, 0.3039656075,
                    0.4454586534, 0.3023203012, 0.3393640657, 0.2998852024, 0.5286660141, 0.3373487263, 0.4076080089,
                    0.3817203227, 0.3138298587, 0.5096743284, 0.4238791880, 0.4349070806, 0.1765178191, 0.2565792669,
                    0.3101770851, 0.3070293209, 0.2168118578, 0.1841160980, 0.3952460752, 0.4074626774, 0.4996911849,
                    0.5105412483, 0.4579124514, 0.2766526450, 0.2512199850, 0.3800065307, 0.4558826443, 0.3406912799,
                    0.3823764267, 0.3348577799, 0.4637711509, 0.5050121301, 0.3874749222, 0.4102414566, 0.3402065697,
                    0.5531622244, 0.5831762003, 0.4468320547, 0.4149048917, 0.2414797663, 0.3565469153, 0.5291109486};

            //From SPSS
            double[] trueDeletedReliability = {
                    0.9097057225, 0.9102508248, 0.9089121222, 0.9104648528, 0.9098877811, 0.9093670957, 0.9096706431,
                    0.9089738573, 0.9096735112, 0.9085055777, 0.9086474766, 0.9095560615, 0.9083172516, 0.9098370392,
                    0.9084562287, 0.9098496186, 0.9094835805, 0.9098783367, 0.9076824735, 0.9095079302, 0.9088112730,
                    0.9090666230, 0.9097167628, 0.9077984126, 0.9086470110, 0.9086216484, 0.9106740260, 0.9102759760,
                    0.9097722080, 0.9097980794, 0.9106339458, 0.9108945007, 0.9089410084, 0.9088108920, 0.9079395871,
                    0.9078051772, 0.9083075200, 0.9100883322, 0.9102864974, 0.9090827841, 0.9083485661, 0.9094747117,
                    0.9090598676, 0.9095155301, 0.9082488668, 0.9078367733, 0.9090098977, 0.9087834141, 0.9094794807,
                    0.9073990866, 0.9071285086, 0.9084175355, 0.9087366245, 0.9104123128, 0.9093127951, 0.9076400828};

            System.out.println("  Cronbach's alpha exam1 item deleted test");
            for(int i=0;i<nItems;i++){
                //True value from SPSS
                assertEquals("Testing alpha item deleted for exam1", trueDeletedReliability[i], deletedReliability[i], 1e-10);
            }

            //Raju beta is same as Coefficient alpha under this condition
            RajuBeta rajuBeta = new RajuBeta(covarianceMatrix.value());
            System.out.println("  Raju's beta for exam1: " + rajuBeta.value());
            //True value from SPSS
            assertEquals("Testing beta for exam1", 0.910616920064902, rajuBeta.value(), 1e-15);

            deletedReliability = rajuBeta.itemDeletedReliability();

            System.out.println("  Raju's beta exam1 item deleted test");
            for(int i=0;i<nItems;i++){
                //True value from SPSS
                assertEquals("Testing beta item deleted for exam1", trueDeletedReliability[i], deletedReliability[i], 1e-10);
            }

            GuttmanLambda2 guttmanLambda2 = new GuttmanLambda2(covarianceMatrix.value());
            System.out.println("  Guttman's lambda for exam1: " + guttmanLambda2.value());
            //True value from SPSS
            assertEquals("Testing Guttman's lambda for exam1", 0.911941572128058, guttmanLambda2.value(), 1e-15);

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void exam1DeletedReliabilityTest(){
        System.out.println("Reliability item deleted test for exam1 data");
        int nItems = 10;
        StreamingCovarianceMatrix covarianceMatrix = new StreamingCovarianceMatrix(nItems, false);

        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/exam1-items-scored.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            double v1 = 0;
            double v2 = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int i=0;i<nItems;i++){
                    v1 = Double.parseDouble(s[i]);
                    for(int j=0;j<nItems;j++){
                        v2 = Double.parseDouble(s[j]);
                        covarianceMatrix.increment(i, j, v1, v2);
                    }
                }
            }
            br.close();

            CoefficientAlpha coefficientAlpha = new CoefficientAlpha(covarianceMatrix.value());
            GuttmanLambda2 lambda2 = new GuttmanLambda2(covarianceMatrix.value());
            FeldtGilmer fg = new FeldtGilmer(covarianceMatrix.value());
            FeldtBrennan fb = new FeldtBrennan(covarianceMatrix.value());
            RajuBeta raju = new RajuBeta(covarianceMatrix.value());

            //Test reliability estimates
            assertEquals(" Lambda2 reliability.", 0.6481, lambda2.value(), 1e-4);
            assertEquals(" Alpha reliability.", 0.6467, coefficientAlpha.value(), 1e-4);
            assertEquals(" Feldt-Gilmer reliability.",  0.6479, fg.value(), 1e-4);
            assertEquals(" Feldt-Brennan reliability.", 0.6469, fb.value(), 1e-4);
            assertEquals(" Raju reliability.", 0.6467, raju.value(), 1e-4);

            double[] lambda2Deleted = lambda2.itemDeletedReliability();
            double[] alphaDeleted = coefficientAlpha.itemDeletedReliability();
            double[] fgDeleted = fg.itemDeletedReliability();
            double[] fbDeleted = fb.itemDeletedReliability();
            double[] rajuDeleted = raju.itemDeletedReliability();

            //From older version of jmetrik (before changes to covariance and reliability classes)
            double[][] trueDeletedReliability = {
                    {0.6259, 0.6242, 0.6259, 0.6245, 0.6242},
                    {0.6420, 0.6408, 0.6419, 0.6411, 0.6408},
                    {0.6029, 0.6016, 0.6027, 0.6019, 0.6016},
                    {0.6394, 0.6380, 0.6393, 0.6382, 0.6380},
                    {0.6304, 0.6290, 0.6303, 0.6293, 0.6290},
                    {0.6162, 0.6148, 0.6162, 0.6151, 0.6148},
                    {0.6343, 0.6327, 0.6342, 0.6329, 0.6327},
                    {0.6069, 0.6055, 0.6068, 0.6056, 0.6055},
                    {0.6343, 0.6327, 0.6342, 0.6329, 0.6327},
                    {0.6041, 0.6028, 0.6039, 0.6031, 0.6028}
            };

            for(int i=0;i<nItems;i++){
                assertEquals(" Lambda2 item deleted for item " + (i+1) + ". ", trueDeletedReliability[i][0], lambda2Deleted[i], 1e-4);
                assertEquals(" Alpha item deleted for item " + (i+1) + ". ", trueDeletedReliability[i][1], alphaDeleted[i], 1e-4);
                assertEquals(" Feldt-Gilmer item deleted for item " + (i+1) + ". ", trueDeletedReliability[i][2], fgDeleted[i], 1e-4);
                assertEquals(" Feldt-Brennan item deleted for item " + (i+1) + ". ", trueDeletedReliability[i][3], fbDeleted[i], 1e-4);
                assertEquals(" Raju item deleted for item " + (i+1) + ". ", trueDeletedReliability[i][4], rajuDeleted[i], 1e-4);
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public double[][] getData(){
        double[][] x = new double[1000][50];
        try{
            File f = FileUtils.toFile(this.getClass().getResource("/testdata/scaling.txt"));
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            String[] s = null;
            int row = 0;
            br.readLine();//eliminate column names by skipping first row
            while((line=br.readLine())!=null){
                s = line.split(",");
                for(int j=0;j<50;j++){
                    x[row][j] = Double.parseDouble(s[j]);
                }
                row++;
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

    /**
     * Covariance matrix and "true" results come from Brennan and Feldt's chapter in teh 1989
     * edition of the book "Educaitonal Measurement."
     */
    @Test
    public void brennanFeldtTest(){
        System.out.println("Reliability test with Brennan-Feldt Data");
        double[][] cov = {
                {7.2, 5.6, 3.5, 5.8},
                {5.6, 6.4, 3.1, 4.1},
                {3.5, 3.1, 2.4, 3.3},
                {5.8, 4.1, 3.3, 6.2}
        };

        CoefficientAlpha alpha = new CoefficientAlpha(cov);
        GuttmanLambda2 lambda = new GuttmanLambda2(cov);
        FeldtGilmer fg = new FeldtGilmer(cov);
        FeldtBrennan fb = new FeldtBrennan(cov);

        System.out.println("  Coefficient alpha = " + alpha.value());
        System.out.println("  Guttman's lambda = " + lambda.value());
        System.out.println("  Feldt-Gilmer = " + fg.value());
        System.out.println("  Feldt-Brennan = " + fb.value());

        //True values from Brennan and Feldt's book chapter
        assertEquals("Testing Coefficient alpha Bennan-Feldt data", 0.9279, alpha.value(), 1e-4);
        assertEquals("Testing Guttman lambda Bennan-Feldt data", 0.9353, lambda.value(), 1e-4);
        assertEquals("Testing Feldt-Gilmer Bennan-Feldt data", 0.9401, fb.value(), 1e-4);
        assertEquals("Testing Feldt-Brennan Bennan-Feldt data", 0.9402, fg.value(), 1e-4);

    }


}

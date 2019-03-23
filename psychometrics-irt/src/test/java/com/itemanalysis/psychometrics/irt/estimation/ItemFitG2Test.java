package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.quadrature.NormalQuadratureRule;
import com.itemanalysis.psychometrics.histogram.Cut;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.statistics.DefaultLinearTransformation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.junit.Test;

import java.io.File;

public class ItemFitG2Test {


    @Test
    public void binaryItemsTest(){
        System.out.println("Binary items item fit test");

        int numberOfBins = 10;
        int minExpectedCount = 5;
        int nItems = 50;

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/binary-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, nItems, true);

        double[][] parscale_param = {
                {0.77134,-1.20157,0.21826},
                {0.92653,1.06962,0.13582},
                {0.7394,0.66486,0.26312},
                {1.27052,-0.68461,0.14705},
                {1.15402,-0.06561,0.27267},
                {0.85473,-1.11052,0.00000},
                {0.87896,-0.84055,0.13343},
                {0.9351,1.09924,0.28928},
                {0.80445,-1.29691,0.14567},
                {1.70099,-1.82461,0.23838},
                {0.84604,-1.8712,0.20175},
                {1.3569,1.77424,0.11301},
                {0.73857,0.17206,0.14577},
                {0.90448,-0.04446,0.16733},
                {1.06378,0.66933,0.20617},
                {1.17214,-1.69306,0.18765},
                {1.19297,-0.836,0.33433},
                {0.96405,-0.02601,0.25501},
                {1.12433,-0.1781,0.23934},
                {1.04734,0.07057,0.22328},
                {1.06249,-2.96365,0.20138},
                {0.87757,-1.38206,0.33482},
                {1.06221,-0.75069,0.18338},
                {0.77941,0.15689,0.27037},
                {0.97511,1.17067,0.18709},
                {0.8666,-1.02596,0.16801},
                {0.6142,2.87065,0.24542},
                {0.92965,-0.20217,0.11476},
                {0.90248,-0.4771,0.09884},
                {1.07959,0.19929,0.15952},
                {0.82947,-0.22282,0.15908},
                {1.00463,-0.4113,0.25502},
                {1.03297,-0.9803,0.26221},
                {0.84426,1.41334,0.15932},
                {0.95875,-1.22485,0.17769},
                {0.70148,-0.14216,0.3191},
                {1.15302,0.00717,0.1719},
                {0.89399,-0.06892,0.23587},
                {1.14904,0.92012,0.12101},
                {1.14389,2.10656,0.24905},
                {0.92095,-0.44256,0.28191},
                {0.73603,-1.99767,0.20534},
                {0.96288,0.54437,0.16837},
                {1.69054,-0.53875,0.20511},
                {0.86499,0.7092,0.13796},
                {0.66786,-0.16466,0.24747},
                {1.1017,-0.65105,0.23805},
                {1.13996,1.50206,0.11545},
                {0.90298,0.26238,0.11898},
                {0.86469,0.13259,0.10187}
        };

        //Create array of item response models using estimate parameters with PARSCALE
        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        Irm3PL pl3 = null;
        for(int j=0;j<nItems;j++) {
            pl3 = new Irm3PL(parscale_param[j][0], parscale_param[j][1], parscale_param[j][2], 1.7);
            pl3.setName(new VariableName("Item" + (j+1)));
            irm[j] = pl3;
        }

        //Estimate person ability using EAP
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(-4.0, 4.0, 40);
        double[] eap = new double[responseData.length];
        IrtExaminee irtExaminee = new IrtExaminee(irm);

        //Create summary statistics for EAP estimates. Will be needed for linear transformation.
        Mean meanEap = new Mean();
        StandardDeviation sdEap = new StandardDeviation();
        Min min = new Min();
        Max max = new Max();

        //First loop over data for computing EAP person ability values
        for(int i=0;i<responseData.length;i++){
            irtExaminee.setResponseVector(responseData[i]);
            eap[i] = irtExaminee.eapEstimate(latentDistribution);
            meanEap.increment(eap[i]);
            sdEap.increment(eap[i]);
            min.increment(eap[i]);
            max.increment(eap[i]);
        }

        //Set up linear transformation
        DefaultLinearTransformation linearTransformation = new DefaultLinearTransformation(
                meanEap.getResult(),
                latentDistribution.getMean(),
                sdEap.getResult(),
                latentDistribution.getStandardDeviation());

        //Create boundaries for grouping examinees by values of theta
        double lower = linearTransformation.transform(min.getResult())-.01;//Subtract a small number to ensure lowest theta is counted
        double upper = linearTransformation.transform(max.getResult())+.01;//Add a small number to ensure largest theta is counted
        Cut thetaCut = new Cut(lower, upper, numberOfBins);

        //Create fit statistic objects
        ItemFitG2[] itemFitG2 = new ItemFitG2[nItems];
        for(int j=0;j<nItems;j++){
            itemFitG2[j] = new ItemFitG2(irm[j], thetaCut, minExpectedCount);
        }

        //Reset incremental statistics
        meanEap.clear();
        sdEap.clear();
        min.clear();
        max.clear();

        //Second loop over response vectors
        //Increment fit statistics
        double A = linearTransformation.getScale();
        for(int i=0;i<responseData.length;i++){
            eap[i] = eap[i]*A;

            meanEap.increment(eap[i]);
            sdEap.increment(eap[i]);
            min.increment(eap[i]);
            max.increment(eap[i]);

            for(int N=0;N<responseData[i].getFrequency();N++) {//Expand table
                for(int j=0;j<nItems;j++){
                    itemFitG2[j].increment(eap[i], responseData[i].getResponseAt(j));
                }
            }
        }

        System.out.println("EAP Mean: " + meanEap.getResult());
        System.out.println("EAP SD: " + sdEap.getResult());
        System.out.println("EAP N: " + meanEap.getN());

        //Compute Item Fit
        double G2sum = 0;
        for(int j=0;j<nItems;j++){
            itemFitG2[j].compute();
            System.out.println(irm[j].getName() + "  " +
                    itemFitG2[j].getValue() + "  " +
                    itemFitG2[j].getDegreesOfFreedom() + "  " +
                    itemFitG2[j].getPValue());
            G2sum += itemFitG2[j].getValue() ;
        }
        System.out.println("Overall fit: " + G2sum);

        //NOTE: Only gets close (+- 0.001) to PARSCALE fit statistic when number of bins  = 1, and
        //the theta values are rounded to two decimal places when computing expected values.
        //I am unable to reproduce PARSCALES method for collapsing rows (or categories)
        //with low expected cell counts.



    }

}
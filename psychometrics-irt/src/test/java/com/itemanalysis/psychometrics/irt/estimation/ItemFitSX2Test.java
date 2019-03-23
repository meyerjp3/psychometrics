package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.quadrature.NormalQuadratureRule;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmGPCM;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Test cases for item fit
 */
public class ItemFitSX2Test {

    /**
     * True value obtained from mirt package in R using the following syntax.
     *
     * data <- expand.table(LSAT7)
     * coef(mod1, IRTpars = TRUE)
     * itemfit(mod1, mincell=0)
     * itemfit(mod1, S_X2.tables = TRUE, mincell=0)
     *
     */
    @Test
    public void LSAT7Test(){
        System.out.println("S-X2 fit statistics test with LSAT7 data");

        //True values from mirt in R.
        double[] sX2mirt = {4.7492191, 14.4528980, 1.2703374, 5.2373430, 0.9407036};
        double[] dfmirt = {2, 2, 2, 2, 2};

        int minExpectedCount = 0;

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/lsat7-expanded.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, true);

        //Create item response model objects with parameters estimated with the mirt R package.
        ItemResponseModel[] irm = new ItemResponseModel[5];
        irm[0] = new Irm3PL(0.988, -1.879, 1.0);
        irm[1] = new Irm3PL(1.081, -0.748, 1.0);
        irm[2] = new Irm3PL(1.706, -1.058, 1.0);
        irm[3] = new Irm3PL(0.765, -0.635, 1.0);
        irm[4] = new Irm3PL(0.736, -2.520, 1.0);

        int nItems = irm.length;
        ItemFitStatistic[] itemFit = new ItemFitSX2[nItems];

        //computation of quadrature points as done in the mirt R package
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(min, max, (int)quadPoints);

        //IRT observed score quadrature
        IrtObservedScoreCollection obsScoreCollection = new IrtObservedScoreCollection(irm, latentDistribution);
        IrtObservedScoreDistribution mainScoreDistribution = obsScoreCollection.getIrtObservedScoreDistribution();

        //Create and increment fit statistics
        int summedScore = 0;
        int maxPossibleTestScore = obsScoreCollection.getMaxPossibleTestScore();
        for(int i=0;i<responseData.length;i++){
            summedScore = (int)responseData[i].getSumScore();

            for(int j=0;j<nItems;j++){
                if(i==0) itemFit[j] = new ItemFitSX2(
                        mainScoreDistribution, obsScoreCollection.getObservedScoreDistributionAt(j),
                        irm[j], maxPossibleTestScore+1, minExpectedCount);
                ((ItemFitSX2)itemFit[j]).increment(summedScore, responseData[i].getResponseAt(j));
            }
        }




        //Compute Item Fit and test values
        for(int j=0;j<nItems;j++){
            itemFit[j].compute();
            System.out.println(itemFit[j].toString());
            assertEquals("S-X2 value"+(j+1), sX2mirt[j], itemFit[j].getValue(), 1e-2);
            assertEquals("  DF value"+(j+1), dfmirt[j], itemFit[j].getDegreesOfFreedom(), 1e-8);
        }


    }

    /**
     * True item fit values obtained using:
     *
     * itemfit(m1, mincell=0)
     *
     * note that mincell=0 prevents the collapsing of cells.
     *
     */
    @Test
    public void polytomousItemTest(){
        System.out.println("S-X2 fit statistics test with polytomous items");

        //Parameter estimates obtained with mirt package in R.
        double[][] mirtStep ={
                {0, -1.760, -0.897, -0.180},
                {0, -0.971,  0.226,  0.386},
                {0, -0.379,  0.544,  0.766},
                {0,  1.483,  1.945,  2.800},
                {0, -1.133, -0.584,  0.303},
                {0, -1.271, -0.869,  0.109},
                {0, -1.066, -0.256, -0.050},
                {0, -0.245,  0.271,  1.738},
                {0, -1.441, -0.197, -0.077},
                {0, -1.278, -0.848, -0.284}
        };
        double[] mirtDiscrim = {0.929, 0.620, 1.321, 1.264, 0.818, 0.776, 1.301, 0.593, 0.556, 1.083};

        //True values from mirt in R.
        double[] sX2mirt = {59.6389, 76.5977, 57.3867, 40.3068, 58.0271, 87.6170, 50.6894, 70.2849, 57.8301, 74.9836};
        double[] dfmirt = {71, 71, 71, 71, 71, 71, 71, 71, 71, 71};


        int minExpectedCount = 0;//Will not collapse cells
        int nItems = mirtDiscrim.length;

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/polytomous-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, nItems, false);

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        for(int j=0;j<nItems;j++){
            irm[j] = new IrmGPCM(mirtDiscrim[j], mirtStep[j], 1.0);
            irm[j].setName(new VariableName("Item"+(j+1)));
        }

        ItemFitStatistic[] itemFit = new ItemFitSX2[nItems];

        //computation of quadrature points as done in the mirt R package
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(min, max, (int)quadPoints);

        //IRT observed score quadrature
        IrtObservedScoreCollection obsScoreCollection = new IrtObservedScoreCollection(irm, latentDistribution);
        IrtObservedScoreDistribution mainScoreDistribution = obsScoreCollection.getIrtObservedScoreDistribution();

//        System.out.println(obsScoreCollection.getObservedScoreDistributionAt(0).toString());

        //Create and increment fit statistics
        int summedScore = 0;
        int maxPossibleTestScore = obsScoreCollection.getMaxPossibleTestScore();

        for(int i=0;i<responseData.length;i++){
            summedScore = (int)responseData[i].getSumScore();

            for(int j=0;j<nItems;j++){
                if(i==0) itemFit[j] = new ItemFitSX2(
                        mainScoreDistribution, obsScoreCollection.getObservedScoreDistributionAt(j),
                        irm[j], maxPossibleTestScore+1, minExpectedCount);
                ((ItemFitSX2)itemFit[j]).increment(summedScore, responseData[i].getResponseAt(j));
            }
        }

        //Compute Item Fit and test values
        for(int j=0;j<nItems;j++){
            itemFit[j].compute();
            System.out.println(itemFit[j].toString());
            assertEquals("S-X2 value " +(j+1), sX2mirt[j], itemFit[j].getValue(), 1e-1);
            assertEquals("  DF value"+(j+1), dfmirt[j], itemFit[j].getDegreesOfFreedom(), 1e-8);
        }

    }

    /**
     * No way to obtain true values from a separate program.
     */
    @Test
    public void polytomousItemTest2(){
        System.out.println("S-X2 fit statistics test with polytomous items: average cell count >= 1");

        //Parameter estimates obtained with mirt package in R.
        double[][] mirtStep ={
                {0, -1.760, -0.897, -0.180},
                {0, -0.971,  0.226,  0.386},
                {0, -0.379,  0.544,  0.766},
                {0,  1.483,  1.945,  2.800},
                {0, -1.133, -0.584,  0.303},
                {0, -1.271, -0.869,  0.109},
                {0, -1.066, -0.256, -0.050},
                {0, -0.245,  0.271,  1.738},
                {0, -1.441, -0.197, -0.077},
                {0, -1.278, -0.848, -0.284}
        };
        double[] mirtDiscrim = {0.929, 0.620, 1.321, 1.264, 0.818, 0.776, 1.301, 0.593, 0.556, 1.083};

        //True values from mirt in R.
        double[] sX2mirt = {59.6389, 76.5977, 57.3867, 40.3068, 58.0271, 87.6170, 50.6894, 70.2849, 57.8301, 74.9836};
        double[] dfmirt = {71, 71, 71, 71, 71, 71, 71, 71, 71, 71};


        int minExpectedCount = 1;//Average expected value should be greater than or equal to 1
        int nItems = mirtDiscrim.length;

        //Read file and create response vectors
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource("/testdata/polytomous-items.txt"));
        ItemResponseVector[] responseData = fileSummary.getResponseVectors(f, 1, nItems, false);

        ItemResponseModel[] irm = new ItemResponseModel[nItems];
        for(int j=0;j<nItems;j++){
            irm[j] = new IrmGPCM(mirtDiscrim[j], mirtStep[j], 1.0);
            irm[j].setName(new VariableName("Item"+(j+1)));
        }

        ItemFitStatistic[] itemFit = new ItemFitSX2[nItems];

        //computation of quadrature points as done in the mirt R package
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1*min;
        NormalQuadratureRule latentDistribution = new NormalQuadratureRule(min, max, (int)quadPoints);

        //IRT observed score quadrature
        IrtObservedScoreCollection obsScoreCollection = new IrtObservedScoreCollection(irm, latentDistribution);
        IrtObservedScoreDistribution mainScoreDistribution = obsScoreCollection.getIrtObservedScoreDistribution();

//        System.out.println(obsScoreCollection.getObservedScoreDistributionAt(0).toString());

        //Create and increment fit statistics
        int summedScore = 0;
        int maxPossibleTestScore = obsScoreCollection.getMaxPossibleTestScore();

        for(int i=0;i<responseData.length;i++){
            summedScore = (int)responseData[i].getSumScore();

            for(int j=0;j<nItems;j++){
                if(i==0) itemFit[j] = new ItemFitSX2(
                        mainScoreDistribution, obsScoreCollection.getObservedScoreDistributionAt(j),
                        irm[j], maxPossibleTestScore+1, minExpectedCount);
                ((ItemFitSX2)itemFit[j]).increment(summedScore, responseData[i].getResponseAt(j));
            }
        }

        //Compute Item Fit and test values
        for(int j=0;j<nItems;j++){
            itemFit[j].compute();
            System.out.println(itemFit[j].toString());
        }

    }

}
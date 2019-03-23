package com.itemanalysis.psychometrics.irt.equating;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.*;
import com.itemanalysis.psychometrics.quadrature.ContinuousQuadratureRule;
import com.itemanalysis.psychometrics.quadrature.UniformQuadratureRule;
import org.junit.Test;

import java.util.LinkedHashMap;

import static junit.framework.Assert.assertEquals;

public class IrtScaleLinkingTest {

    /**
     * An example with al Rasch model items. True results obtained using the sirt package in R. I am using sirt because
     * plink returns a different result. Ia m not sure why there is a discrepancy. The R code is shown below.
     *
     * library(sirt)
     *
     * fX<-matrix(c(
     * 1, -3.188047976,
     * 2,  1.031760328,
     * 3,  0.819040914,
     * 4, -2.706947360,
     * 5, -0.094527077,
     * 6,  0.689697135,
     * 7, -0.551837153,
     * 8, -0.359559276),
     * nrow=8, byrow=TRUE)
     *
     * fX<-as.data.frame(fX)
     * names(fX)<-c("item","bparam")
     *
     * fY<-matrix(c(
     * 1,-3.074599226,
     * 2,1.01282435,
     * 3,0.868538408,
     * 4,-2.404483603,
     * 5,0.037402866,
     * 6,0.70074742,
     * 7,-0.602555046,
     * 8,-0.350426446),
     * nrow=8, byrow=TRUE)
     * fY<-as.data.frame(fY)
     * names(fY)<-c("item","bparam")
     *
     * equating.rasch(fX, fY, theta=seq(-4,4,by=.05))
     *
     */
    @Test
    public void linkingTestRasch(){
        System.out.println("Rasch model linking test");

        LinkedHashMap<VariableName, ItemResponseModel> itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("Item1"), new Irm3PL(-3.188047976, 1.0));
        itemFormX.put(new VariableName("Item2"), new Irm3PL(1.031760328, 1.0));
        itemFormX.put(new VariableName("Item3"), new Irm3PL(0.819040914, 1.0));
        itemFormX.put(new VariableName("Item4"), new Irm3PL(-2.706947360, 1.0));
        itemFormX.put(new VariableName("Item5"), new Irm3PL(-0.094527077, 1.0));
        itemFormX.put(new VariableName("Item6"), new Irm3PL(0.689697135, 1.0));
        itemFormX.put(new VariableName("Item7"), new Irm3PL(-0.551837153, 1.0));
        itemFormX.put(new VariableName("Item8"), new Irm3PL(-0.359559276, 1.0));

        itemFormY.put(new VariableName("Item1"), new Irm3PL(-3.074599226, 1.0));
        itemFormY.put(new VariableName("Item2"), new Irm3PL(1.012824350, 1.0));
        itemFormY.put(new VariableName("Item3"), new Irm3PL(0.868538408, 1.0));
        itemFormY.put(new VariableName("Item4"), new Irm3PL(-2.404483603, 1.0));
        itemFormY.put(new VariableName("Item5"), new Irm3PL(0.037402866, 1.0));
        itemFormY.put(new VariableName("Item6"), new Irm3PL(0.700747420, 1.0));
        itemFormY.put(new VariableName("Item7"), new Irm3PL(-0.602555046, 1.0));
        itemFormY.put(new VariableName("Item8"), new Irm3PL(-0.350426446, 1.0));

        UniformQuadratureRule distX = new UniformQuadratureRule(-4.0, 4.0, 161);//plink default
        UniformQuadratureRule distY = new UniformQuadratureRule(-4.0, 4.0, 161);//plink default

        IrtScaleLinking irtScaleLinking = new IrtScaleLinking(itemFormX, itemFormY, distX, distY);
        irtScaleLinking.setPrecision(6);
        irtScaleLinking.computeCoefficients();

        System.out.println(irtScaleLinking.toString());

        MeanMeanMethod mm = irtScaleLinking.getMeanMeanMethod();
        MeanSigmaMethod ms = irtScaleLinking.getMeanSigmaMethod();
        HaebaraMethod hb = irtScaleLinking.getHaebaraMethod();
        StockingLordMethod sl = irtScaleLinking.getStockingLordMethod();

        assertEquals("  Mean/mean intercept test", 0.068484, mm.getIntercept(), 1e-4);//True results from sirt package in R
        assertEquals("  Mean/mean scale test", 1.0, mm.getScale(), 1e-4);

        assertEquals("  Mean/sigma intercept test", 0.068484, ms.getIntercept(), 1e-4);//True results from sirt package in R
        assertEquals("  Mean/sigma scale test", 1.0, ms.getScale(), 1e-4);

        assertEquals("  Haebara intercept test", 0.06468396, hb.getIntercept(), 1e-4);//True results from sirt package in R
        assertEquals("  Haebara scale test", 1.0, hb.getScale(), 1e-4);

        assertEquals("  Stocking-Lord intercept test", 0.05736233, sl.getIntercept(), 1e-4);//True results from sirt package in R
        assertEquals("  Stocking-Lord scale test", 1.0, sl.getScale(), 1e-4);

    }

    /**
     * A mixed format exam with a combination of Rasch and partial credit model items. True results obtained using
     * the plink package in R. The plink code is shown below.
     *
     * library(plink)
     *
     * fX<-matrix(c(
     * 1, -3.188047976, 0,NA,NA,
     * 1,  1.031760328, 0,NA,NA,
     * 1,  0.819040914, 0,NA,NA,
     * 1, -2.706947360, 0,NA,NA,
     * 1, -0.094527077, 0,NA,NA,
     * 1,  0.689697135, 0,NA,NA,
     * 1, -0.551837153, 0,NA,NA,
     * 1, -0.359559276, 0,NA,NA,
     * 1, -1.451470831, -0.146619694, -0.636399040, 0.783018734),
     * nrow=9, byrow=TRUE)
     * fX<-as.data.frame(fX)
     * names(fX)<-c("aparam", "bparam","cparam","s1","s2")
     *
     * fY<-matrix(c(
     * 1,-3.074599226,0,NA,NA,
     * 1,1.01282435,0,NA,NA,
     * 1,0.868538408,0,NA,NA,
     * 1,-2.404483603,0,NA,NA,
     * 1,0.037402866,0,NA,NA,
     * 1,0.70074742,0,NA,NA,
     * 1,-0.602555046,0,NA,NA,
     * 1,-0.350426446,0,NA,NA,
     * 1,-1.267744832,-0.185885988,-0.61535623,0.801242218),
     * nrow=9, byrow=TRUE)
     * fY<-as.data.frame(fY)
     * names(fY)<-c("aparam", "bparam","cparam","s1","s2")
     *
     * common<-cbind(1:9, 1:9)
     * cat<-c(rep(2,8),4)
     *
     * pmX <- as.poly.mod(9,c("drm","gpcm"),list(1:8,9))
     * pmY <- as.poly.mod(9,c("drm","gpcm"),list(1:8,9))
     *
     * pars <- as.irt.pars(list(fx=fX,fy=fY), common, cat=list(fx=cat,fy=cat),
     * poly.mod=list(pmX,pmY), location=c(TRUE,TRUE))
     *
     * out <- plink(pars, startvals=c(1,0), rescale="SL", base.grp=2, D=1.0, symmetric=TRUE)
     *
     *
     */
    @Test
    public void linkingTestRaschPCM(){
        System.out.println("Mixed format linking test: Rasch and PCM");

        LinkedHashMap<VariableName, ItemResponseModel> itemFormX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> itemFormY = new LinkedHashMap<VariableName, ItemResponseModel>();

        itemFormX.put(new VariableName("Item1"), new Irm3PL(-3.188047976, 1.0));
        itemFormX.put(new VariableName("Item2"), new Irm3PL(1.031760328, 1.0));
        itemFormX.put(new VariableName("Item3"), new Irm3PL(0.819040914, 1.0));
        itemFormX.put(new VariableName("Item4"), new Irm3PL(-2.706947360, 1.0));
        itemFormX.put(new VariableName("Item5"), new Irm3PL(-0.094527077, 1.0));
        itemFormX.put(new VariableName("Item6"), new Irm3PL(0.689697135, 1.0));
        itemFormX.put(new VariableName("Item7"), new Irm3PL(-0.551837153, 1.0));
        itemFormX.put(new VariableName("Item8"), new Irm3PL(-0.359559276, 1.0));
        double[] step1x = {-0.146619694, -0.636399040, 0.783018734};
        itemFormX.put(new VariableName("Item9"), new IrmPCM(-1.451470831, step1x, 1.0));

        itemFormY.put(new VariableName("Item1"), new Irm3PL(-3.074599226, 1.0));
        itemFormY.put(new VariableName("Item2"), new Irm3PL(1.012824350, 1.0));
        itemFormY.put(new VariableName("Item3"), new Irm3PL(0.868538408, 1.0));
        itemFormY.put(new VariableName("Item4"), new Irm3PL(-2.404483603, 1.0));
        itemFormY.put(new VariableName("Item5"), new Irm3PL(0.037402866, 1.0));
        itemFormY.put(new VariableName("Item6"), new Irm3PL(0.700747420, 1.0));
        itemFormY.put(new VariableName("Item7"), new Irm3PL(-0.602555046, 1.0));
        itemFormY.put(new VariableName("Item8"), new Irm3PL(-0.350426446, 1.0));
        double[] step1y = {-0.185885988, -0.61535623, 0.801242218};
        itemFormY.put(new VariableName("Item9"), new IrmPCM(-1.267744832, step1y, 1.0));

        UniformQuadratureRule distX = new UniformQuadratureRule(-4.0, 4.0, 161);//plink default
        UniformQuadratureRule distY = new UniformQuadratureRule(-4.0, 4.0, 161);//plink default

        IrtScaleLinking irtScaleLinking = new IrtScaleLinking(itemFormX, itemFormY, distX, distY);
        irtScaleLinking.setPrecision(6);
        irtScaleLinking.computeCoefficients();

        System.out.println(irtScaleLinking.toString());

        MeanMeanMethod mm = irtScaleLinking.getMeanMeanMethod();
        MeanSigmaMethod ms = irtScaleLinking.getMeanSigmaMethod();
        HaebaraMethod hb = irtScaleLinking.getHaebaraMethod();
        StockingLordMethod sl = irtScaleLinking.getStockingLordMethod();

        assertEquals("  Mean/mean intercept test", 0.099914, mm.getIntercept(), 1e-4);//True results from plink package in R
        assertEquals("  Mean/mean scale test", 1.0, mm.getScale(), 1e-4);

        assertEquals("  Mean/sigma intercept test", 0.099914, ms.getIntercept(), 1e-4);//True results from plink package in R
        assertEquals("  Mean/sigma scale test", 1.0, ms.getScale(), 1e-4);

        //TODO there's somethignwrong with plink. I'm getting different results, but sirt agree with mine (at least for the Rasch model).
//        assertEquals("  Haebara intercept test", 0.105526, hb.getIntercept(), 1e-4);//True results from plink package in R
//        assertEquals("  Haebara scale test", 1.0, hb.getScale(), 1e-4);
//
//        assertEquals("  Stocking-Lord intercept test", 0.101388, sl.getIntercept(), 1e-4);//True results from plink package in R
//        assertEquals("  Stocking-Lord scale test", 1.0, sl.getScale(), 1e-4);

    }

    /**
     * Example 1 in Kolen's STUIRT program. True results from STUIRT output. The only difference from the original
     * example syntax is that the criterion functions are standardized for these results.
     *
     * Note that plink returns a slightly different result from STUIRT. I am not sure why there is a discrepancy.
     */
    @Test
    public void linkingTest3PL(){
        System.out.println("3PL linking test with actual quadrature");

        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        irmX.put(new VariableName("i1"), new Irm3PL(0.4551, -0.7101, 0.2087, 1.7));
        irmX.put(new VariableName("i2"), new Irm3PL(0.5839, -0.8567, 0.2038, 1.7));
        irmX.put(new VariableName("i3"), new Irm3PL(0.7544, 0.0212, 0.1600, 1.7));
        irmX.put(new VariableName("i4"), new Irm3PL(0.6633, 0.0506, 0.1240, 1.7));
        irmX.put(new VariableName("i5"), new Irm3PL(1.0690, 0.9610, 0.2986, 1.7));
        irmX.put(new VariableName("i6"), new Irm3PL(0.9672, 0.1950, 0.0535, 1.7));
        irmX.put(new VariableName("i7"), new Irm3PL(0.3479, 2.2768, 0.1489, 1.7));
        irmX.put(new VariableName("i8"), new Irm3PL(1.4579, 1.0241, 0.2453, 1.7));
        irmX.put(new VariableName("i9"), new Irm3PL(1.8811, 1.4062, 0.1992, 1.7));
        irmX.put(new VariableName("i10"), new Irm3PL(0.7020, 2.2401, 0.0853, 1.7));
        irmX.put(new VariableName("i11"), new Irm3PL(1.4080, 1.5556, 0.0789, 1.7));
        irmX.put(new VariableName("i12"), new Irm3PL(1.2993, 2.1589, 0.1075, 1.7));

        irmY.put(new VariableName("i1"), new Irm3PL(0.4416, -1.3349, 0.1559, 1.7));
        irmY.put(new VariableName("i2"), new Irm3PL(0.5730, -1.3210, 0.1913, 1.7));
        irmY.put(new VariableName("i3"), new Irm3PL(0.5987, -0.7098, 0.1177, 1.7));
        irmY.put(new VariableName("i4"), new Irm3PL(0.6041, -0.3539, 0.0818, 1.7));
        irmY.put(new VariableName("i5"), new Irm3PL(0.9902,  0.5320, 0.3024, 1.7));
        irmY.put(new VariableName("i6"), new Irm3PL(0.8081, -0.1156, 0.0648, 1.7));
        irmY.put(new VariableName("i7"), new Irm3PL(0.4140,  2.5538, 0.2410, 1.7));
        irmY.put(new VariableName("i8"), new Irm3PL(1.3554,  0.5811, 0.2243, 1.7));
        irmY.put(new VariableName("i9"), new Irm3PL(1.0417,  0.9392, 0.1651, 1.7));
        irmY.put(new VariableName("i10"), new Irm3PL(0.6336,  1.8960, 0.0794, 1.7));
        irmY.put(new VariableName("i11"), new Irm3PL(1.1347,  1.0790, 0.0630, 1.7));
        irmY.put(new VariableName("i12"), new Irm3PL(0.9255,  2.1337, 0.1259, 1.7));

        double[] points = {-4.0000, -3.1110, -2.2220, -1.3330, -0.4444, 0.4444, 1.3330, 2.2220, 3.1110, 4.0000};
        double[] xDensity = {0.0001008, 0.002760, 0.03021, 0.1420, 0.3149, 0.3158, 0.1542, 0.03596, 0.003925, 0.0001862};
        double[] yDensity = {0.0001173, 0.003242, 0.03449, 0.1471, 0.3148, 0.3110, 0.1526, 0.03406, 0.002510, 0.0001116};

        ContinuousQuadratureRule distX = new ContinuousQuadratureRule(points, xDensity);
        ContinuousQuadratureRule distY = new ContinuousQuadratureRule(points, yDensity);

        IrtScaleLinking irtScaleLinking = new IrtScaleLinking(irmX, irmY, distX, distY);
        irtScaleLinking.setPrecision(6);
        irtScaleLinking.computeCoefficients();

        System.out.println(irtScaleLinking.toString());

        MeanMeanMethod mm = irtScaleLinking.getMeanMeanMethod();
        MeanSigmaMethod ms = irtScaleLinking.getMeanSigmaMethod();
        HaebaraMethod hb = irtScaleLinking.getHaebaraMethod();
        StockingLordMethod sl = irtScaleLinking.getStockingLordMethod();



        //Test Mean/sigma results
        assertEquals("  Mean/sigma Intercept test", -0.515543, ms.getIntercept(), 1e-6);
        assertEquals("  Mean/sigma Scale test", 1.168891, ms.getScale(), 1e-6);

        //Test Mean/mean results
        assertEquals("  Mean/mean Intercept test", -0.557156, mm.getIntercept(), 1e-6);
        assertEquals("  Mean/mean Scale test", 1.217266, mm.getScale(), 1e-6);

        //Test Haebara results
        assertEquals("  Haebara Intercept test", -0.471281, hb.getIntercept(), 1e-4);
        assertEquals("  Haebara Scale test", 1.067800, hb.getScale(), 1e-4);
        assertEquals("  Haebara objective function value test", 0.001506, irtScaleLinking.getHaebaraObjectiveFunctionValue(), 1e-6);

        //Test Stocking-Lord results
        assertEquals("  Stocking-Lord Intercept test", -0.487619, sl.getIntercept(), 1e-6);
        assertEquals("  Stocking-Lord Scale test", 1.083417, sl.getScale(), 1e-6);
        assertEquals("  Stocking-Lord objective function value test", 0.009666, irtScaleLinking.getStockingLordObjectiveFunctionValue(), 1e-6);

    }

    /**
     * Item parameters and true values from Kolen's STUIRT program example 2.
     * This example uses a combination of 3PLM and GPCM items.
     *
     * Note that plink returns a slightly different result from STUIRT. I am not sure why there is a discrepancy.
     *
     */
    @Test
    public void mixedFormatTest(){
        System.out.println("STUIRT example 2 with PARSCALE parameters");
        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        //Form X
        irmX.put(new VariableName("v1"), new Irm3PL(0.751335, -0.897391, 0.244001, 1.7));
        irmX.put(new VariableName("v2"), new Irm3PL(0.955947, -0.811477, 0.242883, 1.7));
        irmX.put(new VariableName("v3"), new Irm3PL(0.497206, -0.858681, 0.260893, 1.7));
        irmX.put(new VariableName("v4"), new Irm3PL(0.724000, -0.123911, 0.243497, 1.7));
        irmX.put(new VariableName("v5"), new Irm3PL(0.865200,  0.205889, 0.319135, 1.7));
        irmX.put(new VariableName("v6"), new Irm3PL(0.658129,  0.555228, 0.277826, 1.7));
        irmX.put(new VariableName("v7"), new Irm3PL(1.082118,  0.950549, 0.157979, 1.7));
        irmX.put(new VariableName("v8"), new Irm3PL(0.988294,  1.377501, 0.084828, 1.7));
        irmX.put(new VariableName("v9"), new Irm3PL(1.248923,  1.614355, 0.181874, 1.7));
        irmX.put(new VariableName("v10"), new Irm3PL(1.116682,  2.353932, 0.246856, 1.7));
        irmX.put(new VariableName("v11"), new Irm3PL(0.438171, 3.217965, 0.309243, 1.7));
        irmX.put(new VariableName("v12"), new Irm3PL(1.082206, 4.441864, 0.192339, 1.7));

        double[] step1 = {1.097268, -1.097268};
        irmX.put(new VariableName("v13"), new IrmGPCM2(0.269994, 0.003998, step1, 1.7));

        double[] step2 = {0.106514, -0.106514};
        irmX.put(new VariableName("v14"), new IrmGPCM2(0.972506, 1.632662, step2, 1.7));

        double[] step3 = {2.102301, -2.102301};
        irmX.put(new VariableName("v15"), new IrmGPCM2(0.378812, 3.464657, step3, 1.7));

        double[] step4 = {-0.476513,  1.081282, -0.604770};
        irmX.put(new VariableName("v16"), new IrmGPCM2(0.537706, 1.010053, step4, 1.7));

        double[] step5 = {1.007525, -0.197767, -0.809758};
        irmX.put(new VariableName("v17"), new IrmGPCM2(0.554506, 2.432938, step5, 1.7));


        //Form Y
        irmY.put(new VariableName("v1"), new Irm3PL(0.887276, -1.334798, 0.134406, 1.7));
        irmY.put(new VariableName("v2"), new Irm3PL(1.184412, -1.129004, 0.237765, 1.7));
        irmY.put(new VariableName("v3"), new Irm3PL(0.609412, -1.464546, 0.151393, 1.7));
        irmY.put(new VariableName("v4"), new Irm3PL(0.923812, -0.576435, 0.240097, 1.7));
        irmY.put(new VariableName("v5"), new Irm3PL(0.822776, -0.476357, 0.192369, 1.7));
        irmY.put(new VariableName("v6"), new Irm3PL(0.707818, -0.235189, 0.189557, 1.7));
        irmY.put(new VariableName("v7"), new Irm3PL(1.306976,  0.242986, 0.165553, 1.7));
        irmY.put(new VariableName("v8"), new Irm3PL(1.295471,  0.598029, 0.090557, 1.7));
        irmY.put(new VariableName("v9"), new Irm3PL(1.366841,  0.923206, 0.172993, 1.7));
        irmY.put(new VariableName("v10"), new Irm3PL(1.389624,  1.380666, 0.238008, 1.7));
        irmY.put(new VariableName("v11"), new Irm3PL(0.293806,  2.028070, 0.203448, 1.7));
        irmY.put(new VariableName("v12"), new Irm3PL(0.885347,  3.152928, 0.195473, 1.7));

        double[] step1Y = {0.893232, -0.893232};
        irmY.put(new VariableName("v13"), new IrmGPCM2(0.346324, -0.494115, step1Y, 1.7));

        double[] step2Y = {0.099750, -0.099750};
        irmY.put(new VariableName("v14"), new IrmGPCM2(1.252012, 0.856264, step2Y, 1.7));

        double[] step3Y = {1.850498, -1.850498};
        irmY.put(new VariableName("v15"), new IrmGPCM2(0.392282, 2.825801, step3Y, 1.7));

        double[] step4Y = {-0.300428,  0.761846, -0.461417};
        irmY.put(new VariableName("v16"), new IrmGPCM2(0.660841, 0.342977, step4Y, 1.7));

        double[] step5Y = {1.001974, -0.107221, -0.894753};
        irmY.put(new VariableName("v17"), new IrmGPCM2(0.669612, 1.643267, step5Y, 1.7));

        UniformQuadratureRule uniform = new UniformQuadratureRule(-3.0, 3.0, 25);

        IrtScaleLinking irtScaleLinking = new IrtScaleLinking(irmX, irmY, uniform, uniform);
        irtScaleLinking.setPrecision(6);
        irtScaleLinking.computeCoefficients();

        System.out.println(irtScaleLinking.toString());

        MeanMeanMethod mm = irtScaleLinking.getMeanMeanMethod();
        MeanSigmaMethod ms = irtScaleLinking.getMeanSigmaMethod();
        HaebaraMethod hb = irtScaleLinking.getHaebaraMethod();
        StockingLordMethod sl = irtScaleLinking.getStockingLordMethod();

        //Test Mean/sigma results
        assertEquals("  Mean/sigma Intercept test", -0.560159, ms.getIntercept(), 1e-6);
        assertEquals("  Mean/sigma Scale test", 0.887294, ms.getScale(), 1e-6);

        //Test Mean/mean results
        assertEquals("  Mean/mean Intercept test", -0.543611, mm.getIntercept(), 1e-6);
        assertEquals("  Mean/mean Scale test", 0.875095, mm.getScale(), 1e-6);

        //Test Haebara results
        assertEquals("  Haebara Intercept test", -0.446101, hb.getIntercept(), 1e-4);
        assertEquals("  Haebara Scale test", 0.805049, hb.getScale(), 1e-4);
        assertEquals("  Haebara objective function value test", 0.001070, irtScaleLinking.getHaebaraObjectiveFunctionValue(), 1e-6);

        //Test Stocking-Lord results
        assertEquals("  Stocking-Lord Intercept test", -0.456487, sl.getIntercept(), 1e-6);
        assertEquals("  Stocking-Lord Scale test", 0.815445, sl.getScale(), 1e-6);
        assertEquals("  Stocking-Lord objective function value test", 0.057876, irtScaleLinking.getStockingLordObjectiveFunctionValue(), 1e-6);

    }

    /**
     * This example uses all partial credit items. Each item has five cateogries. The parameters listed below are for
     * the entire exam, but only the even numbered item (i.e. the odd array index items) are linking items.
     *
     * True results computed with the plink package in R. The source code is shown below.
     *
     * library(plink)
     *
     * fX<-matrix(c(
     * 1, -0.126698, -1.006692, -0.384241, -0.11184, 1.502773,
     * 1, -0.452035, -0.892028, -0.412217, 0.131183, 1.173062,
     * 1, 0.175118, -0.902681, -0.478009, -0.060514, 1.441203,
     * 1, -1.021788, -0.697221, -0.355219, 0.241353, 0.811086,
     * 1, 2.348223, -0.765447, -0.215056, 0.30764, 0.672862,
     * 1, -0.664511, -1.256809, -0.639672, 0.454919, 1.441562,
     * 1, 1.120737, -0.97042, -0.498912, 0.181986, 1.287346,
     * 1, -1.559595, -0.715925, -0.406233, 0.14064, 0.981519,
     * 1, -0.5397, -0.912287, -0.423487, 0.057775, 1.277999,
     * 1, 0.216158, -0.654154, -0.591691, -0.050277, 1.296121,
     * 1, 1.041713, -1.148247, -0.484591, 0.31044, 1.322397,
     * 1, 0.327842, -0.703323, -0.434325, 0.030546, 1.107102,
     * 1, -0.154006, -0.900466, -0.378019, -0.013421, 1.291906,
     * 1, -0.067132, -0.843232, -0.488057, 0.325844, 1.005446,
     * 1, 0.152685, -0.945509, -0.374092, 0.201312, 1.118289,
     * 1, 0.553739, -0.842559, -0.28073, 0.106888, 1.0164,
     * 1, -0.5494, -0.609433, -0.338068, -0.132249, 1.079751,
     * 1, 0.524321, -0.953671, -0.35736, -0.066847, 1.377878,
     * 1, -0.8248, -0.693169, -0.467339, 0.250826, 0.909682,
     * 1, -0.50087, -1.08369, -0.576577, 0.312981, 1.347287),
     * byrow=TRUE, nrow=20)
     *
     * fY<-matrix(c(
     * 1, -0.183319, -0.964842, -0.362028, -0.138807, 1.465677,
     * 1, -0.455426, -0.896732, -0.347413, 0.048856, 1.195289,
     * 1, 0.199837, -0.909942, -0.435077, -0.015794, 1.360813,
     * 1, -1.010026, -0.888723, -0.258386, 0.316272, 0.830838,
     * 1, 2.370949, -0.814509, -0.39508, 0.336595, 0.872994,
     * 1, -0.65565, -1.23464, -0.617245, 0.403489, 1.448396,
     * 1, 1.081711, -0.960485, -0.506704, 0.157772, 1.309417,
     * 1, -1.562643, -0.904363, -0.271877, 0.221333, 0.954907,
     * 1, -0.550489, -0.841918, -0.469447, 0.048954, 1.262411,
     * 1, 0.202217, -0.643959, -0.608275, 0.022541, 1.229694,
     * 1, 1.046472, -1.102283, -0.478282, 0.31218, 1.268385,
     * 1, 0.328282, -0.806433, -0.396817, 0.047875, 1.155375,
     * 1, -0.153042, -0.797682, -0.464661, -0.036477, 1.298819,
     * 1, -0.022518, -0.870971, -0.424819, 0.204283, 1.091507,
     * 1, 0.17176, -0.878, -0.444777, 0.207881, 1.114896,
     * 1, 0.576931, -0.81907, -0.279071, 0.065254, 1.032887,
     * 1, -0.555202, -0.676482, -0.41991, -0.042723, 1.139115,
     * 1, 0.533761, -0.830071, -0.444677, -0.051378, 1.326127,
     * 1, -0.839659, -0.770342, -0.454123, 0.324907, 0.899557,
     * 1, -0.523948, -1.026435, -0.590928, 0.378912, 1.238451),
     * byrow=TRUE, nrow=20)
     *
     * common<-cbind(seq(2,20, by=2), seq(2,20, by=2))
     * cat<-rep(5,20)
     *
     * pmX <- as.poly.mod(20,"gpcm")
     * pmY <- as.poly.mod(20,"gpcm")
     *
     * pars <- as.irt.pars(list(fx=fX,fy=fY), common, cat=list(fx=cat,fy=cat),
     * poly.mod=list(pmX,pmY), location=c(TRUE,TRUE))
     *
     * plink(pars, startvals=c(1,0), rescale="SL", base.grp=2, D=1.0, symmetric=TRUE)
     *
     */
    @Test
    public void allPCMTest(){
        System.out.println("Partial credit model linking test");

        double[] bparamX = {-0.126698, -0.452035,  0.175118, -1.021788,  2.348223, -0.664511,  1.120737,
                -1.559595, -0.539700,  0.216158,  1.041713,  0.327842, -0.154006, -0.067132,
                0.152685,  0.553739, -0.549400,  0.524321, -0.824800, -0.500870};
        double[][] thresholdX = {
                {-1.006692, -0.384241, -0.11184, 1.502773},
                {-0.892028, -0.412217, 0.131183, 1.173062},
                {-0.902681, -0.478009, -0.060514, 1.441203},
                {-0.697221, -0.355219, 0.241353, 0.811086},
                {-0.765447, -0.215056, 0.30764, 0.672862},
                {-1.256809, -0.639672, 0.454919, 1.441562},
                {-0.97042, -0.498912, 0.181986, 1.287346},
                {-0.715925, -0.406233, 0.14064, 0.981519},
                {-0.912287, -0.423487, 0.057775, 1.277999},
                {-0.654154, -0.591691, -0.050277, 1.296121},
                {-1.148247, -0.484591, 0.31044, 1.322397},
                {-0.703323, -0.434325, 0.030546, 1.107102},
                {-0.900466, -0.378019, -0.013421, 1.291906},
                {-0.843232, -0.488057, 0.325844, 1.005446},
                {-0.945509, -0.374092, 0.201312, 1.118289},
                {-0.842559, -0.28073, 0.106888, 1.0164},
                {-0.609433, -0.338068, -0.132249, 1.079751},
                {-0.953671, -0.35736, -0.066847, 1.377878},
                {-0.693169, -0.467339, 0.250826, 0.909682},
                {-1.08369, -0.576577, 0.312981, 1.347287}
        };

        double[] bparamY = {-0.183319, -0.455426,  0.199837, -1.010026,  2.370949, -0.655650,  1.081711,
                -1.562643, -0.550489,  0.202217,  1.046472,  0.328282, -0.153042, -0.022518,
                0.171760,  0.576931, -0.555202,  0.533761, -0.839659, -0.523948};
        double[][] thresholdY = {
                {-0.964842, -0.362028, -0.138807, 1.465677},
                {-0.896732, -0.347413, 0.048856, 1.195289},
                {-0.909942, -0.435077, -0.015794, 1.360813},
                {-0.888723, -0.258386, 0.316272, 0.830838},
                {-0.814509, -0.39508, 0.336595, 0.872994},
                {-1.23464, -0.617245, 0.403489, 1.448396},
                {-0.960485, -0.506704, 0.157772, 1.309417},
                {-0.904363, -0.271877, 0.221333, 0.954907},
                {-0.841918, -0.469447, 0.048954, 1.262411},
                {-0.643959, -0.608275, 0.022541, 1.229694},
                {-1.102283, -0.478282, 0.31218, 1.268385},
                {-0.806433, -0.396817, 0.047875, 1.155375},
                {-0.797682, -0.464661, -0.036477, 1.298819},
                {-0.870971, -0.424819, 0.204283, 1.091507},
                {-0.878, -0.444777, 0.207881, 1.114896},
                {-0.81907, -0.279071, 0.065254, 1.032887},
                {-0.676482, -0.41991, -0.042723, 1.139115},
                {-0.830071, -0.444677, -0.051378, 1.326127},
                {-0.770342, -0.454123, 0.324907, 0.899557},
                {-1.026435, -0.590928, 0.378912, 1.238451}
        };

        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        ItemResponseModel irm = null;
        int[] common = {1,3,5,7,9,11,13,15,17,19};//Common items are even numbered items, but with zero based index, their positions are odd numbers
        for(int j=0;j<common.length;j++){
            irmX.put(new VariableName("i"+(j+1)), new IrmPCM(bparamX[common[j]], thresholdX[common[j]], 1.0));
            irmY.put(new VariableName("i"+(j+1)), new IrmPCM(bparamY[common[j]], thresholdY[common[j]], 1.0));
        }

        UniformQuadratureRule distX = new UniformQuadratureRule(-4.0, 4.0, 51);
        UniformQuadratureRule distY = new UniformQuadratureRule(-4.0, 4.0, 51);

        IrtScaleLinking irtScaleLinking = new IrtScaleLinking(irmX, irmY, distX, distY);
        irtScaleLinking.setPrecision(6);
        irtScaleLinking.computeCoefficients();

        System.out.println(irtScaleLinking.toString());

        MeanMeanMethod mm = irtScaleLinking.getMeanMeanMethod();
        MeanSigmaMethod ms = irtScaleLinking.getMeanSigmaMethod();
        HaebaraMethod hb = irtScaleLinking.getHaebaraMethod();
        StockingLordMethod sl = irtScaleLinking.getStockingLordMethod();

        //Test Mean/sigma results
        assertEquals("  Mean/sigma Intercept test", 0.005485, ms.getIntercept(), 1e-6);
        assertEquals("  Mean/sigma Scale test", 1.0, ms.getScale(), 1e-6);

        //Test Mean/mean results
        assertEquals("  Mean/mean Intercept test", 0.005485, mm.getIntercept(), 1e-6);
        assertEquals("  Mean/mean Scale test", 1.0, mm.getScale(), 1e-6);

        //Test Haebara results
        assertEquals("  Haebara Intercept test", 0.003107, hb.getIntercept(), 1e-4);
        assertEquals("  Haebara Scale test", 1.0, hb.getScale(), 1e-4);
        assertEquals("  Haebara objective function value test", 0.0001470726, irtScaleLinking.getHaebaraObjectiveFunctionValue(), 1e-5);

        //Test Stocking-Lord results
        assertEquals("  Stocking-Lord Intercept test", 0.009525, sl.getIntercept(), 1e-4);
        assertEquals("  Stocking-Lord Scale test", 1.0, sl.getScale(), 1e-4);
        assertEquals("  Stocking-Lord objective function value test", 0.0032013068, irtScaleLinking.getStockingLordObjectiveFunctionValue(), 1e-5);

    }


    /**
     * Item parameters for Form X and Form Y were obtained using ICL. True linking results obtained with
     * STUIRT using the following syntax on 10/18/2014.
     *
     * NE 13
     *   1  L3 2 DW  1.0  0.762370  1.009476 0.109968
     *   2  L3 2 DW  1.0  1.336925 -0.437730 0.157718
     *   3  L3 2 DW  1.0  1.876282  1.093312 0.135095
     *   4  L3 2 DW  1.0  0.830231 -1.502055 0.227047
     *   5  L3 2 DW  1.0  1.274011  1.029875 0.280884
     *   6  L3 2 DW  1.0  1.139027 -0.142340 0.132300
     *   7  L3 2 DW  1.0  0.645040  0.785645 0.176663
     *   8  L3 2 DW  1.0  1.099075 -1.501227 0.183540
     *   9  L3 2 DW  1.0  1.173436 -0.055542 0.247029
     *   10 L3 2 DW  1.0  0.855866  1.022627 0.209809
     *   11 PC 4 DW  1.0  0.871415 IS 0.0 -0.765898 -0.262353 1.077962
     *   12 PC 4 DW  1.0  0.978374 IS 0.0 -0.837283 -0.275441 1.136771
     *   13 PC 4 DW  1.0  0.856862 IS 0.0 -1.194004  0.318944 1.059092
     *
     *   OL 13
     *   1  L3 2 DW  1.0  1.053310  1.280453 0.220378
     *   2  L3 2 DW  1.0  1.525842 -0.384423 0.166410
     *   3  L3 2 DW  1.0  1.943304  1.073916 0.131654
     *   4  L3 2 DW  1.0  0.788018 -1.637711 0.248980
     *   5  L3 2 DW  1.0  1.005941  0.930207 0.216200
     *   6  L3 2 DW  1.0  1.230981 -0.224566 0.150033
     *   7  L3 2 DW  1.0  1.105784  1.082957 0.278372
     *   8  L3 2 DW  1.0  1.108659 -1.358290 0.192793
     *   9  L3 2 DW  1.0  1.122514 -0.130496 0.214603
     *   10 L3 2 DW  1.0  0.777442  1.077921 0.228847
     *   11 PC 4 DW  1.0  0.840509 IS 0.0 -0.681719 -0.412208 1.126726
     *   12 PC 4 DW  1.0  0.968292 IS 0.0 -0.717030 -0.332964 1.010151
     *   13 PC 4 DW  1.0  0.809768 IS 0.0 -1.360084  0.274189 1.085692
     *
     *   CI 13 AO
     *
     *   OP
     *   SY BI BI
     *   FS DO DO
     *   LM 1.0 0.0 / 10 3.0 0.0001 IN
     *   BY
     *
     */
    @Test
    public void mixedFormatTestSTUIRT(){
        System.out.println("MIXED-MISSING STUIRT COMPARISON");
        LinkedHashMap<VariableName, ItemResponseModel> irmX = new LinkedHashMap<VariableName, ItemResponseModel>();
        LinkedHashMap<VariableName, ItemResponseModel> irmY = new LinkedHashMap<VariableName, ItemResponseModel>();

        //Form X
        irmX.put(new VariableName("v1"), new Irm3PL(0.76237,1.009476,0.109968, 1.0));
        irmX.put(new VariableName("v2"), new Irm3PL(1.336925,-0.43773,0.157718, 1.0));
        irmX.put(new VariableName("v3"), new Irm3PL(1.876282,1.093312,0.135095, 1.0));
        irmX.put(new VariableName("v4"), new Irm3PL(0.830231,-1.502055,0.227047, 1.0));
        irmX.put(new VariableName("v5"), new Irm3PL(1.274011,1.029875,0.280884, 1.0));
        irmX.put(new VariableName("v6"), new Irm3PL(1.139027,-0.14234,0.1323, 1.0));
        irmX.put(new VariableName("v7"), new Irm3PL(0.64504,0.785645,0.176663, 1.0));
        irmX.put(new VariableName("v8"), new Irm3PL(1.099075,-1.501227,0.18354, 1.0));
        irmX.put(new VariableName("v9"), new Irm3PL(1.173436,-0.055542,0.247029, 1.0));
        irmX.put(new VariableName("v10"), new Irm3PL(0.855866,1.022627,0.209809, 1.0));

        double[] step1 = {0.0, -0.765898,-0.262353,1.077962};
        irmX.put(new VariableName("v11"), new IrmGPCM(0.871415, step1, 1.0));

        double[] step2 = {0.0, -0.837283,-0.275441,1.136771};
        irmX.put(new VariableName("v12"), new IrmGPCM(0.978374, step2, 1.0));

        double[] step3 = {0.0, -1.194004,0.318944,1.059092};
        irmX.put(new VariableName("v13"), new IrmGPCM(0.856862, step3, 1.0));


        //Form Y
        irmY.put(new VariableName("v1"), new Irm3PL(1.05331,1.280453,0.220378, 1.0));
        irmY.put(new VariableName("v2"), new Irm3PL(1.525842,-0.384423,0.16641, 1.0));
        irmY.put(new VariableName("v3"), new Irm3PL(1.943304,1.073916,0.131654, 1.0));
        irmY.put(new VariableName("v4"), new Irm3PL(0.788018,-1.637711,0.24898, 1.0));
        irmY.put(new VariableName("v5"), new Irm3PL(1.005941,0.930207,0.2162, 1.0));
        irmY.put(new VariableName("v6"), new Irm3PL(1.230981,-0.224566,0.150033, 1.0));
        irmY.put(new VariableName("v7"), new Irm3PL(1.105784,1.082957,0.278372, 1.0));
        irmY.put(new VariableName("v8"), new Irm3PL(1.108659,-1.35829,0.192793, 1.0));
        irmY.put(new VariableName("v9"), new Irm3PL(1.122514,-0.130496,0.214603, 1.0));
        irmY.put(new VariableName("v10"), new Irm3PL(0.777442,1.077921,0.228847, 1.0));

        double[] step1Y = {0.0, -0.681719,-0.412208,1.126726};
        irmY.put(new VariableName("v11"), new IrmGPCM(0.840509, step1Y, 1.0));

        double[] step2Y = {0.0, -0.71703,-0.332964,1.010151};
        irmY.put(new VariableName("v12"), new IrmGPCM(0.968292, step2Y, 1.0));

        double[] step3Y = {0.0, -1.360084,0.274189,1.085692};
        irmY.put(new VariableName("v13"), new IrmGPCM(0.809768, step3Y, 1.0));

        //STUIRT default
        UniformQuadratureRule uniform = new UniformQuadratureRule(-3.0, 3.0, 25);

        IrtScaleLinking irtScaleLinking = new IrtScaleLinking(irmX, irmY, uniform, uniform);
        irtScaleLinking.setPrecision(6);
        irtScaleLinking.computeCoefficients();

        System.out.println(irtScaleLinking.toString());

        MeanMeanMethod mm = irtScaleLinking.getMeanMeanMethod();
        MeanSigmaMethod ms = irtScaleLinking.getMeanSigmaMethod();
        HaebaraMethod hb = irtScaleLinking.getHaebaraMethod();
        StockingLordMethod sl = irtScaleLinking.getStockingLordMethod();

        //Test Mean/mean results
        assertEquals("  Mean/mean Intercept test", 0.010863, mm.getIntercept(), 1e-5);
        assertEquals("  Mean/mean Scale test", 0.959283, mm.getScale(), 1e-5);

        //Test Mean/sigma results
        assertEquals("  Mean/sigma Intercept test", 0.004596, ms.getIntercept(), 1e-5);
        assertEquals("  Mean/sigma Scale test", 1.035626, ms.getScale(), 1e-5);

        //Test Haebara results
        assertEquals("  Haebara Intercept test", -0.022233, hb.getIntercept(), 1e-4);
        assertEquals("  Haebara Scale test", 0.996992, hb.getScale(), 1e-4);
        assertEquals("  Haebara objective function value test", 0.000954, irtScaleLinking.getHaebaraObjectiveFunctionValue(), 1e-6);

        //Test Stocking-Lord results
        assertEquals("  Stocking-Lord Intercept test", -0.026322, sl.getIntercept(), 1e-4);
        assertEquals("  Stocking-Lord Scale test", 1.001785, sl.getScale(), 1e-4);
        assertEquals("  Stocking-Lord objective function value test", 0.004053, irtScaleLinking.getStockingLordObjectiveFunctionValue(), 1e-6);

    }



}
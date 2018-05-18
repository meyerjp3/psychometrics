package com.itemanalysis.psychometrics.distribution;

//****************************************************************************80

import org.apache.commons.math3.special.Erf;

//****************************************************************************80
//
//  Purpose:
//
//    BIVARIATE_NORMAL_CDF_VALUES returns some values of the bivariate normal CDF.
//
//  Discussion:
//
//    FXY is the probability that two variables A and B, which are
//    related by a bivariate normal quadrature with correlation R,
//    respectively satisfy A <= X and B <= Y.
//
//    Mathematica can evaluate the bivariate normal CDF via the commands:
//
//      <<MultivariateStatistics`
//      cdf = CDF[MultinormalDistribution[{0,0}{{1,r},{r,1}}],{x,y}]
//
//  Licensing:
//
//    This code is distributed under the GNU LGPL license.
//
//  Modified:
//
//    23 November 2010
//
//  Author:
//
//    John Burkardt
//
//  Reference:
//
//    National Bureau of Standards,
//    Tables of the Bivariate Normal Distribution and Related Functions,
//    NBS, Applied Mathematics Series, Number 50, 1959.
//
//  Parameters:
//
//    Input/output, int &N_DATA.  The user sets N_DATA to 0 before the
//    first call.  On each call, the routine increments N_DATA by 1, and
//    returns the corresponding data; when there is no more data, the
//    output value of N_DATA will be 0 again.
//
//    Output, double &X, &Y, the parameters of the function.
//
//    Output, double &R, the correlation value.
//
//    Output, double &FXY, the value of the function.
//

public class BivariateNormalDistribution {

//    int N_MAX = 41;
//
//    static double[] fxy_vec = {
//            0.02260327218569867E+00,
//            0.1548729518584100E+00,
//            0.4687428083352184E+00,
//            0.7452035868929476E+00,
//            0.8318608306874188E+00,
//            0.8410314261134202E+00,
//            0.1377019384919464E+00,
//            0.1621749501739030E+00,
//            0.1827411243233119E+00,
//            0.2010067421506235E+00,
//            0.2177751155265290E+00,
//            0.2335088436446962E+00,
//            0.2485057781834286E+00,
//            0.2629747825154868E+00,
//            0.2770729823404738E+00,
//            0.2909261168683812E+00,
//            0.3046406378726738E+00,
//            0.3183113449213638E+00,
//            0.3320262544108028E+00,
//            0.3458686754647614E+00,
//            0.3599150462310668E+00,
//            0.3742210899871168E+00,
//            0.3887706405282320E+00,
//            0.4032765198361344E+00,
//            0.4162100291953678E+00,
//            0.6508271498838664E+00,
//            0.8318608306874188E+00,
//            0.0000000000000000,
//            0.1666666666539970,
//            0.2500000000000000,
//            0.3333333333328906,
//            0.5000000000000000,
//            0.7452035868929476,
//            0.1548729518584100,
//            0.1548729518584100,
//            0.06251409470431653,
//            0.7452035868929476,
//            0.1548729518584100,
//            0.1548729518584100,
//            0.06251409470431653,
//            0.6337020457912916};
//
//    static double[] r_vec = {
//            0.500, 0.500, 0.500, 0.500, 0.500,
//            0.500, -0.900, -0.800, -0.700, -0.600,
//            -0.500, -0.400, -0.300, -0.200, -0.100,
//            0.000, 0.100, 0.200, 0.300, 0.400,
//            0.500, 0.600, 0.700, 0.800, 0.900,
//            0.673, 0.500, -1.000, -0.500, 0.000,
//            0.500, 1.000, 0.500, 0.500, 0.500,
//            0.500, 0.500, 0.500, 0.500, 0.500,
//            0.500};
//    static double[] x_vec = {
//            -2.0, -1.0, 0.0, 1.0, 2.0,
//            3.0, -0.2, -0.2, -0.2, -0.2,
//            -0.2, -0.2, -0.2, -0.2, -0.2,
//            -0.2, -0.2, -0.2, -0.2, -0.2,
//            -0.2, -0.2, -0.2, -0.2, -0.2,
//            1.0, 2.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 1.0, 1.0, -1.0,
//            -1.0, 1.0, 1.0, -1.0, -1.0,
//            0.7071067811865475};
//    static double[] y_vec = {
//            1.0, 1.0, 1.0, 1.0, 1.0,
//            1.0, 0.5, 0.5, 0.5, 0.5,
//            0.5, 0.5, 0.5, 0.5, 0.5,
//            0.5, 0.5, 0.5, 0.5, 0.5,
//            0.5, 0.5, 0.5, 0.5, 0.5,
//            0.5, 1.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 1.0, -1.0, 1.0,
//            -1.0, 1.0, -1.0, 1.0, -1.0,
//            0.7071067811865475};
//
//    int n_data = 0;

    public BivariateNormalDistribution() {

    }

//    public double bivariate_normal_cdf_values(int n_data, double x, double y, double r) {
//        double fxy = 0;
//        if (n_data < 0) {
//            n_data = 0;
//        }
//
//        n_data = n_data + 1;
//
//        if (N_MAX < n_data) {
//            n_data = 0;
//            r = 0.0;
//            x = 0.0;
//            y = 0.0;
//            fxy = 0.0;
//        } else {
//            r = r_vec[n_data - 1];
//            x = x_vec[n_data - 1];
//            y = y_vec[n_data - 1];
//            fxy = fxy_vec[n_data - 1];
//        }
//        return fxy;
//    }

//        void bivariate_normal_cdf_values ( int &n_data, double &x, double &y,
//  double &r, double &fxy )
//{
//#  define N_MAX 41
//
//
//
//  if ( n_data < 0 )
//  {
//    n_data = 0;
//  }
//
//  n_data = n_data + 1;
//
//  if ( N_MAX < n_data )
//  {
//    n_data = 0;
//    r = 0.0;
//    x = 0.0;
//    y = 0.0;
//    fxy = 0.0;
//  }
//  else
//  {
//    r = r_vec[n_data-1];
//    x = x_vec[n_data-1];
//    y = y_vec[n_data-1];
//    fxy = fxy_vec[n_data-1];
//  }
//
//  return;
//# undef N_MAX
//}
//****************************************************************************80


    //****************************************************************************80
//
//  Purpose:
//
//    BIVNOR computes the bivariate normal CDF.
//
//  Discussion:
//
//    BIVNOR computes the probability for two normal variates X and Y
//    whose correlation is R, that AH <= X and AK <= Y.
//
//  Licensing:
//
//    This code is distributed under the GNU LGPL license.
//
//  Modified:
//
//    13 April 2012
//
//  Author:
//
//    Original FORTRAN77 version by Thomas Donnelly.
//    C++ version by John Burkardt.
//
//  Reference:
//
//    Thomas Donnelly,
//    Algorithm 462: Bivariate Normal Distribution,
//    Communications of the ACM,
//    October 1973, Volume 16, Number 10, page 638.
//
//  Parameters:
//
//    Input, double AH, AK, the lower limits of integration.
//
//    Input, double R, the correlation between X and Y.
//
//    Output, double BIVNOR, the bivariate normal CDF.
//
//  Local Parameters:
//
//    Local, int IDIG, the number of significant digits
//    to the right of the decimal point desired in the answer.
//
    public double bivnor(double ah, double ak, double r) {
        double a2;
        double ap;
        double b;
        double cn;
        double con;
        double conex;
        double ex;
        double g2;
        double gh;
        double gk;
        double gw = 0;
        double h2;
        double h4;
        int i;
        int idig = 15;
        int is = 0;
        double rr;
        double s1;
        double s2;
        double sgn;
        double sn;
        double sp;
        double sqr;
        double t;
        double twopi = 6.283185307179587;
        double w2 = 0;
        double wh = 0;
        double wk = 0;

        b = 0.0;

        gh = gauss(-ah) / 2.0;
        gk = gauss(-ak) / 2.0;

        if (r == 0.0) {
            b = 4.00 * gh * gk;
            b = r8_max(b, 0.0);
            b = r8_min(b, 1.0);
            return b;
        }

        rr = (1.0 + r) * (1.0 - r);

        if (rr < 0.0) {
            throw new IllegalStateException("BIVNOR - Fatal error!");
//    cerr << "\n";
//    cerr << "BIVNOR - Fatal error!\n";
//    cerr << "  1 < |R|.\n";
//    exit ( 0 );
        }

        if (rr == 0.0) {
            if (r < 0.0) {
                if (ah + ak < 0.0) {
                    b = 2.0 * (gh + gk) - 1.0;
                }
            } else {
                if (ah - ak < 0.0) {
                    b = 2.0 * gk;
                } else {
                    b = 2.0 * gh;
                }
            }
            b = r8_max(b, 0.0);
            b = r8_min(b, 1.0);
            return b;
        }

        sqr = Math.sqrt(rr);

        if (idig == 15) {
            con = twopi * 1.0E-15 / 2.0;
        } else {
            con = twopi / 2.0;
            for (i = 1; i <= idig; i++) {
                con = con / 10.0;
            }
        }
//
//  (0,0)
//
        if (ah == 0.0 && ak == 0.0) {
            b = 0.25 + Math.asin(r) / twopi;
            b = r8_max(b, 0.0);
            b = r8_min(b, 1.0);
            return b;
        }
//
//  (0,nonzero)
//
        if (ah == 0.0 && ak != 0.0) {
            b = gk;
            wh = -ak;
            wk = (ah / ak - r) / sqr;
            gw = 2.0 * gk;
            is = 1;
        }
//
//  (nonzero,0)
//
        else if (ah != 0.0 && ak == 0.0) {
            b = gh;
            wh = -ah;
            wk = (ak / ah - r) / sqr;
            gw = 2.0 * gh;
            is = -1;
        }
//
//  (nonzero,nonzero)
//
        else if (ah != 0.0 && ak != 0.0) {
            b = gh + gk;
            if (ah * ak < 0.0) {
                b = b - 0.5;
            }
            wh = -ah;
            wk = (ak / ah - r) / sqr;
            gw = 2.0 * gh;
            is = -1;
        }

        for (; ; ) {
            sgn = -1.0;
            t = 0.0;

            if (wk != 0.0) {
                if (r8_abs(wk) == 1.0) {
                    t = wk * gw * (1.0 - gw) / 2.0;
                    b = b + sgn * t;
                } else {
                    if (1.0 < r8_abs(wk)) {
                        sgn = -sgn;
                        wh = wh * wk;
                        g2 = gauss(wh);
                        wk = 1.0 / wk;

                        if (wk < 0.0) {
                            b = b + 0.5;
                        }
                        b = b - (gw + g2) / 2.0 + gw * g2;
                    }
                    h2 = wh * wh;
                    a2 = wk * wk;
                    h4 = h2 / 2.0;
                    ex = Math.exp(-h4);
                    w2 = h4 * ex;
                    ap = 1.0;
                    s2 = ap - ex;
                    sp = ap;
                    s1 = 0.0;
                    sn = s1;
                    conex = r8_abs(con / wk);

                    for (; ; ) {
                        cn = ap * s2 / (sn + sp);
                        s1 = s1 + cn;

                        if (r8_abs(cn) <= conex) {
                            break;
                        }
                        sn = sp;
                        sp = sp + 1.0;
                        s2 = s2 - w2;
                        w2 = w2 * h4 / sp;
                        ap = -ap * a2;
                    }
                    t = (Math.atan(wk) - wk * s1) / twopi;
                    b = b + sgn * t;
                }
            }
            if (0 <= is) {
                break;
            }
            if (ak == 0.0) {
                break;
            }
            wh = -ak;
            wk = (ah / ak - r) / sqr;
            gw = 2.0 * gk;
            is = 1;
        }

        b = r8_max(b, 0.0);
        b = r8_min(b, 1.0);

        return b;
    }
//****************************************************************************80


    //****************************************************************************80
//
//  Purpose:
//
//    GAUSS returns the area of the lower tail of the normal curve.
//
//  Licensing:
//
//    This code is distributed under the GNU LGPL license.
//
//  Modified:
//
//    13 April 2012
//
//  Author:
//
//    John Burkardt
//
//  Parameters:
//
//    Input, double T, the evaluation point.
//
//    Output, double GAUSS, the lower normal tail area.
//
    double gauss(double t) {
        double value;

        value = (1.0 + Erf.erf(t / Math.sqrt(2.0))) / 2.0;

        return value;
    }
//****************************************************************************80


    //****************************************************************************80
//
//  Purpose:
//
//    R8_ABS returns the absolute value of an R8.
//
//  Licensing:
//
//    This code is distributed under the GNU LGPL license.
//
//  Modified:
//
//    14 November 2006
//
//  Author:
//
//    John Burkardt
//
//  Parameters:
//
//    Input, double X, the quantity whose absolute value is desired.
//
//    Output, double R8_ABS, the absolute value of X.
//
    double r8_abs(double x) {
        double value;

        if (0.0 <= x) {
            value = +x;
        } else {
            value = -x;
        }
        return value;
    }
//****************************************************************************80


    //****************************************************************************80
//
//  Purpose:
//
//    R8_MAX returns the maximum of two R8's.
//
//  Licensing:
//
//    This code is distributed under the GNU LGPL license.
//
//  Modified:
//
//    18 August 2004
//
//  Author:
//
//    John Burkardt
//
//  Parameters:
//
//    Input, double X, Y, the quantities to compare.
//
//    Output, double R8_MAX, the maximum of X and Y.
//
    double r8_max(double x, double y) {
        double value;

        if (y < x) {
            value = x;
        } else {
            value = y;
        }
        return value;
    }
//****************************************************************************80


    //****************************************************************************80
//
//  Purpose:
//
//    R8_MIN returns the minimum of two R8's.
//
//  Licensing:
//
//    This code is distributed under the GNU LGPL license.
//
//  Modified:
//
//    31 August 2004
//
//  Author:
//
//    John Burkardt
//
//  Parameters:
//
//    Input, double X, Y, the quantities to compare.
//
//    Output, double R8_MIN, the minimum of X and Y.
//
    double r8_min(double x, double y) {
        double value;

        if (y < x) {
            value = y;
        } else {
            value = x;
        }
        return value;
    }

}
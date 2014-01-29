psychometrics
=============

A Java library for psychometric analysis. It includes mathematical and statistical 
procedures that are not part of the Apache commons math library. It is a key component 
of jMetrik an open source application for psychometrics (see http://www.ItemAnalysis.com).

Classical Test Theory
---------------------
The library includes classes for classical test scaling methods, reliability estimation,
item analysis, and differential item functioning (DIF). Examples of scaling methods include
normalized scores and Kelley's regressed score. Reliability methods include Coefficient alpha
Guttman's lambda, and other methods. There are classes to support the conditional standard
error of measurement and decision consistency indices. Classes that support DIF include the
Cochran-Mantel-Haenszel procedure and ETS DIF classification levels.

Factor Analysis
---------------
Classes for exploratory factor analysis are in development. They currently include MINRES
factor analysis and principal components analysis. No rotations have yet been implemented.

Item Response Theory (IRT)
--------------------------
This library provides classes for IRT parameter estimation, scale linking, and score equating.
Estimation currently involves joint maximum likelihood for the Rasch, partial credit, and
rating scale models. Marginal maximum likelihood estimation procedure for other models are
currently in development. Scale linking and score equating classes support a wider range of
item response models such as those listed earlier and the two-parameter logistic (2PL), three
parameter logistic (3PL), generalized partial credit model (GPCM), and graded response model
(GRM). Scale linking procedures available in the library include the Stocking-Lord and
Haebara procedures.



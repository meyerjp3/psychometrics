psychometrics
=============

A Java library for psychometric analysis. [View the psychometrics API](https://itemanalysis.com/psychometrics-javadoc/1.4/)
 to learn more about the library. It includes mathematical and statistical procedures that are not part of the Apache
 commons math library. It is a key component of [jMetrik](http://www.ItemAnalysis.com) an open source application for
 psychometrics. See http://www.ItemAnalysis.com for more information about jMetrik and the psychometrics library.
 The library is licensed under the Apache License, Version 2.0.
 
 Recommended Citation:
 
 Meyer, J. P. (2015). Psychometrics: An open source Java<sup>TM</sup> library for measurement [computer software]. Available from https://github.com/meyerjp3/psychometrics. 



The psychometrics library was refactored into seven modules. These are:
* **psychometrics-core** the base module required by all other module. It contains basic classes for data, exceptoins, and statistics
* **psychometrics-ctt** contains classes for classical test theory including item analysis, test scaling, DIF, and reliability estimation.
* **psychometrics-factor** has classes for exploratory factor analysis, and for computing polychoric correlations. This module is still in development.
* **psychometrics-fmm** contains a few classes for multivariate normal mixture models (i.e. finite mixture models).
* **psychometrics-irt** includes classes for item response theory. Joint maximum likelihood for the Rasch family of models is supported.
Marginal maximum likelihood estimation for the 2PL, 3PL, 4PL, and GPCM is also supported. Classes for scale linking (e.g. Stocking-Lord) and score equating are in this module.
* **psychometrics-nirt** has classes for nonparametric item resposne theory. Specifically, it support Ramsay's kernel regression approach for estimating ICCs.
* **psychometrics-optim** is a module for optimization, and it includes the UNCMIN method, a BOBYQAO optimizer, and a Quasi-Newton optimizer.



Item Response Theory (IRT)
--------------------------
This library provides classes for IRT parameter estimation, scale linking, and score equating.
Estimation currently involves joint maximum likelihood for the Rasch, partial credit, and
rating scale models. Marginal maximum likelihood estimation procedures for binary item response models (Rasch,
2PL, 3PL, 4PL) and polytomous item response models (GPCM, PCM) are also available. Scale linking and score equating
classes support a variety of item response models. Scale linking procedures available in the library include the
Stocking-Lord and Haebara procedures.

Factor Analysis
---------------
Classes for exploratory factor analysis are in development. They currently include MINRES
factor analysis and principal components analysis. Some rotations have been implemented.

Classical Test Theory
---------------------
The library includes classes for classical test scaling methods, reliability estimation,
item analysis, and differential item functioning (DIF). Examples of scaling methods include
normalized scores and Kelley's regressed score. Reliability methods include Coefficient alpha
Guttman's lambda, and other methods. There are classes to support the conditional standard
error of measurement and decision consistency indices. Classes that support DIF include the
Cochran-Mantel-Haenszel procedure and ETS DIF classification levels.





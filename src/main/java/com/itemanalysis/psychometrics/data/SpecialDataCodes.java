package com.itemanalysis.psychometrics.data;

public interface SpecialDataCodes extends MissingDataCodes{

    void setMissingDataCode(Object missingDataCode);

    void setOmittedCode(Object omittedCode);

    void setNotReachedCode(Object notReachedCode);

    void setMissingDataScore(double missingDataScore);

    void setOmittedScore(double omittedScore);

    void setNotReachedScore(double notReachedScore);

    Object getMissingDataCode();

    Object getOmittedCode();

    Object getNotReachedCode();

    boolean isOmitted(Object response);

    boolean isNotReached(Object response);

    double computeMissingScore(String response);

    double computeMissingScore(Object response);

    double computeMissingScore(double response);

    double computeMissingScore(int response);

}

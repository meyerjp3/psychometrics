package com.itemanalysis.psychometrics.data;

public interface MissingDataCodes {

    void addMissingDataCode(String response);

    /**
     * Check of value is a missing value code
     *
     * @param response and item response
     * @return true if value is a missing data code
     */
    boolean isMissing(String response);

    /**
     * The opposite of isMissing
     *
     * @param response and item response
     * @return true if value is present
     */
    boolean isPresent(String response);

    boolean isMissing(Object object);

    boolean isPresent(Object object);

    boolean isMissing(double response);

    boolean isPresent(double response);

    boolean isMissing(int response);

    boolean isPresent(int response);


}

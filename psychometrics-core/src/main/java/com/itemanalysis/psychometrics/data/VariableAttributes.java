package com.itemanalysis.psychometrics.data;

import com.itemanalysis.psychometrics.exceptions.ItemScoringException;

public interface VariableAttributes {

    /**
     *
     * @return name of variable
     */
    VariableName getName();

    void setName(VariableName name);

    VariableLabel getLabel();

    void setLabel(String label);

    VariableType getType();

    DataType getDataType();

    ItemType getItemType();

    void setItemType(ItemType itemType);

    void setDataType(DataType dataType);

    boolean hasScoring();

    void setItemScoring(ItemScoring itemScoring);

    /**
     * Legacy code for use with jMetrik versions that have an embedded database (prior to version 5)
     */
    int positionInDb();

    String getItemGroup();

    void setMissingDataCodes(MissingDataCodes missingDataCodes);

    MissingDataCodes getMissingDataCodes();

    boolean isMissing(String response);

    boolean isPresent(String response);

    double computeItemScore(String response) throws ItemScoringException;

    double computeItemScore(double response) throws ItemScoringException;

    double computeItemScore(int response) throws ItemScoringException;

    double getMaxItemScore();

    String[] getAttributeArray();

    String getSubscale(boolean useItemNameWhenEmpty);

    void setItemGroup(String itemGroup);

    void setTestItemOrder(int testItemOrder);

    int getTestItemOrder();

    void setVarcharSize(int varcharSize);

    String getDatabaseTypeString();

    int compareTo(VariableAttributes o);

    String printAttributes();

//===============================================================================================
// THE FOLLOWING METHODS ARE A WRAPPER TO THE ItemScoring object
//===============================================================================================
    void addAllCategories(String optionScoreKey);

    ItemScoring getItemScoring();

    double getMinimumPossibleItemScore();

    Double getMaximumPossibleItemScore();

    void clearCategory();

    double[] scoreArray();

    String printOptionScoreKey();





}

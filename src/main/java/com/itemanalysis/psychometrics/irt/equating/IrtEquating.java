package com.itemanalysis.psychometrics.irt.equating;

/**
 * Interface for IRT equating (either true score equating or observed score equating).
 * All arrays returned in methods described below must be the same length.
 * Form X is equated to the scale of Form Y.
 *
 */
public interface IrtEquating {

    /**
     * Method that performs the equating
     */
    public void equateScores();

    /**
     * Returns an array of Form X observed score values.
     *
     * @return array
     */
    public double[] getFormXScores();

    /**
     * Returns an array of proficiency values that correspond to observed scores.
     * Only needed for true score equating.
     *
     * @return array
     */
    public double[] getFormXThetaValues();

    /**
     * Returns array of equated scores (the Y-equivalent scores)
     *
     * @return array
     */
    public double[] getYEquivalentScores();

    /**
     * An array of status indicators foe the Newtwon-Raphson procedure in true score equating.
     *
     * @return array
     */
    public char[] getStatus();

}

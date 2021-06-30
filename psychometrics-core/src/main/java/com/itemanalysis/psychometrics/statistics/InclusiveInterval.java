package com.itemanalysis.psychometrics.statistics;

public class InclusiveInterval {

    private double leftBound = -Double.MAX_VALUE;

    private double rightBound = Double.MAX_VALUE;

    public InclusiveInterval(double leftBound, double rightBound){
        this.leftBound = leftBound;
        this.rightBound = rightBound;

        if(leftBound>rightBound){
            this.leftBound = rightBound;
            this.rightBound = leftBound;
        }
    }

    public InclusiveInterval(double[] bounds){
        this.leftBound = bounds[0];
        this.rightBound = bounds[1];

        if(bounds[0]>bounds[1]){
            this.leftBound = bounds[1];
            this.rightBound = bounds[0];
        }
    }

    public InclusiveInterval(){

    }

    /**
     * Determines if value is in side the inclusive interval [lower, upper].
     *
     * @param value a numeric value
     * @return true if value is contained in the inclusive interval
     */
    public boolean includes(double value){
        if(value >= leftBound && value <= rightBound) return true;
        return false;
    }

    /**
     * opposite of includes
     *
     * @param value a numeric value
     * @return true if excludes of interval
     */
    public boolean excludes(double value){
        return !includes(value);
    }

}

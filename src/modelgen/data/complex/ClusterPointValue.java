package modelgen.data.complex;

import modelgen.shared.Logger;

public class ClusterPointValue extends ClusterPoint<ClusterPointValue> 
                               implements Cloneable<ClusterPointValue>, Measurable<ClusterPointValue>,
                                          Mergeable<ClusterPointValue>, Metricable, Printable {
    final protected String ERROR_PREFIX = "ClusterPointValue error. ";
    final protected String DEBUG_PREFIX = "ClusterPointValue debug. ";

    final protected Integer DEBUG_LEVEL = 1;
    final protected Double ABS_THRESHOLD = 0.1;

    ClusterPointEvalHelper evalHelper;

    public ClusterPointValue(double data, ClusterPointEvalHelper helper) {
        super(data);
        evalHelper = helper;
    }

    public ClusterPointValue(ClusterPointValue toCopy) {
        super(toCopy);
        evalHelper = toCopy.evalHelper;
    }

    @Override
    public double evaluate() {
        if (Math.abs(center) < ABS_THRESHOLD)
            return Math.abs((max - min)/evalHelper.getDeviation());
        else
            return Math.abs((max - min)/center);
    }

    @Override
    public boolean mergeWith(ClusterPointValue itemToMerge) {
        try {
            if (super.mergeWith(itemToMerge)) {
                evalHelper.processPoint(this);
                return true;
            }
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public ClusterPointValue makeCopy() {
        return new ClusterPointValue(this);
    }
}

package modelgen.data.state;

import java.text.DecimalFormat;

import modelgen.data.DataType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataThreshold;
import modelgen.shared.Logger;

public class StateThresholds extends State implements IState {
    RawDataThreshold thresholds;

    public StateThresholds(String name, Double start, Double end, Double lowBound, Double upperBound) {
        super(name, Double.hashCode(lowBound + upperBound), start, end);
        thresholds = new RawDataThreshold(lowBound, upperBound);
    }

    public StateThresholds(StateThresholds toCopy) {
        this(toCopy.signalName, toCopy.start, toCopy.end, toCopy.thresholds.getLowBound(),
                toCopy.thresholds.getUpperBound());
    }

    @Override
    public String convertToString() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return "[" + df.format(thresholds.getLowBound()) + ", " + df.format(thresholds.getUpperBound()) + "]";
    }

    @Override
    public DataType getType() {
        return DataType.CONTINOUS_THRESHOLD;
    }

    @Override
    public IState makeCopy() {
        return new StateThresholds(this);
    }

    @Override
    public RawDataChunk generateSignal(RawDataChunk baseSignal) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataEquality compareTo(IState stateCmp) {
        //Operation undefined for states of different type
        if (!this.getClass().equals(stateCmp.getClass()))
            return null;

        //Operation undefined for states of different signals
        if (!signalName.equals(stateCmp.getSignalName()))
            return null;

        try {
            RawDataThreshold stateCmpThresholds = ((StateThresholds) stateCmp).thresholds;
            
            if (stateCmpThresholds.getLowBound() > thresholds.getLowBound() &&
                    stateCmpThresholds.getLowBound() < thresholds.getUpperBound())
                return DataEquality.SUBSET;

            if (stateCmpThresholds.getUpperBound() > thresholds.getLowBound() &&
                    stateCmpThresholds.getUpperBound() < thresholds.getUpperBound())
                return DataEquality.SUBSET;

            if (thresholds.getLowBound() > stateCmpThresholds.getLowBound() &&
                    thresholds.getLowBound() < stateCmpThresholds.getUpperBound())
                return DataEquality.SUPERSET;

            if (thresholds.getUpperBound() > stateCmpThresholds.getLowBound() &&
                    thresholds.getUpperBound() < stateCmpThresholds.getUpperBound())
                return DataEquality.SUPERSET;

            if  (stateCmpThresholds.getLowBound().equals(thresholds.getLowBound()) &&
                    stateCmpThresholds.getUpperBound().equals(thresholds.getUpperBound()))
                return DataEquality.EQUAL;

            return DataEquality.UNIQUE;
        } catch (ClassCastException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;

    }

    @Override
    public String convertToGuardCondition() {
        String output = "~(" + signalName + ">=" + convertToInt(thresholds.getUpperBound()) + ")&(" + signalName + ">="
                + convertToInt(thresholds.getLowBound()) + ")";
        return output;
    }

    @Override
    public String convertToAssignmentCondition() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        String output = signalName + ":=uniform(" + convertToInt(thresholds.getUpperBound()) + ","
                + convertToInt(thresholds.getLowBound()) + ")";
        return output;
    }

    @Override
    public String convertToInitialCondition() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        String output = signalName + "=uniform(" + convertToInt(thresholds.getUpperBound()) + ","
                + convertToInt(thresholds.getLowBound()) + ")";
        return output;
    }
}

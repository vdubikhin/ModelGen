package modelgen.data.state;

import java.text.DecimalFormat;

import modelgen.data.DataType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataThreshold;
import modelgen.shared.Logger;

public class StateThresholds extends State implements IState {
    RawDataThreshold thresholds;

    public StateThresholds(String name, Integer id, Double start, Double end, Double lowBound, Double upperBound) {
        super(name, id, start, end);
        thresholds = new RawDataThreshold(lowBound, upperBound);
    }

    public StateThresholds(StateThresholds toCopy) {
        this(toCopy.signalName, toCopy.stateId, toCopy.start, toCopy.end, toCopy.thresholds.getLowBound(),
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
        return DataType.CONTINOUS_RANGE;
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

            if  (stateCmpThresholds.getLowBound().equals(thresholds.getLowBound()) &&
                    stateCmpThresholds.getUpperBound().equals(thresholds.getUpperBound()))
                return DataEquality.EQUAL;

            return DataEquality.UNIQUE;
        } catch (ClassCastException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;

    }
}

package modelgen.data.state;

import java.util.AbstractMap;
import java.util.Map;

import modelgen.shared.Logger;

public abstract class State implements IState {
    final protected String ERROR_PREFIX = "State error. ";
    final protected String DEBUG_PREFIX = "State debug. ";
    final protected Integer DEBUG_LEVEL = 1;

    protected String signalName;
    protected Integer stateId;
    protected Double start, end;
    protected Integer scaleCoeff;

    protected State(String name, Integer id, Double start, Double end) {
        this.signalName = name;
        this.stateId = id;
        this.start = start;
        this.end = end;
        
        //TODO: define as constant for now
        scaleCoeff = 1000;
    }

    protected Integer convertToInt(Double value) {
        if (value == null)
            return null;

        return new Double (value*scaleCoeff).intValue();
    }

    @Override
    public String getSignalName() {
        return signalName;
    }

    @Override
    public Integer getId() {
        return stateId;
    }

    @Override
    public void print() {
        Logger.debugPrintln(DEBUG_PREFIX + getSignalName() + getId() + " "+ convertToString(), DEBUG_LEVEL);
    }

    @Override
    public boolean increaseDuration(IState stateToUse) {
        if (!getSignalName().equals(stateToUse.getSignalName()))
            return false;

        Map.Entry<Double, Double> mergeStateStamp = stateToUse.getTimeStamp();
        Double stateToUseStart = mergeStateStamp.getKey();
        Double stateToUseEnd = mergeStateStamp.getValue();

        if (Double.compare(start, stateToUseEnd) == 0) {
            start = stateToUseStart;
            return true;
        }

        if (Double.compare(end, stateToUseStart) == 0) {
            end = stateToUseEnd;
            return true;
        }

        return false;
    }

    @Override
    public boolean canMergeWith(IState itemToMerge) {
        try {
            if (this.compareTo(itemToMerge) != DataEquality.EQUAL)
                return false;

            if (getDuration() < 0 || itemToMerge.getDuration() < 0)
                return false;

            Map.Entry<Double, Double> mergeStateStamp = itemToMerge.getTimeStamp();
            Double mergeStateStart = mergeStateStamp.getKey();
            Double mergeStateEnd = mergeStateStamp.getValue();

            if (mergeStateStart.equals(start) && mergeStateEnd.equals(end))
                return false;
            if (mergeStateStart.compareTo(start) <= 0 && mergeStateEnd.compareTo(start) >= 0 ||
                    mergeStateStart.compareTo(end) <= 0 && mergeStateEnd.compareTo(end) >= 0) {
                return true;
            }
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public boolean mergeWith(IState state) {
        try {
            if (!canMergeWith(state))
                return false;

            Map.Entry<Double, Double> mergeStateStamp = state.getTimeStamp();
            Double mergeStateStart = mergeStateStamp.getKey();
            Double mergeStateEnd = mergeStateStamp.getValue();

            start = Math.min(mergeStateStart, start);
            end = Math.max(mergeStateEnd, end);
            return true;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public Map.Entry<Double, Double> getTimeStamp() {
        try {
            return new AbstractMap.SimpleEntry<Double, Double>(start, end);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    @Override
    public DataEquality compareTo(IState stateCmp) {
        //Operation undefined for states of different type
        if (!getClass().equals(stateCmp.getClass()))
            return null;

        //Operation undefined for states of different signals
        if (!getSignalName().equals(stateCmp.getSignalName()))
            return null;

        if (getId().compareTo(stateCmp.getId()) != 0)
            return DataEquality.UNIQUE;
        else
            return DataEquality.EQUAL;
    }

    @Override
    public String convertToInitialRateCondition() {
        return null;
    }
}

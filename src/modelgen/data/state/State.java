package modelgen.data.state;

import java.util.AbstractMap;
import java.util.Map;

import modelgen.data.complex.Mergeable;
import modelgen.shared.Logger;

public abstract class State implements IState {
    final protected String ERROR_PREFIX = "State error.";
    
    protected String signalName;
    protected Integer stateId;
    protected Double start, end;

    protected State(String name, Integer id, Double start, Double end) {
        this.signalName = name;
        this.stateId = id;
        this.start = start;
        this.end = end;
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
    public boolean canMergeWith(IState itemToMerge) {
        try {
            if (stateId != itemToMerge.getId())
                return false;

            if (!signalName.equals(itemToMerge.getSignalName()))
                return false;

            //TODO: needs double checking
            if (!this.getClass().equals(itemToMerge.getClass()))
                return false;

            if (getDuration() < 0 || itemToMerge.getDuration() < 0)
                return false;

            Map.Entry<Double, Double> mergeStateStamp = itemToMerge.getTimeStamp();
            Double mergeStateStart = mergeStateStamp.getKey();
            Double mergeStateEnd = mergeStateStamp.getValue();

            if (mergeStateStart.equals(start) && mergeStateEnd.equals(end))
                return false;
            if (mergeStateStart <= start && start <= mergeStateEnd ||
                mergeStateStart <= end && end <= mergeStateEnd) {
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
}

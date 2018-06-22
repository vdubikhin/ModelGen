package modelgen.data.state;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import modelgen.data.DataType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;
import modelgen.shared.Logger;
import modelgen.shared.Util;

public class StateContinousRange extends State implements IState {
    private Double lowerBound, upperBound;
    private Double initialValue;
    
    public StateContinousRange(String name, Double start, Double end, Double lowBound, Double upperBound,
            Double initialValue) {
        super(name, generateStateId(lowBound + upperBound), start, end);
        this.lowerBound = lowBound;
        this.upperBound = upperBound;
        this.initialValue = initialValue;
    }

    public StateContinousRange(StateContinousRange toCopy) {
        this(toCopy.signalName, toCopy.start, toCopy.end, toCopy.lowerBound, toCopy.upperBound,
                toCopy.initialValue);
        stateId = toCopy.stateId;
    }

    @Override
    public String convertToString() {
        return "[" + convertToInt(lowerBound) + ", " + convertToInt(upperBound) + "]";
    }

    @Override
    public DataType getType() {
        return DataType.CONTINOUS_RANGE;
    }

    @Override
    public IState makeCopy() {
        return new StateContinousRange(this);
    }

    @Override
    public RawDataChunk generateSignal(RawDataChunk baseSignal) {
        RawDataChunk outputData = new RawDataChunk();
        
        Random r = new Random();
        RawDataPoint prevPoint = null;
        for (RawDataPoint curPoint: baseSignal) {
            if (start <= curPoint.getTime() && curPoint.getTime() <= end) {
                RawDataPoint newPoint;
                if (prevPoint == null) {
                    prevPoint = curPoint;
                    newPoint = curPoint;
                } else {
                    double delta = curPoint.getTime() - prevPoint.getTime();
                    double linearCoefficient = lowerBound + (upperBound - lowerBound) * r.nextDouble();
                    double newValue = prevPoint.getValue() + linearCoefficient * delta;

                    newPoint = new RawDataPoint(newValue, curPoint.getTime());
                    curPoint = newPoint;
                }
                outputData.add(newPoint);
            }
        }
        return outputData;
    }

    @Override
    public String convertToGuardCondition() {
        //Operation forbidden
        return null;
    }

    @Override
    public String convertToAssignmentCondition() {
        String output = signalName + ":=uniform(" + convertToInt(lowerBound) + "," + convertToInt(upperBound) + ")";
        return output;
    }

    @Override
    public String convertToInitialCondition() {
        String output = signalName + "=" + convertToInt(initialValue);
        return output;
    }

    @Override
    public String convertToInitialRateCondition() {
        String output = signalName + "=[" + convertToInt(lowerBound) + "," + convertToInt(upperBound) + "]";
        return output;
    }

    @Override
    public boolean mergeWith(IState state) {
        try {
            if (!canMergeWith(state))
                return false;

            Map.Entry<Double, Double> mergeStateStamp = state.getTimeStamp();
            Double mergeStateStart = mergeStateStamp.getKey();
            Double mergeStateEnd = mergeStateStamp.getValue();

            if (mergeStateStart < start)
                initialValue = ((StateContinousRange) state).initialValue;

            start = Math.min(mergeStateStart, start);
            end = Math.max(mergeStateEnd, end);
            return true;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        } catch (ClassCastException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Can not merge states of different type.", e);
        }
        return false;
    }

    @Override
    public Integer getScalePower() {
        Integer lBase = Util.base10Power(lowerBound);
        Integer uBase = Util.base10Power(upperBound);
        Integer iBase = Util.base10Power(initialValue);

        return Math.min(Math.min(lBase, uBase), iBase);
    }
}

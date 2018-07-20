package modelgen.processor.rulemining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modelgen.data.ControlType;
import modelgen.data.pattern.DataPatterns;
import modelgen.data.pattern.DataPattern;
import modelgen.data.pattern.PatternVector;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.shared.Logger;

public class StatesToPatternConverter {
    protected String ERROR_PREFIX = "StatesToPatternConvertert error.";
    protected String DEBUG_PREFIX = "StatesToPatternConverter debug.";

    public StatesToPatternConverter() {
    }

    public SignalDataPatterns convertStatesToPatterns(List<StageDataState> inputData) {
        try {
            if (inputData == null) {
                Logger.errorLogger(ERROR_PREFIX + " No input data.");
                return null;
            }

            HashMap<String, List<IState>> outputStates = new HashMap<>();

            //Extract output signals and their states
            for (StageDataState signal: inputData) {
                if (signal.getType() == ControlType.OUTPUT) {
                    if (signal.getStates() == null) {
                        Logger.errorLogger(ERROR_PREFIX + " Output signal: " + signal.getName() +
                                " has not been discretized");
                        return null;
                    }

                    outputStates.put(signal.getName(), signal.getStates());
                }
            }

            HashMap<String, DataPatterns> outputPatterns = new HashMap<>();
            HashMap<String, IState> initialStates = new HashMap<>();

            //Fill data patterns of output signals
            for (String outputSignalName: outputStates.keySet()) {
                int stateId = 0;
                IState prevState = null;
                DataPatterns signalPatterns = new DataPatterns();
                for (IState curOutputState: outputStates.get(outputSignalName)) {
                    //Skip first state
                    if (prevState == null) {
                        prevState = curOutputState;
                        initialStates.put(outputSignalName, prevState);
                        continue;
                    }

                    Double startTime = prevState.getTimeStamp().getKey();
                    Double endTime = prevState.getTimeStamp().getValue();
                    List<IState> patternStates = new ArrayList<>();
                    HashMap<String, RawDataChunk> patternRawData = new HashMap<>();

                    for (StageDataState signal: inputData) {
                        //Skip if it is the same signal
                        if (signal.getName().equals(outputSignalName))
                            continue;

                        if (signal.getStates() != null)
                            patternStates.addAll(getStatesInWindow(startTime, endTime, signal.getStates()));
                        else
                            patternRawData.put(signal.getName(), getPointsInWindow(
                                    curOutputState.getTimeStamp().getKey(),
                                    curOutputState.getTimeStamp().getValue(), signal.getData()));
                    }

                    //Add previous output state
                    patternStates.add(prevState);

                    //Prepend previous state
//                    patternStates.add(0, prevState);
                    prevState = curOutputState;
                    PatternVector vector = new PatternVector(patternStates, stateId++);
                    DataPattern pattern = new DataPattern(curOutputState, vector, patternRawData);
                    signalPatterns.add(pattern);
                }

                outputPatterns.put(outputSignalName, signalPatterns);
            }

            return new SignalDataPatterns(outputPatterns, initialStates);
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    private List<IState> getStatesInWindow(Double startTime, Double endTime, List<IState> inputStates)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        if (inputStates != null && !inputStates.isEmpty()) {
            List<IState> outputStates = new ArrayList<IState>();
            for (IState curState: inputStates) {
                Double curStateStart = curState.getTimeStamp().getKey();
                Double curStateEnd = curState.getTimeStamp().getValue();
                if (curStateStart > endTime)
                    break;
                
                if (curStateStart > startTime && curStateStart <= endTime
                        || curStateEnd > startTime) {
                    outputStates.add(curState);
                }
            }
            return outputStates;
        }
        return null;
    }

    private <T extends RawDataPoint> RawDataChunk getPointsInWindow(Double startTime, Double endTime,
                                                                    List<T> inputPoints)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        if (inputPoints != null && !inputPoints.isEmpty()) {
            RawDataChunk outputPoints = new RawDataChunk();
            for (RawDataPoint curPoint: inputPoints) {
                Double curPointTime = curPoint.getTime();
                if (curPointTime >= startTime && curPointTime <= endTime) {
                    outputPoints.add(curPoint);
                }
            }
            return outputPoints;
        }
        return null;
    }
}

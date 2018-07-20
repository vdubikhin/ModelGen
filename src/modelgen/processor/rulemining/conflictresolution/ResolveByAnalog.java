package modelgen.processor.rulemining.conflictresolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modelgen.data.ControlType;
import modelgen.data.pattern.PatternVector;
import modelgen.data.pattern.StateVector;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyInteger;
import modelgen.data.property.PropertyManager;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.stage.StageDataRaw;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.data.state.StateThresholds;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.processor.discretization.DiscretizeDataByNumStates;
import modelgen.processor.rulemining.conflictdetection.ConflictComparable;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.shared.Logger;

public class ResolveByAnalog extends DataProcessor<RuleComparable<PatternVector, RuleFSMVector>>
                      implements IDataProcessor<RuleComparable<PatternVector, RuleFSMVector>> {
    final private Integer RESOLVE_COST = 2;

    private ConflictComparable<PatternVector, RuleFSMVector> conflictToResolve;

    public ResolveByAnalog() {
        this.conflictToResolve = null;
        
        name = "ResolveByAnalog";

        ERROR_PREFIX = "DataProcessor: ResolveByAnalog error.";
        DEBUG_PREFIX = "DataProcessor: ResolveByAnalog debug.";

        valueBaseCost.setValue(RESOLVE_COST);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public ResolveByAnalog(ConflictComparable<PatternVector, RuleFSMVector> conflict) {
        this();
        conflictToResolve = conflict;
    }

    @Override
    public double processCost() {
        try {
            int vectorId = conflictToResolve.getId();
            RuleFSMVector ruleToFix = conflictToResolve.getRuleToFix();
            PatternVector vectorToFix = ruleToFix.getRuleVectorById(vectorId);
            PatternVector vectorToFull = ruleToFix.getFullRuleVectorById(vectorId);
            HashMap<String, RawDataChunk> rawData = ruleToFix.getAnalogDataById(vectorId);

            if (rawData == null || rawData.isEmpty())
                return -1;

            if (vectorToFull.isEmpty())
                return -1;

            //Check if raw data has been all added
            StateVector stateVectorFull = vectorToFull.values().iterator().next();
            if (stateVectorFull.keySet().containsAll(rawData.keySet()))
                return -1;

            if (vectorToFix.isEmpty())
                return valueBaseCost.getValue();
            
            //TODO: add additional check if analog data can be discretized
            return valueBaseCost.getValue();
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    @Override
    public RuleComparable<PatternVector, RuleFSMVector> processData() {
        try {
            int vectorId = conflictToResolve.getId();
            RuleFSMVector ruleToFix = conflictToResolve.getRuleToFix();
            PatternVector vectorToFix = ruleToFix.getRuleVectorById(vectorId);
            PatternVector vectorFull = ruleToFix.getFullRuleVectorById(vectorId);
            HashMap<String, RawDataChunk> rawData = ruleToFix.getAnalogDataById(vectorId);

            //Check if analog data has been used
            if (processCost() < 0)
                return null;

            StateVector stateVector = null;
            if (!vectorToFix.isEmpty())
                stateVector = vectorToFix.values().iterator().next();

            for (String signal: rawData.keySet()) {
                RawDataChunk curAnalogChunk = rawData.get(signal);

                //Discretize data not contained in state vector and add it to full vector
                if (stateVector == null || !stateVector.containsKey(signal)) {
                    //Separate analog data into two groups
                    //One before output state
                    //Another after output state
                    RawDataChunk dataChunkPre = new RawDataChunk();
                    RawDataChunk dataChunkPost = new RawDataChunk();

                    Double stateStart = ruleToFix.getTimeStampById(vectorId).getKey();

                    for (RawDataPoint curPoint: curAnalogChunk) {
                        if (curPoint.getTime() <= stateStart)
                            dataChunkPre.add(curPoint);
                        else
                            dataChunkPost.add(curPoint);
                    }

                    StateThresholds finalState = rawDataToState(signal, dataChunkPost);

                    StageDataRaw dataRaw = new StageDataRaw(dataChunkPre, signal, ControlType.INPUT);
                    List<IState> combinedStates = new ArrayList<>();

                    IDataProcessor<StageDataState> discretizationProcessor = new DiscretizeDataByNumStates(dataRaw);
                    if (discretizationProcessor.processCost() > 0) {
                        StageDataState states = discretizationProcessor.processData();

                        if (states == null)
                            return null;
                        //TODO: Magic
                        combinedStates.addAll(states.getStates());
                    }

                    combinedStates.add(finalState);
                    combinedStates.addAll(vectorFull.getStates());
                    vectorFull = new PatternVector(combinedStates, vectorFull.getId());
                    ruleToFix.setFullRuleVectorById(vectorId, vectorFull);
                    ruleToFix.resetRuleVectorById(vectorId);

                    return ruleToFix;
                }
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }


    private StateThresholds rawDataToState(String signal, RawDataChunk rawData) 
            throws ArrayIndexOutOfBoundsException, NullPointerException{
        Double startTime = rawData.get(0).getTime();
        Double endTime = rawData.get(rawData.size() - 1).getTime();

        Double minValue = rawData.stream()
                .min( (p1,p2) -> (Double.compare(p1.getValue(), p2.getValue())) )
                .get().getValue();

        Double maxValue = rawData.stream()
                .max( (p1,p2) -> (Double.compare(p1.getValue(), p2.getValue())) )
                .get().getValue();

        return new StateThresholds(signal, startTime, endTime, minValue, maxValue);
    }
}

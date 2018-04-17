package modelgen.processor.rulemining.conflictresolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelgen.data.pattern.PatternVector;
import modelgen.data.pattern.StateVector;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyInteger;
import modelgen.data.property.PropertyManager;
import modelgen.data.state.IState;
import modelgen.data.state.IStateTimeless;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.processor.rulemining.conflictdetection.ConflictComparable;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.shared.Logger;

public class ResolveByState extends DataProcessor<RuleComparable<PatternVector, RuleFSMVector>>
                            implements IDataProcessor<RuleComparable<PatternVector, RuleFSMVector>> {
    protected static final String PD_RESOLVE_COST_OUTPUT_STATE = PD_PREFIX + "RESOLVE_COST_OUTPUT_STATE";

    final private Integer RESOLVE_COST = 2;
    final private Integer RESOLVE_COST_OUTPUT_STATE = 10;

    protected PropertyInteger valueBaseCostOutputState;

    private ConflictComparable<PatternVector, RuleFSMVector> conflictToResolve;
    
    public ResolveByState() {
        this.conflictToResolve = null;
        
        name = "ResolveByState";

        ERROR_PREFIX = "DataProcessor: ResolveByState error.";
        DEBUG_PREFIX = "DataProcessor: ResolveByState debug.";

        valueBaseCost.setValue(RESOLVE_COST);

        valueBaseCostOutputState = new PropertyInteger(PD_RESOLVE_COST_OUTPUT_STATE);
        valueBaseCostOutputState.setValue(RESOLVE_COST_OUTPUT_STATE);
        
        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(valueBaseCost.getName(), valueBaseCostOutputState);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public ResolveByState(ConflictComparable<PatternVector, RuleFSMVector> conflict) {
        this();
        conflictToResolve = conflict;
    }

    @Override
    public double processCost() {
        try {
            int vectorId = conflictToResolve.getId();
            RuleFSMVector ruleToFix = conflictToResolve.getRuleToFix();
            PatternVector vectorToFix = ruleToFix.getRuleVectorById(vectorId);
            PatternVector vectorFull = ruleToFix.getFullRuleVectorById(vectorId);

            StateVector stateFull = vectorFull.values().iterator().next();

            //Check if full state vector contains any states
            if (stateFull.isEmpty())
                return -1;

            //If rule vector is empty then new states can be added
            if (vectorToFix.isEmpty()) {
                if (stateFull.size() == 1 && 
                        stateFull.containsKey(ruleToFix.getOutputState().getSignalName()))
                    return valueBaseCostOutputState.getValue();
                else
                    return valueBaseCost.getValue();
            }

            StateVector stateToFix = vectorToFix.values().iterator().next();

            //If all states from the full vector have been used
            if (stateFull.size() == stateToFix.size())
                return -1;

            //Find intersection of states
            Set<String> difference = new HashSet<String>(stateFull.keySet());
            difference.removeAll(stateToFix.keySet());

            //Adding output state into pattern has a higher base cost
            if (difference.contains(ruleToFix.getOutputState().getSignalName()) && difference.size() == 1)
                return valueBaseCostOutputState.getValue();

            return valueBaseCost.getValue();
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

            //Check if all states have been used
            StateVector stateFull = vectorFull.values().iterator().next();

            //Check if vector is empty
            if (vectorToFix.isEmpty()) {
                if (stateFull.isEmpty())
                    return null;
                
                List<Integer> vectorFullKeys = new ArrayList<Integer>(vectorFull.keySet());
                Integer stateFullId = Collections.max(vectorFullKeys);
                stateFull = vectorFull.get(stateFullId);
                IStateTimeless outputState = ruleToFix.getOutputState();

                for (IState curState: stateFull.values()) {
                    // Do not add output state as first into predicate vector
                    if (curState.getSignalName().equals(outputState.getSignalName()))
                        continue;

                    StateVector newVector = new StateVector(vectorId);
                    newVector.put(curState.getSignalName(), curState);
                    vectorToFix.put(stateFullId, newVector);
                    return ruleToFix;
                }

                for (IState curState: stateFull.values()) {
                    //only output state is present, find and add it
                    if (curState.getSignalName().equals(outputState.getSignalName())) {
                        StateVector newVector = new StateVector(vectorId);
                        newVector.put(curState.getSignalName(), curState);
                        vectorToFix.put(stateFullId, newVector);
                        return ruleToFix;
                    }
                }

                //Failed to add any states
                return null;
            }

            StateVector stateToFix = vectorToFix.values().iterator().next();

            if (stateFull.size() == stateToFix.size())
                return null;

            //Check if it is possible to add any states
            String stateName = null;

            Set<String> difference = new HashSet<String>(stateFull.keySet());
            difference.removeAll(stateToFix.keySet());

            for (String state: difference) {
                //Detect output state
                if (state.equals(ruleToFix.getOutputState().getSignalName())) {
                    //Use it only if it is the only state available
                    if (difference.size() == 1) {
                        stateName = state;
                        break;
                    } else
                        continue;
                }
                stateName = state;
            }

            if (stateName == null)
                return null;

            List<Integer> vectorToFixKeys = new ArrayList<Integer>(vectorToFix.keySet());
            Collections.sort(vectorToFixKeys);

            for (Integer key: vectorToFixKeys) {
                StateVector curVectorToFix = vectorToFix.get(key);
                StateVector curFullVector = vectorFull.get(key);
                IState eventToAdd = curFullVector.get(stateName);
                curVectorToFix.put(stateName, eventToAdd);
                vectorToFix.put(key, curVectorToFix);
            }
            
            return ruleToFix;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

}

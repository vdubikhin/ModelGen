package modelgen.processor.rulemining.conflictresolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelgen.data.pattern.PatternVector;
import modelgen.data.pattern.StateVector;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyManager;
import modelgen.data.state.IState;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.processor.rulemining.conflictdetection.ConflictComparable;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.shared.Logger;

public class ResolveByVector extends DataProcessor<RuleComparable<PatternVector, RuleFSMVector>>
                                    implements IDataProcessor<RuleComparable<PatternVector, RuleFSMVector>> {
    final private Integer RESOLVE_COST = 4;
    
    private ConflictComparable<PatternVector, RuleFSMVector> conflictToResolve;
    
    public ResolveByVector() {
        this.conflictToResolve = null;
        
        name = "ResolveByVector";
        
        ERROR_PREFIX = "DataProcessor: ResolveByVector error.";
        DEBUG_PREFIX = "DataProcessor: ResolveByVector debug.";
        
        valueBaseCost.setValue(RESOLVE_COST);
        
        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        
        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }
    
    public ResolveByVector(ConflictComparable<PatternVector, RuleFSMVector> conflict) {
    this();
        conflictToResolve = conflict;
    }

    @Override
    public int processCost() {
        try {
            int vectorId = conflictToResolve.getId();
            RuleFSMVector ruleToFix = conflictToResolve.getRuleToFix();
            PatternVector vectorToFix = ruleToFix.getRuleVectorById(vectorId);
            PatternVector vectorFull = ruleToFix.getFullRuleVectorById(vectorId);

            //Not possible to expand empty vectors
            if (vectorFull.isEmpty() || vectorToFix.isEmpty())
                return -1;

            //Quickly check if original predicate array can still be used
            if (vectorFull.size() == vectorToFix.size())
                return -1;

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
            
            //Not possible to expand empty vectors
            if (vectorFull.isEmpty() || vectorToFix.isEmpty())
                return null;
            
            //Quickly check if original predicate array can still be used
            if (vectorFull.size() == vectorToFix.size())
                return null;
            
            StateVector refVector = vectorToFix.values().iterator().next();
            
            List<Integer> vectorFullKeys = new ArrayList<Integer>(vectorFull.keySet().size());
            
            for (Integer key: vectorFull.keySet())
                vectorFullKeys.add(key);
            
            Collections.sort(vectorFullKeys);
            Collections.reverse(vectorFullKeys);
            
            //Find first key mismatch and add state vector 
            for (Integer key: vectorFullKeys) {
                if (!vectorToFix.containsKey(key)) {
                    StateVector addVector = new StateVector(vectorId);
                    StateVector fullStateVector = vectorFull.get(key);
                    for (String nameVector: refVector.keySet()) {
                        IState eventToAdd = fullStateVector.get(nameVector);
                        addVector.put(nameVector, eventToAdd);
                    }
                    vectorToFix.put(key, addVector);
                    return ruleToFix;
                }
            }
            
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

}

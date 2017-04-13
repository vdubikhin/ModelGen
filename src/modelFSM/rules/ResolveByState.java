package modelFSM.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelFSM.data.Event;
import modelFSM.rules.data.PredicateVector;
import modelFSM.rules.data.StateVector;

class ResolveByState< V extends RuleComparable<PredicateVector, V > > implements ConflictResolver<PredicateVector, V> {

    int cost;
    
    public ResolveByState(int cost) {
        this.cost = cost;
    }

    @Override
    public boolean ResolveConflict(ConflictComparable<PredicateVector, V> conflictToResolve, List<V> ruleList) {
        try {
            int vectorId = conflictToResolve.getId();
            V ruleToFix = conflictToResolve.getRuleToFix();
            PredicateVector vectorToFix = ruleToFix.getRuleVectorById(vectorId);
            PredicateVector vectorFull = ruleToFix.getFullRuleVectorById(vectorId);
            
            //Check if all states have been used
            StateVector stateFull = vectorFull.values().iterator().next();

            //Check if vector is empty
            if (vectorToFix.isEmpty()) {
                if (stateFull.isEmpty())
                    return false;
                
                List<Integer> vectorFullKeys = new ArrayList<Integer>(vectorFull.keySet());
                Integer stateFullId = Collections.max(vectorFullKeys);
                stateFull = vectorFull.get(stateFullId);
                Event outputState = ruleToFix.getOutputState();
                
                for (Event curEvent: stateFull.values()) {
                    // Do not add output state as first into predicate vector
                    if (curEvent.signalName.equals(outputState.signalName))
                        continue;
                    
                    StateVector newVector = new StateVector(vectorId);
                    newVector.put(curEvent.signalName, curEvent);
                    vectorToFix.put(0, newVector);
                    return true;
                }
                
                for (Event curEvent: stateFull.values()) {
                    //only output state is present, find and add it
                    if (curEvent.signalName.equals(outputState.signalName)) {
                        StateVector newVector = new StateVector(vectorId);
                        newVector.put(curEvent.signalName, curEvent);
                        vectorToFix.put(0, newVector);
                        return true;
                    }
                }
                
                return false;
            }
            
            StateVector stateToFix = vectorToFix.values().iterator().next();

            if (stateFull.size() == stateToFix.size())
                return false;
            
            //Check if it is possible to add any states
            String stateName = null;
            
            for (String state: stateFull.keySet()) {
                if (!stateToFix.containsKey(state)) {
                    stateName = state;
                    break;
                }
            }
            
            if (stateName == null)
                return false;
            
            List<Integer> vectorToFixKeys = new ArrayList<Integer>(vectorToFix.keySet());
            Collections.sort(vectorToFixKeys);
            
            for (Integer key: vectorToFixKeys) {
                StateVector curVectorToFix = vectorToFix.get(key);
                StateVector curFullVector = vectorFull.get(key);
                Event eventToAdd = curFullVector.get(stateName);
                curVectorToFix.put(stateName, eventToAdd);
                vectorToFix.put(key, curVectorToFix);
            }
            
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Integer ResolveCost(ConflictComparable<PredicateVector, V> conflictToResolve) {
        return cost;
    }

}

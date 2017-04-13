package modelFSM.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelFSM.data.Event;
import modelFSM.rules.data.PredicateVector;
import modelFSM.rules.data.StateVector;

class ResolveByVector< V extends RuleComparable<PredicateVector, V > > implements ConflictResolver<PredicateVector, V> {

    int cost;
    
    public ResolveByVector(int cost) {
        this.cost = cost;
    }

    @Override
    public boolean ResolveConflict(ConflictComparable<PredicateVector, V> conflictToResolve, List<V> ruleList) {
        try {
            int vectorId = conflictToResolve.getId();
            V ruleToFix = conflictToResolve.getRuleToFix();
            PredicateVector vectorToFix = ruleToFix.getRuleVectorById(vectorId);
            PredicateVector vectorFull = ruleToFix.getFullRuleVectorById(vectorId);
            
            //Not possible to expand empty vectors
            if (vectorFull.isEmpty() || vectorToFix.isEmpty())
                return false;
            
            //Quickly check if original predicate array can still be used
            if (vectorFull.size() == vectorToFix.size())
                return false;
            
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
                        Event eventToAdd = fullStateVector.get(nameVector);
                        addVector.put(nameVector, eventToAdd);
                    }
                    vectorToFix.put(key, addVector);
                    return true;
                }
            }
            
            return false;
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

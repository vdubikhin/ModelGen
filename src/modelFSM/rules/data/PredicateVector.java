package modelFSM.rules.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import modelFSM.data.Event;
import modelFSM.data.Predicate;

public class PredicateVector extends HashMap<Integer, StateVector> implements VectorComparable<PredicateVector> {
    private static final long serialVersionUID = -4979363811171056551L;
    private final int vectorId;
    public boolean flagged; //means vector is unique and cmp by vectorId
    
    public PredicateVector(int id) {
        super();
        flagged = false;
        vectorId = id;
    }
    
    public PredicateVector(PredicateVector copy) {
        super(copy);
        flagged = copy.flagged;
        vectorId = copy.vectorId;
    }
    
    
    public PredicateVector(Predicate statePredicate) {
        super();
        vectorId = statePredicate.getId();
        flagged = false;
        
        StateVector curVector = new StateVector(statePredicate.getId());
        
        for (Event curEvent:  statePredicate) {
            if (curVector.containsKey(curEvent.signalName))
                continue;
            else
                curVector.put(curEvent.signalName, curEvent);
        }
        
        Integer stateNum = 0;
        put(stateNum++, curVector);
        
        for (Event curEvent:  statePredicate) {
            String signalName = curEvent.signalName;
            int eventId = curEvent.eventId;
            if (curVector.get(signalName).eventId != eventId) {
                curVector = new StateVector(curVector);
                curVector.put(signalName, curEvent);
                put(stateNum++, curVector);
            }
        }
        
    }
    @Override
    public int getId() {return vectorId;}

    // Determine if vectorCmp is a subset, superset or unique relative to this vector
    // Subset => Object states are a subset of vectorCmp(object states form a sequence in comparable vector states)
    // Superset => Object states are a superset of vectorCmp(vector states form a sequence in object states)
    // Unique => both vectors are unique and can be distinguished
    @Override
    public VectorEquality compareTo(PredicateVector vectorCmp) {
        try {
            PredicateVector vectorLarge;
            PredicateVector vectorSmall;
            VectorEquality cmpResult;
            
            if (flagged || vectorCmp.flagged) {
                return VectorEquality.UNIQUE;
            }
            
            // TODO: add size check
            
            if (this.size() > vectorCmp.size() && !this.values().iterator().next().isEmpty()) {
                vectorLarge = this;
                vectorSmall = vectorCmp;
                cmpResult = VectorEquality.SUPERSET;
            } else {
                vectorLarge = vectorCmp;
                vectorSmall = this;
                cmpResult = VectorEquality.SUBSET;
            }
            
            int largeVectorIter = 0;
            int numEq = 0;
            
            List<Integer> vectorSmallKeys = new ArrayList<Integer>(vectorSmall.keySet());
            Collections.sort(vectorSmallKeys);
            
            List<Integer> vectorLargeKeys = new ArrayList<Integer>(vectorLarge.keySet());
            Collections.sort(vectorLargeKeys);

            int i = 0;

            for (Integer smallVectorKey: vectorSmallKeys) {
                StateVector smallVectorState = vectorSmall.get(smallVectorKey);
                // Check if we have not iterated over all large vector states
                if (largeVectorIter >= vectorLargeKeys.size() && !smallVectorState.isEmpty()) {
                    cmpResult = VectorEquality.UNIQUE;
                    break;
                }
                
                
                for (i = largeVectorIter; i < vectorLargeKeys.size(); i++) {
                    StateVector largeVectorState = vectorLarge.get(vectorLargeKeys.get(i));
                    VectorEquality cmpVectorResult = smallVectorState.compareTo(largeVectorState); 
                    if (cmpVectorResult == VectorEquality.EQUAL) {
                        numEq += 1;
                        break;
                    }
                    
                    if (cmpVectorResult == VectorEquality.SUPERSET || cmpVectorResult == VectorEquality.SUBSET) {
                        cmpResult = cmpVectorResult;
                        numEq += 1;
                        break;
                    }
                }
                
                largeVectorIter = i + 1; // TODO: double check i+1
            }
            
            if (numEq < vectorSmall.size() && !vectorSmall.values().iterator().next().isEmpty())
                cmpResult = VectorEquality.UNIQUE;
            
            return cmpResult;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        
        return VectorEquality.UNIQUE;
    }
}

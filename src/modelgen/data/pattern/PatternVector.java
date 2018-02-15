package modelgen.data.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import modelgen.data.complex.ComplexComparable;
import modelgen.data.state.IState;

public class PatternVector extends HashMap<Integer, StateVector> implements ComplexComparable<PatternVector> {
    private static final long serialVersionUID = -4979363811171056551L;
    private final int vectorId;
    private boolean unique; //means vector is unique and cmp by vectorId
    
    public PatternVector(int id) {
        super();
        setUnique(false);
        vectorId = id;
    }
    
    public PatternVector(PatternVector copy) {
        super(copy);
        setUnique(copy.isUnique());
        vectorId = copy.vectorId;
    }

    public PatternVector(List<IState> states, int id) {
        super();
        vectorId = id;
        setUnique(false);

        //Sort list of states by their start time
        states.sort((s1, s2) -> Double.compare(s1.getTimeStamp().getKey(), s2.getTimeStamp().getKey()));

        StateVector curVector = new StateVector(id);

        for (IState curState: states) {
            if (curVector.containsKey(curState.getSignalName()))
                continue;
            else
                curVector.put(curState.getSignalName(), curState);
        }
        
        Integer stateNum = 0;
        put(stateNum++, curVector);
        
        for (IState curState: states) {
            String signalName = curState.getSignalName();
            int eventId = curState.getId();
            if (curVector.get(signalName).getId() != eventId) {
                curVector = new StateVector(curVector);
                curVector.put(signalName, curState);
                put(stateNum++, curVector);
            }
        }
        
    }

    @Override
    public Integer getId() {return vectorId;}

    // Determine if vectorCmp is a subset, superset or unique relative to this vector
    // Subset => Object states are a subset of vectorCmp(object states form a sequence in comparable vector states)
    // Superset => Object states are a superset of vectorCmp(vector states form a sequence in object states)
    // Unique => both vectors are unique and can be distinguished
    @Override
    public DataEquality compareTo(PatternVector vectorCmp) {
        try {
            PatternVector vectorLarge;
            PatternVector vectorSmall;
            DataEquality cmpResult;
            
            if (isUnique() || vectorCmp.isUnique()) {
                return DataEquality.UNIQUE;
            }
            
            // TODO: add size check
            
            if (this.size() > vectorCmp.size() && !this.values().iterator().next().isEmpty()) {
                vectorLarge = this;
                vectorSmall = vectorCmp;
                cmpResult = DataEquality.SUPERSET;
            } else {
                vectorLarge = vectorCmp;
                vectorSmall = this;
                cmpResult = DataEquality.SUBSET;
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
                    cmpResult = DataEquality.UNIQUE;
                    break;
                }
                
                
                for (i = largeVectorIter; i < vectorLargeKeys.size(); i++) {
                    StateVector largeVectorState = vectorLarge.get(vectorLargeKeys.get(i));
                    DataEquality cmpVectorResult = smallVectorState.compareTo(largeVectorState); 
                    if (cmpVectorResult == DataEquality.EQUAL) {
                        numEq += 1;
                        break;
                    }
                    
                    if (cmpVectorResult == DataEquality.SUPERSET || cmpVectorResult == DataEquality.SUBSET) {
                        cmpResult = cmpVectorResult;
                        numEq += 1;
                        break;
                    }
                }
                
                largeVectorIter = i + 1; // TODO: double check i+1
            }
            
            if (numEq < vectorSmall.size() && !vectorSmall.values().iterator().next().isEmpty())
                cmpResult = DataEquality.UNIQUE;
            
            return cmpResult;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        
        return DataEquality.UNIQUE;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean flagged) {
        this.unique = flagged;
    }

    public List<IState> getStates() {
        class IStateWrapper {
            IState state;

            IStateWrapper(IState inputState) {state = inputState;}

            @Override
            public boolean equals(Object objectCmp) {
                if (!(objectCmp instanceof IStateWrapper)) return false;
                IState stateCmp = ((IStateWrapper) objectCmp).state;

                DataEquality cmpResult = state.compareTo(stateCmp);
                if (cmpResult == null || cmpResult != DataEquality.EQUAL)
                    return false;

                return true;
            }
        }

        List<IStateWrapper> filteredStates = new ArrayList<> ();
        for (StateVector stateVector: values()) {
             for (IState state: stateVector.values()) {
                 filteredStates.add(new IStateWrapper(state));
             }
        }

        List<IState> output = filteredStates.stream()
            .distinct()
            .map((s) -> (s.state))
            .collect(Collectors.toList());

        if (!output.isEmpty())
            return output;

        return null;
    }
}

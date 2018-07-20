package modelgen.data.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import modelgen.data.complex.ComplexComparable;
import modelgen.data.state.IState;

//TODO: change to treemap and remove unnecessary sorting of keys
public class PatternVector extends HashMap<Integer, StateVector> implements ComplexComparable<PatternVector> {
    private static final long serialVersionUID = -4979363811171056551L;
    private final int vectorId;
    private Set<Integer> preSet;
    private Set<Integer> postSet;

    public PatternVector(int id) {
        super();
        preSet = new HashSet<>();
        postSet = new HashSet<>();
        vectorId = id;
    }
    
    public PatternVector(PatternVector copy) {
        super(copy);
        preSet = new HashSet<>(copy.preSet);
        postSet = new HashSet<>(copy.postSet);
        vectorId = copy.vectorId;
    }

    public PatternVector(List<IState> states, int id) {
        this(id);

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

            //Check that both vectors have a dependency on each other
            if ( (preSet.contains(vectorCmp.getId()) || postSet.contains(vectorCmp.getId())) &&
                    (vectorCmp.preSet.contains(getId()) || vectorCmp.postSet.contains(getId())) )
                return DataEquality.UNIQUE;

            if (this.size() > vectorCmp.size() && !this.isEmpty() && !this.values().iterator().next().isEmpty()) {
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
            int numIdentical = 0;

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
                    if (cmpVectorResult != DataEquality.UNIQUE) {
                        //Small pattern vector has more states, so large vector needs to be expanded
                        if (cmpVectorResult == DataEquality.SUPERSET)
                            if (vectorSmall.equals(this))
                                cmpResult = DataEquality.SUPERSET;
                            else
                                cmpResult = DataEquality.SUBSET;
                        
                        if (cmpVectorResult == DataEquality.EQUAL)
                            numIdentical += 1;

                        numEq += 1;
                        break;
                    }
                }

                largeVectorIter = i + 1; // TODO: double check i+1
            }

            if (numEq < vectorSmall.size() && !vectorSmall.values().iterator().next().isEmpty())
                cmpResult = DataEquality.UNIQUE;

            //Additional check to see if two vectors are exactly similar
            if (numIdentical == vectorSmall.size() && vectorSmall.size() == vectorLarge.size()) 
                return DataEquality.EQUAL;


            return cmpResult;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        
        return DataEquality.UNIQUE;
    }

    public boolean hasDependency(Integer id) {
        if ( preSet.contains(id) || postSet.contains(id) )
                return true;

        return false;
    }

    public void addDependency(Integer id) {
        if (getId() > id)
            preSet.add(id);

        if (getId() < id)
            postSet.add(id);
    }

    public List<IState> getStates() {
        class IStateWrapper {
            IState state;

            IStateWrapper(IState inputState) {state = inputState;}

            @Override
            public int hashCode() {
                return state.hashCode();
            }

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

    public Set<Integer> getPreSet() {
        return preSet;
    }

    public Set<Integer> getPostSet() {
        return postSet;
    }

    public Double getStartTime() {
        if (isEmpty())
            return null;

        List<Integer> vectorKeys = new ArrayList<Integer>(keySet());
        Collections.sort(vectorKeys);

        return get(vectorKeys.get(0)).getStartTime();
    }

    public Double getEndTime() {
        if (isEmpty())
            return null;

        List<Integer> vectorKeys = new ArrayList<Integer>(keySet());
        Collections.sort(vectorKeys);

        return get(vectorKeys.get(vectorKeys.size() - 1)).getStartTime();
    }
}

package modelgen.data.pattern;

import java.util.HashMap;

import modelgen.data.complex.ComplexComparable;
import modelgen.data.state.IState;


public class StateVector extends HashMap<String, IState> implements ComplexComparable<StateVector>{
    private static final long serialVersionUID = -6169185059255157164L;
    private final int vectorId;
    
    public StateVector(int id) {
        super();
        vectorId = id;
    }
    
    public StateVector(StateVector copy) {
        super(copy);
        vectorId = copy.vectorId;
    }
    
    public Double getStartTime() {
        Double startTime = null;
        for (IState state: values()) {
            Double stateStart = state.getTimeStamp().getKey();
            if (startTime == null) {
                startTime = stateStart;
                continue;
            } else {
                startTime = Math.max(startTime, stateStart);
            }
        }

        return startTime;
    }

    @Override
    public Integer getId() {return vectorId;}

    @Override
    public DataEquality compareTo(StateVector stateVectroCmp) {
        for (String signalName: this.keySet()) {
            // Null state means dont care. 
            if (this.get(signalName) == null || stateVectroCmp.get(signalName) == null)
                continue;
            
            if (this.get(signalName).compareTo(stateVectroCmp.get(signalName)) == DataEquality.UNIQUE)
                return DataEquality.UNIQUE;
        }

        for (String signalName: stateVectroCmp.keySet()) {
            // Null state means dont care. 
            if (this.get(signalName) == null || stateVectroCmp.get(signalName) == null)
                continue;
            
            if (this.get(signalName).compareTo(stateVectroCmp.get(signalName)) == DataEquality.UNIQUE)
                return DataEquality.UNIQUE;
        }

        if (this.keySet().equals(stateVectroCmp.keySet()))
            return DataEquality.EQUAL;

        if (this.keySet().size() > stateVectroCmp.keySet().size()) {
            if (this.keySet().containsAll(stateVectroCmp.keySet()))
                return DataEquality.SUPERSET;
            else
                return DataEquality.UNIQUE;
        } else {
            if (stateVectroCmp.keySet().containsAll(this.keySet()))
                return DataEquality.SUBSET;
            else
                return DataEquality.UNIQUE;
        }
    }
    
}

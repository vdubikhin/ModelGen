package modelFSM.rules.data;

import java.util.HashMap;

import modelFSM.data.Event;

public class StateVector extends HashMap<String, Event> implements VectorComparable<StateVector>{
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
    
    @Override
    public int getId() {return vectorId;}
    
    @Override
    public VectorEquality compareTo(StateVector stateVectroCmp) {
        for (String signalName: this.keySet()) {
            // Null state means dont care. 
            if (this.get(signalName) == null || stateVectroCmp.get(signalName) == null)
                continue;
            
            if (this.get(signalName).eventId != stateVectroCmp.get(signalName).eventId)
                return VectorEquality.UNIQUE;
        }
        
        for (String signalName: stateVectroCmp.keySet()) {
            // Null state means dont care. 
            if (this.get(signalName) == null || stateVectroCmp.get(signalName) == null)
                continue;
            
            if (this.get(signalName).eventId != stateVectroCmp.get(signalName).eventId)
                return VectorEquality.UNIQUE;
        }
        
        if (this.keySet().equals(stateVectroCmp.keySet()))
            return VectorEquality.EQUAL;
        
        if (this.keySet().size() > stateVectroCmp.keySet().size()) {
            if (this.keySet().containsAll(stateVectroCmp.keySet()))
                return VectorEquality.SUPERSET;
            else
                return VectorEquality.UNIQUE;
        } else {
            if (stateVectroCmp.keySet().containsAll(this.keySet()))
                return VectorEquality.SUBSET;
            else
                return VectorEquality.UNIQUE;
        }
    }
    
}

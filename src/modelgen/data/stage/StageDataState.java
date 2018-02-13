package modelgen.data.stage;

import java.util.List;

import modelgen.data.ControlType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.state.IState;

public class StageDataState {
    private RawDataChunkGrouped data;
    private ControlType dataType;
    private String name;
    private List<IState> states;

    public StageDataState(RawDataChunkGrouped data, String name, ControlType dataType, List<IState> states) {
        this.data = data;
        this.name = name;
        this.dataType = dataType;
        this.states = states;
    }
    
    public RawDataChunkGrouped getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public ControlType getType() {
        return dataType;
    }
    
    public List<IState> getStates() {
        return states;
    }
}

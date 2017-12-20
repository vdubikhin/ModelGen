package modelFSM.discretization;

import java.util.List;

import modelFSM.data.DataType;
import modelFSM.data.RawDataChunk;
import modelgen.data.state.IState;

public interface DataDiscretizer {
    public boolean canDiscretizeData();
    
    public List<IState> discretizeData();
}

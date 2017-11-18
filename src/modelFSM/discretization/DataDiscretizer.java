package modelFSM.discretization;

import java.util.List;

import modelFSM.data.DataType;
import modelFSM.data.RawDataChunk;
import modelFSM.data.event.EventInfo;

public interface DataDiscretizer {
    public boolean canDiscretizeData();
    
    public List<EventInfo> discretizeData();
}

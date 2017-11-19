package modelFSM.discretization;

import java.util.List;

import modelFSM.data.RawDataChunkGrouped;
import modelFSM.data.event.EventInfo;

class DiscretizeDataByValues implements DataDiscretizer{

    private RawDataChunkGrouped groupedData;
    
    public DiscretizeDataByValues(RawDataChunkGrouped groupedData) {
        this.groupedData = groupedData;
    }
    
    @Override
    public boolean canDiscretizeData() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<EventInfo> discretizeData() {
        // TODO Auto-generated method stub
        return null;
    }

}

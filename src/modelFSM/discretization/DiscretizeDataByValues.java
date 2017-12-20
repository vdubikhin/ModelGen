package modelFSM.discretization;

import java.util.List;

import modelFSM.data.RawDataChunkGrouped;
import modelgen.data.state.IState;

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
    public List<IState> discretizeData() {
        // TODO Auto-generated method stub
        return null;
    }

}

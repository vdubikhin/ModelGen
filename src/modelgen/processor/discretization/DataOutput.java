package modelgen.processor.discretization;

import java.util.List;

import modelgen.data.ControlType;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.state.IState;

public class DataOutput {
    public RawDataChunkGrouped data;
    public ControlType dataType;
    public String name;
    public List<IState> states;
}

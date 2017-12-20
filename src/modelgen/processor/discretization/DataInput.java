package modelgen.processor.discretization;

import modelgen.data.ControlType;
import modelgen.data.raw.RawDataChunk;

public class DataInput {
    private RawDataChunk data;
    private String name;
    private ControlType type;

    public DataInput(RawDataChunk data, String name, ControlType type) {
        this.data = data;
        this.name = name;
        this.type = type;
    }

    public RawDataChunk getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public ControlType getType() {
        return type;
    }
}

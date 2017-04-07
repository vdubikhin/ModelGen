package modelFSM.data;

import java.util.ArrayList;

public class RawDataChunk extends ArrayList<RawDataPoint> {
    private static final long serialVersionUID = 1L;

    public RawDataChunk() {
        super();
    }

    public RawDataChunk(ArrayList<RawDataPoint> toCopy) {
        super(toCopy);
    }
}

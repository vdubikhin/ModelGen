package modelgen.processor.discretization;

import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.state.IState;

interface IPointDescretizer {
    IState createState(RawDataChunk data, int index)
            throws ArrayIndexOutOfBoundsException, NullPointerException;

    RawDataPointGrouped createGroupedPoint(RawDataChunk data, int index)
            throws ArrayIndexOutOfBoundsException, NullPointerException;
}

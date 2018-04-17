package modelgen.processor.discretization;

import modelgen.data.raw.RawDataChunk;

abstract class APointDescretizer implements IPointDescretizer {
    protected Double getPointStart(RawDataChunk data, int index)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        return data.get(index).getTime();
    }

    protected Double getPointEnd(RawDataChunk data, int index)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        index = Math.min(index + 1, data.size() - 1);
        return data.get(index).getTime();
    }
}

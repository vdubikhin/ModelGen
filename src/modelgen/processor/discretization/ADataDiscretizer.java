package modelgen.processor.discretization;

import java.util.ArrayList;
import java.util.List;

import modelgen.data.ControlType;
import modelgen.data.complex.Mergeable;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;

abstract class ADataDiscretizer extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {

    protected StageDataState createStageData(RawDataChunk inputData, IPointDescretizer pointDescretizer,
            String name, ControlType type) throws NullPointerException, ArrayIndexOutOfBoundsException {
        RawDataChunkGrouped groupedData = new RawDataChunkGrouped();
        List<IState> outputStates = new ArrayList<>();
        for (int i = 0; i < inputData.size(); i++) {
            RawDataPointGrouped groupedPoint = pointDescretizer.createGroupedPoint(inputData, i);
            IState state = pointDescretizer.createState(inputData, i);

            if (groupedPoint == null || state == null)
                return null;

            groupedData.add(groupedPoint);
            outputStates.add(state);
        }

        Mergeable.mergeEntries(outputStates);

        StageDataState result = new StageDataState(groupedData, name, type, outputStates);
        return result;
    }
}

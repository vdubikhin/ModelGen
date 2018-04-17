package modelgen.processor.discretization;

import modelgen.data.ControlType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.stage.StageDataRaw;
import modelgen.data.stage.StageDataState;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.shared.Logger;

public class DiscretizeDataDummy extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {
    private RawDataChunk inputData;
    private ControlType inputType;
    private String inputName;

    public DiscretizeDataDummy() {
        this.inputData = null;
        
        name = "Dummy";

        ERROR_PREFIX = "DataProcessor: DiscretizeDataDummy error.";
        DEBUG_PREFIX = "DataProcessor: DiscretizeDataDummy debug.";
    }

    public DiscretizeDataDummy(StageDataRaw inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
    }
    
    @Override
    public double processCost() {
        if (inputData == null || inputType == ControlType.OUTPUT)
            return -1;

        return Double.MAX_VALUE;
    }

    @Override
    public StageDataState processData() {
        try {
            if (inputData == null || inputType == ControlType.OUTPUT)
                return null;

            RawDataChunkGrouped groupedData = new RawDataChunkGrouped();
            for (int i = 0; i < inputData.size(); i++) {
                RawDataPoint point = inputData.get(i);
                Integer pointGroup = -1;
                RawDataPointGrouped groupedPoint = new RawDataPointGrouped(point, pointGroup);
                groupedData.add(groupedPoint);
            }
            
            StageDataState result = new StageDataState(groupedData, inputName, inputType, null);
            return result;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

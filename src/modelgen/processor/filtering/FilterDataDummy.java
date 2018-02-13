package modelgen.processor.filtering;

import modelgen.data.stage.StageDataState;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;

public class FilterDataDummy extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {
    StageDataState data;

    public FilterDataDummy() {
        ERROR_PREFIX = "DataProcessor: FilterDataDummy error. ";
        DEBUG_PREFIX = "DataProcessor: FilterDataDummy debug. ";

        name = "FilterDataDummy";
    }
    
    public FilterDataDummy(StageDataState inputData) {
        this();
        data = inputData;
    }

    @Override
    public int processCost() {
        return Integer.MAX_VALUE;
    }

    @Override
    public StageDataState processData() {
        return data;
    }
}

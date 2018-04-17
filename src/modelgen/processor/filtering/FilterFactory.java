package modelgen.processor.filtering;

import modelgen.data.stage.StageDataState;
import modelgen.processor.DataProcessorFactory;
import modelgen.processor.IDataProcessorFactory;
import modelgen.shared.Logger;

public class FilterFactory extends DataProcessorFactory<StageDataState, StageDataState>
                           implements IDataProcessorFactory<StageDataState, StageDataState> {
    public FilterFactory() {
        try {
            processorClasses.put(FilterDataByDurationCluster.class.newInstance().getName(), FilterDataByDurationCluster.class);
            processorClasses.put(FilterDataByPattern.class.newInstance().getName(), FilterDataByPattern.class);
            processorClasses.put(FilterDataDummy.class.newInstance().getName(), FilterDataDummy.class);
            inputDataClass = StageDataState.class;
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Failed to create processor factory.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
    }
}

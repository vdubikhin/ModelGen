package modelgen.processor.filtering;

import modelgen.processor.DataProcessorFactory;
import modelgen.processor.IDataProcessorFactory;
import modelgen.processor.discretization.DataOutput;
import modelgen.shared.Logger;

public class FilterFactory extends DataProcessorFactory<DataOutput, DataOutput>
                           implements IDataProcessorFactory<DataOutput, DataOutput> {
    public FilterFactory() {
        try {
            processorClasses.put(FilterDataByDurationCluster.class.newInstance().getName(), FilterDataByDurationCluster.class);
            inputDataClass = DataOutput.class;
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Failed to create processor factory.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
    }
}

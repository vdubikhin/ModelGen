package modelgen.processor.discretization;

import modelgen.processor.DataProcessorFactory;
import modelgen.processor.IDataProcessorFactory;
import modelgen.shared.Logger;

public class DiscretizerFactory extends DataProcessorFactory<DataInput, DataOutput>
                                implements IDataProcessorFactory<DataInput, DataOutput> {
    public DiscretizerFactory() {
        super();
        try {
            processorClasses.put(DiscretizeDataByValues.class.newInstance().getName(), DiscretizeDataByValues.class);
            inputDataClass = DataInput.class;
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Failed to create processor factory.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
    }

}

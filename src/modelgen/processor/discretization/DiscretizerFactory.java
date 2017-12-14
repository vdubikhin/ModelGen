package modelgen.processor.discretization;

import modelgen.processor.DataProcessorFactory;
import modelgen.processor.IDataProcessor;

public class DiscretizerFactory extends DataProcessorFactory<DataInput, DataOutput> {
    protected final String[] HANDLER_NAMES_ARRAY = {"ValueType", "Stability"};
    
    public DiscretizerFactory() {
        super();
    }

    @Override
    public IDataProcessor<DataOutput> createDataProcessor(String name, DataInput data) {
        // TODO Auto-generated method stub
        return null;
    }
    
}

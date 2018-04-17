package modelgen.manager;


import java.util.Map;

import modelgen.data.property.PropertySettable;
import modelgen.processor.IDataProcessorFactory;

public interface IDataManager<I, O> extends PropertySettable {
    Map.Entry<O, Double> processData(I inputData, IDataProcessorFactory<I, O> processorFactory);
}

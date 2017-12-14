package modelgen.manager;


import java.util.Map;

import modelgen.data.property.Properties;
import modelgen.processor.IDataProcessorFactory;

public interface IDataManager<I, O> {
    boolean setManagerProperties(Properties properties);

    Properties getManagerProperties();

    Map.Entry<O, Integer> processData(I inputData, IDataProcessorFactory<I, O> processorFactory);
}

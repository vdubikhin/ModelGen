package modelgen.processor;

import modelgen.data.property.Properties;

public interface IDataProcessor<T> {
    boolean setProcessorProperties(Properties properties);

    Properties getProcessorProperties();

    String getName();

    int processCost();

    T processData();
}

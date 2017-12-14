package modelgen.processor;

import java.util.Map;
import java.util.Set;

import modelgen.data.property.Properties;

public interface IDataProcessorFactory<I, O> {

    Set<String> getProcessorNames();

    IDataProcessor<O> createDataProcessor(String name, I data);

    boolean setProcessorProperties(Map<String, Properties> properties);

    Map<String, Properties> getProcessorProperties();
}

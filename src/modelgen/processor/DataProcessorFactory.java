package modelgen.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import modelgen.data.property.Properties;
import modelgen.shared.Util;

public abstract class DataProcessorFactory<T, S>  implements IDataProcessorFactory<T, S>{
    final protected String ERROR_PREFIX = "DataProcessorFactory error.";
    final protected String DEBUG_PREFIX = "DataProcessorFactory debug.";

    final protected String[] HANDLER_NAMES_ARRAY = {""};

    protected final Set<String> handlerNames;
    @SuppressWarnings("unused")
    private Map<String, Properties> processorsProperties;

    public DataProcessorFactory() {
        handlerNames = new HashSet<>(Arrays.asList(HANDLER_NAMES_ARRAY));
    }

    @Override
    public Set<String> getProcessorNames() {
        return handlerNames;
    }

    @Override
    public boolean setProcessorProperties(Map<String, Properties> properties) {
            try {
                if (properties == null) {
                    Util.errorLogger(ERROR_PREFIX + " Input property map not initialized.");
                    return false;
                }

                if (handlerNames == null) {
                    Util.errorLogger(ERROR_PREFIX + " Data processors names set not initialized.");
                    return false;
                }

                boolean success = true;
                //Test properties using empty data processors
                for (String propertyName: properties.keySet() ) {
                    if (handlerNames.contains(propertyName)) {
                        IDataProcessor<S> processor = createEmptyDataProcessor(propertyName);
                        Properties processorProperties = properties.get(propertyName);
                        if (!processor.setProcessorProperties(processorProperties)) {
                            Util.errorLogger(ERROR_PREFIX + " Failure to set properties for processor " + propertyName + ".");
                            success = false;
                        }
                    }
                }

                if (success) {
                    processorsProperties = properties;
                    return true;
                }
            } catch (NullPointerException e) {
                Util.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
            }
            return false;
        }

    @Override
    public Map<String, Properties> getProcessorProperties() {
        try {
            //TODO: think if existing processorsProperties should override default
            Map<String, Properties> properties = new HashMap<String, Properties>();

            for (String propertyName: handlerNames) {
                if (handlerNames.contains(propertyName)) {
                    IDataProcessor<S> processor = createEmptyDataProcessor(propertyName);
                    Properties processorProperties = processor.getProcessorProperties();
                    properties.put(propertyName, processorProperties);
                }
            }

            return properties;
        } catch (NullPointerException e) {
            Util.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    private IDataProcessor<S> createEmptyDataProcessor(String name) {
        return createDataProcessor(name, null);
    }

    @Override
    abstract public IDataProcessor<S> createDataProcessor(String name, T data);
}
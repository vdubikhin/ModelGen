package modelgen.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import modelgen.data.property.Properties;
import modelgen.shared.Logger;

public abstract class DataProcessorFactory<T, S> implements IDataProcessorFactory<T, S> {
    final protected String ERROR_PREFIX = "DataProcessorFactory error.";
    final protected String DEBUG_PREFIX = "DataProcessorFactory debug.";


    protected Map<String, Properties> processorsProperties;
    protected Map<String, Class<? extends IDataProcessor<S>>> processorClasses;
    protected Class<T> inputDataClass;

    public DataProcessorFactory() {
        processorClasses = new HashMap<>();
    }

    @Override
    public Set<String> getProcessorNames() {
        return processorClasses.keySet();
    }

    @Override
    public boolean setProcessorProperties(Map<String, Properties> properties) {
        try {
            if (properties == null) {
                Logger.errorLogger(ERROR_PREFIX + " Input property map not initialized.");
                return false;
            }

            if (processorClasses == null) {
                Logger.errorLogger(ERROR_PREFIX + " Data processors not initialized.");
                return false;
            }

            boolean success = true;
            //Test properties using empty data processors
            for (String propertyName: properties.keySet() ) {
                if (processorClasses.containsKey(propertyName)) {
                    IDataProcessor<S> processor = createEmptyDataProcessor(propertyName);
                    Properties processorProperties = properties.get(propertyName);
                    if (!processor.setModuleProperties(processorProperties)) {
                        Logger.errorLogger(ERROR_PREFIX + " Failure to set properties for processor " + propertyName + ".");
                        success = false;
                    }
                }
            }

            if (success) {
                processorsProperties = properties;
                return true;
            }
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public Map<String, Properties> getProcessorProperties() {
        try {
            //TODO: think if existing processorsProperties should override default
            Map<String, Properties> properties = new HashMap<String, Properties>();

            for (String propertyName: processorClasses.keySet()) {
                IDataProcessor<S> processor = createEmptyDataProcessor(propertyName);
                Properties processorProperties = processor.getModuleProperties();
                properties.put(propertyName, processorProperties);
            }

            return properties;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    private IDataProcessor<S> createEmptyDataProcessor(String name) {
        return createGenericDataProcessor(name, null);
    }

    @Override
    public IDataProcessor<S> createDataProcessor(String name, T data) {
        if (data != null) {
            return createGenericDataProcessor(name, data);
        }

        Logger.errorLogger(ERROR_PREFIX + " Creation of non-initialized data processors is forbidden.");
        return null;
    }
    
    private IDataProcessor<S> createGenericDataProcessor(String name, T data) {
        try {
            if (processorClasses.containsKey(name)) {
                Class<? extends IDataProcessor<S>> processorClass = processorClasses.get(name);
                IDataProcessor<S> processor;
                if (data == null)
                    processor = processorClass.newInstance();
                else
                    processor = processorClass.getConstructor(inputDataClass).newInstance(data);
                return processor;
            }
            Logger.errorLogger(ERROR_PREFIX + " Requested data processor is not found.");
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 IllegalArgumentException | InvocationTargetException | SecurityException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Failed to create data processor.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}
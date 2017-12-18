package modelgen.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import modelgen.data.property.*;
import modelgen.manager.IDataManager;
import modelgen.processor.IDataProcessorFactory;
import modelgen.shared.Logger;

abstract class Stage<I, O> implements IStage<I, O> {
    final protected String ERROR_PREFIX = "Stage error.";
    final protected String DEBUG_PREFIX = "Stage debug.";

    final protected static String PD_PREFIX = "STAGE_";
    final private static String PD_DEBUG_PRINT = PD_PREFIX + "DEBUG_PRINT";

    final private Integer DEBUG_PRINT_LEVEL = 1;

    IDataManager<I, O> dataManager;
    IDataProcessorFactory<I, O> processorFactory;

    PropertySettable propertyManager;
    PropertyInteger debugPrint;

    Stage() {
        debugPrint = new PropertyInteger(PD_DEBUG_PRINT);
        debugPrint.setValue(DEBUG_PRINT_LEVEL);

        Properties moduleProperties = new Properties();
        moduleProperties.put(debugPrint.getName(), debugPrint);

        propertyManager = new PropertyManager(moduleProperties, DEBUG_PREFIX);
    }

//    Stage(Properties extraProperties) {
//        debugPrint = new PropertyInteger(PD_DEBUG_PRINT);
//        debugPrint.setValue(DEBUG_PRINT_LEVEL);
//
//        moduleProperties = new Properties();
//        moduleProperties.put(debugPrint.getName(), debugPrint);
//        moduleProperties.putAll(extraProperties);
//
//        propertyManager = new PropertyManager(moduleProperties, DEBUG_PREFIX);
//    }

    @Override
    public boolean setModuleProperties(Properties properties) {
        try {
            return dataManager.setModuleProperties(properties);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public boolean setProcessorProperties(Map<String, Properties> properties) {
        try {
            return processorFactory.setProcessorProperties(properties);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public Properties getModuleProperties() {
        return propertyManager.getModuleProperties();
    }

    @Override
    public Map<String, Properties> getProcessorProperties() {
        return processorFactory.getProcessorProperties();
    }

    @Override
    public List<Map.Entry<O, Integer>> processData(final List<I> inputData) {
        try {
            if (inputData == null) {
                Logger.errorLogger(ERROR_PREFIX + " Input data is not initialized.");
                return null;
            }

            if (processorFactory == null) {
                Logger.errorLogger(ERROR_PREFIX + " Data processor factory is not initialized.");
                return null;
            }

            if (dataManager == null) {
                Logger.errorLogger(ERROR_PREFIX + " Data manager is not initialized.");
                return null;
            }

            List<Map.Entry<O, Integer>> returnData = new ArrayList<>();
            for (I data: inputData) {
                Map.Entry<O, Integer> result = dataManager.processData(data, processorFactory);
                if (result == null) {
                    Logger.errorLogger(ERROR_PREFIX + " Failed to process data.");
                    return null;
                }
                returnData.add(result);
            }
            return returnData;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

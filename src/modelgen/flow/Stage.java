package modelgen.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import modelgen.data.property.IProperty;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyInteger;
import modelgen.manager.IDataManager;
import modelgen.processor.IDataProcessorFactory;
import modelgen.shared.Util;

abstract class Stage<I, O> implements IStage<I, O> {
    final protected String ERROR_PREFIX = "Stage error.";
    final protected String DEBUG_PREFIX = "Stage debug.";

    final protected static String PD_PREFIX = "STAGE_";
    final private static String PD_DEBUG_PRINT = PD_PREFIX + "DEBUG_PRINT";

    final private Integer DEBUG_PRINT_LEVEL = 1;

    IDataManager<I, O> dataManager;
    IDataProcessorFactory<I, O> processorFactory;

    Properties managerProperties;
    Map<String, Properties> processorProperties;

    Properties stageProperties;
    PropertyInteger debugPrint;

    Stage() {
        debugPrint = new PropertyInteger(PD_DEBUG_PRINT);
        debugPrint.setValue(DEBUG_PRINT_LEVEL);
    }

    @Override
    public boolean setStageProperties(Properties properties) {
        try {
            if (properties == null)
                return false;

            boolean success = true;
            if (properties.containsKey(PD_DEBUG_PRINT)) {
                if (IProperty.setProperty(properties.get(PD_DEBUG_PRINT), debugPrint, PropertyInteger.class, ERROR_PREFIX))
                    Util.debugPrintln(DEBUG_PREFIX + " Setting property " + PD_DEBUG_PRINT, debugPrint.getValue());
                else
                    success = false;
            }

            return success;
        } catch (NullPointerException e) {
            Util.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public boolean setManagerProperties(Properties properties) {
        try {
            return dataManager.setManagerProperties(properties);
        } catch (NullPointerException e) {
            Util.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public boolean setProcessorProperties(Map<String, Properties> properties) {
        try {
            return processorFactory.setProcessorProperties(properties);
        } catch (NullPointerException e) {
            Util.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public Properties getStageProperties() {
        Properties managerProperties = new Properties();
        managerProperties.put(PD_DEBUG_PRINT, debugPrint);

        return managerProperties;
    }

    @Override
    public Properties getManagerProperties() {
        return dataManager.getManagerProperties();
    }

    @Override
    public Map<String, Properties> getProcessorProperties() {
        return processorFactory.getProcessorProperties();
    }

    @Override
    public List<Map.Entry<O, Integer>> processData(final List<I> inputData) {
        try {
            if (inputData == null)  {
                Util.errorLogger(ERROR_PREFIX + " Input data is not initialized.");
                return null;
            }

            if (processorFactory == null)  {
                Util.errorLogger(ERROR_PREFIX + " Data processor factory is not initialized.");
                return null;
            }

            if (dataManager == null)  {
                Util.errorLogger(ERROR_PREFIX + " Data manager is not initialized.");
                return null;
            }

            List<Map.Entry<O, Integer>> returnData = new ArrayList<>();
            for (I data: inputData) {
                Map.Entry<O, Integer> result = dataManager.processData(data, processorFactory);
                if (result == null) {
                    Util.errorLogger(ERROR_PREFIX + " Failed to process data.");
                    return null;
                }
                returnData.add(result);
            }
            return returnData;
        } catch (NullPointerException e) {
            Util.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

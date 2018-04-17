package modelgen.manager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelgen.data.property.Properties;
import modelgen.data.property.PropertyArrayList;
import modelgen.data.property.PropertyHashSet;
import modelgen.data.property.PropertyInteger;
import modelgen.data.property.PropertyManager;
import modelgen.data.property.PropertySettable;
import modelgen.processor.IDataProcessor;
import modelgen.processor.IDataProcessorFactory;
import modelgen.shared.Logger;

abstract class DataManager<T, S> implements IDataManager<T, S>, PropertySettable {
    protected String ERROR_PREFIX = "DataManager: Abstract error.";
    protected String DEBUG_PREFIX = "DataManager: Abstract debug.";

    private final String PD_PREFIX = "DM_";
    private final String PD_PROCESSOR_NAMES = PD_PREFIX + "PROCESSOR_NAMES";
    private final String PD_DEBUG_PRINT = PD_PREFIX + "DEBUG_PRINT";

    private final String USE_ALL_PROCESSORS = "Any";
    private final Integer DEBUG_PRINT_LEVEL = 1;

    PropertySettable propertyManager;
    protected PropertyHashSet<String> processorNames;
    protected PropertyInteger debugPrint;

    public DataManager() {
        processorNames = new PropertyHashSet<>(PD_PROCESSOR_NAMES, String.class);
        processorNames.setValue(new HashSet<String>(Arrays.asList(USE_ALL_PROCESSORS)));

        debugPrint = new PropertyInteger(PD_DEBUG_PRINT);
        debugPrint.setValue(DEBUG_PRINT_LEVEL);
        
        Properties moduleProperties = new Properties();
        moduleProperties.put(debugPrint.getName(), debugPrint);
        moduleProperties.put(processorNames.getName(), processorNames);

        propertyManager = new PropertyManager(moduleProperties, DEBUG_PREFIX);
    }

    @Override
    public boolean setModuleProperties(Properties properties) {
        return propertyManager.setModuleProperties(properties);
    }

    @Override
    public Properties getModuleProperties() {
        return propertyManager.getModuleProperties();
    }

    @Override
    public Map.Entry<S, Double> processData(T inputData, IDataProcessorFactory<T, S> processorFactory) {
        try {
            Set<String> factoryProcessorsNames = processorFactory.getProcessorNames();
            Set<String> managerProcessorsNames = processorNames.getValue();

            if (factoryProcessorsNames == null) {
                Logger.errorLogger(ERROR_PREFIX + " Factory has no known data processors.");
                return null;
            }

            if (managerProcessorsNames == null) {
                Logger.errorLogger(ERROR_PREFIX + " Manager has no known data processors.");
                return null;
            }

            //Decide which data processors can be used
            Set<String> processorsToUse = managerProcessorsNames;
            if (managerProcessorsNames.contains(USE_ALL_PROCESSORS))
                processorsToUse = factoryProcessorsNames;

            //Create data processors
            Map<String, IDataProcessor<S>> dataProcessors = new HashMap<>();
            for (String processorName: processorsToUse) {
                if (factoryProcessorsNames.contains(processorName)) {
                    dataProcessors.put(processorName, processorFactory.createDataProcessor(processorName, inputData));
                } else {
                    Logger.errorLogger(ERROR_PREFIX + " Data processor " + processorName + " does not exist");
                }
            }

            if (dataProcessors.isEmpty()) {
                Logger.errorLogger(ERROR_PREFIX + " No data processors to use.");
                return null;
            }

            //Sort processors by their cost in ascending order
            List<IDataProcessor<S>> sortedProcessors = new ArrayList<>(dataProcessors.values());
            sortDataProcessors(sortedProcessors);

            //Use sorted processors list to process data
            for (IDataProcessor<S> processor: sortedProcessors) {
                Double cost = processor.processCost();
                //Negative or zero cost means processor can not be applied
                if (cost > 0) {
                    S output = processor.processData();
                    if (output != null) {
                        Logger.debugPrintln(DEBUG_PREFIX + " Using processor: " + processor.getName() + " Cost: "
                                            + cost, debugPrint.getValue());
                        return new AbstractMap.SimpleEntry<S, Double>(output, cost);
                    }
                }
            }

            Logger.debugPrintln(DEBUG_PREFIX + " Failed to process data.", debugPrint.getValue());
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    abstract protected void sortDataProcessors(List<IDataProcessor<S>> processorsToSort) throws NullPointerException;
}
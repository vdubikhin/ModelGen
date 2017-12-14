package modelgen.manager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelgen.data.property.*;
import modelgen.processor.IDataProcessor;
import modelgen.processor.IDataProcessorFactory;
import modelgen.shared.Util;

public class ManagerLowCost<T, S> implements IDataManager<T, S> {
    final private String ERROR_PREFIX = "ManagerLowCost error.";
    final private String DEBUG_PREFIX = "ManagerLowCost debug.";

    //PD=property descriptor
    final private static String PD_PREFIX = "MS_";
    final private static String PD_PROCESSOR_NAMES = PD_PREFIX + "PROCESSOR_NAMES";
    final private static String PD_DEBUG_PRINT = PD_PREFIX + "DEBUG_PRINT";

    //Default values
    final private static String USE_ALL_PROCESSORS = "Any";
    final private static Integer DEBUG_PRINT_LEVEL = 1;

    PropertyHashSet<String> processorNames;
    PropertyInteger debugPrint;
    

    public ManagerLowCost() {
        processorNames = new PropertyHashSet<>(PD_PROCESSOR_NAMES, String.class);
        processorNames.setValue(new HashSet<String>(Arrays.asList(USE_ALL_PROCESSORS)));

        debugPrint = new PropertyInteger(PD_DEBUG_PRINT);
        debugPrint.setValue(DEBUG_PRINT_LEVEL);
    }

    @Override
    public boolean setManagerProperties(Properties properties) {
        if (properties == null)
            return false;

        boolean success = true;
        if (properties.containsKey(PD_DEBUG_PRINT)) {
            if (IProperty.setProperty(properties.get(PD_DEBUG_PRINT), debugPrint, PropertyInteger.class, ERROR_PREFIX))
                Util.debugPrintln(DEBUG_PREFIX + " Setting property " + PD_DEBUG_PRINT, debugPrint.getValue());
            else
                success = false;
        }

        if (properties.containsKey(PD_PROCESSOR_NAMES)) {
            if (IProperty.setProperty(properties.get(PD_PROCESSOR_NAMES), processorNames, PropertyArrayList.class, ERROR_PREFIX))
                Util.debugPrintln(DEBUG_PREFIX + " Setting property " + PD_PROCESSOR_NAMES, debugPrint.getValue());
            else
                success = false;
        }
        
        return success;
    }

    @Override
    public Properties getManagerProperties() {
        Properties managerProperties = new Properties();
        managerProperties.put(PD_PROCESSOR_NAMES, processorNames);
        managerProperties.put(PD_DEBUG_PRINT, debugPrint);

        return managerProperties;
    }

    @Override
    public Map.Entry<S, Integer> processData(T inputData, IDataProcessorFactory<T, S> processorFactory) {
        try {
            Set<String> factoryProcessorsNames = processorFactory.getProcessorNames();
            Set<String> managerProcessorsNames = processorNames.getValue();
            
            if (factoryProcessorsNames == null) {
                Util.errorLogger(ERROR_PREFIX + " Factory has no known data processors.");
                return null;
            }

            if (managerProcessorsNames == null) {
                Util.errorLogger(ERROR_PREFIX + " Manager has no known data processors.");
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
                    Util.errorLogger(ERROR_PREFIX + " Data processor " + processorName + " does not exist");
                }
            }

            if (dataProcessors.isEmpty()) {
                Util.errorLogger(ERROR_PREFIX + " No data processors to use.");
                return null;
            }

            //Sort processors by their cost in ascending order
            List<IDataProcessor<S>> sortedProcessors = new ArrayList<>(dataProcessors.values());
            sortedProcessors.sort((proc1, proc2) -> proc1.processCost() - proc2.processCost());

            //Use sorted processors list to process data
            for (IDataProcessor<S> processor: sortedProcessors) {
                Integer cost = processor.processCost();
                //Negative or zero cost means processor can not be applied
                if (cost > 0) {
                    S output = processor.processData();
                    if (output != null) {
                        return new AbstractMap.SimpleEntry<S, Integer>(output, cost);
                    }
                }
            }
        } catch (NullPointerException e) {
            Util.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

}

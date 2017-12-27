package modelgen.processor;

import modelgen.data.property.Properties;
import modelgen.data.property.PropertyInteger;
import modelgen.data.property.PropertyManager;
import modelgen.data.property.PropertySettable;

public abstract class DataProcessor<S> implements IDataProcessor<S> {
    protected String ERROR_PREFIX = "DataProcessor: Abstract error.";
    protected String DEBUG_PREFIX = "DataProcessor: Abstract debug.";
    
    protected String name = "AbstractType";
    
    protected static final String PD_PREFIX = "DP_";
    protected static final String PD_DEBUG_PRINT = PD_PREFIX + "DEBUG_PRINT";
    protected static final String PD_VALUE_BASE_COST = PD_PREFIX + "VALUE_BASE_COST";
    
    protected final Integer DEBUG_PRINT_LEVEL = 1;
    protected final Integer VALUE_BASE_COST = 1;

    protected PropertyInteger valueBaseCost;
    protected PropertyInteger debugPrint;

    protected PropertySettable propertyManager;
    
    public DataProcessor() {
        debugPrint = new PropertyInteger(PD_DEBUG_PRINT);
        debugPrint.setValue(DEBUG_PRINT_LEVEL);

        valueBaseCost = new PropertyInteger(PD_VALUE_BASE_COST);
        valueBaseCost.setValue(VALUE_BASE_COST);

        Properties moduleProperties = new Properties();
        moduleProperties.put(debugPrint.getName(), debugPrint);
        
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
    public String getName() {
        return name;
    }
}
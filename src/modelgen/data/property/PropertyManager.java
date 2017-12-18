package modelgen.data.property;

import modelgen.shared.Logger;

public class PropertyManager implements PropertySettable {
    final String errorPrefix;
    final Properties moduleProperties;

    public PropertyManager(Properties properties, String prefix) {
        moduleProperties = properties;
        errorPrefix = prefix;
    }
    
    @Override
    public boolean setModuleProperties(Properties properties) {
        try {
            if (properties == null)
                return false;

            boolean success = true;
            for (String propertyToSetName: properties.keySet()) {
                if (moduleProperties.containsKey(propertyToSetName)) {
                    if (!setProperty(properties.get(propertyToSetName), moduleProperties.get(propertyToSetName),
                            moduleProperties.get(propertyToSetName).getClass(), errorPrefix))
                        success = false;
                }
            }
            return success;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(errorPrefix + " Null pointer exception.", e);
        }
        return false;
    }

    @Override
    public Properties getModuleProperties() {
        return moduleProperties;
    }

    boolean setProperty(IProperty propertyToUse, IProperty propertyToSet, Class<?> propertyClass, String errorPrefix) {
        try {
            if (propertyToUse.getName().equals(propertyToSet.getName())) {
                IProperty temp = (IProperty) propertyClass.cast(propertyToUse);
                //HACK: assign value to itself to check if we have collection that holds values of the declared type
                if (temp.setValue(temp.getValue())) {
                    propertyToSet = temp;
                    return true;
                }
                Logger.errorLogger(errorPrefix + " Failure to set property " + propertyToSet.getName());
            } else {
                Logger.errorLogger(errorPrefix + " Property name mismatch for property " + propertyToSet.getName());
            }
        } catch (ClassCastException e) {
            Logger.errorLoggerTrace(errorPrefix + " Failure to set property value.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(errorPrefix + " Property not set.", e);
        }
        return false;
    }
}

package modelgen.data.property;

import modelgen.shared.Util;

public interface IProperty {

    String getName();

    Object getValue();

    boolean setValue(Object value);
    
    static boolean setProperty(IProperty propertyToUse, IProperty propertyToSet, Class<?> propertyClass, String errorPrefix) {
        try {
            if (propertyToUse.getName().equals(propertyToSet.getName())) {
                IProperty temp = (IProperty) propertyClass.cast(propertyToUse);
                //HACK: assign value to itself to check if we have collection that holds values of the declared type
                if (temp.setValue(temp.getValue())) {
                    propertyToSet = temp;
                    return true;
                }
                Util.errorLogger(errorPrefix + " Failure to set property " + propertyToSet.getName());
            } else {
                Util.errorLogger(errorPrefix + " Property name mismatch for property " + propertyToSet.getName());
            }
        } catch (ClassCastException e) {
            Util.errorLoggerTrace(errorPrefix + " Failure to set property value.", e);
        } catch (NullPointerException e) {
            Util.errorLoggerTrace(errorPrefix + " Property not set.", e);
        }
        return false;
    }
}
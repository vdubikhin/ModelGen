package modelgen.data.property;

import modelgen.shared.Util;

abstract class Property<T> implements IProperty {
    final protected String ERROR_PREFIX = "Property error.";
    final protected String DEBUG_PREFIX = "Property debug.";

    final private Class<T> classType;
    private String name;
    protected T value;
    
    public Property (String name, Class<T> classType) {
        this.name = name;
        this.classType = classType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getValue() {
        return value;
    }
    
    @Override
    public boolean setValue(Object value) {
        try {
            T temp = assignValue(value);
            if (temp != null) {
                this.value = temp;
                return true;
            }
        } catch (ClassCastException e) {
            Util.errorLoggerTrace(ERROR_PREFIX + " Failure to set property value.", e);
        }
        return false;
    }
    
    protected T assignValue(Object value) throws ClassCastException {
        if (classType == null) {
            Util.errorLoggerTrace(ERROR_PREFIX + " Class type not set", new Throwable());
            return null;
        }
        
        return classType.cast(value);
    }
}

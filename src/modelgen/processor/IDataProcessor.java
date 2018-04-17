package modelgen.processor;

import modelgen.data.property.PropertySettable;

public interface IDataProcessor<T> extends PropertySettable {

    String getName();

    double processCost();

    T processData();
}

package modelgen.processor;

import modelgen.data.property.PropertySettable;

public interface IDataProcessor<T> extends PropertySettable {

    String getName();

    int processCost();

    T processData();
}

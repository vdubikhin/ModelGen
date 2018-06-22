package modelgen.manager;


import java.util.Map;

import modelgen.data.property.PropertySettable;
import modelgen.data.stage.IStageData;
import modelgen.processor.IDataProcessorFactory;

public interface IDataManager<I extends IStageData, O extends IStageData> extends PropertySettable {
    Map.Entry<O, Double> processData(I inputData, IDataProcessorFactory<I, O> processorFactory);
}

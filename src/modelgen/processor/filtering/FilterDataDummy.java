package modelgen.processor.filtering;

import modelgen.data.property.Properties;
import modelgen.data.property.PropertyDouble;
import modelgen.data.property.PropertyManager;
import modelgen.data.stage.StageDataState;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;

public class FilterDataDummy extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {
    final private static String PD_MAX_PRECISION_LOSS = PD_PREFIX + "MAX_PRECISION_LOSS";


    final private Double MAX_PRECISION_LOSS =2125.0;
    PropertyDouble maxPrecisionLoss;

    StageDataState data;

    public FilterDataDummy() {
        ERROR_PREFIX = "DataProcessor: FilterDataDummy error. ";
        DEBUG_PREFIX = "DataProcessor: FilterDataDummy debug. ";

        name = "FilterDataDummy";

        maxPrecisionLoss = new PropertyDouble(PD_MAX_PRECISION_LOSS);
        maxPrecisionLoss.setValue(MAX_PRECISION_LOSS);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(maxPrecisionLoss.getName(), maxPrecisionLoss);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }
    
    public FilterDataDummy(StageDataState inputData) {
        this();
        data = inputData;
    }

    @Override
    public double processCost() {
        return maxPrecisionLoss.getValue();
    }

    @Override
    public StageDataState processData() {
        return data;
    }
}

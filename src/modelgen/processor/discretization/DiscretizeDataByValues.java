package modelgen.processor.discretization;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import modelgen.data.ControlType;
import modelgen.data.complex.Mergeable;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyInteger;
import modelgen.data.property.PropertyManager;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.stage.StageDataRaw;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.data.state.StateDMV;
import modelgen.processor.IDataProcessor;
import modelgen.shared.Logger;
import modelgen.shared.Util;

public class DiscretizeDataByValues extends ADataDiscretizer implements IDataProcessor<StageDataState> {
    final private static String PD_MAX_UNIQUE_VALUES = PD_PREFIX + "MAX_UNIQUE_VALUES";

    final private Integer VALUE_BASE_COST = 1;
    final private Integer MAX_UNIQUE_VALUES = 15;

    PropertyInteger maxUniqueValues;

    private RawDataChunk inputData;
    private ControlType inputType;
    private String inputName;
    private Map<Double, Integer> uniqueValues;
    StageDataState output;
    Double cost;

    private class ValueDescretizer extends APointDescretizer implements IPointDescretizer {
        Map<Double, Integer> uniqueValues;

        ValueDescretizer (Map<Double, Integer> uniqueValues) {
            this.uniqueValues = uniqueValues;
        }

        @Override
        public IState createState(RawDataChunk data, int index)
                throws ArrayIndexOutOfBoundsException, NullPointerException {
            if (uniqueValues == null || uniqueValues.isEmpty())
                return null;

            Double start, end;
            start = getPointStart(data, index);
            end = getPointEnd(data, index);

            RawDataPoint point = inputData.get(index);

            return  new StateDMV(inputName, start, end, point.getValue());
        }

        @Override
        public RawDataPointGrouped createGroupedPoint(RawDataChunk data, int index)
                throws ArrayIndexOutOfBoundsException, NullPointerException {
            if (uniqueValues == null || uniqueValues.isEmpty())
                return null;

            RawDataPoint point = inputData.get(index);
            Integer pointGroup = uniqueValues.get(point.getValue());

            return new RawDataPointGrouped(point, pointGroup);
        }
    }

    public DiscretizeDataByValues() {
        this.inputData = null;
        
        name = "ValueType";

        ERROR_PREFIX = "DataProcessor: DiscretizeDataByValues error.";
        DEBUG_PREFIX = "DataProcessor: DiscretizeDataByValues debug.";

        valueBaseCost.setValue(VALUE_BASE_COST);

        maxUniqueValues = new PropertyInteger(PD_MAX_UNIQUE_VALUES);
        maxUniqueValues.setValue(MAX_UNIQUE_VALUES);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(maxUniqueValues.getName(), maxUniqueValues);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public DiscretizeDataByValues(StageDataRaw inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
    }

    @Override
    public double processCost() {
        try {
            if (inputData == null)
                return -1;

            //If data has already been processed, use cached result
            if (uniqueValues != null)
                return costFunction();

            uniqueValues = new HashMap<>();
            Integer totalNum = 1;
            for (RawDataPoint point: inputData) {
                Double curValue = point.getValue();
                if (!uniqueValues.containsKey(curValue))
                    uniqueValues.put(curValue, totalNum++);

                if (uniqueValues.size() > maxUniqueValues.getValue())
                    break;
            }

            return costFunction();
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        uniqueValues = null;
        return -1;
    }

    private double costFunction() {
        try {
            if (cost != null)
                return cost;

            if (uniqueValues == null)
                return -1;

            if (uniqueValues.size() > 0 && uniqueValues.size() <= maxUniqueValues.getValue()) {
                IPointDescretizer pointDescretizer = new ValueDescretizer(uniqueValues);
                StageDataState result = createStageData(inputData, pointDescretizer, inputName, inputType);

                if (result == null)
                    return -1;

                RawDataChunk generatedData = Util.generateSignalFromStates(inputData, result.getStates());

                Double difference = Util.compareWaveForms(inputData, generatedData);

                if (difference >= 0.0)
                    output = result;

                cost = difference * valueBaseCost.getValue() + valueBaseCost.getValue();
                return cost;
            }
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    @Override
    public StageDataState processData() {
        try {
            if (output != null)
                return output;

            if (costFunction() < 0)
                return null;

            return output;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

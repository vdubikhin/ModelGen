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
import modelgen.data.state.IState;
import modelgen.data.state.StateDMV;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.shared.Logger;

public class DiscretizeDataByValues extends DataProcessor<DataOutput> implements IDataProcessor<DataOutput> {
    final private static String PD_VALUE_BASE_COST = PD_PREFIX + "VALUE_BASE_COST";
    final private static String PD_MAX_UNIQUE_VALUES = PD_PREFIX + "MAX_UNIQUE_VALUES";

    final private Integer VALUE_BASE_COST = 1;
    final private Integer MAX_UNIQUE_VALUES = 10;

    PropertyInteger valueBaseCost;
    PropertyInteger maxUniqueValues;

    private RawDataChunk inputData;
    private ControlType inputType;
    private String inputName;
    private Map<Double, Integer> uniqueValues;
    
    public DiscretizeDataByValues() {
        this.inputData = null;
        
        name = "ValueType";

        ERROR_PREFIX = "DataProcessor: DiscretizeDataByValues error.";
        DEBUG_PREFIX = "DataProcessor: DiscretizeDataByValues debug.";
        
        valueBaseCost = new PropertyInteger(PD_VALUE_BASE_COST);
        valueBaseCost.setValue(VALUE_BASE_COST);

        maxUniqueValues = new PropertyInteger(PD_MAX_UNIQUE_VALUES);
        maxUniqueValues.setValue(MAX_UNIQUE_VALUES);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(maxUniqueValues.getName(), maxUniqueValues);

        propertyManager = new PropertyManager(moduleProperties, DEBUG_PREFIX);
    }

    public DiscretizeDataByValues(DataInput inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
    }

    @Override
    public int processCost() {
        try {
            if (inputData == null)
                return -1;

            //If data has already been processed, use cached result
            if (uniqueValues != null)
                return costFunction();

            uniqueValues = new HashMap<>();
            Integer totalNum = 1;
            for (RawDataPoint point: inputData) {
                Double curValue = point.value;
                if (!uniqueValues.containsKey(curValue))
                    uniqueValues.put(curValue, totalNum++);

                if (uniqueValues.size() > maxUniqueValues.getValue())
                    break;
            }

            int cost = costFunction();

            if (cost > 0)
                return cost;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        uniqueValues = null;
        return -1;
    }

    private int costFunction() {
        try {
            if (uniqueValues.size() > 0 && uniqueValues.size() <= maxUniqueValues.getValue())
                return uniqueValues.size() * valueBaseCost.getValue();
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    @Override
    public DataOutput processData() {
        try {
            if (inputData == null)
                return null;

            if (uniqueValues == null || costFunction() < 0)
                return null;

            RawDataChunkGrouped groupedData = new RawDataChunkGrouped();
            List<IState> outputStates = new ArrayList<>();
            for (int i = 0; i < inputData.size(); i++) {
                RawDataPoint point = inputData.get(i);
                Integer pointGroup = uniqueValues.get(point.value);
                RawDataPointGrouped groupedPoint = new RawDataPointGrouped(point, pointGroup);

                Double start, end;
                start = inputData.get(i).time;
                if (i != inputData.size() - 1)
                    end = inputData.get(i + 1).time;
                else
                    end = inputData.get(i).time;

                IState curState = new StateDMV(inputName, pointGroup, start, end, point.value);

                outputStates.add(curState);
                groupedData.add(groupedPoint);
            }

            Mergeable.mergeEntries(outputStates);

            DataOutput result = new DataOutput(groupedData, inputName, inputType, outputStates);

            return result;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

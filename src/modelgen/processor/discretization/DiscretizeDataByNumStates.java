package modelgen.processor.discretization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelgen.data.ControlType;
import modelgen.data.complex.Mergeable;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyDouble;
import modelgen.data.property.PropertyInteger;
import modelgen.data.property.PropertyManager;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.stage.StageDataRaw;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.data.state.StateThresholds;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.shared.Logger;

public class DiscretizeDataByNumStates extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {
    final private static String PD_NUM_STATES = PD_PREFIX + "NUM_STATES";
    final private static String PD_THRESHOLD_TOLERANCE = PD_PREFIX + "THRESHOLD_TOLERANCE";

    final private Integer VALUE_BASE_COST = 50;
    final private Integer NUM_STATES = 2;
    final private Double THRESHOLD_TOLERANCE = 0.05;

    PropertyInteger numStates;
    PropertyDouble thresholdTolerance;

    protected RawDataChunk inputData;
    protected ControlType inputType;
    protected String inputName;

    public DiscretizeDataByNumStates() {
        this.inputData = null;
        
        name = "ValueType";

        ERROR_PREFIX = "DataProcessor: DiscretizeDataByNumThresholds error.";
        DEBUG_PREFIX = "DataProcessor: DiscretizeDataByNumThresholds debug.";

        valueBaseCost.setValue(VALUE_BASE_COST);

        numStates = new PropertyInteger(PD_NUM_STATES);
        numStates.setValue(NUM_STATES);

        thresholdTolerance = new PropertyDouble(PD_THRESHOLD_TOLERANCE);
        thresholdTolerance.setValue(THRESHOLD_TOLERANCE);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(numStates.getName(), numStates);
        moduleProperties.put(thresholdTolerance.getName(), thresholdTolerance);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public DiscretizeDataByNumStates(StageDataRaw inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
    }

    @Override
    public int processCost() {
        try {
            if (inputData == null || inputData.isEmpty())
                return -1;

            if (numStates.getValue() <= 1)
                return -1;
            
            return valueBaseCost.getValue()*numStates.getValue();
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    @Override
    public StageDataState processData() {
        try {
            if (processCost() < 0)
                return null;

            int numPointsPerState = inputData.size()/numStates.getValue();
            List<Double> thresholds = new ArrayList<>();

            //Calculate thresholds as min, max values over each region
            for (int i = 0; i < numStates.getValue(); i++) {
                int startIndex = numPointsPerState * i;
                int endIndex = numPointsPerState * (i + 1);
                if (i == numStates.getValue() - 1)
                    endIndex = inputData.size();

                List<RawDataPoint> curData = inputData.subList(startIndex, endIndex);
                Double minValue = curData.stream()
                        .min((p1, p2) -> Double.compare(p1.getValue(), p2.getValue()))
                        .get()
                        .getValue();

                Double maxValue = curData.stream()
                        .max((p1, p2) -> Double.compare(p1.getValue(), p2.getValue()))
                        .get()
                        .getValue();

                thresholds.add(minValue);
                thresholds.add(maxValue);
            }

            if (thresholds.size() < 2)
                return null;

            thresholds.sort((d1, d2) -> Double.compare(d1, d2));
            List<Double> thresholdsFiltered = new ArrayList<>();
            thresholdsFiltered.add(thresholds.get(0));

            for (int i = 0; i < thresholds.size() - 1; i++) {
                if (Math.abs(thresholds.get(i) - thresholds.get(i+1)) >= thresholdTolerance.getValue()) {
                    thresholdsFiltered.add(thresholds.get(i + 1));
                }
            }

            if (thresholdsFiltered.size() < 2) {
                Logger.debugPrintln(DEBUG_PREFIX + " Not enough threshold values", debugPrint.getValue());
                return null;
            }

            thresholds = thresholdsFiltered;

            RawDataChunkGrouped groupedData = new RawDataChunkGrouped();
            List<IState> outputStates = new ArrayList<>();
            //Distribute data points between threshold boundaries
            for (int i = 0; i < inputData.size(); i++) {
                RawDataPoint curPoint = inputData.get(i);
                int index;

                for (index = 0; index < thresholds.size() - 1; index++) {
                    if (curPoint.getValue() >= thresholds.get(index) && 
                            curPoint.getValue() <= thresholds.get(index + 1))
                        break;
                }

                Double start, end;
                start = inputData.get(i).getTime();
                if (i != inputData.size() - 1)
                    end = inputData.get(i + 1).getTime();
                else
                    end = inputData.get(i).getTime();

                groupedData.add(new RawDataPointGrouped(curPoint, index));
                outputStates.add(new StateThresholds(inputName, index, start,end,
                        thresholds.get(index), thresholds.get(index + 1)));
            }

            Mergeable.mergeEntries(outputStates);

            StageDataState result = new StageDataState(groupedData, inputName, inputType, outputStates);

            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

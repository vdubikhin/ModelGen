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
import modelgen.data.state.StateSymbolic;
import modelgen.data.state.StateThresholds;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.processor.filtering.FilterDataByDurationCluster;
import modelgen.shared.Logger;
import modelgen.shared.Util;

public class DiscretizeDataByNumStates extends ADataDiscretizer implements IDataProcessor<StageDataState> {
    final private static String PD_NUM_STATES = PD_PREFIX + "NUM_STATES";
    final private static String PD_THRESHOLD_TOLERANCE = PD_PREFIX + "THRESHOLD_TOLERANCE";
    final private static String PD_DISTRIBUTION_PEAK = PD_PREFIX + "DISTRIBUTION_PEAK";

    final private Integer VALUE_BASE_COST = 50;
    final private Integer NUM_STATES = 1;
    final private Double THRESHOLD_TOLERANCE = 0.05;
    final private Double DISTRIBUTION_PEAK = 0.8;

    private enum DataSign {
        POSITIVE(1),
        NEGATIVE(-1);

        Integer id;

        Integer getValue() {return id;}

        DataSign(Integer id) {
            this.id = id;
        }
    }

    private class DerivativeDescretizer extends APointDescretizer implements IPointDescretizer {
        RawDataChunk derivData;

        DerivativeDescretizer (RawDataChunk inputData) {
            derivData = Util.calculateFirstDerivative(inputData);
        }

        @Override
        public IState createState(RawDataChunk data, int index) 
                throws ArrayIndexOutOfBoundsException, NullPointerException{
            if (derivData == null || index >= derivData.size())
                return null;

            Double start, end;
            start = getPointStart(data, index);
            end = getPointEnd(data, index);

            DataSign pointSign = derivData.get(index).getValue() >= 0 ? DataSign.POSITIVE : DataSign.NEGATIVE;
            return new StateSymbolic(inputName, pointSign.getValue(), start, end);
        }

        @Override
        public RawDataPointGrouped createGroupedPoint(RawDataChunk data, int index)
                throws ArrayIndexOutOfBoundsException, NullPointerException {
            if (derivData == null || index >= derivData.size())
                return null;

            DataSign pointSign = derivData.get(index).getValue() >= 0 ? DataSign.POSITIVE : DataSign.NEGATIVE;
            return new RawDataPointGrouped(inputData.get(index), pointSign.getValue());
        }
    }

    private class ThresholdDescretizer extends APointDescretizer implements IPointDescretizer {
        List<Double> thresholds;

        ThresholdDescretizer (List<Double> inputData) {
            thresholds = inputData;
        }

        @Override
        public IState createState(RawDataChunk data, int index)
                throws ArrayIndexOutOfBoundsException, NullPointerException {
            if (thresholds == null || thresholds.isEmpty())
                return null;

            Double start, end;
            start = getPointStart(data, index);
            end = getPointEnd(data, index);

            RawDataPoint curPoint = data.get(index);

            int i;
            for (i = 0; i < thresholds.size() - 1; i++) {
                if (curPoint.getValue() >= thresholds.get(i) &&
                        curPoint.getValue() <= thresholds.get(i + 1))
                    break;
            }

            return new StateThresholds(inputName, start, end,
                    thresholds.get(i), thresholds.get(i + 1));
        }

        @Override
        public RawDataPointGrouped createGroupedPoint(RawDataChunk data, int index)
                throws ArrayIndexOutOfBoundsException, NullPointerException {
            if (thresholds == null || thresholds.isEmpty())
                return null;

            IState state = createState(data, index);

            if (state == null)
                return null;

            return new RawDataPointGrouped(data.get(index), state.getId());
        }
    }

    PropertyInteger numStates;
    PropertyDouble thresholdTolerance;
    PropertyDouble filterDistributionPeak;

    protected RawDataChunk inputData;
    protected ControlType inputType;
    protected String inputName;

    List<RawDataChunk> monotonicChunks;

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

        filterDistributionPeak = new PropertyDouble(PD_DISTRIBUTION_PEAK);
        filterDistributionPeak.setValue(DISTRIBUTION_PEAK);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(numStates.getName(), numStates);
        moduleProperties.put(thresholdTolerance.getName(), thresholdTolerance);
        moduleProperties.put(filterDistributionPeak.getName(), filterDistributionPeak);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public DiscretizeDataByNumStates(StageDataRaw inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
    }

    @Override
    public double processCost() {
        try {
            //TODO: add proper cost function
            if (monotonicChunks != null && !monotonicChunks.isEmpty())
                return 1;
            
            if (inputData == null || inputData.isEmpty())
                return -1;

            if (numStates.getValue() < 1)
                return -1;

            if (inputData.size() < numStates.getValue()) {
                Logger.errorLogger(ERROR_PREFIX + " Number of states exceeds number of points.");
                return -1;
            }

            RawDataChunk derivData = Util.calculateFirstDerivative(this.inputData);

            if (derivData == null)
                return -1;

            //Group data by derivative sign
            IPointDescretizer pointDescretizer = new DerivativeDescretizer(derivData);
            StageDataState result = createStageData(inputData, pointDescretizer, inputName, inputType);

            //Filter data
            IDataProcessor<StageDataState> filter = new FilterDataByDurationCluster(result);
            Properties filterProperties = new Properties();
            filterProperties.put(filterDistributionPeak.getName(), filterDistributionPeak);

            if (!filter.setModuleProperties(filterProperties)) {
                Logger.errorLogger(ERROR_PREFIX + " Failed to set filter properties.");
            }

            StageDataState filteredData;
            if (filter.processCost() > 0)
                filteredData = filter.processData();
            else
                filteredData = result;

            if (filteredData == null)
                return -1;

            //Use filtered data to split original data into subarrays
            RawDataChunkGrouped groupedData = filteredData.getData();
            monotonicChunks = new ArrayList<>();

            RawDataChunk curData = new RawDataChunk();
            Integer prevGroup = null;

            for (RawDataPointGrouped point: groupedData) {
                if (prevGroup == null) {
                    curData.add(point);
                    prevGroup = point.getGroup();
                    continue;
                }

                if (point.getGroup() != prevGroup) {
                    monotonicChunks.add(curData);
                    curData = new RawDataChunk();
                }

                prevGroup = point.getGroup();
                curData.add(point);
            }

            monotonicChunks.add(curData);

            return valueBaseCost.getValue() * numStates.getValue() * monotonicChunks.size();
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    protected List<Double> calculateThresholds(List<RawDataChunk> monotonicChunks, Integer numStates)
            throws ArrayIndexOutOfBoundsException, NullPointerException {
        List<Double> thresholds = new ArrayList<>();

        for (RawDataChunk curData: monotonicChunks) {
            int numPointsPerState = curData.size()/numStates;
            //Calculate thresholds as min, max values over each region
            for (int i = 0; i < numStates; i++) {
                int startIndex = numPointsPerState * i;
                int endIndex = Math.max(numPointsPerState * (i + 1), curData.size());

                List<RawDataPoint> data = curData.subList(startIndex, endIndex);

                Double minValue = data.stream()
                        .min((p1, p2) -> Double.compare(p1.getValue(), p2.getValue()))
                        .get()
                        .getValue();

                Double maxValue = data.stream()
                        .max((p1, p2) -> Double.compare(p1.getValue(), p2.getValue()))
                        .get()
                        .getValue();

                thresholds.add(minValue);
                thresholds.add(maxValue);
            }
        }

        return thresholds;
    }

    //Math.abs(thresholds.get(i) - thresholds.get(i + 1)) >= thresholdTolerance.getValue())
    protected List<Double> filterThresholds(List<Double> thresholds)
            throws ArrayIndexOutOfBoundsException, NullPointerException {

        if (thresholds.size() < 2)
            return null;

        if (thresholds.size() == 2)
            return thresholds;

        thresholds.sort((d1, d2) -> Double.compare(d1, d2));

        List<Double> thresholdsFiltered = new ArrayList<>(thresholds);
        List<Integer> thresholdsToRemove = new ArrayList<>();

        do {
            thresholdsToRemove.clear();
            for (int i = 1; i < thresholdsFiltered.size() - 1; i ++) {
                if (Math.abs(thresholdsFiltered.get(i) - thresholdsFiltered.get(i + 1)) <=
                        thresholdTolerance.getValue() ||
                    Math.abs(thresholdsFiltered.get(i) - thresholdsFiltered.get(i - 1)) <=
                        thresholdTolerance.getValue()
                        )
                    thresholdsToRemove.add(i);
            }

            Collections.reverse(thresholdsToRemove);
            for (Integer id: thresholdsToRemove) {
                if (id % 2 != 0 || thresholdsToRemove.size() == 1)
                    thresholdsFiltered.remove(id.intValue());
            }

        } while (!thresholdsToRemove.isEmpty());

        return thresholdsFiltered;
    }

    @Override
    public StageDataState processData() {
        try {
            if (processCost() < 0)
                return null;

            List<Double> thresholds = calculateThresholds(monotonicChunks, numStates.getValue());
            thresholds = filterThresholds(thresholds);

            if (thresholds == null) {
                Logger.debugPrintln(DEBUG_PREFIX + " Not enough threshold values", debugPrint.getValue());
                return null;
            }

            IPointDescretizer thresholdDescretizer = new ThresholdDescretizer(thresholds);
            StageDataState result = createStageData(inputData, thresholdDescretizer, inputName, inputType);

            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

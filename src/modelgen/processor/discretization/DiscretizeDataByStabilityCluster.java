package modelgen.processor.discretization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import modelgen.data.ControlType;
import modelgen.data.complex.ClusterPointValue;
import modelgen.data.complex.ClusterPointEvalHelper;
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
import modelgen.data.state.StateDMV;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.processor.filtering.FilterDataByDurationCluster;
import modelgen.shared.Logger;
import modelgen.shared.Util;
import modelgen.shared.clustering.AgglomerativeClusteringMax;
import modelgen.shared.clustering.ClusteringAlgorithm;
import modelgen.shared.clustering.ICluster;

public class DiscretizeDataByStabilityCluster extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {
    final private static String PD_VAR_COEFF = PD_PREFIX + "VAR_COEFF";
    final private static String PD_MAX_UNIQUE_VALUES = PD_PREFIX + "MAX_UNIQUE_VALUES";
    final private static String PD_MIN_STABLE_DURATION = PD_PREFIX + "MIN_STABLE_DURATION";
    final private static String PD_DISTRIBUTION_PEAK = PD_PREFIX + "DISTRIBUTION_PEAK";

    final private double VAR_COEFF = 0.25;
    final private double MIN_STABLE_DURATION = 0.8;
    final private Integer VALUE_BASE_COST = 3;
    final private Integer MAX_UNIQUE_STATES = 15;
    final private Double DISTRIBUTION_PEAK = 0.65;

    PropertyDouble varCoefficient;
    PropertyDouble minStableDuration;
    PropertyInteger maxUniqueStates;
    PropertyDouble filterDistributionPeak;

    protected RawDataChunk inputData;
    protected ControlType inputType;
    protected String inputName;

    protected List<ClusterPointValue> stabilityPoints;
    private Map<Integer, ClusterPointValue> stabilityPointsStateMap;
    protected StageDataState output;
    protected Double cost;

    public DiscretizeDataByStabilityCluster() {
        ERROR_PREFIX = "DataProcessor: DiscretizeDataByStabilityCluster error. ";
        DEBUG_PREFIX = "DataProcessor: DiscretizeDataByStabilityCluster debug. ";

        name = "StabilityCluster";

        varCoefficient = new PropertyDouble(PD_VAR_COEFF);
        varCoefficient.setValue(VAR_COEFF);

        minStableDuration = new PropertyDouble(PD_MIN_STABLE_DURATION);
        minStableDuration.setValue(MIN_STABLE_DURATION);

        valueBaseCost.setValue(VALUE_BASE_COST);

        maxUniqueStates = new PropertyInteger(PD_MAX_UNIQUE_VALUES);
        maxUniqueStates.setValue(MAX_UNIQUE_STATES);

        filterDistributionPeak = new PropertyDouble(PD_DISTRIBUTION_PEAK);
        filterDistributionPeak.setValue(DISTRIBUTION_PEAK);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(varCoefficient.getName(), varCoefficient);
        moduleProperties.put(filterDistributionPeak.getName(), filterDistributionPeak);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public DiscretizeDataByStabilityCluster(StageDataRaw inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
    }

    @Override
    public double processCost() {
        return processCost(inputData);
    }
    
    protected double processCost(RawDataChunk data) {
        try {
            if (data == null)
                return -1;

            if (stabilityPoints != null)
                return costFunction();

            List<ClusterPointValue> clusterPointArray = createClusterPoints(data);

            ClusteringAlgorithm<ClusterPointValue> clusterAlgorithm = new AgglomerativeClusteringMax<>();
            ICluster<ClusterPointValue> root = clusterAlgorithm.formDendrogram(clusterPointArray);
            stabilityPoints = clusterAlgorithm.layerSearch(root, varCoefficient.getValue());

            return costFunction();
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    protected List<ClusterPointValue> createClusterPoints(List<RawDataPoint> inputData)
                                                     throws NullPointerException, ArrayIndexOutOfBoundsException {
        ClusterPointEvalHelper evalHelper = new ClusterPointEvalHelper();
        List<ClusterPointValue> clusterPointArray = new ArrayList<>();
        for (RawDataPoint curPoint: inputData) {
            clusterPointArray.add(new ClusterPointValue(curPoint.getValue(), evalHelper));
        }
        return clusterPointArray;
    }

    protected double costFunction() {
        try {
            if (cost != null)
                return cost;

            if (stabilityPoints == null)
                return -1;

            StageDataState result = createOutputData(inputData, stabilityPoints);

            stabilityPointsStateMap = findStableStates(result);

            if (stabilityPointsStateMap == null) {
                cost = -1.0;
                return -1;
            }

            stabilityPoints = new ArrayList<>(stabilityPointsStateMap.values());

            result = createOutputData(inputData, stabilityPoints);

            if (result == null)
                return -1;

            RawDataChunk generatedData = Util.generateSignalFromStates(inputData, result.getStates());
            Double difference = Util.compareWaveForms(inputData, generatedData);

            if (difference >= 0.0)
                output = result;

            cost = difference * valueBaseCost.getValue() + valueBaseCost.getValue();
            return cost;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    protected Map<Integer, ClusterPointValue> findStableStates(StageDataState data)
            throws NullPointerException {
        //Filter data
        IDataProcessor<StageDataState> filter = new FilterDataByDurationCluster(data);
        Properties filterProperties = new Properties();
        filterProperties.put(filterDistributionPeak.getName(), filterDistributionPeak);

        if (!filter.setModuleProperties(filterProperties)) {
            Logger.errorLogger(ERROR_PREFIX + " Failed to set filter properties.");
        }

        StageDataState filteredData;
        if (filter.processCost() > 0)
            filteredData = filter.processData();
        else
            return null;

        if (filteredData == null)
            return null;

        Set<Integer> uniqueStates = new HashSet<>();
        for (IState curState: filteredData.getStates())
            uniqueStates.add(curState.getId());

        if (uniqueStates.size() > maxUniqueStates.getValue())
            return null;

        Map<Integer, ClusterPointValue> points = new HashMap<>();
        for (Integer id: uniqueStates) {
            if (stabilityPointsStateMap.containsKey(id))
                points.put(id, stabilityPointsStateMap.get(id));
        }

        return points;
    }

    @Override
    public StageDataState processData() {
        return processData(inputData);
    }

    protected StageDataState processData(RawDataChunk data) {
        try {
            if (output != null)
                return output;

            if (data == null)
                return null;

            if (costFunction() < 0)
                return null;

            StageDataState result = createOutputData(inputData, stabilityPoints);

            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    protected ClusterPointValue getClosestCluster(RawDataPoint point, List<ClusterPointValue> stabilityData)
                                                  throws NullPointerException, ArrayIndexOutOfBoundsException {
        double minDistance = Double.POSITIVE_INFINITY;
        ClusterPointValue bestCluster = null;
        for (ClusterPointValue cluster: stabilityData) {
            double distance = Math.abs(cluster.getClusterCenter() - point.getValue());
            
            if (distance < minDistance) {
                minDistance = distance;
                bestCluster = cluster;
            }
        }
        return bestCluster;
    }

    protected StageDataState createOutputData(RawDataChunk data, List<ClusterPointValue> stabilityData)
                                              throws NullPointerException, ArrayIndexOutOfBoundsException {
        List<IState> outputStates = new ArrayList<>();
        RawDataChunkGrouped groupedData = new RawDataChunkGrouped();
        stabilityPointsStateMap = new HashMap<>();

        for (int i = 0; i < data.size(); i++) {
            Double start, end;
            start = data.get(i).getTime();
            if (i != data.size() - 1)
                end = data.get(i + 1).getTime();
            else
                end = data.get(i).getTime();

            ClusterPointValue cluster = getClosestCluster(data.get(i), stabilityData);
            IState curState = createState(cluster, start, end);
            outputStates.add(curState);

            stabilityPointsStateMap.put(curState.getId(), cluster);

            RawDataPointGrouped groupedPoint = new RawDataPointGrouped(data.get(i), curState.getId());
            groupedData.add(groupedPoint);
        }

        Mergeable.mergeEntries(outputStates);

        StageDataState result = new StageDataState(groupedData, inputName, inputType, outputStates);
        return result;
    }

    protected IState createState(ClusterPointValue stabilityData, Double start, Double end) {
        IState curState = new StateDMV(inputName, start, end, stabilityData.getClusterMin(),
                stabilityData.getClusterMax());
        return curState;
    }
}

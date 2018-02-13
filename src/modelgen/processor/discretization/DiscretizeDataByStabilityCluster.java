package modelgen.processor.discretization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import modelgen.shared.Logger;
import modelgen.shared.clustering.AgglomerativeClusteringMax;
import modelgen.shared.clustering.ClusteringAlgorithm;
import modelgen.shared.clustering.ICluster;

public class DiscretizeDataByStabilityCluster extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {
    final private static String PD_VAR_COEFF = PD_PREFIX + "VAR_COEFF";
    final private static String PD_MAX_UNIQUE_VALUES = PD_PREFIX + "MAX_UNIQUE_VALUES";

    final private double VAR_COEFF = 0.25;
    final private Integer VALUE_BASE_COST = 3;
    final private Integer MAX_UNIQUE_STATES = 3;

    PropertyDouble varCoefficient;
    PropertyInteger maxUniqueStates;

    protected RawDataChunk inputData;
    protected ControlType inputType;
    protected String inputName;

    protected Map<Integer, ClusterPointValue> stabilityPoints;
    
    public DiscretizeDataByStabilityCluster() {
        ERROR_PREFIX = "DataProcessor: DiscretizeDataByStabilityCluster error. ";
        DEBUG_PREFIX = "DataProcessor: DiscretizeDataByStabilityCluster debug. ";

        name = "StabilityCluster";

        varCoefficient = new PropertyDouble(PD_VAR_COEFF);
        varCoefficient.setValue(VAR_COEFF);

        valueBaseCost.setValue(VALUE_BASE_COST);

        maxUniqueStates = new PropertyInteger(PD_MAX_UNIQUE_VALUES);
        maxUniqueStates.setValue(MAX_UNIQUE_STATES);
        
        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(varCoefficient.getName(), varCoefficient);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public DiscretizeDataByStabilityCluster(StageDataRaw inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
    }

    @Override
    public int processCost() {
        return processCost(inputData);
    }
    
    protected int processCost(RawDataChunk data) {
        try {
            if (data == null || inputType == ControlType.INPUT)
                return -1;

            if (stabilityPoints != null)
                return costFunction();

            List<ClusterPointValue> clusterPointArray = createClusterPoints(data);

            ClusteringAlgorithm<ClusterPointValue> clusterAlgorithm = new AgglomerativeClusteringMax<>();
            ICluster<ClusterPointValue> root = clusterAlgorithm.formDendrogram(clusterPointArray);
            List<ClusterPointValue> stabilityPointsArray = clusterAlgorithm.layerSearch(root, varCoefficient.getValue());

            if (stabilityPointsArray == null)
                return -1;

            stabilityPoints = new HashMap<>();
            for (int i = 0; i < stabilityPointsArray.size(); i++) {
                stabilityPoints.put(i + 1, stabilityPointsArray.get(i));
            }

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

    protected int costFunction() {
        try {
            if (stabilityPoints == null || inputType == ControlType.INPUT)
                return -1;
            
            if (stabilityPoints.size() > maxUniqueStates.getValue())
                return -1;

            return stabilityPoints.size()*valueBaseCost.getValue();
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    @Override
    public StageDataState processData() {
        return processData(inputData);
    }

    protected StageDataState processData(RawDataChunk data) {
        try {
            if (data == null)
                return null;

            if (costFunction() < 0)
                return null;

            RawDataChunkGrouped groupedData = groupDataPoints(data, stabilityPoints);
            List<IState> outputStates = createOutputStates(groupedData, stabilityPoints);

            StageDataState result = new StageDataState(groupedData, inputName, inputType, outputStates);

            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
    
    protected RawDataChunkGrouped groupDataPoints(RawDataChunk data, Map<Integer, ClusterPointValue> stabilityData)
                                                  throws NullPointerException, ArrayIndexOutOfBoundsException {
        RawDataChunkGrouped groupedData = new RawDataChunkGrouped();
        for (RawDataPoint point: data) {

            Integer pointGroup = -1;
            double minDistance = 0;

            for (Integer curGroup: stabilityData.keySet()) {
                ClusterPointValue clusterPoint = stabilityData.get(curGroup);
                double distance = Math.abs(clusterPoint.getClusterCenter() - point.getValue());
                
                if (pointGroup < 0 || distance < minDistance) {
                    minDistance = distance;
                    pointGroup = curGroup;
                }
            }

            RawDataPointGrouped groupedPoint = new RawDataPointGrouped(point, pointGroup);
            groupedData.add(groupedPoint);
        }
        return groupedData;
    }
    
    protected List<IState> createOutputStates(RawDataChunkGrouped data, Map<Integer, ClusterPointValue> stabilityData)
                                              throws NullPointerException, ArrayIndexOutOfBoundsException {
        List<IState> outputStates = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            Double start, end;
            start = data.get(i).getTime();
            if (i != data.size() - 1)
                end = data.get(i + 1).getTime();
            else
                end = data.get(i).getTime();

            int pointGroup = data.get(i).getGroup();
            IState curState = createState(stabilityData.get(pointGroup), start, end, pointGroup);

            outputStates.add(curState);
        }
        Mergeable.mergeEntries(outputStates);
        return outputStates;
    }

    protected IState createState(ClusterPointValue stabilityData, Double start, Double end, int pointGroup) {
        double groupValue = stabilityData.getClusterCenter();
        IState curState = new StateDMV(inputName, pointGroup, start, end, groupValue);
        return curState;
    }
}

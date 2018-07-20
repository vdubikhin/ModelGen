package modelgen.processor.discretization;

import java.util.ArrayList;
import java.util.List;

import modelgen.data.ControlType;
import modelgen.data.complex.ClusterPointValue;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyDouble;
import modelgen.data.property.PropertyManager;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.stage.StageDataRaw;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.data.state.StateContinousRange;
import modelgen.shared.Logger;
import modelgen.shared.Util;

public class DiscretizeDataByDerivativeCluster extends DiscretizeDataByStabilityCluster {
    final private Integer VALUE_BASE_COST = 3;
    final private Integer MAX_UNIQUE_STATES = 100;
    final private double VAR_COEFF = 0.5;

    private RawDataChunk derivData;

    public DiscretizeDataByDerivativeCluster() {
        super();
        name = "DerivativeCluster";
        
        Properties moduleProperties = propertyManager.getModuleProperties();

        valueBaseCost.setValue(VALUE_BASE_COST);
        maxUniqueStates.setValue(MAX_UNIQUE_STATES);
        varCoefficient.setValue(VAR_COEFF);

        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(valueBaseCost.getName(), maxUniqueStates);
        moduleProperties.put(varCoefficient.getName(), varCoefficient);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);

        derivData = null;
    }

    public DiscretizeDataByDerivativeCluster(StageDataRaw inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
        derivData = Util.calculateFirstDerivative(this.inputData);
    }

    @Override
    public double processCost() {
        if (inputType == ControlType.INPUT)
            return -1;

        return processCost(derivData);
    }

    @Override
    public StageDataState processData() {
        try {
            if (inputType == ControlType.INPUT)
                return null;

            StageDataState result = processData(derivData);

            if (result == null)
                return null;

            result = markInputData(result);
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    protected StageDataState markInputData(StageDataState result) {
        RawDataChunkGrouped groupedDataPoints = new RawDataChunkGrouped();
        for (int i = 0; i < result.getData().size(); i++) {
            int curGroup = result.getData().get(i).getGroup();
            RawDataPointGrouped groupedPoint = new RawDataPointGrouped(inputData.get(i), curGroup);
            groupedDataPoints.add(groupedPoint);
        }

        result = new StageDataState(groupedDataPoints, result.getName(), result.getType(), result.getStates());
        return result;
    }

    @Override
    protected IState createState(ClusterPointValue stabilityData, Double start, Double end) {
        double boundaryLow = stabilityData.getClusterMin();
        double boundaryHigh = stabilityData.getClusterMax();
        
        int i;
        for (i = 0; i < inputData.size(); i ++) {
            if (inputData.get(i).getTime().equals(start))
                break;
        }
        IState curState = new StateContinousRange(inputName, start, end, boundaryLow, boundaryHigh,
                inputData.get(i).getValue());
        return curState;
    }

    @Override
    protected double costFunction() {
        try {
            if (cost != null)
                return cost;

            if (stabilityPoints == null || inputType == ControlType.INPUT)
                return -1;

            if (stabilityPoints.size() > maxUniqueStates.getValue())
                return -1;

            StageDataState result = createOutputData(derivData, stabilityPoints);

            if (result == null)
                return -1;

            result = markInputData(result);

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
   
    protected ClusterPointValue getClosestCluster(RawDataPoint point, List<ClusterPointValue> stabilityData)
            throws NullPointerException, ArrayIndexOutOfBoundsException {
        double minDistance = Double.POSITIVE_INFINITY;
        ClusterPointValue bestCluster = null;
        for (ClusterPointValue cluster: stabilityData) {
            double distance;
            if (Math.signum(cluster.getClusterCenter() ) == Math.signum(point.getValue()))
                distance = Math.abs(cluster.getClusterCenter() - point.getValue());
            else
                distance = Double.MAX_VALUE;

            if (distance < minDistance) {
                minDistance = distance;
                bestCluster = cluster;
            }
        }
        return bestCluster;
    }
}

package modelgen.processor.discretization;

import java.util.Map;

import modelgen.data.complex.ClusterPointValue;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyManager;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.state.IState;
import modelgen.data.state.StateContinousRange;
import modelgen.shared.Logger;
import modelgen.shared.Util;

public class DiscretizeDataByDerivativeCluster extends DiscretizeDataByStabilityCluster {
    final private Integer VALUE_BASE_COST = 5;
    final private Integer MAX_UNIQUE_STATES = 20;
    
    private RawDataChunk derivData;

    public DiscretizeDataByDerivativeCluster() {
        super();
        name = "DerivativeCluster";
        
        Properties moduleProperties = propertyManager.getModuleProperties();

        valueBaseCost.setValue(VALUE_BASE_COST);
        maxUniqueStates.setValue(MAX_UNIQUE_STATES);

        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(valueBaseCost.getName(), maxUniqueStates);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);

        derivData = null;
    }

    public DiscretizeDataByDerivativeCluster(DataInput inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
        derivData = Util.calculateFirstDerivative(this.inputData);
    }
    
    @Override
    public int processCost() {
        return processCost(derivData);
    }
    
    @Override
    public DataOutput processData() {
        try {
            DataOutput result = processData(derivData);

            if (result == null)
                return null;

            RawDataChunkGrouped groupedDataPoints = new RawDataChunkGrouped();
            for (int i = 0; i < result.getData().size(); i++) {
                int curGroup = result.getData().get(i).getGroup();
                RawDataPointGrouped groupedPoint = new RawDataPointGrouped(inputData.get(i), curGroup);
                groupedDataPoints.add(groupedPoint);
            }

            result = new DataOutput(groupedDataPoints, result.getName(), result.getType(), result.getStates());
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    @Override
    protected IState createState(ClusterPointValue stabilityData, Double start, Double end, int pointGroup) {
        double boundaryLow = stabilityData.getClusterMin();
        double boundaryHigh = stabilityData.getClusterMax();
        IState curState = new StateContinousRange(inputName, pointGroup, start, end, boundaryLow, boundaryHigh);
        return curState;
    }
}

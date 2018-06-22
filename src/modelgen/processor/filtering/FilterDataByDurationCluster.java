package modelgen.processor.filtering;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import modelgen.data.ControlType;
import modelgen.data.complex.ClusterPointNumberHelper;
import modelgen.data.complex.ClusterPointNumbers;
import modelgen.data.complex.Mergeable;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyDouble;
import modelgen.data.property.PropertyManager;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.shared.Logger;
import modelgen.shared.Util;
import modelgen.shared.clustering.AgglomerativeClusteringMax;
import modelgen.shared.clustering.ClusteringAlgorithm;
import modelgen.shared.clustering.ICluster;

public class FilterDataByDurationCluster extends FilterDataBase implements IDataProcessor<StageDataState> {
    final private static String PD_DISTRIBUTION_PEAK = PD_PREFIX + "DISTRIBUTION_PEAK";
    final private static String PD_RELATIVE_THRESHOLD = PD_PREFIX + "RELATIVE_THRESHOLD";

    final private Integer VALUE_BASE_COST = 5;
    final private Double DISTRIBUTION_PEAK = 0.6;
    final private Double RELATIVE_THRESHOLD = 0.2;

    protected RawDataChunkGrouped inputData;
    protected ControlType inputType;
    protected String inputName;
    protected List<IState> inputStates;

    PropertyDouble distributionPeak;
    PropertyDouble relativeThreshold;

    List<IState> filteredStates;

    public FilterDataByDurationCluster() {
        ERROR_PREFIX = "DataProcessor: FilterDataByDuration error. ";
        DEBUG_PREFIX = "DataProcessor: FilterDataByDuration debug. ";

        name = "FilterDuration";

        valueBaseCost.setValue(VALUE_BASE_COST);

        distributionPeak = new PropertyDouble(PD_DISTRIBUTION_PEAK);
        distributionPeak.setValue(DISTRIBUTION_PEAK);

        relativeThreshold = new PropertyDouble(PD_RELATIVE_THRESHOLD);
        relativeThreshold.setValue(RELATIVE_THRESHOLD);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(relativeThreshold.getName(), relativeThreshold);
        moduleProperties.put(distributionPeak.getName(), distributionPeak);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public FilterDataByDurationCluster(StageDataState inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
        this.inputStates = inputData.getStates();
    }

    @Override
    public double processCost() {
        try {
            if (inputStates == null)
                return -1;

            if (filteredStates != null)
                return costFunction(filteredStates, inputStates, inputData);

            double totalDuration = inputStates.stream()
                    .mapToDouble(s -> s.getDuration())
                    .sum();

            //Remove 0 duration states
            filteredStates = inputStates.stream()
                    .filter(s -> s.getDuration() > 0.0)
                    .collect(Collectors.toList());

            List<ClusterPointNumbers> clusterPointArray = createClusterPoints(filteredStates);
            double minDuration = -Double.MAX_VALUE;
            double clMaxDuration = 0;

            //Do not perform clusterization if all clusters are going to be preserved
            if (distributionPeak.getValue() < 1.0) {
                //Clusterize filtered states to find cluster representing the majority of durations 
                ClusteringAlgorithm<ClusterPointNumbers> clusterAlgorithm = new AgglomerativeClusteringMax<>();
                ICluster<ClusterPointNumbers> root = clusterAlgorithm.formDendrogram(clusterPointArray);
                clusterPointArray = clusterAlgorithm.layerSearch(root, distributionPeak.getValue());

                //Find cut off duration
                //TODO: add parameter and think about it
                for (ClusterPointNumbers cl: clusterPointArray) {
                    Logger.debugPrintln("Cl min: " + cl.getClusterMin() + " Cl max: " + cl.getClusterMax() + 
                            " Cl duration: " + cl.getClusterDuraion() + " Cl eval: " + cl.evaluate(), debugPrint.getValue());
                    if (cl.evaluate() > 0.20 && (minDuration > cl.getClusterMin() || minDuration < 0)) {
                        clMaxDuration = cl.evaluate();
                        minDuration = cl.getClusterMin();
                    }
                    
//                    if (cl.evaluate() > clMaxDuration) {
//                        clMaxDuration = cl.evaluate();
//                        minDuration = cl.getClusterMin();
//                    }
                }
            }

            Logger.debugPrintln("Signal " + inputName + " minDuration: " + minDuration +
                    " clMaxDuration: " + clMaxDuration + " Size: " + clusterPointArray.size(), debugPrint.getValue());

            //Use cut off duration to remove noise states
            filteredStates.clear();
            for (IState curState: inputStates) {
                if ((curState.getDuration() >= minDuration
                        || curState.getDuration() >= totalDuration * relativeThreshold.getValue())
                        && curState.getDuration() > 0.0)
                    filteredStates.add(curState);
            }

            if (filteredStates.isEmpty())
                return -1;

            //Use noise states to expand filtered states
            filteredStates = correctStates(inputStates, filteredStates);
            if (filteredStates == null)
                return -1;

            Mergeable.mergeEntries(filteredStates);

            if (filteredStates == null)
                Logger.errorLogger(ERROR_PREFIX + " Failed to filter signal: " + inputName);

            return costFunction(filteredStates, inputStates, inputData);
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    protected List<ClusterPointNumbers> createClusterPoints(List<IState> inputData)
            throws NullPointerException, ArrayIndexOutOfBoundsException {
        ClusterPointNumberHelper evalHelper = new ClusterPointNumberHelper();
        List<ClusterPointNumbers> clusterPointArray = new ArrayList<>();
        for (IState curPoint: inputData) {
            clusterPointArray.add(new ClusterPointNumbers(curPoint.getDuration(), evalHelper));
        }
        return clusterPointArray;
    }

    @Override
    public StageDataState processData() {
        try {
//            List<IState> filteredStates = new ArrayList<IState>();
//            for (IState curState: inputStates) {
//                if (!statesToFilter.contains(curState.getId()))
//                    filteredStates.add(curState);
//            }
            if (costFunction(filteredStates, inputStates, inputData) < 0)
                return null;

            RawDataChunkGrouped outputData = new RawDataChunkGrouped();
            for (RawDataPointGrouped dataPoint: inputData) {
                for (IState state: filteredStates) {
                   double stateStart = state.getTimeStamp().getKey();
                   double stateEnd = state.getTimeStamp().getValue();

                   if (dataPoint.getTime() >= stateStart && dataPoint.getTime() <= stateEnd) {
                       outputData.add(new RawDataPointGrouped(dataPoint, state.getId()));
                       break;
                   }
                }
            }
            
            StageDataState result = new StageDataState(outputData, inputName, inputType, filteredStates);
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

package modelgen.processor.filtering;

import java.util.ArrayList;
import java.util.List;

import modelgen.data.ControlType;
import modelgen.data.complex.ClusterPointNumberHelper;
import modelgen.data.complex.ClusterPointNumbers;
import modelgen.data.complex.Mergeable;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyDouble;
import modelgen.data.property.PropertyManager;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.raw.RawDataPointGrouped;
import modelgen.data.state.IState;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.processor.discretization.DataOutput;
import modelgen.shared.Logger;
import modelgen.shared.clustering.AgglomerativeClusteringMax;
import modelgen.shared.clustering.ClusteringAlgorithm;
import modelgen.shared.clustering.ICluster;

public class FilterDataByDurationCluster extends DataProcessor<DataOutput> implements IDataProcessor<DataOutput> {
//    final private static String PD_DURATION_CUTOFF = PD_PREFIX + "DURATION_CUTOFF";
    final private static String PD_DISTRIBUTION_PEAK = PD_PREFIX + "DISTRIBUTION_PEAK";

    final private Integer VALUE_BASE_COST = 5;
//    final private Double DURATION_CUTOFF = 0.01;
    final private Double DISTRIBUTION_PEAK = 0.8;

    protected RawDataChunkGrouped inputData;
    protected ControlType inputType;
    protected String inputName;
    protected List<IState> inputStates;

//    PropertyDouble durationCutOff;
    PropertyDouble distributionPeak;

    List<IState> filteredStates;

    public FilterDataByDurationCluster() {
        ERROR_PREFIX = "DataProcessor: FilterDataByDuration error. ";
        DEBUG_PREFIX = "DataProcessor: FilterDataByDuration debug. ";

        name = "FilterDuration";

        valueBaseCost.setValue(VALUE_BASE_COST);
        
//        durationCutOff = new PropertyDouble(PD_DURATION_CUTOFF);
//        durationCutOff.setValue(DURATION_CUTOFF);

        distributionPeak = new PropertyDouble(PD_DISTRIBUTION_PEAK);
        distributionPeak.setValue(DISTRIBUTION_PEAK); 

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
//        moduleProperties.put(durationCutOff.getName(), durationCutOff);
        moduleProperties.put(distributionPeak.getName(), distributionPeak);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public FilterDataByDurationCluster(DataOutput inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
        this.inputStates = inputData.getStates();
    }

    @Override
    public int processCost() {
        try {
            if (inputStates == null)
                return -1;
            
            if (filteredStates != null)
                return costFunction();

            
            filteredStates = new ArrayList<>(inputStates);
            
          //Sort by duration
//            filteredStates.sort((s1,s2) -> Double.compare(s1.getDuration(), s2.getDuration()));
//            
//            double totalDuration = 0.0;
//            for (IState curState: inputStates) 
//                totalDuration += curState.getDuration();
//
//            double maxDurationToFilter = totalDuration * durationCutOff.getValue();
//            double filteredDuration = 0;
//            int startIndex;
//
//            //Find cut off point
//            for (startIndex = 0; startIndex < filteredStates.size(); startIndex += 1) {
//                double curDuration = filteredStates.get(startIndex).getDuration();
//                if (filteredDuration + curDuration > maxDurationToFilter)
//                    break;
//
//                filteredDuration += curDuration;
//            }
//
//            filteredStates = filteredStates.subList(startIndex, filteredStates.size());
////            filteredStates.sort((s1,s2) -> {
//                double s1Start = s1.getTimeStamp().getKey();
//                double s2Start = s2.getTimeStamp().getKey();
//                return Double.compare(s1Start, s2Start);
//            });
//            Logger.debugPrintln("Signal " + inputName + " filteredDuration: " + filteredDuration + 
//                    " maxDurationToFilter: " + maxDurationToFilter + " Size: " + filteredStates.size(), debugPrint.getValue());

            List<ClusterPointNumbers> clusterPointArray = createClusterPoints(filteredStates);

            //Clusterize filtered states to find cluster representing the majority of durations 
            ClusteringAlgorithm<ClusterPointNumbers> clusterAlgorithm = new AgglomerativeClusteringMax<>();
            ICluster<ClusterPointNumbers> root = clusterAlgorithm.formDendrogram(clusterPointArray);
            clusterPointArray = clusterAlgorithm.layerSearch(root, distributionPeak.getValue());

            //Find cut off duration
            double minDuration = Double.MAX_VALUE;
            double clMaxDuration = 0;
            for (ClusterPointNumbers cl: clusterPointArray) {
//                Logger.debugPrintln("Cl min: " + cl.getClusterMin() + "Cl max: " + cl.getClusterMax() + 
//                        " Cl duration: " + cl.getClusterDuraion() + " Cl eval: " + cl.evaluate(), debugPrint.getValue());
                if (cl.evaluate() > clMaxDuration) {
                    clMaxDuration = cl.evaluate();
                    minDuration = cl.getClusterMin();
                }
            }
//            Logger.debugPrintln("Signal " + inputName + " minDuration: " + minDuration +
//                    " clMaxDuration: " + clMaxDuration + " Size: " + clusterPointArray.size(), debugPrint.getValue());

            //Use cut off duration to remove noise states
            filteredStates.clear();
            for (IState curState: inputStates) {
                if (curState.getDuration() >= minDuration)
                    filteredStates.add(curState);
//                else
//                    Logger.debugPrintln("Filtered state: " + curState.getSignalName() + " Id: " + curState.getId() +
//                            " Duration: " + curState.getDuration(), debugPrint.getValue());
            }

            //Use noise states to expand filtered states
            filteredStates = correctStates(inputStates, filteredStates);
            Mergeable.mergeEntries(filteredStates);

            if (filteredStates == null)
                Logger.errorLogger(ERROR_PREFIX + " Failed to filter signal: " + inputName);

            return costFunction();
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

    //XXX: should represent precision loss??
    private int costFunction() throws NullPointerException {
        if (filteredStates != null)
            return (inputStates.size() - filteredStates.size()) * valueBaseCost.getValue() + 1;

        return -1;
    }

    private List<IState> correctStates(List<IState> originalStates, List<IState> filteredStates) 
                               throws NullPointerException, ArrayIndexOutOfBoundsException {
        List<IState> correctedStates = new ArrayList<>(filteredStates.size());
        
        int indexOriginal = 0, indexFiltered = 0;
        IState curCorrectedState = filteredStates.get(indexFiltered).makeCopy();
        List<IState> curNoiseStates = new ArrayList<>();

        for (indexOriginal = 0; indexOriginal < originalStates.size(); indexOriginal++ ) {
            IState curOriginalState = originalStates.get(indexOriginal);
            IState curFilteredState = filteredStates.get(indexFiltered);

            if (curOriginalState.equals(curFilteredState)) {
                if (!curCorrectedState.increaseDuration(curNoiseStates)) {
                    Logger.errorLogger(ERROR_PREFIX + " Failed to merge noise states.");
                    return null;
                }

                curNoiseStates.clear();
                correctedStates.add(curCorrectedState);

                if (indexFiltered + 1 < filteredStates.size())
                    curCorrectedState = filteredStates.get(++indexFiltered).makeCopy();
                else
                    break;
            } else {
                curNoiseStates.add(curOriginalState);
            }
        }

        return correctedStates;
    }

    @Override
    public DataOutput processData() {
        try {
//            List<IState> filteredStates = new ArrayList<IState>();
//            for (IState curState: inputStates) {
//                if (!statesToFilter.contains(curState.getId()))
//                    filteredStates.add(curState);
//            }
            
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
            
            DataOutput result = new DataOutput(outputData, inputName, inputType, filteredStates);
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

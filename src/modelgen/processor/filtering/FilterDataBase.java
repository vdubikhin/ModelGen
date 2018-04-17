package modelgen.processor.filtering;

import java.util.ArrayList;
import java.util.List;

import modelgen.data.ControlType;
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

abstract class FilterDataBase extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {

    protected List<IState> correctStates(List<IState> originalStates, List<IState> filteredStates) 
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

    protected StageDataState processData(List<IState> filteredStates, List<RawDataPointGrouped> inputData,
            String inputName, ControlType inputType) throws ArrayIndexOutOfBoundsException,
                                                            NullPointerException {
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
    }

    //XXX: should represent precision loss??
    protected double costFunction(List<IState> filteredStates, List<IState> inputStates, RawDataChunkGrouped inputData)
            throws NullPointerException {
        if (filteredStates != null) {
            if (filteredStates.size() >= inputStates.size())
                return -1;

            RawDataChunk originalPoints = new RawDataChunk();
            for (RawDataPoint point: inputData)
                originalPoints.add(point);

            RawDataChunk originalData = Util.generateSignalFromStates(originalPoints, inputStates);
            RawDataChunk generatedData = Util.generateSignalFromStates(originalPoints, filteredStates);

            Double difference = Util.compareWaveForms(originalData, generatedData);
            
            return difference/(inputStates.size() - filteredStates.size());
        }
        return -1;
    }

}

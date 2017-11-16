package modelFSM.discretization;

import java.util.ArrayList;
import java.util.HashMap;

import modelFSM.data.DataType;
import modelFSM.data.RawDataChunk;
import modelFSM.data.RawDataChunkGrouped;
import modelFSM.data.RawDataPoint;
import modelFSM.data.RawDataPointGrouped;

public class DetectDMVbyStability implements HandlerDMV {
    private final int MIN_POINTS = 10;
    private final int WINDOW_SIZE = 5;
    private final double VAR_COEFF = 0.05;

    private RawDataChunkGrouped groupedData;
    private ArrayList<Double> stabilityValues;

    DetectDMVbyStability(RawDataChunk inputData) {
        
        groupedData = new RawDataChunkGrouped();
        for (RawDataPoint curDataPoint: inputData) {
            RawDataPointGrouped dataPointGrouped = new RawDataPointGrouped(curDataPoint, -1);
            groupedData.add(dataPointGrouped);
        }
    }

    @Override
    public DataType detectDMV() {
        try {
            if (groupedData.size() < MIN_POINTS) {
                System.out.println("Not enough data points to check signal for DMV via stability.");
                return null;
            }

            stabilityValues = new ArrayList<>();
            for (int i = 0; i < groupedData.size() - WINDOW_SIZE; i = i + WINDOW_SIZE) {
                double curAverage = calculateAverage(groupedData, i, WINDOW_SIZE);
                //Check that current window is stable and save average
                if (checkWindowStability(groupedData, i, WINDOW_SIZE, curAverage))
                    stabilityValues.add(curAverage);
            }

            return stabilityValues.size()*WINDOW_SIZE/(groupedData.size() - WINDOW_SIZE) < VAR_COEFF ?
                    DataType.DMV : DataType.CONTINOUS;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void markDataPoints(RawDataChunkGrouped groupedData, int startIndex, int numElements,
                                int group) throws IndexOutOfBoundsException {
        for (int i = startIndex; i < startIndex + numElements; i++) {
            RawDataPointGrouped curDataPoint = groupedData.get(i + 1);
            curDataPoint.group = group;
        }
    }
    
    private boolean checkWindowStability(RawDataChunkGrouped groupedData, int startIndex, int numElements,
                                         double average) throws IndexOutOfBoundsException {
        double standardDeviation = 0;
        for (int i = startIndex; i < startIndex + numElements; i++)
            standardDeviation += Math.pow(groupedData.get(i + 1).value - average, 2);

        standardDeviation = Math.sqrt(standardDeviation/numElements);
        if (Math.abs(standardDeviation/average) < VAR_COEFF)
            return true;

        return false;
    }
    
    //TODO: move to utils if used elsewhere
    private double calculateAverage(RawDataChunkGrouped groupedData, int startIndex, int numElements) 
                                    throws IndexOutOfBoundsException {
        double average = 0;
        for (int i = startIndex; i < startIndex + numElements; i++)
            average = average + groupedData.get(i).value;

        return average/numElements;
    }

}

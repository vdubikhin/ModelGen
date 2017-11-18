package modelFSM.discretization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modelFSM.data.DataType;
import modelFSM.data.RawDataChunk;
import modelFSM.data.RawDataChunkGrouped;
import modelFSM.data.RawDataPoint;
import modelFSM.data.RawDataPointGrouped;
import modelFSM.data.event.EventInfo;
import modelFSM.shared.Util;

class DiscretizeDataByStability implements DataDiscretizer {
    private int MIN_POINTS = 10;
    private int WINDOW_SIZE = 2;
    private double VAR_COEFF = 0.05;
    private double MEAN_THRESHOLD = 0.01;
    private String DEBUG_SUFFIX = "";
    private boolean DEBUG_PRINT = true;
    
    private class StabilityValues{
        double average;
        double duration;
        double standardDeviation;
        int numPoints;
        
        boolean compareTo(StabilityValues compareTo) {
            if ( Math.abs( (this.average - compareTo.average)/Math.min(this.average, compareTo.average) ) < VAR_COEFF )
                return true;
            return false;
        }
        
        void merge(StabilityValues merge) {
            double averageTotal = (this.numPoints * this.average + merge.numPoints * merge.average)/
                                  (this.numPoints + merge.numPoints);
            double durationTotal = this.duration + merge.duration;
            int numPointsTotal = this.numPoints + merge.numPoints;
            double standardDeviationTotal = Math.sqrt( (this.numPoints * Math.pow(this.standardDeviation, 2) +
                                                        merge.numPoints * Math.pow(merge.standardDeviation, 2) +
                                                        this.numPoints * Math.pow(this.average - averageTotal, 2) + 
                                                        merge.numPoints * Math.pow(merge.average - averageTotal, 2))/numPointsTotal );
            
            this.average = averageTotal;
            this.duration = durationTotal;
            this.standardDeviation = standardDeviationTotal;
            this.numPoints = numPointsTotal;
        }
    }

    private RawDataChunkGrouped groupedData;
    private ArrayList<StabilityValues> stabilityValues;

    public DiscretizeDataByStability(RawDataChunkGrouped groupedData) {
        this.groupedData = groupedData;
        stabilityValues = new ArrayList<>();
    }

    @Override
    public boolean canDiscretizeData() {
        try {
            if (groupedData.size() < MIN_POINTS) {
                Util.debugPrintln(DEBUG_SUFFIX + "Not enough data points to check signal for DMV via stability.", DEBUG_PRINT);
                return false;
            }

            ArrayList<StabilityValues> potentialStabilityValues = new ArrayList<>();
            double minStandardDeviation = -1;
            for (int i = 0; i < groupedData.size(); i = i + WINDOW_SIZE) {
                //Determine max amount of points to process
                int max_step = i + WINDOW_SIZE < groupedData.size() ? WINDOW_SIZE : groupedData.size() - i;
                double curAverage = calculateAverage(groupedData, i, max_step);
                double standardDeviation = calculateStandardDeviation(groupedData, i, max_step, curAverage);

                StabilityValues stabilityValue = new StabilityValues();
                stabilityValue.average = curAverage;
                stabilityValue.duration = groupedData.get(Math.min(max_step + i, groupedData.size() - 1)).time
                                        - groupedData.get(i).time;
                stabilityValue.numPoints = max_step;
                stabilityValue.standardDeviation = standardDeviation;
                
                //Check if window stability can be calculated via coefficient of variation
                if (curAverage >= MEAN_THRESHOLD) {
                  //Check that current window is stable
                    if (Math.abs(standardDeviation/curAverage) < VAR_COEFF) {
                        stabilityValues.add(stabilityValue);

                        if (minStandardDeviation >= 0)
                            minStandardDeviation = Math.min(minStandardDeviation, standardDeviation);
                        else
                            minStandardDeviation = standardDeviation;
                    }
                } else {
                    potentialStabilityValues.add(stabilityValue);
                }
            }

            //Process potential stability values
            //Data points with average value close to zero are considered stable, if their standard deviation
            //is not worse than that of a stable region, determined via coefficient of variation with the
            //lowest standard deviation. If no such reference regions exist, then no stability areas are processed. 
            if (minStandardDeviation >= 0) {
                for (StabilityValues stabilityValue: potentialStabilityValues) {
                    //Comparison is inclusive to include special case, when standard deviation is zero
                    if (stabilityValue.standardDeviation <= minStandardDeviation)
                        stabilityValues.add(stabilityValue);
                }
            }

            double stableTime = 0;
            for (StabilityValues stabilityValue: stabilityValues)
                stableTime += stabilityValue.duration;

            //Assuming time is linear..
            double totalTime = groupedData.get(groupedData.size() - 1).time - groupedData.get(0).time;
            Util.debugPrintln(DEBUG_SUFFIX + "stable time: " + stableTime + " total time: " + totalTime, DEBUG_PRINT);
            return stableTime/totalTime >= (1 - VAR_COEFF) ? true : false;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<EventInfo> discretizeData() {
        try {
            if (groupedData.size() < MIN_POINTS) {
                Util.debugPrintln(DEBUG_SUFFIX + "Not enough data points to check signal for DMV via stability.", DEBUG_PRINT);
                return null;
            }
            
            if (stabilityValues.isEmpty()) {
                Util.debugPrintln(DEBUG_SUFFIX + "No stability values detected", DEBUG_PRINT);
                return null;
            }
            
            //minimize number of stability values by merging averages close to each other.
            ArrayList<StabilityValues> mergedStabilityValues = new ArrayList<>();
            for (StabilityValues stabilityValue: stabilityValues) {
                int mergeIndex;
                boolean merge = false;
                for (mergeIndex = 0; mergeIndex < mergedStabilityValues.size(); mergeIndex++) {
                    StabilityValues mergeStabilityValue = mergedStabilityValues.get(mergeIndex);
                    //Check if relative distance between two sta
                    if (mergeStabilityValue.compareTo(mergeStabilityValue)) {
                        
                    }
                }
            }
            
            
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void markDataPoints(RawDataChunkGrouped data, int startIndex, int numElements,
                                int group) throws IndexOutOfBoundsException {
        for (int i = startIndex; i < startIndex + numElements; i++) {
            RawDataPointGrouped curDataPoint = data.get(i + 1);
            curDataPoint.group = group;
        }
    }

    //TODO: move to utils if used elsewhere
    private double calculateStandardDeviation(RawDataChunkGrouped data, int startIndex, int numElements,
                                              double average) throws IndexOutOfBoundsException {
        double standardDeviation = 0;
        for (int i = startIndex; i < startIndex + numElements; i++)
            standardDeviation += Math.pow(data.get(i).value - average, 2);
        
        standardDeviation = Math.sqrt(standardDeviation/numElements);
        return standardDeviation;
    }
    
    //TODO: move to utils if used elsewhere
    private double calculateAverage(RawDataChunkGrouped data, int startIndex, int numElements) 
                                    throws IndexOutOfBoundsException {
        double average = 0;
        for (int i = startIndex; i < startIndex + numElements; i++)
            average = average + data.get(i).value;

        return average/numElements;
    }

}

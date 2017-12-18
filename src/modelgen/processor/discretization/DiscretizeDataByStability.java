package modelgen.processor.discretization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modelgen.data.property.Properties;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.data.state.IState;
import modelgen.processor.IDataProcessor;
import modelgen.shared.Util;

public class DiscretizeDataByStability implements IDataProcessor<DataOutput> {
    private int WINDOW_SIZE = 25;
    private int MIN_POINTS = WINDOW_SIZE*10;
    private double VAR_COEFF = 0.1;
    private double MEAN_THRESHOLD = 0.01;
    private String DEBUG_SUFFIX = "";
    private boolean DEBUG_PRINT = true;
    
    private class StabilityValues {
        double average;
        double duration;
        double standardDeviation;
        int numPoints;
        
        boolean compareTo(StabilityValues compareTo) {
            if (Math.min(this.average, compareTo.average) < MEAN_THRESHOLD) {
                if (Math.abs( (this.average - compareTo.average) ) < MEAN_THRESHOLD)
                    return true;
                else
                    return false;
            }
            
            if ( Math.abs( (this.average - compareTo.average)/Math.max(this.average, compareTo.average) ) < VAR_COEFF )
                return true;
            return false;
        }
        
        void mergeWith(StabilityValues merge) {
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
    double minStandardDeviation;
    
    public DiscretizeDataByStability(RawDataChunkGrouped groupedData) {
        this.groupedData = groupedData;
        stabilityValues = new ArrayList<>();
    }

    public boolean canDiscretizeData() {
        try {
            if (groupedData.size() < MIN_POINTS) {
                Util.debugPrintln(DEBUG_SUFFIX + "Not enough data points to check signal for DMV via stability.", DEBUG_PRINT);
                return false;
            }
            
            minStandardDeviation = -1;
            double totalTime = groupedData.get(groupedData.size() - 1).time - groupedData.get(0).time;
            ArrayList<StabilityValues> potentialStabilityValues = new ArrayList<>();
            //TODO: Dynamically  recalculate step size, so every chunk is the same time duration.
            for (int i = 0; i < groupedData.size(); i = i + WINDOW_SIZE) {
                //Determine max amount of points to process
                int max_step = i + WINDOW_SIZE < groupedData.size() ? WINDOW_SIZE : groupedData.size() - i;
                //TODO: change average from points based to duration based.
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
                    if (Math.abs(standardDeviation/curAverage) < VAR_COEFF && stabilityValue.duration/totalTime >= (double) max_step/groupedData.size()) {
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
            Util.debugPrintln(DEBUG_SUFFIX + "stable time: " + stableTime + " total time: " + totalTime + " stability: "
                              + String.format( "%.2f", stableTime/totalTime*100) + " %", DEBUG_PRINT);
            return stableTime/totalTime >= (1 - VAR_COEFF) ? true : false;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<IState> discretizeData() {
        try {
            if (groupedData.size() < MIN_POINTS) {
                Util.debugPrintln(DEBUG_SUFFIX + "Not enough data points to check signal for DMV via stability.", DEBUG_PRINT);
                return null;
            }
            
            if (stabilityValues.isEmpty()) {
                Util.debugPrintln(DEBUG_SUFFIX + "No stability values detected", DEBUG_PRINT);
                return null;
            }
            
            Util.debugPrintln(DEBUG_SUFFIX + "Number of averages before merge: " + stabilityValues.size(), DEBUG_PRINT);
//            for (StabilityValues stabilityValue: stabilityValues)
//                Util.debugPrintln(DEBUG_SUFFIX + "Average " + stabilityValue.average + " SD: " + stabilityValue.standardDeviation + " Duration: " +
//                                  stabilityValue.duration, DEBUG_PRINT);
//            
            //minimize number of stability values by merging averages close to each other.
            ArrayList<StabilityValues> mergedStabilityValues = new ArrayList<>();
            boolean merged = true;
            while (merged) {
                merged = false;
                for (StabilityValues stabilityValue: stabilityValues) {
                    boolean mergePossible = false;
                    for (StabilityValues mergeStabilityValue: mergedStabilityValues) {
                        //Check if relative distance between two stable averages is small and merge is possible
                        if (mergeStabilityValue.compareTo(stabilityValue)) {
                            mergeStabilityValue.mergeWith(stabilityValue);
                            mergePossible = true;
                            merged = true;
                            break;
                        }
                    }
                    
                    if (!mergePossible)
                        mergedStabilityValues.add(stabilityValue);
                }
                stabilityValues = mergedStabilityValues;
                mergedStabilityValues = new ArrayList<>();
            }
            
            Util.debugPrintln(DEBUG_SUFFIX + "Number of averages after merge: " + stabilityValues.size(), DEBUG_PRINT);
            for (StabilityValues stabilityValue: stabilityValues)
                Util.debugPrintln(DEBUG_SUFFIX + "Average " + stabilityValue.average + " SD: " + stabilityValue.standardDeviation + " Duration: " +
                                  stabilityValue.duration, DEBUG_PRINT);
            
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

    @Override
    public boolean setProcessorProperties(Properties properties) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Properties getProcessorProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int processCost() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DataOutput processData() {
        // TODO Auto-generated method stub
        return null;
    }

}

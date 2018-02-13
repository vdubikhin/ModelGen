package modelgen.processor.discretization;

import java.util.ArrayList;
import java.util.List;

import modelgen.data.ControlType;
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

public class DiscretizeDataByStability extends DataProcessor<StageDataState> implements IDataProcessor<StageDataState> {
    private class StabilityValue implements Mergeable<StabilityValue> {
        double average;
        double duration;
        double standardDeviation;
        int numPoints;

        @Override
        public boolean canMergeWith(StabilityValue compareTo) {
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

        @Override
        public boolean mergeWith(StabilityValue merge) {
            if (!canMergeWith(merge))
                return false;

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

            return true;
        }
    }

    final private static String PD_WINDOW_SIZE = PD_PREFIX + "WINDOW_SIZE";
    final private static String PD_MIN_POINTS = PD_PREFIX + "MIN_POINTS";
    final private static String PD_VAR_COEFF = PD_PREFIX + "VAR_COEFF";
    final private static String PD_MEAN_THRESHOLD = PD_PREFIX + "MEAN_THRESHOLD";
    final private static String PD_MAX_UNIQUE_VALUES = PD_PREFIX + "MAX_UNIQUE_VALUES";

    final private Integer VALUE_BASE_COST = 2;
    final private Integer MAX_UNIQUE_STATES = 10;
    final private int WINDOW_SIZE = 49;
    final private int MIN_POINTS = WINDOW_SIZE;
    final private double VAR_COEFF = 0.25;
    final private double MEAN_THRESHOLD = 0.1;

    private RawDataChunk inputData;
    private ControlType inputType;
    private String inputName;

    private ArrayList<StabilityValue> stabilityValues;
    double stableTime, totalTime;
    double minStandardDeviation;

    PropertyInteger valueBaseCost;
    PropertyInteger maxUniqueStates;
    PropertyInteger windowSize;
    PropertyInteger minPoints;
    PropertyDouble varCoefficient;
    PropertyDouble meanThreshold;

    public DiscretizeDataByStability() {
        ERROR_PREFIX = "DataProcessor: DiscretizeDataByStability error. ";
        DEBUG_PREFIX = "DataProcessor: DiscretizeDataByStability debug. ";

        stableTime = -1;
        totalTime = -1;

        name = "Stability";

        valueBaseCost.setValue(VALUE_BASE_COST);

        maxUniqueStates = new PropertyInteger(PD_MAX_UNIQUE_VALUES);
        maxUniqueStates.setValue(MAX_UNIQUE_STATES);

        windowSize = new PropertyInteger(PD_WINDOW_SIZE);
        windowSize.setValue(WINDOW_SIZE);
        
        minPoints = new PropertyInteger(PD_MIN_POINTS);
        minPoints.setValue(MIN_POINTS);
        
        varCoefficient = new PropertyDouble(PD_VAR_COEFF);
        varCoefficient.setValue(VAR_COEFF);
        
        meanThreshold = new PropertyDouble(PD_MEAN_THRESHOLD);
        meanThreshold.setValue(MEAN_THRESHOLD);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);
        moduleProperties.put(maxUniqueStates.getName(), maxUniqueStates);
        moduleProperties.put(windowSize.getName(), windowSize);
        moduleProperties.put(minPoints.getName(), minPoints);
        moduleProperties.put(varCoefficient.getName(), varCoefficient);
        moduleProperties.put(meanThreshold.getName(), meanThreshold);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public DiscretizeDataByStability(StageDataRaw inputData) {
        this();
        this.inputData = inputData.getData();
        this.inputType = inputData.getType();
        this.inputName = inputData.getName();
    }

    @Override
    public int processCost() {
        try {
            if (stabilityValues != null)
                return costFunction();

            if (inputData == null)
                return -1;

            if (inputData.size() < minPoints.getValue()) {
                Logger.errorLogger(DEBUG_PREFIX + "Not enough data points to check signal for DMV via stability.");
                return -1;
            }

            totalTime = inputData.get(inputData.size() - 1).getTime() - inputData.get(0).getTime();
            if (totalTime <= 0) {
                Logger.errorLogger(DEBUG_PREFIX + "Total waveform time is less than zero.");
                return -1;
            }

            stabilityValues = new ArrayList<>();
            minStandardDeviation = -1;

            ArrayList<StabilityValue> potentialStabilityValues = new ArrayList<>();

            //TODO: Dynamically  recalculate step size, so every chunk is the same time duration.
            for (int i = 0; i < inputData.size(); i = i + windowSize.getValue()) {

                //Determine max amount of points to process
                int max_step = i + windowSize.getValue() < inputData.size() ? windowSize.getValue() :
                                                                              inputData.size() - i;

                //TODO: change average from points based to duration based.
                double curAverage = calculateAverage(inputData, i, max_step);
                double standardDeviation = calculateStandardDeviation(inputData, i, max_step, curAverage);

                StabilityValue stabilityValue = new StabilityValue();
                stabilityValue.average = curAverage;
                stabilityValue.duration = inputData.get(Math.min(max_step + i, inputData.size() - 1)).getTime()
                                        - inputData.get(i).getTime();
                stabilityValue.numPoints = max_step;
                stabilityValue.standardDeviation = standardDeviation;
                
                //Check if window stability can be calculated via coefficient of variation
                if (Math.abs(curAverage) >= meanThreshold.getValue()) {
                  //Check that current window is stable
                    if (Math.abs(standardDeviation/curAverage) < varCoefficient.getValue() &&
                        stabilityValue.duration/totalTime >= (double) max_step/inputData.size()) {
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
                for (StabilityValue stabilityValue: potentialStabilityValues) {
                    //Comparison is inclusive to include special case, when standard deviation is zero
                    if (stabilityValue.standardDeviation <= minStandardDeviation)
                        stabilityValues.add(stabilityValue);
                }
            }

            Logger.debugPrintln(DEBUG_PREFIX + "Number of averages before merge: " + stabilityValues.size(),
                                debugPrint.getValue());

            Mergeable.mergeEntries(stabilityValues);

            Logger.debugPrintln(DEBUG_PREFIX + "Number of averages after merge: " + stabilityValues.size(),
                                debugPrint.getValue());

            stableTime = 0;
            for (StabilityValue stabilityValue: stabilityValues) {
                stableTime += stabilityValue.duration;
                Logger.debugPrintln(DEBUG_PREFIX + " average: " + stabilityValue.average +
                                    " SD: " + stabilityValue.standardDeviation +
                                    " variation: " + String.format( "%.2f", Math.abs(stabilityValue.standardDeviation/stabilityValue.average)*100) + " %" +
                                    " duration: " + stabilityValue.duration,
                                    debugPrint.getValue());
            }

            Logger.debugPrintln(DEBUG_PREFIX + "stable time: " + stableTime + " total time: " + totalTime + " stability: "
                              + String.format( "%.2f", stableTime/totalTime*100) + " %", debugPrint.getValue());

            //Assuming time is linear..
            return costFunction();
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }

    private int costFunction() {
        try {
            if (stabilityValues == null)
                return -1;
            
            if (stableTime < 0 || totalTime < 0)
                return -1;

            if (stableTime/totalTime >= (1 - varCoefficient.getValue())) {
                if (stabilityValues.size() > 0 && stabilityValues.size() <= maxUniqueStates.getValue())
                    return stabilityValues.size() * valueBaseCost.getValue();
            }
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return -1;
    }
    
    @Override
    public StageDataState processData() {
        try {
            if (costFunction() < 0)
                return null;

            RawDataChunkGrouped groupedData = new RawDataChunkGrouped();
            List<IState> outputStates = new ArrayList<>();
            for (int i = 0; i < inputData.size(); i++) {
                RawDataPoint point = inputData.get(i);

                Integer pointGroup = -1;
                double minDistance = 0;
                Double stabValue = 0.0;

                for (int curGroup = 0; curGroup < stabilityValues.size(); curGroup++) {
                    StabilityValue stabilityValue = stabilityValues.get(curGroup);
                    double value = stabilityValue.average;
                    double distance = Math.abs(value - point.getValue());

                    if (pointGroup < 0 || distance < minDistance) {
                        minDistance = distance;
                        pointGroup = curGroup + 1;
                        stabValue = value;
                    }
                }

                RawDataPointGrouped groupedPoint = new RawDataPointGrouped(point, pointGroup);

                Double start, end;
                start = inputData.get(i).getTime();
                if (i != inputData.size() - 1)
                    end = inputData.get(i + 1).getTime();
                else
                    end = inputData.get(i).getTime();

                IState curState = new StateDMV(inputName, pointGroup, start, end, stabValue);

                outputStates.add(curState);
                groupedData.add(groupedPoint);
            }

            Mergeable.mergeEntries(outputStates);

            StageDataState result = new StageDataState(groupedData, inputName, inputType, outputStates);

            return result;

        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    //TODO: move to utils if used elsewhere
    private double calculateStandardDeviation(RawDataChunk groupedData, int startIndex, int numElements,
                                              double average) throws IndexOutOfBoundsException {
        double standardDeviation = 0;
        for (int i = startIndex; i < startIndex + numElements; i++)
            standardDeviation += Math.pow(groupedData.get(i).getValue() - average, 2);
        
        standardDeviation = Math.sqrt(standardDeviation/numElements);
        return standardDeviation;
    }
    
    //TODO: move to utils if used elsewhere
    private double calculateAverage(RawDataChunk groupedData2, int startIndex, int numElements) 
                                    throws IndexOutOfBoundsException {
        double average = 0;
        for (int i = startIndex; i < startIndex + numElements; i++)
            average = average + groupedData2.get(i).getValue();

        return average/numElements;
    }
}

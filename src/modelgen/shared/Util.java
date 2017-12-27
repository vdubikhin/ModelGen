package modelgen.shared;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.state.IState;

public final class Util {

    private Util() {};
    
    static public Map<String, RawDataChunk> parseCSVFile(String fileName) {
        BufferedReader br = null;
        try {
            String line = "";
            String csvSplitBy = ",";
            boolean firstLine = true;
            String[] csvHeader = null;
            
            if (fileName == null || fileName.isEmpty()) {
                Logger.errorLogger("Failed to parse csv file. Empty or missing name.");
                return null;
            }
            
            br = new BufferedReader(new FileReader(fileName));
            Map<String, RawDataChunk> csvData = new HashMap<>();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                
                if (firstLine) {
                    csvHeader = line.split(csvSplitBy);
                    
                    // At least 2 columns are expected
                    if (csvHeader.length < 2) {
                        Logger.errorLogger("Failed to parse csv file: " + fileName + ". Insufficient data columns.");
                        return null;
                    }
                    
                    // Column 0 is time
                    for (int i = 1; i < csvHeader.length; i++) {
                        csvData.put(csvHeader[i], new RawDataChunk());
                    }
                } else {
                    String[] rawData = line.split(csvSplitBy);
                    
                    if (rawData.length < 2) {
                        Logger.errorLogger("Failed to parse csv file: " + fileName + ". Insufficient raw data.");
                        return null;
                    }
                    
                    if (rawData.length != csvHeader.length) {
                        Logger.errorLogger("Failed to parse csv file: " + fileName + 
                                ". Number of data points not equal to number of columns.");
                        return null;
                    }

                    Double curTime = Double.parseDouble(rawData[0]);

                    for (int i = 1; i < csvHeader.length; i++) {
                        Double curData = Double.parseDouble(rawData[i]);
                        RawDataPoint curDataPoint = new RawDataPoint(curData, curTime);
                        RawDataChunk curDataChunk = csvData.get(csvHeader[i]);
                        curDataChunk.add(curDataPoint);
                    }
                }
                firstLine = false;
            }
            
            return csvData;
        } catch (FileNotFoundException e) {
            Logger.errorLoggerTrace("Failed to parse csv file: " + fileName + ". File not found.", e);
        } catch (IOException e) {
            Logger.errorLoggerTrace("Failed to parse csv file: " + fileName + ". File empty.", e);
        } catch(NumberFormatException e) {
            Logger.errorLoggerTrace("Failed to parse csv file: " + fileName + ". Error on data conversion.", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Logger.errorLoggerTrace("Failed to parse csv file: " + fileName + ". Error on closing the file", e);
                }
            }
        }

        return null;
    }

    static public RawDataChunk calculateFirstDerivative(RawDataChunk inputData) {
        try {
            RawDataChunk outputData = new RawDataChunk();
            for (int i = 0; i < inputData.size(); i++) {
                
                // TODO: Check delta time is not zero
                // Most left point
                if (i == 0) {
                    Double derivPointValue = (inputData.get(i+1).getValue() - inputData.get(i).getValue())/
                                             (inputData.get(i+1).getTime() - inputData.get(i).getTime());
                    outputData.add(new RawDataPoint(derivPointValue, inputData.get(i).getTime()));
                    continue;
                }
                
                // Most right point
                if (i == inputData.size() - 1) { 
                    Double derivPointValue = (inputData.get(i).getValue() - inputData.get(i-1).getValue())/
                                             (inputData.get(i).getTime() - inputData.get(i-1).getTime());
                    outputData.add(new RawDataPoint(derivPointValue, inputData.get(i).getTime()));
                    continue;
                }
                
                // Middle points
                Double derivPointValue = (inputData.get(i+1).getValue() - inputData.get(i-1).getValue())/
                                         (inputData.get(i+1).getTime() - inputData.get(i-1).getTime());
                outputData.add(new RawDataPoint(derivPointValue, inputData.get(i).getTime()));
            }

            return outputData;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace("Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace("Null pointer exception.", e);
        }
        return null;
    }

//    static public ArrayList<Event> dataToEvents(String name, ArrayList<Double> timeArray, ArrayList<Integer> dataArrayGroup) {
//        // Convert read data to event format
//        // Event - duration pair
//        try {
//            ArrayList<Event> outEvents = new ArrayList<Event>();
//            
//            Double minTime = timeArray.get(0);
//            Double maxTime = timeArray.get(0);
//            
//            int curGroup = dataArrayGroup.get(0);
//            
//            for (int i = 0; i < dataArrayGroup.size(); i++) {
//                if (curGroup != dataArrayGroup.get(i)) {
//                    maxTime = timeArray.get(i-1);
//                    Event tempEvent = new Event();
//                    
//                    //TODO: think on treating 0 group as a special one
//                    // Filtering
//                //    if (curGroup != 0) {
//                        tempEvent.eventId = curGroup;
//                        tempEvent.start = minTime;
//                        tempEvent.end = maxTime;
//                        tempEvent.signalName = name;
//                        outEvents.add(tempEvent);
//                  //  }
//                        
//                    minTime = timeArray.get(i);
//                    curGroup = dataArrayGroup.get(i);
//                }
//            }
//            
//            Event tempEvent = new Event();
//            maxTime = timeArray.get(dataArrayGroup.size()-1);
//            tempEvent.eventId = curGroup;
//            tempEvent.start = minTime;
//            tempEvent.end = maxTime;
//            tempEvent.signalName = name;
//            outEvents.add(tempEvent);
//            
//            return outEvents;
//        } catch (ArrayIndexOutOfBoundsException e) {
//            e.printStackTrace();
//        }
//        
//        return null;
//    }
}

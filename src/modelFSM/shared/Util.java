package modelFSM.shared;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import modelFSM.data.Event;
import modelFSM.data.RawDataChunk;
import modelFSM.data.RawDataPoint;

public final class Util {

    private Util() {};
    
    static public void debugPrintln(String string, boolean print) {
        if (print)
            System.out.println(string);
    }
    
    private static final String csvSplitBy = ",";
    static public HashMap<String, RawDataChunk> parseCSVFile(String fileName) {
        BufferedReader br = null;
        String line = "";

        try {
            boolean firstLine = true;
            String[] csvHeader = null;
            br = new BufferedReader(new FileReader(fileName));
            HashMap<String, RawDataChunk> csvData = new HashMap<>();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                
                if (firstLine) {
                    csvHeader = line.split(csvSplitBy);
                    
                    // At least 2 columns are expected
                    if (csvHeader.length < 2) {
                        System.out.println("Failed to parse csv file: " + fileName + ". Insufficient data columns.");
                        return null;
                    }
                    
                    // Column 0 is time
                    for (int i = 1; i < csvHeader.length; i++) {
                        csvData.put(csvHeader[i], new RawDataChunk());
                    }
                } else {
                    String[] rawData = line.split(csvSplitBy);
                    
                    if (rawData.length < 2) {
                        System.out.println("Failed to parse csv file: " + fileName + ". Insufficient raw data.");
                        return null;
                    }
                    
                    if (rawData.length != csvHeader.length) {
                        System.out.println("Failed to parse csv file: " + fileName + 
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
            System.out.println("Failed to parse csv file: " + fileName + ". File not found.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to parse csv file: " + fileName + ". File empty.");
            e.printStackTrace();
        } catch(NumberFormatException e) {
            System.out.println("Failed to parse csv file: " + fileName + ". Error on data conversion.");
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    static public ArrayList<Event> dataToEvents(String name, ArrayList<Double> timeArray, ArrayList<Integer> dataArrayGroup) {
        // Convert read data to event format
        // Event - duration pair
        try {
            ArrayList<Event> outEvents = new ArrayList<Event>();
            
            Double minTime = timeArray.get(0);
            Double maxTime = timeArray.get(0);
            
            int curGroup = dataArrayGroup.get(0);
            
            for (int i = 0; i < dataArrayGroup.size(); i++) {
                if (curGroup != dataArrayGroup.get(i)) {
                    maxTime = timeArray.get(i-1);
                    Event tempEvent = new Event();
                    
                    //TODO: think on treating 0 group as a special one
                    // Filtering
                //    if (curGroup != 0) {
                        tempEvent.eventId = curGroup;
                        tempEvent.start = minTime;
                        tempEvent.end = maxTime;
                        tempEvent.signalName = name;
                        outEvents.add(tempEvent);
                  //  }
                        
                    minTime = timeArray.get(i);
                    curGroup = dataArrayGroup.get(i);
                }
            }
            
            Event tempEvent = new Event();
            maxTime = timeArray.get(dataArrayGroup.size()-1);
            tempEvent.eventId = curGroup;
            tempEvent.start = minTime;
            tempEvent.end = maxTime;
            tempEvent.signalName = name;
            outEvents.add(tempEvent);
            
            return outEvents;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}

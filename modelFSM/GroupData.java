package modelFSM;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

public class GroupData {
    private static final String cvsSplitBy = ",";
    private static final int NUM_MACRO_EVENTS = 2;
    private static final double GROUP_WINDOW_SIZE = 0.03;
    private static final int GROUP_DEPTH_SIZE = 2;
    private static final String MACRO_GROUP_SUF = "_grouped";
    private static final String SIMTRACE_GROUPED_SUF = "_simdata";
    
    private class SignalData {
        ArrayList<Double> timeArray;
        ArrayList<Double> dataArray;
        ArrayList<Double> dataArrayGroup;
        
        SignalData () {
            timeArray = new ArrayList<Double>();
            dataArray = new ArrayList<Double>();
            dataArrayGroup = new ArrayList<Double>();
        }
    }
    
    private class SignalEvents {
        ArrayList<Double> eventsArray;
        ArrayList<Double> timeStartArray;
        ArrayList<Double> timeEndArray;
        
        SignalEvents () {
            eventsArray = new ArrayList<Double>();
            timeStartArray = new ArrayList<Double>();
            timeEndArray = new ArrayList<Double>();
        }
        
        SignalEvents (Integer size) {
            eventsArray = new ArrayList<Double>(size);
            timeStartArray = new ArrayList<Double>(size);
            timeEndArray = new ArrayList<Double>(size);
            
            for (int i = 0; i < size; i++) {
                eventsArray.add((double) 0);
                timeStartArray.add((double) 0);
                timeEndArray.add((double) 0);
            }
        }
    }
    
    private HashMap<String, SignalData> signalDataArray;
    private HashMap<String, SignalEvents> signalEventsArray;
    private HashMap<String, ArrayList<DerivCalc.Event>> signalEventsInfoArray;
    
    private double getRndRate(ArrayList<DerivCalc.Event> eventsInfo, int id) {
        for (int i = 0; i < eventsInfo.size(); i++) {
            if (eventsInfo.get(i).id == id)
                return ThreadLocalRandom.current().nextDouble(eventsInfo.get(i).start, eventsInfo.get(i).end );
        }
        
        return 0.0;
    }
    
    private double getAvgRate(ArrayList<DerivCalc.Event> eventsInfo, int id) {
        for (int i = 0; i < eventsInfo.size(); i++) {
            if (eventsInfo.get(i).id == id)
                return (eventsInfo.get(i).end + eventsInfo.get(i).start)/2;
        }
        
        return 0.0;
    }
    
    private double cmpDataEvents(SignalData simData, SignalEvents eventsData, ArrayList<DerivCalc.Event> eventsInfo) {
        ArrayList<Double> errorArray = new ArrayList<Double>();
        double avgError = 0;
        try {
            Double curGroup = eventsData.eventsArray.get(0);
            Double initValue = simData.dataArray.get(0);
            int eventIndex = 0;
            
            System.out.println("E" + curGroup + " " + "start: " + eventsData.timeStartArray.get(eventIndex) + " end: "
                    + eventsData.timeEndArray.get(eventIndex));
            
            // check that first timestamp matches
            if (eventsData.timeStartArray.get(0) != simData.timeArray.get(0))
                return -1.0;
            
            for (int i = 0; i < simData.timeArray.size(); i++) {
                double curTimeStamp = simData.timeArray.get(i);
                double curValue = simData.dataArray.get(i);
                double error;
                double timeDelta = 0;
                
                // check that eventIndex is within bounds
                
                // if current data point belongs to currently active event
                if (!(curTimeStamp >= eventsData.timeStartArray.get(eventIndex) && 
                        curTimeStamp <= eventsData.timeEndArray.get(eventIndex))) {
                    eventIndex += 1;
                    curGroup = eventsData.eventsArray.get(eventIndex);
                    initValue = curValue;
                    
                    System.out.println("Timestamp: " + curTimeStamp);
                    System.out.println("E" + curGroup + " " + "start: " + eventsData.timeStartArray.get(eventIndex) + " end: "
                            + eventsData.timeEndArray.get(eventIndex));
                }
                
                timeDelta = curTimeStamp - eventsData.timeStartArray.get(eventIndex);
                
                if (timeDelta < 0)
                    return -1.0;
                
                double curRate = getAvgRate(eventsInfo, curGroup.intValue());
                
                if (curValue == 0)
                    error = 0;
                else
                    error = Math.abs((initValue+timeDelta*curRate - curValue)/curValue);
                
                System.out.println("Time:" +curTimeStamp + " Data: " + curValue + " Approximation: " + (initValue+timeDelta*curRate) + " Error: " + error + " Rate: " + curRate);
                
                if (error != 0) {
                    errorArray.add(error);
                    avgError += error;
                }
            }
            
            avgError = avgError/errorArray.size();
            double meanSquared = 0;
            int totalNum = 0;
            
            for (int i = 0; i < errorArray.size(); i++) {
                double delta = errorArray.get(i) - avgError;
                if (errorArray.get(i) > 0 && errorArray.get(i) < avgError*5) {
                    meanSquared += delta*delta;
                    totalNum += 1;
                }

            }
            System.out.println("Avg diff: " + avgError);
            return Math.sqrt(meanSquared/totalNum);
            
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
        
        return -1.0;
    }
    
    public void initSignal(String path) {
        SignalData tempSignal1 = new SignalData();
        
        tempSignal1.timeArray = new ArrayList<Double>();
        tempSignal1.dataArray = new ArrayList<Double>();
        tempSignal1.dataArrayGroup = new ArrayList<Double>();
        
        SignalEvents tempSignal2 = new SignalEvents();
        tempSignal2.eventsArray = new ArrayList<Double>();
        tempSignal2.timeStartArray = new ArrayList<Double>();
        tempSignal2.timeEndArray = new ArrayList<Double>();
        
        ArrayList<DerivCalc.Event> tempEvents = new ArrayList<DerivCalc.Event>();
        
        signalDataArray.put(path, tempSignal1);
        readData(path);
        
        signalEventsArray.put(path, tempSignal2);
        dataToEvents(path);
        
        signalEventsInfoArray.put(path, tempEvents);
        readEventsInfo(path);
        
//        System.out.println("\n\nChecking data vs events original\n\n");
//        System.out.println("Mean squared: " + cmpDataEvents(signalDataArray.get(path), signalEventsArray.get(path), signalEventsInfoArray.get(path)));
//        
        
        System.out.println("\n\nGrouping data\n\n");
        groupEventsV2(path);
        
        groupedEventsToData(path);
//        System.out.println("\n\nChecking data vs events grouped\n\n");
//        System.out.println("Mean squared: " + cmpDataEvents(signalDataArray.get(path), signalEventsArray.get(path+MACRO_GROUP_SUF), signalEventsInfoArray.get(path)));
    }
    
    private void readData(String path) {
        // Read raw data from file and store it
        BufferedReader br = null;
        String line = "";
    
        try {
            boolean firstLine = true;
            SignalData tempSignal = signalDataArray.get(path);
            
            br = new BufferedReader(new FileReader(path+".txt"));
            
            while ((line = br.readLine()) != null) {
                if (!firstLine) {
                    String[] rawData = line.split(cvsSplitBy);
                    tempSignal.timeArray.add(Double.parseDouble(rawData[0]));
                    tempSignal.dataArray.add(Double.parseDouble(rawData[1]));
                    tempSignal.dataArrayGroup.add(Double.parseDouble(rawData[2]));
                }
                firstLine = false;
            }
            signalDataArray.put(path, tempSignal);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
    }
    
    private void dataToEvents(String name) {
        // Convert read data to event format
        // Event - duration pair
        try {
            System.out.println("Test grouping");
            SignalData tempSignalData = signalDataArray.get(name);
            SignalEvents tempSignalEvents = signalEventsArray.get(name);
            
            Double minTime = tempSignalData.timeArray.get(0);
            Double maxTime = 0.0;
            
            double curGroup = tempSignalData.dataArrayGroup.get(0).intValue();
            int curNum = 1;
            
            for (int i = 0; i < tempSignalData.dataArrayGroup.size(); i++) {
                if (curGroup != tempSignalData.dataArrayGroup.get(i).intValue()) {
                    maxTime = tempSignalData.timeArray.get(i-1);
                    // Filtering
                    if (curGroup != 0) {
                        //System.out.print(key + "_E" + curGroup + "(" + (maxTime - minTime) + ")" + nextIdentifier);
                        System.out.println("E" + curGroup + " " + "start: " + minTime + " end: " + maxTime);
                        tempSignalEvents.eventsArray.add(curGroup);
                        tempSignalEvents.timeStartArray.add(minTime);
                        tempSignalEvents.timeEndArray.add(maxTime);
                    }
                    minTime = tempSignalData.timeArray.get(i);
                    curGroup = tempSignalData.dataArrayGroup.get(i).intValue();
                    curNum = 1;
                } else {
                    curNum++;
                }
            }
            maxTime = tempSignalData.timeArray.get(tempSignalData.dataArrayGroup.size()-1);
            tempSignalEvents.eventsArray.add(curGroup);
            tempSignalEvents.timeStartArray.add(minTime);
            tempSignalEvents.timeEndArray.add(maxTime);
            signalEventsArray.put(name, tempSignalEvents); 
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    private void readEventsInfo(String path) {
        // Read raw data from file and store it
        BufferedReader br = null;
        String line = "";
    
        try {
            boolean firstLine = true;
            SignalData tempSignal = signalDataArray.get(path);
            
            br = new BufferedReader(new FileReader(path + "_events" + ".txt"));
            ArrayList<DerivCalc.Event> tempEvents = new ArrayList<DerivCalc.Event>();
            
            while ((line = br.readLine()) != null) {
                if (!firstLine) {
                    String[] rawData = line.split(cvsSplitBy);
                    DerivCalc.Event tempEvent = new DerivCalc.Event();
                    tempEvent.id = Integer.parseInt(rawData[0]);
                    tempEvent.start = Double.parseDouble(rawData[1]);
                    tempEvent.end = Double.parseDouble(rawData[2]);
                    tempEvents.add(tempEvent);
                   // System.out.println(tempEvent.id + " " + tempEvent.start + " " + tempEvent.end);
                }
                firstLine = false;
            }
            
            signalEventsInfoArray.put(path, tempEvents);
            signalDataArray.put(path, tempSignal);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
    }
    
    private double eventsMetric(double val) {
        return val*val;
    }
    
    private double eventsMetricRev(double val) {
        return Math.sqrt(val);
    }
    
    private ArrayList<Integer> numToId(int numId, int size) {
        ArrayList<Integer> vectorId = new ArrayList<Integer>(NUM_MACRO_EVENTS);
        
        int localNumId = numId;
        for (int i = NUM_MACRO_EVENTS; i > 0; i--) {
            int layer = (int) Math.pow(size, i - 1);
            int modulo = localNumId/layer;
            vectorId.add(modulo + 1);
            localNumId = numId % layer;
        }
        
        return vectorId;
    }
    
    private boolean checkPatternCell(HashSet<Integer> pattern, ArrayList<Integer> cell) {
        for (Integer j : pattern) {
            if (!cell.contains(j)) {
                return false;
            }
        }
        
        return true;
    }
    
    private void groupEventsV1(String name) {
        // Group events into macroevents and report score
        try {
            final int numGroups = signalEventsInfoArray.get(name).size();
            final int totalEvents = signalEventsArray.get(name).eventsArray.size();
            final int arraySize = (int) Math.pow(numGroups, NUM_MACRO_EVENTS);
            
            
            ArrayList<Double> resArray = new ArrayList<Double>(totalEvents);
            ArrayList<HashSet<Integer>> resIdArray = new ArrayList<HashSet<Integer>>(totalEvents);
            ArrayList<ArrayList<Double>> patternArray = new ArrayList<ArrayList<Double>>(totalEvents);
            
            double resValuePrevPeak = 0;
            
            for (int i = 0; i < totalEvents; i++) {
                ArrayList<Double> curPatternArray = new ArrayList<Double>(arraySize);
                double maxEventMetric = 0.0;
                double eventDuration = signalEventsArray.get(name).timeEndArray.get(i) - signalEventsArray.get(name).timeStartArray.get(i);
                
                ArrayList<Integer> patternIdMaxVal = new ArrayList<Integer>();
                HashSet<Integer> patternEvents = new HashSet<Integer>();
                HashSet<Integer> prevPatternEvents = new HashSet<Integer>();
                
                double resValue = 0;
                
                
                if (i > 0) {
                    prevPatternEvents = resIdArray.get(i - 1);
                    patternEvents = new HashSet<Integer>(prevPatternEvents);
                    resValue = resArray.get(i - 1);
                }

                int curEvent = signalEventsArray.get(name).eventsArray.get(i).intValue();
                
                // check if macroevent pattern has been fully filled with events
                if (patternEvents.size() < NUM_MACRO_EVENTS) {
                    patternEvents.add(curEvent);
                }
                
                for (int k = 0; k < arraySize; k++) {
                    ArrayList<Integer> patternId = numToId(k, numGroups);
                    double eventMetric = 0;
                    
                    for (int j = 0; j < patternId.size(); j++) {
                        if (curEvent == patternId.get(j)) {
                            eventMetric = eventsMetric(eventDuration);
                            break;
                        }
                    }
                    
                    if (i > 0) {
                        double patternValue = patternArray.get(i - 1).get(k);
                        
                        // filter 0 duration events and just copy entire matrix
                        if (eventMetric > 0) {
                            // check if current cell also belongs to one of events in the pattern
                            if (checkPatternCell(patternEvents, patternId)) {
                                // if it does then continue pattern
                                eventMetric = Math.max(eventsMetric(eventsMetricRev(patternValue) + eventDuration), resValuePrevPeak);
                                //eventMetric = eventMetric + patternValue;
                            } else {
                                // if - not then create new pattern
                                eventMetric = Math.max(eventMetric + patternValue, resValuePrevPeak);
//                                double eventMetricCont = Math.max(eventsMetric(eventsMetricRev(patternValue) + eventDuration), resValuePrevPeak);
//                                if (eventMetricCont > resValue)
//                                    eventMetric = eventMetricCont;
                            }
                            
                            //eventMetric = eventMetric + patternValue;
                            //eventMetric = Math.max(eventMetric + patternValue, eventMetric + resValue);
                        } else {
                            eventMetric = patternValue;
                        }
                    }
                    
                    curPatternArray.add(k, eventMetric);
                    
                    if (eventMetric > maxEventMetric) {
                        patternIdMaxVal = new ArrayList<Integer>(patternId);
                    }
                        
                    maxEventMetric = Math.max(maxEventMetric, eventMetric);
                }
                
                boolean newMacro = false;
                
                if (i > 0) {
                    // check if new max metric value is larger than found previously
                    if (patternEvents.size() == NUM_MACRO_EVENTS && maxEventMetric > resValue) {
                        resValuePrevPeak = resValue;
                        newMacro = true;
                        // create new macroevent
                        patternEvents.clear();
                        for (int j = 0; j < patternIdMaxVal.size(); j++) {
                            patternEvents.add(patternIdMaxVal.get(j));
                        }
                    }
                }
                patternArray.add(curPatternArray);
                resArray.add(maxEventMetric);
                resIdArray.add(patternEvents);
                
                System.out.println("Current event(" + i + "): " + signalEventsArray.get(name).eventsArray.get(i).intValue() + " Duration: " + eventDuration 
                        + " Pattern value: " + maxEventMetric +  " Pattern id: " + patternIdMaxVal.toString() +
                        " resArray: " + resArray.get(i) + " resPeak: " + resValuePrevPeak + " Max pattern id: " + resIdArray.get(i).toString() + " New macro: " + newMacro);
            }
            
            System.out.println("\n\nPrinting pattern events \n");
            
            HashSet<Integer> patternEventsResCur = new HashSet<Integer>();
            HashSet<Integer>  patternEventsResPrev = new HashSet<Integer>();
            int indexStart = 0, indexEnd = 0;
            
            // Check that resid not empty
            patternEventsResCur = resIdArray.get(0);
            patternEventsResPrev = resIdArray.get(0);
            
            for (int i = 1; i < resArray.size(); i++) {
                patternEventsResCur = resIdArray.get(i);
                if ( !patternEventsResCur.equals(patternEventsResPrev)) {
                    System.out.println("Positions: [" + indexStart + ", " + indexEnd + "] Pattern: " + patternEventsResPrev.toString());
                    patternEventsResPrev = new HashSet<Integer>(patternEventsResCur);
                    indexStart = i;
                }
                indexEnd = i;
            }
            
            System.out.println("Positions: [" + indexStart + ", " + indexEnd + "] Patter: " + patternEventsResPrev.toString());
            
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValuesDes(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1;
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
    
    static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValuesAsc(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1;
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
    
    class Event{
        int name;
        int numPoints;
        double duration;
        
        Event(int name, int points, double dur) {
            this.name = name;
            this.numPoints = points;
            this.duration = dur;
        }
    }
    
    class Pattern {
        HashMap<Integer, Event> pattern;
        double weight;
        
        Pattern() {
            pattern = new HashMap<Integer, GroupData.Event>();
        }
        
        Pattern(Event event) {
            pattern = new HashMap<Integer, GroupData.Event>();
            pattern.put(event.name, event);
            weight = event.duration;
        }
        
        Pattern(Pattern origPattern) {
            pattern = new HashMap<Integer, GroupData.Event>(origPattern.pattern);
            weight = origPattern.weight;
        }
        
        double weightFunction() {
            if (pattern != null) {
                double curWeight = 0;
                int minNumPoints = 99999999;
                for (Integer eventName: pattern.keySet()) {
                    int curNumPoints = pattern.get(eventName).numPoints;
                    curWeight += pattern.get(eventName).duration;///curNumPoints;
                    minNumPoints = Math.min(minNumPoints, curNumPoints);
                }
               // curWeight *= minNumPoints;
                weight = curWeight;
                
                if (pattern.size() > 1)
                    weight = curWeight/(pattern.size() - 1);
            }
            return weight;
        }
        
        double addEvent(Event event) {
            if (pattern != null) {
                pattern.put(event.name, event);
                return weightFunction();
            }
            return -1;
        }
    }
    
    private ArrayList<Pattern> exploreEvents(ArrayList<Pattern> inputPatterns, final HashMap<Integer, Event> eventsMap) {
        if (inputPatterns != null) {
            ArrayList<Pattern> allPatterns = new ArrayList<GroupData.Pattern>();
            // Add all possible patterns found at current depth level
            for (int i = 0; i < inputPatterns.size(); i++) {
                Pattern curPattern = inputPatterns.get(i);
                
                if (curPattern.pattern.size() >= GROUP_DEPTH_SIZE)
                    continue;
                
                ArrayList<Pattern> curPatterns = new ArrayList<GroupData.Pattern>();
                
                // Extend current patterns with all possible patterns and add to array
                // TODO: think about adding only one or few patterns with max weight
                for (Integer eventName: eventsMap.keySet()) {
                    if(!curPattern.pattern.containsKey(eventName)) {
                        Pattern tempPattern = new Pattern(curPattern);
                        tempPattern.addEvent(eventsMap.get(eventName));
                        curPatterns.add(tempPattern);
                    }
                }
                
                allPatterns.addAll(curPatterns);
            }
            
            if (allPatterns.size() != 0) 
                allPatterns.addAll(exploreEvents(allPatterns, eventsMap));
            
            return allPatterns;
        }
        
        return null;
    }
    
    private boolean applyGroup(String name, SignalEvents groupedEvents, Set<Integer> groupMacro, int indexStart, int indexEnd) {
        SignalEvents baseEvents = signalEventsArray.get(name);
        
        // Check input
        if (indexEnd < indexStart || indexStart < 0 || indexEnd >= baseEvents.eventsArray.size()) {
            System.out.println("Error applying groups: incorrect boundaries " + indexStart + " - " + indexEnd);
            return false;
        }
        
        HashMap<Integer, Integer> groupStartIndexMap = new HashMap<Integer, Integer>();
        // Determine order of events in macro group 
        for (int i = indexStart; i <= indexEnd; i++) {
            Integer curEvent =  baseEvents.eventsArray.get(i).intValue();
            
            if (groupMacro.contains(curEvent) && !groupStartIndexMap.containsKey(curEvent)) {
                groupStartIndexMap.put(curEvent, i);
            }
        }
        
        SortedSet<Entry<Integer, Integer>> sortedMacroSet = entriesSortedByValuesAsc(groupStartIndexMap);
        
        if (sortedMacroSet.size() != groupMacro.size())
            return false;
        
        ArrayList<Integer> sortedMacroArray = new ArrayList<Integer>();
        
        Iterator<Entry<Integer, Integer>> it = sortedMacroSet.iterator();
        
        while (it.hasNext()) {
            Entry<Integer, Integer> entry = it.next();
            sortedMacroArray.add(entry.getKey());
        }
        
        System.out.println("Positions: [" + indexStart + ", " + (indexEnd) + "] Pattern: " + groupMacro.toString() + " Pattern_sorted: " 
                + sortedMacroSet.toString());
        
        int curEventIndex = 0;
        int maxEventIndex = sortedMacroArray.size();
        
        for (int i = indexStart; i <= indexEnd; i++) {
            groupedEvents.timeStartArray.set(i, baseEvents.timeStartArray.get(i));
            groupedEvents.timeEndArray.set(i, baseEvents.timeEndArray.get(i));
            
            int curEventBase = baseEvents.eventsArray.get(i).intValue();
            int nextEventIndex = (curEventIndex + 1) % maxEventIndex;
           
            if (curEventBase == sortedMacroArray.get(nextEventIndex)) {
                groupedEvents.eventsArray.set(i, sortedMacroArray.get(nextEventIndex).doubleValue());
                curEventIndex = nextEventIndex;
            } else {
                groupedEvents.eventsArray.set(i, sortedMacroArray.get(curEventIndex).doubleValue());
            }
        }
        
        return true;
    }
    
    private void groupEventsV2(String name) {
        try { 
            final int numGroups = signalEventsInfoArray.get(name).size();
            final int totalEvents = signalEventsArray.get(name).eventsArray.size();
            final int windowSize = (int) ((int) totalEvents*GROUP_WINDOW_SIZE);
            
            
            ArrayList<Pattern> tracePatterns = new ArrayList<GroupData.Pattern>();
            for (int i = 0; i < totalEvents - windowSize; i++) {
                int upB = Math.min(totalEvents, i + windowSize);
                
                HashMap<Integer, Event> eventsMap = new HashMap<Integer, Event>(numGroups);
                // Calculate total duration of all events in window
                for (int j = i; j < upB; j++) {
                     double eventDuration = signalEventsArray.get(name).timeEndArray.get(j) - signalEventsArray.get(name).timeStartArray.get(j);
                     int eventName = signalEventsArray.get(name).eventsArray.get(j).intValue();
                     
                     if (eventsMap.containsKey(eventName)) {
                         double curDuration = eventsMap.get(eventName).duration + eventDuration;
                         int curNum = eventsMap.get(eventName).numPoints + 1;
                         Event tempEvent = new Event(eventName, curNum, curDuration);
                         
                         eventsMap.put(eventName, tempEvent);
                     } else {
                         Event tempEvent = new Event(eventName, 1, eventDuration);
                         eventsMap.put(eventName, tempEvent);
                     }
                }
                
                //SortedSet<Map.Entry<Integer, Double>> sortedEvents = entriesSortedByValuesGroup(eventsDurationMap);
                //System.out.println(sortedEvents.toString() + "\n");
                
                ArrayList<Pattern> stepPatterns = new ArrayList<GroupData.Pattern>();
                for (Integer j : eventsMap.keySet()) {
                    Pattern singlePattern = new Pattern(eventsMap.get(j));
                    stepPatterns.add(singlePattern);
                }
                
                stepPatterns.addAll(exploreEvents(stepPatterns, eventsMap));
                
                int maxId = 0;
                double maxWeight = -999999;
                for (int j = 0; j < stepPatterns.size(); j++) {
                    Pattern curPattern = stepPatterns.get(j);
                    
                    if (maxWeight < curPattern.weight) {
                        maxWeight = curPattern.weight;
                        maxId = j;
                    }
                    //System.out.println("Step: " + i + " Pattern: " + curPattern.pattern.keySet() + " Weight: " + curPattern.weight);
                }
                //System.out.println("\n");
                tracePatterns.add(stepPatterns.get(maxId));
                System.out.println("Step: " + i + " Pattern: " + stepPatterns.get(maxId).pattern.keySet() + 
                        " Weight: " + stepPatterns.get(maxId).weight);
            }
            
            System.out.println("\n");
            
            SignalEvents groupedEvents = new SignalEvents(totalEvents);
            
            int indexStart = 0, indexEnd = 0;
            Pattern prevPattern = tracePatterns.get(0);
            Pattern prevValidPattern = tracePatterns.get(0);
            Pattern curPattern;
            for (int i = 1; i < tracePatterns.size(); i++) {
                curPattern = tracePatterns.get(i);
                if (!curPattern.pattern.keySet().equals(prevPattern.pattern.keySet())) {
                    if (applyGroup(name, groupedEvents, prevPattern.pattern.keySet(), indexStart, indexEnd/*+windowSize/2*/)) {
                        indexStart = i;// + windowSize/2;
                        prevValidPattern = new Pattern(prevPattern);
                    } else {
                        applyGroup(name, groupedEvents, prevValidPattern.pattern.keySet(), indexStart, indexEnd/*+windowSize/2*/);
                    }
                    prevPattern = new Pattern(curPattern);
                }
                indexEnd = i;
            }
            
            //System.out.println("Positions: [" + indexStart + ", " + (indexEnd + windowSize - 1) + "] Pattern: " + prevPattern.pattern.keySet().toString());
            if (!applyGroup(name, groupedEvents, prevPattern.pattern.keySet(), indexStart, indexEnd + windowSize))
                applyGroup(name, groupedEvents, prevValidPattern.pattern.keySet(), indexStart, indexEnd + windowSize);
            
            System.out.println("\n");
            System.out.println("Printing grouped event trace\n");
            
            for (int i = 0; i < groupedEvents.eventsArray.size(); i++) {
                double origEvent = signalEventsArray.get(name).eventsArray.get(i);
                double origEventStart = signalEventsArray.get(name).timeStartArray.get(i);
                double origEventEnd = signalEventsArray.get(name).timeEndArray.get(i);
                
                double groupEvent = groupedEvents.eventsArray.get(i);
                double groupEventStart = groupedEvents.timeStartArray.get(i);
                double groupEventEnd = groupedEvents.timeEndArray.get(i);
                
                
                System.out.println("Event(" + i + ")Original: "  + origEvent + "[" + origEventStart + ", " + origEventEnd 
                        + "]; Grouped: " + groupEvent + "[" + groupEventStart + ", " + groupEventEnd + "]");
            }
            
            signalEventsArray.put(name+MACRO_GROUP_SUF, groupedEvents);
            
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    private void groupedEventsToData(String name) {
        try {
            SignalData origSimData = signalDataArray.get(name);
            SignalData groupedSimData = new SignalData();
            
            SignalEvents groupedEvents = signalEventsArray.get(name+MACRO_GROUP_SUF);
            ArrayList<DerivCalc.Event> eventsInfo = signalEventsInfoArray.get(name);
            
            double curValue = origSimData.dataArray.get(0);
            int curGroup = groupedEvents.eventsArray.get(0).intValue();
            double curTime = origSimData.timeArray.get(0);
            double prevTime = curTime;
            
            int indexEvents = 0;
            
            groupedSimData.dataArray.add(curValue);
            groupedSimData.timeArray.add(curTime);
            groupedSimData.dataArrayGroup.add((double) curGroup);
            
            for (int i = 1; i < origSimData.timeArray.size(); i++) {
                curTime = origSimData.timeArray.get(i);
                double curRate = 0;
                
                for (int j = indexEvents; j < groupedEvents.eventsArray.size(); j++) {
                    double timeLow = groupedEvents.timeStartArray.get(j);
                    double timeHigh = groupedEvents.timeEndArray.get(j);
                    
                    if (curTime >= timeLow && curTime <= timeHigh) {
                        indexEvents = j;
                        curGroup = groupedEvents.eventsArray.get(j).intValue();
                        curRate = getRndRate(eventsInfo, curGroup);
                        break;
                    }
                }
                
                curValue = curValue + curRate*(curTime-prevTime);
                
                //to check grouping only
                //curValue = origSimData.dataArray.get(i);
                
                groupedSimData.dataArray.add(curValue);
                groupedSimData.timeArray.add(curTime);
                groupedSimData.dataArrayGroup.add((double) curGroup);
                prevTime = curTime;
            }
            
            signalDataArray.put(name+SIMTRACE_GROUPED_SUF, groupedSimData);
            
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    public ArrayList<Double> getSignalData(String name) {
        if (signalDataArray.containsKey(name))
            return signalDataArray.get(name).dataArray;
        else
            return null;
    }
    
    public ArrayList<Double> getSignalTime(String name) {
        if (signalDataArray.containsKey(name))
            return signalDataArray.get(name).timeArray;
        else
            return null;
    }
    
    public ArrayList<Double> getSignalGroup(String name) {
        if (signalDataArray.containsKey(name))
            return signalDataArray.get(name).dataArrayGroup;
        else
            return null;
    }
    
    private void getEventScore(String name) {
        // Calculate score for macroevent
    }
    
    public GroupData() {
        signalDataArray = new HashMap<String, GroupData.SignalData>();
        signalEventsArray = new HashMap<String, GroupData.SignalEvents>();
        signalEventsInfoArray = new HashMap<String, ArrayList<DerivCalc.Event>>();
    }
}

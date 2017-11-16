package modelFSM;


import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import modelFSM.data.ControlType;
import modelFSM.data.Event;
import modelFSM.data.Predicate;
import modelFSM.data.OutputDataChunk;
import modelFSM.data.RawDataChunk;
import modelFSM.data.RawDataPoint;
import modelFSM.rules.RuleManager;
import modelFSM.shared.Util;

public class AnalyzeData {

    public final static boolean DISGARD_INIT_STATE = true;
    public final static String COUNTER_PREFIX = "PRED_CNT_";
    
    @SuppressWarnings("serial")
    class OutputData extends ArrayList<OutputDataChunk> {
        public OutputData() {
            super();
        }
    }
    
    @SuppressWarnings("serial")
    class InputDataDiscrete extends ArrayList<Event> {
        public InputDataDiscrete(ArrayList<Event> events) {
            super();
            this.addAll(events);
        }
    }
    
    HashMap<String, RuleManager> dataManagers;
    HashMap<String, OutputData> outputSignals;
    HashMap<String, InputDataDiscrete> inputSignalsDiscrete;
    HashMap<String, RawDataChunk> inputSignalsAnalog;
    
    public AnalyzeData() {
        inputSignalsDiscrete = new HashMap<String, AnalyzeData.InputDataDiscrete>();
        inputSignalsAnalog = new HashMap<String, RawDataChunk>();
        outputSignals = new HashMap<String, AnalyzeData.OutputData>();
        dataManagers = new HashMap<String, RuleManager>();
    }
    
    
    public boolean analyzeDataRules(String name) {
        try {
            if (!dataManagers.containsKey(name) || dataManagers.size() < 1) {
                System.out.println("ControlData Error: can not analyze data rules for empty output signal: " + name);
            }
            
            RuleManager ruleManager = dataManagers.get(name); 
            ruleManager.analyzeData();
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public void initDataRules(String name) {
        try {
            if (!outputSignals.containsKey(name) || outputSignals.size() < 1) {
                System.out.println("ControlData Error: can not calculate next state functions for empty output signal: " + name);
            }
            
            System.out.println("\nCalculating next state functions for signal: " + name);
            
            calculateDataChunks(name);
            RuleManager ruleManager = new RuleManager(outputSignals.get(name));
            
            dataManagers.put(name, ruleManager);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        
    }
    
    private void addEventPredicates(List<OutputDataChunk> outputData, InputDataDiscrete inputData) {
        try {
            Event prevEvent = null;
            
            double startTime = 0;
            double endTime = 0;
            
            int curEventId = 0;
            
            for (OutputDataChunk outputChunk: outputData) {
                Event outEvent = outputChunk.outputEvent;
                endTime = outEvent.start;
                
                if (prevEvent == null)
                    prevEvent = outEvent;
                
                Predicate eventPredicate = outputChunk.outputPredicate;
                
                for (int i = curEventId; i < inputData.size(); i++) {
                    Event curEvent = inputData.get(i);
                    
                    // Input event is out of the window - end cycle - stop the suffering
                    if (curEvent.start > endTime) {
                        if (i > 0)
                            curEventId = i - 1;
                        else
                            curEventId = 0;
                        break;
                    }
                    
                    // Find overlap in the current output event and input events time
                    if (curEvent.start > startTime && curEvent.start <= endTime
                            || curEvent.end > startTime) {
                        eventPredicate.add(curEvent);
                        //TODO: remove
                        System.out.println("OutEvent: " + outEvent.signalName + "<" + outEvent.eventId + ">" 
                                + " InputEvent: " + curEvent.signalName + "<" + curEvent.eventId + ">" 
                                + " Event.start: " + curEvent.start + " Event.end: "
                                + curEvent.end + " Start: " + startTime + " End: " + endTime);
                    }
                }
                
                outputChunk.outputPredicate = eventPredicate;
                prevEvent = outEvent;
                startTime = outEvent.start;
            } 
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    private void orderEventPredicates(List<OutputDataChunk> outputData) {
        try {
            Event prevEvent = null;
            
            for (OutputDataChunk outputChunk: outputData) {
                Event outEvent = outputChunk.outputEvent;
                if (prevEvent == null)
                    prevEvent = outEvent;
                
                Predicate eventPredicate = outputChunk.outputPredicate;
                
                Collections.sort(eventPredicate, new Comparator<Event>() {
                    @Override
                    public int compare(Event e0, Event e1) {
                        return Double.compare(e0.start, e1.start);
                    }
                });
                
                // Prepend previous output state to the predicate
                eventPredicate.add(0, prevEvent);
                outputChunk.outputPredicate = eventPredicate;
                prevEvent = outEvent;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    private void addRawData(List<OutputDataChunk> outputData, HashMap<String, RawDataChunk> inputData) {
        try {
            for (String inputName: inputSignalsAnalog.keySet()) {
                RawDataChunk inputDataFull = inputSignalsAnalog.get(inputName);
                //TODO: remove all debug messages
                System.out.println("Input signal: " + inputName);
                
                int dataPointId = 0;
                double startTime = 0;
                double endTime = 0;
                for (int i = 0; i < outputData.size(); i++) {
                    OutputDataChunk outputChunk = outputData.get(i);
                    RawDataChunk inputDataChunk = new RawDataChunk();
                    if (i == 0) {
                        startTime = 0;
                        endTime = outputChunk.outputEvent.end;
                    } else {
                        startTime = outputData.get(i - 1).outputEvent.start;
                        endTime = outputChunk.outputEvent.end;
                    }
                    
                    
                    System.out.println("Chunk id: " + outputChunk.outputEvent.eventId 
                            + " start: " + startTime + " end: " + endTime + "\n");
                    
                    for (int j = dataPointId++; j < inputDataFull.size(); j++) {
                        RawDataPoint dataPoint = inputDataFull.get(j);
                        
                        if (dataPoint.time <= endTime) {
                            if (dataPoint.time >= startTime) {
                                inputDataChunk.add(dataPoint);
                                System.out.println("Data point value: " + dataPoint.value +
                                        " time: " + dataPoint.time);
                            }
                        } else
                            break;
                    }
                    System.out.println();
                    outputChunk.inputRawData.put(inputName, inputDataChunk);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    private void calculateDataChunks(String name) {
        try {
            if (!outputSignals.containsKey(name) || outputSignals.size() < 1) {
                System.out.println("ControlData Error: can not calculate predicates for empty output signal: " + name);
            }
            
            System.out.println("\nCalculating predicates for signal: " + name);
            List<OutputDataChunk> chunksList = outputSignals.get(name);
            
            for (String inputName: inputSignalsDiscrete.keySet()) {
                addEventPredicates(chunksList, inputSignalsDiscrete.get(inputName));
            }
            
            orderEventPredicates(chunksList);
            
            System.out.println("\nAssigning analog data to signal: " + name);
            addRawData(chunksList, inputSignalsAnalog);
            
            //TODO: Test what has been added
            System.out.println("\nPrinting predicates for signal: " + name + "\n");
            for (int i = 0; i < chunksList.size(); i++) {
                Event curEvent = chunksList.get(i).outputEvent;
                Predicate eventPredicate = chunksList.get(i).outputPredicate;
                System.out.println(curEvent.signalName + "<" + curEvent.eventId + ">");
                
                for (Event predEvent: eventPredicate) {
                    System.out.print(" " + predEvent.signalName + "<" + predEvent.eventId + ">");
                }
                
                System.out.println();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    // TODO: data grouping into RawDataChunk should be done outside this class
    // TODO: think if class should be inititalized only in constructor once
    // Add already discretized signal
    public void addSignal(ControlType type, String name, ArrayList<Double> timeArray, ArrayList<Double> dataArray, ArrayList<Integer> dataArrayGroup) {
        System.out.println("\nControl data adding signal " + name);
        if (type == ControlType.INPUT) {
            //Indicates that input is DMV
            if (dataArrayGroup != null) {
                InputDataDiscrete signal = new InputDataDiscrete(Util.dataToEvents(name, timeArray, dataArrayGroup));
                inputSignalsDiscrete.put(name, signal);
            } else {
                RawDataChunk rawData = new RawDataChunk();
                for (int i = 0; i < timeArray.size(); i++) {
                    RawDataPoint dataPoint = new RawDataPoint(dataArray.get(i), timeArray.get(i));
                    rawData.add(dataPoint);
                }
                inputSignalsAnalog.put(name, rawData);
            }
        }
        
        if (type == ControlType.OUTPUT) {
            if (dataArrayGroup == null) {
                System.out.println("ControlData Error: output data must be descretized");
                return;
            }
            
            OutputData signal = new OutputData();
            List<Event> events = Util.dataToEvents(name, timeArray, dataArrayGroup);
            int predNum = 0;
            for (Event event: events) {
                OutputDataChunk dataChunk = new OutputDataChunk(event, predNum++);
                dataChunk.outputEvent = event;
                signal.add(dataChunk);
            }
            
            outputSignals.put(name, signal);
        }
    }
    
    // Add undiscretized(analog) signal
    // TODO: think on how to dynamically discretize analog signals based on values
    public void addSignal(ControlType type, String name, ArrayList<Double> timeArray, ArrayList<Double> dataArray) {
        addSignal(type, name, timeArray, dataArray, null);
    }
    
}

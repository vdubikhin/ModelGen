package modelgen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import java.util.Set;

import modelgen.data.ControlType;
import modelgen.data.pattern.DataPattern;
import modelgen.data.pattern.DataPatterns;
import modelgen.data.pattern.PatternVector;
import modelgen.data.pattern.StateVector;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.stage.StageDataRaw;
import modelgen.data.stage.StageDataState;
import modelgen.data.state.IState;
import modelgen.flow.DiscretizeData;
import modelgen.flow.FilterData;
import modelgen.flow.IStage;
import modelgen.flow.RuleMiningFSM;
import modelgen.shared.Util;

public class ModelFSM {
    private Map<String, RawDataChunk> rawData;
    private Map<String, ControlType> signalType;

    private IStage<StageDataRaw, StageDataState> discretizeData;
    private IStage<StageDataState, StageDataState> filterData;
    
    public ModelFSM(String fileName) {
        rawData = Util.parseCSVFile(fileName);
        
        signalType = new HashMap<>();
        
        discretizeData = new DiscretizeData();
        filterData = new FilterData();
    }
    
    public Set<String> getSignalNames() {
        return rawData.keySet();
     }
    
    public boolean setSignalType(String signal, ControlType type) {
        if (rawData.containsKey(signal) || type == null) {
            signalType.put(signal, type);
            System.out.println("Setting signal: " + signal + " to type: " + type);
            return true;
        }
        
        System.out.println("Failed to set signal: " + signal + " to type: " + type);
        return false;
    }
    
    public boolean processData() {
        List<StageDataRaw> dataIn = new ArrayList<StageDataRaw>();
        for (String signalName: signalType.keySet()) {
            StageDataRaw packedData = new StageDataRaw(rawData.get(signalName), signalName, signalType.get(signalName));
            dataIn.add(packedData);
        }

        List<Entry<StageDataState, Integer>> dataOut = discretizeData.processData(dataIn);
        List<StageDataState> dataIn2 = new ArrayList<>();
        for (Entry<StageDataState, Integer> entry: dataOut) {
            if (entry.getKey().getStates() != null) {
                System.out.println("Signal: " + entry.getKey().getName() + " Cost: " + entry.getValue() + " Size: " +
                        entry.getKey().getStates().size());
                
                for (IState state: entry.getKey().getStates()) {
                    Map.Entry<Double, Double> mergeStateStamp = state.getTimeStamp();
                    Double start = mergeStateStamp.getKey();
                    Double end = mergeStateStamp.getValue();
    
                    System.out.println("State: " + state.getSignalName() + " Id: " + state.getId() + " Start: " + start
                                       + " End: " + end + " Duration: " + state.getDuration() + " Value: " + state.convertToString());
                }
            } else
                System.out.println("Signal: " + entry.getKey().getName() + " Cost: " + entry.getValue() + " Size: 0");

            dataIn2.add(entry.getKey());
        }
        
//        //TODO: FILTERING
//        System.out.println("--------------");
//        dataOut = filterData.processData(dataIn2);
//        
//        for (Entry<StageDataStates, Integer> entry: dataOut) {
//            System.out.println("Signal: " + entry.getKey().getName() + " Cost: " + entry.getValue() + " Size: " +
//                    entry.getKey().getStates().size());
//            for (IState state: entry.getKey().getStates()) {
//                Map.Entry<Double, Double> mergeStateStamp = state.getTimeStamp();
//                Double start = mergeStateStamp.getKey();
//                Double end = mergeStateStamp.getValue();
//
//                System.out.println("State: " + state.getSignalName() + " Id: " + state.getId() + " Start: " + start
//                                   + " End: " + end + " Duration: " + state.getDuration() + " Value: " + state.convertToString());
//            }
//        }

        System.out.println("--------------");
        RuleMiningFSM ruleMiningStage = new RuleMiningFSM();
        ruleMiningStage.processData(dataIn2);
        
//        SignalDataPatterns dataIn3 = new StatesToPatternConverter().convertStatesToPatterns(dataIn2);
//        printVectors(dataIn3.getSignalPatterns(), false);
        
//        ConflictDetector dataRules = new ConflictDetector(dataIn3);
        
        Map<String, List<IState>> discreteStates = new HashMap<String, List<IState>>();
        for (Entry<StageDataState, Integer> entry: dataOut) {
            StageDataState data = entry.getKey();
            discreteStates.put(data.getName(), data.getStates());
        }
        
        Map<String, RawDataChunk> generatedData = generateWaveform(rawData, discreteStates);
        dumpWaveform("GeneratedData", generatedData);
        
        return false;
    }
    
    private void printVectors(Map<String, DataPatterns> dataPatterns, boolean printRuleVectors) {
        try {
            for (String signal: dataPatterns.keySet()) {
                
                for (DataPatterns signalPatterns: dataPatterns.values()) {
                    for (DataPattern curPattern: signalPatterns) {
                        PatternVector pattern;
                        if (printRuleVectors)
                            pattern = curPattern.getRuleVector();
                        else
                            pattern = curPattern.getFullRuleVector();
    
                        System.out.println(signal + "<" + curPattern.getOutputState().getId()  + ">");
                        // Get set of printable names first
                        HashSet<String> stateNames = new HashSet<String>();
                        for (StateVector stateVector: pattern.values()) {
                            stateNames.addAll(stateVector.keySet());
                        }
                        
                        int stateNum = 0;
                        String prefixString = "";
                        int leadingSpaceNum = 5;
                        for (String stateName: stateNames) {
                            // Reverse order so we print final state last
                            List<Integer> patternVectorKeysReverse = new ArrayList<Integer>(pattern.keySet());
                            Collections.sort(patternVectorKeysReverse);
                            
                            String printLine;
                            int patternVectorId = pattern.getId();
        
                            prefixString = "";
                            
                            if (stateNum == (int)(stateNames.size()/2)) {
                                prefixString = patternVectorId + ": ";
                                for (int i = prefixString.length(); i < leadingSpaceNum; i++)
                                    prefixString = " " + prefixString;
                                
                                printLine = prefixString + stateName + " ";
                            } else {
                                
                                for (int i = 0; i < leadingSpaceNum; i++)
                                    prefixString += " ";
                                
                                printLine = prefixString + stateName + " ";
                            }
                            
                            for (Integer key: patternVectorKeysReverse) {
                                StateVector stateVector = pattern.get(key);
                                if (stateVector.containsKey(stateName))
                                    printLine += stateVector.get(stateName).getId() + "("+ 
                                            stateVector.get(stateName).getTimeStamp().getKey() + ") ";
                            }
                            System.out.println(printLine);
                            stateNum++;
                        }
                        prefixString = "";
                        for (int i = prefixString.length(); i < leadingSpaceNum; i++)
                            prefixString = " " + prefixString;
                        
                        if (stateNames.isEmpty())
                            System.out.println(pattern.getId() + ": " + pattern.isUnique());
                        else 
                            System.out.println(prefixString  + "unique: " + pattern.isUnique());
                                    
                        System.out.println();
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    
    public Map<String, RawDataChunk> generateWaveform(Map<String, RawDataChunk> originalData, Map<String,
            List<IState>> discreteStates) {
        Map<String, RawDataChunk> result = new HashMap<String, RawDataChunk>();

        for (String name: signalType.keySet()) {
            RawDataChunk curChunk = originalData.get(name);
            RawDataChunk generatedChunk = new RawDataChunk();
            List<IState> states = discreteStates.get(name);
            if (states != null) {
                for (IState state: states) {
                    generatedChunk.addAll(state.generateSignal(curChunk));
                }
            } else {
                generatedChunk = curChunk;
            }
            
            generatedChunk.sort((p1, p2) -> Double.compare(p1.getTime(), p2.getTime()));
            result.put(name, generatedChunk);
        }
        
        return result;
    }
    
    public void dumpWaveform(String fileName, Map<String, RawDataChunk> originalData) {
        FileWriter file;
        try {
            file = new FileWriter(fileName + ".csv");
            String curLine = "Time";
            List<String> signalNames = new ArrayList<>(originalData.keySet());
            
            for (String name: signalNames) {
                curLine += ",Generated" + name;
            }

            curLine += "\n";
            file.write(curLine);

            int maxSize = originalData.get(signalNames.get(0)).size();
            for(int i = 0; i < maxSize; i++)
            {
                boolean exit = false;
                List<RawDataPoint> dataPoints = new ArrayList<RawDataPoint>();
                for (String name: signalNames) {
                    RawDataChunk curChunk = originalData.get(name);
                    if (i >= curChunk.size()) {
                        exit = true;
                        break;
                    }
                        
                    dataPoints.add(curChunk.get(i));
                }
                
                if (exit)
                    break;
                
                curLine = dataPoints.get(0).getTime().toString();
                for (RawDataPoint curPoint: dataPoints) {
                    curLine += "," + curPoint.getValue().toString();
                }
                curLine += "\n";
                file.write(curLine);
            }

            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}

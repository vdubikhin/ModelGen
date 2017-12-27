package modelgen;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import modelgen.data.ControlType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.state.IState;
import modelgen.flow.DiscretizeData;
import modelgen.flow.FilterData;
import modelgen.flow.IStage;
import modelgen.processor.discretization.DataInput;
import modelgen.processor.discretization.DataOutput;
import modelgen.shared.Util;

public class ModelFSM {
    private Map<String, RawDataChunk> rawData;
    private Map<String, ControlType> signalType;

    private IStage<DataInput, DataOutput> discretizeData;
    private IStage<DataOutput, DataOutput> filterData;
    
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
        List<DataInput> dataIn = new ArrayList<DataInput>();
        for (String signalName: signalType.keySet()) {
            DataInput packedData = new DataInput(rawData.get(signalName), signalName, signalType.get(signalName));
            dataIn.add(packedData);
        }

        List<Entry<DataOutput, Integer>> dataOut = discretizeData.processData(dataIn);
        List<DataOutput> dataIn2 = new ArrayList<>();
        for (Entry<DataOutput, Integer> entry: dataOut) {
            System.out.println("Signal: " + entry.getKey().getName() + " Cost: " + entry.getValue() + " Size: " +
                    entry.getKey().getStates().size());
            dataIn2.add(entry.getKey());
            for (IState state: entry.getKey().getStates()) {
                Map.Entry<Double, Double> mergeStateStamp = state.getTimeStamp();
                Double start = mergeStateStamp.getKey();
                Double end = mergeStateStamp.getValue();

                System.out.println("State: " + state.getSignalName() + " Id: " + state.getId() + " Start: " + start
                                   + " End: " + end + " Duration: " + state.getDuration() + " Value: " + state.convertToString());
            }
        }
        
        System.out.println("--------------");
        dataOut = filterData.processData(dataIn2);
        
        for (Entry<DataOutput, Integer> entry: dataOut) {
            System.out.println("Signal: " + entry.getKey().getName() + " Cost: " + entry.getValue() + " Size: " +
                    entry.getKey().getStates().size());
            for (IState state: entry.getKey().getStates()) {
                Map.Entry<Double, Double> mergeStateStamp = state.getTimeStamp();
                Double start = mergeStateStamp.getKey();
                Double end = mergeStateStamp.getValue();

                System.out.println("State: " + state.getSignalName() + " Id: " + state.getId() + " Start: " + start
                                   + " End: " + end + " Duration: " + state.getDuration() + " Value: " + state.convertToString());
            }
        }
        
        Map<String, List<IState>> discreteStates = new HashMap<String, List<IState>>();
        for (Entry<DataOutput, Integer> entry: dataOut) {
            DataOutput data = entry.getKey();
            discreteStates.put(data.getName(), data.getStates());
        }
        
        Map<String, RawDataChunk> generatedData = generateWaveform(rawData, discreteStates);
        dumpWaveform("GeneratedData", generatedData);
        
        return false;
    }
    
    public Map<String, RawDataChunk> generateWaveform(Map<String, RawDataChunk> originalData, Map<String,
            List<IState>> discreteStates) {
        Map<String, RawDataChunk> result = new HashMap<String, RawDataChunk>();

        for (String name: originalData.keySet()) {
            RawDataChunk curChunk = originalData.get(name);
            RawDataChunk generatedChunk = new RawDataChunk();
            List<IState> states = discreteStates.get(name);
            for (IState state: states) {
                generatedChunk.addAll(state.generateSignal(curChunk));
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

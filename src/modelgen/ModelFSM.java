package modelgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import modelgen.data.ControlType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.state.IState;
import modelgen.flow.DiscretizeData;
import modelgen.flow.IStage;
import modelgen.processor.discretization.DataInput;
import modelgen.processor.discretization.DataOutput;
import modelgen.shared.Util;

public class ModelFSM {
    private Map<String, RawDataChunk> rawData;
    private Map<String, ControlType> signalType;

    private IStage<DataInput, DataOutput> discretizeData;
    
    public ModelFSM(String fileName) {
        rawData = Util.parseCSVFile(fileName);
        
        signalType = new HashMap<>();
        
        discretizeData = new DiscretizeData();
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

        for (Entry<DataOutput, Integer> entry: dataOut) {
            System.out.println("Signal: " + entry.getKey().getName() + " Cost: " + entry.getValue());
            for (IState state: entry.getKey().getStates()) {
                Map.Entry<Double, Double> mergeStateStamp = state.getTimeStamp();
                Double start = mergeStateStamp.getKey();
                Double end = mergeStateStamp.getValue();

                System.out.println("State: " + state.getSignalName() + " Id: " + state.getId() + " Start: " + start
                                   + " End: " + end + " Duration: " + state.getDuration() + " Value: " + state.convertToString());
            }
        }
        
        return false;
    }
}

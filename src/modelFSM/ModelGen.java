package modelFSM;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelFSM.data.ControlType;
import modelFSM.data.RawDataChunk;
import modelFSM.data.RawDataChunkGrouped;
import modelFSM.data.RawDataPoint;
import modelFSM.data.RawDataPointGrouped;
import modelFSM.data.event.EventInfo;
import modelFSM.discretization.DataDiscretizerCreator;
import modelFSM.shared.Util;

public class ModelGen {

    private HashMap<String, RawDataChunkGrouped> rawData;
    private HashMap<String, ControlType> signalType;
    private HashMap<String, List<EventInfo>> eventInfo;
    
    private String[] dataHandlers;
    
    public ModelGen(String fileName) {
        HashMap<String, RawDataChunk> data = Util.parseCSVFile(fileName);
        
        rawData = new HashMap<>();
        signalType = new HashMap<>();
        eventInfo = new HashMap<>();
        
        //convert raw data to grouped type
        for (String signal: data.keySet()) {
            RawDataChunkGrouped dataChunkGrouped = new RawDataChunkGrouped();
            for (RawDataPoint rawPoint: data.get(signal)) {
                RawDataPointGrouped pointGrouped = new RawDataPointGrouped(rawPoint, -1);
                dataChunkGrouped.add(pointGrouped);
            }
            rawData.put(signal, dataChunkGrouped);
        }
    }
    
    public Set<String> getSignalNames() {
        return rawData.keySet();
     }
    
    public boolean setSignalType(String signal, ControlType type) {
        if (rawData.containsKey(signal) || type == null) {
            signalType.put(signal, type);
            System.out.println("Setting signal: " + signal + "to type: " + type);
            return true;
        }
        
        System.out.println("Failed to set signal: " + signal + "to type: " + type);
        return false;
    }
    
    public boolean processData() {
        dataHandlers = DataDiscretizerCreator.getDiscretizerTypes();
        
        DiscretizeData discretizeData = new DiscretizeData(rawData, signalType);
        discretizeData.analyzeRawData(dataHandlers);
        
        return false;
    }
}

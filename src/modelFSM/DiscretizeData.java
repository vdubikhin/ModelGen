package modelFSM;

import java.util.HashMap;

import modelFSM.data.RawDataChunk;
import modelFSM.data.RawDataPoint;
import modelFSM.discretization.HandlerDMV;
import modelFSM.shared.Util;

public class DiscretizeData {
    
    private HashMap<String, RawDataChunk> rawData;
    private HandlerDMV handlerDMV;
    
    public DiscretizeData(String fileName) {
        rawData = Util.parseCSVFile(fileName);
        
       
        //TODO: debug test
        System.out.println("DiscretizeData test");
        for (String signal: rawData.keySet()) {
            System.out.println(signal);
            RawDataChunk curDataChunk = rawData.get(signal);
            for (RawDataPoint curDataPoint: curDataChunk) {
                System.out.println("Time: " + curDataPoint.time + " Data: " + curDataPoint.value);
            }
        }
    }
    
    //DetectDMV - same as in Scott's thesis
    //CalcDerivative - wrapper for one of calculation methods
    //GroupData -wrapper for one of methods: simple or HCA
}

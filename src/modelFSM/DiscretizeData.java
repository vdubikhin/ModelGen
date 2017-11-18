package modelFSM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modelFSM.data.ControlType;
import modelFSM.data.RawDataChunk;
import modelFSM.data.RawDataChunkGrouped;
import modelFSM.data.RawDataPoint;
import modelFSM.discretization.*;
import modelFSM.shared.Util;

public class DiscretizeData {
    
    private HashMap<String, RawDataChunkGrouped> rawData;
    private HashMap<String, ControlType> signalType;
    
    public DiscretizeData(HashMap<String, RawDataChunkGrouped> data, HashMap<String, ControlType> type) {
        rawData = data;
        signalType = type;
        //TODO: debug test
//        System.out.println("DiscretizeData test");
//        for (String signal: rawData.keySet()) {
//            System.out.println(signal);
//            RawDataChunk curDataChunk = rawData.get(signal);
//            for (RawDataPoint curDataPoint: curDataChunk) {
//                System.out.println("Time: " + curDataPoint.time + " Data: " + curDataPoint.value);
//            }
//        }
    }

    //TODO: public void analyzeRawData(String[] dataHandlers, HashMap<String, HashMap<String, Double>> handlerParams)
    public void analyzeRawData(String[] dataHandlers) {
        try {
            for (String signal: rawData.keySet()) {
                for (String handlerName: dataHandlers) {
                    DataDiscretizer dataHandler = DataDiscretizerCreator.createDataDiscretizer(handlerName, rawData.get(signal), signalType.get(signal));
                    if (dataHandler != null) {
                        //TODO: dataHandler.setParams(HashMap<String, Double> handlerParams);
                        System.out.println("Signal " + signal + " Handler: " + handlerName + " Can use: " + dataHandler.canDiscretizeData());
                    } else {
                        System.out.println("Signal " + signal + " Handler: " + handlerName + " is not found");
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

}

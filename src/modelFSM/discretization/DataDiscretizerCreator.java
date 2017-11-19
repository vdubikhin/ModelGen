package modelFSM.discretization;

import modelFSM.data.ControlType;
import modelFSM.data.RawDataChunkGrouped;

public class DataDiscretizerCreator {
    private static final String[] handlerNames = {"ValueType", "Stability"};
    
    private DataDiscretizerCreator() {}
    
    public static String[] getDiscretizerTypes() {
        return handlerNames;
    }
    
    public static DataDiscretizer createDataDiscretizer(String name, RawDataChunkGrouped groupedData, ControlType type) {
        if(name == null) {
            return null;
         }
        
        if(name.equals("Stability")) {
            return new DiscretizeDataByStability(groupedData);
         }
        
        if(name.equals("ValueType")) {
            return new DiscretizeDataByValues(groupedData);
         }
        
        return null;
    }
    
}

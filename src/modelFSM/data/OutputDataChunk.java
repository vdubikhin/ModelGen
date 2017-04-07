package modelFSM.data;

import java.util.HashMap;

public class OutputDataChunk {
    public Event outputEvent;
    public Predicate outputPredicate;
    public HashMap<String, RawDataChunk> inputRawData;
    
    public OutputDataChunk() {
    }

}

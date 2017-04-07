package modelFSM.data;

import java.util.HashMap;

public class OutputDataChunk {
    public Event outputEvent;
    public Predicate outputPredicate;
    public HashMap<String, RawDataChunk> inputRawData;
    
    public OutputDataChunk(Event event, Integer id) {
        this.outputEvent = event;
        this.outputPredicate = new Predicate(id); 
        this.inputRawData = new HashMap<String, RawDataChunk>();
    }

}

package modelFSM.data;

public class RawDataPoint {
    public final Double value;
    public final Double time;
    
    public RawDataPoint(Double value, Double time) {
        this.value = value;
        this.time = time;
    }
    
    public RawDataPoint(RawDataPoint toCopy) {
        this.value = toCopy.value;
        this.time = toCopy.time;
    }
}

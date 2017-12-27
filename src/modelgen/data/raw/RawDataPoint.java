package modelgen.data.raw;

public class RawDataPoint {
    protected final Double value;
    protected final Double time;
    
    public RawDataPoint(Double value, Double time) {
        this.value = value;
        this.time = time;
    }
    
    public RawDataPoint(RawDataPoint toCopy) {
        this.value = new Double(toCopy.value);
        this.time = new Double(toCopy.time);
    }
    
    public Double getValue() {
        return value;
    }

    public Double getTime() {
        return time;
    }
}

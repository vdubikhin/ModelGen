package modelgen.data.raw;

public class RawDataPointGrouped extends RawDataPoint{
    int group;

    public RawDataPointGrouped(Double value, Double time, int groupId) {
        super(value, time);
        group = groupId;
    }
    
    public RawDataPointGrouped(RawDataPoint curDataPoint, int groupId) {
        super(curDataPoint);
        group = groupId;
    }
    
    public int getGroup() {return group;}
}
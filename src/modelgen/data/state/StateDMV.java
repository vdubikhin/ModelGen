package modelgen.data.state;

import java.text.DecimalFormat;

import modelgen.data.DataType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;

public class StateDMV extends State implements IState {
    private Double value;

    public StateDMV (String name, Integer id, Double start, Double end, Double value) {
        super(name, id, start, end);
        this.value = value;
    }
    
    public StateDMV (StateDMV toCopy) {
        this(toCopy.signalName, toCopy.stateId, toCopy.start, toCopy.end, toCopy.value);
    }
    
    @Override
    public String convertToString() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return "[    " + df.format(value) + "   ]";
    }

    @Override
    public DataType getType() {
        return DataType.DMV;
    }

    @Override
    public IState makeCopy() {
        return new StateDMV(this);
    }

    @Override
    public RawDataChunk generateSignal(RawDataChunk baseSignal) {
        RawDataChunk outputData = new RawDataChunk();
        
        for (RawDataPoint curPoint: baseSignal) {
            if (start <= curPoint.getTime() && curPoint.getTime() <= end) {
                RawDataPoint newPoint = new RawDataPoint(value, curPoint.getTime());
                outputData.add(newPoint);
            }
        }
        return outputData;
    }

    @Override
    public String convertToGuardCondition() {
        String output = "(" + signalName + "=" + convertToInt(value) + ")";
        return output;
    }

    @Override
    public String convertToAssignmentCondition() {
        String output = signalName + ":=" + convertToInt(value);
        return output;
    }

    @Override
    public String convertToInitialCondition() {
        String output = signalName + "=" + convertToInt(value);
        return output;
    }

}

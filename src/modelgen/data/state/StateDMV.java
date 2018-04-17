package modelgen.data.state;

import java.text.DecimalFormat;

import modelgen.data.DataType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;
import modelgen.data.raw.RawDataThreshold;

public class StateDMV extends State implements IState {
    RawDataThreshold thresholds;

    public StateDMV (String name, Double start, Double end, Double lb, Double ub) {
        super(name, Double.hashCode(lb + ub), start, end);
        thresholds = new RawDataThreshold(lb, ub);
    }

    public StateDMV (String name, Double start, Double end, Double value) {
        super(name, Double.hashCode(value), start, end);
        thresholds = new RawDataThreshold(value, value);
    }

    public StateDMV (StateDMV toCopy) {
        this(toCopy.signalName, toCopy.start, toCopy.end, toCopy.thresholds.getLowBound(),
                toCopy.thresholds.getUpperBound());
    }
    
    @Override
    public String convertToString() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return "[" + df.format(thresholds.getLowBound()) + ", " + df.format(thresholds.getUpperBound()) + "]";
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
                RawDataPoint newPoint = new RawDataPoint(thresholds.getCenter(), curPoint.getTime());
                outputData.add(newPoint);
            }
        }
        return outputData;
    }

    @Override
    public String convertToGuardCondition() {
        String output;
        if (thresholds.getUpperBound().equals(thresholds.getLowBound()))
            output = "(" + signalName + "=" + convertToInt(thresholds.getCenter()) + ")";
        else
            output = "~(" + signalName + ">=" + convertToInt(thresholds.getUpperBound()) + ")&(" + signalName + ">="
                + convertToInt(thresholds.getLowBound()) + ")";
        return output;
    }

    @Override
    public String convertToAssignmentCondition() {
        String output = signalName + ":=" + convertToInt(thresholds.getCenter());
        return output;
    }

    @Override
    public String convertToInitialCondition() {
        String output = signalName + "=" + convertToInt(thresholds.getCenter());
        return output;
    }

}

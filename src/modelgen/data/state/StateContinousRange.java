package modelgen.data.state;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import modelgen.data.DataType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;

public class StateContinousRange extends State implements IState {
    private Double lowerBound, upperBound;
    
    public StateContinousRange(String name, Integer id, Double start, Double end, Double lowBound, Double upperBound) {
        super(name, id, start, end);
        this.lowerBound = lowBound;
        this.upperBound = upperBound;
    }

    public StateContinousRange(StateContinousRange toCopy) {
        this(toCopy.signalName, toCopy.stateId, toCopy.start, toCopy.end, toCopy.lowerBound, toCopy.upperBound);
    }

    @Override
    public String convertToString() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return "[" + df.format(lowerBound) + ", " + df.format(upperBound) + "]";
    }

    @Override
    public DataType getType() {
        return DataType.CONTINOUS_RANGE;
    }

    @Override
    public IState makeCopy() {
        return new StateContinousRange(this);
    }

    @Override
    public RawDataChunk generateSignal(RawDataChunk baseSignal) {
        RawDataChunk outputData = new RawDataChunk();
        
        Random r = new Random();
        RawDataPoint prevPoint = null;
        for (RawDataPoint curPoint: baseSignal) {
            if (start <= curPoint.getTime() && curPoint.getTime() <= end) {
                RawDataPoint newPoint;
                if (prevPoint == null) {
                    prevPoint = curPoint;
                    newPoint = curPoint;
                } else {
                    double delta = curPoint.getTime() - prevPoint.getTime();
                    double linearCoefficient = lowerBound + (upperBound - lowerBound) * r.nextDouble();
                    double newValue = prevPoint.getValue() + linearCoefficient * delta;

                    newPoint = new RawDataPoint(newValue, curPoint.getTime());
                    curPoint = newPoint;
                }
                outputData.add(newPoint);
            }
        }
        return outputData;
    }

    
}

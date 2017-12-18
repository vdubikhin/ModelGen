package modelgen.data.state;

import modelgen.data.DataType;

public class StateContinousRange extends State implements IState {
    private Double lowBound, upperBound;
    
    protected StateContinousRange(String name, Integer id, Double start, Double end, Double lowBound, Double upperBound) {
        super(name, id, start, end);
        this.lowBound = lowBound;
        this.upperBound = upperBound;
    }

    @Override
    public String convertToString() {
        return "[" + lowBound.toString() + ", " + upperBound.toString() + "]";
    }

    @Override
    public DataType getType() {
        return DataType.CONTINOUS_RANGE;
    }

}

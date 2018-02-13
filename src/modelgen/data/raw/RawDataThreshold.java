package modelgen.data.raw;

public class RawDataThreshold {
    Double lowBound, upperBound;

    public RawDataThreshold(Double lBound, Double uBound) {
        lowBound = lBound;
        upperBound = uBound;
    }

    public Double getLowBound() {
        return lowBound;
    }

    public Double getUpperBound() {
        return upperBound;
    }
}

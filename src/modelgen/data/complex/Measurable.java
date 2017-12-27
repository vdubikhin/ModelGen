package modelgen.data.complex;

public interface Measurable<T extends Measurable<T>> {
    double measureDistanceTo(T objectToMeasure);

    int compareTo(T objectToCompare);
}

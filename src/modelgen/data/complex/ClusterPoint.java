package modelgen.data.complex;

import java.text.DecimalFormat;

import modelgen.shared.Logger;

public abstract class ClusterPoint<T extends ClusterPoint<T>> implements Cloneable<T>, Measurable<T>,
                                                                Mergeable<T>, Metricable, Printable {
    final protected String ERROR_PREFIX = "ClusterPoint error. ";
    final protected String DEBUG_PREFIX = "ClusterPoint debug. ";

    final protected Integer DEBUG_LEVEL = 1;

    double center;
    double min, max;

    protected ClusterPoint(double data) {
        center = data;
        min = center;
        max = center;
    }

    protected ClusterPoint(ClusterPoint<T> toCopy) {
        center = toCopy.center;
        min = toCopy.min;
        max = toCopy.max;
    }

    public double getClusterCenter() {
        return center;
    }

    public double getClusterMin() {
        return min;
    }

    public double getClusterMax() {
        return max;
    }

    @Override
    public String convertToString() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        return "[" + df.format(min) + ", " + df.format(center) + ", " + df.format(max) + "]";
    }

    @Override
    public void print() {
        Logger.debugPrintln(DEBUG_PREFIX + convertToString() + " " + evaluate(), DEBUG_LEVEL);
    }

    @Override
    public boolean canMergeWith(T itemToMerge) {
        return true;
    }

    @Override
    public boolean mergeWith(T itemToMerge) {
        center = (center + itemToMerge.center)/2;
        min = Math.min(min, itemToMerge.min);
        max = Math.max(max, itemToMerge.max);
        return true;
    }

    @Override
    public double measureDistanceTo(T objectToMeasure) {
        return Math.abs(center - objectToMeasure.center);
    }

    @Override
    public int compareTo(T objectToCompare) {
        return Double.compare(center, objectToCompare.center);
    }
}

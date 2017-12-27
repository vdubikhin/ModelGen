package modelgen.data.complex;

public class ClusterPointEvalHelper {
    double maxDeviation;

    public ClusterPointEvalHelper() {
        maxDeviation = 0.0;
    }

    public void processPoint(ClusterPointValue point) {
        double pointDeviation = Math.abs(point.max - point.min);
        maxDeviation = Math.max(pointDeviation, maxDeviation);
    }

    public double getDeviation() {
        return maxDeviation;
    }
}

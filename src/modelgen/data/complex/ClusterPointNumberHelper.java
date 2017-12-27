package modelgen.data.complex;

public class ClusterPointNumberHelper {
    int totalNum;
    double maxDuration;

    public ClusterPointNumberHelper() {
        totalNum = 0;
        maxDuration = 0;
    }

    public void processPoint(ClusterPointNumbers clusterPointNumbers) {
        totalNum++;
        maxDuration += clusterPointNumbers.getClusterDuraion();
    }

    public int getTotalNum() {
        return totalNum;
    }

    public double getMaxDuration() {
        return maxDuration;
    }
}

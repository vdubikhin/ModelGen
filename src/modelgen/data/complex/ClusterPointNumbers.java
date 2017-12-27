package modelgen.data.complex;

public class ClusterPointNumbers extends ClusterPoint<ClusterPointNumbers> 
                                 implements Cloneable<ClusterPointNumbers>, Measurable<ClusterPointNumbers>,
                                            Mergeable<ClusterPointNumbers>, Metricable, Printable {
    ClusterPointNumberHelper helper;
    double clusterDuration;
    int pointNum;

    public ClusterPointNumbers(double data, ClusterPointNumberHelper helper) {
        super(data);
        this.helper = helper;
        clusterDuration = data;
        if (helper != null)
            helper.processPoint(this);
        pointNum = 1;
    }

    public ClusterPointNumbers(ClusterPointNumbers toCopy) {
        super(toCopy);
        pointNum = toCopy.pointNum;
        helper = toCopy.helper;
        clusterDuration = toCopy.clusterDuration;
    }

    public int getNumPoints() {
        return pointNum;
    }

    public double getClusterDuraion() {
        return clusterDuration;
    }

    @Override
    public double evaluate() {
//        return (double) pointNum/helper.getTotalNum();
        return clusterDuration/helper.getMaxDuration();
    }

    @Override
    public boolean mergeWith(ClusterPointNumbers itemToMerge) {
        if (super.mergeWith(itemToMerge)) {
            pointNum += itemToMerge.pointNum;
            clusterDuration += itemToMerge.clusterDuration;
            return true;
        }
         return false;
    }
    
    @Override
    public ClusterPointNumbers makeCopy() {
        return new ClusterPointNumbers(this);
    }
}

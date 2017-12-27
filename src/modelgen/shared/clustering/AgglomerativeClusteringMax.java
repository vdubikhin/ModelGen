package modelgen.shared.clustering;

import modelgen.data.complex.Cloneable;
import modelgen.data.complex.Measurable;
import modelgen.data.complex.Mergeable;
import modelgen.data.complex.Metricable;
import modelgen.data.complex.Printable;

public class AgglomerativeClusteringMax<T extends Cloneable<T> & Measurable<T> &
                                                  Mergeable<T> & Metricable & Printable>
                                          extends AgglomerativeClustering<T> implements ClusteringAlgorithm<T> {

    @Override
    protected boolean layerCondition(ICluster<T> cl, double metric) {
        return cl.evaluate() <= metric;
    }
}

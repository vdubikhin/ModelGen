package modelgen.shared.clustering;

import java.util.List;

import modelgen.data.complex.Cloneable;
import modelgen.data.complex.Measurable;
import modelgen.data.complex.Mergeable;
import modelgen.data.complex.Metricable;
import modelgen.data.complex.Printable;

public interface ClusteringAlgorithm<T extends Cloneable<T> & Measurable<T> & Mergeable<T> & Metricable & Printable> {
    List<T> layerSearch(ICluster<T> root, double metric);

    ICluster<T> formDendrogram(List<T> elementArray);
}
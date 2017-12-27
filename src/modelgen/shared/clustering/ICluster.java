package modelgen.shared.clustering;

import java.util.List;

import modelgen.data.complex.Cloneable;
import modelgen.data.complex.Measurable;
import modelgen.data.complex.Mergeable;
import modelgen.data.complex.Metricable;
import modelgen.data.complex.Printable;

public interface ICluster<T extends Cloneable<T> & Measurable<T> & Mergeable<T> & Metricable & Printable> 
                            extends Measurable<Cluster<T>>, Mergeable<Cluster<T>>, Metricable, Printable {
    List<Cluster<T>> getChildren();

    T getElement();
}
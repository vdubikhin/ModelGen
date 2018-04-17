package modelgen.shared.clustering;

import java.util.ArrayList;
import java.util.List;

import modelgen.data.complex.Cloneable;
import modelgen.data.complex.Measurable;
import modelgen.data.complex.Mergeable;
import modelgen.data.complex.Metricable;
import modelgen.data.complex.Printable;
import modelgen.shared.Logger;


public abstract class AgglomerativeClustering<T extends Cloneable<T> & Measurable<T> &
                                                        Mergeable<T> & Metricable & Printable>
                                                implements ClusteringAlgorithm<T> {
    protected String ERROR_PREFIX = "AgglomerativeClustering error.";
    protected String DEBUG_PREFIX = "AgglomerativeClustering debug.";


    public AgglomerativeClustering() {
    }

    @Override
    public ICluster<T> formDendrogram(List<T> elementArray) {
        try {
            List<Cluster<T>> clusterArray = new ArrayList<>();
            
            //Initialize array with singletons
            for (T element: elementArray) {
                clusterArray.add(new Cluster<T>(element));
            }

            if (clusterArray.isEmpty())
                return null;

            while (clusterArray.size() >= 1) {
                clusterArray.sort((cl1, cl2) -> cl1.compareTo(cl2));

                // Find two closest clusters
                Double minDistance = Double.POSITIVE_INFINITY;
                int index = -1;

                for (int i = 0; i < clusterArray.size() - 1; i++) {
                    double distance = clusterArray.get(i).measureDistanceTo(clusterArray.get(i + 1));

                    if (distance < minDistance) {
                        index = i;
                        minDistance = distance;
                    }
                }

                if (index < 0)
                    break; //something bad

                // Merge clusters
                Cluster<T> tempCluster = new Cluster<>(clusterArray.get(index), clusterArray.get(index + 1));
                clusterArray.set(index, tempCluster);
                clusterArray.remove(index + 1);
            }

            if (clusterArray.size() != 1) {
                Logger.errorLogger(ERROR_PREFIX + " Failed to created dendogram.");
                return null;
            }

            ICluster<T> root = clusterArray.get(0);
            return root;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    protected abstract boolean layerCondition(ICluster<T> cl, double metric);

    /* (non-Javadoc)
     * @see modelgen.shared.clustering.ClusteringAlgorithm#layerSearch(double)
     */
    @Override
    public List<T> layerSearch(ICluster<T> root, double metric) {
        try {
            if (root == null) {
                Logger.errorLogger(ERROR_PREFIX + " Root not initialized.");
                return null;
            }

            //Logger.debugPrintln(DEBUG_PREFIX + " Search for layer with metric: " + metric ,1);

            List<ICluster<T>> searchLayer = new ArrayList<>();
            List<ICluster<T>> outputLayer = new ArrayList<>();
            searchLayer.add(root);

            while(!searchLayer.isEmpty()) {
                List<ICluster<T>> extraLayer = new ArrayList<>();
                for (ICluster<T> cl: searchLayer) {
                    if (layerCondition(cl, metric)) {
                        outputLayer.add(cl);
                    } else {
                        List<Cluster<T>> children = cl.getChildren();
                        //Either search among children or add cluster to the output
                        if (children != null && !children.isEmpty())
                            extraLayer.addAll(children);
                        else
                            outputLayer.add(cl);
                    }
                }
                searchLayer = extraLayer;
            }

            if (!outputLayer.isEmpty()) {
                //Logger.debugPrintln(DEBUG_PREFIX + " Creating output layer.",1);
                List<T> output = new ArrayList<>();
                for (ICluster<T> cl: outputLayer) {
                    T curElement = cl.getElement();
                    output.add(curElement);
                }
                return output;
            }
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

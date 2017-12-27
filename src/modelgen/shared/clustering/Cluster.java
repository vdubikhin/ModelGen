package modelgen.shared.clustering;


import java.util.ArrayList;
import java.util.List;

import modelgen.data.complex.Cloneable;
import modelgen.data.complex.Measurable;
import modelgen.data.complex.Mergeable;
import modelgen.data.complex.Metricable;
import modelgen.data.complex.Printable;
import modelgen.shared.Logger;

public class Cluster<T extends Cloneable<T> & Measurable<T> & Mergeable<T> & Metricable & Printable> 
                    implements ICluster<T> {
    protected String ERROR_PREFIX = "Cluster error.";
    protected String DEBUG_PREFIX = "Cluster debug.";

    private Cluster<T> childLeft, childRight;
    private T element;

    public Cluster(T initElement) {
        element = initElement;
    }

    public Cluster(Cluster<T> childLeft, Cluster<T> childRight) {
        T mergedElement = childLeft.element.makeCopy();
        if(!mergedElement.mergeWith(childRight.element)) {
            Logger.errorLogger(ERROR_PREFIX + " Failed to merge elements, when creating new cluster.");
        }

        this.childLeft = childLeft;
        this.childRight = childRight;
        this.element = mergedElement;
    }

    @Override
    public double evaluate() {
        return element.evaluate();
    }

    @Override
    public boolean canMergeWith(Cluster<T> itemToMerge) {
        return element.canMergeWith(itemToMerge.element);
    }

    @Override
    public boolean mergeWith(Cluster<T> itemToMerge) {
        return element.mergeWith(itemToMerge.element);
    }

    @Override
    public double measureDistanceTo(Cluster<T> objectToMeasure) {
        return element.measureDistanceTo(objectToMeasure.element);
    }

    @Override
    public int compareTo(Cluster<T> objectToCompare) {
        return element.compareTo(objectToCompare.element);
    }

    @Override
    public String convertToString() {
        return element.convertToString();
    }

    @Override
    public void print() {
        element.print();
    }

    /* (non-Javadoc)
     * @see modelgen.shared.clustering.ICluster#getChildren()
     */
    @Override
    public List<Cluster<T>> getChildren() {
        if (childLeft != null && childRight != null) {
            List<Cluster<T>> result = new ArrayList<>();
            result.add(childLeft);
            result.add(childRight);
            return result;
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see modelgen.shared.clustering.ICluster#getElement()
     */
    @Override
    public T getElement() {
        return element;
    }
}

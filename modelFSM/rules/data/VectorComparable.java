package modelFSM.rules.data;

public interface VectorComparable< T extends VectorComparable<T> > extends Comparable {
    // Compare self with given vector
    public VectorEquality compareTo(T vectorCmp);
    
    public int getId();
}

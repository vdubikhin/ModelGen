package modelgen.data.complex;

import modelgen.data.pattern.DataComparable;

public interface ComplexComparable< T extends ComplexComparable<T> > extends DataComparable {
    public DataEquality compareTo(T vectorCmp);

    public Integer getId();
}

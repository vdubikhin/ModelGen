package modelgen.processor.rulemining.conflictdetection;

import modelgen.data.complex.ComplexComparable;

public interface ConflictComparable< T extends ComplexComparable<T>, V extends RuleComparable<T, V> >
    extends ComplexComparable<T>, ERuleComparable {
    String toString();

    DataEquality compareTo(T vectorToCmp);

    V getRuleToFix();

    V getOffendingRule();

    Integer getOffendingVectorId();
}

package modelFSM.rules;

import modelFSM.rules.data.VectorComparable;

interface ConflictComparable< T extends VectorComparable<T>, V extends RuleComparable<T, V> >
    extends VectorComparable<T>, Comparable {
    String toString();

    VectorEquality compareTo(T vectorToCmp);
    
    V getRuleToFix();
}

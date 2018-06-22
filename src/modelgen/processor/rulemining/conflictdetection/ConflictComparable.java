package modelgen.processor.rulemining.conflictdetection;

import modelgen.data.complex.ComplexComparable;
import modelgen.data.stage.IStageData;

public interface ConflictComparable< T extends ComplexComparable<T>, V extends RuleComparable<T, V> >
    extends ComplexComparable<T>, ERuleComparable, IStageData {
    String toString();

    DataEquality compareTo(T vectorToCmp);

    V getRuleToFix();

    V getOffendingRule();

    Integer getOffendingVectorId();
}

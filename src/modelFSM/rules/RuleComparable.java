package modelFSM.rules;

import java.util.List;

import modelFSM.data.Event;
import modelFSM.rules.data.VectorComparable;

interface RuleComparable< T extends VectorComparable<T>, V extends RuleComparable<T, V> >
    extends Comparable {
    void print();
    
    List<ConflictComparable<T, V>> compareRules(V ruleToCmp, RuleConflictType conflictType);
    
    T getRuleVectorById(Integer id);
    
    T getFullRuleVectorById(Integer id);
    
    Event getOutputState();
}

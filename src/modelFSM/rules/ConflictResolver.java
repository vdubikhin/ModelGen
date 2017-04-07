package modelFSM.rules;

import java.util.List;

import modelFSM.rules.data.VectorComparable;

interface ConflictResolver< T extends VectorComparable<T>, V extends RuleComparable<T, V> > {
    // Try to resolve specified conflict and return true if successful
    boolean ResolveConflict(ConflictComparable<T,V> conflictToResolve, List<V> ruleList);
    
    // Return cost for resolving the conflict
    Integer ResolveCost(ConflictComparable<T,V> conflictToResolve);
}

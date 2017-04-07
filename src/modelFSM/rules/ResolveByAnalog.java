package modelFSM.rules;

import java.util.List;

import modelFSM.rules.data.PredicateVector;

class ResolveByAnalog< V extends RuleComparable<PredicateVector, V> > implements ConflictResolver<PredicateVector, V> {
    
    int cost;
    
    public ResolveByAnalog(int cost) {
        this.cost = cost;
    }

    @Override
    public boolean ResolveConflict(ConflictComparable<PredicateVector, V> conflictToResolve, List<V> ruleList) {
        try {
            int vectorId = conflictToResolve.getId();
            V ruleToFix = conflictToResolve.getRuleToFix();
            
            
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Integer ResolveCost(ConflictComparable<PredicateVector, V> conflictToResolve) {
        return cost;
    }
}

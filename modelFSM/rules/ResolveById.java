package modelFSM.rules;

import java.util.List;

import modelFSM.rules.data.PredicateVector;

class ResolveById< V extends RuleComparable<PredicateVector, V> > implements ConflictResolver<PredicateVector, V> {
    
    int cost;
    
    public ResolveById(int cost) {
        this.cost = cost;
    }

    @Override
    public boolean ResolveConflict(ConflictComparable<PredicateVector, V> conflictToResolve, List<V> ruleList) {
        try {
            int vectorId = conflictToResolve.getId();
            V ruleToFix = conflictToResolve.getRuleToFix();
            PredicateVector vectorToFix = ruleToFix.getRuleVectorById(vectorId);
            
            // Check quickly if counter state has been added already
            if (vectorToFix.flagged)
                return false;
            else 
                vectorToFix.flagged = true;
            
            return true;
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

package modelFSM.rules;

import modelFSM.rules.data.VectorComparable;

class ConflictCSC<T extends VectorComparable<T>, V extends RuleComparable<T, V>> implements ConflictComparable<T, V> {

    V ruleA, ruleB;
    int vectorA, vectorB;
    // TODO: remove these variables if they are not used anywhere
    VectorEquality conflictType;
    RuleConflictType ruleConflictType;
    
    public ConflictCSC(V ruleA, V ruleB, 
            T vectorA, T vectorB,
            RuleConflictType ruleConflictType) {
        
        conflictType = vectorA.compareTo(vectorB);
        this.ruleConflictType = ruleConflictType;
        
        this.ruleA = null;
        this.ruleB = null;
        
        if (conflictType == VectorEquality.SUBSET) {
            this.ruleA = ruleA;
            this.ruleB = ruleB;
            this.vectorA = vectorA.getId();
            this.vectorB = vectorB.getId();
        }
        
        if (conflictType == VectorEquality.SUPERSET && ruleConflictType != RuleConflictType.RuleVsPredicate) {
            this.ruleA = ruleB;
            this.ruleB = ruleA;
            this.vectorA = vectorB.getId();
            this.vectorB = vectorA.getId();
        }
        
    }

    public String toString() {
        String result = "RuleA: " + ruleA.getOutputState().eventId +" VectorA: " + vectorA +
                " RuleB: " + ruleB.getOutputState().eventId +" VectorB: " + vectorB + 
                " Type: " + conflictType.toString() + "  " + ruleConflictType.toString();
        return result;
    }
    
    public void print() {
        System.out.println(this.toString());
    }
    
    @Override
    public VectorEquality compareTo(T vectorToCmp) {
        VectorEquality conflictType = ruleA.getRuleVectorById(vectorA).compareTo(vectorToCmp);
        return conflictType;
    }
    
    @Override
    public V getRuleToFix() {
        return ruleA;
    }
    
    @Override
    public int getId() {
        return vectorA;
    }

}

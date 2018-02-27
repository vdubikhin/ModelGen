package modelgen.processor.rulemining.conflictdetection;

import modelgen.data.pattern.PatternVector;

public class ConflictCSC implements ConflictComparable<PatternVector, RuleFSMVector> {

    RuleFSMVector ruleA, ruleB;
    int vectorA, vectorB;
    // TODO: remove these variables if they are not used anywhere
    DataEquality conflictType;
    RuleConflictType ruleConflictType;
    
    public ConflictCSC(RuleFSMVector ruleA, RuleFSMVector ruleB, 
            PatternVector vectorA, PatternVector vectorB,
            RuleConflictType ruleConflictType) {
        
        conflictType = vectorA.compareTo(vectorB);
        this.ruleConflictType = ruleConflictType;
        
        this.ruleA = null;
        this.ruleB = null;
        
        if (conflictType == DataEquality.SUBSET || conflictType == DataEquality.EQUAL) {
            this.ruleA = ruleA;
            this.ruleB = ruleB;
            this.vectorA = vectorA.getId();
            this.vectorB = vectorB.getId();
        }
        
        if (conflictType == DataEquality.SUPERSET && ruleConflictType != RuleConflictType.RuleVsFullPattern) {
            this.ruleA = ruleB;
            this.ruleB = ruleA;
            this.vectorA = vectorB.getId();
            this.vectorB = vectorA.getId();
        }
        
    }

    public String toString() {
        String result = "RuleA: " + ruleA.getOutputState().getId() +" VectorA: " + vectorA +
                " RuleB: " + ruleB.getOutputState().getId() +" VectorB: " + vectorB + 
                " Type: " + conflictType.toString() + "  " + ruleConflictType.toString();
        return result;
    }
    
    public void print() {
        System.out.println(this.toString());
    }
    
    @Override
    public DataEquality compareTo(PatternVector vectorToCmp) {
        DataEquality conflictType = ruleA.getRuleVectorById(vectorA).compareTo(vectorToCmp);
        return conflictType;
    }

    @Override
    public RuleFSMVector getRuleToFix() {
        return ruleA;
    }

    @Override
    public Integer getId() {
        return vectorA;
    }

    @Override
    public RuleFSMVector getOffendingRule() {
        return ruleB;
    }

    @Override
    public Integer getOffendingVectorId() {
        return vectorB;
    }
}

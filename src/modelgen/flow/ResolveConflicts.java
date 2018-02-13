package modelgen.flow;

import modelgen.data.pattern.PatternVector;
import modelgen.manager.ManagerLowCost;
import modelgen.processor.rulemining.conflictdetection.ConflictComparable;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.processor.rulemining.conflictresolution.ConflictResolverFactory;

public class ResolveConflicts extends Stage<ConflictComparable<PatternVector, RuleFSMVector>, 
                                            RuleComparable<PatternVector, RuleFSMVector>>
                              implements IStage<ConflictComparable<PatternVector, RuleFSMVector>, 
                                                RuleComparable<PatternVector, RuleFSMVector>> {
    public ResolveConflicts() {
        dataManager = new ManagerLowCost<ConflictComparable<PatternVector, RuleFSMVector>, 
                                         RuleComparable<PatternVector, RuleFSMVector>>();
        processorFactory = new ConflictResolverFactory();

        ERROR_PREFIX = "Stage: ResolveConflicts error.";
        DEBUG_PREFIX = "Stage: ResolveConflicts debug.";
        PD_PREFIX    = "ResolveConflicts_";
    }
}

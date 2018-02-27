package modelgen.processor.rulemining.conflictresolution;


import modelgen.data.pattern.PatternVector;
import modelgen.data.property.Properties;
import modelgen.data.property.PropertyManager;
import modelgen.processor.DataProcessor;
import modelgen.processor.IDataProcessor;
import modelgen.processor.rulemining.conflictdetection.ConflictComparable;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.shared.Logger;

public class ResolveById extends DataProcessor<RuleComparable<PatternVector, RuleFSMVector>>
                  implements IDataProcessor<RuleComparable<PatternVector, RuleFSMVector>> {
    final private Integer RESOLVE_COST = 50;

    private ConflictComparable<PatternVector, RuleFSMVector> conflictToResolve;

    public ResolveById() {
        this.conflictToResolve = null;
        
        name = "ResolveById";

        ERROR_PREFIX = "DataProcessor: ResolveById error.";
        DEBUG_PREFIX = "DataProcessor: ResolveById debug.";

        valueBaseCost.setValue(RESOLVE_COST);

        Properties moduleProperties = propertyManager.getModuleProperties();
        moduleProperties.put(valueBaseCost.getName(), valueBaseCost);

        propertyManager = new PropertyManager(moduleProperties, ERROR_PREFIX);
    }

    public ResolveById(ConflictComparable<PatternVector, RuleFSMVector> conflict) {
        this();
        conflictToResolve = conflict;
    }

    @Override
    public int processCost() {
        Integer vectorToFixId = conflictToResolve.getId();
        Integer dependancyVectorId = conflictToResolve.getOffendingVectorId();

        RuleFSMVector ruleToFix = conflictToResolve.getRuleToFix();

        PatternVector vectorToFix = ruleToFix.getRuleVectorById(vectorToFixId);

        if (vectorToFix.hasDependency(dependancyVectorId))
            return -1;

        return valueBaseCost.getValue();
    }

    @Override
    public RuleComparable<PatternVector, RuleFSMVector> processData() {
        try {
            Integer vectorToFixId = conflictToResolve.getId();
            Integer dependencyVectorId = conflictToResolve.getOffendingVectorId();

            RuleFSMVector ruleToFix = conflictToResolve.getRuleToFix();
            RuleFSMVector dependencyRule = conflictToResolve.getOffendingRule();

            PatternVector vectorToFix = ruleToFix.getRuleVectorById(vectorToFixId);
            PatternVector vectorToFixFull = ruleToFix.getFullRuleVectorById(vectorToFixId);

            PatternVector dependencyVector = dependencyRule.getRuleVectorById(dependencyVectorId);
            PatternVector dependencyVectorFull = dependencyRule.getFullRuleVectorById(dependencyVectorId);

            // Check quickly if counter state has been added already
            if (vectorToFix.hasDependency(dependencyVectorId))
                return null;
            else {
                vectorToFix.addDependency(dependencyVectorId);
                dependencyVector.addDependency(vectorToFixId);

                vectorToFixFull.addDependency(dependencyVectorId);
                dependencyVectorFull.addDependency(vectorToFixId);
            }
            
            return ruleToFix;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }



}

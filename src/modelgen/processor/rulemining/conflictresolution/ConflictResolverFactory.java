package modelgen.processor.rulemining.conflictresolution;

import modelgen.data.pattern.PatternVector;
import modelgen.processor.DataProcessorFactory;
import modelgen.processor.IDataProcessorFactory;
import modelgen.processor.rulemining.conflictdetection.ConflictComparable;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.shared.Logger;

public class ConflictResolverFactory extends DataProcessorFactory<ConflictComparable<PatternVector, RuleFSMVector>, 
                                                                  RuleComparable<PatternVector, RuleFSMVector>>
                                     implements IDataProcessorFactory<ConflictComparable<PatternVector, RuleFSMVector>, 
                                                RuleComparable<PatternVector, RuleFSMVector>> {
    public ConflictResolverFactory() {
    try {
            processorClasses.put(ResolveById.class.newInstance().getName(), ResolveById.class);
            processorClasses.put(ResolveByState.class.newInstance().getName(), ResolveByState.class);
            processorClasses.put(ResolveByVector.class.newInstance().getName(), ResolveByVector.class);
            processorClasses.put(ResolveByAnalog.class.newInstance().getName(), ResolveByAnalog.class);
            inputDataClass = ConflictComparable.class;
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Failed to create processor factory.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
    }
}

package modelgen.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import modelgen.data.pattern.PatternVector;
import modelgen.data.stage.StageDataRule;
import modelgen.data.stage.StageDataState;
import modelgen.processor.rulemining.SignalDataPatterns;
import modelgen.processor.rulemining.StatesToPatternConverter;
import modelgen.processor.rulemining.conflictdetection.ConflictComparable;
import modelgen.processor.rulemining.conflictdetection.ConflictDetector;
import modelgen.processor.rulemining.conflictdetection.ConflictMap;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.shared.Logger;

public class RuleMiningFSM {
    protected String ERROR_PREFIX = "RuleMiningFSM error.";
    protected String DEBUG_PREFIX = "RuleMiningFSM debug.";
    protected int DEBUG_LEVEL = 1; //TODO: change to property class

    IStage<ConflictComparable<PatternVector, RuleFSMVector>, 
           RuleComparable<PatternVector, RuleFSMVector>> conflictResolver;

    public RuleMiningFSM() {
        conflictResolver = new ResolveConflicts();
    }
    
    public List<StageDataRule> processData(List<StageDataState> signalStates) {
        try {
            SignalDataPatterns signalPatterns = new StatesToPatternConverter().convertStatesToPatterns(signalStates);

            if (signalPatterns == null) {
                Logger.errorLogger(ERROR_PREFIX + " No input data.");
                return null;
            }

            ConflictDetector dataRules = new ConflictDetector(signalPatterns);
            
            int safeLimit = 0;
            Map<String, ConflictMap> conflicts = dataRules.detectConflicts();

            if (conflicts == null)
                return null;

            List<Entry<RuleComparable<PatternVector, RuleFSMVector>, Double>> dataOut = null;
            while (!conflicts.isEmpty() && safeLimit++ <= 50) {
                List<ConflictComparable<PatternVector, RuleFSMVector>> conflictList = new ArrayList<>();

                //Convert all conflicts into list
                for (String signal: conflicts.keySet()) {
                    conflictList.addAll(conflicts.get(signal).values());
                }

                dataOut = conflictResolver.processData(conflictList);
                //TODO: add check for dataOut
                conflicts = dataRules.detectConflicts();
                
                System.out.println("Iteration: " + safeLimit);
                
                for (RuleComparable<PatternVector, RuleFSMVector> entry: dataRules.getStageRules()) {
                    //TODO: debug print
                    entry.print();
                }
            }

            List<RuleFSMVector> resolvedRules = dataRules.getStageRules();
            if (!conflicts.isEmpty() || resolvedRules == null || resolvedRules.isEmpty())
                return null;

            List<StageDataRule> output = new ArrayList<>();
            System.out.println("--------------");
            for (String signalName: signalPatterns.getInitialStates().keySet()) {
                List<RuleComparable<PatternVector, RuleFSMVector>> rules = new ArrayList<>();
                Set<Integer> activeRules = new HashSet<>();

                //Collect all rules for the same output signal
                for (RuleComparable<PatternVector, RuleFSMVector> entry: dataRules.getStageRules()) {
                    if (entry.getOutputState().getSignalName().equals(signalName)) {
                        entry.print();
                        

                        entry.minimizeRules();
                        rules.add(entry);

                        for (PatternVector signalRule: entry.getRulePatterns()) {
                            activeRules.add(signalRule.getId());
                        }

                    }
                }

                System.out.println("--Minimization--");

                //Remove pre/postset dependencies for removed rules
                for (RuleComparable<PatternVector, RuleFSMVector> entry: dataRules.getStageRules()) {
                    for (PatternVector signalRule: entry.getRulePatterns()) {
                        signalRule.getPreSet().retainAll(activeRules);
                        signalRule.getPostSet().retainAll(activeRules);
                    }
                    //TODO: debug print
                    entry.print();
                }
                
                output.add(new StageDataRule(rules, signalPatterns.getInitialStates().get(signalName)));
            }

            if (output.isEmpty())
                return null;

            return output;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

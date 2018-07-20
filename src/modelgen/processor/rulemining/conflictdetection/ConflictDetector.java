package modelgen.processor.rulemining.conflictdetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelgen.data.pattern.DataPattern;
import modelgen.data.pattern.DataPatterns;
import modelgen.data.pattern.PatternVector;
import modelgen.processor.rulemining.SignalDataPatterns;
import modelgen.processor.rulemining.conflictdetection.ERuleComparable.RuleConflictType;
import modelgen.shared.Logger;



public class ConflictDetector {
    protected String ERROR_PREFIX = "StatesToPatternConvertert error.";
    protected String DEBUG_PREFIX = "StatesToPatternConverter debug.";
    protected int DEBUG_LEVEL = 1; //TODO: change to property class

    Map<String, List<RuleFSMVector>> signalsDataRules;

    public ConflictDetector(SignalDataPatterns signalDataPatterns) {
        signalsDataRules = new HashMap<>();

        Map<String, DataPatterns> signalPatterns = signalDataPatterns.getSignalPatterns();
        for (String signal: signalPatterns.keySet()) {
            // Combine all patterns leading to the same output
            HashMap<Integer, DataPatterns> rulePatterns = new HashMap<>();
            for (DataPattern curPattern: signalPatterns.get(signal)) {
                Integer curEventId = curPattern.getOutputState().getId();

                DataPatterns outputDataArray;

                if (rulePatterns.containsKey(curEventId)) {
                    outputDataArray = rulePatterns.get(curEventId);
                } else {
                    outputDataArray = new DataPatterns();
                }
              
                outputDataArray.add(curPattern);
                rulePatterns.put(curEventId, outputDataArray);
            }

            // Initialize data rules array
            List<RuleFSMVector> dataRules = new ArrayList<>();
            
            for (Integer curEventId: rulePatterns.keySet()) {
                RuleFSMVector newRule = new RuleFSMVector(rulePatterns.get(curEventId).get(0).getOutputState(),
                        rulePatterns.get(curEventId));
                dataRules.add(newRule);
            }

            signalsDataRules.put(signal, dataRules);

            // TODO: debug printing
            System.out.println();
            for (RuleComparable<?, ?> rule: dataRules)
                rule.print();
        }
    }

    public Map<String, ConflictMap> detectConflicts() {
        try {
            Map<String, ConflictMap> signalsConflicts = new HashMap<>();

            for (String signal: signalsDataRules.keySet()) {
                ConflictMap signalConflicts = new ConflictMap();
                List<RuleFSMVector> dataRules = signalsDataRules.get(signal);

                for (RuleConflictType conflictType: RuleConflictType.values()) {
                    //TODO: debug
//                    if (conflictType == RuleConflictType.RuleVsFullPattern)
//                        continue;
                    
                    for (int i = 0; i < dataRules.size(); i++) {
                        RuleFSMVector ruleA = dataRules.get(i);
                        for (int j = 0; j < dataRules.size(); j++) {
                            if (i == j)
                                continue;
                            
                            RuleFSMVector ruleB = dataRules.get(j);
                            
                            List<ConflictComparable<PatternVector, RuleFSMVector>> conflictList =
                                    ruleA.compareRules(ruleB, conflictType);
                            
                            for (ConflictComparable<PatternVector, RuleFSMVector> conflict: conflictList) {
                                if (signalConflicts.containsKey(conflict.getId())) 
                                    continue;

                                signalConflicts.put(conflict.getId(), conflict);
                            }
                        }
                    }
                }

                if (!signalConflicts.isEmpty())
                    signalsConflicts.put(signal, signalConflicts);
            }

            return signalsConflicts;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        }

        return null;
    }

    public List<RuleFSMVector> getStageRules() {
        try {
            if (signalsDataRules == null)
                return null;
            
            List<RuleFSMVector> output = new ArrayList<>();
            for (String signal: signalsDataRules.keySet()) {
                output.addAll(signalsDataRules.get(signal));
            }
            return output;
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
}

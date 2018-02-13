package modelgen.processor.rulemining.conflictdetection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import modelgen.data.pattern.DataPattern;
import modelgen.data.pattern.DataPatterns;
import modelgen.data.pattern.PatternVector;
import modelgen.data.pattern.StateVector;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.state.IStateTimeless;
import modelgen.shared.Logger;

public class RuleFSMVector implements RuleComparable<PatternVector, RuleFSMVector> {
    protected String ERROR_PREFIX = "StatesToPatternConvertert error.";
    protected String DEBUG_PREFIX = "StatesToPatternConverter debug.";
    protected int DEBUG_LEVEL = 1; //TODO: change to property class

    IStateTimeless outputState;
    Map<Integer, DataPattern> outputPatterns;
    
    public RuleFSMVector(IStateTimeless state, DataPatterns patterns) {
        outputState = state;
        outputPatterns = new HashMap<>();
        for (DataPattern curPattern: patterns) {
            outputPatterns.put(curPattern.getFullRuleVector().getId(), curPattern);
        }
    }

    private void printVectors(Collection<DataPattern> patterns, boolean printRuleVectors) {
        try {
            for (DataPattern curPattern: patterns) {
                PatternVector pattern;
                if (printRuleVectors)
                    pattern = curPattern.getRuleVector();
                else
                    pattern = curPattern.getFullRuleVector();

                Logger.debugPrintln(outputState.getSignalName() + "<" + curPattern.getOutputState().getId()  + ">", 
                        DEBUG_LEVEL);
                // Get set of printable names first
                HashSet<String> stateNames = new HashSet<String>();
                for (StateVector stateVector: pattern.values()) {
                    stateNames.addAll(stateVector.keySet());
                }
                
                int stateNum = 0;
                String prefixString = "";
                int leadingSpaceNum = 5;
                for (String stateName: stateNames) {
                    // Reverse order so we print final state last
                    List<Integer> patternVectorKeysReverse = new ArrayList<Integer>(pattern.keySet());
                    Collections.sort(patternVectorKeysReverse);
                    
                    String printLine;
                    int patternVectorId = pattern.getId();

                    prefixString = "";
                    
                    if (stateNum == (int)(stateNames.size()/2)) {
                        prefixString = patternVectorId + ": ";
                        for (int i = prefixString.length(); i < leadingSpaceNum; i++)
                            prefixString = " " + prefixString;
                        
                        printLine = prefixString + stateName + " ";
                    } else {
                        
                        for (int i = 0; i < leadingSpaceNum; i++)
                            prefixString += " ";
                        
                        printLine = prefixString + stateName + " ";
                    }
                    
                    for (Integer key: patternVectorKeysReverse) {
                        StateVector stateVector = pattern.get(key);
                        if (stateVector.containsKey(stateName))
                            printLine += stateVector.get(stateName).getId() + 
                                    stateVector.get(stateName).convertToString() + "-("+ 
                                    stateVector.get(stateName).getTimeStamp().getKey() + "); ";
                    }
                    Logger.debugPrintln(printLine, DEBUG_LEVEL);
                    stateNum++;
                }
                prefixString = "";
                for (int i = prefixString.length(); i < leadingSpaceNum; i++)
                    prefixString = " " + prefixString;
                
                if (stateNames.isEmpty())
                    Logger.debugPrintln(pattern.getId() + " unique: " + pattern.isUnique(), DEBUG_LEVEL);
                else 
                    Logger.debugPrintln(prefixString  + "unique: " + pattern.isUnique(), DEBUG_LEVEL);
                            
                Logger.debugPrintln("", DEBUG_LEVEL);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        }
    }
    
    public void printFullVectors() {
        printVectors(outputPatterns.values(), false);
    }
    
    public void printRuleVectors() {
        printVectors(outputPatterns.values(), true);
    }
    
    @Override
    public void print() {
        Logger.debugPrintln("\nPrinting rule vectors", DEBUG_LEVEL);
        printRuleVectors();
        Logger.debugPrintln("\nPrinting full rule vectors", DEBUG_LEVEL);
        printFullVectors();
    }
    
    @Override
    public IStateTimeless getOutputState() {
        return outputState;
    }

    @Override
    public List<ConflictComparable<PatternVector, RuleFSMVector>> compareRules(
        RuleFSMVector ruleToCmp, RuleConflictType conflictType) {
        try {
            // List of CSC conflicts
            ArrayList<ConflictComparable<PatternVector, RuleFSMVector>> conflictsList
                    = new ArrayList<ConflictComparable<PatternVector, RuleFSMVector>>();
            
            Map<Integer, DataPattern> patternsA = this.outputPatterns;
            Map<Integer, DataPattern> patternsB = ruleToCmp.outputPatterns;

            for (DataPattern patternA: patternsA.values()) {
                PatternVector vectorA = patternA.getRuleVector();
                for (DataPattern predicateVectorExtB: patternsB.values()) {
                    PatternVector vectorB = predicateVectorExtB.getRuleVector();
                    
                    if (conflictType == RuleConflictType.RuleVsRule)
                        vectorB = predicateVectorExtB.getRuleVector();
                    
                    if (conflictType == RuleConflictType.RuleVsPredicate)
                        vectorB = predicateVectorExtB.getFullRuleVector();
                        
                    ConflictComparable<PatternVector, RuleFSMVector> conflict = 
                            new ConflictCSC(this, ruleToCmp, vectorA, vectorB, conflictType);
                    
                    if (conflict.getRuleToFix() != null)
                        conflictsList.add(conflict);
                }
            }
            
            return conflictsList;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        }
        
        return null;
    }

    @Override
    public PatternVector getRuleVectorById(Integer id) {
        return outputPatterns.get(id).getRuleVector();
    }

    @Override
    public void resetRuleVectorById(Integer id) {
        outputPatterns.get(id).setRuleVector(new PatternVector(id));
    }


    @Override
    public PatternVector getFullRuleVectorById(Integer id) {
        return outputPatterns.get(id).getFullRuleVector();
    }

    @Override
    public void setFullRuleVectorById(Integer id, PatternVector vector) {
        outputPatterns.get(id).setFullRuleVector(vector);
    }

    @Override
    public HashMap<String, RawDataChunk> getAnalogDataById(Integer id) {
        return outputPatterns.get(id).getInputRawData();
    }
}

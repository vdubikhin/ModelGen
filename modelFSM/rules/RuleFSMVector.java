package modelFSM.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import modelFSM.data.Event;
import modelFSM.data.OutputDataChunk;
import modelFSM.rules.data.PredicateVector;
import modelFSM.rules.data.StateVector;

class RuleFSMVector implements RuleComparable<PredicateVector, RuleFSMVector>{

    class PredicateVectorExt {
        PredicateVector ruleVector;
        PredicateVector fullRuleVector;
        
        //Store raw analog stuff
        //OutputDataChunk
        
        public PredicateVectorExt(int id) {
            ruleVector = new PredicateVector(id);
            fullRuleVector = new PredicateVector(id);
        }
        
        public PredicateVectorExt(PredicateVectorExt copy) {
            ruleVector = new PredicateVector(copy.ruleVector);
            fullRuleVector = new PredicateVector(copy.fullRuleVector);
        }
    }
    
    Event outputState;
    
    // List of original predicates in vector format
    HashMap<Integer, PredicateVectorExt> predicateVectorList;
    
    public RuleFSMVector(Event outputState, List<OutputDataChunk> predicateList) {
        this.outputState = outputState;
        this.predicateVectorList = new HashMap<Integer, RuleFSMVector.PredicateVectorExt>();
        for (OutputDataChunk curDataChunk: predicateList) {
            PredicateVectorExt predVectorExt = new PredicateVectorExt(curDataChunk.outputPredicate.getId());
            predVectorExt.fullRuleVector = new PredicateVector(curDataChunk.outputPredicate);
            predicateVectorList.put(curDataChunk.outputPredicate.getId(), predVectorExt);
        }
    }
    
    private void printVectors(HashMap<Integer, PredicateVectorExt>predicateList, boolean printRuleVectors) {
        try {
            System.out.println(outputState.signalName + "<" + outputState.eventId + ">");
            
            for (PredicateVectorExt predicateVectorExt: predicateList.values()) {
                PredicateVector predicateVector;
                if (printRuleVectors)
                    predicateVector = predicateVectorExt.ruleVector;
                else
                    predicateVector = predicateVectorExt.fullRuleVector;
                
                // Get set of printable names first
                HashSet<String> stateNames = new HashSet<String>();
                for (StateVector stateVector: predicateVector.values()) {
                    stateNames.addAll(stateVector.keySet());
                }
                
                int stateNum = 0;
                String prefixString = "";
                int leadingSpaceNum = 5;
                for (String stateName: stateNames) {
                    // Reverse order so we print final state last
                    List<Integer> predicateVectorKeysReverse = new ArrayList<Integer>(predicateVector.keySet());
                    Collections.sort(predicateVectorKeysReverse);
                    Collections.reverse(predicateVectorKeysReverse);
                    
                    String printLine;
                    int predVectorId = predicateVector.getId();

                    prefixString = "";
                    
                    if (stateNum == (int)(stateNames.size()/2)) {
                        prefixString = predVectorId + ": ";
                        for (int i = prefixString.length(); i < leadingSpaceNum; i++)
                            prefixString = " " + prefixString;
                        
                        printLine = prefixString + stateName + " ";
                    } else {
                        
                        for (int i = 0; i < leadingSpaceNum; i++)
                            prefixString += " ";
                        
                        printLine = prefixString + stateName + " ";
                    }
                    
                    for (Integer key: predicateVectorKeysReverse) {
                        StateVector stateVector = predicateVector.get(key);
                        if (stateVector.containsKey(stateName))
                            printLine += stateVector.get(stateName).eventId + " ";
                    }
                    System.out.println(printLine);
                    stateNum++;
                }
                prefixString = "";
                for (int i = prefixString.length(); i < leadingSpaceNum; i++)
                    prefixString = " " + prefixString;
                
                if (stateNames.isEmpty())
                    System.out.println(predicateVector.getId() + ": " + predicateVector.flagged);
                else 
                    System.out.println(prefixString  + "unique: " + predicateVector.flagged);
                            
                System.out.println();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    
    public void printFullVectors() {
        printVectors(predicateVectorList, false);
    }
    
    public void printRuleVectors() {
        printVectors(predicateVectorList, true);
    }
    
    @Override
    public void print() {
        System.out.println("\nPrinting rule vectors");
        printRuleVectors();
        System.out.println("\nPrinting full rule vectors");
        printFullVectors();
    }
    
    @Override
    public Event getOutputState() {
        return outputState;
    }

    @Override
    public List<ConflictComparable<PredicateVector, RuleFSMVector>> compareRules(
        RuleFSMVector ruleToCmp, RuleConflictType conflictType) {
        try {
            // List of CSC conflicts
            ArrayList<ConflictComparable<PredicateVector, RuleFSMVector>> conflictsList
                    = new ArrayList<ConflictComparable<PredicateVector,RuleFSMVector>>();
            
            HashMap<Integer, PredicateVectorExt> predVectorA = this.predicateVectorList;
            HashMap<Integer, PredicateVectorExt> predVectorB = ruleToCmp.predicateVectorList;

            for (PredicateVectorExt predicateVectorExtA: predVectorA.values()) {
                PredicateVector vectorA = predicateVectorExtA.ruleVector;
                for (PredicateVectorExt predicateVectorExtB: predVectorB.values()) {
                    PredicateVector vectorB = predicateVectorExtB.ruleVector;
                    
                    if (conflictType == RuleConflictType.RuleVsRule)
                        vectorB = predicateVectorExtB.ruleVector;
                    
                    if (conflictType == RuleConflictType.RuleVsPredicate)
                        vectorB = predicateVectorExtB.fullRuleVector;
                        
                    ConflictComparable<PredicateVector, RuleFSMVector> conflict = 
                            new ConflictCSC<PredicateVector, RuleFSMVector>(this, ruleToCmp, vectorA, vectorB, conflictType);
                    
                    if (conflict.getRuleToFix() != null)
                        conflictsList.add(conflict);
                }
            }
            
            return conflictsList;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public PredicateVector getRuleVectorById(Integer id) {
        return predicateVectorList.get(id).ruleVector;
    }
    
    @Override
    public PredicateVector getFullRuleVectorById(Integer id) {
        return predicateVectorList.get(id).fullRuleVector;
    }
}

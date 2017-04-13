package modelFSM.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import modelFSM.data.OutputDataChunk;
import modelFSM.data.Predicate;
import modelFSM.rules.data.PredicateVector;


public class RuleManager {
    private final static boolean DISGARD_INIT_STATE = true;
    
    // TODO: move to a separate class
    class RulesNode implements Comparable {
        //List of all data rules
        ArrayList<RuleFSMVector> dataRules;
        //List of data rules conflicts
        HashMap<Integer, ConflictComparable<PredicateVector, RuleFSMVector>> conflictSet;
        //List of child nodes
        ArrayList<RulesNode> childNodes;
        //Parent node
        RulesNode parentNode;
        
        // Initialize tree root
        RulesNode(ArrayList<RuleFSMVector> dataRules) {
            this.dataRules = dataRules;
            parentNode = null;
            childNodes = new ArrayList<RuleManager.RulesNode>();
        }
        
        //TODO:
        // copy constructor
        RulesNode(RulesNode baseNode) {
            
        }
        
        void addNode(RulesNode branchNode) {
            
        }
        
        // conflict detection
        public boolean detectConflicts() {
            try {
                conflictSet = new HashMap<Integer, ConflictComparable<PredicateVector,RuleFSMVector>>();
            
                for (RuleConflictType conflictType: RuleConflictType.values()) {
                    for (int i = 0; i < dataRules.size(); i++) {
                        RuleFSMVector ruleA = dataRules.get(i);
                        for (int j = 0; j < dataRules.size(); j++) {
                            if (i == j)
                                continue;
                            
                            RuleFSMVector ruleB = dataRules.get(j);
                            
                            List<ConflictComparable<PredicateVector, RuleFSMVector>> conflictList =
                                    ruleA.compareRules(ruleB, conflictType);
                            
                            for (ConflictComparable<PredicateVector, RuleFSMVector> conflict: conflictList) {
                                if (conflictSet.containsKey(conflict.getId())) 
                                    continue;
                                
                                conflictSet.put(conflict.getId(), conflict);
                            }
                        }
                    }
                    
                    if (!conflictSet.isEmpty())
                        return true;
                }
                
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            
            return false;
        }

        // conflict resolution
    }
    
    RulesNode treeRoot;
    
    List<ConflictResolver<PredicateVector, RuleFSMVector>> conflictResolvers;
    
    public RuleManager(List<OutputDataChunk> outputData) {
        HashMap<Integer, ArrayList<OutputDataChunk>> outputStateData = new HashMap<Integer, ArrayList<OutputDataChunk>>();
        
        // Combine all predicates leading to the same output
        for (OutputDataChunk dataChunk: outputData) {
            Integer curEventId = dataChunk.outputEvent.eventId;
            Predicate eventPredicate = dataChunk.outputPredicate;
            
            if (DISGARD_INIT_STATE) {
                if (eventPredicate.getId() == 0) {
                    continue;
                }
            }
            
            ArrayList<OutputDataChunk> outputDataArray;
            
            if (outputStateData.containsKey(curEventId)) {
                outputDataArray = outputStateData.get(curEventId);
            } else {
                outputDataArray = new ArrayList<OutputDataChunk>();
            }
          
            outputDataArray.add(dataChunk);
            outputStateData.put(curEventId, outputDataArray);
        }
        
        // Initialize data rules array
        ArrayList<RuleFSMVector> dataRules = new ArrayList<RuleFSMVector>();
        
        for (Integer curEventId: outputStateData.keySet()) {
            ArrayList<OutputDataChunk> outputDataArray = outputStateData.get(curEventId);
            RuleFSMVector newRule = new RuleFSMVector(outputDataArray.get(0).outputEvent, outputDataArray);
            dataRules.add(newRule);
        }
        
        initConflictResolvers();
        
        // TODO: debug printing
        System.out.println();
        for (RuleFSMVector rule: dataRules)
            rule.print();
        
        treeRoot = new RulesNode(dataRules);
    }
    
    private void initConflictResolvers() {
        conflictResolvers = new ArrayList<ConflictResolver<PredicateVector,RuleFSMVector>>();
        
        ConflictResolver<PredicateVector,RuleFSMVector> resolveById = new ResolveById<RuleFSMVector>(10);
        conflictResolvers.add(resolveById);
        
        ConflictResolver<PredicateVector,RuleFSMVector> resolveByState = new ResolveByState<RuleFSMVector>(1);
        conflictResolvers.add(resolveByState);
        
        ConflictResolver<PredicateVector,RuleFSMVector> resolveByVector = new ResolveByVector<RuleFSMVector>(5);
        conflictResolvers.add(resolveByVector);
        
        ConflictResolver<PredicateVector,RuleFSMVector> resolveByAnalog = new ResolveByAnalog<RuleFSMVector>(2);
        conflictResolvers.add(resolveByAnalog);
    }
    
    public boolean analyzeData() {
        RulesNode curNode = treeRoot;
        //Deep copy of curNode
        //RulesNode newNode = new RulesNode(curNode)
        
        int safeLimit = 0;
        
        //Detect conflicts for current node
        while (curNode.detectConflicts() && safeLimit < 50) {
            System.out.println("Iteration: " + safeLimit++);
            //Determine how to resolve each conflict using one of the managers
            //After conflicts resolved add newNode to curNode children list
            for (Integer vectorId: curNode.conflictSet.keySet()) {
                final ConflictComparable<PredicateVector, RuleFSMVector> conflict = 
                        curNode.conflictSet.get(vectorId);
                
                Collections.sort(conflictResolvers, 
                        new Comparator<ConflictResolver<PredicateVector,RuleFSMVector>>() {
                            @Override
                            public int compare(ConflictResolver<PredicateVector, RuleFSMVector> cr1,
                                    ConflictResolver<PredicateVector, RuleFSMVector> cr2) {
                                Integer cr1Cost = cr1.ResolveCost(conflict);
                                Integer cr2Cost = cr2.ResolveCost(conflict);
                                
                                return cr1Cost.compareTo(cr2Cost);
                            } 
                });
                
                Integer resolveCost = -1;
                for (ConflictResolver<PredicateVector,RuleFSMVector> curResolver: conflictResolvers) {
                    if (curResolver.ResolveConflict(conflict, curNode.dataRules)) {
                        resolveCost = curResolver.ResolveCost(conflict);
                        break;
                    }
                }
                
                System.out.println(conflict.toString() + " Resolve cost: " + resolveCost);
            }
            
            for (RuleFSMVector rule: curNode.dataRules)
                rule.print();
        }
        
        if (safeLimit >= 50)
            System.out.println("Safe limit reached");
        
        return false;
    }

}

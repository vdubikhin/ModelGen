package modelFSM.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import modelFSM.data.Event;
import modelFSM.data.Predicate;
import modelFSM.data.RawDataChunk;
import modelFSM.data.RawDataPoint;
import modelFSM.rules.data.PredicateVector;
import modelFSM.rules.data.StateVector;
import modelFSM.shared.Util;

class ResolveByAnalog< V extends RuleComparable<PredicateVector, V> > implements ConflictResolver<PredicateVector, V> {
    
    int cost;
    HashMap<String, ArrayList<Double>> thresholdValues;
    
    public ResolveByAnalog(int cost) {
        this.cost = cost;
        thresholdValues = new HashMap<String, ArrayList<Double>> ();
    }

    //Simple version which allows only one threshold per output chunk
    @Override
    public boolean ResolveConflict(ConflictComparable<PredicateVector, V> conflictToResolve, List<V> ruleList) {
        try {
            int vectorId = conflictToResolve.getId();
            V ruleToFix = conflictToResolve.getRuleToFix();
            PredicateVector vectorToFix = ruleToFix.getRuleVectorById(vectorId);
            PredicateVector vectorFull = ruleToFix.getFullRuleVectorById(vectorId);
            HashMap<String, RawDataChunk> rawData = ruleToFix.getAnalogDataById(vectorId);
            
            //Check if all states have been used
            StateVector stateVector = vectorToFix.values().iterator().next();

            //Check if analog data has been used
            if (stateVector.keySet().containsAll(thresholdValues.keySet()) && !thresholdValues.isEmpty())
                return false;
            
            for (String signal: rawData.keySet()) {
                RawDataChunk curAnalogChunk = rawData.get(signal);
                ArrayList<Double> curThresholds;
                
                //Skip input data that is already contained in predicated vector
                if (stateVector.containsKey(signal))
                    continue;
                
                if (thresholdValues.containsKey(signal))
                    curThresholds = thresholdValues.get(signal);
                else
                    curThresholds = new ArrayList<>();
                
                //Detect value range for current data chunk
                Double chunkMin = null;
                Double chunkMax = null;
                
                for (RawDataPoint curPoint: curAnalogChunk) {
                    Double curValue = curPoint.value;
                    
                    if (chunkMin == null)
                        chunkMin = curValue;
                    
                    if (chunkMax == null)
                        chunkMax = curValue;
                    
                    if (curValue < chunkMin)
                        chunkMin = curValue;
                    
                    if (curValue > chunkMax)
                        chunkMax = curValue;
                }
                
                if (chunkMin == null || chunkMax == null) {
                    System.out.println("Something went wrong in analog resolver");
                    continue;
                }
                
                Double threshold = null;
                boolean thresholdFound = false;
                
                //Try to reuse existing thresholds first
                for (int i = 0; i < curThresholds.size(); i++) {
                    threshold = curThresholds.get(i);
                    if (threshold >= chunkMin && threshold <= chunkMax) {
                        thresholdFound = true;
                        break;
                    }
                }
                
                //Create new threshold
                if (!thresholdFound) {
                    threshold = (chunkMax - chunkMin)/2 + chunkMin;
                    curThresholds.add(threshold);
                    Collections.sort(curThresholds);
                    thresholdValues.put(signal, curThresholds);
                    
                    //TODO: debug printing
                    printThresholds();
                }
                
                Integer stateId = curThresholds.indexOf(threshold);
                
                
                addAnalogState(vectorToFix, curAnalogChunk, threshold, signal, stateId);
                addAnalogState(vectorFull, curAnalogChunk, threshold, signal, stateId);
                
                
                return true;
            }
            
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void printThresholds() {
        System.out.println("\n Printing Analog Thresholds");
        for (String signal: thresholdValues.keySet()) {
            ArrayList<Double> curThresholds = thresholdValues.get(signal);
            Collections.sort(curThresholds);
            System.out.println("Signal: " + signal + " Thresholds: " + curThresholds); 
        }
    }
    
    private void addAnalogState(PredicateVector vectorToAdd, RawDataChunk analogChunk,
                                Double threshold,  String signalName, Integer stateId) {
        //TODO: try catch + checks
        ArrayList<Double> timeArray = new ArrayList<Double> ();
        ArrayList<Integer> dataArrayGroup = new ArrayList<Integer> ();
        
        for (int i = 0; i < analogChunk.size(); i++) {
            RawDataPoint dataPoint = analogChunk.get(i);
            timeArray.add(dataPoint.time);
            if (dataPoint.value <= threshold)
                dataArrayGroup.add(stateId);
            else 
                dataArrayGroup.add(stateId + 1);
        }
        
        ArrayList<Event> discreteEvents = Util.dataToEvents(signalName, timeArray, dataArrayGroup);
        //Quit on empty analog events list
        if (discreteEvents.size() < 1)
            return;
        
        List<Integer> vectorToAddKeys = new ArrayList<Integer>(vectorToAdd.keySet().size());
        
        for (Integer key: vectorToAdd.keySet())
            vectorToAddKeys.add(key);
        
        Collections.sort(vectorToAddKeys);
        Collections.reverse(vectorToAddKeys);
        
        int eventsPointer = discreteEvents.size() - 1;
        
        for (Integer vectorPos: vectorToAddKeys) {
            StateVector curVector = vectorToAdd.get(vectorPos);
            Double vectorStartTime = curVector.getStartTime();
            
            Event curEvent = discreteEvents.get(eventsPointer);
            
            for (int i = eventsPointer; i >= 0; i--) {
                eventsPointer = i;
                curEvent = discreteEvents.get(eventsPointer);
                Double eventStartTime = curEvent.start;
                if (eventStartTime < vectorStartTime)
                    break;
            }
            curVector.put(signalName, curEvent);
        }
    }
    
    @Override
    public Integer ResolveCost(ConflictComparable<PredicateVector, V> conflictToResolve) {
        return cost;
    }
}

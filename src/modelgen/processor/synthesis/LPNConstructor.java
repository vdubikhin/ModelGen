package modelgen.processor.synthesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

import modelgen.data.ControlType;
import modelgen.data.DataType;
import modelgen.data.model.LPNModel;
import modelgen.data.model.LPNPlace;
import modelgen.data.model.LPNTransition;
import modelgen.data.pattern.PatternVector;
import modelgen.data.pattern.StateVector;
import modelgen.data.stage.StageDataRule;
import modelgen.data.state.IState;
import modelgen.data.state.IStateTimeless;
import modelgen.data.state.StateSymbolic;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
import modelgen.shared.Logger;

public class LPNConstructor {    
    protected String ERROR_PREFIX = "LPNConstructor error.";
    protected String DEBUG_PREFIX = "LPNConstructor debug.";
    protected int DEBUG_LEVEL = 1; //TODO: change to property class

    protected String RESET_PREFIX      = "R";
    protected String PLACE_PREFIX      = "P_";
    protected String TRANSITION_PREFIX = "T_";

    int transitionNumber;
    int placeNumber;

    public LPNConstructor() {
        transitionNumber = 0;
        placeNumber = 0;
    }

    int getTransitionNumber() {
        return transitionNumber++;
    }

    int getPlaceNumber() {
        return placeNumber++;
    }

    public LPNModel processData(StageDataRule signalRules) {
        try {
            if (signalRules == null) 
                return null;

            String signalName = signalRules.getSignalName();

            LPNModel model = new LPNModel();

            Map<PatternVector, List<LPNTransition>> modelTransitions = new HashMap<>();
            Map<PatternVector, List<LPNPlace>> modelPlaces = new HashMap<>();

            model.addInitialCondition(signalRules.getInitialState());
            model.addInitialCondition( convetToSymbolicState(signalRules.getInitialState()) );
            model.addInitialCondition(new StateSymbolic(RESET_PREFIX, 0));

            for (RuleComparable<PatternVector, RuleFSMVector> signalRule: signalRules.getSignalRules()) {
                //Create partial model
                for (PatternVector patternVector: signalRule.getRulePatterns()) {
                    List<LPNTransition> transitionList = new ArrayList<>();
                    List<LPNPlace> placeList = new ArrayList<>();

                    convertPatternVectorToModel(signalName, model, signalRule, patternVector,
                            transitionList, placeList);

                    //There should always be something
                    if (transitionList.isEmpty() || placeList.isEmpty())
                        return null;

                    addFinalResetLink(signalName, model, transitionList, placeList);

                    modelTransitions.put(patternVector, transitionList);
                    modelPlaces.put(patternVector, placeList);
                }
                model.setSignalType(signalRule.getOutputState().getSignalName(), ControlType.OUTPUT);
            }

            Map<Integer, List<LPNTransition>> patternVectorPreSet = new HashMap<>();
            Map<Integer, List<LPNPlace>> patternVectorPostSet = new HashMap<>();

            for (RuleComparable<PatternVector, RuleFSMVector> signalRule: signalRules.getSignalRules()) {
                //Process postset and preset of every pattern vector
                for (PatternVector patternVector: signalRule.getRulePatterns()) {
                    Set<Integer> postSet = patternVector.getPostSet();
                    Set<Integer> preSet = patternVector.getPreSet();

                    List<LPNTransition> transitionList = modelTransitions.get(patternVector);
                    List<LPNPlace> placeList = modelPlaces.get(patternVector);

                    addPostSetPlace(signalName, patternVectorPostSet, postSet, transitionList, placeList);

                    if(!preSet.isEmpty())
                        addPreSetTransition(signalName, patternVectorPreSet, patternVector, transitionList, placeList);

                    for (LPNPlace place: placeList)
                        model.addPlace(place);

                    for (LPNTransition transition: transitionList)
                        model.addTransition(transition);
                }
            }

            //Add necessary connections
            for (Integer connection: patternVectorPreSet.keySet()) {
                List<LPNTransition> transitionList = patternVectorPreSet.get(connection);

                if (!patternVectorPostSet.containsKey(connection)) {
                    Logger.errorLogger(ERROR_PREFIX + " Connection:" + signalName +"<" + connection + ">"
                            +" not found in post set");
                    return null;
                }

                List<LPNPlace> placeList = patternVectorPostSet.get(connection);

                addConnections(transitionList, placeList);
            }

            if (!loopModel(model, modelPlaces, signalName)) {
                Logger.errorLogger(ERROR_PREFIX + " Failed to loop model for signal: " + signalName);
                return null;
            }

            model.printDOT();
            System.out.println("--------------");
            model.printLPN();

            return model;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    protected boolean loopModel(LPNModel model, Map<PatternVector, List<LPNPlace>> patternPlaces, String signalName)
            throws NullPointerException {
        Map<LPNPlace, Set<LPNTransition>> placeConnections = new HashMap<>();

        for (PatternVector vector: patternPlaces.keySet()) {
            //Skip vector that have any dependency in preset
            if (!vector.getPreSet().isEmpty())
                continue;

            LPNPlace firstPlace = patternPlaces.get(vector).get(0);
            Set<LPNPlace> visitedPlaces = new HashSet<>();
            visitedPlaces.add(firstPlace);

            Set<LPNTransition> finalTransitions = new HashSet<>();
            Set<LPNTransition> placeTransitions = firstPlace.getPostSet();
            while (!placeTransitions.isEmpty()) {
                LPNTransition curTransition = placeTransitions.iterator().next();

                Set<LPNPlace> transitionPlaces = curTransition.getPostSet();
                for (LPNPlace curPlace: transitionPlaces) {
                    if (visitedPlaces.contains(curPlace))
                        continue;

                    placeTransitions.addAll(curPlace.getPostSet());
                }

                if (transitionPlaces.isEmpty())
                    finalTransitions.add(curTransition);

                placeTransitions.remove(curTransition);
            }
            
            if (finalTransitions.isEmpty())
                return false;

            placeConnections.put(firstPlace, finalTransitions);
        }

        for (LPNPlace place: placeConnections.keySet()) {
            place.setInitialized();

            //If only one transition no synchronization needed
            if (placeConnections.get(place).size() == 1) {
                LPNTransition transition = placeConnections.get(place).iterator().next();
                place.addTransitionPreSet(transition);
                transition.addPlacePostSet(place);
                continue;
            }

            //If multiple transitions are preset they need to be synchronized first
            LPNTransition finalTransition = new LPNTransition(TRANSITION_PREFIX + signalName +
                    getTransitionNumber());
            for (LPNTransition transition: placeConnections.get(place)) {
                LPNPlace syncPlace = new LPNPlace(PLACE_PREFIX + signalName + getPlaceNumber());
                syncPlace.addTransitionPreSet(transition);
                syncPlace.addTransitionPostSet(finalTransition);
                transition.addPlacePostSet(syncPlace);
            }

            place.addTransitionPreSet(finalTransition);
            finalTransition.addPlacePostSet(place);
        }

        return true;
    }

    protected void addConnections(List<LPNTransition> transitionList, List<LPNPlace> placeList)
            throws NullPointerException {
        for (LPNTransition curTransition: transitionList) {
            for (LPNPlace curPlace: placeList) {
                curPlace.addTransitionPostSet(curTransition);
                curTransition.addPlacePreSet(curPlace);
            }
        }
    }

    protected void addPreSetTransition(String signalName, Map<Integer, List<LPNTransition>> patternVectorPreSet,
            PatternVector patternVector, List<LPNTransition> transitionList, List<LPNPlace> placeList)
                    throws NullPointerException {
        LPNPlace firstPlace = placeList.get(0);
        LPNTransition preSetTransition = new LPNTransition(TRANSITION_PREFIX + signalName +
                getTransitionNumber());

        preSetTransition.addPlacePostSet(firstPlace);
        firstPlace.addTransitionPreSet(preSetTransition);

        transitionList.add(preSetTransition);

        List<LPNTransition> curPreSetList = patternVectorPreSet.get(patternVector.getId());

        if (curPreSetList == null)
            curPreSetList = new ArrayList<>();

        curPreSetList.add(preSetTransition);
        patternVectorPreSet.put(patternVector.getId(), curPreSetList);
    }

    protected void addPostSetPlace(String signalName, Map<Integer, List<LPNPlace>> patternVectorPostSet,
            Set<Integer> postSet, List<LPNTransition> transitionList, List<LPNPlace> placeList)
                    throws NullPointerException {
        LPNTransition lastTransition = transitionList.get(transitionList.size() - 1);
        for (Integer connection: postSet) {
            LPNPlace postSetPlace = new LPNPlace(PLACE_PREFIX + signalName + getPlaceNumber());

            lastTransition.addPlacePostSet(postSetPlace);
            postSetPlace.addTransitionPreSet(lastTransition);

            placeList.add(postSetPlace);

            List<LPNPlace> curPostSetList = patternVectorPostSet.get(connection);

            if (curPostSetList == null)
                curPostSetList = new ArrayList<>();

            curPostSetList.add(postSetPlace);
            patternVectorPostSet.put(connection, curPostSetList);
        }
    }

    protected void addFinalResetLink(String signalName, LPNModel model, List<LPNTransition> transitionList,
            List<LPNPlace> placeList) throws NullPointerException {
        LPNPlace resetPlace = new LPNPlace(PLACE_PREFIX + signalName + getPlaceNumber());
        LPNTransition resetTransition = new LPNTransition(TRANSITION_PREFIX + signalName +
                getTransitionNumber());

        LPNTransition lastTransition = transitionList.get(transitionList.size() - 1);

        resetTransition.addAssignmentConditions(new StateSymbolic(RESET_PREFIX, 0, true));

        lastTransition.addPlacePostSet(resetPlace);

        resetPlace.addTransitionPreSet(lastTransition);
        resetPlace.addTransitionPostSet(resetTransition);

        resetTransition.addPlacePreSet(resetPlace);

        transitionList.add(resetTransition);
        placeList.add(resetPlace);

        //TODO: for temp debug
        model.addTransition(resetTransition);
        model.addPlace(resetPlace);
    }

    protected LPNPlace convertPatternVectorToModel(String signalName, LPNModel model,
            RuleComparable<PatternVector, RuleFSMVector> signalRule, PatternVector patternVector,
            List<LPNTransition> transitionList, List<LPNPlace> placeList) throws NullPointerException {
        TreeMap<Integer, StateVector> sortedPatternVector = new TreeMap<>(patternVector);
        LPNTransition prevTransition = null;
        LPNPlace firstPlace = null;

        //TODO: needs analysis if this should be allowed
        //if no guard states are present
//        if (sortedPatternVector.isEmpty()) {
//            LPNTransition transition = new LPNTransition(TRANSITION_PREFIX + signalName +
//                    getTransitionNumber());
//            LPNPlace place = new LPNPlace(PLACE_PREFIX + signalName + getPlaceNumber());
//            
//            transition.addPlacePreSet(place);
//            place.addTransitionPostSet(transition);
//
//            addAssignmentConditions(outputState, transition);
//
//            transitionList.add(transition);
//            placeList.add(place);
//
//            //TODO: test
//            model.addPlace(place);
//            model.addTransition(transition);
//        }

        for (Integer key: sortedPatternVector.navigableKeySet()) {
            LPNTransition transition = new LPNTransition(TRANSITION_PREFIX + signalName +
                    getTransitionNumber());
            LPNPlace place = new LPNPlace(PLACE_PREFIX + signalName + getPlaceNumber());

            transition.addPlacePreSet(place);
            place.addTransitionPostSet(transition);

            if (prevTransition != null) {
                place.addTransitionPreSet(prevTransition);
                prevTransition.addPlacePostSet(place);
            }

            if (key.equals(sortedPatternVector.firstKey()))
                firstPlace = place;

            StateVector curVector = sortedPatternVector.get(key);

            for (String signal: curVector.keySet())
                model.setSignalType(signal, ControlType.INPUT);

            addGuardConditions(signalRule.getOutputState(), curVector, transition);

            //Add assignment condition for the last transition
            if (key.equals(sortedPatternVector.lastKey())) {
                addAssignmentConditions(signalRule.getOutputState(), transition);
                Map.Entry<Double, Double> delays = signalRule.getDelayById(patternVector.getId());
                transition.setDelayLow(delays.getKey());
                transition.setDelayHigh(delays.getValue());
            }

            //Add reset links to all intermediate places
            if (!key.equals(sortedPatternVector.firstKey())) {
                LPNTransition resetTransition = addResetLink(signalName, firstPlace, place);

                model.addTransition(resetTransition);
            }

            transitionList.add(transition);
            placeList.add(place);

            prevTransition = transition;

            //TODO: test
            model.addPlace(place);
            model.addTransition(transition);
        }

        return firstPlace;
    }

    protected LPNTransition addResetLink(String signalName, LPNPlace firstPlace, LPNPlace place) 
            throws NullPointerException {
        LPNTransition resetTransition = new LPNTransition(TRANSITION_PREFIX + signalName +
                                                          getTransitionNumber(), true, 10);

        resetTransition.addGuardConditions(new StateSymbolic(RESET_PREFIX, 1));

        resetTransition.addPlacePreSet(place);
        resetTransition.addPlacePostSet(firstPlace);

        firstPlace.addTransitionPreSet(resetTransition);
        return resetTransition;
    }

    protected void addAssignmentConditions(IStateTimeless outputState, LPNTransition transition)
            throws NullPointerException {
        //Main assignment
        transition.addAssignmentConditions(outputState);

        //Symbolic version of main assignment
        transition.addAssignmentConditions(new StateSymbolic(outputState.getSignalName(),
                outputState.getId(), true));

        //Reset state
        transition.addAssignmentConditions(new StateSymbolic(RESET_PREFIX, 1, true));
    }

    protected void addGuardConditions(IStateTimeless outputState, StateVector curVector,
            LPNTransition transition) throws NullPointerException {
        transition.addGuardConditions( addSymbolicStates(curVector.values(),
                                      Arrays.asList(outputState.getSignalName() )));

        //Add reset guard
        transition.addGuardConditions( new StateSymbolic(RESET_PREFIX, 1, true) );

        //Add livelock guard
        transition.addGuardConditions(new StateSymbolic(outputState.getSignalName(),
                outputState.getId(), true));
    }

    private IState convetToSymbolicState(IState state) throws NullPointerException {
        Entry<Double, Double> timeStamp = state.getTimeStamp();
        return new StateSymbolic(state.getSignalName(), state.getId(),
                timeStamp.getKey(), timeStamp.getValue());
    }

    private List<IState> addSymbolicStates(Collection<IState> collection, List<String> list) {
        List<IState> outputStates = new ArrayList<IState>();

        for (IState curState: collection) {
            IState stateToAdd = curState;
            for (String symbolicName: list) {
                if (curState.getSignalName().equals(symbolicName)) {
                    stateToAdd = convetToSymbolicState(curState);
                    break;
                }
            }
            outputStates.add(stateToAdd);
        }

        return outputStates;
    }
}

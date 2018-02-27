package modelgen.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import modelgen.processor.synthesis.LPNConstructor;
import modelgen.shared.Logger;

public class LPNSynthesisFSM {
    protected String ERROR_PREFIX = "LPNSynthesisFSM error.";
    protected String DEBUG_PREFIX = "LPNSynthesisFSM debug.";
    protected int DEBUG_LEVEL = 1; //TODO: change to property class

    public LPNSynthesisFSM() {
        // TODO Auto-generated constructor stub
    }
    
    public List<LPNModel> processData(List<StageDataRule> signalsRules) {
        try {
            List<LPNModel> models = new ArrayList<>();

            if (signalsRules == null) {
                Logger.debugPrintln("No rules to construct model", DEBUG_LEVEL);
                return null;
            }

            for (StageDataRule signalRules: signalsRules) {
                String signalName = signalRules.getSignalName();

                LPNModel model = new LPNModel();
                LPNConstructor modelCreator = new LPNConstructor();
                model = modelCreator.processData(signalRules);

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }

    public List<LPNModel> processDataTest(List<StageDataRule> signalsRules) {
        try {
            List<LPNModel> models = new ArrayList<>();

            if (signalsRules == null) {
                Logger.debugPrintln("No rules to construct model", DEBUG_LEVEL);
                return null;
            }

            for (StageDataRule signalRules: signalsRules) {
                String signalName = signalRules.getSignalName();

                int transitionNumber = 0;
                int placeNumber = 0;

                LPNModel model = new LPNModel();

                for (RuleComparable<PatternVector, RuleFSMVector> signalRule: signalRules.getSignalRules()) {
                    //Perform rule minimization
                    //Detects similar rule patterns within rule and removes duplicates.
                    //Possibly, adds timing information.
                    //Possibly transforms unique patterns into patterns with explicit state
                    //TODO: signalRule.minimizeRules();

                    for (PatternVector patternVector: signalRule.getRulePatterns()) {
                        TreeMap<Integer, StateVector> sortedPatternVector = new TreeMap<>(patternVector);

                        LPNTransition prevTransition = null;
                        LPNPlace firstPlace = null;
                        for (Integer key: sortedPatternVector.navigableKeySet()) {
                            LPNTransition transition = new LPNTransition("T_" + signalName + transitionNumber++);
                            LPNPlace place = new LPNPlace("P_" + signalName + placeNumber++);

                            transition.addPlacePreSet(place);
                            place.addTransitionPostSet(transition);

                            if (prevTransition != null) {
                                place.addTransitionPreSet(prevTransition);
                                prevTransition.addPlacePostSet(place);
                            }

                            if (key.equals(sortedPatternVector.firstKey())) {
                                firstPlace = place;
                                firstPlace.setInit();
                            }

                            StateVector curVector = sortedPatternVector.get(key);

                            transition.addGuardConditions( addSymbolicStates(curVector.values(),
                                                          Arrays.asList(signalRule.getOutputState().getSignalName() )));

                            //Add reset guard
                            transition.addGuardConditions( new StateSymbolic("R", 1, 0.0, 0.0, true) );

                            //Add livelock guard
                            IStateTimeless outputState = signalRule.getOutputState();
                            transition.addGuardConditions(new StateSymbolic(outputState.getSignalName(),
                                    outputState.getId(), 0.0, 0.0, true));

                            if (key.equals(sortedPatternVector.lastKey())) {
                                transition.addAssignmentConditions(outputState);
                                transition.addAssignmentConditions(new StateSymbolic(outputState.getSignalName(),
                                                                   outputState.getId(), 0.0, 0.0, true));
                                transition.addAssignmentConditions(new StateSymbolic("R", 1, 0.0, 0.0, true));

                                LPNPlace resetPlace = new LPNPlace("P_" + signalName + placeNumber++);
                                LPNTransition resetTransition = new LPNTransition("T_" + signalName +
                                                                                  transitionNumber++);

                                resetTransition.addAssignmentConditions(new StateSymbolic("R", 0, 0.0, 0.0, true));

                                transition.addPlacePostSet(resetPlace);

                                resetPlace.addTransitionPreSet(transition);
                                resetPlace.addTransitionPostSet(resetTransition);

                                resetTransition.addPlacePreSet(resetPlace);
                                resetTransition.addPlacePostSet(firstPlace);

                                firstPlace.addTransitionPreSet(resetTransition);

                                model.addTransition(resetTransition);
                                model.addPlace(resetPlace);
                            }

                            //Add reset transition
                            //TODO: indicate that enabling is persistent
                            if (!key.equals(sortedPatternVector.firstKey())) {
                                LPNTransition resetTransition = new LPNTransition("T_" + signalName +
                                                                                  transitionNumber++);

                                resetTransition.addGuardConditions(new StateSymbolic("R", 1, 0.0, 0.0, false));

                                resetTransition.addPlacePreSet(place);
                                resetTransition.addPlacePostSet(firstPlace);

                                firstPlace.addTransitionPreSet(resetTransition);

                                model.addTransition(resetTransition);
                            }

                            model.addTransition(transition);
                            model.addPlace(place);

                            prevTransition = transition;
                        }
                    }
                }

                model.print();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Array out of bounds exception.", e);
        } catch (NullPointerException e) {
            Logger.errorLoggerTrace(ERROR_PREFIX + " Null pointer exception.", e);
        }
        return null;
    }
    
    private List<IState> addSymbolicStates(Collection<IState> collection, List<String> list) {
        List<IState> outputStates = new ArrayList<IState>();

        for (IState curState: collection) {
            IState stateToAdd = curState;
            for (String symbolicName: list) {
                if (curState.getSignalName().equals(symbolicName)) {
                    Entry<Double, Double> timeStamp = stateToAdd.getTimeStamp();
                    stateToAdd = new StateSymbolic(symbolicName, stateToAdd.getId(),
                            timeStamp.getKey(), timeStamp.getValue());
                    break;
                }
            }
            outputStates.add(stateToAdd);
        }

        return outputStates;
    }
}

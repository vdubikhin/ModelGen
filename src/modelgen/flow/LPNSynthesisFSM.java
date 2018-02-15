package modelgen.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import modelgen.data.model.LPNModel;
import modelgen.data.model.LPNPlace;
import modelgen.data.model.LPNTransition;
import modelgen.data.pattern.PatternVector;
import modelgen.data.pattern.StateVector;
import modelgen.data.stage.StageDataRule;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;
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

                            transition.addGuardConditions(curVector.values());

                            if (key.equals(sortedPatternVector.lastKey())) {
                                transition.addAssignmentConditions(signalRule.getOutputState());
                                transition.addPlacePostSet(firstPlace);
                                firstPlace.addTransitionPreSet(transition);
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
}

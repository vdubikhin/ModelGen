package modelgen.data.model;

import java.util.HashSet;
import java.util.Set;

import modelgen.data.state.IState;

public class LPNModel {
    Set<LPNPlace> places;
    Set<LPNTransition> transitions;
    Set<IState> initialConditions;

    public LPNModel() {
        places = new HashSet<>();
        transitions = new HashSet<>();
        initialConditions = new HashSet<>();
    }

    public void addTransition(LPNTransition transition) {
        transitions.add(transition);
    }

    public void addPlace(LPNPlace place) {
        places.add(place);
    }

    public void addInitialCondition(IState state) {
        initialConditions.add(state);
    }

    public Set<LPNPlace> getPlaces() {
        return places;
    }

    public Set<LPNTransition> getTransitions() {
        return transitions;
    }

    public void print() {
        System.out.println("digraph G {");

        String inits = "Inits [shape=plaintext,label=" + '"';
        for (IState state: initialConditions) {
            inits += state.convertToAssignmentCondition() + "\\n";
        }

        inits += '"' +"]";

        System.out.println(inits);

        for (LPNTransition transition: transitions) {
            System.out.println(transition.getLabel());
        }

        for (LPNPlace place: places) {
            System.out.println(place.getLabel());
        }

        for (LPNTransition transition: transitions) {
            for (LPNPlace place: transition.getPreSet()) {
                System.out.println(place.getName() + " -> " + transition.getName());
            }

            for (LPNPlace place: transition.getPostSet()) {
                System.out.println(transition.getName() + " -> " + place.getName());
            }
        }

        System.out.println("}");
    }

}

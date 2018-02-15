package modelgen.data.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelgen.data.state.IState;

public class LPNModel {
    Set<LPNPlace> places;
    Set<LPNTransition> transitions;
    List<IState> initialConditions;

    public LPNModel() {
        places = new HashSet<>();
        transitions = new HashSet<>();
    }

    public void addTransition(LPNTransition transition) {
        transitions.add(transition);
    }

    public void addPlace(LPNPlace place) {
        places.add(place);
    }

    public void print() {
        System.out.println("digraph G {");

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

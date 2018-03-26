package modelgen.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import modelgen.data.ControlType;
import modelgen.data.DataType;
import modelgen.data.state.IState;

public class LPNModel {
    Set<LPNPlace> places;
    Set<LPNTransition> transitions;
    Set<IState> initialConditions;
    Map<String, ControlType> signalType;

    public LPNModel() {
        places = new HashSet<>();
        transitions = new HashSet<>();
        initialConditions = new HashSet<>();
        signalType = new HashMap<>();
    }

    public void setSignalType(String signal, ControlType type) {
        if (signal == null || type == null)
            return;

        if (signalType.containsKey(signal)) {
            if (signalType.get(type) != ControlType.OUTPUT)
                signalType.put(signal, type);
        } else {
            signalType.put(signal, type);
        }
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

    class Line extends ArrayList<String>{
        String prefix;
        String delim;
        String bracketLeft;
        String bracketRight;

        Line(String prefix, String delim, String bracketLeft, String bracketRight) {
            super();
            this.prefix = prefix;
            this.delim = delim;
            this.bracketLeft = bracketLeft;
            this.bracketRight = bracketRight;
        }

        @Override
        public boolean add(String item) {
            if (item == null)
                return false;

            if (item.equals(""))
                return false;

            return super.add(item);
        }

        String convertToLine() {
            if (isEmpty())
                return null;

            String output = prefix + " " + bracketLeft;

            int id = 0;
            for (String item: this) {
                //last element - dont add delim
                if (id == size() - 1)
                    output += item;
                else
                    output += item + delim;
                id++;
            }

            output += bracketRight;
            return output;
        }
    }

    public void printLPN() {
        Map<String, Line> lines = new LinkedHashMap<>();

        lines.put("inputs",               new Line(".inputs", " ", "", ""));
        lines.put("outputs",              new Line(".outputs", " ", "", ""));
        lines.put("internal",             new Line(".internal", " ", "", ""));
        lines.put("dummy",                new Line(".dummy", " ", "", ""));
        lines.put("variables",            new Line("#@.variables", " ", "", ""));
        lines.put("non_disabling",        new Line("#@.non_disabling", " ", "", ""));
        lines.put("places",               new Line("#|.places", " ", "", ""));
        lines.put("graph",                new Line(".graph", System.lineSeparator(), System.lineSeparator(), ""));
        lines.put("marking",              new Line(".marking", " ", "{", "}"));
        lines.put("init_vals",            new Line("#@.init_vals", " ", "{", "}"));
        lines.put("init_rates",           new Line("#@.init_rates", " ", "{", "}"));
        lines.put("enablings",            new Line("#@.enablings", "", "{", "}"));
        lines.put("assignments",          new Line("#@.assignments", "", "{", "}"));
        lines.put("rate_assignments",     new Line("#@.rate_assignments", "", "{", "}"));
        lines.put("delay_assignments",    new Line("#@.delay_assignments", "", "{", "}"));
        lines.put("priority_assignments", new Line("#@.priority_assignments", "", "{", "}"));
        lines.put("continuous",           new Line("#@.continuous", " ", "", ""));

        for (IState state: initialConditions) {
            String name = state.getSignalName();
            lines.get("variables").add(name);
            if (signalType.containsKey(name)) {
                if (signalType.get(name) == ControlType.OUTPUT)
                    lines.get("outputs").add(name);

                if (signalType.get(name) == ControlType.INPUT)
                    lines.get("inputs").add(name);
            } else {
                lines.get("internal").add(name);
            }

            if (state.getType() == DataType.CONTINOUS_RANGE)
                lines.get("continuous").add(name);

            lines.get("init_vals").add("<" + state.convertToInitialCondition() + ">");
            String initRate = state.convertToInitialRateCondition();
            if (initRate != null)
                lines.get("init_rates").add("<" + state.convertToInitialRateCondition() + ">");
        }

        //HACK: handling of input variables that are not part of initial condition
        for (String name: signalType.keySet()) {
            if (signalType.get(name) == ControlType.INPUT) {
                lines.get("inputs").add(name);
                lines.get("init_vals").add("<" + name + "=0" + ">");
                lines.get("variables").add(name);
            }
        }

        //Process transitions
        for (LPNTransition transition: transitions) {
            lines.get("enablings").add(transition.getEnablingLabel());
            lines.get("assignments").add(transition.getAssingmentLabel());
            lines.get("rate_assignments").add(transition.getRateAssingmentLabel());
            lines.get("dummy").add(transition.getName());
            lines.get("delay_assignments").add(transition.getDelayLabel());
            lines.get("priority_assignments").add(transition.getPriorityLabel());

            if (transition.persistent)
                lines.get("non_disabling").add(transition.getName());

            for (LPNPlace place: transition.getPreSet()) {
                lines.get("graph").add(place.getName() + " " + transition.getName());
            }

            for (LPNPlace place: transition.getPostSet()) {
                lines.get("graph").add(transition.getName() + " " + place.getName());
            }
        }

        for (LPNPlace place: places) {
            lines.get("places").add(place.getName());
            if (place.isInitialized())
                lines.get("marking").add(place.getName());
        }

        for (Line line: lines.values()) {
            String output = line.convertToLine();
            if (output != null && !output.equals(""))
                System.out.println(output);
        }
        System.out.println(".end");
    }

    public void printDOT() {
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

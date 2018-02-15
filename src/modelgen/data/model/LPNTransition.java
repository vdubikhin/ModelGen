package modelgen.data.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelgen.data.state.IState;
import modelgen.data.state.IStateTimeless;

public class LPNTransition {
    String transitionName;
    List<IState> guardConditions;
    List<IStateTimeless> assignmentConditions;

    Set<LPNPlace> preSet;
    Set<LPNPlace> postSet;

    public LPNTransition(String name) {
        transitionName = name;
        guardConditions = new ArrayList<>();
        assignmentConditions = new ArrayList<>();
        preSet = new HashSet<>();
        postSet = new HashSet<>();
    }

    public void addGuardConditions(Collection<IState> conditionsToAdd) {
        if (conditionsToAdd != null)
            guardConditions.addAll(conditionsToAdd);
    }

    public void addGuardConditions(IState conditionsToAdd) {
        if (conditionsToAdd != null)
            guardConditions.add(conditionsToAdd);
    }

    public void addAssignmentConditions(Collection<IStateTimeless> conditionsToAdd) {
        if (conditionsToAdd != null)
            assignmentConditions.addAll(conditionsToAdd);
    }

    public void addAssignmentConditions(IStateTimeless conditionsToAdd) {
        if (conditionsToAdd != null)
            assignmentConditions.add(conditionsToAdd);
    }

    public void addPlacePreSet(LPNPlace place) {
        preSet.add(place);
    }

    public void addPlacePostSet(LPNPlace place) {
        postSet.add(place);
    }

    public Set<LPNPlace> getPreSet() {
        return preSet;
    }

    public Set<LPNPlace> getPostSet() {
        return postSet;
    }

    public String getName() {
        return transitionName;
    }

    public String getLabel() {
        String output = null;
        if (transitionName != null || guardConditions != null) {
            output = transitionName + "  [shape=plaintext,label=" + '"' + transitionName + "\\n{";
        }

        boolean prepend = false;
        for (IState guard: guardConditions) {
            if (prepend)
                output += " && ";

            output += guard.convertToGuardCondition();
            prepend = true;
        }

        output += "}";

        if (assignmentConditions != null && !assignmentConditions.isEmpty()) {
            prepend = false;
            output += "\\n<";
            for (IStateTimeless guard: assignmentConditions) {
                if (prepend)
                    output += ", ";

                output += guard.convertToAssignmentCondition();
                prepend = true;
            }
            output += ">";
        }

        output += '"' + "];";

        return output;
    }
}

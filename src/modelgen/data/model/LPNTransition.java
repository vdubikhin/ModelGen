package modelgen.data.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelgen.data.DataType;
import modelgen.data.state.IState;
import modelgen.data.state.IStateTimeless;

public class LPNTransition {
    String transitionName;
    Integer delayLow, delayHigh;

    boolean persistent;
    int priority;

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
        delayLow = 0;
        delayHigh = 0;
        persistent = false;
        priority = 1;
    }

    public LPNTransition(String name, boolean persistent, int priority) {
        this(name);
        this.persistent = persistent;
        this.priority = priority;
    }
    
    public boolean isPersistent() {
        return persistent;
    }
    
    public void setDelayLow(Integer delay) {
        delayLow = delay;
    }

    public void setDelayHigh(Integer delay) {
        delayHigh = delay;
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

    public String getDelayLabel() {
        String output = null;
        if (delayLow >= 0 && delayHigh >= 0) {
            if (delayLow.equals(delayHigh))
                output = "<" + getName() + "=[" + delayLow +  "]>";
            else
                output = "<" + getName() + "=[uniform(" + delayLow + ","+ delayHigh + ")]>";
        }
        return output;
    }

    public String getPriorityLabel() {
        String output = null;
        if (priority > 1) 
            output = "<" + getName() + "=["+ priority +"]>";
        return output;
    }

    public String getEnablingLabel() {
        String output = "";
        
        output += "<" + getName() + "=[";

        if (guardConditions.isEmpty())
            output += "true";
        else {
            boolean prepend = false;
            for (IState guard: guardConditions) {
                if (prepend)
                    output += "&";

                output += guard.convertToGuardCondition();
                prepend = true;
            }
        }
        output += "]>";

        return output;
    }

    public String getAssingmentLabel() {
        String output = "";
        if (assignmentConditions != null && !assignmentConditions.isEmpty()) {
            for (IStateTimeless guard: assignmentConditions) {
                if (guard.getType() != DataType.CONTINOUS_RANGE) {
                    output += "<" + getName() + "=[";
                    output += guard.convertToAssignmentCondition();
                    output += "]>";
                }
            }
        } else 
            return null;

        return output;
    }

    public String getRateAssingmentLabel() {
        String output = "";
        if (assignmentConditions != null && !assignmentConditions.isEmpty()) {
            for (IStateTimeless guard: assignmentConditions) {
                if (guard.getType() == DataType.CONTINOUS_RANGE) {
                    output += "<" + getName() + "=[";
                    output += guard.convertToAssignmentCondition();
                    output += "]>";
                }
            }
        } else 
            return null;

        return output;
    }

    public String getLabel() {
        String output = null;
        if (transitionName != null || guardConditions != null) {
            String fontColor = "";
            if (persistent)
                fontColor = ",fontcolor=Blue";
            output = transitionName + "  [shape=plaintext" + fontColor + ",label=" + '"' + transitionName + "\\n{";
        }

        boolean prepend = false;
        for (IState guard: guardConditions) {
            if (prepend)
                output += " && ";

            output += guard.convertToGuardCondition();
            prepend = true;
        }

        if (guardConditions.isEmpty())
            output += "true";

        output += "}";

        if (delayLow > 0.0 && delayHigh > 0.0) {
            output += "\\n[" + delayLow + ", " + delayHigh + "]";
        }

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

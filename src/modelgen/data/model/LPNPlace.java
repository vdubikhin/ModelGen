package modelgen.data.model;

import java.util.HashSet;
import java.util.Set;

public class LPNPlace {
    boolean initialized;
    String placeName;

    Set<LPNTransition> preSet;
    Set<LPNTransition> postSet;

    public LPNPlace(String name) {
        placeName = name;
        preSet = new HashSet<>();
        postSet = new HashSet<>();
    }

    public void addTransitionPreSet(LPNTransition transition) {
        preSet.add(transition);
    }

    public void addTransitionPostSet(LPNTransition transition) {
        postSet.add(transition);
    }

    public Set<LPNTransition> getPreSet() {
        return preSet;
    }

    public Set<LPNTransition> getPostSet() {
        return postSet;
    }

    public void setInitialized() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
    
    public String getName() {
        return placeName;
    }

    public String getLabel() {
        String output = null;
        output = placeName + "  [label=" + placeName + "];\n";
        output += placeName + "  [shape=circle,width=0.40,height=0.40]";

        if (initialized)
            output += "\n" + placeName +
                "  [height=.3,width=.3,peripheries=2,style=filled,color=black,fontcolor=white];";

        return output;
    }
}

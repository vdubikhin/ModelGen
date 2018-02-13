package modelgen.processor.rulemining;

import java.util.HashMap;
import java.util.Map;

import modelgen.data.pattern.DataPatterns;
import modelgen.data.state.IState;

public class SignalDataPatterns {
    Map<String, DataPatterns> signalPatterns;
    Map<String, IState> initialStates;

    public SignalDataPatterns(Map<String,DataPatterns> patterns, HashMap<String, IState> states) {
        signalPatterns = patterns;
        initialStates = states;
    }

    public Map<String, IState> getInitialStates() {
        return initialStates;
    }

    public Map<String,DataPatterns> getSignalPatterns() {
        return signalPatterns;
    }
}

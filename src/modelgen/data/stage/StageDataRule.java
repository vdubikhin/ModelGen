package modelgen.data.stage;

import java.util.List;

import modelgen.data.pattern.PatternVector;
import modelgen.data.state.IState;
import modelgen.processor.rulemining.conflictdetection.RuleComparable;
import modelgen.processor.rulemining.conflictdetection.RuleFSMVector;

public class StageDataRule {
    List<? extends RuleComparable<PatternVector, RuleFSMVector>> signalRules;
    IState initialState;

    public StageDataRule(List<? extends RuleComparable<PatternVector, RuleFSMVector>> rules, 
            IState state) {
        signalRules = rules;
        initialState = state;
    }

    public IState getInitialState() {
        return initialState;
    }

    public List<? extends RuleComparable<PatternVector, RuleFSMVector>> getSignalRules() {
        return signalRules;
    }

    public String getSignalName() {
        return initialState.getSignalName();
    }
}

package modelgen.data.pattern;

import java.util.HashMap;

import modelgen.data.raw.RawDataChunk;
import modelgen.data.state.IState;

public class DataPattern {
    private IState outputState;
    private PatternVector ruleVector;
    private PatternVector fullRuleVector;
    private HashMap<String, RawDataChunk> inputRawData;
    
    public DataPattern(IState state, PatternVector fullVector, HashMap<String, RawDataChunk> rawData) {
        setOutputState(state);
        setRuleVector(new PatternVector(fullVector.getId()));
        setFullRuleVector(fullVector);
        setInputRawData(rawData);
    }
    
    public DataPattern(DataPattern copy) {
        setRuleVector(new PatternVector(copy.getRuleVector()));
        setFullRuleVector(new PatternVector(copy.getFullRuleVector()));
        setInputRawData(copy.getInputRawData());
    }

    public PatternVector getRuleVector() {
        return ruleVector;
    }

    public void setRuleVector(PatternVector ruleVector) {
        this.ruleVector = ruleVector;
    }

    public PatternVector getFullRuleVector() {
        return fullRuleVector;
    }

    public void setFullRuleVector(PatternVector fullRuleVector) {
        this.fullRuleVector = fullRuleVector;
    }

    public IState getOutputState() {
        return outputState;
    }

    public void setOutputState(IState outputState) {
        this.outputState = outputState;
    }

    public HashMap<String, RawDataChunk> getInputRawData() {
        return inputRawData;
    }

    public void setInputRawData(HashMap<String, RawDataChunk> inputRawData) {
        this.inputRawData = inputRawData;
    }
}

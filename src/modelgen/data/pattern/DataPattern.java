package modelgen.data.pattern;

import java.util.HashMap;

import modelgen.data.complex.Mergeable;
import modelgen.data.pattern.DataComparable.DataEquality;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.state.IState;

public class DataPattern implements Mergeable<DataPattern>{
    private IState outputState;
    private PatternVector ruleVector;
    private PatternVector fullRuleVector;
    private HashMap<String, RawDataChunk> inputRawData;
    private Double mergeDelayLow, mergeDelayHigh;

    public DataPattern(IState state, PatternVector fullVector, HashMap<String, RawDataChunk> rawData) {
        setOutputState(state);
        setRuleVector(new PatternVector(fullVector.getId()));
        setFullRuleVector(fullVector);
        setInputRawData(rawData);
        mergeDelayLow = null;
        mergeDelayHigh = null;
    }

    public DataPattern(DataPattern copy) {
        copyInit(copy);
    }

    protected void copyInit(DataPattern copy) {
        setRuleVector(new PatternVector(copy.getRuleVector()));
        setFullRuleVector(new PatternVector(copy.getFullRuleVector()));
        setInputRawData(copy.getInputRawData());
        outputState = copy.outputState;
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

    private Double calculateRuleVectorDelay() {
        if (ruleVector == null)
            return null;

        Double vectorEndTime = ruleVector.getEndTime();
        Double outputStateTime = outputState.getTimeStamp().getKey();

        if (vectorEndTime == null)
            return null;

        Double output = outputStateTime - vectorEndTime;

        if (output < 0)
            return 0.0;

        return output;
    }

    public Double getDelayLow() {
        Double patternDelay = calculateRuleVectorDelay();

        if (mergeDelayLow == null)
            return patternDelay;

        if (patternDelay == null)
            return null;

        return Math.min(mergeDelayLow, patternDelay);
    }
    
    public Double getDelayHigh() {
        Double patternDelay = calculateRuleVectorDelay();

        if (mergeDelayHigh == null)
            return patternDelay;

        if (patternDelay == null)
            return null;

        return Math.max(mergeDelayHigh, patternDelay);
    }

    @Override
    public boolean canMergeWith(DataPattern itemToMerge) {
        DataEquality cmpResult = this.ruleVector.compareTo(itemToMerge.ruleVector);
        if (cmpResult == DataEquality.UNIQUE)
            return false;

        //TODO: think on how to merge rules with non-empty preset/postsets
//        if (!this.ruleVector.getPostSet().isEmpty() || !this.ruleVector.getPreSet().isEmpty())
//            return false;
//
//        if (!itemToMerge.ruleVector.getPostSet().isEmpty() || !itemToMerge.ruleVector.getPreSet().isEmpty())
//            return false;

        //Simple merge for elements with equals post and pre sets
        if (!this.ruleVector.getPostSet().equals( itemToMerge.ruleVector.getPostSet() ) || 
                !this.ruleVector.getPreSet().equals( itemToMerge.ruleVector.getPreSet()) )
            return false;

        return true;
    }

    @Override
    public boolean mergeWith(DataPattern itemToMerge) {
        if (!canMergeWith(itemToMerge))
            return false;

        Double itemToMergeDelayLow = itemToMerge.getDelayLow();
        Double itemToMergeDelayHigh = itemToMerge.getDelayHigh();

        Double patternDelay = calculateRuleVectorDelay();

        if (patternDelay == null || itemToMergeDelayLow == null || itemToMergeDelayHigh == null)
            return false;

        //Copy fields of the superset DataPattern
        if (this.ruleVector.compareTo(itemToMerge.ruleVector) == DataEquality.SUBSET)
            copyInit(itemToMerge);

        mergeDelayLow = Math.min(itemToMergeDelayLow, patternDelay);
        mergeDelayHigh = Math.max(itemToMergeDelayHigh, patternDelay);

        return true;
    }
}

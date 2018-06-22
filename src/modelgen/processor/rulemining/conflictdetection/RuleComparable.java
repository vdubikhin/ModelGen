package modelgen.processor.rulemining.conflictdetection;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import modelgen.data.complex.ComplexComparable;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.stage.IStageData;
import modelgen.data.state.IStateTimeless;

public interface RuleComparable< T extends ComplexComparable<T>, V extends RuleComparable<T, V> >
    extends ERuleComparable, IStageData{

    void print();

    List<ConflictComparable<T, V>> compareRules(V ruleToCmp, RuleConflictType conflictType);

    T getRuleVectorById(Integer id);

    List<T> getRulePatterns();

    T getFullRuleVectorById(Integer id);

    //TODO: create a container to store chunks of analog data
    //Use hashmap for now
    Map<String, RawDataChunk> getAnalogDataById(Integer id);

    IStateTimeless getOutputState();

    void setFullRuleVectorById(Integer id, T vector);

    void resetRuleVectorById(Integer id);

    void minimizeRules();

    Entry<Integer, Integer> getDelayById(Integer id);

    Integer getScaleFactor();

    void setScaleFactor(Double scale);
}

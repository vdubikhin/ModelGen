package modelgen.flow;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import modelgen.data.property.Properties;
import modelgen.data.property.PropertySettable;

public interface IStage<I, O> extends PropertySettable {

    boolean setProcessorProperties(Map<String, Properties> properties);

    boolean setManagerProperties(Properties properties);

    Map<String, Properties> getProcessorProperties();

    Properties getManagerProperties();

    List<Entry<O, Integer>> processData(List<I> inputData);
}
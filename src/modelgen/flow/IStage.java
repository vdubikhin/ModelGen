package modelgen.flow;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import modelgen.data.property.Properties;

interface IStage<I, O> {

    boolean setStageProperties(Properties properties);

    boolean setManagerProperties(Properties properties);

    boolean setProcessorProperties(Map<String, Properties> properties);

    Properties getStageProperties();

    Properties getManagerProperties();

    Map<String, Properties> getProcessorProperties();

    List<Entry<O, Integer>> processData(List<I> inputData);
}
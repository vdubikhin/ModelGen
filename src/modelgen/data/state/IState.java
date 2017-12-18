package modelgen.data.state;

import java.util.Map;

import modelgen.data.DataType;
import modelgen.data.complex.Mergeable;

public interface IState extends Mergeable<IState>{
    String convertToString();

    String getSignalName();

    Integer getId();

    DataType getType();

    //Key-start, Value-end
    Map.Entry<Double, Double> getTimeStamp();

    default Double getDuration() {
        Map.Entry<Double, Double> stamp = getTimeStamp();
        if (stamp != null)
            return stamp.getValue() - stamp.getKey();
        return null;
    }
}

package modelgen.data.state;

import java.util.List;
import java.util.Map;

import modelgen.data.DataType;
import modelgen.data.complex.Cloneable;
import modelgen.data.complex.Mergeable;
import modelgen.data.complex.Printable;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;

public interface IState extends Mergeable<IState>, Printable, Cloneable<IState> {

    String getSignalName();

    Integer getId();

    DataType getType();

    boolean increaseDuration(IState stateToUse);

    default boolean increaseDuration(List<IState> statesToUse) {
        boolean success = true;

        for (int i = 0; i < statesToUse.size(); i++) {
            if ( !(success = this.increaseDuration(statesToUse.get(i))) )
                break;
        }

        if (!success) {
            for (int i = statesToUse.size() - 1; i >= 0; i--) {
                if ( !(success = this.increaseDuration(statesToUse.get(i))) )
                    break;
            }
        }

        return success;
    }

    //Key-start, Value-end
    Map.Entry<Double, Double> getTimeStamp();

    default Double getDuration() {
        Map.Entry<Double, Double> stamp = getTimeStamp();
        if (stamp != null)
            return stamp.getValue() - stamp.getKey();
        return null;
    }

    RawDataChunk generateSignal(RawDataChunk baseSignal);
}

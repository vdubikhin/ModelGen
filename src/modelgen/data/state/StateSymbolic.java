package modelgen.data.state;

import modelgen.data.DataType;
import modelgen.data.raw.RawDataChunk;
import modelgen.data.raw.RawDataPoint;
import modelgen.shared.Util;

public class StateSymbolic extends State implements IState {
    static final String POSTFIX = "_STATE";
    boolean guardNegation;

    public StateSymbolic(String name, Integer id) {
        super(name, id, -1.0, -1.0);
        guardNegation = false;
    }

    public StateSymbolic(String name, Integer id, boolean guard) {
        this(name, id);
        guardNegation = guard;
    }

    public StateSymbolic(String name, Integer id, Double start, Double end) {
        super(name, id, start, end);
        guardNegation = false;
    }

    public StateSymbolic(String name, Integer id, Double start, Double end, boolean guard) {
        super(name, id, start, end);
        guardNegation = guard;
    }

    public StateSymbolic (StateSymbolic toCopy) {
        this(toCopy.signalName, toCopy.stateId, toCopy.start, toCopy.end, toCopy.guardNegation);
    }

    @Override
    public String getSignalName() {
        return signalName + POSTFIX;
    }

    @Override
    public String convertToString() {
        return "[" + stateId + "]";
    }

    @Override
    public IState makeCopy() {
        return new StateSymbolic(this);
    }

    @Override
    public String convertToGuardCondition() {
        String output = "";
        if (guardNegation)
            output = "~";

        output += "(" +  signalName + POSTFIX + "=" + stateId + ")";

        return output;
    }

    @Override
    public String convertToAssignmentCondition() {
        String output = signalName + POSTFIX + ":=" + stateId;
        return output;
    }

    @Override
    public DataType getType() {
        return DataType.SYMBOLIC;
    }

    @Override
    public RawDataChunk generateSignal(RawDataChunk baseSignal) {
        RawDataChunk outputData = new RawDataChunk();

        for (RawDataPoint curPoint: baseSignal) {
            if (start <= curPoint.getTime() && curPoint.getTime() <= end) {
                RawDataPoint newPoint = new RawDataPoint(new Double(stateId), curPoint.getTime());
                outputData.add(newPoint);
            }
        }
        return outputData;
    }

    @Override
    public String convertToInitialCondition() {
        String output = signalName + POSTFIX + "=" + stateId;
        return output;
    }

    @Override
    public Integer getScalePower() {
        return  Util.base10Power(new Double(getId()));
    }
}

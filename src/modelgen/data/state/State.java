package modelgen.data.state;

public abstract class State implements IState {
    public String signalName;
    public Integer stateId;
    public Double start, end;

    protected State(String name, Integer id, Double start, Double end) {
        this.signalName = name;
        this.stateId = id;
        this.start = start;
        this.end = end;
    }

}

package modelgen.data.state;

import modelgen.data.DataType;

public class StateDMV extends State implements IState {
    private Double value;

    public StateDMV (String name, Integer id, Double start, Double end, Double value) {
        super(name, id, start, end);
        this.value = value;
    }
    
//    StateDMV (String name, Integer id, Double value) {
//        super(name, id, -1.0, -1.0);
//        this.value = value;
//    }
    
    @Override
    public String convertToString() {
        return "[" + value.toString() + "]";
    }

    @Override
    public DataType getType() {
        return DataType.DMV;
    }

}

package modelgen.data.state;

class StateDMV extends State{
    private Double value;

    StateDMV (String name, Integer id, Double start, Double end, Double value) {
        super(name, id, start, end);
        this.value = value;
    }
    
    @Override
    public String convertToString() {
        return value.toString();
    }

    @Override
    public String getSignalName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getId() {
        // TODO Auto-generated method stub
        return null;
    }
}
